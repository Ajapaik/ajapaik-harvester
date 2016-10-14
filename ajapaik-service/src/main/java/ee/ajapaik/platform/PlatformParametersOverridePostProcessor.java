package ee.ajapaik.platform;

import java.net.URL;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class PlatformParametersOverridePostProcessor implements BeanPostProcessor {

	private URL url;
	
	public void setUrl(URL url) {
		this.url = url;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if(bean instanceof BaseHttpClient && url != null) {
			BaseHttpClient baseClient = (BaseHttpClient) bean;

			String protocol = url.getProtocol();
			
			baseClient.setProtocol(protocol);
			baseClient.setHost(url.getHost());
			baseClient.setPort(
					url.getPort() != -1 ? 
					url.getPort() : 
					(protocol.equals("http") ? 80 : 443));
		}
		
		return bean;
	}
}
