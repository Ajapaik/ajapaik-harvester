package ee.ajapaik.servlet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ee.ajapaik.db.Repository;
import ee.ajapaik.model.search.RecordView;
import ee.ajapaik.service.AjapaikService;
import ee.ajapaik.util.IOHandler;

/**
 * 
 * @author <a href="mailto:kaido@quest.ee?subject=CSVServlet">Kaido Kalda</a>
 */
public class CSVServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(CachedImageServlet.class);
	
	private static final String SEPARATOR = ";";
	private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd_HHmmss", new Locale("et_EE"));
	
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String ids = request.getParameter("ids");
			
			logger.debug("Building ZIP for ids: " + ids);
			
			if(ids != null && ids.length() > 0) {
				final StringBuilder result = new StringBuilder("institution;number;autor;title;description;date;place;url;image;width;height;\n");
				
				WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
				AjapaikService service = ctx.getBean("ajapaikService", AjapaikService.class);
	
				String name = FORMAT.format(new Date());
				
				// Remove duplicates
				Set<String> set = new HashSet<String>(Arrays.asList(ids.split(",")));
				RecordView[] rw = service.getRecords(set.toArray(new String[set.size()]));
				
				response.addHeader("Content-Disposition", "attachment;filename=" + name + ".zip");
				
				ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
				
				for (RecordView recordView : rw) {
					
					logger.debug("Adding entry: " + recordView.getId());
					
					String institution = recordView.getInstitution();
					if(institution.contains(",")) {
						addField(result, institution.split(",")[0]);
					} else {
						addField(result, institution);
					}
					
					addField(result, recordView.getIdentifyingNumber());
					addField(result, recordView.getCreators());
					addField(result, recordView.getTitle());
					addField(result, recordView.getDescription());
					addField(result, recordView.getDate() != null ? recordView.getDate() : ""); // date
					addField(result, ""); // place
					addField(result, recordView.getUrlToRecord());
					try {
						if(recordView.getImageUrl() != null && !recordView.getImageUrl().equals("null")) {
							grabImage(zos, name, recordView.getImageUrl(), new Callback() {
								
								@Override
								public void notify(String name, Integer width, Integer height) {
									addField(result, name);
									addField(result, width.toString());
									addField(result, height.toString());
								}
							});
						} else if(recordView.getCachedThumbnailUrl() != null) {
							Repository repository = ctx.getBean("repository", Repository.class);
							
							byte[] data = repository.queryImage(recordView.getCachedThumbnailUrl());
							if (data != null) {
								BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(data));
								
								int width = bimg.getWidth();
								int height = bimg.getHeight();
								
								try {
									ZipEntry ze = new ZipEntry(recordView.getCachedThumbnailUrl() + ".jpg");
						    		zos.putNextEntry(ze);
						    		
									ImageIO.write( bimg, "jpg", zos );
									
									zos.closeEntry();
								} catch (ZipException e) {
									logger.warn("Error adding entry", e);
								}
								
								addField(result, recordView.getCachedThumbnailUrl() + ".jpg");
								addField(result, String.valueOf(width));
								addField(result, String.valueOf(height));
							}
						}
					} catch (Exception e) {
						logger.error("Error getting data", e);
					}
					
					result.append("\n");
					
					logger.debug("Entry added");
				}
				
	    		zos.putNextEntry(new ZipEntry(name + ".csv"));
	    		zos.write(result.toString().getBytes("UTF-8"));
	    		
	    		zos.closeEntry();
	    		
				zos.close();
				
				logger.debug("ZIP done: " + name);
				
	
				
	//			response.setContentType("text/csv; charset=UTF-8");
	//			response.setCharacterEncoding("UTF-8");
	//			response.getWriter().write(result.toString());
			}
		} catch (Exception e) {
			logger.error("Error building ZIP", e);
		}
	}

	private void grabImage(ZipOutputStream zos, String name, String query, Callback c) throws Exception {
		
		// XXX: muis hack
		if(query.contains("portaal/")) {
			query = query.replace("portaal/", "");
		}
		
		URL url = new URL(query);
		InputStream is = IOHandler.openStream(url);
		if (is != null) {
			String fileName = getFileName(query);
			
			ZipEntry ze = new ZipEntry(fileName);
    		zos.putNextEntry(ze);
    		
			int width = 0;
			int height = 0;
			
			try {
				BufferedImage bimg = ImageIO.read(is);
				
				width = bimg.getWidth();
				height = bimg.getHeight();
				
				ImageIO.write( bimg, "jpg", zos );
			} finally {
				zos.closeEntry();
				is.close();
			}
			
			logger.debug("Got data: " + fileName);
			
			c.notify(fileName, width, height);
			
			return;
		}
		
		c.notify("", 0, 0);
	}

	private String getFileName(String query) {
		String[] split = query.split("/");
		String lastPart = split[split.length - 1];
		
		String fileName;
		if(lastPart.contains("=")) {
			split = lastPart.split("=");
			fileName = split[split.length - 1];
		} else {
			fileName = lastPart;
		}
		
		if(!fileName.contains(".jpg")) {
			fileName += ".jpg";
		}
		
		return fileName;
	}

	private void addField(StringBuilder result, String data) {
		result.append("\"").append(data
			.replaceAll(SEPARATOR, ":")
				.replaceAll("\"","\"\"")
					.replaceAll("\n", "")
						.replaceAll("\r", ""))
							.append("\"")
								.append(SEPARATOR);
	}
}
