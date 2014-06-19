package me.automationdomination.service.commandline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class CommandLineExecutionService {
	
	public CommandLineOutput executeCommand(final String command) throws CommandLineExecutionServiceException {
		final StringBuilder outputBuilder = new StringBuilder();
		final Runtime runtime = Runtime.getRuntime();
		
		
		final Process process;
		
		try {
			process = runtime.exec(command);
		} catch (final IOException e) {
			throw new CommandLineExecutionServiceException("exception executing command: \"" + command + "\"", e);
		}
		
		
		final int returnValue;
		
		try {
			// TODO: need some kind of timeout here?
			returnValue = process.waitFor();
		} catch (final InterruptedException e) {
			throw new CommandLineExecutionServiceException("exception waiting for end of command: \"" + command + "\"", e);
		}
		
		
		final InputStream processOutputStream = process.getInputStream();
		final InputStreamReader processOutputStreamReader = new InputStreamReader(processOutputStream);
		final BufferedReader processOutputReader = new BufferedReader(processOutputStreamReader);
		
		try {
			String line = "";
			
			while ((line = processOutputReader.readLine()) != null) {
				outputBuilder.append(line);
				outputBuilder.append("\n");
			}
		} catch (final IOException e) {
			throw new CommandLineExecutionServiceException("exception reading output from command: \"" + command + "\"", e);
		}
		
		
		final CommandLineOutput commandLineOutput = new CommandLineOutput(returnValue, outputBuilder.toString());
		
		return commandLineOutput;
	}

}
