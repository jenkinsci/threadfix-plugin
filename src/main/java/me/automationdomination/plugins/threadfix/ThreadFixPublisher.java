package me.automationdomination.plugins.threadfix;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.remoting.Callable;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;

import javax.servlet.ServletException;

import me.automationdomination.plugins.threadfix.service.UnixEnvironmentVariableParsingService;
import me.automationdomination.plugins.threadfix.service.EnvironmentVariableParsingService;
import me.automationdomination.plugins.threadfix.service.ThreadFixService;
import me.automationdomination.plugins.threadfix.service.WindowsEnvironmentVariableParsingService;
import me.automationdomination.plugins.threadfix.validation.ApacheCommonsUrlValidator;
import me.automationdomination.plugins.threadfix.validation.ApiKeyStringValidator;
import me.automationdomination.plugins.threadfix.validation.ConfigurationValueValidator;
import me.automationdomination.plugins.threadfix.validation.NumericStringValidator;
import me.automationdomination.plugins.threadfix.validation.SimpleStringValidator;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.SystemUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.Organization;
import com.denimgroup.threadfix.data.entities.Scan;
import com.denimgroup.threadfix.remote.response.RestResponse;

/**
 * Created with IntelliJ IDEA. User: bspruth Date: 3/22/14 Time: 12:05 AM To
 * change this template use File | Settings | File Templates.
 */
@SuppressWarnings("unused")
public class ThreadFixPublisher extends Recorder implements Serializable {

    private static final long serialVersionUID = 3393285563021058327L;

    private static final String appIdErrorTemplate = "app id \"%s\" is invalid";
    private static final String scanFileErrorTemplate = "scan file \"%s\" is invalid or file is unreadable";

	private final String appId;
	private final String scanFile;
	private final ConfigurationValueValidator appIdValidator = new NumericStringValidator();


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
		final PrintStream out = launcher.getListener().getLogger();

        log("Starting ThreadFix publisher execution", out);
        log("Raw Application ID: " + appId, out);
        log("Using Application ID: " + appId, out);
        log("Raw scan: " + scanFile, out);

		if (!appIdValidator.isValid(appId))
			throw new AbortException(String.format(appIdErrorTemplate, appId));

        // TODO: validate that environment was retrieved?
        final EnvVars envVars = build.getEnvironment(listener);

        EnvironmentVariableParsingService envService;
        if (SystemUtils.IS_OS_WINDOWS) {
            log("Detected Windows OS", out);
            envService = new WindowsEnvironmentVariableParsingService();
        } else {
            log("Detected Non-Windows OS", out);
            envService = new UnixEnvironmentVariableParsingService();
        }

        final String parsedScanFile = envService.parse(envVars, scanFile);
        final FilePath filePath = new FilePath(build.getWorkspace(), parsedScanFile);

        if (!filePath.exists())
			throw new AbortException(String.format(scanFileErrorTemplate, scanFile));

		log("Using scan file: " + parsedScanFile, out);
		log("Retrieving global configurations", out);
		
		final DescriptorImpl descriptor = this.getDescriptor();
		final String threadFixServerUrl = descriptor.getUrl();
		final ConfigurationValueValidator threadFixServerUrlValidator = descriptor.getThreadFixServerUrlValidator();

		if (!threadFixServerUrlValidator.isValid(threadFixServerUrl))
			throw new AbortException(String.format(descriptor.getThreadFixServerUrlErrorTemplate(), threadFixServerUrl));
		
		log("Using ThreadFix server URL: " + threadFixServerUrl, out);

		// TODO: mask this token in the output?
		// TODO: some kind of error checking whether the command was successful
		final String token = descriptor.getToken();
		final ConfigurationValueValidator simpleStringValidator = descriptor.getSimpleStringValidator();
		final ConfigurationValueValidator apiKeyStringValidator = descriptor.getApiKeyStringValidator();
		if (!(simpleStringValidator.isValid(token) && apiKeyStringValidator.isValid(token)))
			throw new AbortException(String.format(descriptor.getTokenErrorTemplate(), token));

		log("Uploading scan file", out);

        // Node agnostic execution of ThreadFix upload service
        boolean success = launcher.getChannel().call(new Callable<Boolean, IOException>() {
            public Boolean call() throws IOException {
                final ThreadFixService tfcliService = new ThreadFixService(threadFixServerUrl, token);
                final RestResponse<Scan> uploadFileResponse = tfcliService.uploadFile(appId, filePath);
                return uploadFileResponse.success;
            }
        });

        if (success) {
            log("Scan file uploaded successfully!", out);
        } else {
            log("Scan file upload failed", out);
        }
        return success;
	}

    /**
     * Log messages to the builds console
     * @param message The message to log
     */
    private void log(String message, PrintStream out) {
        String outtag = "[ThreadFix] ";
        message = message.replaceAll("\\n", "\n" + outtag);
        out.println(outtag + message);
    }

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE; // NONE since this is not dependent on the last step
	}

    @SuppressWarnings("unused")
	public String getAppId() {
		return appId;
	}

    @SuppressWarnings("unused")
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
		private final ConfigurationValueValidator simpleStringValidator = new SimpleStringValidator();
		private final ConfigurationValueValidator apiKeyStringValidator = new ApiKeyStringValidator();
		
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
        @SuppressWarnings("unused")
		public FormValidation doCheckUrl(@QueryParameter final String url) throws IOException, ServletException {
			if (!threadFixServerUrlValidator.isValid(url))
				return FormValidation.error(String.format(threadFixServerUrlErrorTemplate, url));
			
			return FormValidation.ok();
		}

        @SuppressWarnings("unused")
		public FormValidation doCheckToken(@QueryParameter final String token) throws IOException, ServletException {

			if (!(simpleStringValidator.isValid(token) && apiKeyStringValidator.isValid(token)))
				return FormValidation.error(String.format(tokenErrorTemplate, token));

			return FormValidation.ok();
		}

        @SuppressWarnings("unused")
		public FormValidation doTestConnection(@QueryParameter final String url, @QueryParameter final String token) throws IOException, ServletException {
			final ThreadFixService tfcliService = new ThreadFixService(url, token);
			
			final RestResponse<Organization[]> getAllTeamsResponse = tfcliService.getAllTeams();
			
			if (getAllTeamsResponse.success) {
				return FormValidation.ok("ThreadFix connection success!");
			} else {
				return FormValidation.error("Unable to connect to ThreadFix server");
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

			if (!(simpleStringValidator.isValid(token) && apiKeyStringValidator.isValid(token)))
				throw new FormException(String.format(tokenErrorTemplate, token), TOKEN_PARAMETER);
			
			save();
			
			return super.configure(staplerRequest, formData);
		}

        @SuppressWarnings("unused")
		public ListBoxModel doFillAppIdItems() {
			final ListBoxModel appIds = new ListBoxModel();
			
			final ThreadFixService threadFixSErvice = new ThreadFixService(url, token);
			
			final RestResponse<Organization[]> getAllTeamsResponse = threadFixSErvice.getAllTeams();
			
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

		public ConfigurationValueValidator getSimpleStringValidator() {
			return simpleStringValidator;
		}

		public ConfigurationValueValidator getApiKeyStringValidator() {
			return apiKeyStringValidator;
		}

		public String getThreadFixServerUrlErrorTemplate() {
			return threadFixServerUrlErrorTemplate;
		}

		public String getTokenErrorTemplate() {
			return tokenErrorTemplate;
		}

	}

}
