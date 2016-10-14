package ee.ajapaik.axis.service;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.EntityEnclosingRequestWrapper;

import ee.ajapaik.xml.model.Task;
import ee.ra.ais.TaskServiceStub;
import ee.ra.ais.TaskServiceStub.TaskObjectList_type0;
import ee.ra.ais.TaskServiceStub.TaskObject_type0;

public class TaskServiceClient extends AbstractSOAPClient<TaskServiceStub> {

	private ThreadLocal<HttpPost> posts = new ThreadLocal<HttpPost>();
	
	@Override
	protected void beforeRequest(HttpRequest request) {
		posts.set((HttpPost) ((EntityEnclosingRequestWrapper) request).getOriginal());
	}
	
	@Override
	protected TaskServiceStub getService(ConfigurationContext context, String endpoint) throws AxisFault {
		return new TaskServiceStub(context, endpoint);
	}
	
	public List<Task> getTaskList(Long taskId) throws RemoteException {
		List<Task> result = new ArrayList<Task>();
		
		TaskObjectList_type0 list = service.taskObjectList(BigInteger.valueOf(taskId));
		for (TaskObject_type0 to : list.getTaskObject()) {
			Task task = new Task();
			task.setObjectId(to.getObjectId().longValue());
			task.setTaskId(to.getTaskId().longValue());
			task.setTaskObjectType(to.getXTaskObjectTypeId());
			task.setObjectPuris(Arrays.asList(to.getObjectPuris().getObjectPuri()));
			
			result.add(task);
		}
		
		if(posts.get() != null) {
			posts.get().reset();
			posts.remove();
		}
		
		return result;
	}
	
	@Override
	protected void beforeResponse(HttpResponse response) {
		response.removeHeaders("Content-Type");
	}
}