package com.logesh.simpleWebApp.service;

import com.logesh.simpleWebApp.model.Flight;
import com.logesh.simpleWebApp.model.Airport;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@Service
public class AirportService {

    @Value("${aviationstack.api.key}")
    private String apiKey;

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // convert to kilometers
    }

    public String fetchIATACode(String airportName) {
        String url = String.format("http://api.aviationstack.com/v1/airports?access_key=%s&search=%s", apiKey, airportName);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        try {
            // Parse the JSON response to extract the IATA code
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode data = root.path("data");

            if (data.isArray() && data.size() > 0) {
                String iataCode = data.get(0).path("iata_code").asText();
                return iataCode.isEmpty() ? "IATA code not found" : iataCode;
            } else {
                return "IATA code not found";
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching IATA code from API", e);
        }
    }

    public List<Airport> loadAirports(String csvFile) {
        List<Airport> airports = new ArrayList<>();
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] airportData = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); // Handles commas inside quotes

                String name = airportData[0].trim();
                String code = airportData[2].trim(); // Assuming the airport code is the 3rd column (index 2)
                String address = airportData[3].trim();
                double latitude = Double.parseDouble(airportData[4].trim());
                double longitude = Double.parseDouble(airportData[5].trim());
                String type = airportData[8].trim(); // Assuming the 'Type' column is the 9th column (index 8)

                // Filter to include only airports with "International" in their name
                if (type.equalsIgnoreCase("Airport") && name.toLowerCase().contains("international")) {
                    airports.add(new Airport(name, code, address, latitude, longitude, type));
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return airports;
    }

    public Map<String, Double> getLatLon(String location, String apiKey) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + location + "&key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            if ("OK".equals(root.path("status").asText())) {
                JsonNode locationNode = root.path("results").get(0).path("geometry").path("location");
                Map<String, Double> latLon = new HashMap<>();
                latLon.put("lat", locationNode.path("lat").asDouble());
                latLon.put("lon", locationNode.path("lng").asDouble());
                return latLon;
            } else {
                throw new RuntimeException("Failed to get latitude and longitude for location: " + location);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing JSON response", e);
        }
    }

    public Airport findNearestAirport(double lat, double lon, List<Airport> airports) {
        Airport nearestAirport = null;
        double minDistance = Double.MAX_VALUE;

        for (Airport airport : airports) {
            double distance = calculateDistance(lat, lon, airport.getLatitude(), airport.getLongitude());
            if (distance < minDistance) {
                minDistance = distance;
                nearestAirport = airport;
            }
        }

        return nearestAirport;
    }

    public List<Flight> loadFlights(String csvFile) {
        List<Flight> flights = new ArrayList<>();
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] flightData = line.split(",");

                if (flightData.length < 11) {
                    System.err.println("Skipping invalid line (not enough columns): " + line);
                    continue; // skip invalid rows
                }

                String airline = flightData[0].trim();
                String flightID = flightData[1].trim();
                String originAirport = flightData[3].trim();
                String destinationAirport = flightData[6].trim();
                String originCountry = flightData[5].trim();
                String destinationCountry = flightData[9].trim();

                // Convert Total Flight Time to minutes
                int totalFlightTime = 0;
                try {
                    String[] timeParts = flightData[10].trim().split(" ");
                    int hours = 0;
                    int minutes = 0;

                    for (int i = 0; i < timeParts.length; i++) {
                        if (timeParts[i].contains("hr")) {
                            hours = Integer.parseInt(timeParts[i - 1].trim());
                        } else if (timeParts[i].contains("min")) {
                            minutes = Integer.parseInt(timeParts[i - 1].trim());
                        }
                    }

                    totalFlightTime = hours * 60 + minutes;
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing flight time for flight " + flightID + ": " + e.getMessage());
                    continue; // skip invalid time entries
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println("Unexpected time format for flight " + flightID + ": " + flightData[10].trim());
                    continue; // skip lines with unexpected time format
                }

                flights.add(new Flight(airline, flightID, originAirport, destinationAirport, originCountry,
                        destinationCountry, totalFlightTime));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return flights;
    }

    public List<Flight> findShortestFlightPath(List<Flight> flights, String origin, String destination) {
        PriorityQueue<FlightNode> pq = new PriorityQueue<>(Comparator.comparingInt(fn -> fn.totalTime));
        Map<String, FlightNode> airportMap = new HashMap<>();

        pq.add(new FlightNode(origin, null, 0));
        while (!pq.isEmpty()) {
            FlightNode current = pq.poll();

            if (current.airport.equals(destination)) {
                return buildPath(current);
            }

            for (Flight flight : flights) {
                if (flight.getOriginAirport().equals(current.airport)) {
                    int newTime = current.totalTime + flight.getTotalFlightTime();

                    if (!airportMap.containsKey(flight.getDestinationAirport())
                            || newTime < airportMap.get(flight.getDestinationAirport()).totalTime) {
                        FlightNode nextNode = new FlightNode(flight.getDestinationAirport(), current, newTime, flight);
                        airportMap.put(flight.getDestinationAirport(), nextNode);
                        pq.add(nextNode);
                    }
                }
            }
        }

        return null; // No path found
    }

    private List<Flight> buildPath(FlightNode node) {
        List<Flight> path = new ArrayList<>();
        while (node.previous != null) {
            path.add(node.flight);
            node = node.previous;
        }
        Collections.reverse(path);
        return path;
    }

    static class FlightNode {
        String airport;
        FlightNode previous;
        int totalTime;
        Flight flight;

        FlightNode(String airport, FlightNode previous, int totalTime) {
            this.airport = airport;
            this.previous = previous;
            this.totalTime = totalTime;
        }

        FlightNode(String airport, FlightNode previous, int totalTime, Flight flight) {
            this.airport = airport;
            this.previous = previous;
            this.totalTime = totalTime;
            this.flight = flight;
        }
    }

    public String getTravelTimeAndDistance(String origin, String destination, String apiKey) {
        try {
            origin = URLEncoder.encode(origin, "UTF-8");
            destination = URLEncoder.encode(destination, "UTF-8");

            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + origin
                    + "&destinations=" + destination + "&key=" + apiKey;

            StringBuilder result = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();

            // Log the entire response for debugging
            System.out.println("Raw JSON Response: " + result.toString());

            // Parse JSON response using Jackson
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(result.toString());

            // Defensive check for "rows" and "elements"
            JsonNode rowsNode = rootNode.path("rows");
            if (rowsNode.isArray() && rowsNode.size() > 0) {
                JsonNode elementsNode = rowsNode.get(0).path("elements");
                if (elementsNode.isArray() && elementsNode.size() > 0) {
                    JsonNode firstElementNode = elementsNode.get(0);

                    String distance = firstElementNode.path("distance").path("text").asText("Distance Unavailable");
                    String duration = firstElementNode.path("duration").path("text").asText("Duration Unavailable");

                    return "Distance: " + distance + ", Duration: " + duration;
                }
            }

            return "Error: No valid route found between origin and destination.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error retrieving travel information: " + e.getMessage();
        }
    }
}
