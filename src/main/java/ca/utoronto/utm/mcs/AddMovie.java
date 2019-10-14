package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AddMovie implements HttpHandler{

	public AddMovie() {}
	
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
			
			String movieName = "",movieId = "";
			
			if (deserialized.has("name"))
				movieName = deserialized.getString("name");
			if (deserialized.has("movieId"))
				movieId = deserialized.getString("movieId");
			
			if (movieName.isEmpty() || movieId.isEmpty()) {
				r.sendResponseHeaders(400, 0);
		        OutputStream os = r.getResponseBody();
		        os.close();
			}
			else {
				try {
					Movie m = new Movie();
					m.setMovieId(movieId);
					m.setMovieName(movieName);
					
					DB connection = new DB();
					connection.addMovie(m);
					
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
