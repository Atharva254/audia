package com.projects.audia.handlers;

import com.projects.audia.components.GuildMusicManager;
import com.projects.audia.constants.EmojiConstants;
import com.projects.audia.utils.GenericUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

@Slf4j
@RequiredArgsConstructor
public class AudioResultHandler implements AudioLoadResultHandler {

	private final GuildMusicManager musicManager;
	private final MessageChannel messageChannel;
	private final String songName;

	@Override
	public void trackLoaded(AudioTrack track) {
		musicManager.scheduler.queue(track);
		messageChannel.sendMessage("Playing " + EmojiConstants.EMOJI_MUSIC_SIGN + "`" + GenericUtils.toTitleCase(songName) + "`" + EmojiConstants.EMOJI_MUSIC_SIGN).queue();
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		AudioTrack firstTrack = playlist.getSelectedTrack() != null ? playlist.getSelectedTrack() : playlist.getTracks().getFirst();

		musicManager.scheduler.queue(firstTrack);
		messageChannel.sendMessage("Queued from playlist: **" + firstTrack.getInfo().title + "**").queue();
	}

	@Override
	public void noMatches() {
		messageChannel.sendMessage("No matches found for " + songName).queue();
		log.error("No matches found for: " + songName);
	}

	@Override
	public void loadFailed(FriendlyException exception) {
		log.error("Exception loading:" + exception.getMessage());
	}
}
