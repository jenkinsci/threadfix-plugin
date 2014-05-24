package me.automationdomination.plugins.threadfix;

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
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created with IntelliJ IDEA.
 * User: bspruth
 * Date: 3/22/14
 * Time: 12:05 AM
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("unchecked")
public class ThreadfixPublisher extends Recorder {

    public final String token;
    public final String url;
    public final String tfcli;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    //public static class HelloWorldBuilder extends BuildStepDescriptor<Publisher> {
    @DataBoundConstructor
    public ThreadfixPublisher(String token, String url, String tfcli) {

        this.token = token;
        this.url = url;
        this.tfcli = tfcli;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getToken() {
        return token;
    }

    public String getUrl() {
    return url;
    }

    public String getTfcli() {
    return tfcli;
    }

    //required per jenkins recorder
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        /*
        This is what will be executed when the job is build.
        This also shows how you can use listener and build.
        Will be seen in the jenkins Console output
         */
        PrintStream log = launcher.getListener().getLogger();
        log.println("Publishing Scan Results");
        listener.getLogger().println("Using" + "Token" + token + "URL" + url + "Threadfix CLI" + tfcli);
        listener.getLogger().println("This is job number: "+ build.getDisplayName());
        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link ThreadfixPublisher}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldPublisher/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String useThreadfixAppName;

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }
        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         *      <p>
         *      Note that returning {@link FormValidation#error(String)} does not
         *      prevent the form from being saved. It just means that a message
         *      will be displayed to the user.
         */
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a name");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }
        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Threadfix Server Configuration";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            useThreadfixAppName = formData.getString("useThreadfixAppName");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

        /**
         * This method returns true if the global configuration says we should speak French.
         *
         * The method name is bit awkward because global.jelly calls this method to determine
         * the initial state of the checkbox by the naming convention.
         */
        public String getUseThreadfixAppName() {
            return useThreadfixAppName;
        }
    }
}
