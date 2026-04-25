package com.example.flights.service;

import com.example.flights.dao.BookingsDAO;
import com.example.flights.model.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class BookingService {

    @Autowired
    private BookingsDAO bookingsDAO;

    @Transactional
    public Map<String, Object> bookFlight(String flightNumber, String passengerName) {
        Double price = bookingsDAO.getFlightPrice(flightNumber);
        Double balance = bookingsDAO.getWalletBalance(passengerName);

        boolean isNewWallet = false; // ✅ 1. Add a flag to track new users

        if (balance == null) {
            System.out.println("🚀 New user detected! Auto-provisioning wallet for: " + passengerName);
            bookingsDAO.createWallet(passengerName, 15000.0);
            balance = 15000.0;
            isNewWallet = true; // ✅ 2. Flip the flag to true
        }

        if (balance < price) {
            throw new RuntimeException("Insufficient funds! Balance: ₹" + balance + ", Cost: ₹" + price);
        }

        bookingsDAO.updateWalletBalance(passengerName, -price);
        bookingsDAO.updateFlightSeats(flightNumber, -1);
        int id = bookingsDAO.createBooking(flightNumber, passengerName);

        return Map.of(
                "message", "Booking successful! ₹" + price + " deducted.",
                "bookingId", id,
                "flightNumber", flightNumber,
                "passengerName", passengerName,
                "newBalance", balance - price,
                "newWalletCreated", isNewWallet // ✅ 3. Send the flag back to Python
        );
    }

    @Transactional
    public Map<String, Object> cancelBooking(int bookingId) {
        // 1. Get current booking details
        Map<String, Object> booking = bookingsDAO.getBookingDetails(bookingId);
        String status = (String) booking.get("status");
        String flightNum = (String) booking.get("flight_number");
        String passenger = (String) booking.get("passenger_name");

        if ("CANCELLED".equalsIgnoreCase(status)) {
            throw new RuntimeException("Booking #" + bookingId + " is already cancelled.");
        }

        // 2. Calculate Refund
        Double refundAmount = bookingsDAO.getFlightPrice(flightNum);

        // 3. Execute Transaction
        bookingsDAO.updateBookingStatus(bookingId, "CANCELLED"); // Mark as cancelled
        bookingsDAO.updateWalletBalance(passenger, refundAmount); // Refund money
        bookingsDAO.updateFlightSeats(flightNum, 1);              // Free up the seat

        // 4. Return clean JSON response map
        return Map.of(
                "message", "Cancellation successful! ₹" + refundAmount + " refunded.",
                "bookingId", bookingId,
                "refundedAmount", refundAmount
        );
    }

    public Booking getBooking(int id) {
        return bookingsDAO.getBooking(id);
    }

    public java.util.List<Map<String, Object>> getBookingHistory(String passengerName) {
        return bookingsDAO.getBookingHistory(passengerName);
    }
}