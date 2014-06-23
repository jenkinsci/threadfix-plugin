package me.automationdomination.plugins.threadfix.validation;

import org.junit.Assert;
import org.junit.Test;

public class ApacheCommonsUrlValidatorTest {
	
	private final ApacheCommonsUrlValidator apacheCommonsUrlValidator = new ApacheCommonsUrlValidator();
	
	@Test
	public void automationDominationTest() {
		Assert.assertTrue(apacheCommonsUrlValidator.isValid("http://automationdominaion.me"));
	}

}
