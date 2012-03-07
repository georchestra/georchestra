package extractorapp.ws.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.NestedServletException;

import extractorapp.ws.extractor.task.ExecutionMetadata;
import extractorapp.ws.extractor.task.ExecutionPriority;

/**
 * 
 * Test case for the ExtractorController class
 * 
 * @author Mauricio Pazos
 *
 */
public class ExtractorControllerTest extends AbstractControllerTestSupport{

	
	/**
	 * Test example: uri=/extractor/tasks/{uuid} method=PUT
	 * @throws Exception
	 */
	@Test
	public void testUpdateTaskUsingUuidInURL() throws Exception{
		
		String uuid = "7a5a2af2-f93e-49c4-b20a-8f39afe04707";
		MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/extractor/tasks/" + uuid );
		String content = "{\"uuid\":\"eff597a8-2f34-42ca-94c6-182752dc46a9\",\"requestor\":\"Fran\u00e7ois Van Der Biest (Camptocamp)\",\"priority\":1,\"status\":\"CANCELLED\",\"spec\":{\"emails\":[\"francois.vanderbiest@camptocamp.com\"],\"globalProperties\":{\"projection\":\"EPSG:2154\",\"resolution\":0.5,\"rasterFormat\":\"geotiff\",\"vectorFormat\":\"shp\",\"bbox\":{\"srs\":\"EPSG:2154\",\"value\":[300000,6175000,600000,6530000]}},\"layers\":[{\"projection\":\"EPSG:27572\",\"resolution\":null,\"format\":\"tab\",\"bbox\":{\"srs\":\"EPSG:2154\",\"value\":[373991.51116992,6413369.7356194,374999.51062561,6414377.7350751]},\"owsUrl\":\"http://ids.pigma.org/geoserver/wfs/WfsDispatcher?\",\"owsType\":\"WFS\",\"layerName\":\"pigma:PARCAD3323\"}]},\"request_ts\":\"2012-02-20 12:32:06\",\"begin_ts\":\"2012-02-20 12:32:07\",\"end_ts\":\"\",\"id\":\"\"}";
		request.setContent(content.getBytes());
		request.setContentType("application/json");
		request.setCharacterEncoding("UTF-8");
		
		MockHttpServletResponse response = new MockHttpServletResponse();

		getServletInstance().service(request, response);
		
		//TODO asserts
	}
	
	/**
	 * Update Status from from waiting to canceled
	 * @throws Exception
	 */
	@Test
	public void testUpdateTask() throws Exception{
		
		MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/extractor/tasks");
		String content = "{\"uuid\":\"eff597a8-2f34-42ca-94c6-182752dc46a9\",\"requestor\":\"Fran\u00e7ois Van Der Biest (Camptocamp)\",\"priority\":1,\"status\":\"WAITING\",\"spec\":{\"emails\":[\"francois.vanderbiest@camptocamp.com\"],\"globalProperties\":{\"projection\":\"EPSG:2154\",\"resolution\":0.5,\"rasterFormat\":\"geotiff\",\"vectorFormat\":\"shp\",\"bbox\":{\"srs\":\"EPSG:2154\",\"value\":[300000,6175000,600000,6530000]}},\"layers\":[{\"projection\":\"EPSG:27572\",\"resolution\":null,\"format\":\"tab\",\"bbox\":{\"srs\":\"EPSG:2154\",\"value\":[373991.51116992,6413369.7356194,374999.51062561,6414377.7350751]},\"owsUrl\":\"http://ids.pigma.org/geoserver/wfs/WfsDispatcher?\",\"owsType\":\"WFS\",\"layerName\":\"pigma:PARCAD3323\"}]},\"request_ts\":\"2012-02-20 12:32:06\",\"begin_ts\":\"2012-02-20 12:32:07\",\"end_ts\":\"\",\"id\":\"\"}";
		request.setContent(content.getBytes());
		request.setContentType("application/json");
		request.setCharacterEncoding("UTF-8");
		
		MockHttpServletResponse response = new MockHttpServletResponse();

		getServletInstance().service(request, response);
		
		//TODO asserts the task should be status = CANCELED
	}

	/**
	 * Priority value must be between 0-LOW and 2-HIGH
	 * @throws Exception
	 * @throws IOException
	 */
	@Test
	public void testUpdateTask_wrongPriority() throws Exception{
		
		MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/extractor/tasks");
		request.addParameter("JSON", "{\"uuid\":\"eff597a8-2f34-42ca-94c6-182752dc46a9\",\"requestor\":\"Fran\u00e7ois Van Der Biest (Camptocamp)\",\"priority\":4,\"status\":\"CANCELLED\",\"spec\":{\"emails\":[\"francois.vanderbiest@camptocamp.com\"],\"globalProperties\":{\"projection\":\"EPSG:2154\",\"resolution\":0.5,\"rasterFormat\":\"geotiff\",\"vectorFormat\":\"shp\",\"bbox\":{\"srs\":\"EPSG:2154\",\"value\":[300000,6175000,600000,6530000]}},\"layers\":[{\"projection\":\"EPSG:27572\",\"resolution\":null,\"format\":\"tab\",\"bbox\":{\"srs\":\"EPSG:2154\",\"value\":[373991.51116992,6413369.7356194,374999.51062561,6414377.7350751]},\"owsUrl\":\"http://ids.pigma.org/geoserver/wfs/WfsDispatcher?\",\"owsType\":\"WFS\",\"layerName\":\"pigma:PARCAD3323\"}]},\"request_ts\":\"2012-02-20 12:32:06\",\"begin_ts\":\"2012-02-20 12:32:07\",\"end_ts\":\"\",\"id\":\"\"}");
		
	
		MockHttpServletResponse response = new MockHttpServletResponse();

		getServletInstance().service(request, response);
		
		//TODO asserts the task is not changed
	}
	
