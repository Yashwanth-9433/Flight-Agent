package com.example.flights.service;

import com.example.flights.dao.BookingsDAO;
import com.example.flights.model.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    @Autowired
    private BookingsDAO bookingsDAO;

    public Booking bookFlight(String flightNumber, String passengerName) {
        int id = bookingsDAO.createBooking(flightNumber, passengerName);
        return bookingsDAO.getBooking(id);
    }

    public Booking getBooking(int id) {
        return bookingsDAO.getBooking(id);
    }
}
