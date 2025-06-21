package com.projects.audia.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@Getter
@Slf4j
public class LocalSongsConfig {
	@Value("${songs.directory:Songs}")
	private String songsDirectory;

	@Value("${local.search.similarity.threshold:0.85}")
	private double localSearchSimilarityThreshold;

	@PostConstruct
	private void initDir(){
		File dir = new File(songsDirectory);
		if(!dir.exists() || !dir.isDirectory()){
			log.info("Configured songs directory not found. Creating it.");
			boolean mkdir = dir.mkdir();

			if(mkdir){
			log.info("Created songs directory at configured path");
			}else {
				log.warn("Couldn't create Songs directory at specified path. Check permissions and try again.");
			}
		}
	}
}
