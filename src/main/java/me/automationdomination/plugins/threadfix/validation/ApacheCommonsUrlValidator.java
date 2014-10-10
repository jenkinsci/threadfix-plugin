package me.automationdomination.plugins.threadfix.validation;

import org.apache.commons.validator.routines.UrlValidator;

import java.io.Serializable;

public class ApacheCommonsUrlValidator implements ConfigurationValueValidator, Serializable {

    private static final long serialVersionUID = -1444298188137115496L;

    private final UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https" }, UrlValidator.ALLOW_LOCAL_URLS);

	@Override
	public boolean isValid(final String value) {
		boolean isValid = true;

		if (value == null || value.length() == 0) {
			isValid = false;
		} else if (!urlValidator.isValid(value)) {
			isValid = false;
		}

		return isValid;
	}

}
