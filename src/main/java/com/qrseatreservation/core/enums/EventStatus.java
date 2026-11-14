package com.qrseatreservation.core.enums;

/**
 * Lifecycle of a ticket campaign. Several events can be ACTIVE at the same time.
 * DRAFT events can be prepared by admins; ARCHIVED events are read-only.
 */
public enum EventStatus {
    DRAFT, ACTIVE, ARCHIVED
}
