package com.acme.modres.mbean.reservation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.acme.modres.Constants;

/**
 * Cloud-ready Date Checker
 * Designed to work with Azure Service Bus scheduled messages
 * instead of local java.util.Timer
 */
public class DateChecker implements Runnable {
  
  private static final Logger logger = Logger.getLogger(DateChecker.class.getName());
  
  ReservationCheckerData data;
  List<Reservation> reservations;

  public DateChecker(ReservationCheckerData data) {
    this.data = data;
    this.reservations = data.getReservationList().getReservations();
  }

  /**
   * Check date availability
   * This method is designed to be invoked by Azure Service Bus message handler
   * instead of java.util.Timer for cloud-native distributed scheduling
   */
  public void run() {
    try {
      logger.info("DateChecker task started at UTC: " + 
                  new Date().toInstant().atOffset(ZoneOffset.UTC));
      
      for (int i = 0; i < reservations.size(); i++) {
        Reservation reservation = reservations.get(i);
        Date selectedDate = data.getSelectedDate();

        try {
          Date fromDate = new SimpleDateFormat(Constants.DATA_FORMAT).parse(reservation.getFromDate());
          Date toDate = new SimpleDateFormat(Constants.DATA_FORMAT).parse(reservation.getToDate());
          if (selectedDate.after(fromDate) && selectedDate.before(toDate)) {
            data.setAvailablility(false);
            logger.info("Date unavailable: " + selectedDate);
            return;
          }
        } catch (ParseException ex) {
          logger.severe("Failed to parse reservation dates: " + ex.getMessage());
          ex.printStackTrace();
        }
      }
      data.setAvailablility(true);
      logger.info("Date available: " + data.getSelectedDate());
    } catch (Exception e) {
      logger.severe("Error in DateChecker task: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
