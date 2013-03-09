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

import ee.ajapaik.model.search.RecordView;
import ee.ajapaik.service.AjapaikService;

/**
 * 
 * @author Kaido
 */
public class CSVServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CSVServlet.class);

	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
	}

	/**
	 *	1) muuseum, nt Eesti Arhitektuurimuuseum
		2) kogunumber koos muuseumi tähisega, EAM Fk <nnnn> (võivad ka 2 eraldi välja/veergu olla)
		3) foto autor
		4) kirjeldus
		5) dateering (kui on)
		6) koht (kui on)
		7) muisi link, nt http://muis.ee/museaalview/1527962
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ids = request.getParameter("ids");
		
		if(ids != null && ids.length() > 0) {
			StringBuilder result = new StringBuilder();
			
			WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			AjapaikService service = ctx.getBean("ajapaikService", AjapaikService.class);
			
			RecordView[] rw = service.getRecords(ids.split(","));
			for (RecordView recordView : rw) {
				result.append(recordView.getInstitution()).append(",");
				result.append(recordView.getIdentifyingNumber()).append(",");
				result.append(recordView.getCreators()).append(",");
				result.append(recordView.getDescription()).append(",");
				result.append("").append(",");
				result.append("").append(",");
				result.append(recordView.getUrlToRecord()).append("\n");
			}
			
			response.addHeader("Content-Disposition", "attachment;filename=" + System.currentTimeMillis() + ".csv");
			
			response.setContentType("text/csv; charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(result.toString());
		}
	}
}