	/**
	 * Test getTaskQueue operation
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetTaskQueue() throws Exception{

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/extractor/tasks");
		
		MockHttpServletResponse response = new MockHttpServletResponse();

		getServletInstance().service(request, response);

		String dataResponse = response.getContentAsString();
		
		//Response analysis
		JSONObject jsonResult = new JSONObject(dataResponse);
		
		JSONArray jsonQueue = jsonResult.getJSONArray("tasks");
		
		assertEquals(jsonQueue.length(), 0); // right now the queue is empty
	}

	/**
	 * Test the getTaskQueue response
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExtractorGetTaskQueueResponse() throws Exception{
		
		List<ExecutionMetadata> taskQueue = new LinkedList<ExecutionMetadata>();

		ExecutionMetadata e1 = new ExecutionMetadata(UUID.randomUUID(), "userTest", new Date(), "{}");
		e1.setPriority(ExecutionPriority.HIGH);
		e1.setRunning();
		taskQueue.add(e1);
		
		ExecutionMetadata e2 = new ExecutionMetadata(UUID.randomUUID(), "userTest", new Date(), "{}");
		e2.setRunning();
		e2.setCompleted();
		e2.setPriority(ExecutionPriority.HIGH);
	
		taskQueue.add(e2);

		ExtractorGetTaskQueueResponse request = ExtractorGetTaskQueueResponse.newInstance(taskQueue);
		String dataResponse = request.asJsonString();
		
		//Response analysis
		JSONObject jsonResult = new JSONObject(dataResponse);
		
		JSONArray jsonQueue = jsonResult.getJSONArray("tasks");
		
		assertEquals(jsonQueue.length(), 2);
		
		for (int j = 0; j < jsonQueue.length(); j++) {
			
			JSONObject jsonTask = (JSONObject) jsonQueue.get(j);
			
			assertNotNull(jsonTask.get("uuid"));
			assertNotNull(jsonTask.get("requestor"));
			assertNotNull(jsonTask.get("priority"));
			assertNotNull(jsonTask.get("status"));
			assertNotNull(jsonTask.get("spec"));
			assertNotNull(jsonTask.get("request_ts"));
			assertNotNull(jsonTask.get("begin_ts"));
			assertNotNull(jsonTask.get("end_ts"));
		}
	}
	/**
	 * Test the deploy done in dev.pigma.org
	 * 
	 * This test was written to hack dev.pigma.org deployment
	 * @throws Exception
	 */
	@Ignore 
	public void testInvokingDevPigmaOrg_Put() throws Exception{
		
		//change priority
		PutMethod putMethod = new PutMethod("http://dev.pigma.org/extractorapp-private/ws/extractor/tasks");
		String content = "{\"uuid\":\"eff597a8-2f34-42ca-94c6-182752dc46a9\",\"requestor\":\"Fran\u00e7ois Van Der Biest (Camptocamp)\",\"priority\":1,\"status\":\"CANCELLED\",\"spec\":{\"emails\":[\"francois.vanderbiest@camptocamp.com\"],\"globalProperties\":{\"projection\":\"EPSG:2154\",\"resolution\":0.5,\"rasterFormat\":\"geotiff\",\"vectorFormat\":\"shp\",\"bbox\":{\"srs\":\"EPSG:2154\",\"value\":[300000,6175000,600000,6530000]}},\"layers\":[{\"projection\":\"EPSG:27572\",\"resolution\":null,\"format\":\"tab\",\"bbox\":{\"srs\":\"EPSG:2154\",\"value\":[373991.51116992,6413369.7356194,374999.51062561,6414377.7350751]},\"owsUrl\":\"http://ids.pigma.org/geoserver/wfs/WfsDispatcher?\",\"owsType\":\"WFS\",\"layerName\":\"pigma:PARCAD3323\"}]},\"request_ts\":\"2012-02-20 12:32:06\",\"begin_ts\":\"2012-02-20 12:32:07\",\"end_ts\":\"\",\"id\":\"\"}";
		RequestEntity entity = new StringRequestEntity(content, "application/json", "UTF-8");
		putMethod.setRequestEntity(entity);
		
		HttpClient httpClient = new HttpClient();
		httpClient.executeMethod(putMethod);
	}

}
