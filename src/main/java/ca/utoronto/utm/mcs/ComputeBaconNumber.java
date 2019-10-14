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


public class ComputeBaconNumber implements HttpHandler {
	
public ComputeBaconNumber() {}
	
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

			String actorId = "";
			DB connection = new DB();
			
			if (deserialized.has("actorId"))
				actorId = deserialized.getString("actorId");
			//System.out.println(connection.getActor(actorId).getActorName());

			if (actorId.isEmpty()) {
				r.sendResponseHeaders(400, 0);
		        OutputStream os = r.getResponseBody();
		        os.close();
			}
			else if(actorId.equals(App.getBaconID()) ) {
				JSONObject object = new JSONObject();
				object.put("baconNumber", "0");
				String response = object.toString();
			    r.sendResponseHeaders(200, response.length());
				OutputStream os = r.getResponseBody();
				os.write(response.getBytes());
				os.close();
			}
			else if(connection.getActor(actorId).getActorName().isEmpty())
			{
				r.sendResponseHeaders(400, 0);
				OutputStream os = r.getResponseBody();
				os.close();
			}
			else {
				try {

					Long result = new Long(connection.computeBaconNumber(actorId));

					if(result == -1) {
						r.sendResponseHeaders(404, 0);
				        OutputStream os = r.getResponseBody();
				        os.close();
					}
					else { //otherwise you will get error
						String response = "";
						JSONObject object = new JSONObject();

						object.put("baconNumber", result.toString());

						response = object.toString();

						r.sendResponseHeaders(200, response.length());
						OutputStream os = r.getResponseBody();
						os.write(response.getBytes());
						os.close();
					}
					
				} catch (Exception e) {
					r.sendResponseHeaders(500, 0);
			        OutputStream os = r.getResponseBody();
			        os.close();
				}
			}
			
		} catch (Exception e) {
			r.sendResponseHeaders(400, 0);
	        OutputStream os = r.getResponseBody();
	        os.close();
		}
    }

}
