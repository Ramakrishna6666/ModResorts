package com.acme.modres.security;

public class Service {
  public static final String OPERATION = "my-operation";

  public void operation() {
    // SecurityManager is deprecated for removal in Java 21
    // Removed SecurityManager usage as it's no longer recommended
    // Modern applications should use other security mechanisms like
    // Spring Security, Jakarta Security, or custom authorization frameworks
    System.out.println("Operation is executed");
  }
}
