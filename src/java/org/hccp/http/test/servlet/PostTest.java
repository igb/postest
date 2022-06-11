package org.hccp.http.test.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ibrown on 8/11/17.
 */
public class PostTest extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("got a request...");
        System.out.println("request.getHeader(\"Content-Type\") = " + request.getHeader("Content-Type"));
        System.err.println("Content-Length: " + request.getHeader("Content-Length"));
      //  InputStream is = request.getInputStream();

       /* byte[] buffer = new byte[1024];
        int i = -1;
        List<Integer> count = new LinkedList<Integer>();
        while ( (i = is.read()) > 0) {
            count.add(i);
        }*/

      //  System.out.println("count = " + count.size());


        Collection<Part> parts = request.getParts();
        Iterator<Part> itr = parts.iterator();

        while (itr.hasNext()) {
            Part part = itr.next();
            String name = part.getName();
            System.out.println("part name = " + name);
            System.out.println("part.getContentType() = " + part.getContentType());

            if (name.equals("media")) {
                System.out.println("writing media...");
                FileOutputStream fos = new FileOutputStream("/tmp/foo3000.png");
                InputStream is = part.getInputStream();
                byte[] buffer = new byte[1024];
                while (is.read(buffer) > 0) {
                    fos.write(buffer);
                }

                fos.flush();
                fos.close();
            }



        }



        response.flushBuffer();

    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("got a get request...");


        response.getWriter().print("postest is running");
    }
}
