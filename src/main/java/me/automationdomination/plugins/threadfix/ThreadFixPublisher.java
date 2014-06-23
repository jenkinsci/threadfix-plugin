package me.automationdomination.plugins.threadfix;

import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.Organization;
import com.denimgroup.threadfix.data.entities.Scan;
import com.denimgroup.threadfix.remote.response.RestResponse;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import me.automationdomination.plugins.threadfix.service.JenkinsEnvironmentVariableParsingService;
import me.automationdomination.plugins.threadfix.service.TfcliService;
import me.automationdomination.plugins.threadfix.validation.*;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created with IntelliJ IDEA. User: bspruth Date: 3/22/14 Time: 12:05 AM To
 * change this template use File | Settings | File Templates.
 */
public class ThreadFixPublisher extends Recorder {
	
	private static final Logger logger = Logger.getLogger(ThreadFixPublisher.class);
	
	private final String appId;
	private final String scanFile;

	private final ConfigurationValueValidator appIdValidator = new NumericStringValidator();
	private final ConfigurationValueValidator scanFileValidator = new FileValidator();
	
	private final String appIdErrorTemplate = "app id \"%s\" is invalid";
	private final String scanFileErrorTemplate = "scan file \"%s\" is invalid or file is unreadble";

	@DataBoundConstructor
	public ThreadFixPublisher(final String appId, final String scanFile) {
		this.appId = appId;
		this.scanFile = scanFile;
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
		final PrintStream log = launcher.getListener().getLogger();
		
        // TODO: validate that environment was retrieved?
		final EnvVars envVars = build.getEnvironment(listener);
		
		// TODO: why doesn't this work as a member variable?
		final JenkinsEnvironmentVariableParsingService jenkinsEnvironmentVariableParsingService = new JenkinsEnvironmentVariableParsingService();
		
		log.println("beginning threadfix publisher execution");		
		
		
		
		log.println("raw app id: " + appId);

		final String parsedAppId = jenkinsEnvironmentVariableParsingService.parseEnvironentVariables(envVars, appId);
		
		if (!appIdValidator.isValid(parsedAppId))
			throw new AbortException(String.format(appIdErrorTemplate, appId));
		
		log.println("using app id: " + parsedAppId);
		
		
		
		log.println("raw scan file: " + scanFile);
		
		final String parsedScanFile = jenkinsEnvironmentVariableParsingService.parseEnvironentVariables(envVars, scanFile);
		
		if (!scanFileValidator.isValid(parsedScanFile))
			throw new AbortException(String.format(scanFileErrorTemplate, scanFile));
		
		log.println("using scan file: " + parsedScanFile);

		
		
		log.println("retrieving global configurations");
		
		final DescriptorImpl descriptor = this.getDescriptor();

		
		
		final String threadFixServerUrl = descriptor.getUrl();
		final ConfigurationValueValidator threadFixServerUrlValidator = descriptor.getThreadFixServerUrlValidator();
		if (!threadFixServerUrlValidator.isValid(threadFixServerUrl))
			throw new AbortException(String.format(descriptor.getThreadFixServerUrlErrorTemplate(), threadFixServerUrl));
		
		log.println("using threadfix server url: " + threadFixServerUrl);
		
		
		
		// TODO: mask this token in the output?
		// TODO: some kind of error checking whether the command was successful
		final String token = descriptor.getToken();
		final ConfigurationValueValidator tokenValidator = descriptor.getTokenValidator();
		if (!tokenValidator.isValid(token))
			throw new AbortException(String.format(descriptor.getTokenErrorTemplate(), token));
		
		log.println("using token: " + token);
		
		
		
		// the scan file validator should have verified that this file exists already
		log.println("uploading scan file");
		// TODO: does this need to be a member variable?  part of the descriptor?  etc...
		final TfcliService tfcliService = new TfcliService(threadFixServerUrl, token);
		final RestResponse<Scan> uploadFileResponse = tfcliService.uploadFile(parsedAppId, parsedScanFile);
		
		if (uploadFileResponse.success) {
			log.println("scan file uploaded successfully!");
		} else {
			log.println("scan file upload failed");
		}
		
		
		
		log.println("threadfix publisher execution complete");

		// returning true/false should be considered deprecated...
		// throw an AbortException to indicate failure
		return true;
	}
	
	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE; // NONE since this is not dependent on the last step
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	public String getAppId() {
		return appId;
	}

