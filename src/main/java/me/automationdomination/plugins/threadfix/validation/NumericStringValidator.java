package me.automationdomination.plugins.threadfix.validation;

import java.io.Serializable;

public class NumericStringValidator implements ConfigurationValueValidator, Serializable {

    private static final long serialVersionUID = 4555269426299715085L;

    @Override
	public boolean isValid(final String value) {
		if (value == null || value.isEmpty())
			return false;

		try {
			Integer.parseInt(value);
		} catch (final NumberFormatException e) {
			return false;
		}

		return true;
	}

}
