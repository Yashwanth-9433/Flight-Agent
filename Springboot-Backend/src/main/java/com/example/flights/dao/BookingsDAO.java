package com.example.flights.dao;

import com.example.flights.model.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class BookingsDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- BOOKING OPERATIONS ---

    public int createBooking(String flightNumber, String passengerName) {
        // Automatically sets status to CONFIRMED on creation
        String sql = "INSERT INTO bookings (flight_number, passenger_name, status) VALUES (?, ?, 'CONFIRMED')";
        jdbcTemplate.update(sql, flightNumber, passengerName);
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
    }

    public Booking getBooking(int id) {
        String sql = "SELECT * FROM bookings WHERE id=?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new Booking(
                        rs.getInt("id"),
                        rs.getString("flight_number"),
                        rs.getString("passenger_name")
                ), id);
    }

    // Fetches raw map so we can easily check the 'status' column without altering your Booking model
    public Map<String, Object> getBookingDetails(int id) {
        return jdbcTemplate.queryForMap("SELECT * FROM bookings WHERE id=?", id);
    }

    public void updateBookingStatus(int bookingId, String status) {
        jdbcTemplate.update("UPDATE bookings SET status = ? WHERE id = ?", status, bookingId);
    }


    // --- WALLET & FLIGHT OPERATIONS ---

    public Double getWalletBalance(String passengerName) {
        try {
            return jdbcTemplate.queryForObject("SELECT balance FROM wallets WHERE passenger_name = ?", Double.class, passengerName);
        } catch (Exception e) {
            return null; // Returns null if the passenger hasn't set up a wallet
        }
    }

    // ✅ NEW METHOD: Auto-creates a wallet for new users
    public void createWallet(String passengerName, Double initialBalance) {
        String sql = "INSERT INTO wallets (passenger_name, balance) VALUES (?, ?)";
        jdbcTemplate.update(sql, passengerName, initialBalance);
    }

    public void updateWalletBalance(String passengerName, Double amountDelta) {
        // amountDelta can be negative (for booking) or positive (for refund)
        jdbcTemplate.update("UPDATE wallets SET balance = balance + ? WHERE passenger_name = ?", amountDelta, passengerName);
    }

    public Double getFlightPrice(String flightNumber) {
        return jdbcTemplate.queryForObject("SELECT price FROM flights WHERE flight_number = ?", Double.class, flightNumber);
    }

    public void updateFlightSeats(String flightNumber, int seatDelta) {
        // seatDelta can be -1 (booking) or +1 (cancellation)
        jdbcTemplate.update("UPDATE flights SET seats = seats + ? WHERE flight_number = ?", seatDelta, flightNumber);
    }

    // --- HISTORY OPERATIONS ---
    public java.util.List<Map<String, Object>> getBookingHistory(String passengerName) {
        // Fetches all bookings for a user, newest first
        String sql = "SELECT * FROM bookings WHERE passenger_name = ? ORDER BY id DESC";
        return jdbcTemplate.queryForList(sql, passengerName);
    }
}