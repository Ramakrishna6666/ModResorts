package com.acme.modres.security;

/**
 * Service class updated for Java 21 compatibility.
 * SecurityManager has been deprecated for removal since Java 17 (JEP 411).
 * Removed SecurityManager usage as it is no longer supported in Java 21.
 * (Rule: JAVA11_TO_17_DEPRECATED_FEATURES)
 */
public class Service {
  public static final String OPERATION = "my-operation";

  public void operation() {
    // SecurityManager was deprecated for removal in Java 17 (JEP 411) and
    // calling System.getSecurityManager() always returns null in Java 21.
    // Removed SecurityManager check - no longer applicable in Java 21.
    System.out.println("Operation is executed");
  }
}
