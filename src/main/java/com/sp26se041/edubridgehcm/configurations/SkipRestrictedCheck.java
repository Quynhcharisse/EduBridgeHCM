package com.sp26se041.edubridgehcm.configurations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// this annotation will be checked in the RestrictedAccountFilter to bypass restriction
// use this for endpoints like login, logout, etc.
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipRestrictedCheck {
}

