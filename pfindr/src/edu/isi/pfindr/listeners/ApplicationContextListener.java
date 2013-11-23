package edu.isi.pfindr.listeners;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ApplicationContextListener implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent event) {
		// Do nothing
	}

	public void contextInitialized(ServletContextEvent event) {

		String realPath = event.getServletContext().getRealPath("/home.jsp");
		realPath = realPath.substring(0, realPath.lastIndexOf(File.separator) + 1);
		System.out.println("****Real Path:"+ realPath);
		
		ServletContextInfo.setContextPath(realPath);
		ServletContextInfo.setServletContext(event.getServletContext());
	}
}
