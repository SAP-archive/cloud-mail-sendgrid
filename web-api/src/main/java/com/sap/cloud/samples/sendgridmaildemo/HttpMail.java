package com.sap.cloud.samples.sendgridmaildemo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.connectivity.api.DestinationException;
import com.sap.core.connectivity.api.DestinationFactory;
import com.sap.core.connectivity.api.DestinationNotFoundException;
import com.sap.core.connectivity.api.http.HttpDestination;

public class HttpMail {
	private static SendgridClient sClient;
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpMail.class);
	
	private String from, subject, body, templateId;
	private ArrayList<String> to, toName;
	private String[] templateSubs = {":first_name", ":order_number"};
	
	public HttpMail(SendgridClient sendgridClient) {
		sClient = sendgridClient;
	}
	
	public void composeMail(String from, String to, String subject, String body, String templateId) {
		setFrom(from); 
		setTo(to); 
		setSubject(subject); 
		setBody(body);
		setTemplateId(templateId);
	}
	
	public String send() throws ClientProtocolException, URISyntaxException, DestinationException, IOException {
		String extraURL = SendgridClient.actionSuffix.SEND.suffix() + SendgridClient.actionFormat.JSON.format();
		return sClient.makeAPIPOSTCall(extraURL, null, makeParams());
	}
	
	private List<NameValuePair> makeParams() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		params.add(new BasicNameValuePair("from", from));
		
		for (int i = 0; i < this.to.size(); i++) {
			params.add(new BasicNameValuePair("to[]", getTo().get(i)));
			params.add(new BasicNameValuePair("toname[]", getToName().get(i)));
		}
		
		params.add(new BasicNameValuePair("subject", getSubject()));
		
		if (templateId.isEmpty()) {
			params.add(new BasicNameValuePair("text", getBody()));
		} else {
			params.add(new BasicNameValuePair("x-smtpapi", makeTemplateParam()));
			params.add(new BasicNameValuePair("text", " "));
		}
				
		return params;
	}

	private String makeTemplateParam() {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("to", getTo());
		
		Map<String, ArrayList<String>> subs = new HashMap<>();
		subs.put(templateSubs[0], getToName());
		
		ArrayList<String> orderNumberSubs = new ArrayList<>();
		for (int i = 0; i < getTo().size(); i++) {
			orderNumberSubs.add(String.valueOf((int) Math.random()*1000000 + 1000000));
		}
		subs.put(templateSubs[1], orderNumberSubs);
		
		jsonObj.put("sub", subs);
		
		Map<String, String> settings = new HashMap<>();
		settings.put("enable", "1");
		settings.put("template_id", getTemplateId());
		JSONObject templates = new JSONObject();
		templates.put("settings", settings);
		
		JSONObject filters = new JSONObject();
		filters.put("templates", templates);
		
		jsonObj.put("filters", filters);
		
		return jsonObj.toString();
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public ArrayList<String> getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = new ArrayList<String>();
		this.toName = new ArrayList<String>();
		
		InternetAddress[] toAddresses;
		try {
			toAddresses = InternetAddress.parse(to);
			for (int i = 0; i < toAddresses.length; i++) {
				this.to.add(toAddresses[i].getAddress());
				this.toName.add(toAddresses[i].getPersonal() == null ? toAddresses[i].getAddress() : toAddresses[i].getPersonal());
			}
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<String> getToName() {
		return this.toName;
		
	}
	
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	public String getTemplateId() {
		return this.templateId;
		
	}
	
	private void setTemplateId(String templateId) {
		this.templateId = templateId;
		
	}
	
}
