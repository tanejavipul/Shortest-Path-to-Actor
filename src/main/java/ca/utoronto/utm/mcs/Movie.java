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
public class Movie {
	@Id
	private String movieId;
	private String movieName;
	
	@Relationship(type = "ACTORS", direction = Relationship.UNDIRECTED)
	private List<Actor> actors = null;
	
	public Movie() {}
	
	public Movie(String movieId,String movieName) {
		this.movieName = movieName;
		this.movieId = movieId;
	}
	
	public Movie(Movie m) {
		this.movieName = m.movieName;
		this.movieId = m.movieId;
		for (Actor a : m.actors) {
			this.addActor(a);
		}
	}
	
	public List<Actor> getActors() {
		return actors;
	}

	public void setActors(List<Actor> actors) {
		for (Actor a : actors) {
			this.addActor(a);
		}
	}

	public void addActor (Actor a) {
		if(actors == null)
			actors = new ArrayList<Actor>();
		actors.add(a);
	}

	public String getMovieName() {
		return movieName;
	}

	public void setMovieName(String movieName) {
		this.movieName = movieName;
	}

	public String getMovieId() {
		return movieId;
	}

	public void setMovieId(String movieId) {
		this.movieId = movieId;
	}
	
}
