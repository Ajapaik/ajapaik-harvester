package ee.ajapaik.servlet;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ee.ajapaik.model.search.RecordView;
import ee.ajapaik.platform.BaseHttpClient;
import ee.ajapaik.platform.PlatformFactory;
import ee.ajapaik.service.AjapaikService;

/**
 * 
 * @author Kaido
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
				StringBuilder result = new StringBuilder("institution;number;autor;title;description;date;place;url;image;\n");
				
				WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
				AjapaikService service = ctx.getBean("ajapaikService", AjapaikService.class);
	
				String name = FORMAT.format(new Date());
				
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
						addField(result, grabImage(zos, name, recordView.getImageUrl()));
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

	private String grabImage(ZipOutputStream zos, String name, String query) throws Exception {
		URL url = new URL(query);
		BaseHttpClient client = PlatformFactory.getInstance().getClient(url);
		
		logger.debug("Getting data from url: " + url);
		
		HttpGet get = new HttpGet(url.getFile());
		get.addHeader(new BasicHeader("Accept-Encoding", "gzip,deflate"));
		
		HttpResponse result = client.getHttpClient().execute(get);
		HttpEntity entity = result.getEntity();
		
		if(entity != null) {
			if (result.getStatusLine().getStatusCode() != 404) {
				String[] split = query.split("/");
				
				String fileName = split[split.length - 1] + ".jpg";
				ZipEntry ze = new ZipEntry(fileName);
	    		zos.putNextEntry(ze);
	    		
				try {
					IOUtils.copy(entity.getContent(), zos);
				} finally {
					zos.closeEntry();
				}
				
				logger.debug("Got data: " + fileName);
				
				return fileName;
			}
		}
		
		return null;
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
