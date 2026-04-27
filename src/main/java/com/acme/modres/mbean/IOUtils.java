package com.acme.modres.mbean;

import java.io.IOException;
import java.io.InputStream;

import com.acme.modres.mbean.reservation.ReservationList;
import com.acme.modres.util.JsonInputStream;

/**
 * Cloud-ready utility for loading configuration files.
 * Uses classpath resources instead of temporary file system operations.
 */
public final class IOUtils {

  /**
   * Load resource directly from classpath without creating temporary files.
   * This is cloud-native and works in containerized environments.
   */
  public static InputStream getResourceAsStream(String path) {
    InputStream stream = IOUtils.class.getClassLoader().getResourceAsStream(path);
    if (stream == null) {
      throw new RuntimeException("Resource not found: " + path);
    }
    return stream;
  }

  public static OpMetadataList getOpListFromConfig() {
    try (InputStream is = getResourceAsStream("ops.json")) {
      JsonInputStream jsonStream = new JsonInputStream(is);
      OpMetadataList opList = (OpMetadataList) jsonStream.parseJsonAs(OpMetadataList.class);
      return opList;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static ReservationList getReservationListFromConfig() {
    try (InputStream is = getResourceAsStream("reservations.json")) {
      JsonInputStream jsonStream = new JsonInputStream(is);
      ReservationList reservationList = (ReservationList) jsonStream.parseJsonAs(ReservationList.class);
      return reservationList;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

}
