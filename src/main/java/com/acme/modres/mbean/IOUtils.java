package com.acme.modres.mbean;

import java.io.IOException;
import java.io.InputStream;

import com.acme.modres.mbean.reservation.ReservationList;
import com.acme.modres.util.JsonInputStream;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public final class IOUtils {

  // S3 configuration from environment variables
  private static final String S3_BUCKET_NAME = System.getenv().getOrDefault("S3_BUCKET_NAME", "modresorts-data");
  private static final String AWS_REGION = System.getenv().getOrDefault("AWS_REGION", "us-east-1");
  private static final boolean USE_S3 = Boolean.parseBoolean(System.getenv().getOrDefault("USE_S3_STORAGE", "false"));

  /**
   * Get input stream from classpath resource or S3 based on configuration
   */
  public static InputStream getInputStreamFromPath(String path) {
    if (USE_S3) {
      return getInputStreamFromS3(path);
    } else {
      return IOUtils.class.getClassLoader().getResourceAsStream(path);
    }
  }

  /**
   * Get input stream from S3
   */
  private static InputStream getInputStreamFromS3(String s3Key) {
    try {
      S3Client s3Client = S3Client.builder()
          .region(Region.of(AWS_REGION))
          .build();
      
      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(S3_BUCKET_NAME)
          .key(s3Key)
          .build();
      
      ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
      return s3Object;
      
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static OpMetadataList getOpListFromConfig() {
    // Use classpath resource instead of temporary file
    try (InputStream is = getInputStreamFromPath("ops.json")) {
      if (is == null) {
        System.err.println("ops.json not found in classpath or S3");
        return new OpMetadataList(); // empty default
      }
      
      try (JsonInputStream jsonIs = new JsonInputStream(is)) {
        OpMetadataList opList = (OpMetadataList) jsonIs.parseJsonAs(OpMetadataList.class);
        return opList;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return new OpMetadataList(); // empty default
    }
  }

  public static ReservationList getReservationListFromConfig() {
    // Use classpath resource instead of temporary file
    try (InputStream is = getInputStreamFromPath("reservations.json")) {
      if (is == null) {
        System.err.println("reservations.json not found in classpath or S3");
        return new ReservationList(); // empty default
      }
      
      try (JsonInputStream jsonIs = new JsonInputStream(is)) {
        ReservationList reservationList = (ReservationList) jsonIs.parseJsonAs(ReservationList.class);
        return reservationList;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return new ReservationList(); // empty default
    }
  }

}
