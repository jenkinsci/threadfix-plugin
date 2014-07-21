package me.automationdomination.plugins.threadfix.validation;

import hudson.EnvVars;
import me.automationdomination.plugins.threadfix.service.WindowsEnvironmentVariableParsingService;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

public class WindowsVariableParsingServiceTest {
	
	@Test
	public void simpleTest() {
		String variableName = "VARIABLE"; 
		String value = "VALUE";
		
		EnvVars envVars = EasyMock.createNiceMock(EnvVars.class);
		EasyMock.expect(envVars.get(variableName)).andReturn(value);
		EasyMock.replay(envVars);
		
		String test = "%VARIABLE%";
		
		WindowsEnvironmentVariableParsingService w = new WindowsEnvironmentVariableParsingService();
		String result = w.parseEnvironentVariables(envVars, test);
		
		Assert.assertEquals(value, result);
	}
	
	@Test
	public void simpleNegativeTest() {
		String variableName = "POOP"; 
		String value = "VALUE";
		
		EnvVars envVars = EasyMock.createNiceMock(EnvVars.class);
		EasyMock.expect(envVars.get(variableName)).andReturn(value);
		EasyMock.replay(envVars);
		
		String test = "%VARIABLE%";
		
		WindowsEnvironmentVariableParsingService w = new WindowsEnvironmentVariableParsingService();
		String result = w.parseEnvironentVariables(envVars, test);
		
		Assert.assertEquals(test, result);
	}

}
