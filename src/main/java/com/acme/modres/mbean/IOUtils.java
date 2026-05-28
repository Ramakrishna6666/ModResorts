package com.acme.modres.mbean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.acme.modres.mbean.reservation.ReservationList;
import com.acme.modres.util.JsonInputStream;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public final class IOUtils {

  // S3 configuration from environment variables
  private static final String S3_BUCKET_NAME = System.getenv().getOrDefault("S3_BUCKET_NAME", "modresorts-data");
  private static final String S3_REGION = System.getenv().getOrDefault("AWS_REGION", "us-east-1");
  private static final boolean USE_S3 = Boolean.parseBoolean(System.getenv().getOrDefault("USE_S3_STORAGE", "false"));

  /**
   * Get input stream from classpath resource or S3
   * This eliminates the need for temporary file creation
   */
  public static InputStream getInputStreamFromResource(String path) {
    InputStream stream = null;
    
    try {
      if (USE_S3) {
        // Load from S3
        S3Client s3Client = S3Client.builder()
            .region(software.amazon.awssdk.regions.Region.of(S3_REGION))
            .build();
        
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(S3_BUCKET_NAME)
            .key("config/" + path)
            .build();
        
        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
        
        // Read S3 content into byte array to return as ByteArrayInputStream
        byte[] content = s3Object.readAllBytes();
        stream = new ByteArrayInputStream(content);
        
        s3Object.close();
        s3Client.close();
      } else {
        // Load from classpath (default for local development)
        stream = IOUtils.class.getClassLoader().getResourceAsStream(path);
      }
    } catch (Exception e) {
      e.printStackTrace();
      // Fallback to classpath if S3 fails
      stream = IOUtils.class.getClassLoader().getResourceAsStream(path);
    }
    
    return stream;
  }

  public static OpMetadataList getOpListFromConfig() {
    try (InputStream stream = getInputStreamFromResource("ops.json");
         JsonInputStream is = new JsonInputStream(stream)) {
      OpMetadataList opList = new OpMetadataList(); // empty default
      opList = (OpMetadataList) is.parseJsonAs(OpMetadataList.class);
      return opList;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static ReservationList getReservationListFromConfig() {
    try (InputStream stream = getInputStreamFromResource("reservations.json");
         JsonInputStream is = new JsonInputStream(stream)) {
      ReservationList reservationList = new ReservationList(); // empty default
      reservationList = (ReservationList) is.parseJsonAs(ReservationList.class);
      return reservationList;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

}
