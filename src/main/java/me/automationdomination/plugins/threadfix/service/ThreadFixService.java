package me.automationdomination.plugins.threadfix.service;

import com.denimgroup.threadfix.data.entities.Organization;
import com.denimgroup.threadfix.data.entities.Scan;
import com.denimgroup.threadfix.remote.ThreadFixRestClient;
import com.denimgroup.threadfix.remote.ThreadFixRestClientImpl;
import com.denimgroup.threadfix.remote.response.RestResponse;
import hudson.FilePath;

import java.io.Serializable;

/**
 * A proxy to the actual {@link ThreadFixRestClient}. An abstraction
 * for communicating with the actual ThreadFix server
 */
public class ThreadFixService implements Serializable {

    private static final long serialVersionUID = -4389106192185067967L;

    private final String url;
    private final String token;

    private final ThreadFixRestClient threadFixRestClient = new ThreadFixRestClientImpl();

    public ThreadFixService(final String url, final String token) {
        super();
        this.url = url;
        this.token = token;
    }

    /**
     * Uploads the parameter scan file for the parameter application ID.
     * Returns true if the upload was successful, otherwise false.
     *
     * @param appId
     * @param filePath
     * @return
     */
    public boolean uploadFile(final String appId, final FilePath filePath) {
        threadFixRestClient.setUrl(url);
        threadFixRestClient.setKey(token);

        final RestResponse<Scan> uploadFileResponse = threadFixRestClient.uploadScan(appId, filePath.getRemote());
        return uploadFileResponse.success;
    }

    /**
     * Retrieves a listing of teams
     *
     * @return
     */
    public RestResponse<Organization[]> getAllTeams() {
        threadFixRestClient.setUrl(url);
        threadFixRestClient.setKey(token);
        return threadFixRestClient.getAllTeams();
    }

}
