package ee.ajapaik.platform;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class PlatformFactory implements BeanFactoryAware, InitializingBean{

	private Map<String, BaseHttpClient> clients = new HashMap<String, BaseHttpClient>();
	private PlatformParametersOverridePostProcessor processor = new PlatformParametersOverridePostProcessor();
	
	private ConfigurableListableBeanFactory ctx;
	
	private static PlatformFactory instance;
	
	private PlatformFactory() {}
	
	public static PlatformFactory getInstance() {
		if(instance == null) {
			instance = new PlatformFactory();
		}
		return instance;
	}
	
	@Override
	public void setBeanFactory(BeanFactory ctx) throws BeansException {
		this.ctx = (ConfigurableListableBeanFactory)ctx;
	}

	public BaseHttpClient getClient(URL url) {
		String host = url.getHost();
		if(!clients.containsKey(host)) {
			processor.setUrl(url);
			clients.put(host, ctx.getBean("baseHttpClient", BaseHttpClient.class));
		}
		return clients.get(host);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		ctx.addBeanPostProcessor(processor);
	}
}
