package me.automationdomination.plugins.threadfix.validation;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiKeyStringValidator implements ConfigurationValueValidator, Serializable {

    private static final long serialVersionUID = 3042514933290226124L;

    // TODO: validate this pattern
	private final String TOKEN_PATTERN = "^[A-Za-z0-9]{40,}$";
	private final Pattern apiKeyPattern = Pattern.compile(TOKEN_PATTERN);

	@Override
	public boolean isValid(final String value) {
		final Matcher matcher = apiKeyPattern.matcher(value);
		
		return matcher.matches();
	}

}
