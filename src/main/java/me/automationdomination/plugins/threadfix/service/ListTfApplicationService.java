package me.automationdomination.plugins.threadfix.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by xolian on 6/19/14.
 */
public class ListTfApplicationService  {

    String stdoutResults = "";
    String stderrResults = "";
    String allResults = "";

    public String getResults() {
        return stdoutResults;
    }

    public String getErrors() {
        return stderrResults;
    }

    protected String getAllResults() {
        return stdoutResults + "\n" + stderrResults;
    }

     ///

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

    // need to add command method here.  Copied ThreadFixUploadService

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
