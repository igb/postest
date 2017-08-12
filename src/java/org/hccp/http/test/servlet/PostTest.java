package org.hccp.http.test.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by ibrown on 8/11/17.
 */
public class PostTest extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Collection<Part> parts = request.getParts();
        Iterator<Part> itr = parts.iterator();
        while (itr.hasNext()) {
            Part part = itr.next();
            String name = part.getName();
            System.out.println("part name = " + name);

        }

    }
}
