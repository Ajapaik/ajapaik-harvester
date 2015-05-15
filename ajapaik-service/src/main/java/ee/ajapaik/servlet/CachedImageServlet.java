package ee.ajapaik.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ee.ajapaik.db.Repository;

/**
 * 
 * @author <a href="mailto:kaido@quest.ee?subject=CachedImageServlet">Kaido Kalda</a>
 */
public class CachedImageServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private byte[] noThumbnail;
	
	@Override
	public void init() throws ServletException {
		super.init();
		try {
			byte[] buffer = new byte[2024];

			int length = IOUtils.read(new ClassPathResource("nt.jpg").getInputStream(), buffer);
			
			this.noThumbnail = new byte[length];
			System.arraycopy(buffer, 0, noThumbnail, 0, length);
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] requestPath = request.getRequestURI().split("/");
		String requestFile = requestPath[requestPath.length - 1];

		WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		Repository repository = ctx.getBean("repository", Repository.class);
		
		byte[] data = repository.queryImage(requestFile);
		
		response.addHeader("Cache-Control", "max-age=31556926");
		response.getOutputStream().write(data == null || data.length == 0 ? noThumbnail : data);
		response.getOutputStream().close();
	}
}
