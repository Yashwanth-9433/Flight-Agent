package com.example.flights.model;

public class Flight {
    private String flightNumber;
    private String origin;
    private String destination;
    private String date;
    private String timeOfDay;
    private double price;
    private String airline;
    private int seats;

    public Flight() {}

    public Flight(String flightNumber, String origin, String destination,
                  String date, String timeOfDay, double price,
                  String airline, int seats) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.date = date;
        this.timeOfDay = timeOfDay;
        this.price = price;
        this.airline = airline;
        this.seats = seats;
    }

    // Getters and Setters
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTimeOfDay() { return timeOfDay; }
    public void setTimeOfDay(String timeOfDay) { this.timeOfDay = timeOfDay; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }

    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
}
