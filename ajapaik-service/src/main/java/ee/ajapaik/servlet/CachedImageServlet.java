package ee.ajapaik.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ee.ajapaik.db.Repository;

/**
 * 
 * @author Kaido
 */
public class CachedImageServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CachedImageServlet.class);

	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] requestPath = request.getRequestURI().split("/");
		String requestFile = requestPath[requestPath.length - 1];

		WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		Repository repository = ctx.getBean("repository", Repository.class);
		
		byte[] data = repository.queryImage(requestFile);
		if (data != null) {
			try {
				response.getOutputStream().write(data);
			} catch (IOException e) {
				logger.error("Failed to send image data:", e);
			}
		}
	}
}
