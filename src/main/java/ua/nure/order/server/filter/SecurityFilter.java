package ua.nure.order.server.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.nure.order.entity.user.Role;
import ua.nure.order.entity.user.User;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * Servlet Filter implementation class SecurityFilter
 */
@WebFilter(
		dispatcherTypes = {DispatcherType.REQUEST }, 
		urlPatterns = { "/*" }, 
		initParams = { 
				@WebInitParam(name = "client", value = "/profile"),
				@WebInitParam(name = "admin", value = "/order/*,/profile,/book/*"),
				@WebInitParam(name = "userAttribute", value = "user"),
		})
public class SecurityFilter implements Filter {
	private static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);
	
	private static final LinkedHashMap<String, String[]> resources = new LinkedHashMap<>();
	private String ua = null;

	/**
	 * @see Filter#destroy()
	 */
	@Override
	public void destroy() {
		log.trace("SecurityFilter destroy start");
		// do nothing
		log.trace("SecurityFilter destroy finish");
	}
	
	private boolean accept(Role role, String res) {
		
		String sRole = role == null ? null : role.toString();
		
		// resource unrestricted
		if (!contains(res)) {
			log.debug("Accept access to unrestricted resource --> {}", res);
			return true;
		} 
		// unknown user go to the restricted resource
		if (!resources.containsKey(sRole) ) {
			log.debug("Deny access for Unknown user to the restricted resource --> {}", res);
			return false;
		} 
		// known user go to the restricted resource
		if (contains(res, resources.get(sRole))) {
			log.debug("Accept access for Known user to the restricted resource --> {}", res);
			return true;
		}
		return false;
	}
	
	private boolean contains(String res) {
		for (Entry<String, String[]> e : resources.entrySet()) {
			for (String r : e.getValue()) {
				log.debug("Matches --> {} Resource --> {}", r, res);
				if (res.matches(r)) {
					log.debug("Found matches --> {}", r);
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean contains(String res, String[] resources) {
		for (String r : resources) {
			if (res.matches(r)) {
				log.debug("Found matches --> {} in --> {}", res, resources);
				return true;
			}
		}
		return false;
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
			throws IOException, ServletException {
		// place your code here
		log.trace("SecurityFilter filter start");

		HttpServletRequest req = (HttpServletRequest) request;
		HttpSession session = req.getSession();
		User user = null;
		if (session.isNew()) {
			user = new User();
			session.setAttribute(ua, user);
		}
		user = (User) session.getAttribute(ua);
		log.debug("User --> {}", user);
		
		String contextPath = req.getServletContext().getContextPath();
		log.debug("ContextPath --> {}", contextPath);
		
		String resourse = req.getRequestURI().substring(contextPath.length());
		log.debug("Resourse --> {}", resourse);
		
		if (!accept(user.getRole(), resourse)) {
			((HttpServletResponse) response).sendRedirect(req.getContextPath() + "/login.jsp");
			return;
		}
		
		// pass the request along the filter chain
		chain.doFilter(request, response);
		
		log.trace("SecurityFilter finish");
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		log.trace("SecurityFilter init start");
		String contextPath = fConfig.getServletContext().getContextPath();
		log.debug("contextPath --> {}", contextPath);
		Enumeration<String> roles = fConfig.getInitParameterNames();
		while (roles.hasMoreElements()) {
			String role = roles.nextElement();
			log.debug("Init param --> {}", role);
			if (role.equals("userAttribute")) {
				ua = fConfig.getInitParameter(role);
				log.debug("User session attribute name --> {}", ua);
				continue;
			}
			String[] path = fConfig.getInitParameter(role).split(",");
			Arrays.sort(path);
			for (int i = 0; i < path.length; i++) {
				path[i] = path[i].replace("*", ".*");
			}
			resources.put(role, path);
			log.debug("deny --> {}: {}", role, Arrays.toString(path));
		}
		log.trace("SecurityFilter init finish");
	}

}
