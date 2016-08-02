package me.automationdomination.plugins.threadfix;

import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.Organization;
import com.denimgroup.threadfix.data.entities.Scan;
import com.denimgroup.threadfix.remote.response.RestResponse;
import hudson.*;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.remoting.Callable;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import me.automationdomination.plugins.threadfix.service.ThreadFixService;
import net.sf.json.JSONObject;
import org.apache.commons.validator.routines.IntegerValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ThreadFix Plugin, publish scan results in Project page and uploads scan artifact to ThreadFix server.
 */
public class ThreadFixPublisher extends Recorder implements Serializable {

    private static final long serialVersionUID = 3393285563021058327L;

    private final String LOG_FORMAT = "[ThreadFix Publisher] %s";

    private final String appId;
    private final String scanFile;

    @DataBoundConstructor
    public ThreadFixPublisher(final String appId, final String scanFile) {
        this.appId = appId;
        this.scanFile = scanFile;
    }

    /**
     * This is what will be executed when the job is build.
     */
    @Override
    public boolean perform(
            final AbstractBuild<?, ?> build,
            final Launcher launcher,
            final BuildListener listener) throws InterruptedException, IOException {
        final PrintStream out = launcher.getListener().getLogger();

        log("Starting ThreadFix publisher execution", out);

        log("Parameter Application ID: " + appId, out);
        validateApplicationId(appId);

        log("Parameter Scan File: " + scanFile, out);
        final EnvVars envVars = build.getEnvironment(listener);
        final String expandedScanFilePath = envVars.expand(scanFile);
        log("Expanded Scan File: " + expandedScanFilePath, out);
        final FilePath filePath = new FilePath(build.getWorkspace(), expandedScanFilePath);
        validateFilePathExists(filePath);

        log("Retrieving global configurations", out);
        final DescriptorImpl descriptor = this.getDescriptor();
        descriptor.validateToken();
        descriptor.validateUrl();

        final String threadFixServerUrl = descriptor.getUrl();
        log("Using ThreadFix server URL: " + threadFixServerUrl, out);

        // TODO: mask this token in the output?
        // TODO: some kind of error checking whether the command was successful
        final String token = descriptor.getToken();

        final ThreadFixService threadFixService = new ThreadFixService(threadFixServerUrl, token);
        final boolean success = this.uploadScanFile(launcher, threadFixService, filePath);
        return success;
    }

    /**
     * Log messages to the parameter output stream
     *
     * @param message
     * @param out
     */
    private void log(final String message, final PrintStream out) {
        out.println(String.format(LOG_FORMAT, message));
    }

    /**
     * Validate parameter application ID is numeric. If it is not numeric,
     * abort by throwing an exception.
     *
     * @param applicationId
     * @throws AbortException
     */
    private void validateApplicationId(final String applicationId) throws AbortException {
        final Integer value = IntegerValidator.getInstance().validate(applicationId);

        if (value == null) {
            throw new AbortException(String.format("application id \"%s\" is invalid", appId));
        }
    }

    /**
     * Validate parameter file path exists. If it does not exist, abort by
     * throwing an excpetion.
     *
     * @param filePath
     * @throws AbortException
     */
    private void validateFilePathExists(final FilePath filePath) throws IOException, InterruptedException {
        if (!filePath.exists()) {
            throw new AbortException(String.format("scan file \"%s\" is invalid or file is unreadable", filePath));
        }
    }

    /**
     * Uploads the parameter scan file via the parameter ThreadFixService
     *
     * @param launcher
     * @param threadFixService
     * @param filePath
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean uploadScanFile(
            final Launcher launcher,
            final ThreadFixService threadFixService,
            final FilePath filePath) throws IOException, InterruptedException {
        final PrintStream out = launcher.getListener().getLogger();

        log(String.format("Uploading Scan File: %s", filePath), out);

        // Node agnostic execution of ThreadFix upload service
        final boolean success = launcher.getChannel().call(new Callable<Boolean, IOException>() {
            public Boolean call() throws IOException {
                final RestResponse<Scan> uploadFileResponse = threadFixService.uploadFile(appId, filePath);
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
     * Returns the {@link ThreadFixPublisher} descriptor. This doesn't do
     * anything special other than casting the return value for convenience.
     *
     * @return
     */
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Returns NONE since this is not dependent on the last step
     *
     * @return
     */
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    /**
     * Returns the configured application ID
     *
     * @return
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Returns the configured scan file
     *
     * @return
     */
    public String getScanFile() {
        return scanFile;
    }

