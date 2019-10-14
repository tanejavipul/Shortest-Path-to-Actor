package ca.utoronto.utm.mcs;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.Values;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.sound.midi.SysexMessage;

import static org.neo4j.driver.v1.Values.NULL;
import static org.neo4j.driver.v1.Values.parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongToIntFunction;
import java.util.function.ToIntFunction;


public class DB implements AutoCloseable {
    private final Driver driver;

    public DB() {
        String uri = "bolt://localhost:7687";
        String user = "neo4j";
        String password = "secret";

        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

    public void addActor(Actor actor) {
        String aName = actor.getActorName();
        String aId = actor.getActorId();

        try (Session session = driver.session()) {
            String result = session.writeTransaction(new TransactionWork<String>() {
                @Override
                public String execute(Transaction tx) {
                    StatementResult result = tx.run("MERGE (a:actor {name: {aName}, id: {aId} })" +
                                    "RETURN a.name + ' ' + a.id",
                            Values.parameters("aName", aName, "aId", aId)
                    );
                    return result.single().get(0).asString();
                }
            });
            //System.out.println( result );
        }
    }

    public void addMovie(Movie movie) {
        String mName = movie.getMovieName();
        String mId = movie.getMovieId();

        try (Session session = driver.session()) {
            String result = session.writeTransaction(new TransactionWork<String>() {
                @Override
                public String execute(Transaction tx) {
                    StatementResult result = tx.run("MERGE (m:movie {name: {mName}, id: {mId}})" +
                                    "RETURN m.name + ' ' + m.id",
                            Values.parameters("mName", mName, "mId", mId)
                    );
                    return result.single().get(0).asString();
                }
            });
            //System.out.println( result );
        }
    }

    public void addRelationship(String actorId, String movieId) {
        try (Session session = driver.session()) {
            String result = session.writeTransaction(new TransactionWork<String>() {
                @Override
                public String execute(Transaction tx) {
                    StatementResult result = tx.run("MATCH (a:actor {id: {actorId}}), (m:movie {id: {movieId}})" +
                                    "MERGE (a)-[:ACTED_IN]-(m)" +
                                    "RETURN a.name + ' ' + m.name",
                            Values.parameters("actorId", actorId, "movieId", movieId));
                    return result.single().get(0).asString();
                }
            });
            
        }
    }

    public Movie getMovie(String movieId) {
        try (Session session = driver.session()) {
            return session.readTransaction(new TransactionWork<Movie>() {
                @Override
                public Movie execute(Transaction tx) {
                    Movie m = new Movie(movieId, "");

                    StatementResult result = tx.run("MATCH (m:movie {id: {movieId}}) RETURN m.name",
                            Values.parameters("movieId", movieId));
                    
                    if (result.hasNext()) m.setMovieName(result.single().get(0).asString());

                    result = tx.run("MATCH (m:movie {id: {movieId}}), (m)<-[:ACTED_IN]-(a:actor) RETURN a.id, a.name",
                            Values.parameters("movieId", movieId));

                    while (result.hasNext()) {
                        Record r = result.next();
                        Actor a = new Actor(r.get(0).asString(), r.get(1).asString());
                        m.addActor(a);
                    }

                    return m;
                }
            });
        }
    }

    public Actor getActor(String actorId) {
        try (Session session = driver.session()) {
            return session.readTransaction(new TransactionWork<Actor>() {
                @Override
                public Actor execute(Transaction tx) {
                    Actor a = new Actor(actorId, "");


                    StatementResult result = tx.run("MATCH (a:actor {id: {actorId}}) RETURN a.name",
                            Values.parameters("actorId", actorId));
                    if (result.hasNext()) a.setActorName(result.single().get(0).asString());

                    result = tx.run("MATCH (a:actor {id: {actorId}}), (a)-[:ACTED_IN]->(m:movie) RETURN m.id, m.name",
                            Values.parameters("actorId", actorId));
                    int x = 0;
                    while (result.hasNext()) {
                        Record r = result.next();
                        Movie m = new Movie(r.get(0).asString(), r.get(1).asString());
                        a.addMovie(m);
                        x++;
                    }


                    return a;
                }
            });
        }
    }

    public Boolean hasRelationship(String actorId, String movieId) {
        try (Session session = driver.session()) {
            Boolean result = session.readTransaction(new TransactionWork<Boolean>() {
                @Override
                public Boolean execute(Transaction tx) {
                    StatementResult result = tx.run("MATCH (a:actor {id: {actorId}}), (m:movie {id: {movieId}})" +
                                    "RETURN EXISTS( (a)-[:ACTED_IN]-(m) )",
                            Values.parameters("actorId", actorId, "movieId", movieId)
                    );

                    return result.single().get(0).asBoolean();
                }
            });
            return result;
        }

    }

    public Long computeBaconNumber(String actorId) {

        try (Session session = driver.session()) {
            Long result = session.readTransaction(new TransactionWork<Long>() {
                @Override
                public Long execute(Transaction tx) {
                    StatementResult result = tx.run("OPTIONAL MATCH p=shortestPath((a:actor {id: {actorId}})-[*]-(b:actor {id: {baconId}}))" +
                                    "RETURN CASE p WHEN NULL THEN (-1) ELSE length(p) END",
                            Values.parameters("actorId", actorId, "baconId", App.getBaconID())
                    );
                    Long ret = result.single().get(0).asLong();
                    //System.out.println(ret);

                    return ret == -1 ? ret : ret/2;
                }
            });

            return result;
        }
    }

    public String[] computeBaconPath(String actorId) {
        long baconNumber = computeBaconNumber(actorId);
        //System.out.println(baconNumber);
        //System.out.println("ddd");
        /*
	    if (baconNumber == -1)
        {
            System.out.println("ddd");
            return null;
        }*/

        try (Session session = driver.session()) {
            //System.out.println("d");
            String[] result = session.readTransaction(new TransactionWork<String[]>() {
                @Override
                public String[] execute(Transaction tx) {
                    //MATCH p=shortestPath (bacon:Person {name:"Kevin Bacon"})-[*]-(meg:Person {name:"Meg Ryan"}) RETURN p
                    StatementResult result = tx.run("MATCH p=shortestPath((a:actor {id: {actorId}})-[*]-(b:actor {id: {baconId}}))" +
                                    "RETURN nodes(p)",
                            Values.parameters("actorId", actorId, "baconId", App.getBaconID())
                    );
                    Record record = result.next();
                    int size = record.get(0).size();
                    String ids[] = new String[size-1];
                    /*
                    System.out.println(size);
                    System.out.println((record.get(0).toString()));
                    System.out.println(record.get(0).get(0).get("actorId").toString());
                    System.out.println(record.get(0).get(1).get("actorId").toString());

                    int y = 0;
                    while(y < size*2)
                    {
                        System.out.println(record.get(0).get(y).get("id").toString());
                        y++;
                    }
                    */

                    //System.out.println(record.get(0).size());
                    int x = 1;
                    while (x < record.get(0).size())
                    {
                        //System.out.println("ids: " + (record.get(0).get(x).get("id").toString()));
                        ids[x-1] = record.get(0).get(x).get("id").toString();
                        //System.out.println("index: " + x);
                        x++;
                    }
                    //System.out.println("index: " + x);

                    /*
                    x = 0;
                    while (x < size-1) {
                        System.out.println(ids[x]);
                        x++;
                    }
                    */


                    return ids;


                    //System.out.println(result.single().get(1).get(1).get("movieId"));



                }
            });
            return result;
        }
    }


}
