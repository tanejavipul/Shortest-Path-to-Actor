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


public class ComputeBaconPath implements HttpHandler {

    public ComputeBaconPath() {
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
            DB connection = new DB();


            String actorId = "";

            if (deserialized.has("actorId"))
                actorId = deserialized.getString("actorId");
            Long baconNumber;
            if (actorId.isEmpty()) {
                r.sendResponseHeaders(400, 0);
                OutputStream os = r.getResponseBody();
                os.close();
            } else if (actorId.equals(App.getBaconID())) {
                JSONObject object = new JSONObject();
                object.put("baconNumber", "0");
                List<JSONObject> baconPath = new ArrayList<JSONObject>();
                object.put("baconPath", baconPath);

                String response = object.toString();
                r.sendResponseHeaders(200, response.length());
                OutputStream os = r.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
            else if (connection.getActor(actorId).getActorName().isEmpty()) //actor does not exist
            {
                r.sendResponseHeaders(400, 0);
                OutputStream os = r.getResponseBody();
                os.close();
            } else {
                baconNumber = connection.computeBaconNumber(actorId);
                if (baconNumber == -1) {
                    r.sendResponseHeaders(404, 0); //fix
                    OutputStream os = r.getResponseBody();
                    os.close();
                } else {

                    String ids[] = connection.computeBaconPath(actorId);

                    /* LEAVE THIS for testing purposes
                    int x = 0;
                    while (x < 2) {
                    System.out.println(ids[x]);
                    x++;
                    }
                    */

                    JSONObject object = new JSONObject();
                    List<JSONObject> baconPath = new ArrayList<JSONObject>();

                    try {
                        object.put("baconNumber", baconNumber.toString());

                        int y = 0;

                        while (y < baconNumber * 2) { //even is movies since first actor is out
                            JSONObject temp = new JSONObject();
                            temp.put("actorId", ids[y + 1].replaceAll("\"", ""));
                            temp.put("movieId", ids[y].replaceAll("\"", ""));
                            baconPath.add(temp);
                            y += 2;
                        }

                        object.put("baconPath", baconPath);


                        String response = "";
                        response = object.toString();
                        r.sendResponseHeaders(200, response.length());
                        OutputStream os = r.getResponseBody();
                        os.write(response.getBytes());
                        os.close();

                    } catch (Exception e) {
                        e.getMessage();
                        r.sendResponseHeaders(500, 0);
                        OutputStream os = r.getResponseBody();
                        os.close();

                    }
                }
            }
        } catch (Exception e) {
            r.sendResponseHeaders(400, 0);
            OutputStream os = r.getResponseBody();
            os.close();
        }
    }

}
