package me.automationdomination.plugins.threadfix.service;

import hudson.EnvVars;

public interface EnvironmentVariableParsingService {

	public String parseEnvironentVariables(EnvVars envVars, String value);

}