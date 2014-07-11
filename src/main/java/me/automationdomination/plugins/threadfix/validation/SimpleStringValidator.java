package me.automationdomination.plugins.threadfix.validation;

public class SimpleStringValidator implements ConfigurationValueValidator {

    private static final String TOKEN_PATTERN = "^(?=.*\\d)(?=.*[A-Za-z])[A-Za-z0-9]{44}$";

	@Override
	public boolean isValid(final String value) {
		if (value == null || value.isEmpty())
			return false;

        return true;
    }
    // TODO: add test of 44 alphanumeric on string for token value
    public boolean IsAlphaNumeric(final String value)
    {
        //if (RegexpMatcher matcher = RegexpUtils.getMatcher(TOKEN_PATTERN);
        return true;
    }

}
