package com.campus360.testsupport;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCampusUserSecurityContextFactory.class)
public @interface WithMockCampusUser {

    long userId() default 1L;
    long tenantId() default 1L;
    String email() default "test@campus360.com";
    String role() default "INSTITUTION_ADMIN";
}
