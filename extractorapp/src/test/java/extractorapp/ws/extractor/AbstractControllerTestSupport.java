/**
 * 
 */
package extractorapp.ws.extractor;

import java.util.Map;  

import javax.servlet.ServletException;  
  
import junit.framework.TestCase;  
  
import org.apache.log4j.Logger;
import org.junit.runner.RunWith;  
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;  
import org.springframework.mock.web.MockHttpServletResponse;  
import org.springframework.mock.web.MockServletConfig;  
import org.springframework.web.context.WebApplicationContext;  
import org.springframework.web.servlet.DispatcherServlet;  


import org.springframework.test.context.ContextConfiguration;  
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;  
import org.springframework.test.context.support.AbstractContextLoader;

/**
 * @author Mauricio Pazos
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)  
@ContextConfiguration(loader=MockWebContextLoader.class, locations={"file:///home/mauro/devel-box/projects/georchestra/trunk/extractorapp/src/test/java/extractorapp/ws/extractor/config/ws-servlet-test.xml"})  
public class AbstractControllerTestSupport extends AbstractContextLoader {
	 private static DispatcherServlet dispatcherServlet;  
	 
	 
	    @SuppressWarnings("serial")  
	    public static DispatcherServlet getServletInstance() {  
	            if(null == dispatcherServlet) {  
	                dispatcherServlet = new DispatcherServlet() {  
	                    protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {  
	                        return MockWebContextLoader.getWebAppContext();  
	                    }  
	                };  
	                try {  
	                  dispatcherServlet.init(new MockServletConfig());  
	                } catch (ServletException se) {  
	                  // log exception  
	                }  
	            }  
	        return dispatcherServlet;  
	    }  
	   
	    protected MockHttpServletRequest mockRequest(String method, String uri, Map<String ,String> params) {  
	        MockHttpServletRequest req = new MockHttpServletRequest(method, uri);  
	        if (params != null) {  
	          for(String key : params.keySet()) {  
	              req.addParameter(key, params.get(key));  
	          }  
	        }  
	        return req;  
	    }  
	   
	    protected MockHttpServletResponse mockResponse() {  
	        return new MockHttpServletResponse();  
	    }

		@Override
		public ApplicationContext loadContext(String... arg0) throws Exception {
			
			return MockWebContextLoader.getWebAppContext(); 
		}

		@Override
		protected String getResourceSuffix() {
			// TODO Auto-generated method stub
			return null;
		}  

}
