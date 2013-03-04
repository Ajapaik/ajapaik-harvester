package ee.ajapaik.servlet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ee.ajapaik.image.FileCache;

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
		FileCache fc = ctx.getBean(FileCache.class);
		
		InputStream is = fc.getImage(requestFile);
		if (is != null) {
			try {
				// Read the original image from the Server Location
				BufferedImage bufferedImage = ImageIO.read(is);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				String imageOutput = getParam(request, "type", "jpg");
				
				// Write the image
				ImageIO.write(bufferedImage, imageOutput, baos);
				
				// Set content type
				response.setContentType("image/" + imageOutput);
				
				// Empty buffer
				baos.writeTo(response.getOutputStream());
				
				return;
			} catch (Exception e) {
				logger.error("Failed to send image data:", e);
			}
		}
	}
	
	// Check the param if it's not present return the default
	private String getParam(HttpServletRequest request, String param, String def) {
		String parameter = request.getParameter(param);
		if (parameter == null || "".equals(parameter)) {
			return def;
		} else {
			return parameter;
		}
	}
}
