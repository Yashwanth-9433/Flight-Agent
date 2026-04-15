package com.example.flights.dao;

import com.example.flights.model.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BookingsDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int createBooking(String flightNumber, String passengerName) {
        String sql = "INSERT INTO bookings (flight_number, passenger_name) VALUES (?, ?)";
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
}
