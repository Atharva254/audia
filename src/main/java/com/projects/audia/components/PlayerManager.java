package com.projects.audia.components;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
	private static PlayerManager INSTANCE;
	@Getter
	private final AudioPlayerManager playerManager;
	private final Map<Long, GuildMusicManager> musicManagers;

	public PlayerManager() {
		this.musicManagers = new HashMap<>();
		this.playerManager = new DefaultAudioPlayerManager();
		this.playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
		AudioSourceManagers.registerLocalSource(playerManager);
	}

	public static synchronized PlayerManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PlayerManager();
		}
		return INSTANCE;
	}

	public synchronized GuildMusicManager getGuildMusicManager(Guild guild) {
		long guildId = guild.getIdLong();
		GuildMusicManager musicManager = musicManagers.get(guildId);

		if (musicManager == null) {
			musicManager = new GuildMusicManager(playerManager);
			musicManagers.put(guildId, musicManager);
		}

		guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

		return musicManager;
	}
}

