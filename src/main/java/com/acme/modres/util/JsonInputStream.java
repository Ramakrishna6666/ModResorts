package com.acme.modres.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.Gson;

public class JsonInputStream extends FileInputStream {

  private File file;

  public JsonInputStream(File file) throws FileNotFoundException {
    super(file);
    this.file = file;
  }

  public Object parseJsonAs(Class<?> cls) {
    if (!file.exists()) {
      return null;
    }
    
    // Using try-with-resources for proper resource management
    try (JsonInputStream is = new JsonInputStream(file);
         BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      
      Gson gson = new Gson();
      return gson.fromJson(reader, cls);
      
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
