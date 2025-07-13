package com.projects.audia.utils;

import com.projects.audia.config.SongsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SongUtils {

	private static final double THRESHOLD = 0.80; // similarity threshold
	private final SongsConfig songsConfig;

	/**
	 * List all the files with .mp3 extension in the configured songs directory
	 *
	 * @param root songs directory file
	 * @return List of mp3 files
	 */
	private static List<File> listAllMp3Files(File root) {
		return Arrays.stream(Objects.requireNonNull(root.listFiles((file) -> file.getName().toLowerCase().endsWith(".mp3")))).toList();
	}

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
			double score = similarityAlgo.apply(fileNameWithoutExt.toLowerCase().replace("_", " "), songName.toLowerCase().replace("_", " "));
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

	public String fetchSongTitleFromYoutube(String searchTerm, boolean isUrl) {
		ProcessBuilder processBuilder = initializeProcessBuilderToFetchTitle(searchTerm, isUrl);
		Process process;
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				// Print output
				log.info("[yt-dlp] " + line);
				if (line.contains("filename ")) {
					int idx = line.indexOf("filename ") + "filename ".length();
					return line.substring(idx).trim();
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return null;
	}

	private ProcessBuilder initializeProcessBuilderToFetchTitle(String searchTerm, boolean isUrl) {
		List<String> command = new ArrayList<>();
		command.add(songsConfig.getYtDlpPath());
		//Restrict file names to ASCII characters
		command.add("--restrict-filenames");
		command.add("-o");
		command.add("filename %(title)s");
		command.add("--print");
		command.add("filename");
		if (isUrl) {
			command.add(searchTerm);
		} else {
			command.add("ytsearch:" + searchTerm);
		}

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(new File(System.getProperty("user.dir")));
		processBuilder.redirectErrorStream(true);

		return processBuilder;
	}

	public String downloadSong(String songName, boolean isUrl) {
		String savedPath = null;
		ProcessBuilder processBuilder = initializeProcessBuilderForDownload(songName, isUrl);

		Process process;
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				// Print output
				log.info("[yt-dlp] " + line);
				if (line.contains("Destination:")) {
					int idx = line.indexOf("Destination:") + "Destination:".length();
					savedPath = line.substring(idx).trim();
				}
			}

			int exitCode = process.waitFor();
			if (exitCode == 0) {
				log.info("Download completed successfully.");
			} else {
				log.error("yt-dlp exited with code: " + exitCode);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		if (StringUtils.hasText(savedPath)) {
			String[] filename = savedPath.split("\\\\");
			return filename[filename.length - 1];
		}

		return null;
	}

	@NotNull
	private ProcessBuilder initializeProcessBuilderForDownload(String songName, boolean isUrl) {
		List<String> command = new ArrayList<>();
		command.add(songsConfig.getYtDlpPath());
		command.add("-x");
		command.add("--audio-format");
		command.add("mp3");
		command.add(isUrl ? songName : ("ytsearch:" + songName));

		command.add("-o");
		if (isUrl) {
			command.add("%(title)s.%(ext)s");
		} else {
			command.add(GenericUtils.toTitleCase(songName));
		}
		command.add("-P");
		command.add(songsConfig.getSongsDirectory());
		//Restrict file names to ASCII characters
		command.add("--restrict-filenames");

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(new File(System.getProperty("user.dir")));
		processBuilder.redirectErrorStream(true);


		return processBuilder;
	}


}
