package me.automationdomination.plugins.threadfix.service;

import com.denimgroup.threadfix.data.entities.Organization;
import com.denimgroup.threadfix.data.entities.Scan;
import com.denimgroup.threadfix.remote.ThreadFixRestClient;
import com.denimgroup.threadfix.remote.ThreadFixRestClientImpl;
import com.denimgroup.threadfix.remote.response.RestResponse;

public class TfcliService {

	private final String url;
	private final String token;
	
	private final ThreadFixRestClient threadFixRestClient = new ThreadFixRestClientImpl();

	public TfcliService(
			final String url,
			final String token) {
		super();
		this.url = url;
		this.token = token;
	}
	
	public RestResponse<Scan> uploadFile(final String appId, final String file) {
		threadFixRestClient.setUrl(url);
		threadFixRestClient.setKey(token);
		return threadFixRestClient.uploadScan(appId, file);
	}
	
	public RestResponse<Organization[]> getAllTeams() {
		threadFixRestClient.setUrl(url);
		threadFixRestClient.setKey(token);
		return threadFixRestClient.getAllTeams();
	}

}
