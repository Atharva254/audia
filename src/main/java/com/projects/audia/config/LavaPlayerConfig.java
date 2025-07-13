package com.projects.audia.config;

import com.projects.audia.handlers.MyAudioSendHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LavaPlayerConfig {

	@Bean
	public AudioPlayerManager audioPlayerManager() {
		AudioPlayerManager manager = new DefaultAudioPlayerManager();
		manager.setFrameBufferDuration(5000);
		manager.registerSourceManager(new LocalAudioSourceManager()); // Enable local file support
		return manager;
	}

	@Bean
	public AudioPlayer audioPlayer(AudioPlayerManager playerManager) {
		return playerManager.createPlayer();
	}

	@Bean
	public MyAudioSendHandler myAudioSendHandler(AudioPlayer audioPlayer) {
		return new MyAudioSendHandler(audioPlayer);
	}
}

