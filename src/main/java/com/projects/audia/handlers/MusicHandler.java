package com.projects.audia.handlers;


import com.projects.audia.components.GuildMusicManager;
import com.projects.audia.components.PlayerManager;
import com.projects.audia.constants.EmojiConstants;
import com.projects.audia.utils.GenericUtils;
import com.projects.audia.utils.SongUtils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class MusicHandler {

	private final SongUtils songUtils;

	/**
	 * Handle incoming request to play a song
	 *
	 * @param messageChannel the text channel that the message was sent in
	 * @param audioChannel   the voice channel to play the song in
	 * @param songName       name of the requested song
	 */
	public void handlePlayMusicRequest(MessageChannel messageChannel, AudioChannel audioChannel, String songName) {
		try {
			findAndPlaySong(messageChannel, audioChannel, songName);
		} catch (IOException | InterruptedException e) {
			messageChannel.sendMessage("Couldn't connect to " + audioChannel.getName() + EmojiConstants.EMOJI_SAD_FACE).queue();
			log.error(e.getMessage());
		}
	}

	public void handleStopMusicRequest(MessageChannel messageChannel, AudioChannel audioChannel) {
		Guild guild = audioChannel.getGuild();
		PlayerManager playerManager = PlayerManager.getInstance();
		GuildMusicManager musicManager = playerManager.getGuildMusicManager(guild);

		if (musicManager.player.getPlayingTrack() != null) {
			musicManager.player.stopTrack();
			messageChannel.sendMessage("Stopped the music" + EmojiConstants.EMOJI_RAISED_HAND).queue();
		} else {
			messageChannel.sendMessage("No song is currently playing in " + audioChannel.getName() + " 🙁").queue();
		}
	}

	public void handleStopAndDisconnect(Guild guild, MessageChannel messageChannel) {
		PlayerManager playerManager = PlayerManager.getInstance();
		GuildMusicManager musicManager = playerManager.getGuildMusicManager(guild);

		musicManager.player.stopTrack();

		// 2. Disconnect bot from voice channel
		AudioManager audioManager = guild.getAudioManager();
		if (audioManager.isConnected()) {
			audioManager.closeAudioConnection();
			messageChannel.sendMessage("Tatataaa" + EmojiConstants.EMOJI_BYE + "🥰").queue();
		} else {
			messageChannel.sendMessage("I'm literally not even connected " + EmojiConstants.EMOJI_HANDS_ON_HEAD).queue();
		}
	}

	/**
	 * Search, download and play the requested song in the audio channel
	 *
	 * @param messageChannel message channel
	 * @param audioChannel   audio channel to play the song in
	 * @param searchTerm     name of the song
	 * @throws IOException          IO exception
	 * @throws InterruptedException Interrupt exception
	 */
	private void findAndPlaySong(MessageChannel messageChannel, AudioChannel audioChannel, String searchTerm) throws IOException, InterruptedException {
		String path;
		String songName = GenericUtils.isUrlFormat(searchTerm) ? songUtils.fetchSongTitleFromYoutube(searchTerm, true) : songUtils.fetchSongTitleFromYoutube(searchTerm, false);

		if (!StringUtils.hasText(songName)) {
			//Couldn't find URL
			messageChannel.sendMessage("Couldn't find the song" + EmojiConstants.EMOJI_SAD_FACE).queue();
			return;
		}

		path = songUtils.searchSongInLocalDir(songName);

		if (path == null) {
			messageChannel.sendMessage("Hold up!" + EmojiConstants.EMOJI_RAISED_HAND + "Getting it" + EmojiConstants.EMOJI_FOCUS + "....").queue();
			String savedFileName = songUtils.downloadSong(songName, false);

			if (!StringUtils.hasText(savedFileName)) {
				messageChannel.sendMessage("Something went wrong" + EmojiConstants.EMOJI_CROSS_EYES).queue();
				return;
			} else {
				messageChannel.sendMessage("....saved!").queue();
				path = songUtils.searchSongInLocalDir(savedFileName);
			}
		}

		if (path == null) {
			messageChannel.sendMessage("Couldn't find this one" + EmojiConstants.EMOJI_SLEEPY_FACE).queue();
			return;
		}

		Guild guild = audioChannel.getGuild();
		AudioManager audioManager = guild.getAudioManager();
		audioManager.openAudioConnection(audioChannel);

		// Get player
		PlayerManager playerManager = PlayerManager.getInstance();
		GuildMusicManager musicManager = playerManager.getGuildMusicManager(guild);

		log.info("Song search source:" + searchTerm);

		playerManager.getPlayerManager().

				loadItem(path, new AudioResultHandler(musicManager, messageChannel, songName));
	}

	/**
	 * To skip currently playing song
	 *
	 * @param guild          guild
	 * @param messageChannel the text channel that the message was sent in
	 */
	public void handleSkipMusicRequest(Guild guild, MessageChannel messageChannel) {
		GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(guild);
		AudioTrack nextTrack = musicManager.scheduler.getQueue().poll();
		musicManager.player.stopTrack();
		if (nextTrack == null) {
			messageChannel.sendMessage("End of queue!" + EmojiConstants.EMOJI_STOP_SIGN).queue();
		} else {
			musicManager.player.startTrack(nextTrack, true);
			messageChannel.sendMessage("Skipping to next!" + EmojiConstants.EMOJI_FORWARD).queue();
		}
	}
}
