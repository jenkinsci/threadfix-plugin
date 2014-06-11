package me.automationdomination.plugins.threadfix;

public class StringValidator implements ConfigurationValueValidator {

	@Override
	public boolean isValid(final String value) {
		if (value == null || value.isEmpty()) 
			return false;
			
		return true;
	}

}
