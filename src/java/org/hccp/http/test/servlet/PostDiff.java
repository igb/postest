package org.hccp.http.test.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hccp.http.test.DiffTest;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class PostDiff extends HttpServlet  {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletContext ctxt = this.getServletContext();

        String testSessionId = getTestSessionId(req);

        DiffTest test = (DiffTest) ctxt.getAttribute(testSessionId);
        if (test != null && test.isDiffable()) {

            PrintWriter writer = resp.getWriter();
            writer.println("<html><body>");
            writer.println("<pre>");
            writer.println(test.diffHeaders());
            writer.println("</pre>");
            writer.println("</body><html>");

            resp.flushBuffer();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletContext ctxt = this.getServletContext();

        String testSessionId = getTestSessionId(req);
        int diffSourceId = getDiffSourceId(req);

        System.out.println("testSessionId = " + testSessionId);
        System.out.println("diffSourceId = " + diffSourceId);

        DiffTest test = (DiffTest) ctxt.getAttribute(testSessionId);
        if (test == null) {
            test = new DiffTest();
            ctxt.setAttribute(testSessionId, test);
        }

        String body = getBody(req);
        List<Header> headers = getHeaders(req);
        if (diffSourceId == 1) {
            test.setHeaders1(headers);
            test.setBody1(body);
        } else if (diffSourceId == 2) {
            test.setHeaders2(headers);
            test.setBody2(body);
        }

        if (test.isDiffable()) {
            resp.sendRedirect("/postest/diff/" +  testSessionId + "/");
        } else {
            resp.sendRedirect("/postest");
        }
    }

    private int getDiffSourceId(HttpServletRequest request) {
        String diffSourceIdStr = request.getRequestURI().substring(request.getRequestURI().indexOf(getTestSessionId(request)) + getTestSessionId(request).length() + 1);
        return Integer.parseInt(diffSourceIdStr);
    }

    private String getTestSessionId(HttpServletRequest request) {
        String path = getRelativePath(request);
        String testSessionId = path.substring(1, path.indexOf('/', 1));
        return testSessionId;

    }

    /**
     * Extracts relative path after servlet mapping prefix (inclusive of trailing '/').
     * @param request
     * @return
     */
    private String getRelativePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String servletPath = request.getServletPath();

        return uri.substring(uri.indexOf(servletPath) + servletPath.length());
    }

    private String getBody(HttpServletRequest req) throws IOException {
        InputStream is = req.getInputStream();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        return body;

    }

    private List<Header> getHeaders(HttpServletRequest req) {
        List<Header> headers = new LinkedList<>();

        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Header h = new Header(headerName, Collections.list(req.getHeaders(headerName)));
            headers.add(h);
        }
        return headers;
    }
}
