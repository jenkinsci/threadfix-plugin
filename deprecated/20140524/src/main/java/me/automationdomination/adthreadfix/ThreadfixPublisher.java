package me.automationdomination.adthreadfix;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.File;
import java.io.PrintStream;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.jvnet.hudson.plugins.fortify360.FPRSummary;
import org.jvnet.hudson.plugins.fortify360.RemoteService;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class ThreadfixPublisher extends Recorder {
	
	private final int teamId;
	
	private final int projectId;
	
	@DataBoundConstructor
    public ThreadfixPublisher(final int teamId, final int projectId) {
		super();
		this.teamId = teamId;
		this.projectId = projectId;
	}

    @Override
    public boolean perform(
    		final AbstractBuild<?, ?> build, 
    		final Launcher launcher, 
    		final BuildListener listener) {
    	
    	listener.getLogger().println("beginning threadfix publisher execution");
    	
    	listener.getLogger().println("publishing team id <" + teamId + "> and project id <" + projectId + "> to url <" + getDescriptor().getUrl() + ">");
    	
    	listener.getLogger().println("WORKSPACE: " + System.getenv("WORKSPACE"));
    	listener.getLogger().println("JOB_NAME: " + System.getenv("JOB_NAME"));
    	
    	final File currentWorkingDirectory = new File(".");
    	final File[] fileList = currentWorkingDirectory.listFiles();
    	for (final File file : fileList) {
    		if (file.isFile()) {
    			listener.getLogger().println(file.getName());
    			listener.getLogger().println(file.getAbsolutePath());
    		}
    	}
    	
		PrintStream log = launcher.getListener().getLogger();		
		log.println("Publishing Fortify 360 FPR Data");
		
		// calling the remote slave to retrieve the NVS
		// build.getActions().add(new ChartAction(build.getProject()));
		String jarsPath = DESCRIPTOR.getJarsPath();
		String suggestedFortifyHome = null;
		if ( !StringUtils.isBlank(jarsPath) ) {
			// jarsPath should be <SCA_Install_Path>/Core/lib
			File f = new File(jarsPath);
			suggestedFortifyHome = f.getParentFile().getParentFile().toString();			
		}
		RemoteService service = new RemoteService(fpr, filterSet, searchCondition, suggestedFortifyHome);
		FPRSummary summary = build.getWorkspace().act(service);
		String logMsg = summary.getLogMessage();
		if ( !StringUtils.isBlank(logMsg) ) log.println(logMsg);
		
		// if FPR is a remote FilePath, copy to local
		File localFPR = null;
		if ( summary.getFprFile().isRemote() ) {
			localFPR = copyToLocalTmp(summary.getFprFile());
		} else {
			localFPR = new File(summary.getFprFile().toURI());
		}
		log.printf("Using FPR: %s\n", summary.getFprFile().toURI());
		//if ( summary.getFprFile().isRemote() ) 
		log.printf("Local FPR: %s\n", localFPR.getCanonicalFile());
		log.printf("Calculated NVS = %f\n", summary.getNvs());
		
		// save data under the builds directory, this is always in Hudson master node
		log.println("Saving FPR summary");
		summary.save(new File(build.getRootDir(), FPRSummary.FILE_BASENAME));
		
		// if the project ID is not null, then we need to upload the FPR to 360 server
		if ( null != f360projId && f360projId > 0L && DESCRIPTOR.canUploadToF360() ) {
			// the FPR may be in remote slave, we need to call launcher to do this for me
			log.printf("Uploading FPR to Fortify 360 Server at %s\n", DESCRIPTOR.getUrl());
			try {
				Object[] args = new Object[] { localFPR, f360projId};
				invokeFortifyClient(DESCRIPTOR.getToken(), "uploadFPR", args, log);
				log.println("FPR uploaded successfully");
			} catch ( Throwable t ) {
				log.println("Error uploading to F360 Server: " + DESCRIPTOR.getUrl());
				t.printStackTrace(log);
			} finally {
				// if this is a remote FPR, I need to delete the local temp FPR after use
				if ( summary.getFprFile().isRemote() ) {
					if ( null != localFPR && localFPR.exists() ) {
						try { 
							boolean deleted = localFPR.delete();
							if ( !deleted ) log.printf("Can't delete local FPR file: %s\n", localFPR.getCanonicalFile());
						} catch ( Exception e ) {
							e.printStackTrace(log);
						}
					}
				}
			}
		}
		
		// now check if the fail count
		if ( !StringUtils.isBlank(searchCondition) ) {
			Integer failedCount = summary.getFailedCount();
			if ( null != failedCount && failedCount > 0 ) {
				log.printf("Fortify 360 Plugin: this build is unstable because there are %d critical vulnerabilities\n", failedCount);
				build.setResult(Result.UNSTABLE);
			}
		}
		
		// now do job assignment
		if ( null != f360projId && f360projId > 0L && DESCRIPTOR.canUploadToF360() && !StringUtils.isBlank(auditScript) ) {
			int sleep = (uploadWaitTime != null) ? uploadWaitTime : 1;
			log.printf("Sleep for %d minute(s)\n", sleep);
			sleep = sleep * 60 * 1000; // wait time is in minute(s)
			long sleepUntil = System.currentTimeMillis() + sleep;
			while(true) {
				long diff = sleepUntil - System.currentTimeMillis();
				if ( diff > 0 ) {
					try {
						Thread.sleep(diff);
					} catch ( InterruptedException e ) { }
				} else {
					break;
				}
			}
			log.printf("Auto JobAssignment, AuditToken = %s\n", auditToken);
			try {
				jobAssignment(build, log);
			} catch ( Throwable t ) {
				log.println("Error auditing FPR");
				t.printStackTrace(log);
			}
		}
    	
    	// returning true/false should be considered deprecated...
    	// throw an AbortException to indicate failure
        return true;
    }

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		// NONE since this is not dependent on the last step
		return BuildStepMonitor.NONE;
	}
	
	/**
	 * overridden for better type safety
	 */
	@Override
	public DescriptorImpl getDescriptor() {
		// TODO: does our own singleton have to be managed here?
		return (DescriptorImpl) super.getDescriptor();
	}

    public int getTeamId() {
		return teamId;
	}

	public int getProjectId() {
		return projectId;
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		
		private String url;

		private String apiToken;

		public DescriptorImpl() {
			// in order to load the persisted global configuration, you have to
			// call load() in the constructor
			load();
		}

		@Override
		public boolean isApplicable(@SuppressWarnings("rawtypes") final Class<? extends AbstractProject> jobType) {
			// applicable to all project types
			return true;
		}

		/**
         * This human readable name is used in the configuration screen.
         */
		@Override
		public String getDisplayName() {
			return "Publish scans to Threadfix";
		}

		@Override
		public boolean configure(final StaplerRequest staplerRequest, final JSONObject formData) throws FormException {
			url = formData.getString("url");
			apiToken = formData.getString("apiToken");
			
			save();
			
			return super.configure(staplerRequest, formData);
		}

		public String getUrl() {
			return url;
		}

		public String getApiToken() {
			return apiToken;
		}
		
	}

}
