package com.projects.audia.components;
import com.projects.audia.handlers.MyAudioSendHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager {
	public final AudioPlayer player;
	public final TrackScheduler scheduler;

	public GuildMusicManager(AudioPlayerManager manager) {
		this.player = manager.createPlayer();
		this.scheduler = new TrackScheduler(this.player);
		this.player.addListener(this.scheduler);
	}

	public MyAudioSendHandler getSendHandler() {
		return new MyAudioSendHandler(player);
	}
}

