package com.example.flights.service;

import com.example.flights.dao.FlightsDAO;
import com.example.flights.model.Flight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FlightService {

    @Autowired
    private FlightsDAO flightsDAO;

    public List<Flight> searchFlights(String origin, String destination, String date, String timeOfDay) {
        return flightsDAO.searchFlights(origin, destination, date, timeOfDay);
    }

    public Flight getFlight(String flightNumber) {
        return flightsDAO.getFlight(flightNumber);
    }
}
