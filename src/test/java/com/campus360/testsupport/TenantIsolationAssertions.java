package com.campus360.testsupport;

import com.campus360.platform.error.ApiException;
import org.junit.jupiter.api.Assertions;

public class TenantIsolationAssertions {

    public static void assertThrowsNotFoundOrForbidden(Runnable action) {
        try {
            action.run();
            Assertions.fail("Expected an exception preventing access to another tenant's data");
        } catch (ApiException e) {
            Assertions.assertTrue(
                e.getStatus().value() == 404 || e.getStatus().value() == 403,
                "Expected 404 Not Found or 403 Forbidden, got " + e.getStatus()
            );
        } catch (Exception e) {
            Assertions.fail("Expected ApiException but got: " + e.getClass().getName());
        }
    }
}
