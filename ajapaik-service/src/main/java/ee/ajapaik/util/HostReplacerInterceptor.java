package ee.ajapaik.util;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

public class HostReplacerInterceptor implements HttpRequestInterceptor {

	@Override
	public void process(HttpRequest req, HttpContext arg1) throws HttpException, IOException {
		String host = req.getHeaders("Host")[0].getValue();
		if(host.contains(":")) {
			req.setHeader("Host", host.split(":")[0]);
		}
	}

}
