package com.example.flights.dao;

import com.example.flights.model.Flight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class FlightsDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Flight mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Flight(
                rs.getString("flight_number"),
                rs.getString("origin"),
                rs.getString("destination"),
                rs.getString("date"),
                rs.getString("time_of_day"),
                rs.getDouble("price"),
                rs.getString("airline"),
                rs.getInt("seats")
        );
    }

    public List<Flight> searchFlights(String origin, String destination, String date, String timeOfDay) {
        String sql = "SELECT * FROM flights WHERE origin=? AND destination=? AND date=? AND time_of_day=?";
        return jdbcTemplate.query(sql, this::mapRow, origin, destination, date, timeOfDay);
    }

    public Flight getFlight(String flightNumber) {
        String sql = "SELECT * FROM flights WHERE flight_number=?";
        return jdbcTemplate.queryForObject(sql, this::mapRow, flightNumber);
    }
}
