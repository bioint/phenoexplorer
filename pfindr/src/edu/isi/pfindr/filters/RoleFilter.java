package edu.isi.pfindr.filters;

/* 
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import edu.isi.pfindr.helper.Utils;

/**
 * Servlet Filter implementation class RoleFilter
 * 
 * @author Serban Voinea
 */
@WebFilter(description = "Filter for managing the users", urlPatterns = { "/role" })
public class RoleFilter implements Filter {

	static private Logger logger = Logger.getLogger("AppLogging");       

	/**
	 * Default constructor. 
	 */
	public RoleFilter() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
		// place your code here
		String action = ((HttpServletRequest) request).getParameter("action");
		if (action != null && (action.equals("newuser") || action.equals("register") || action.equals("recaptcha") || action.equals("verify"))) {
			chain.doFilter(request, response);
			return;
		}
		HttpSession session = ((HttpServletRequest)request).getSession (false);
		if (session != null && session.getAttribute("newPhenotypeTableName") != null && 
				session.getAttribute("conn") != null && session.getAttribute("user") != null) {
			String tempTable = (String) session.getAttribute("newPhenotypeTableName");
			Connection conn = (Connection) session.getAttribute("conn");
			MDC.put("userid", session.getAttribute("user"));
			Utils.dropTable(conn, tempTable);
			session.removeAttribute("newPhenotypeTableName");
			session.removeAttribute("newPhenotypeTableCount");
			session.removeAttribute("newPhenotypeName");
			session.removeAttribute("newPhenotypeDescription");
			MDC.remove("userid");
		}
		if (!((HttpServletRequest) request).isRequestedSessionIdValid() || 
				session == null || session.getAttribute("isadmin") == null || 
				session.getAttribute("user") == null || session.getAttribute("conn") == null) {
			// send to login
			logger.info("No user session.");
			((HttpServletResponse)response).sendRedirect(((HttpServletRequest)request).getContextPath() + "/index.html");
		} else {
			action = request.getParameter("action");
			if (!((Boolean) session.getAttribute("isadmin")) && (action == null || !action.equals("change"))) {
				// send to query
				((HttpServletResponse)response).sendRedirect("/pfindr/query");
			} else {
				// pass the request along the filter chain
				MDC.put("userid", session.getAttribute("user"));
				chain.doFilter(request, response);
				MDC.remove("userid");
			}
		}
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
