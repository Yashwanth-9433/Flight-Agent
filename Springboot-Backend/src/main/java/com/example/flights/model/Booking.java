package com.example.flights.model;

public class Booking {
    private int id;
    private String flightNumber;
    private String passengerName;

    public Booking() {}

    public Booking(int id, String flightNumber, String passengerName) {
        this.id = id;
        this.flightNumber = flightNumber;
        this.passengerName = passengerName;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
}
