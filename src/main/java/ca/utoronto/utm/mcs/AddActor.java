package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class AddActor implements HttpHandler {
	public AddActor() {}
	
	public void handle(HttpExchange r) {
	        try {
	            if (r.getRequestMethod().equals("PUT")) {
	                handlePut(r);
	            } 
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	 
	 public void handlePut(HttpExchange r) throws IOException, JSONException {
    	try {
			String body = Utils.convert(r.getRequestBody());
			JSONObject deserialized = new JSONObject(body);
			
			String actorName = "",actorId = "";
			
			if (deserialized.has("name"))
				actorName = deserialized.getString("name");
			if (deserialized.has("actorId"))
				actorId = deserialized.getString("actorId");

			if (actorName.isEmpty() || actorId.isEmpty()) {
				r.sendResponseHeaders(400, 0);
		        OutputStream os = r.getResponseBody();
		        os.close();
			}
			else {
				try {
					Actor a = new Actor();
					a.setActorId(actorId);
					a.setActorName(actorName);
					
					DB connection = new DB();
					connection.addActor(a);
					
				} catch (Exception e) {
					r.sendResponseHeaders(500, 0);
			        OutputStream os = r.getResponseBody();
			        os.close();
			        throw e;
				}
				
				r.sendResponseHeaders(200, 0);
				OutputStream os = r.getResponseBody();
				os.close();
			}
						
		} catch (Exception e) {
			r.sendResponseHeaders(400, 0);
	        OutputStream os = r.getResponseBody();
	        os.close();
		}

    }

}
