package com.acme.modres.mbean.reservation;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.acme.modres.Constants;

/**
 * Cloud-ready Reservation Checker Data
 * Uses UTC timezone for consistent behavior across distributed cloud environments
 */
public class ReservationCheckerData {
  
  private static final Logger logger = Logger.getLogger(ReservationCheckerData.class.getName());
  
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
   * Set selected date with UTC timezone for cloud consistency
   * Ensures timezone-agnostic behavior across multiple regions
   */
  public boolean setSelectedDate(String dateStr) {
    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATA_FORMAT);
      // Use UTC timezone for consistent behavior in cloud environments
      dateFormat.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
      selectedDate = dateFormat.parse(dateStr);
      logger.info("Selected date set to: " + selectedDate + " (UTC)");
    } catch (Exception e) {
      logger.warning("Failed to parse date: " + dateStr + " - " + e.getMessage());
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
