package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GetMovie implements HttpHandler {
	
	public GetMovie() {}
	
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
			
			String movieId = "";
			
			if (deserialized.has("movieId"))
				movieId = deserialized.getString("movieId");
			
			if (movieId.isEmpty()) {
				r.sendResponseHeaders(400, 0);
		        OutputStream os = r.getResponseBody();
		        os.close();
			}
			else {
				try {
					DB connection = new DB();
					Movie m = connection.getMovie(movieId);

					String response = "";
					
					if(!m.getMovieName().isEmpty()) {
						JSONObject object = new JSONObject();
						List<String> actorsIds = new ArrayList<String>();

						object.put("name", m.getMovieName());
						object.put("movieId", m.getMovieId());

						if(m.getActors() != null)
						{
							for (Actor a : m.getActors()) {
								actorsIds.add(a.getActorId());
							}
						}

						object.put("actors", actorsIds);		
						
						response = object.toString();
						r.sendResponseHeaders(200, response.length());
						OutputStream os = r.getResponseBody();
						os.write(response.getBytes());
						os.close();
					}
					else
					{
						r.sendResponseHeaders(400, 0);
						OutputStream os = r.getResponseBody();
						os.close();
					}



					
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
