package me.automationdomination.plugins.threadfix;

public class ThreadfixUploadService {
	
	// 05/29/2014: copied from threadfix cli
//	public RestResponse<Scan> uploadScan(String applicationId, String filePath) {
//		return httpRestUtils.httpPostFile("/applications/" + applicationId + "/upload",
//                new File(filePath), new String[]{}, new String[]{}, Scan.class);
//	}
//	
//    @NotNull
//	public <T> RestResponse<T> httpPostFile(@NotNull String path,
//                                            @NotNull File file,
//                                            @NotNull String[] paramNames,
//                                            @NotNull String[] paramVals,
//                                            @NotNull Class<T> targetClass) {
//
//		//	TODO - Revisit how we handle certificate errors here
//		Protocol.registerProtocol("https", new Protocol("https", new AcceptAllTrustFactory(), 443));
//
//        String completeUrl = makePostUrl(path);
//
//		PostMethod filePost = new PostMethod(completeUrl);
//
//		filePost.setRequestHeader("Accept", "application/json");
//
//        RestResponse<T> response;
//        int status = -1;
//
//		try {
//			Part[] parts = new Part[paramNames.length + 2];
//			parts[paramNames.length] = new FilePart("file", file);
//            parts[paramNames.length + 1] = new StringPart("apiKey", propertiesManager.getKey());
//
//			for (int i = 0; i < paramNames.length; i++) {
//				parts[i] = new StringPart(paramNames[i], paramVals[i]);
//			}
//
//			filePost.setRequestEntity(new MultipartRequestEntity(parts,
//					filePost.getParams()));
//
//			filePost.setContentChunked(true);
//			HttpClient client = new HttpClient();
//            status = client.executeMethod(filePost);
//
//			if (status != 200) {
//                LOGGER.warn("Request for '" + completeUrl + "' status was " + status + ", not 200 as expected.");
//			}
//
//            response = ResponseParser.getRestResponse(filePost.getResponseBodyAsStream(), status, targetClass);
//
//        } catch (IOException e1) {
//            LOGGER.error("There was an error and the POST request was not finished.", e1);
//            response = ResponseParser.getErrorResponse(
//                    "There was an error and the POST request was not finished.",
//                    status);
//        }
//
//        return response;
//    }

}