	public String getScanFile() {
		return scanFile;
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
		
		private static final String DISPLAY_NAME = "Publish ThreadFix Scan";

		private static final String URL_PARAMETER = "url";
		private static final String TOKEN_PARAMETER = "token";

		/**
		 * To persist global configuration information, simply store it in a
		 * field and call save().
		 * 
		 * <p>
		 * If you don't want fields to be persisted, use <tt>transient</tt>.
		 */
		private String url;
		private String token;
		
		private final ConfigurationValueValidator threadFixServerUrlValidator = new ApacheCommonsUrlValidator();
		private final ConfigurationValueValidator tokenValidator = new SimpleStringValidator();
		
		private final String threadFixServerUrlErrorTemplate = "threadfix server url \"%s\" is invalid";
		private final String tokenErrorTemplate = "threadfix server api key \"%s\" is invalid";

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
		 * @param url
		 *            This parameter receives the value that the user has typed.
		 * @return Indicates the outcome of the validation. This is sent to the
		 *         browser.
		 *         <p>
		 *         Note that returning {@link FormValidation#error(String)} does
		 *         not prevent the form from being saved. It just means that a
		 *         message will be displayed to the user.
		 */
		public FormValidation doCheckUrl(@QueryParameter final String url) throws IOException, ServletException {
			if (!threadFixServerUrlValidator.isValid(url))
				return FormValidation.error(String.format(threadFixServerUrlErrorTemplate, url));
			
			return FormValidation.ok();
		}

		public FormValidation doCheckToken(@QueryParameter final String token) throws IOException, ServletException {
			if (!tokenValidator.isValid(token))
				return FormValidation.error(String.format(tokenErrorTemplate, token));

			return FormValidation.ok();
		}

		public FormValidation doTestConnection(
				@QueryParameter final String url,
				@QueryParameter final String token) throws IOException,
				ServletException {


			try {
				// TODO add test - tfcli
				// http://automationdomination.me/threadfix/rest/teams?apiKey=oNgiwdVwHwkFAUX22LJeExwrTtfher8q5W26ihgkBI
                final TfcliService tfcliService = new TfcliService(url, token);

                RestResponse<Organization[]> getAllTeamsResponse = tfcliService.getAllTeams();

                return FormValidation.ok("ThreadFix connection success!");
			} catch (Exception e) {
				return FormValidation.error("ThreadFix connection error : " + e.getMessage());
			}
		}


		@Override
		public boolean isApplicable(@SuppressWarnings("rawtypes") final Class<? extends AbstractProject> jobType) {
			// Indicates that this builder can be used with all kinds of project
			// types applicable to all project types
			return true;
		}

		@Override
		public boolean configure(final StaplerRequest staplerRequest, final JSONObject formData) throws FormException {
			url = formData.getString(URL_PARAMETER);

			if (!threadFixServerUrlValidator.isValid(url))
				throw new FormException(String.format(threadFixServerUrlErrorTemplate, url), URL_PARAMETER);
			

			token = formData.getString(TOKEN_PARAMETER);

			if (!tokenValidator.isValid(token))
				throw new FormException(String.format(tokenErrorTemplate, token), TOKEN_PARAMETER);
			
			
			save();

			
			return super.configure(staplerRequest, formData);
		}
		
		public ListBoxModel doFillAppIdItems() {
			final ListBoxModel appIds = new ListBoxModel();
			
			final TfcliService tfcliService = new TfcliService(url, token);
			
			RestResponse<Organization[]> getAllTeamsResponse = tfcliService.getAllTeams();
			
			if (getAllTeamsResponse.success) {
				for (final Organization organization : getAllTeamsResponse.object) {
					for (final Application application : organization.getActiveApplications()) {
						appIds.add(organization.getName() + " - " + application.getName(), Integer.toString(application.getId()));
					}
				}
			} else {
				appIds.add("ERROR RETRIEVING TEAMS", "-1");
			}

            return appIds;
        }

		/**
		 * This human readable name is used in the configuration screen.
		 */
		@Override
		public String getDisplayName() {
			return DISPLAY_NAME;
		}

		public String getUrl() {
			return url;
		}

		public String getToken() {
			return token;
		}

		public ConfigurationValueValidator getThreadFixServerUrlValidator() {
			return threadFixServerUrlValidator;
		}

		public ConfigurationValueValidator getTokenValidator() {
			return tokenValidator;
		}

		public String getThreadFixServerUrlErrorTemplate() {
			return threadFixServerUrlErrorTemplate;
		}

		public String getTokenErrorTemplate() {
			return tokenErrorTemplate;
		}

	}

}
