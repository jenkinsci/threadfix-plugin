package me.automationdomination.plugins.threadfix.service;

import java.util.HashMap;
import java.util.StringTokenizer;

import me.automationdomination.service.commandline.CommandLineExecutionResult;
import me.automationdomination.service.commandline.CommandLineExecutionService;
import me.automationdomination.service.commandline.CommandLineExecutionServiceException;

public class TfcliService {
	
	private final String tfcli;
	private String url;
	private String token;
	
	private final CommandLineExecutionService commandLineExecutionService = new CommandLineExecutionService();
	
	private final String configureThreadFixServerUrlCommandTemplate = "java -jar \"%s\" --set url \"%s\"";
	private final String configureTokenCommandTemplate = "java -jar \"%s\" --set key \"%s\"";
	private final String uploadFileCommandTemplate = "java -jar \"%s\" --upload \"%s\" \"%s\"";

	public TfcliService(
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
	
    public HashMap<Long, String> getTfApplications () {
        HashMap<Long, String> tfapplications = new HashMap<Long, String>();

        // each line is either an empty new line, a line with three header values we want to ignore
        // or a line with three values we want (Team name, application name, application ID)
        StringTokenizer lineTokenizer = new StringTokenizer(getResults());
        while ( lineTokenizer.hasMoreTokens() ) {
            String line = lineTokenizer.nextToken();
            //String[] values = line.split("\n+");
            String[] values = line.split(";");
            //we assume a length of three means we have a line with real projects listed
            if ( values.length == 3 ) {
                try {
                    Long applicationID = Long.parseLong(values[2]);
                    tfapplications.put(applicationID, values[0] + " (" + values[1] + ")");
                }
                // if we don't parse a long something weird must be going on, but we will keep going
                catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return tfapplications;
    }

}
