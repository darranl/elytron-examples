/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.security.examples;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;

import javax.security.auth.kerberos.KerberosTicket;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpMethodConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.wildfly.security.auth.server.IdentityCredentials;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.auth.server.SecurityIdentity;
import org.wildfly.security.credential.Credential;
import org.wildfly.security.credential.GSSKerberosCredential;

/**
 * A simple secured HTTP servlet.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
@WebServlet("/secured")
@ServletSecurity(httpMethodConstraints = { @HttpMethodConstraint(value = "GET", rolesAllowed = { "Users" }) })
public class SecuredServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (PrintWriter writer = resp.getWriter()) {
            writer.println("<html>");
            writer.println("  <head><title>Secured Servlet</title></head>");
            writer.println("  <body>");
            writer.println("    <h1>Secured Servlet</h1>");
            writer.println("    <p>");
            writer.print(" Current Principal '");
            Principal user = req.getUserPrincipal();
            writer.print(user != null ? user.getName() : "NO AUTHENTICATED USER");
            writer.print("'");
            writer.println("    </p>");
            writer.println("    <p>");
            SecurityDomain securityDomain = SecurityDomain.getCurrent();
            SecurityIdentity currentIdentity = securityDomain.getCurrentSecurityIdentity();
            writer.print(" Current SecurityIdentity '");
            writer.print(currentIdentity.getPrincipal().getName());
            writer.print("<br>");
            IdentityCredentials credentials = currentIdentity.getPrivateCredentials();
            if (credentials.size() == 0) {
                writer.print("NO PRIVATE CREDENTIALS");
            } else {
                writer.print("Private Credentials");
                writer.print("<ul>");
                for (Credential current : credentials) {
                    writer.print("<li>" + current.getClass().getName());
                    if (current instanceof GSSKerberosCredential) {
                        GSSKerberosCredential gssKerbCred = (GSSKerberosCredential) current;
                        GSSCredential gssCredential = gssKerbCred.getGssCredential();
                        KerberosTicket ticket = gssKerbCred.getKerberosTicket();
                        writer.print("<ul>");
                            if (gssCredential != null) {
                                try {
                                    writer.print("<li>Name = " + gssCredential.getName().toString() + "</li>");
                                    writer.print("<li>Remaining Lifetime = " + gssCredential.getRemainingLifetime() + "</li>");
                                } catch (GSSException e) {
                                    throw new IOException(e);
                                }
                            } else {
                                writer.println("<li>GSSCredential is nullM</li>");
                            }

                            if (ticket != null) {
                                writer.print("<li>KerberosClient =" + ticket.getClient().getName() + "</li>");
                                writer.print("<li>KerberosServer =" + ticket.getServer().getName() + "</li>");
                                writer.print("<li>End Time = " + ticket.getEndTime().toString() + "</li>");
                            } else {
                                writer.println("<li>KerberosTicket is null</li>");
                            }
                        writer.print("</ul>");
                    }
                    writer.print("</li>");
                }
                writer.print("</ul>");
            }

            writer.println("    </p>");
            writer.println("  </body>");
            writer.println("</html>");
        }
    }

}
