package com.projects.audia.utils;

import com.projects.audia.config.LocalSongsConfig;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SongUtils {

	private final LocalSongsConfig localSongsConfig;
	private static final double THRESHOLD = 0.85; // similarity threshold

	/**
	 * Search the song by name in the configured local directory.
	 * Current implementation is based on Jaro Winkler similarity algorithm
	 * @param songName name of the song to be searched
	 * @return Path of the song if found; null if not found
	 */
	public String searchSongInLocalDir(String songName) {
		File dir = new File(localSongsConfig.getSongsDirectory());
		if (!dir.exists() || !dir.isDirectory()) {
			System.err.println("Invalid directory: " + localSongsConfig.getSongsDirectory());
			return null;
		}

		List<File> songFiles = listAllMp3Files(dir);
		Map<File, Double> scoreMap = new HashMap<>();
		JaroWinklerSimilarity similarityAlgo = new JaroWinklerSimilarity();

		for (File file : songFiles) {
			String fileNameWithoutExt = file.getName().replaceAll("(?i)\\.mp3$", "");
			double score = similarityAlgo.apply(fileNameWithoutExt, songName);
			// Immediately return the file if it is a perfect match
			if(score == 1){
				return file.getAbsolutePath();
			}

			System.out.println("Score for: "+ fileNameWithoutExt + " : " + score);
			scoreMap.put(file, score);
		}

		Optional<Entry<File, Double>> bestMatch = scoreMap.entrySet().stream().max(Entry.comparingByValue());

		if (bestMatch.isPresent()) {
			File bestFile = bestMatch.get().getKey();
			double score = bestMatch.get().getValue();

			if (score >= THRESHOLD) {
				System.out.println("Matched: " + bestFile.getName() + " with score " + score);
				return bestFile.getAbsolutePath();
			}
		}

		return null;
	}

	/**
	 * List all the files with .mp3 extension in the configured songs directory
	 * @param root songs directory file
	 * @return List of mp3 files
	 */
	private static List<File> listAllMp3Files(File root) {
		return Arrays.stream(Objects.requireNonNull(root
				.listFiles((file) -> file.getName().toLowerCase().endsWith(".mp3"))))
				.toList();
	}
}
