package com.logesh.simpleWebApp.controller;

import com.logesh.simpleWebApp.model.Airport;
import com.logesh.simpleWebApp.model.Flight;
import com.logesh.simpleWebApp.service.AirportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api") // Base URL for all API endpoints
public class FlightController {

    @Autowired
    private AirportService airportService;

    // Endpoint to find the nearest airports to the origin and destination locations
    @GetMapping("/findNearestAirports")
    public Map<String, String> findNearestAirports(@RequestParam String origin, @RequestParam String destination) {
        // Load airports data from CSV
        List<Airport> airports = airportService.loadAirports("src/main/resources/data/airports.csv");

        // Assuming origin and destination are coordinates for now. Modify as needed.
        String[] originCoordinates = origin.split(",");
        String[] destinationCoordinates = destination.split(",");

        double originLat = Double.parseDouble(originCoordinates[0].trim());
        double originLon = Double.parseDouble(originCoordinates[1].trim());
        double destinationLat = Double.parseDouble(destinationCoordinates[0].trim());
        double destinationLon = Double.parseDouble(destinationCoordinates[1].trim());

        // Find nearest airports to the origin and destination
        Airport nearestOriginAirport = airportService.findNearestAirport(originLat, originLon, airports);
        Airport nearestDestinationAirport = airportService.findNearestAirport(destinationLat, destinationLon, airports);

        // Prepare response with nearest airports
        Map<String, String> response = new HashMap<>();
        response.put("origin", nearestOriginAirport.getName());
        response.put("destination", nearestDestinationAirport.getName());

        return response;
    }

    // Endpoint to get travel time and distance using Google Maps API
    @GetMapping("/getRouteDetails")
    public Map<String, String> getRouteDetails(@RequestParam String origin, @RequestParam String destination) {
        String apiKey = "AIzaSyCa7pTHN0LYZrk2u8yW7KbQ3fcZADa5ooE"; // Replace with your actual API key
        String routeDetails = airportService.getTravelTimeAndDistance(origin, destination, apiKey);

        // Assuming the routeDetails method returns a string, split it to extract distance and duration
        // Modify this as per your `getTravelTimeAndDistance` method output.
        String[] details = routeDetails.split(", ");
        String distance = details.length > 0 ? details[0].replace("Distance: ", "") : "Unavailable";
        String duration = details.length > 1 ? details[1].replace("Duration: ", "") : "Unavailable";

        // Prepare response with route details
        Map<String, String> response = new HashMap<>();
        response.put("distance", distance);
        response.put("duration", duration);

        return response;
    }

    // Endpoint to find the shortest flight path between two airports
    @GetMapping("/findFlightPath")
    public Map<String, String> findFlightPath(@RequestParam String origin, @RequestParam String destination) {
        // Load flights data from CSV
        List<Flight> flights = airportService.loadFlights("src/main/resources/data/flights.csv");

        // Find the shortest flight path between the two airports
        List<Flight> flightPath = airportService.findShortestFlightPath(flights, origin, destination);

        // Prepare flight path details
        StringBuilder flightPathDetails = new StringBuilder();
        if (flightPath != null && !flightPath.isEmpty()) {
            for (Flight flight : flightPath) {
                flightPathDetails.append(flight.getAirline())
                        .append(" flight ")
                        .append(flight.getFlightID())
                        .append(" from ")
                        .append(flight.getOriginAirport())
                        .append(" to ")
                        .append(flight.getDestinationAirport())
                        .append(" (")
                        .append(flight.getTotalFlightTime())
                        .append(" minutes)")
                        .append("\n");
            }
        } else {
            flightPathDetails.append("No flight path found.");
        }

        // Prepare response with flight path details
        Map<String, String> response = new HashMap<>();
        response.put("details", flightPathDetails.toString().trim());

        return response;
    }
}