    /**
     * Descriptor for {@link ThreadFixPublisher}. Used as a singleton. The class
     * is marked as public so that it can be accessed from views.
     * <p>
     * <p>
     * See
     * <tt>src/main/resources/hudson/me/automationdomination/plugins/threadfix/ThreadFixPublisher/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private static final String DISPLAY_NAME = "Publish ThreadFix Scan";

        private static final String URL_PARAMETER = "url";
        private static final String TOKEN_PARAMETER = "token";

        private static final String THREAD_FIX_SERVER_URL_ERROR_FORMAT = "threadfix server url \"%s\" is invalid";
        private static final String THREAD_FIX_TOKEN_ERROR_FORMAT = "threadfix server api token \"%s\" is invalid";

        private static final String API_TOKEN_PATTERN = "^[A-Za-z0-9]{40,}$";

        private final UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"}, UrlValidator.ALLOW_LOCAL_URLS);
        private final Pattern apiTokenPattern = Pattern.compile(API_TOKEN_PATTERN);

        private String url;
        private String token;

        /**
         * Default constructor.
         * Calls load() to load persisted global configuration.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the ThreadFix server URL parameter
         *
         * @param url
         * @return
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheckUrl(@QueryParameter final String url) throws IOException, ServletException {
            if (!isUrlValid(url)) {
                return FormValidation.error(String.format(THREAD_FIX_SERVER_URL_ERROR_FORMAT, url));
            } else {
                return FormValidation.ok();
            }
        }

        /**
         * Performs on-the-fly validation of the ThreadFix api token parameter
         *
         * @param token
         * @return
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheckToken(@QueryParameter final String token) throws IOException, ServletException {
            if (!isApiTokenValid(token)) {
                return FormValidation.error(String.format(THREAD_FIX_TOKEN_ERROR_FORMAT, token));
            } else {
                return FormValidation.ok();
            }
        }

        /**
         * Performs an on-the-fly check of the ThreadFix url and api token
         * parameters by making a simple call to the server and validating
         * the response code.
         *
         * @param url
         * @param token
         * @return
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doTestConnection(@QueryParameter final String url, @QueryParameter final String token) throws IOException, ServletException {
            final ThreadFixService threadFixService = new ThreadFixService(url, token);

            final RestResponse<Organization[]> getAllTeamsResponse = threadFixService.getAllTeams();

            if (getAllTeamsResponse.success) {
                return FormValidation.ok("ThreadFix connection success!");
            } else {
                return FormValidation.error("Unable to connect to ThreadFix server");
            }
        }

        /**
         * Returns true to indicate that this builder can be used with all
         * project types
         *
         * @param jobType
         * @return
         */
        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * Validate and save configuration parameters
         *
         * @param staplerRequest
         * @param formData
         * @return
         * @throws FormException
         */
        @Override
        public boolean configure(final StaplerRequest staplerRequest, final JSONObject formData) throws FormException {
            url = formData.getString(URL_PARAMETER);

            if (!isUrlValid(url)) {
                throw new FormException(String.format(THREAD_FIX_SERVER_URL_ERROR_FORMAT, url), URL_PARAMETER);
            }

            token = formData.getString(TOKEN_PARAMETER);

            if (!isApiTokenValid(token)) {
                throw new FormException(String.format(THREAD_FIX_TOKEN_ERROR_FORMAT, token), TOKEN_PARAMETER);
            }

            save();

            return super.configure(staplerRequest, formData);
        }

        /**
         * Retrieve the teams and applications to populate the application I
         * dropdown
         *
         * @return
         */
        public ListBoxModel doFillAppIdItems() {
            final ListBoxModel appIds = new ListBoxModel();

            final ThreadFixService threadFixService = new ThreadFixService(url, token);

            final RestResponse<Organization[]> getAllTeamsResponse = threadFixService.getAllTeams();

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

        /**
         * Returns true if the url format appears correct
         *
         * @param url
         * @return
         */
        private boolean isUrlValid(final String url) {
            return urlValidator.isValid(url);
        }

        /**
         * Just in case a bad configuration value was saved, this method can be
         * called to validate the ThreadFix URL
         *
         * @throws AbortException
         */
        private void validateUrl() throws AbortException {
            if (!isUrlValid(url)) {
                throw new AbortException(String.format(THREAD_FIX_SERVER_URL_ERROR_FORMAT, url));
            }
        }

        /**
         * Returns true if the API token format appears correct
         *
         * @param apiToken
         * @return
         */
        private boolean isApiTokenValid(final String apiToken) {
            if (apiToken == null) {
                return false;
            }

            if (apiToken.isEmpty()) {
                return false;
            }

            final Matcher matcher = apiTokenPattern.matcher(apiToken);
            return matcher.matches();
        }

        /**
         * Just in case a bad configuration value was saved, this method can be
         * called to validate the ThreadFix API token parameter
         *
         * @throws AbortException
         */
        private void validateToken() throws AbortException {
            if (!isApiTokenValid(token)) {
                throw new AbortException(String.format(THREAD_FIX_TOKEN_ERROR_FORMAT, token));
            }
        }

        /**
         * Returns the configured ThreadFix URL
         *
         * @return
         */
        public String getUrl() {
            return url;
        }

        /**
         * Returns the configured ThreadFix API token
         *
         * @return
         */
        public String getToken() {
            return token;
        }

    }

}
