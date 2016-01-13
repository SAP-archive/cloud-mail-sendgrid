package com.sap.cloud.samples.sendgridmaildemo;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class TemplatesServlet
 */
@WebServlet("/templates")
public class TemplatesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static SendgridClient sClient;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TemplatesServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		sClient = SendgridClient.getInstance(getServletContext());
		if (request.getParameter("templateId") == null) {
			getAllTemplates(response);
		} else {
			getTemplate(response, request.getParameter("templateId"));
		}
		
		
	}
	
	private void getAllTemplates(HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		String extraURL = SendgridClient.actionSuffix.V3_GET_TEMPLATES.suffix();
		try {
			String responseContent = sClient.makeAPIV3GETCall(extraURL);
			out.println(responseContent);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();
			out.println("There was an error when trying to retrieve the list of templates...");
		}
	}

	private void getTemplate(HttpServletResponse response, String templateId) throws IOException {
		PrintWriter out = response.getWriter();
		String extraURL = SendgridClient.actionSuffix.V3_GET_TEMPLATES.suffix() + "/" + templateId;
		try {
			String responseContent = sClient.makeAPIV3GETCall(extraURL);
			out.println(responseContent);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();
			out.println("There was an error when trying to retrieve the list of templates...");
		}
	}

}
