package com.campus360.iam.web.dto;

import com.campus360.iam.domain.User;

/**
 * Per-row result in a bulk create response.
 * {@code success=true} and {@code user} populated on success;
 * {@code success=false} and {@code error} populated on failure.
 */
public record BulkCreateUserResult(
        String email,
        boolean success,
        User user,
        String error) {

    public static BulkCreateUserResult ok(String email, User user) {
        return new BulkCreateUserResult(email, true, user, null);
    }

    public static BulkCreateUserResult fail(String email, String error) {
        return new BulkCreateUserResult(email, false, null, error);
    }
}
