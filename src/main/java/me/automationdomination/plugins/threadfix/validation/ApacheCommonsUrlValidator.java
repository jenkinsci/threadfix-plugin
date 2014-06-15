package me.automationdomination.plugins.threadfix.validation;

import org.apache.commons.validator.routines.UrlValidator;
import me.automationdomination.plugins.threadfix.ThreadFixPublisher;

/**
 * public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
 throws IOException, ServletException {

 HttpServletRequest req = (HttpServletRequest)request;

 String requestUrl = req.getRequestURL().toString();
 String queryString = req.getQueryString();
 if (queryString != null) {
 requestUrl += "?" + queryString;
 }

 if (urlValidator.isValid(requestUrl) && isValidQuery(queryString)) {
 chain.doFilter(request, response);
 } else {
 HttpServletResponse res = (HttpServletResponse)response;
 res.sendError(HttpServletResponse.SC_BAD_REQUEST);
 return;
 }
 */

public class ApacheCommonsUrlValidator implements ConfigurationValueValidator {

	private final UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https" }, UrlValidator.ALLOW_LOCAL_URLS);

	@Override
	public boolean isValid(final String value) {
		if (value == null || value.length() == 0)
			return false;
		
		if (!urlValidator.isValid(value))
			return false;
<<<<<<< HEAD
			String url = ThreadFixPublisher.DescriptorImpl.getCurrentDescriptorByNameUrl();
         */
        // TODO how do you access "url" from ThreadFixPublisher
        if (urlValidator.isValid(ThreadFixPublisher.DescriptorImpl.getCurrentDescriptorByNameUrl())) {
            System.out.println(ThreadFixPublisher.DescriptorImpl.getCurrentDescriptorByNameUrl() + " is valid");
        }
        else {
            System.out.println(ThreadFixPublisher.DescriptorImpl.getCurrentDescriptorByNameUrl() + " is invalid");
        }
=======
>>>>>>> 73c310abae6e2f5be4c818cedb612d7c2f2b5f22

		return true;
	}

}
