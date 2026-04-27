package com.acme.modres.mbean.reservation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.acme.modres.Constants;

/**
 * Cloud-ready date checker using java.time API for timezone-safe operations.
 * Standardized on UTC to avoid timezone inconsistencies in distributed cloud environments.
 */
public class DateChecker implements Runnable {
  ReservationCheckerData data;
  List<Reservation> reservations;

  public DateChecker(ReservationCheckerData data) {
    this.data = data;
    this.reservations = data.getReservationList().getReservations();
  }

  public void run() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT);
    
    for (int i = 0; i < reservations.size(); i++) {
      Reservation reservation = reservations.get(i);
      
      // Convert java.util.Date to LocalDate using UTC timezone
      LocalDate selectedDate = LocalDate.ofInstant(
          Instant.ofEpochMilli(data.getSelectedDate().getTime()), 
          ZoneId.of("UTC"));

      try {
        LocalDate fromDate = LocalDate.parse(reservation.getFromDate(), formatter);
        LocalDate toDate = LocalDate.parse(reservation.getToDate(), formatter);
        
        if (selectedDate.isAfter(fromDate) && selectedDate.isBefore(toDate)) {
          data.setAvailablility(false);
          return;
        }
      } catch (DateTimeParseException ex) {
        ex.printStackTrace();
      }
    }
    data.setAvailablility(true);
  }
}
