package me.automationdomination.plugins.threadfix.service;

import me.automationdomination.service.commandline.CommandLineExecutionResult;
import me.automationdomination.service.commandline.CommandLineExecutionService;
import me.automationdomination.service.commandline.CommandLineExecutionServiceException;

public class ThreadFixUploadService {
	
	private final String tfcli;
	private String url;
	private String token;
	
	private final CommandLineExecutionService commandLineExecutionService = new CommandLineExecutionService();
	
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
	
	public void uploadFile(final String appId, final String file) throws CommandLineExecutionServiceException {
		// TODO: some kind of error checking whether these commands execute sucessfully
		
		// TODO: print this output somehow
		@SuppressWarnings("unused")
		final CommandLineExecutionResult configureServerUrlCommandOutput = commandLineExecutionService.executeCommand(String.format(configureThreadFixServerUrlCommandTemplate, (Object[]) new String[] { tfcli, url }));
		
		// TODO: print this output somehow
		@SuppressWarnings("unused")
		final CommandLineExecutionResult configureTokenCommandOutput = commandLineExecutionService.executeCommand(String.format(configureTokenCommandTemplate, (Object[]) new String[] { tfcli, token }));
		
		// TODO: print this output somehow
		@SuppressWarnings("unused")
		final CommandLineExecutionResult uploadFileCommandOutput = commandLineExecutionService.executeCommand(String.format(uploadFileCommandTemplate, (Object[]) new String[] { tfcli, appId, file }));
	}

}
