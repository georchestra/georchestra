/**
 * 
 */
package extractorapp.ws.extractor;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.support.AbstractContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 * @author Mauricio Pazos
 * 
 */
public class MockWebContextLoader extends AbstractContextLoader {

	public static final ServletContext SERVLET_CONTEXT = new MockServletContext(
			"/WebContent", new FileSystemResourceLoader());

	private final static GenericWebApplicationContext webContext = new GenericWebApplicationContext();

	protected BeanDefinitionReader createBeanDefinitionReader(
			final GenericApplicationContext context) {
		return new XmlBeanDefinitionReader(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.test.context.ContextLoader#loadContext(java.lang.
	 * String[])
	 */
	@Override
	public ConfigurableApplicationContext loadContext(String... locations)
			throws Exception {

		SERVLET_CONTEXT.setAttribute(
				WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
				webContext);
		webContext.setServletContext(SERVLET_CONTEXT);
		createBeanDefinitionReader(webContext).loadBeanDefinitions(locations);
		AnnotationConfigUtils.registerAnnotationConfigProcessors(webContext);
		webContext.refresh();
		webContext.registerShutdownHook();
		return webContext;
	}

	public static WebApplicationContext getInstance() {
		return webContext;
	}

	public static WebApplicationContext getWebAppContext() {
		return getInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.test.context.support.AbstractContextLoader#
	 * getResourceSuffix()
	 */
	@Override
	protected String getResourceSuffix() {
		return "-context.xml";
	}

}
