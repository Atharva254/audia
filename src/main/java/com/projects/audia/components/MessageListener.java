package com.projects.audia.components;


import com.projects.audia.config.CommandConfig;
import com.projects.audia.constants.EmojiConstants;
import com.projects.audia.handlers.MusicHandler;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class MessageListener extends ListenerAdapter {
	private final CommandConfig commandConfig;

	private final MusicHandler musicHandler;

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		// Ignore if message is from a bot or not in a guild
		if (event.getAuthor().isBot() || !event.isFromGuild()) return;

		String content = event.getMessage().getContentRaw();
		if (!content.startsWith(commandConfig.getPrefix())) return;

		//Command without prefix
		String input = content.substring(commandConfig.getPrefix().length());
		MessageChannel messageChannel = event.getChannel();

		if (commandConfig.getMusicCommands().contains(input.split(" ")[0])) {
			Member member = event.getMember();
			GuildVoiceState voiceState = Objects.requireNonNull(member).getVoiceState();
			AudioChannel audioChannel = Objects.requireNonNull(voiceState).getChannel();
			if (audioChannel == null) {
				messageChannel.sendMessage("You must be connected to a voice channel to play music!" + EmojiConstants.EMOJI_MALE_FACEPALM).queue();
				return;
			}
			Guild guild = audioChannel.getGuild();
			if (input.startsWith(commandConfig.getPlayMusicCommand())) {
				String songName = input.substring(commandConfig.getPlayMusicCommand().length() + 1);
				musicHandler.handlePlayMusicRequest(messageChannel, audioChannel, songName);
			} else if (input.equalsIgnoreCase(commandConfig.getStopMusicCommand())) {
				musicHandler.handleStopMusicRequest(messageChannel, audioChannel);
			} else if (input.equalsIgnoreCase(commandConfig.getDisconnectCommand())) {
				musicHandler.handleStopAndDisconnect(guild, messageChannel);
			} else if (input.equalsIgnoreCase(commandConfig.getSkipSongCommand())) {
				musicHandler.handleSkipMusicRequest(guild, messageChannel);
			}
		} else {
			messageChannel.sendMessage("Idk what to do with this" + EmojiConstants.EMOJI_SKULL).queue();
		}
	}
}
