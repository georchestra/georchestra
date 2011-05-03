/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * 
 */
package org.apache.directory.samples.embed.webapp;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.directory.server.core.CoreSession;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.entry.DefaultServerEntry;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifReader;

/**
 * A Servlet context listener to start and stop ApacheDS.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory
 *         Project</a>
 */
public class StartStopListener implements ServletContextListener {

    private DirectoryService directoryService;

    private LdapServer       ldapServer;

    /**
     * Startup ApacheDS embedded.
     */
    public void contextInitialized (ServletContextEvent evt) {

        try {
            directoryService = new DefaultDirectoryService ();
            directoryService.setShutdownHookEnabled (true);

            ldapServer = new LdapServer ();
            ldapServer.setDirectoryService (directoryService);
            ldapServer.setAllowAnonymousAccess (true);

            // Set LDAP port to 10389
            String port = evt.getServletContext().getInitParameter("port");
            TcpTransport ldapTransport = new TcpTransport (Integer.parseInt(port));
            ldapServer.setTransports (ldapTransport);

            // Determine an appropriate working directory
            ServletContext servletContext = evt.getServletContext ();
            File tmp = (File) servletContext.getAttribute ("javax.servlet.context.tempdir");
            File workingDir = new File (tmp, "ldap");
            boolean needsInit = !workingDir.exists ();
            directoryService.setWorkingDirectory (workingDir);


            directoryService.startup ();
            ldapServer.start ();
            
            servletContext.setAttribute( DirectoryService.JNDI_KEY, directoryService );
            
            if (needsInit) {
                importLdif (directoryService);
            }
            
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    protected void importLdif (DirectoryService directoryService2) throws Exception {
        CoreSession ctx = directoryService.getAdminSession ();
        if (ctx == null) {
            throw new RuntimeException ("Admin Session is not available");
        }
        ClassLoader classLoader = getClass ().getClassLoader ();
        InputStream in = classLoader.getResourceAsStream ("default_" + InetAddress.getLocalHost ().getHostName ()
                + ".ldif");
        if (in == null) {
            in = classLoader.getResourceAsStream ("default.ldif");
        }
        if (in == null) {
            throw new RuntimeException ("ldif file was not found");
        }
        for (LdifEntry ldifEntry : new LdifReader (in)) {
            ctx.add (new DefaultServerEntry (directoryService.getRegistries (), ldifEntry.getEntry ()));
        }
    }

    /**
     * Shutdown ApacheDS embedded.
     */
    public void contextDestroyed (ServletContextEvent evt) {
        try {
            ldapServer.stop ();
            directoryService.shutdown ();
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }
}
