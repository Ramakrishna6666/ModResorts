package com.acme.modres.security;

import java.util.logging.Logger;

public class Service {
  private static final Logger logger = Logger.getLogger(Service.class.getName());
  public static final String OPERATION = "my-operation";

  public void operation() {
    // SecurityManager has been deprecated for removal in Java 17 and removed in Java 21
    // Removed SecurityManager usage as it's no longer available
    // If security checks are needed, implement using modern security frameworks
    // such as Spring Security or Jakarta Security
    
    logger.info("Operation is executed");
    System.out.println("Operation is executed");
  }
}
