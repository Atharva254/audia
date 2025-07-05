package com.projects.audia.utils;


import java.util.Arrays;
import java.util.stream.Collectors;

public class GenericUtils {
	public static String toTitleCase(String songName) {
		return Arrays.stream(songName.toLowerCase().split(" "))
				.filter(word -> !word.isBlank())
				.map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
				.collect(Collectors.joining(" "));
	}
}
