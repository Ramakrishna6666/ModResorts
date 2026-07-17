package com.acme.modres.mbean;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.acme.modres.mbean.reservation.ReservationList;
import com.acme.modres.util.JsonInputStream;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * blocker-3 (Local File System Write Operations), blocker-6 (Local Temporary Storage Reliance):
 * Replaced local temporary file creation (File.createTempFile) and FileOutputStream writes
 * with Amazon S3 object storage. Resources are read directly from S3 or classpath,
 * eliminating ephemeral local /tmp dependencies.
 */
public final class IOUtils {

  // S3 configuration from environment variables
  private static final String S3_BUCKET_NAME = System.getenv("S3_BUCKET_NAME") != null
      ? System.getenv("S3_BUCKET_NAME") : "modresorts-data";

  /**
   * Reads a resource by key from Amazon S3. Falls back to classpath if S3 is unavailable.
   * blocker-6: Eliminates reliance on ephemeral local temporary directories.
   * blocker-3: No local file write operations; data is read directly from S3.
   */
  public static byte[] getBytesFromS3OrClasspath(String resourceName) {
    // Try S3 first
    try {
      S3Client s3Client = S3Client.builder().build();
      try (ResponseInputStream<GetObjectResponse> s3Object =
               s3Client.getObject(GetObjectRequest.builder()
                   .bucket(S3_BUCKET_NAME)
                   .key("data/" + resourceName)
                   .build())) {
        return s3Object.readAllBytes();
      }
    } catch (NoSuchKeyException | IOException e) {
      // Fall back to classpath resource
    } catch (Exception e) {
      // Fall back to classpath resource
    }

    // Fallback: read from classpath
    try (InputStream classpathStream = IOUtils.class.getClassLoader().getResourceAsStream(resourceName)) {
      if (classpathStream != null) {
        return classpathStream.readAllBytes();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static OpMetadataList getOpListFromConfig() {
    byte[] data = getBytesFromS3OrClasspath("ops.json");
    if (data == null) return null;
    try (JsonInputStream is = new JsonInputStream(data)) {
      OpMetadataList opList = new OpMetadataList();
      opList = (OpMetadataList) is.parseJsonAs(OpMetadataList.class);
      return opList;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static ReservationList getReservationListFromConfig() {
    byte[] data = getBytesFromS3OrClasspath("reservations.json");
    if (data == null) return null;
    try (JsonInputStream is = new JsonInputStream(data)) {
      ReservationList reservationList = new ReservationList();
      reservationList = (ReservationList) is.parseJsonAs(ReservationList.class);
      return reservationList;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

}
