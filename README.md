# Audia
**A discord music bot**

## Features
* Easy to set up
* Supports songs in local directory
* Support for queuing songs
* No external keys needed (Except the Discord token)
* Smooth playback
* Supports many sites, including Youtube

## Supported sources and formats
Audia supports all sources and formats supported by [lavaplayer](https://github.com/lavalink-devs/lavaplayer).
### Sources
* YouTube
* SoundCloud
* Bandcamp
* Vimeo
* Twitch streams
* Local files
* HTTP URLs
### Formats
* MP3
* FLAC
* WAV
* Matroska/WebM (AAC, Opus or Vorbis codecs)
* MP4/M4A (AAC codec)
* OGG streams (Opus, Vorbis and FLAC codecs)
* AAC streams
* Stream playlists (M3U and PLS)

## Setup
* Install java 21+
* Go to discord developer's portal, create an application and create a dev token
* Go to the `Bot` section and enable the following intents:
  * Presence Intent
  * Server Members Intent
  * Message Content Intent
* Go to `Oauth2` section, scroll down to the `URL generator` and select the scope `bot`. In the `Bot permissions` section, select `Administrator`
* Paste that token in the application.properties file
* For local songs, either paste the songs in the Songs directory in the base folder or update the `songs.directory` property and point it to your local songs folder. 