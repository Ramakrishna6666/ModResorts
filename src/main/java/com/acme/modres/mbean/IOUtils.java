package com.acme.modres.mbean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.acme.modres.mbean.reservation.ReservationList;
import com.acme.modres.service.AzureBlobStorageService;
import com.acme.modres.util.JsonInputStream;

/**
 * Cloud-native IO utilities using Azure Blob Storage
 * Replaces local file system dependencies with cloud storage
 */
public final class IOUtils {

  private static AzureBlobStorageService blobStorageService;
  
  /**
   * Set the Azure Blob Storage Service (injected by Spring)
   */
  public static void setBlobStorageService(AzureBlobStorageService service) {
    blobStorageService = service;
  }

  /**
   * Get resource from Azure Blob Storage or classpath as fallback
   * Replaces File.createTempFile with cloud-native storage
   */
  public static InputStream getResourceStream(String path) {
    InputStream stream = null;
    
    // Try Azure Blob Storage first
    if (blobStorageService != null && blobStorageService.isAvailable()) {
      try {
        byte[] data = blobStorageService.downloadBlob(path);
        if (data != null) {
          return new ByteArrayInputStream(data);
        }
      } catch (Exception e) {
        // Fall through to classpath resource
      }
    }
    
    // Fallback to classpath resource
    try {
      stream = IOUtils.class.getClassLoader().getResourceAsStream(path);
      if (stream == null) {
        throw new IOException("Resource not found: " + path);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return stream;
  }

  /**
   * Upload data to Azure Blob Storage
   * Replaces local file write operations
   */
  public static void uploadResource(String path, byte[] data) {
    if (blobStorageService != null && blobStorageService.isAvailable()) {
      try {
        blobStorageService.uploadBlob(path, data);
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to upload resource to Azure Blob Storage: " + path, e);
      }
    } else {
      throw new RuntimeException("Azure Blob Storage not available. Cannot upload: " + path);
    }
  }

  public static OpMetadataList getOpListFromConfig() {
    try (InputStream stream = getResourceStream("ops.json")) {
      if (stream == null) {
        return new OpMetadataList(); // empty default
      }
      
      // Read stream into byte array for JsonInputStream
      byte[] buffer = new byte[stream.available()];
      stream.read(buffer);
      
      try (JsonInputStream is = new JsonInputStream(new ByteArrayInputStream(buffer))) {
        OpMetadataList opList = (OpMetadataList) is.parseJsonAs(OpMetadataList.class);
        return opList;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return new OpMetadataList(); // empty default
    }
  }

  public static ReservationList getReservationListFromConfig() {
    try (InputStream stream = getResourceStream("reservations.json")) {
      if (stream == null) {
        return new ReservationList(); // empty default
      }
      
      // Read stream into byte array for JsonInputStream
      byte[] buffer = new byte[stream.available()];
      stream.read(buffer);
      
      try (JsonInputStream is = new JsonInputStream(new ByteArrayInputStream(buffer))) {
        ReservationList reservationList = (ReservationList) is.parseJsonAs(ReservationList.class);
        return reservationList;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return new ReservationList(); // empty default
    }
  }
}
