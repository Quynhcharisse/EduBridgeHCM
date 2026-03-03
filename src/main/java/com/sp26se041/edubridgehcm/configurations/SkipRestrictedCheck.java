package com.sp26se041.edubridgehcm.configurations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// this annotation is used by RestrictedAccountAspect to bypass restricted-account checks
// use this for endpoints like login, logout, etc.
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipRestrictedCheck {
}

