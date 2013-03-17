package ee.ajapaik.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
	
	private static final String SEPARATOR = ";";
	private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
	private static final String PATH = "export";
	
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ids = request.getParameter("ids");
		
		if(ids != null && ids.length() > 0) {
			StringBuilder result = new StringBuilder();
			
			WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			AjapaikService service = ctx.getBean("ajapaikService", AjapaikService.class);

			String name = FORMAT.format(new Date());
			
			RecordView[] rw = service.getRecords(ids.split(","));
			for (RecordView recordView : rw) {
				
				String institution = recordView.getInstitution();
				if(institution.contains(",")) {
					addField(result, institution.split(",")[0]);
				} else {
					addField(result, institution);
				}
				
				addField(result, recordView.getIdentifyingNumber());
				addField(result, recordView.getCreators());
				addField(result, recordView.getDescription());
				addField(result, recordView.getDate() != null ? recordView.getDate() : ""); // date
				addField(result, ""); // place
				addField(result, recordView.getUrlToRecord());
				addField(result, grabImage(name, recordView.getImageUrl()));
				
				result.append("\n");
			}
			
			response.addHeader("Content-Disposition", "attachment;filename=" + name + ".csv");
			
			response.setContentType("text/csv; charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(result.toString());
		}
	}

	private String grabImage(String name, String query) throws IOException {
		File dir = new File(PATH + "/" + name);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		
		try {
			URL url = new URL(query);
			BaseHttpClient client = PlatformFactory.getInstance().getClient(url);
			
			HttpGet get = new HttpGet(url.getFile());
			get.addHeader(new BasicHeader("Accept-Encoding", "gzip,deflate"));
			
			HttpResponse result = client.getHttpClient().execute(get);
			HttpEntity entity = result.getEntity();
			
			if(entity != null) {
				if (result.getStatusLine().getStatusCode() != 404) {
					String[] split = query.split("/");
					FileOutputStream fos = new FileOutputStream(PATH + "/" + name + "/" + split[split.length - 1]);
					try {
						IOUtils.copy(entity.getContent(), fos);
					} finally {
						fos.close();
						entity.getContent().close();
					}
					
					return split[split.length - 1];
				}
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		}
		
		return null;
	}

	private void addField(StringBuilder result, String data) {
		result.append(data.replaceAll(SEPARATOR, ":").replaceAll("\n", "")).append(SEPARATOR);
	}
}
