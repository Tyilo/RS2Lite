package com.tyilo.GitHubLatestRelease;

import java.util.Scanner;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubLatestRelease
{
	public static String getLatest(String url)
	{
		try {
        	String contents = new Scanner(new URL(url).openStream(), "UTF-8").useDelimiter("\\A").next();
			
			Pattern pattern = Pattern.compile("class=\"tag-name\">([^<]*)<");
			Matcher tagMatcher = pattern.matcher(contents);
			tagMatcher.find();
			String tag = tagMatcher.group(1);

			return tag;
		} catch(Exception e) {
		}

		return null;
	}
}
