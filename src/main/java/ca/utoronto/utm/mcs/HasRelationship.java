package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.http.HTTPBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HasRelationship implements HttpHandler{
	
	public HasRelationship() {}
	
	public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public void handleGet(HttpExchange r) throws IOException, JSONException {
    	try {
			String body = Utils.convert(r.getRequestBody());
			JSONObject deserialized = new JSONObject(body);
			
			String movieId = "",actorId = "";
			
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
					//System.out.println("ddd");
					Boolean result = new Boolean(connection.hasRelationship(actorId, movieId));
					//System.out.println("ddd");


					String response = "";
					JSONObject object = new JSONObject();
			
					object.put("actorId", actorId);
					object.put("movieId", movieId);
					object.put("hasRelationship", result);
					
					response = object.toString();

				    r.sendResponseHeaders(200, response.length());
					OutputStream os = r.getResponseBody();
					os.write(response.getBytes());
					os.close();
					
				} catch (Exception e) {
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
