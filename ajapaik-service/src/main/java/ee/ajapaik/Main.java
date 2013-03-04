package ee.ajapaik;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class Main {

	public static void main(String[] args) {
		GenericApplicationContext ctx = new GenericApplicationContext();
		XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
		xmlReader.loadBeanDefinitions(new ClassPathResource("server-config.xml"));
		
		ctx.refresh(); 								// context init
		ctx.start(); 								// lifecycle start
		ctx.registerShutdownHook();					// close context on JVM exit
	}

}
