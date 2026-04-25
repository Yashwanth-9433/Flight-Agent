package com.example.flights.controller;

import com.example.flights.model.Booking;
import com.example.flights.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flights")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // ✅ Updated Booking Endpoint (now requires wallet check)
    @RequestMapping(value = "/book", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> bookFlight(
            @RequestParam String flightNumber,
            @RequestParam String passengerName) {
        try {
            // We'll update the service to return a success message string instead of a raw Booking object
            return ResponseEntity.ok(bookingService.bookFlight(flightNumber, passengerName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // ✅ New Cancellation Endpoint
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelBooking(@RequestParam int bookingId) {
        try {
            return ResponseEntity.ok(bookingService.cancelBooking(bookingId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // ✅ Check booking status (Keeping your existing one)
    @GetMapping("/status")
    public Booking getBookingStatus(@RequestParam int bookingId) {
        return bookingService.getBooking(bookingId);
    }

    // ✅ New History Endpoint
    @GetMapping("/history")
    public ResponseEntity<?> getBookingHistory(@RequestParam String passengerName) {
        try {
            return ResponseEntity.ok(bookingService.getBookingHistory(passengerName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}