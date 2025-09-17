package com.tamildhool

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

// Custom extractors for TamilDhool specific video sources
class TamilDhoolExtractor : ExtractorApi() {
    override val name = "TamilDhool"
    override val mainUrl = "https://tamildhool.tech"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val document = app.get(url, referer = referer).document
        
        // Extract video sources
        document.select("source").forEach { source ->
            val videoUrl = source.attr("src")
            if (videoUrl.isNotEmpty()) {
                callback.invoke(
                    ExtractorLink(
                        name,
                        name,
                        videoUrl,
                        referer ?: mainUrl,
                        getQualityFromUrl(videoUrl),
                        videoUrl.contains(".m3u8")
                    )
                )
            }
        }
    }

    private fun getQualityFromUrl(url: String): Int {
        return when {
            url.contains("1080") -> Qualities.P1080.value
            url.contains("720") -> Qualities.P720.value
            url.contains("480") -> Qualities.P480.value
            url.contains("360") -> Qualities.P360.value
            else -> Qualities.Unknown.value
        }
    }
}

// Additional extractor for common embedded players
class TamilPlayerExtractor : ExtractorApi() {
    override val name = "TamilPlayer"
    override val mainUrl = "https://player.tamildhool.tech"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        try {
            val response = app.get(url, referer = referer)
            val script = response.document.select("script").joinToString { it.data() }
            
            // Extract M3U8 URLs
            val m3u8Regex = Regex("file\\s*:\\s*[\"'](.*?\\.m3u8.*?)[\"']")
            m3u8Regex.findAll(script).forEach { match ->
                val m3u8Url = match.groupValues[1]
                callback.invoke(
                    ExtractorLink(
                        name,
                        "$name M3U8",
                        m3u8Url,
                        referer ?: mainUrl,
                        Qualities.Unknown.value,
                        true
                    )
                )
            }
            
            // Extract MP4 URLs
            val mp4Regex = Regex("file\\s*:\\s*[\"'](.*?\\.mp4.*?)[\"']")
            mp4Regex.findAll(script).forEach { match ->
                val mp4Url = match.groupValues[1]
                callback.invoke(
                    ExtractorLink(
                        name,
                        "$name MP4",
                        mp4Url,
                        referer ?: mainUrl,
                        getQualityFromUrl(mp4Url),
                        false
                    )
                )
            }
        } catch (e: Exception) {
            // Handle extraction errors gracefully
        }
    }

    private fun getQualityFromUrl(url: String): Int {
        return when {
            url.contains("1080") || url.contains("hd") -> Qualities.P1080.value
            url.contains("720") -> Qualities.P720.value
            url.contains("480") || url.contains("sd") -> Qualities.P480.value
            url.contains("360") -> Qualities.P360.value
            else -> Qualities.Unknown.value
        }
    }
}
