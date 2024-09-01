package com.logesh.simpleWebApp.model;

public class Flight {
    private String airline;
    private String flightID;
    private String originAirport;
    private String destinationAirport;
    private String originCountry;
    private String destinationCountry;
    private int totalFlightTime; // in minutes

    // Constructor
    public Flight(String airline, String flightID, String originAirport, String destinationAirport,
                  String originCountry, String destinationCountry, int totalFlightTime) {
        this.airline = airline;
        this.flightID = flightID;
        this.originAirport = originAirport;
        this.destinationAirport = destinationAirport;
        this.originCountry = originCountry;
        this.destinationCountry = destinationCountry;
        this.totalFlightTime = totalFlightTime;
    }

    // Getters
    public String getAirline() {
        return airline;
    }

    public String getFlightID() {
        return flightID;
    }

    public String getOriginAirport() {
        return originAirport;
    }

    public String getDestinationAirport() {
        return destinationAirport;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public String getDestinationCountry() {
        return destinationCountry;
    }

    public int getTotalFlightTime() {
        return totalFlightTime;
    }

    // Setters (if needed)
    public void setAirline(String airline) {
        this.airline = airline;
    }

    public void setFlightID(String flightID) {
        this.flightID = flightID;
    }

    public void setOriginAirport(String originAirport) {
        this.originAirport = originAirport;
    }

    public void setDestinationAirport(String destinationAirport) {
        this.destinationAirport = destinationAirport;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public void setDestinationCountry(String destinationCountry) {
        this.destinationCountry = destinationCountry;
    }

    public void setTotalFlightTime(int totalFlightTime) {
        this.totalFlightTime = totalFlightTime;
    }
}
