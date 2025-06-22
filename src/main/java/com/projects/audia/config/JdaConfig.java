package com.projects.audia.config;


import com.projects.audia.listeners.MessageListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;

@Configuration
public class JdaConfig {
	@Value("${dev.token}")
	private String devToken;

	@Bean
	JDA initializeJda(MessageListener messageListener){
		EnumSet<GatewayIntent> intents = EnumSet.of(
				// We need messages in guilds to accept commands from users
				GatewayIntent.GUILD_MESSAGES,
				// We need voice states to connect to the voice channel
				GatewayIntent.GUILD_VOICE_STATES,
				// Enable access to message.getContentRaw()
				GatewayIntent.MESSAGE_CONTENT,
				// Enable emojis
				GatewayIntent.GUILD_EXPRESSIONS,GatewayIntent.SCHEDULED_EVENTS
		);

		return JDABuilder
				.createDefault(devToken, intents)
				.addEventListeners(messageListener)
				.setActivity(Activity.listening("doing my best :)"))
				.setStatus(OnlineStatus.DO_NOT_DISTURB)
				.enableCache(CacheFlag.VOICE_STATE) //To find a user's state in voice channel
				.build();
	}
}
