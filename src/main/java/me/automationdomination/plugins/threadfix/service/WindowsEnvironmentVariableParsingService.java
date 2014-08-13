package me.automationdomination.plugins.threadfix.service;

import hudson.EnvVars;
import java.io.PrintStream;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WindowsEnvironmentVariableParsingService implements EnvironmentVariableParsingService {

	private final Pattern environmentVariablePattern = Pattern.compile("%.+?%");

	@Override
	public String parseEnvironentVariables(final EnvVars envVars, final String value, PrintStream log) {
		final Matcher matcher = this.environmentVariablePattern.matcher(value);

		String parsedValue = value;

		while (matcher.find()) {
			final String matchedValue = matcher.group();
			log.println("matchedValue: " + matchedValue);

			// TODO: can this be done more efficiently?
			final String environmentVariableKey = matchedValue.replaceAll("%", "");
			//
			log.println("environmentVariableKey: " + environmentVariableKey);

			String environmentVariableValue = envVars.get(environmentVariableKey);
			//
			log.println("environmentVariableValue: " + environmentVariableValue);

			// if this is null, that means the environment variable was not found
			if (environmentVariableValue != null) {
				environmentVariableValue.replaceAll("\\\\", "\\\\\\\\\\");

				// TODO: can this be done more efficiently?
				parsedValue = parsedValue.replaceAll("%" + environmentVariableKey + "%", environmentVariableValue);
				//
				log.println("parsedValue: " + parsedValue);
			}
		}

		return parsedValue;
	}

}
