package com.sap.cloud.samples.sendgridmaildemo;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class HttpServlet
 */
@WebServlet("/http")
public class HttpMailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see javax.servlet.http.HttpServlet#javax.servlet.http.HttpServlet()
     */
    public HttpMailServlet() {
        super();
    }

	/**
	 * @see HttpMailServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//TODO: TO check if there is a template involved and send the email using it in this case
		PrintWriter out = response.getWriter();
		String action = request.getParameter("action");
		String templateId = request.getParameter("templateId");
		
		SendgridClient sendgridClient = SendgridClient.getInstance(getServletContext());
		HttpMail httpMail = new HttpMail(sendgridClient);
		
		String from = request.getParameter("fromaddress");
        String to = request.getParameter("toaddress");
        String subjectText = request.getParameter("subjecttext");
        String mailText = request.getParameter("mailtext");
        try {
	        if (action.equals("send")) {
	        	httpMail.composeMail(from, to, subjectText, mailText, templateId);
	        	
	        	String sendResult = httpMail.send();
	        	if (sendResult.contains("success")) {
	        		out.println("The email has been sent via Sendgrid using the Sendgrid HTTP API.");
	        		out.println(sendResult);
	        	} else {
	        		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        		out.println("Something has gone wrong, please check the logs...");
	        		out.println(sendResult);
	        	}
	        } else {
	        	out.println("No action selected");
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        	out.println("There was an error sending mail via HTTP API: " + e.getMessage());
        }
	}

}
