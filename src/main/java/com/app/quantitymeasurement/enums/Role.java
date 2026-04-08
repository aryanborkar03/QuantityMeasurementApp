package com.app.quantitymeasurement.enums;


public enum Role {

    /**
     * Standard user role.
     * Granted to every account created via the local registration flow
     * or via Google OAuth2 login.
     */
    USER,

    /**
     * Administrator role.
     * Must be assigned manually in the database; it is never granted
     * automatically during registration or OAuth2 sign-in.
     */
    ADMIN
}
