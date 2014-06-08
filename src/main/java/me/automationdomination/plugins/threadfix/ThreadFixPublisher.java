package me.automationdomination.plugins.threadfix;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Created with IntelliJ IDEA. User: bspruth Date: 3/22/14 Time: 12:05 AM To
 * change this template use File | Settings | File Templates.
 */
@SuppressWarnings("unchecked")
public class ThreadFixPublisher extends Recorder {
	
	private final String appId;

	@DataBoundConstructor
	public ThreadFixPublisher(final String appId) {
		if (appId == null) {
			throw new IllegalArgumentException("appId cannot be null");
		}
		
		final String trimmedAppId = appId.trim();
		
		if (trimmedAppId.length() == 0) {
			throw new IllegalArgumentException("illegal appId: \"" + appId + "\"");
		}
		
		try {
			Integer.parseInt(trimmedAppId);
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("illegal appId: \"" + appId + "\"");
		}
		
		this.appId = trimmedAppId;
	}

	/**
	 * This is what will be executed when the job is build. This also shows how
	 * you can use listener and build. Will be seen in the jenkins Console
	 * output
	 */
	@Override
	public boolean perform(
			final AbstractBuild<?, ?> build,
			final Launcher launcher, 
			final BuildListener listener) throws InterruptedException, IOException {
		// TODO: does this appId need to be validated before execution?
		final PrintStream log = launcher.getListener().getLogger();
		log.println("beginning threadfix publisher execution with app id \"" + appId + "\"");
		

		log.println("retrieving global configurations...");
		final DescriptorImpl descriptor = this.getDescriptor();
		
		
		// TODO: better way to share validation with descriptor?
		final String tfcli = descriptor.getTfcli();
		log.println("found tfcli: \"" + tfcli + "\"");
		final FormValidation tfcliFormValidation;
		try {
			tfcliFormValidation = descriptor.doCheckTfcli(tfcli);
			
			if (tfcliFormValidation != FormValidation.ok())
				throw new AbortException("threadfix-cli jar is invalid");
		} catch (final ServletException e) {
			throw new RuntimeException("exception validating threadfix cli", e);
		}
		
		
		// TODO: better way to share validation with descriptor?
		final String url = descriptor.getUrl();
		log.println("found url: \"" + url + "\"");
		final FormValidation urlFormValidation;
		try {
			urlFormValidation = descriptor.doCheckUrl(url);

			if (urlFormValidation != FormValidation.ok())
				throw new AbortException("threadfix server url is invalid");
		} catch (ServletException e) {
			throw new RuntimeException("exception validating threadfix url", e);
		}
		
		
		// TODO: better way to share validation with descriptor?
		final String token = descriptor.getToken();
		log.println("found token: \"" + token + "\"");
		final FormValidation tokenFormValidation;
		try {
			tokenFormValidation = descriptor.doCheckToken(token);

			if (tokenFormValidation != FormValidation.ok())
				throw new AbortException("threadfix api key is invalid");
		} catch (ServletException e) {
			throw new RuntimeException("exception validating threadfix api key", e);
		}
		
		
		
		// TODO: build this with a string builder or use a constant or something
		log.println("setting threadfix server url");
		final String setServerUrlCommand = "java -jar " + tfcli + " --set url " + url;
		// TODO: anything need to be done with this output? it should show up in
		// the console anyway...
		final String setServerUrlCommandOutput = runCommand(setServerUrlCommand);
		log.println(setServerUrlCommandOutput);
		
		
		// TODO: build this with a string builder or use a constant or something
		log.println("setting threadfix server api key");
		final String setApiKeyCommand = "java -jar " + tfcli + " --set key " + token;
		// TODO: anything need to be done with this output? it should show up in
		// the console anyway...
		@SuppressWarnings("unused")
		final String setApiKeyCommandOutput = runCommand(setApiKeyCommand);
		log.println(setApiKeyCommandOutput);
		
		
		final FilePath workspace = build.getWorkspace();
		log.println("checking for scans in workspace \"" + workspace + "\"");
		
		// TODO: sort this by date and only pull the most recent?
		// TODO: delete scan after successful upload?
		for (final FilePath workspaceFile : workspace.list()) {
			log.println("found file \"" + workspaceFile.getName() + "\"");
			
			// TODO: build this with a string builder or use a constant or something
			final URI workspaceFileURI = workspaceFile.toURI();
			final File file = new File(workspaceFileURI);
			final String canonicalPath = file.getCanonicalPath();
			
			final String uploadFileCommand = "java -jar " + tfcli + " --upload " + appId + " " + canonicalPath;
			log.println("running command: \"" + uploadFileCommand + "\"");
			// TODO: anything need to be done with this output? it should show
			// up in the console anyway...
			final String uploadFileCommandOutput = runCommand(uploadFileCommand);
			log.println(uploadFileCommandOutput);
		}

		
		log.println("threadfix publisher execution complete");

		// returning true/false should be considered deprecated...
		// throw an AbortException to indicate failure
		return true;
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
	
	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE; // NONE since this is not dependent on the last step
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	/**
	 * Descriptor for {@link ThreadFixPublisher}. Used as a singleton. The class
	 * is marked as public so that it can be accessed from views.
	 * 
	 * <p>
	 * See
	 * <tt>src/main/resources/hudson/me/automationdomination/plugins/threadfix/ThreadFixPublisher/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension // This indicates to Jenkins that this is an implementation of an extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		/**
		 * To persist global configuration information, simply store it in a
		 * field and call save().
		 * 
		 * <p>
		 * If you don't want fields to be persisted, use <tt>transient</tt>.
		 */
		private String tfcli;
		private String url;
		private String token;

		/**
		 * In order to load the persisted global configuration, you have to call
		 * load() in the constructor.
		 */
		public DescriptorImpl() {
			load();
		}

		/**
		 * Performs on-the-fly validation of the form fields 'tfcli'.
		 * 
		 * @param value
		 *            This parameter receives the value that the user has typed.
		 * @return Indicates the outcome of the validation. This is sent to the
		 *         browser.
		 *         <p>
		 *         Note that returning {@link FormValidation#error(String)} does
		 *         not prevent the form from being saved. It just means that a
		 *         message will be displayed to the user.
		 */
		public FormValidation doCheckTfcli(@QueryParameter final String value) throws IOException, ServletException {
			// TODO: add more validation
			if (value == null || value.length() == 0)
				return FormValidation.error("Please provide the path to the threadfix-cli jar");
			
			final File tfcliFile = new File(value);
			
			if (!tfcliFile.exists()) 
				return FormValidation.error("threadfix-cli jar file not found");
			
			return FormValidation.ok();
		}
		
		public FormValidation doCheckUrl(@QueryParameter final String value) throws IOException, ServletException {
			// TODO: add more validation
			if (value == null || value.length() == 0)
				return FormValidation.error("Please provide the RESTful URL of your ThreadFix server");
			
			if (value.length() < 7)
				return FormValidation.warning("This is not a URL");
			
			return FormValidation.ok();
		}

		public FormValidation doCheckToken(@QueryParameter final String value) throws IOException, ServletException {
			// TODO: add more validation
			if (value == null || value.length() == 0)
				return FormValidation.error("Please provide the API token of your ThreadFix server");
			
			return FormValidation.ok();
		}

		public boolean isApplicable(@SuppressWarnings("rawtypes") final Class<? extends AbstractProject> jobType) {
			// Indicates that this builder can be used with all kinds of project
			// types applicable to all project types
			return true;
		}

		@Override
		public boolean configure(final StaplerRequest staplerRequest, final JSONObject formData) throws FormException {
			url = formData.getString("url");

			token = formData.getString("token");
			
			tfcli = formData.getString("tfcli");

			save();

			return super.configure(staplerRequest, formData);
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "ThreadFix Scan to ThreadFix Server";
		}
		
		public String getTfcli() {
			return tfcli;
		}

		public String getUrl() {
			return url;
		}

		public String getToken() {
			return token;
		}

	}

}
