package me.automationdomination.plugins.threadfix;

import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created with IntelliJ IDEA.
 * User: bspruth
 * Date: 3/22/14
 * Time: 12:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class ThreadfixResultsPublisher extends Recorder {

    private static final String DEFAULT_TOKEN = "x";
    private String scan;
    private Long appId;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    //public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
    @DataBoundConstructor
    public ThreadfixResultsPublisher(String scan, Long appId) {

        this.scan = scan;
        this.appId = appId;

    }

    public String getScan() {
        return scan;
    }

    public Long getAppId() {
        return appId;
    }

    public String getToken() {
        return DEFAULT_TOKEN;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

}
