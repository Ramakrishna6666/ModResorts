package com.acme.modres.mbean.reservation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.acme.modres.Constants;

public class DateChecker implements Runnable {
  private static final Logger logger = Logger.getLogger(DateChecker.class.getName());
  
  ReservationCheckerData data;
  List<Reservation> reservations;

  public DateChecker(ReservationCheckerData data) {
    this.data = data;
    this.reservations = data.getReservationList().getReservations();
  }

  public void run() {
    for (int i = 0; i < reservations.size(); i++) {
      Reservation reservation = reservations.get(i);
      
      // Assuming selectedDate is converted to LocalDate in ReservationCheckerData
      // If it's still a Date, we need to convert it
      LocalDate selectedDate = convertToLocalDate(data.getSelectedDate());

      try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT);
        LocalDate fromDate = LocalDate.parse(reservation.getFromDate(), formatter);
        LocalDate toDate = LocalDate.parse(reservation.getToDate(), formatter);
        
        if (selectedDate.isAfter(fromDate) && selectedDate.isBefore(toDate)) {
          data.setAvailablility(false);
          break;
        }
      } catch (DateTimeParseException ex) {
        logger.log(Level.SEVERE, "Error parsing date", ex);
      }
    }
    data.setAvailablility(true);
  }
  
  private LocalDate convertToLocalDate(Object date) {
    if (date instanceof LocalDate) {
      return (LocalDate) date;
    } else if (date instanceof java.util.Date) {
      return ((java.util.Date) date).toInstant()
          .atZone(java.time.ZoneId.systemDefault())
          .toLocalDate();
    }
    throw new IllegalArgumentException("Unsupported date type: " + date.getClass());
  }
}
