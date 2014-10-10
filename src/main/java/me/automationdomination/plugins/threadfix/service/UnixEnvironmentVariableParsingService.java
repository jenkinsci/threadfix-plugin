package me.automationdomination.plugins.threadfix.service;

import hudson.EnvVars;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class environment variables on non-Windows platforms
 */
public class UnixEnvironmentVariableParsingService implements EnvironmentVariableParsingService {

	private static final Pattern environmentVariablePattern = Pattern.compile("\\$\\{.+?\\}");

    @Override
	public String parse(final EnvVars envVars, String value) {
		final Matcher matcher = environmentVariablePattern.matcher(value);
		
		String parsedValue = value;
		
		while (matcher.find()) {
			final String matchedValue = matcher.group();
			
			// TODO: can this be done more efficiently?
			final String environmentVariableKey = matchedValue.replaceAll("\\$\\{", "").replaceAll("\\}", "");
			
			final String environmentVariableValue = envVars.get(environmentVariableKey);
			
			// if this is null, that means the environment variable was not found
			if (environmentVariableValue != null) {
				// TODO: can this be done more efficiently?
				parsedValue = parsedValue.replaceAll("\\$\\{" + environmentVariableKey + "\\}", environmentVariableValue);
			}
		}
		return parsedValue;
	}

}
