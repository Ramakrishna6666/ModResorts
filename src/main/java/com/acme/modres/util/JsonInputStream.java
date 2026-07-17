package com.acme.modres.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;

/**
 * Updated to support byte array input in addition to File input,
 * enabling cloud-native usage without local file system dependencies.
 */
public class JsonInputStream extends FileInputStream {

  private File file;
  private byte[] data;
  private boolean useByteArray;

  public JsonInputStream(File file) throws FileNotFoundException {
    super(file);
    this.file = file;
    this.useByteArray = false;
  }

  /**
   * Constructor accepting byte array data directly (no local file required).
   * Uses a temporary placeholder file reference but reads from the byte array.
   */
  public JsonInputStream(byte[] data) throws IOException {
    // We need a valid FileInputStream for the super constructor.
    // Use /dev/null as a placeholder; actual reading is done from byte array.
    super(new File("/dev/null"));
    this.data = data;
    this.useByteArray = true;
  }

  public Object parseJsonAs(Class<?> cls) {
    if (useByteArray) {
      if (data == null) return null;
      try (InputStream is = new ByteArrayInputStream(data);
           BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
        Gson gson = new Gson();
        return gson.fromJson(reader, cls);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    if (file != null && file.exists()) {
      JsonInputStream is = null;
      Object jsonObject = null;
      try {
        is = new JsonInputStream(file);
        Gson gson = new Gson();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        jsonObject = gson.fromJson(reader, cls);
      } catch (Exception e) {
        e.printStackTrace();
      } catch (Throwable e) {
        e.printStackTrace();
      } finally {
        if (is != null) {
          try {
            is.close();
            is.read(); // test if file is closed
          } catch (IOException e) {
            // closed successfully
            return jsonObject;
          } catch (Throwable e) {
            e.printStackTrace();
          }
        }
      }
    }
    return null;
  }

}
