package me.automationdomination.plugins.threadfix;


public class UrlValidator implements ConfigurationValueValidator {
	
	@Override
	public boolean isValid(final String value) {
		// TODO: check apache commons for url validator
		
		if (value == null || value.length() == 0)
			return false;

		if (value.length() < 7)
			return false;

		return true;
	}

}
