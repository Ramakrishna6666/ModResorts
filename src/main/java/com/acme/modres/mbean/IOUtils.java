package com.acme.modres.mbean;

import java.io.IOException;
import java.io.InputStream;

import com.acme.modres.mbean.reservation.ReservationList;
import com.acme.modres.util.JsonInputStream;

/**
 * Cloud-native utility class for loading configuration from classpath resources.
 * Eliminates local file system dependencies by using classpath resources only.
 */
public final class IOUtils {

  /**
   * Load configuration from classpath resources instead of file system.
   * This approach works in containerized environments where file system is ephemeral.
   */
  public static InputStream getResourceAsStream(String path) {
    return IOUtils.class.getClassLoader().getResourceAsStream(path);
  }

  public static OpMetadataList getOpListFromConfig() {
    // Load directly from classpath - no temporary files needed
    try (InputStream is = getResourceAsStream("ops.json")) {
      if (is == null) {
        System.err.println("ops.json not found in classpath");
        return new OpMetadataList(); // empty default
      }
      try (JsonInputStream jis = new JsonInputStream(is)) {
        OpMetadataList opList = (OpMetadataList) jis.parseJsonAs(OpMetadataList.class);
        return opList;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return new OpMetadataList(); // empty default
    }
  }

  public static ReservationList getReservationListFromConfig() {
    // Load directly from classpath - no temporary files needed
    try (InputStream is = getResourceAsStream("reservations.json")) {
      if (is == null) {
        System.err.println("reservations.json not found in classpath");
        return new ReservationList(); // empty default
      }
      try (JsonInputStream jis = new JsonInputStream(is)) {
        ReservationList reservationList = (ReservationList) jis.parseJsonAs(ReservationList.class);
        return reservationList;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return new ReservationList(); // empty default
    }
  }

}
