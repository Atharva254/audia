package com.projects.audia.handlers;


import com.projects.audia.components.GuildMusicManager;
import com.projects.audia.components.PlayerManager;
import com.projects.audia.constants.EmojiConstants;
import com.projects.audia.utils.SongUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.stereotype.Component;

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
			boolean songPlayed = findAndPlaySong(messageChannel, audioChannel, songName);
			if (songPlayed) {
				messageChannel.sendMessage("Playing " + songName + " in " + audioChannel.getName() + EmojiConstants.EMOJI_SUNGLASSES).queue();
			}
		} catch (IOException | InterruptedException e) {
			messageChannel.sendMessage("Couldn't connect to " + audioChannel.getName() + EmojiConstants.EMOJI_SAD_FACE).queue();
			System.out.println(e.getMessage());
		}
	}

	public void handleStopMusicRequest(MessageChannel messageChannel, AudioChannel audioChannel) {
		System.out.println("Entered handle music request");
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
			messageChannel.sendMessage("Bye bye" + EmojiConstants.EMOJI_SALUTE).queue();
		} else {
			messageChannel.sendMessage("I'm literally not even connected " + EmojiConstants.EMOJI_HANDS_ON_HEAD).queue();
		}
	}

	/**
	 * Search, download and play the requested song in the audio channel
	 *
	 * @param messageChannel message channel
	 * @param audioChannel   audio channel to play the song in
	 * @param songName       name of the song
	 * @return boolean describing success or failure in playing the song
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private boolean findAndPlaySong(MessageChannel messageChannel, AudioChannel audioChannel, String songName) throws IOException, InterruptedException {
		//Searching song in local directory first
		String path = songUtils.searchSongInLocalDir(songName);

		Guild guild = audioChannel.getGuild();
		AudioManager audioManager = guild.getAudioManager();
		audioManager.openAudioConnection(audioChannel);

		// Get player
		PlayerManager playerManager = PlayerManager.getInstance();
		GuildMusicManager musicManager = playerManager.getGuildMusicManager(guild);

		String searchSource = (path != null) ? path : "ytsearch:" + songName;

		log.info("Song search source:" + searchSource);

		playerManager.getPlayerManager().loadItem(searchSource, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				musicManager.scheduler.queue(track);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack() != null
						? playlist.getSelectedTrack()
						: playlist.getTracks().getFirst();

				musicManager.scheduler.queue(firstTrack);
				messageChannel.sendMessage("Queued from playlist: **" + firstTrack.getInfo().title + "**").queue();
			}

			@Override
			public void noMatches() {
				System.err.println("No matches found for: " + path);
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				System.out.println("Exception loading:" + exception.getMessage());
			}
		});
		return true;
	}

	public void handleSkipMusicRequest(Guild guild, MessageChannel messageChannel) {
		GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(guild);
		AudioTrack nextTrack = musicManager.scheduler.getQueue().peek();
		musicManager.player.stopTrack();
		messageChannel.sendMessage("⏭️ Skipped to: " + (nextTrack != null ? nextTrack.getInfo().title : "End of queue")).queue();
	}
}
