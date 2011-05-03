package org.apache.directory.samples.embed.webapp;

import java.io.PrintWriter;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.jndi.CoreContextFactory;

/**
 * A servlet which displays the Root DSE of the embedded server.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory
 *         Project</a>
 */
public class RootDseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet (HttpServletRequest req, HttpServletResponse resp) throws ServletException {

        try {
            resp.setContentType ("text/plain");
            PrintWriter out = resp.getWriter ();

            out.println ("*** ApacheDS RootDSE ***\n");

            DirContext ctx = new InitialDirContext (this.createEnv ());

            SearchControls ctls = new SearchControls ();
            ctls.setReturningAttributes (new String[] { "*", "+" });
            ctls.setSearchScope (SearchControls.OBJECT_SCOPE);

            NamingEnumeration<SearchResult> result = ctx.search ("", "(objectClass=*)", ctls);
            if (result.hasMore ()) {
                SearchResult entry = result.next ();
                Attributes as = entry.getAttributes ();

                NamingEnumeration<String> ids = as.getIDs ();
                while (ids.hasMore ()) {
                    String id = ids.next ();
                    Attribute attr = as.get (id);
                    for (int i = 0; i < attr.size (); ++i) {
                        out.println (id + ": " + attr.get (i));
                    }
                }
            }
            ctx.close ();

            out.flush ();
        } catch (Exception e) {
            throw new ServletException (e);
        }
    }

    /**
     * Creates an environment configuration for JNDI access.
     */
    protected Hashtable<Object, Object> createEnv () {

        // Fetch directory servive from servlet context
        ServletContext servletContext = this.getServletContext ();
        DirectoryService directoryService = (DirectoryService) servletContext.getAttribute (DirectoryService.JNDI_KEY);

        Hashtable<Object, Object> env = new Hashtable<Object, Object> ();
        env.put (DirectoryService.JNDI_KEY, directoryService);
        env.put (Context.PROVIDER_URL, "");
        env.put (Context.INITIAL_CONTEXT_FACTORY, CoreContextFactory.class.getName ());

        env.put (Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
        env.put (Context.SECURITY_CREDENTIALS, "secret");
        env.put (Context.SECURITY_AUTHENTICATION, "simple");

        return env;
    }
}
