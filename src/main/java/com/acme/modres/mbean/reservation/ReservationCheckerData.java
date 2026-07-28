package com.acme.modres.mbean.reservation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.logging.Logger;

import com.acme.modres.Constants;
import com.acme.modres.mbean.IOUtils;

public class ReservationCheckerData {
  private static final Logger logger = Logger.getLogger(ReservationCheckerData.class.getName());
  
  private ReservationList reservations;
  private Object selectedDate; // Can be Date or LocalDate for backward compatibility
  private boolean available; // changed from Boolean to boolean

  public ReservationCheckerData() {
    this.reservations = IOUtils.getReservationListFromConfig();
    this.available = true;
  }

  public ReservationCheckerData(ReservationList reservations) {
    this.reservations = reservations;
    this.available = true;
  }

  public ReservationList getReservationList() {
    return reservations;
  }

  public Object getSelectedDate() {
    return selectedDate;
  }

  public boolean setSelectedDate(String dateStr) {
    try {
      // Try to parse using modern Java Time API
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT);
      selectedDate = LocalDate.parse(dateStr, formatter);
      return true;
    } catch (DateTimeParseException e) {
      logger.warning("Failed to parse date: " + dateStr);
      return false;
    }
  }

  public boolean isAvailible() {
    return available;
  }

  public void setAvailablility(boolean available) { // fix parameter type
    this.available = available;
  }
}
