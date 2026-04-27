package com.acme.modres.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;

/**
 * Cloud-ready JSON input stream that works with any InputStream.
 * No longer depends on File objects, making it compatible with classpath resources and S3 streams.
 */
public class JsonInputStream {

  private InputStream inputStream;

  public JsonInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public Object parseJsonAs(Class<?> cls) {
    Object jsonObject = null;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      Gson gson = new Gson();
      jsonObject = gson.fromJson(reader, cls);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return jsonObject;
  }

  public void close() throws IOException {
    if (inputStream != null) {
      inputStream.close();
    }
  }
}
