package me.automationdomination.plugins.threadfix.validation;

import org.junit.Assert;
import org.junit.Test;

public class ApiKeyStringValidatorTest {
	
	@Test
	public void simpleTest1() {
		ApiKeyStringValidator a = new ApiKeyStringValidator();
		Assert.assertTrue(a.isValid("Pqf0LGd8cLMFjBYtZqqoq0rzgRqbjZzkMta571XVGVuw"));
	}
	
	@Test
	public void simpleTest2() {
		ApiKeyStringValidator a = new ApiKeyStringValidator();
		Assert.assertTrue(a.isValid("fMIUkd1wNUcpUcaUUi6Ako4bsKj2pYooGgwnLFqVg"));
	}
	
	@Test
	public void lessThan40ShouldFail() {
		ApiKeyStringValidator a = new ApiKeyStringValidator();
		Assert.assertFalse(a.isValid("111111111111111111111111111111111111111"));
	}
	
	@Test
	public void longKeyTest() {
		ApiKeyStringValidator a = new ApiKeyStringValidator();
		Assert.assertTrue(a.isValid("111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"));
	}

}
