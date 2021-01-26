package org.wildfly.security.examples;

import static org.wildfly.security.examples.InfoServlet.NODE;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        req.login("alice", "alice");

        Principal principal = req.getUserPrincipal();
        String username = principal != null ? principal.getName() : "NOT AUTHENTICATED";
        try (PrintWriter writer = resp.getWriter()) {
            writer.println("Servlet called on " + NODE + " by " + username);
        }
    }

}
