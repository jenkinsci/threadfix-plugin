package me.automationdomination.plugins.threadfix;

public class NumericStringValidator implements ConfigurationValueValidator {

	@Override
	public boolean isValid(final String value) {
		if (value == null || value.isEmpty())
			return false;

		final String trimmedAppId = value.trim();

		if (trimmedAppId.length() == 0)
			return false;

		try {
			Integer.parseInt(trimmedAppId);
		} catch (final NumberFormatException e) {
			return false;
		}

		return true;
	}

}
