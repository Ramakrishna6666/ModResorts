package com.acme.modres.security;

public class Service {
  public static final String OPERATION = "my-operation";

  public void operation() {
    // SecurityManager is deprecated and removed in Java 21
    // Replaced with modern security approach or removed if not critical
    // If security checks are needed, use SecurityManager alternatives or 
    // application-level security frameworks
    
    // Original code commented out:
    // SecurityManager securityManager = System.getSecurityManager();
    // if (securityManager != null) {
    //   securityManager.checkMemberAccess(Service.class, Member.PUBLIC);
    // }
    
    System.out.println("Operation is executed");
  }
}
