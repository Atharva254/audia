package com.projects.audia.utils;

import com.projects.audia.config.SongsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SongUtils {

	private final SongsConfig songsConfig;
	private static final double THRESHOLD = 0.85; // similarity threshold

	/**
	 * Search the song by name in the configured local directory.
	 * Current implementation is based on Jaro Winkler similarity algorithm
	 *
	 * @param songName name of the song to be searched
	 * @return Path of the song if found; null if not found
	 */
	public String searchSongInLocalDir(String songName) {
		File dir = new File(songsConfig.getSongsDirectory());
		if (!dir.exists() || !dir.isDirectory()) {
			log.error("Invalid directory: " + songsConfig.getSongsDirectory());
			return null;
		}

		List<File> songFiles = listAllMp3Files(dir);
		Map<File, Double> scoreMap = new HashMap<>();
		JaroWinklerSimilarity similarityAlgo = new JaroWinklerSimilarity();

		for (File file : songFiles) {
			String fileNameWithoutExt = file.getName().replaceAll("(?i)\\.mp3$", "");
			double score = similarityAlgo.apply(fileNameWithoutExt.toLowerCase(), songName.toLowerCase());
			// Immediately return the file if it is a perfect match
			if (score == 1) {
				return file.getAbsolutePath();
			}

			log.info("Score for: " + fileNameWithoutExt + " : " + score);
			scoreMap.put(file, score);
		}

		Optional<Entry<File, Double>> bestMatch = scoreMap.entrySet().stream().max(Entry.comparingByValue());

		if (bestMatch.isPresent()) {
			File bestFile = bestMatch.get().getKey();
			double score = bestMatch.get().getValue();

			if (score >= THRESHOLD) {
				log.info("Matched: " + bestFile.getName() + " with score " + score);
				return bestFile.getAbsolutePath();
			}
		}

		log.error("No match found in local directory for " + songName);
		return null;
	}

	/**
	 * List all the files with .mp3 extension in the configured songs directory
	 *
	 * @param root songs directory file
	 * @return List of mp3 files
	 */
	private static List<File> listAllMp3Files(File root) {
		return Arrays.stream(Objects.requireNonNull(root.listFiles((file) -> file.getName().toLowerCase().endsWith(".mp3")))).toList();
	}

	public boolean downloadSong(String songName) {
		ProcessBuilder processBuilder = initializProcessBuilder(songName);

		Process process;
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// Print output
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				log.info("[yt-dlp] " + line);
			}

			int exitCode = process.waitFor();
			if (exitCode == 0) {
				log.info("Download completed successfully.");
			} else {
				log.error("yt-dlp exited with code: " + exitCode);
				return false;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		}
		File songsDir = new File(System.getProperty("user.dir"), songsConfig.getSongsDirectory());

		// Filter .mp3 files
		File[] mp3Files = songsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));

		if (mp3Files == null || mp3Files.length == 0) {
			log.error("downloadSong() >> No MP3 files found in the Songs directory.");
			return false;
		}

		return true;
	}

	@NotNull
	private ProcessBuilder initializProcessBuilder(String songName) {
		// Title-case the song name
		String titleCasedSongName = GenericUtils.toTitleCase(songName);

		String outputFileName = titleCasedSongName + ".%(ext)s";

		List<String> command = List.of(songsConfig.getYtDlpPath(),
				"-x",
				"--audio-format",
				"mp3",
				"ytsearch:" + songName,
				"-o", titleCasedSongName,
				"-P",
				songsConfig.getSongsDirectory());

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(new File(System.getProperty("user.dir")));
		processBuilder.redirectErrorStream(true);
		return processBuilder;
	}


}
