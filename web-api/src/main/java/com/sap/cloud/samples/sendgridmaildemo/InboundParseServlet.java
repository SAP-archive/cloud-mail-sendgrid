package com.sap.cloud.samples.sendgridmaildemo;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class InboundParseServlet
 */
@WebServlet("/inbound")
@MultipartConfig
public class InboundParseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(InboundParseServlet.class);
    
    private static final String hardCodedSubject = "New email arrived via Sendgrid Inbound Webhook";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InboundParseServlet() {
        super();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String 	to = request.getParameter("to"),
				from = request.getParameter("from"),
				subject = request.getParameter("subject"),
				text = request.getParameter("text");
		
		SendgridClient sendgridClient = SendgridClient.getInstance(getServletContext());
		HttpMail httpMail = new HttpMail(sendgridClient);
		
		try {
        	httpMail.composeMail(sendgridClient.sendgridProps.getProperty("INBOUND_FROM"),
				        			sendgridClient.sendgridProps.getProperty("INBOUND_TO"),
				        			hardCodedSubject, 
				        			makeInboundMailText(to, from, subject, text), "");
        	httpMail.send();
        } catch (Exception e) {
        	e.printStackTrace();
        	LOGGER.error("There was an error when processing email via Sendgrid inbound webhook: " + e.getMessage(), e);
        }
	}
	
	private String makeInboundMailText(String to, String from, String subject, String text) {
		String returnText = "An email was received via Sendgrid Inbound Webhook.\n";
		returnText += "It was automatically categorized as: " + getCategory(subject) + ".\n\n";
		returnText += "Email follows:\n\n";
		returnText += "From: " + from + "\n";
		returnText += "To: " + to + "\n";
		returnText += "Subject: " + subject + "\n\n";
		returnText += "Mail text: " + text + "\n";
		
		return returnText;
	}

	/**
	 * This is a basic (i.e. very dumb) email categorization method. It will categorize the emails based on the content of its subject.
	 * For demonstration purposes, we're considering the following keywords: "fw:", "support", "error", "question", "ref:" and "order.
	 * @param subject - the subject text
	 * @return a string whose meaning is the category of the email. Possible values are: 
	 * "FORWARDED", "SUPPORT REQUEST", "CUSTOMER SERVICE", "SALES ORDER TRACKING" and "GENERAL".
	 */
	private String getCategory(String subject) {
		if (subject.toLowerCase().startsWith("fw:"))
			return "FORWARDED";
		if (subject.toLowerCase().contains("support") || subject.toLowerCase().contains("error"))
			return "SUPPORT REQUEST";
		if (subject.toLowerCase().contains("question"))
			return "CUSTOMER SERVICE";
		if (subject.toLowerCase().startsWith("ref:") || subject.toLowerCase().contains("order"))
			return "SALES ORDER TRACKING";
		return "GENERAL";
	}
	
	

}
