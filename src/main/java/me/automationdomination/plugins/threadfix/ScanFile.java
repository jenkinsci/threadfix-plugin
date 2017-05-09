package me.automationdomination.plugins.threadfix;

import java.io.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

/**
 * An artifact that can be uploaded to a ThreadFix server for an
 * application
 */
public final class ScanFile extends AbstractDescribableImpl<ScanFile> implements Serializable {

    private final String path;

    @DataBoundConstructor
    public ScanFile(final String path) {
        this.path = path;
    }

    /**
     * Returns the path of the scan file
     *
     * @return
     */
    public String getPath() {
        return path;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ScanFile> {

        private static final String DISPLAY_NAME = "Scan File";

        private static final String PATH_PARAMETER = "path";

        /**
         * This human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return DISPLAY_NAME;
        }

        /**
         * Perofrms on-the-fly validation of the scan file path
         *
         * @param path
         * @return
         */
        public FormValidation doCheckPath(@QueryParameter(PATH_PARAMETER) final String path) {
            if (path == null || path.isEmpty()) {
                return FormValidation.error("path cannot be blank");
            } else {
                return FormValidation.ok();
            }
        }
    }
}
