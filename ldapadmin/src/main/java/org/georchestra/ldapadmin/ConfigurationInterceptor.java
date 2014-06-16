/**
 *
 */
package org.georchestra.ldapadmin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Configuration interceptor
 *
 * <p>
 * This class adds the configuration parameters to model before calling Controllers.
 * </p>
 *
 * @author Sylvain Lesage
 *
 */
public class ConfigurationInterceptor extends HandlerInterceptorAdapter{

	@Autowired
	private Configuration config;

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
	    throws Exception {

		HttpSession currentSession = request.getSession();
		currentSession.setAttribute("publicContextPath", config.getPublicContextPath());
		return true;
	}

}
