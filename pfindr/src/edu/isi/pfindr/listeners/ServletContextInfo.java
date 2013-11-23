package edu.isi.pfindr.listeners;

import javax.servlet.ServletContext;

public class ServletContextInfo {
	
	private static String contextPath;
	private static ServletContext servletContext;

	public static String getContextPath() {
		return contextPath;
	}
	public static ServletContext getServletContext() {
		return servletContext;
	}

	public static void setContextPath(String contextPath) {
		ServletContextInfo.contextPath = contextPath;
	}
	
	public static void setServletContext(ServletContext servletContext){
		ServletContextInfo.servletContext = servletContext; 
	}

}
