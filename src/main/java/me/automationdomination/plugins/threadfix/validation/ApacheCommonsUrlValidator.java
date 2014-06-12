package me.automationdomination.plugins.threadfix.validation;

import org.apache.commons.validator.routines.UrlValidator;
import me.automationdomination.plugins.threadfix.ThreadFixPublisher;

public class ApacheCommonsUrlValidator implements ConfigurationValueValidator {

	private final UrlValidator urlValidator = new UrlValidator();


	@Override
	public boolean isValid(final String value) {
		if (value == null || value.length() == 0)
			return false;

		if (!urlValidator.isValid(value))
            (value == null || value.length() == 0)
			return false;

        // TODO how do you access "url" from ThreadFixPublisher
        if (urlValidator.isValid(url)) {
            System.out.println(url + " is valid");
        }
        else {
            System.out.println(url + " is invalid");
        }

		return true;
	}

}
