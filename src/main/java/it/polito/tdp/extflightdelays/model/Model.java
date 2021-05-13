package it.polito.tdp.extflightdelays.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.extflightdelays.db.Collegamento;
import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	Graph<Airport, DefaultWeightedEdge> grafo;
	Map<Integer, Airport> airports;
	List<Flight> flights;
	List<Airline> airlines;
	ExtFlightDelaysDAO dao;
	
	public Graph<Airport, DefaultWeightedEdge> creaGrafo(int distanzaMinima){
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		this.dao = new ExtFlightDelaysDAO();
		
		if(this.airports==null)
			airports = dao.loadAllAirports();
		if(this.flights==null)
			flights = dao.loadAllFlights();
		if(this.airlines==null)
			airlines = dao.loadAllAirlines();
		
		//Aggiungo due aeroporti al grafo solo se sono collegati da almeno 1 volo
		//e se la distanza media dei voli che li collegano è > di distanzaMinima
		Map<String, Collegamento> collegamenti = dao.loadCollegamenti();
		for (Collegamento c : collegamenti.values()) {
			
			//Per ogni coppia di aeroporti vado a vedere se c'è anche con origine e destinazione 
			//invertite e in tal caso il calcolo della distanza media dovrà tenerne conto.
			String keyC1 = ""+c.getDestination_airport_id()+"_"+c.getOrigin_airport_id();
			Collegamento c1 = collegamenti.get(keyC1);
			double distanzaMedia = 0.0;
			if(c1!=null) {
				distanzaMedia = c.getDistanza_totale()+c1.getDistanza_totale();
				distanzaMedia = distanzaMedia/(c.getNumero_voli()+c1.getNumero_voli());
			} else distanzaMedia = c.getDistanza_totale()/c.getNumero_voli();
			
		
			if(distanzaMedia>distanzaMinima) {
				
				//Aggiungo solo i vertici che non c'erano già
				Airport a = this.airports.get(c.getOrigin_airport_id());
				if(!this.grafo.containsVertex(a)) {
					this.grafo.addVertex(a);
				}
				Airport b = this.airports.get(c.getDestination_airport_id());
				if(!this.grafo.containsVertex(b)) {
					this.grafo.addVertex(b);
				}
				
				//E li collego con un arco opportunamente pesato, se non esiste già
				//un arco diretto tra loro
				if(this.grafo.getEdge(a, b)==null) {
					DefaultWeightedEdge e = this.grafo.addEdge(a, b);
					this.grafo.setEdgeWeight(e, distanzaMedia);
				}
			}
		}
		System.out.println("Grafo creato con "+this.getVertexNumber()+" vertici e "+this.getEdgesNumber()+" archi");
		return grafo;
	}
	
	public int getVertexNumber() {
		return this.grafo.vertexSet().size();
	}
	public int getEdgesNumber() {
		return this.grafo.edgeSet().size();
	}
	public String getAllEdges() {
		String result = "";
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			result += e.toString()+" "+this.grafo.getEdgeWeight(e)+"\n";
			
		}
		return result;
	}
}
