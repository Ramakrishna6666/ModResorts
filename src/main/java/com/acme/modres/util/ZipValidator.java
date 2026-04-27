package com.acme.modres.util;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ZipValidator {

  private File file;

  public ZipValidator(File file) {
    this.file = file;
  }

  public boolean isValid() throws IOException {
    if (file.exists()) {
      // Use try-with-resources to ensure proper resource management
      try (ZipFile zipFile = new ZipFile(file)) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        if (!entries.hasMoreElements()) {
          return true;
        }
        // ZipFile is automatically closed by try-with-resources
        return true;
      } catch (ZipException e) {
        // Invalid zip file
        return false;
      }
    }
    return false;
  }

}
