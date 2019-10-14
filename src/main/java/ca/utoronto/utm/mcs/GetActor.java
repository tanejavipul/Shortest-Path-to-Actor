package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.StatementResult;
import org.slf4j.*;
import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javax.sound.midi.SysexMessage;

public class GetActor implements HttpHandler {
    public GetActor() {

    }

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
			
			if (deserialized.has("actorId"))
				actorId = deserialized.getString("actorId");

			if (actorId.isEmpty()) {
				r.sendResponseHeaders(400, 0);
		        OutputStream os = r.getResponseBody();
		        os.close();
			}
			else {
				try {
					DB connection = new DB();
					Actor a = connection.getActor(actorId) ;

					String response = "";

					if(!a.getActorName().isEmpty()) {
						JSONObject object = new JSONObject();
						List<String> moviesIds = new ArrayList<String>();

						object.put("name", a.getActorName());
						object.put("actorId", a.getActorId());

						if(a.getMovies()!=null)
						{
							for (Movie m : a.getMovies()) {
								moviesIds.add(m.getMovieId());
							}

						}
						object.put("movies", moviesIds);
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

					
				}
				catch (Exception e) {
					r.sendResponseHeaders(404, 0);
			        OutputStream os = r.getResponseBody();
			        os.close();
				}
			}
			
		}
    	catch (Exception e) {
			r.sendResponseHeaders(500, 0);
	        OutputStream os = r.getResponseBody();
	        os.close();
		}
    }


}



