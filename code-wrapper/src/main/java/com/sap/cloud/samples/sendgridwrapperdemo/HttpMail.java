package com.sap.cloud.samples.sendgridwrapperdemo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
import com.sendgrid.SendGrid;
import com.sendgrid.SendGrid.Email;
import com.sendgrid.SendGridException;

public class HttpMail {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpMail.class);
	
	private static final String SENDGRID_API_DESTINATION = "SendgridAPI";
	private static final String DEFAULT_PROPS_FILE_LOCATION = "/WEB-INF/lib/sendgrid.properties";
	private static ConnectivityConfiguration connectivityConfiguration;
	private static DestinationConfiguration destConfiguration;
	
	private SendGrid sendgrid;
	private Email email;

	public Properties sendgridProps;
	
	static {
		Context ctx;
		try {
			ctx = new InitialContext();
			connectivityConfiguration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
			destConfiguration = connectivityConfiguration.getConfiguration(SENDGRID_API_DESTINATION);
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	public HttpMail(ServletContext servletContext) {
		loadPropertiesFromContext(servletContext);
		sendgrid = new SendGrid(sendgridProps.getProperty("SENDGRID_API_KEY"));
		try {
			sendgrid.setClient(makeClient());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void loadPropertiesFromContext(ServletContext servletContext) {
		sendgridProps = new Properties();
		try {
			InputStream stream = servletContext.getResourceAsStream(DEFAULT_PROPS_FILE_LOCATION);
			try {
				sendgridProps.load(stream);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				stream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void composeMail(String from, String to, String subject, String body) {
		email = new Email();
		email.setFrom(from);
		
		InternetAddress[] toAddresses;
		try {
			toAddresses = InternetAddress.parse(to);
			for (int i = 0; i < toAddresses.length; i++) {
				String name = toAddresses[i].getPersonal() == null ? "" : toAddresses[i].getPersonal();
				email.addTo(toAddresses[i].getAddress(), name);
			}
		} catch (AddressException e) {
			e.printStackTrace();
		}
		
		email.setSubject(subject);
		email.setText(body);
		
	}
	
	public String send() {
		try {
		      SendGrid.Response response = sendgrid.send(email);
		      return response.getMessage();
	    }
	    catch (SendGridException e) {
	      LOGGER.error(e.getMessage());
	    }
		
		return "There was an error when sending the message, please see the logs."; 
	}
	
	private CloseableHttpClient makeClient() throws Exception {
		String proxyType = destConfiguration.getProperty("ProxyType");
		HttpHost proxy = getProxy(proxyType);
		if (proxy == null)
			throw new Exception("Unable to get system proxy");
		CloseableHttpClient newClient = HttpClientBuilder.create().setProxy(proxy).build();
		return newClient;
	}
	
	private HttpHost getProxy(String proxyType) {
        String proxyHost = null;
        int proxyPort = 0;
        if (proxyType.equals("Internet")) {
            // Get proxy for internet destinations
            proxyHost = System.getProperty("http.proxyHost");
            proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
            return new HttpHost(proxyHost, proxyPort);
        }
        return null;
    }
}
