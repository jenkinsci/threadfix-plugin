package me.automationdomination.plugins.threadfix;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.util.ChartUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.category.CategoryDataset;

/**
 * Created with IntelliJ IDEA.
 * User: bspruth
 * Date: 5/20/14
 * Time: 11:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChartAction implements Action {
    private AbstractProject<?,?> project;

    public ChartAction(AbstractProject<?,?> project) {
        this.project = project;
    }

    public String getDisplayName() {
        return "Threadfix Plugin";
    }

    public String getIconFileName() {
        return null;
    }
    public String getUrlName() {
        return "threadfix-plugin";
    }
}
