package ca.utoronto.utm.mcs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class Actor {
	@Id
	private String actorId;
	private String actorName;
	
	@Relationship(type = "ACTID_IN", direction = Relationship.UNDIRECTED)
	private List<Movie> movies = null;
	
	public Actor() {}
	
	public Actor(String actorId,String actorName) {
		this.actorName = actorName;
		this.actorId = actorId;
	}

	public Actor(Actor a) {
		this.actorName = a.actorName;
		this.actorId = a.actorId;
		for (Movie m : a.movies) {
			this.addMovie(m);
		}
	}
	
	public void addMovie (Movie m) {
		if(movies == null)
			movies = new ArrayList<Movie>();
		movies.add(m);
	}
	
	public List<Movie> getMovies() {
		return movies;
	}

	public void setMovies(List<Movie> movies) {
		for (Movie m : movies) {
			this.addMovie(m);
		}
	}

	public String getActorName() {
		return actorName;
	}

	public void setActorName(String actorName) {
		this.actorName = actorName;
	}

	public String getActorId() {
		return actorId;
	}

	public void setActorId(String actorId) {
		this.actorId = actorId;
	}
	
	
}
