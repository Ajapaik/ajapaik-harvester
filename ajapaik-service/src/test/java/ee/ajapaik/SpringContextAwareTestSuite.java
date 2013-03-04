package ee.ajapaik;

import java.util.Properties;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler.Context;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * TestSuite with spring initialization
 * 
 * @author <a href="mailto:kaido@quest.ee?subject=TestSuite">Kaido Kalda</a>
 */
public class SpringContextAwareTestSuite {

	public static WebApplicationContext SERVER_CONTEXT;
	public static GenericApplicationContext TESTING_CONTEXT;
	
	public static Server server;

	public void startJettyContext() throws Exception {
		Properties properties = new Properties();
		properties.load(SpringContextAwareTestSuite.class.getResourceAsStream("/server-config.properties"));
		
    	SelectChannelConnector httpConnector = new SelectChannelConnector();
    	httpConnector.setPort(Integer.valueOf(properties.getProperty("test.jetty.http.port")));
    	
    	SslContextFactory sslContextFactory = new SslContextFactory(properties.getProperty("test.jetty.https.keystore"));
    	sslContextFactory.setKeyStorePassword(properties.getProperty("test.jetty.https.password"));
    	
		SslSelectChannelConnector httpsConnector  = new SslSelectChannelConnector(sslContextFactory);            
    	httpsConnector.setPort(Integer.valueOf(properties.getProperty("test.jetty.https.port")));
                    
        ContextHandlerCollection contextCollection = new ContextHandlerCollection();
        
        // Create test servlet context
        createContext(contextCollection, "/test",  "src/test/webapp", properties);
        
        // Create main context
		Context serverContext = createContext(contextCollection, "/",  "src/main/webapp", properties);
    	
		server = new Server();
		server.addConnector(httpConnector);
		server.addConnector(httpsConnector);
    	server.setHandler(contextCollection);
    	server.start();
    	
    	SERVER_CONTEXT = WebApplicationContextUtils.getWebApplicationContext(serverContext);
	}
	
	private static Context createContext(ContextHandlerCollection contextCollection, String contextPath, String webapp, Properties properties) throws Exception {
		Resource resource = Resource.newResource(webapp);
		if(resource != null && resource.exists()){
			HandlerCollection handlerCollection = new HandlerCollection();
			
	        WebAppContext appContext = new WebAppContext();
	        appContext.setContextPath(contextPath);
	        appContext.setBaseResource(resource);
	        
	        if(properties.getProperty("test.jetty.user") != null) {
	        	ConstraintSecurityHandler handler = new ConstraintSecurityHandler();

	        	HashLoginService l = new HashLoginService();
	            l.putUser(properties.getProperty("test.jetty.user"), Credential.getCredential(properties.getProperty("test.jetty.password")), new String[] {"user"});
	            l.setName("Test Jetty Server");
	            
	            Constraint constraint = new Constraint();
	            constraint.setName(Constraint.__BASIC_AUTH);
	            constraint.setRoles(new String[]{"user"});
	            constraint.setAuthenticate(true);
	             
	            ConstraintMapping cm = new ConstraintMapping();
	            cm.setConstraint(constraint);
	            cm.setPathSpec("/*");
	            
	            handler.setAuthenticator(new BasicAuthenticator());
	            handler.setRealmName("myrealm");
	            handler.addConstraintMapping(cm);
	            handler.setLoginService(l);
	            
				appContext.setSecurityHandler(handler);
	        }
	        
	        handlerCollection.addHandler(appContext);
	        contextCollection.addHandler(handlerCollection);

	        return appContext.getServletContext();
		}
		return null;
	}
	
	/**
	 * Spring context initialization
	 * 
	 * @throws Exception 
	 */
	@BeforeSuite(alwaysRun=true)
	public void createContext() throws Exception {
		startJettyContext();
		startTestingContext();
	}

	private void startTestingContext() {
		TESTING_CONTEXT = new GenericApplicationContext();
		XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(TESTING_CONTEXT);
		xmlReader.loadBeanDefinitions(new ClassPathResource("test-config.xml"));
		
		TESTING_CONTEXT.refresh();
		TESTING_CONTEXT.start();
	}
	
	/**
	 * Context close after tests are done
	 * @throws Exception 
	 */
	@AfterSuite(alwaysRun = true)
	public void stopContainer() throws Exception {
		server.stop();
		TESTING_CONTEXT.close();
	}
}
