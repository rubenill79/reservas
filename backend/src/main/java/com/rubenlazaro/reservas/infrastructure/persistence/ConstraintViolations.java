package com.rubenlazaro.reservas.infrastructure.persistence;

/** Recognises which database constraint caused an integrity violation. */
final class ConstraintViolations {

    private ConstraintViolations() {
    }

    /**
     * PostgreSQL names the violated constraint, in double quotes, in its error message
     * (for example: conflicting key value violates exclusion constraint "booking_no_overlap").
     * The message sits at the end of the cause chain, so every level is checked.
     */
    static boolean isViolationOf(String constraintName, Throwable error) {
        String quoted = "\"" + constraintName + "\"";
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            String message = cause.getMessage();
            if (message != null && message.contains(quoted)) {
                return true;
            }
        }
        return false;
    }
}
