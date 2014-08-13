package me.automationdomination.plugins.threadfix.service;


import hudson.EnvVars;
import java.io.PrintStream;

public interface EnvironmentVariableParsingService
{
    public abstract String parseEnvironentVariables(EnvVars paramEnvVars, String paramString, PrintStream paramPrintStream);
}