package me.automationdomination.plugins.threadfix;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import org.kohsuke.stapler.DataBoundConstructor;

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

    private String token;
    private String url;
    private String tfcli;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    //public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
    @DataBoundConstructor
    public ThreadfixPublisher(String token, String url, String tfcli) {

        this.token = token;
        this.url = url;
        this.tfcli = tfcli;

    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
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

    //To change body of implemented methods use File | Settings | File Templates.
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // We'll use this from the config.jelly
    public String getToken() {
        return token;
    }
    public String getUrl() {
        return url;
    }
    public String getTfcli(){
        return tfcli;
    }

}
