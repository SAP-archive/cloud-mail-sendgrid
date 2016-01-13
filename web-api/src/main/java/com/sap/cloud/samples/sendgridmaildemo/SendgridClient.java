package com.sap.cloud.samples.sendgridmaildemo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.KeyStore.LoadStoreParameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.connectivity.api.DestinationException;
import com.sap.core.connectivity.api.DestinationFactory;
import com.sap.core.connectivity.api.DestinationNotFoundException;
import com.sap.core.connectivity.api.http.HttpDestination;

public final class SendgridClient {
	private static SendgridClient instance = null;

	private static final Logger LOGGER = LoggerFactory.getLogger(SendgridClient.class);
	
	private static final String DEFAULT_PROPS_FILE_LOCATION = "/WEB-INF/lib/sendgrid.properties";

	private static HttpDestination sendgridDestination;
	private static HttpDestination sendgridV3Destination;
	
	public static Properties sendgridProps;
	
	public static enum actionSuffix {
		SEND ("mail.send."),
		GET_PROFILE ("profile.get."),
		SET_PROFILE ("profile.set."),
		V3_GET_TEMPLATES ("templates"),
		RS_ADD_USER("reseller.add.");

		private String suffix;

		actionSuffix(String suffix) {
			this.suffix = suffix;
		}

		public String suffix() {
			return suffix;
		}
	}

	public static enum actionFormat {
		JSON ("json"),
		XML ("xml");

		private String format;

		actionFormat(String format) {
			this.format = format;
		}

		public String format() {
			return format;
		}
	}

	private SendgridClient() {
		Context ctx;
		try {
			ctx = new InitialContext();
			DestinationFactory destinationFactory = (DestinationFactory)ctx.lookup(DestinationFactory.JNDI_NAME);
			sendgridDestination = (HttpDestination) destinationFactory.getDestination(sendgridProps.getProperty("SENDGRID_API_DESTINATION"));
			sendgridV3Destination = (HttpDestination) destinationFactory.getDestination(sendgridProps.getProperty("SENDGRID_API_V3_DESTINATION"));
		} catch (NamingException | DestinationNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static SendgridClient getInstance(ServletContext servletContext) {
		synchronized(SendgridClient.class) {
			if (instance == null) {
				SendgridClient.loadPropertiesFromContext(servletContext);
				instance = new SendgridClient();
			}
		}
		return instance;
	}

	private static void loadPropertiesFromContext(ServletContext servletContext) {
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
	
	public String makeAPIPOSTCall(String extraURL, List<NameValuePair> headerParams, List<NameValuePair> params)
			throws ClientProtocolException, URISyntaxException, DestinationException, IOException {
		params.add(new BasicNameValuePair("api_user", sendgridProps.getProperty("SENDGRID_USER")));
		params.add(new BasicNameValuePair("api_key", sendgridProps.getProperty("SENDGRID_PASSWORD")));
		return makePOSTCall(sendgridDestination, extraURL, headerParams, params);
	}


	public String makeAPIV3GETCall(String extraURL, List<NameValuePair> headerParams)
			throws ClientProtocolException, URISyntaxException, DestinationException, IOException {
		headerParams.add(new BasicNameValuePair("Authorization", "Bearer " + sendgridProps.getProperty("SENDGRID_API_KEY")));
		return makeGETCall(sendgridV3Destination, extraURL, headerParams);
	}

	public String makeAPIV3GETCall(String extraURL)
			throws ClientProtocolException, URISyntaxException, DestinationException, IOException {
		List<NameValuePair> headerParams = new ArrayList<>();
		return makeAPIV3GETCall(extraURL, headerParams);
	}

	private String makePOSTCall(HttpDestination destination, String extraURL, List<NameValuePair> headerParams, List<NameValuePair> params)
			throws URISyntaxException, DestinationException, IOException, ClientProtocolException {
		String url = destination.getURI() + extraURL;
		HttpClient client = destination.createHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);

		HttpPost request = new HttpPost(url);
		request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

		HttpResponse response = client.execute(request);
		return EntityUtils.toString(response.getEntity());
	}

	private String makeGETCall(HttpDestination destination, String extraURL, List<NameValuePair> headerParams)
			throws URISyntaxException, DestinationException, IOException, ClientProtocolException {
		String url = destination.getURI() + extraURL;
		HttpClient client = destination.createHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);

		HttpGet request = new HttpGet(url);
		for (NameValuePair headerParam : headerParams) {
			request.addHeader(headerParam.getName(), headerParam.getValue());
		}

		HttpResponse response = client.execute(request);
		return EntityUtils.toString(response.getEntity());
	}
}
