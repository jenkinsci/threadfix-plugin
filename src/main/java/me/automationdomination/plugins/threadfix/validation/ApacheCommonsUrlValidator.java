package me.automationdomination.plugins.threadfix.validation;

import org.apache.commons.validator.routines.UrlValidator;

public class ApacheCommonsUrlValidator implements ConfigurationValueValidator {

	private final UrlValidator urlValidator = new UrlValidator();

	@Override
	public boolean isValid(final String value) {
		if (value == null || value.length() == 0)
			return false;

		if (!urlValidator.isValid(value))
			return false;

		return true;
	}

}
