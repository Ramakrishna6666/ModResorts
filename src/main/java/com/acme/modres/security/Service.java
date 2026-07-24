package com.acme.modres.security;

public class Service {
  public static final String OPERATION = "my-operation";

  public void operation() {
    // SecurityManager has been deprecated for removal since Java 17 and removed in Java 21.
    // System.getSecurityManager() is no longer available in Java 21.
    // The SecurityManager-based security model has been replaced by other security mechanisms.
    // Removed: SecurityManager securityManager = System.getSecurityManager();
    System.out.println("Operation is executed");
  }
}
