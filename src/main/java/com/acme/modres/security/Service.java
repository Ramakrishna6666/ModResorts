package com.acme.modres.security;

public class Service {
  public static final String OPERATION = "my-operation";

  public void operation() {
    // SecurityManager is deprecated for removal in Java 17+
    // Removed SecurityManager usage as it's no longer recommended
    // Modern applications should use alternative security mechanisms like:
    // - Spring Security
    // - Java Security Manager alternatives
    // - Custom permission frameworks
    
    System.out.println("Operation is executed");
  }
}
