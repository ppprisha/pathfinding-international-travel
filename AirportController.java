package com.logesh.simpleWebApp.controller;

import com.logesh.simpleWebApp.model.Airport;
import com.logesh.simpleWebApp.model.Flight;
import com.logesh.simpleWebApp.service.AirportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/airports")
public class AirportController {

    private static final Logger logger = LoggerFactory.getLogger(AirportController.class);

    @Autowired
    private AirportService airportService;

    @Value("${airports.csv.path}")
    private String airportsCsvPath;

    @Value("${flights.csv.path}")
    private String flightsCsvPath;

    @GetMapping("/")
    public String printName() {
        return "Welcome Logesh. Welcome to Spring Boot!";
    }

    @GetMapping("/nearest")
    public Airport findNearestAirport(@RequestParam double lat, @RequestParam double lon) {
        List<Airport> airports = airportService.loadAirports(airportsCsvPath);
        if (airports.isEmpty()) {
            logger.warn("No airports data loaded.");
            throw new RuntimeException("No airports data found.");
        }
        Airport nearest = airportService.findNearestAirport(lat, lon, airports);
        logger.info("Nearest airport found: {}", nearest.getName());
        return nearest;
    }


    @GetMapping("/iata-code")
    public String getIATACode(@RequestParam String airportName) {
        return airportService.fetchIATACode(airportName);
    }


    @GetMapping("/geocode")
    public Map<String, Double> getLatLon(@RequestParam String location, @RequestParam String apiKey) {
        // Call the AirportService to get latitude and longitude for the location
        return airportService.getLatLon(location, apiKey);
    }

    @GetMapping("/geocode-to-nearest")
    public Airport getNearestAirportFromLocation(@RequestParam String location, @RequestParam String apiKey) {
        // Get latitude and longitude from the location using the geocode service
        Map<String, Double> latLon = airportService.getLatLon(location, apiKey);

        // Use the obtained latitude and longitude to find the nearest airport
        return findNearestAirport(latLon.get("lat"), latLon.get("lon"));
    }


    @GetMapping("/shortest-flight")
    public List<Flight> findShortestFlightPath(@RequestParam String origin, @RequestParam String destination) {
        List<Flight> flights = airportService.loadFlights(flightsCsvPath);
        if (flights.isEmpty()) {
            logger.warn("No flights data loaded.");
            throw new RuntimeException("No flights data found.");
        }
        List<Flight> flightPath = airportService.findShortestFlightPath(flights, origin, destination);
        if (flightPath == null || flightPath.isEmpty()) {
            logger.info("No flight path found between {} and {}", origin, destination);
        }
        return flightPath;
    }

    @GetMapping("/travel-info")
    public String getTravelInfo(@RequestParam String origin, @RequestParam String destination, @RequestParam String apiKey) {
        String travelInfo = airportService.getTravelTimeAndDistance(origin, destination, apiKey);
        if (travelInfo == null || travelInfo.isEmpty()) {
            logger.warn("Travel information could not be retrieved.");
            throw new RuntimeException("Failed to retrieve travel information.");
        }
        return travelInfo;
    }
}
