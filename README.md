# TamilDhool Cloudstream Extension

This extension allows you to watch Tamil serials and shows from TamilDhool.tech in Cloudstream.

## Features

- **TV Channels**: Sun TV, Vijay TV, Zee Tamil, Kalaignar TV
- **Content Categories**: 
  - Serials
  - Reality Shows  
  - Movies
- **Episode Information**: Title, date, and video links
- **Video Quality**: Supports multiple quality options (HD, SD)
- **Search**: Find specific shows and episodes

## Installation

### Method 1: Direct Installation
1. Download the latest `.cs3` extension file from the releases
2. Open Cloudstream app
3. Go to Settings → Extensions
4. Click the "+" button and select the downloaded file
5. Enable the TamilDhool extension

### Method 2: Repository Installation
1. In Cloudstream, go to Settings → Extensions → Extension Repositories
2. Add this repository URL: `https://your-repo-url/extensions`
3. Find "TamilDhool" in the list and install

## Usage

1. Open Cloudstream
2. You'll see "TamilDhool" in your provider list
3. Browse by:
   - **Home**: Latest episodes and trending shows
   - **TV Channels**: Browse by Sun TV, Vijay TV, Zee Tamil, etc.
   - **Categories**: Filter by Serials, Reality Shows, Movies
   - **Search**: Find specific content

## Supported Content

### Channels
- **Sun TV**: Popular serials like Nandini, Vani Rani
- **Vijay TV**: Shows like Cooku with Comali, Super Singer
- **Zee Tamil**: Serials like Karthigai Deepam, Tamil shows
- **Kalaignar TV**: Regional content and serials

### Categories
- **Serials**: Daily soap operas and drama series
- **Reality Shows**: Music, dance, comedy shows
- **Movies**: Tamil movies and special programs

## Technical Details

- **Language**: Kotlin
- **Cloudstream Version**: 3.x+
- **Video Sources**: M3U8, MP4
- **Search**: Full-text search across all content
- **Updates**: Automatic episode updates

## Troubleshooting

### Common Issues

1. **Extension not loading**: 
   - Ensure you have the latest Cloudstream version
   - Check if the extension is enabled in settings

2. **Videos not playing**:
   - Try different quality options
   - Check your internet connection
   - Some content may be geo-restricted

3. **Search not working**:
   - Clear the app cache
   - Restart Cloudstream

### Support

If you encounter issues:
1. Check the [Issues](https://github.com/your-repo/issues) page
2. Create a new issue with details about the problem
3. Include your Cloudstream version and device information

## Development

### Building from Source

1. Clone this repository
2. Open in Android Studio or IntelliJ IDEA
3. Build using: `./gradlew build`
4. The extension file will be in `build/outputs/`

### Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## Legal Notice

This extension is for educational purposes only. Users are responsible for ensuring they have the right to access content through this extension. The extension developers do not host or distribute any copyrighted content.

## License

MIT License - see LICENSE file for details

## Changelog

### v1.0.0
- Initial release
- Support for Sun TV, Vijay TV, Zee Tamil
- Episode search and browsing
- Multiple video quality options

### v1.0.1 (Planned)
- Add Kalaignar TV support
- Improve search functionality
- Bug fixes and performance improvements
