package com.tamildhool

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URI

class TamilDhoolProvider : MainAPI() {
    override var mainUrl = "https://tamildhool.tech"
    override var name = "TamilDhool"
    override val hasMainPage = true
    override var lang = "ta"
    override val hasDownloadSupport = true
    override val hasQuickSearch = false
    override val supportedTypes = setOf(
        TvType.TvSeries,
        TvType.Movie,
        TvType.Others
    )

    companion object {
        const val SUN_TV = "Sun TV"
        const val VIJAY_TV = "Vijay TV"
        const val ZEE_TAMIL = "Zee Tamil"
        const val KALAIGNAR_TV = "Kalaignar TV"
    }

    override val mainPage = mainPageOf(
        "$mainUrl/sun-tv-programs/" to "Sun TV Shows",
        "$mainUrl/vijay-tv-programs/" to "Vijay TV Shows", 
        "$mainUrl/zee-tamil-programs/" to "Zee Tamil Shows",
        "$mainUrl/kalaignar-tv-programs/" to "Kalaignar TV Shows",
        "$mainUrl/latest-episodes/" to "Latest Episodes"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data + if (page > 1) "page/$page/" else "").document
        val home = document.select("div.post-item, div.episode-item, article.post").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home, hasNext = home.size >= 20)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h2 a, h3 a, .post-title a")?.text()?.trim() ?: return null
        val href = fixUrl(this.selectFirst("h2 a, h3 a, .post-title a")?.attr("href") ?: return null)
        val posterUrl = fixUrlNull(
            this.selectFirst("img")?.attr("data-src") 
            ?: this.selectFirst("img")?.attr("src")
        )
        val episode = this.selectFirst(".episode-number, .ep-num")?.text()
        val date = this.selectFirst(".post-date, .episode-date")?.text()
        
        return newTvSeriesSearchResponse(
            title + (episode?.let { " - $it" } ?: ""),
            href,
            TvType.TvSeries,
        ) {
            this.posterUrl = posterUrl
            this.posterHeaders = mapOf("Referer" to mainUrl)
            if (date != null) {
                this.year = extractYear(date)
            }
        }
    }

    private fun extractYear(dateString: String): Int? {
        val yearRegex = Regex("(20\\d{2})")
        return yearRegex.find(dateString)?.value?.toIntOrNull()
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchUrl = "$mainUrl/?s=${query.replace(" ", "+")}"
        val document = app.get(searchUrl).document
        
        return document.select("article.post, div.search-item").mapNotNull {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        
        val title = document.selectFirst("h1.entry-title, h1.post-title")?.text()?.trim() ?: return null
        val description = document.selectFirst(".post-content p, .entry-content p")?.text()?.trim()
        val poster = fixUrlNull(
            document.selectFirst("meta[property=og:image]")?.attr("content")
            ?: document.selectFirst("img.wp-post-image")?.attr("data-src")
            ?: document.selectFirst("img.wp-post-image")?.attr("src")
        )
        
        // Check if this is a series with multiple episodes
        val episodeElements = document.select("div.episode-list a, .episodes-list a")
        
        if (episodeElements.isNotEmpty()) {
            // This is a series with episodes
            val episodes = episodeElements.mapIndexedNotNull { index, element ->
                val episodeTitle = element.text().trim()
                val episodeUrl = fixUrl(element.attr("href"))
                val episodeNum = index + 1
                
                Episode(
                    data = episodeUrl,
                    name = episodeTitle,
                    episode = episodeNum,
                    posterUrl = poster,
                    description = description
                )
            }
            
            return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.plot = description
                this.posterHeaders = mapOf("Referer" to mainUrl)
            }
        } else {
            // This is a single episode or movie
            return newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = description
                this.posterHeaders = mapOf("Referer" to mainUrl)
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        
        // Look for video embeddings
        val iframes = document.select("iframe[src*=player], iframe[src*=embed]")
        val videoElements = document.select("video source, video")
        val scriptElements = document.select("script").map { it.data() }
        
        // Extract from iframes
        iframes.forEach { iframe ->
            val src = iframe.attr("src")
            if (src.isNotEmpty()) {
                loadExtractor(fixUrl(src), data, subtitleCallback, callback)
            }
        }
        
        // Extract direct video links
        videoElements.forEach { video ->
            val src = video.attr("src")
            if (src.isNotEmpty()) {
                callback.invoke(
                    ExtractorLink(
                        name,
                        name,
                        fixUrl(src),
                        referer = data,
                        quality = getVideoQuality(src),
                        isM3u8 = src.contains(".m3u8")
                    )
                )
            }
        }
        
        // Extract from JavaScript
        scriptElements.forEach { script ->
            extractFromScript(script, data, callback)
        }
        
        return true
    }

    private fun getVideoQuality(url: String): Int {
        return when {
            url.contains("1080") -> Qualities.P1080.value
            url.contains("720") -> Qualities.P720.value  
            url.contains("480") -> Qualities.P480.value
            url.contains("360") -> Qualities.P360.value
            else -> Qualities.Unknown.value
        }
    }

    private suspend fun extractFromScript(
        script: String, 
        referer: String, 
        callback: (ExtractorLink) -> Unit
    ) {
        // Common patterns for video URLs in JavaScript
        val patterns = listOf(
            Regex("file\\s*:\\s*[\"'](.*?)[\"']"),
            Regex("src\\s*:\\s*[\"'](.*?)[\"']"),
            Regex("source\\s*:\\s*[\"'](.*?)[\"']"),
            Regex("[\"'](https?://.*?\\.(?:m3u8|mp4|mkv|avi).*?)[\"']")
        )
        
        patterns.forEach { pattern ->
            pattern.findAll(script).forEach { match ->
                val url = match.groupValues[1]
                if (url.isNotEmpty() && (url.contains(".m3u8") || url.contains(".mp4"))) {
                    callback.invoke(
                        ExtractorLink(
                            name,
                            name,
                            url,
                            referer = referer,
                            quality = getVideoQuality(url),
                            isM3u8 = url.contains(".m3u8")
                        )
                    )
                }
            }
        }
    }
}
