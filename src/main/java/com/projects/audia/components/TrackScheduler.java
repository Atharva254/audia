package com.projects.audia.components;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class TrackScheduler extends AudioEventAdapter {
	private final AudioPlayer player;
	@Getter
	private final BlockingQueue<AudioTrack> queue;

	public TrackScheduler(AudioPlayer player) {
		this.player = player;
		queue = new LinkedBlockingQueue<>();
	}

	public void queue(AudioTrack track) {
		if (!player.startTrack(track, true)) {
			log.info("");
			queue.offer(track);
		}
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) {
			player.startTrack(queue.poll(), false);
		}
	}

	public void clearQueue() {
		queue.clear();
	}
}

