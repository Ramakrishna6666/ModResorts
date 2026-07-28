package com.acme.modres.util;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ZipValidator extends ZipFile {

  private File file;

  public ZipValidator(File file) throws ZipException, IOException {
    super(file);
    this.file = file;
  }

  public boolean isValid() throws IOException {
    if (!file.exists()) {
      return false;
    }
    
    // Using try-with-resources to ensure proper closure
    try (ZipValidator zipFile = new ZipValidator(file)) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      return !entries.hasMoreElements();
    } catch (ZipException e) {
      // Invalid zip file
      return false;
    }
  }
}
