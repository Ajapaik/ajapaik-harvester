package ee.ajapaik.platform;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyValue;

public class BeanWrapperUtil {
	
	protected static final Logger logger = Logger.getLogger(BeanWrapperUtil.class);

	public static void overrideProperties(Object bean, Map<String, String> parameters) {
		for (Entry<String, String> entry : parameters.entrySet()) {
			BeanWrapperImpl wrapper = new BeanWrapperImpl(bean);

			logger.debug("Overriding property '" + entry.getKey() + "' with value '" + entry.getValue() + "' for bean instance '" + bean.getClass().getSimpleName() + "'");
			
			PropertyValue pv = new PropertyValue(entry.getKey(), entry.getValue());
			pv.setOptional(true);
			wrapper.setPropertyValue(pv);
		}
	}
}
