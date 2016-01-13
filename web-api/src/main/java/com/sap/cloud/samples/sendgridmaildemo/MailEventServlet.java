package com.sap.cloud.samples.sendgridmaildemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class MailEventServlet
 */
@WebServlet("/mailevent")
public class MailEventServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MailEventServlet.class);      
    
    private static final String hardCodedSubject = "New email event from Sendgrid Event Webhook";
    
    public MailEventServlet() {
        super();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		SendgridClient sendgridClient = SendgridClient.getInstance(getServletContext());
		HttpMail httpMail = new HttpMail(sendgridClient);
		
		try {
        	httpMail.composeMail(sendgridClient.sendgridProps.getProperty("EMAIL_EVENT_FROM"),
        						sendgridClient.sendgridProps.getProperty("EMAIL_EVENT_TO"),
        						hardCodedSubject, 
        						makeInboundMailText(getBodyContent(request)), "");
        	httpMail.send();
        } catch (Exception e) {
        	e.printStackTrace();
        	LOGGER.error("There was an error when processing email via Sendgrid inbound webhook: " + e.getMessage(), e);
        }
	}
	
	private JSONArray getBodyContent(HttpServletRequest request) {
		StringBuilder content = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = request.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line);
			}
			reader.close();
		} catch (IOException e) {
			LOGGER.error("There was an error when trying to read the request body: " + e.getMessage(), e);
		}
		
		JSONArray jsonArr = new JSONArray(content.toString());
				
		return jsonArr;
	}
	
	private String makeInboundMailText(JSONArray eventArray) {
		StringBuilder returnText = new StringBuilder();
		
		returnText.append("The following emails had problems being delivered:\n\n");
		
		for (int i = 0; i < eventArray.length(); i++) {
			JSONObject currentEvent = eventArray.getJSONObject(i);
			if (!currentEvent.getString("event").equals("dropped") && !currentEvent.getString("event").equals("bounced"))
				continue;
			returnText.append("Emails sent to "); 
			returnText.append(currentEvent.getString("email"));
			returnText.append(" could not be delievered.\n");
			returnText.append("Reason: ");
			returnText.append(currentEvent.getString("reason"));
			returnText.append(".\n\n");
		}
		
		return returnText.toString();
	}

}
