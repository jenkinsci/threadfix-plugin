package me.automationdomination.plugins.threadfix;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import hudson.*;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.ChangeLogSet.Entry;
import hudson.tasks.*;
import hudson.util.FormValidation;
import hudson.model.*;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import net.sf.json.JSONObject;
import hudson.tasks.BuildStep;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
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

    public String getScan() {
        return scan;
    }

    public String getSearchCondition() {
        return searchCondition;
    }

    public Long getAppId() {
        return appId;
    }

    public String getToken() {
        return token;
    }

    public Integer getUploadWaitTime() {
        return uploadWaitTime;
    }

    public String getAuditScript() {
        return auditScript;
    }
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public Action getProjectAction(AbstractProject<?,?> project) {
        return new ChartAction(project);
    }

}
