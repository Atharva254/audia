package com.projects.audia.utils;


import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GenericUtils {
	private static final Pattern URL_PATTERN = Pattern.compile(
			"^(https?|ftp)://[\\w.-]+(?:.[\\w.-]+)+[/\\\\w\\-._~:/?#[\\\\]@!$&'()*+,;=.]*$",
			Pattern.CASE_INSENSITIVE
	);

	public static String toTitleCase(String songName) {
		return Arrays.stream(songName.toLowerCase().split(" "))
				.filter(word -> !word.isBlank())
				.map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
				.collect(Collectors.joining(" "));
	}

	public static boolean isUrlFormat(String url) {
		return url != null && URL_PATTERN.matcher(url).matches();
	}
}
