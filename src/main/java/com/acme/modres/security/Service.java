package com.acme.modres.security;

public class Service {
  public static final String OPERATION = "my-operation";

  public void operation() {
    // SecurityManager is deprecated for removal in Java 21
    // Removed SecurityManager usage as it's no longer recommended
    // Security checks should be implemented using modern security frameworks
    System.out.println("Operation is executed");
  }
}
