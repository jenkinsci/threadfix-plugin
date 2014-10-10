package me.automationdomination.plugins.threadfix.validation;

import java.io.Serializable;

public class SimpleStringValidator implements ConfigurationValueValidator, Serializable {

    private static final long serialVersionUID = -247258005957731845L;

    @Override
	public boolean isValid(final String value) {
		if (value == null || value.isEmpty())
			return false;

        return true;
    }

}
