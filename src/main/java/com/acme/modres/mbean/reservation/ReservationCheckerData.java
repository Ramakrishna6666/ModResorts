package com.acme.modres.mbean.reservation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.acme.modres.Constants;

/**
 * Updated for Java 21 compatibility:
 * - Replaced legacy java.util.Date and SimpleDateFormat with java.time.LocalDate
 *   and DateTimeFormatter for thread-safe, modern date handling.
 *   (Rule: JAVA8_TO_21_DATE_TIME_CHANGES)
 */
public class ReservationCheckerData {
  private ReservationList reservations;
  private LocalDate selectedDate;
  private boolean available; // changed from Boolean to boolean

  public ReservationCheckerData(ReservationList reservations) {
    this.reservations = reservations;
    this.available = true;
  }

  public ReservationList getReservationList() {
    return reservations;
  }

  public LocalDate getSelectedLocalDate() {
    return selectedDate;
  }

  /**
   * @deprecated Use getSelectedLocalDate() instead.
   * Kept for backward compatibility.
   */
  @Deprecated
  public java.util.Date getSelectedDate() {
    if (selectedDate == null) return null;
    return java.sql.Date.valueOf(selectedDate);
  }

  public boolean setSelectedDate(String dateStr) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT);
      selectedDate = LocalDate.parse(dateStr, formatter);
    } catch (DateTimeParseException e) {
      return false;
    }
    return true;
  }

  public boolean isAvailible() {
    return available;
  }

  public void setAvailablility(boolean available) {
    this.available = available;
  }
}
