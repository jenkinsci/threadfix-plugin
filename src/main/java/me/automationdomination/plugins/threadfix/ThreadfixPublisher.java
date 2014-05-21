package me.automationdomination.plugins.threadfix;

import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.kohsuke.stapler.DataBoundConstructor;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created with IntelliJ IDEA.
 * User: bspruth
 * Date: 3/22/14
 * Time: 12:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class ThreadfixPublisher extends Recorder {

    private String scan;
    private String searchCondition;
    private Long appId;
    private String token;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public ThreadfixPublisher (String scan, String searchCondition, Long appId, String token) {

        this.scan = scan;
        this.searchCondition = searchCondition;
        this.appId = appId;
        this.token = token;

    }

    public String getFpr() {
        return scan;
    }

    public String getSearchCondition() {
        return searchCondition;
    }

    public Long getF360projId() {
        return appId;
    }

    public String token() {
        return token;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public Action getProjectAction(AbstractProject<?,?> project) {
        return new ChartAction(project);
    }
}
