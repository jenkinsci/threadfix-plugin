package me.automationdomination.plugins.threadfix.service;

import com.denimgroup.threadfix.remote.ThreadFixRestClient;
import com.denimgroup.threadfix.remote.ThreadFixRestClientImpl;

public class TfcliService {
	
	
	
	private String url;
	private String token;
	
	private final ThreadFixRestClient threadFixRestClient = new ThreadFixRestClientImpl();

	public TfcliService(
			final String url,
			final String token) {
		super();
		this.url = url;
		this.token = token;
	}
	
	public void uploadFile(final String appId, final String file) {
		threadFixRestClient.setUrl(url);
		threadFixRestClient.setKey(token);
		threadFixRestClient.uploadScan(appId, file);
	}

}
