package com.sap.cloud.samples.sendgridwrapperdemo;

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
		PrintWriter out = response.getWriter();
		String action = request.getParameter("action");
		
		HttpMail httpMail = new HttpMail(getServletContext());
		
		String from = request.getParameter("fromaddress");
        String to = request.getParameter("toaddress");
        String subjectText = request.getParameter("subjecttext");
        String mailText = request.getParameter("mailtext");
        if (action.equals("send")) {
        	httpMail.composeMail(from, to, subjectText, mailText);
        	String sendResult = httpMail.send();
        	if (sendResult.contains("success")) {
        		out.println("The email has been sent via Sendgrid using the Sendgrid Java library.");
        	} else {
        		out.println("Something has gone wrong, please check the logs...");
        	}
        } else {
        	out.println("No action selected");
        }
	}

}
