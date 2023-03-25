package ua.nure.order.server.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Forwards to login page when the current session is invalidate.
 */
public class SessionInvalidateFilter implements Filter {

	private static final Logger log = LoggerFactory.getLogger(SessionInvalidateFilter.class);

	/**
	 * Forwards to login page when the current session is invalidate.
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		log.debug("Filter starts");

		// request is the HTTP request at this point
		// cast it to HttpServletRequest to get possibility to obtain the HTTP session
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;

		// if there is no a valid session
		if (httpServletRequest.getSession(false) == null) {
			// create the new session
			httpServletRequest.getSession(true);

			// and forward to the login page
			httpServletRequest.getRequestDispatcher("/login.jsp").forward(request, response);
			return;
		}

		// otherwise go further
		chain.doFilter(request, response);
		log.debug("Filter finished");
	}

}
