package com.example.flights.controller;

import com.example.flights.model.Booking;
import com.example.flights.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flights")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // ✅ Allow both GET and POST for booking (for browser/Python testing)
    @RequestMapping(value = "/book", method = {RequestMethod.GET, RequestMethod.POST})
    public Booking bookFlight(
            @RequestParam String flightNumber,
            @RequestParam String passengerName) {
        return bookingService.bookFlight(flightNumber, passengerName);
    }

    // ✅ Check booking status
    @GetMapping("/status")
    public Booking getBookingStatus(@RequestParam int bookingId) {
        return bookingService.getBooking(bookingId);
    }
}
