package com.acme.modres.mbean.reservation;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import com.acme.modres.Constants;

/**
 * Cloud-ready reservation checker using java.time API for timezone-safe operations.
 * Standardized on UTC to avoid timezone inconsistencies in distributed cloud environments.
 */
public class ReservationCheckerData {
  private ReservationList reservations;
  private Date selectedDate;
  private boolean available;

  public ReservationCheckerData(ReservationList reservations) {
    this.reservations = reservations;
    this.available = true;
  }

  public ReservationList getReservationList() {
    return reservations;
  }

  public Date getSelectedDate() {
    return selectedDate;
  }

  /**
   * Parse date string using java.time API and store as UTC.
   * This ensures consistent date handling across distributed cloud environments.
   */
  public boolean setSelectedDate(String dateStr) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT);
      LocalDate localDate = LocalDate.parse(dateStr, formatter);
      
      // Convert to Date using UTC timezone
      this.selectedDate = Date.from(localDate.atStartOfDay(ZoneId.of("UTC")).toInstant());
      return true;
    } catch (DateTimeParseException e) {
      return false;
    }
  }

  public boolean isAvailible() {
    return available;
  }

  public void setAvailablility(boolean available) {
    this.available = available;
  }
}
