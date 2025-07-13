package com.projects.audia.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "command")
@Getter
public class CommandConfig {
	@Getter
	private final Set<String> musicCommands = new HashSet<>();
	@Value("${prefix:!}")
	private String prefix;
	@Value("${play.music:play}")
	private String playMusicCommand;

	@Value("${stop.music:stop}")
	private String stopMusicCommand;

	@Value("${disconnect:leave}")
	private String disconnectCommand;

	@Value("${skip.song:skip}")
	private String skipSongCommand;

	@PostConstruct
	public void initMusicCommands() {
		//Play a song
		musicCommands.add(playMusicCommand);
		//Stop the queue
		musicCommands.add(stopMusicCommand);
		//Stop the queue and disconnect from the voice channel
		musicCommands.add(disconnectCommand);
		//Immediately play the next song
		musicCommands.add(skipSongCommand);
	}
}
