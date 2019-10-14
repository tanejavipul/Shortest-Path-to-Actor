package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class AddRelationship implements HttpHandler {
	
	public AddRelationship() {}
	
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
			
			String actorId = "",movieId = "";
			
			if (deserialized.has("actorId"))
				actorId = deserialized.getString("actorId");
			if (deserialized.has("movieId"))
				movieId = deserialized.getString("movieId");
			
			if (actorId.isEmpty() || movieId.isEmpty()) {
				r.sendResponseHeaders(400, 0);
		        OutputStream os = r.getResponseBody();
		        os.close();
			}
			else {
				try {
					DB connection = new DB();
					connection.addRelationship(actorId, movieId);
					r.sendResponseHeaders(200, 0);
					OutputStream os = r.getResponseBody();
					os.close();
					
				}
				catch (Exception e)
				{
					r.sendResponseHeaders(400, 0);
			        OutputStream os = r.getResponseBody();
			        os.close();
				}
			}
		} catch (Exception e) {
			r.sendResponseHeaders(500, 0);
	        OutputStream os = r.getResponseBody();
	        os.close();
		}
    }

}
