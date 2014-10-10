package me.automationdomination.plugins.threadfix.service;

import hudson.EnvVars;

public interface EnvironmentVariableParsingService {
	
	public String parse(EnvVars paramEnvVars, String paramString);
	
}