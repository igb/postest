package org.hccp.http.test.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;

public class ReqRespLogger implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("in filter!");

        Enumeration<String> headersEnum = ((HttpServletRequest)servletRequest).getHeaderNames();

        while (headersEnum.hasMoreElements()) {
            String s = headersEnum.nextElement();
            System.out.println(s + ": " +  ((HttpServletRequest)servletRequest).getHeader(s));
        }

        System.out.println();

        InputStream is = servletRequest.getInputStream();
        //OutputStream os = System.out;
        OutputStream os = new FileOutputStream("/tmp/req.log");

        byte[] buffer = new byte[1024];
        while(is.read(buffer) !=-1) {
            os.write(buffer);
            os.flush();
        }
        os.close();

        filterChain.doFilter(servletRequest, servletResponse);

    }

    @Override
    public void destroy() {
        System.out.println("destroyed!");

    }
}
