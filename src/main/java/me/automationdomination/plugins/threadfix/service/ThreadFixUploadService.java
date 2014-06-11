package me.automationdomination.plugins.threadfix.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ThreadFixUploadService {
	
	private final String tfcli;
	private String url;
	private String token;
	
	private final String configureThreadFixServerUrlCommandTemplate = "java -jar \"%s\" --set url \"%s\"";
	private final String configureTokenCommandTemplate = "java -jar \"%s\" --set key \"%s\"";
	private final String uploadFileCommandTemplate = "java -jar \"%s\" --upload \"%s\" \"%s\"";

	public ThreadFixUploadService(
			final String tfcli,
			final String url,
			final String token) {
		super();
		this.tfcli = tfcli;
		this.url = url;
		this.token = token;
	}
	
	public void uploadFile(final String appId, final String file) {
		// TODO: some kind of error checking whether these commands execute sucessfully
		
		// TODO: print this output somehow
		@SuppressWarnings("unused")
		final String configureServerUrlCommandOutput = runCommand(String.format(configureThreadFixServerUrlCommandTemplate, (Object[]) new String[] { tfcli, url }));
		
		// TODO: print this output somehow
		@SuppressWarnings("unused")
		final String configureTokenCommandOutput = runCommand(String.format(configureTokenCommandTemplate, (Object[]) new String[] { tfcli, token }));
		
		// TODO: print this output somehow
		@SuppressWarnings("unused")
		final String uploadFileCommandOutput = runCommand(String.format(uploadFileCommandTemplate, (Object[]) new String[] { tfcli, appId, file }));
	}
	
	private String runCommand(final String command) {
		final StringBuilder outputBuilder = new StringBuilder();
		final Runtime runtime = Runtime.getRuntime();
		
		final Process process;
		try {
			process = runtime.exec(command);
		} catch (final IOException e) {
			// TODO: throw a better exception
			throw new RuntimeException("exception executing command: \"" + command + "\"", e);
		}
		
		try {
			// TODO: need some kind of timeout here?
			// TODO: do something with this return value?
			@SuppressWarnings("unused")
			final int returnValue = process.waitFor();
		} catch (final InterruptedException e) {
			// TODO: throw a better exception
			throw new RuntimeException("exception waiting for end of command: \"" + command + "\"", e);
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
			throw new RuntimeException("exception reading output from command: \"" + command + "\"", e);
		}
		
		return outputBuilder.toString();
	}

}
