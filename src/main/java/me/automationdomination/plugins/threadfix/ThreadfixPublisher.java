package me.automationdomination.plugins.threadfix;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;

import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Created with IntelliJ IDEA.
 * User: bspruth
 * Date: 3/22/14
 * Time: 12:05 AM
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("unchecked")
public class ThreadfixPublisher extends Recorder {

    private final String token;
    private final String url;
    private final String tfcli;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public ThreadfixPublisher(
    		final String token, 
    		final String url, 
    		final String tfcli) {
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

    // required per jenkins recorder
    public BuildStepMonitor getRequiredMonitorService() {
    	// NONE since this is not dependent on the last step
        return BuildStepMonitor.NONE;
    }

    /**
	 * This is what will be executed when the job is build. This also shows
	 * how you can use listener and build. Will be seen in the jenkins
	 * Console output
	 */
	@Override
	public boolean perform(
			final AbstractBuild<?, ?> build, 
			final Launcher launcher,
			final BuildListener listener) throws InterruptedException, IOException {
		final PrintStream log = launcher.getListener().getLogger();
		
		listener.getLogger().println("beginning threadfix publisher execution");
		
		log.println("Publishing Scan Results");
		log.println(
				"Using" + "Token" + token + "URL" + url + "Threadfix CLI"
						+ tfcli);
		log.println(
				"This is job number: " + build.getDisplayName());
		
		
		// TODO: 05/29/2014 - merged from old class... still needs to be cleaned up nand
//    	listener.getLogger().println("WORKSPACE: " + System.getenv("WORKSPACE"));
//    	listener.getLogger().println("JOB_NAME: " + System.getenv("JOB_NAME"));
//    	
//    	final File currentWorkingDirectory = new File(".");
//    	final File[] fileList = currentWorkingDirectory.listFiles();
//    	for (final File file : fileList) {
//    		if (file.isFile()) {
//    			listener.getLogger().println(file.getName());
//    			listener.getLogger().println(file.getAbsolutePath());
//    		}
//    	}
    	
//		PrintStream log = launcher.getListener().getLogger();		
//		log.println("Publishing Fortify 360 FPR Data");
//		
//		// calling the remote slave to retrieve the NVS
//		// build.getActions().add(new ChartAction(build.getProject()));
//		String jarsPath = DESCRIPTOR.getJarsPath();
//		String suggestedFortifyHome = null;
//		if ( !StringUtils.isBlank(jarsPath) ) {
//			// jarsPath should be <SCA_Install_Path>/Core/lib
//			File f = new File(jarsPath);
//			suggestedFortifyHome = f.getParentFile().getParentFile().toString();			
//		}
//		RemoteService service = new RemoteService(fpr, filterSet, searchCondition, suggestedFortifyHome);
//		FPRSummary summary = build.getWorkspace().act(service);
//		String logMsg = summary.getLogMessage();
//		if ( !StringUtils.isBlank(logMsg) ) log.println(logMsg);
//		
//		// if FPR is a remote FilePath, copy to local
//		File localFPR = null;
//		if ( summary.getFprFile().isRemote() ) {
//			localFPR = copyToLocalTmp(summary.getFprFile());
//		} else {
//			localFPR = new File(summary.getFprFile().toURI());
//		}
//		log.printf("Using FPR: %s\n", summary.getFprFile().toURI());
//		//if ( summary.getFprFile().isRemote() ) 
//		log.printf("Local FPR: %s\n", localFPR.getCanonicalFile());
//		log.printf("Calculated NVS = %f\n", summary.getNvs());
		
  
//		// save data under the builds directory, this is always in Hudson master node
//		log.println("Saving FPR summary");
//		summary.save(new File(build.getRootDir(), FPRSummary.FILE_BASENAME));
//		
//		// if the project ID is not null, then we need to upload the FPR to 360 server
//		if ( null != f360projId && f360projId > 0L && DESCRIPTOR.canUploadToF360() ) {
//			// the FPR may be in remote slave, we need to call launcher to do this for me
//			log.printf("Uploading FPR to Fortify 360 Server at %s\n", DESCRIPTOR.getUrl());
//			try {
//				Object[] args = new Object[] { localFPR, f360projId};
//				invokeFortifyClient(DESCRIPTOR.getToken(), "uploadFPR", args, log);
//				log.println("FPR uploaded successfully");
//			} catch ( Throwable t ) {
//				log.println("Error uploading to F360 Server: " + DESCRIPTOR.getUrl());
//				t.printStackTrace(log);
//			} finally {
//				// if this is a remote FPR, I need to delete the local temp FPR after use
//				if ( summary.getFprFile().isRemote() ) {
//					if ( null != localFPR && localFPR.exists() ) {
//						try { 
//							boolean deleted = localFPR.delete();
//							if ( !deleted ) log.printf("Can't delete local FPR file: %s\n", localFPR.getCanonicalFile());
//						} catch ( Exception e ) {
//							e.printStackTrace(log);
//						}
//					}
//				}
//			}
//		}
//		
//		// now check if the fail count
//		if ( !StringUtils.isBlank(searchCondition) ) {
//			Integer failedCount = summary.getFailedCount();
//			if ( null != failedCount && failedCount > 0 ) {
//				log.printf("Fortify 360 Plugin: this build is unstable because there are %d critical vulnerabilities\n", failedCount);
//				build.setResult(Result.UNSTABLE);
//			}
//		}
//		
//		// now do job assignment
//		if ( null != f360projId && f360projId > 0L && DESCRIPTOR.canUploadToF360() && !StringUtils.isBlank(auditScript) ) {
//			int sleep = (uploadWaitTime != null) ? uploadWaitTime : 1;
//			log.printf("Sleep for %d minute(s)\n", sleep);
//			sleep = sleep * 60 * 1000; // wait time is in minute(s)
//			long sleepUntil = System.currentTimeMillis() + sleep;
//			while(true) {
//				long diff = sleepUntil - System.currentTimeMillis();
//				if ( diff > 0 ) {
//					try {
//						Thread.sleep(diff);
//					} catch ( InterruptedException e ) { }
//				} else {
//					break;
//				}
//			}
//			log.printf("Auto JobAssignment, AuditToken = %s\n", auditToken);
//			try {
//				jobAssignment(build, log);
//			} catch ( Throwable t ) {
//				log.println("Error auditing FPR");
//				t.printStackTrace(log);
//			}
//		}
		
		// returning true/false should be considered deprecated...
    	// throw an AbortException to indicate failure
		return true;
	}

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
    	// TODO: does our own singleton have to be managed here?
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
        public FormValidation doCheckUrl(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please provide the URL of your Threadfix server");
            if (value.length() < 7)
                return FormValidation.warning("This is not a URL");
            return FormValidation.ok();
        }

        public boolean isApplicable(@SuppressWarnings("rawtypes") final Class<? extends AbstractProject> jobType) {
            // Indicates that this builder can be used with all kinds of project types
        	// applicable to all project types
            return true;
        }
        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Threadfix Scan to Threadfix";
        }
        
        // 05/29/2014: clean this up... from the old stuff...
        // private String url;
		// private String apiToken;

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        	// 05/29/2014: clean this up... from the old stuff...
        	// url = formData.getString("url");
			// apiToken = formData.getString("apiToken");
			
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
        
//        public String getUrl() {
//			return url;
//		}
//
//		public String getApiToken() {
//			return apiToken;
//		}
    }
}
