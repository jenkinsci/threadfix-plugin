package me.automationdomination.plugins.threadfix.validation;

import hudson.EnvVars;
import me.automationdomination.plugins.threadfix.service.LinuxEnvironmentVariableParsingService;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import java.io.PrintStream;

public class LinuxVariableParsingServiceTest {
	
	@Test
	public void simpleTest() {
		String variableName = "VARIABLE"; 
		String value = "VALUE";
		
		EnvVars envVars = EasyMock.createNiceMock(EnvVars.class);
        PrintStream log = EasyMock.createNiceMock(PrintStream.class);
		EasyMock.expect(envVars.get(variableName)).andReturn(value);
		EasyMock.replay(envVars);
		
		String test = "${VARIABLE}";
		
		LinuxEnvironmentVariableParsingService l = new LinuxEnvironmentVariableParsingService();
		String result = l.parseEnvironentVariables(envVars, test, log);
		
		Assert.assertEquals(value, result);
	}
	
	@Test
	public void simpleNegativeTest() {
		String variableName = "POOP"; 
		String value = "VALUE";
		
		EnvVars envVars = EasyMock.createNiceMock(EnvVars.class);
        PrintStream log = EasyMock.createNiceMock(PrintStream.class);
		EasyMock.expect(envVars.get(variableName)).andReturn(value);
		EasyMock.replay(envVars);
		
		String test = "${VARIABLE}";
		
		LinuxEnvironmentVariableParsingService l = new LinuxEnvironmentVariableParsingService();
		String result = l.parseEnvironentVariables(envVars, test, log);
		
		Assert.assertEquals(test, result);
	}

}
