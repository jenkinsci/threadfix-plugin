package me.automationdomination.plugins.threadfix.validation;

import org.apache.commons.validator.routines.UrlValidator;
import me.automationdomination.plugins.threadfix.ThreadFixPublisher;


public class ApacheCommonsUrlValidator implements ConfigurationValueValidator {

	private final UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https" }, UrlValidator.ALLOW_LOCAL_URLS);

	@Override
	public boolean isValid(final String value) {
		if (value == null || value.length() == 0)
			return false;

		if (!urlValidator.isValid(value))
			return false;
			//String url = ThreadFixPublisher.DescriptorImpl.getCurrentDescriptorByNameUrl();

        // TODO how do you access "url" from ThreadFixPublisher
        if (urlValidator.isValid(ThreadFixPublisher.DescriptorImpl.getCurrentDescriptorByNameUrl())) {
            System.out.println(ThreadFixPublisher.DescriptorImpl.getCurrentDescriptorByNameUrl() + " is valid");
        }
        else {
            System.out.println(ThreadFixPublisher.DescriptorImpl.getCurrentDescriptorByNameUrl() + " is invalid");
        }

		return true;
	}

}
