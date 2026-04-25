package com.mbd.cmsadmin.models;

/**
 * AlertItem.java
 * Plain data model for a single "Recent Academic Alert" row.
 * Replace the fake constructor calls in HomeFragment with Firestore documents later.
 */
public class AlertItem {

    private final String title;
    private final String body;
    private final String meta;   // e.g. "POSTED 2 HOURS AGO • REGISTRAR OFFICE"

    public AlertItem(String title, String body, String meta) {
        this.title = title;
        this.body  = body;
        this.meta  = meta;
    }

    public String getTitle() { return title; }
    public String getBody()  { return body;  }
    public String getMeta()  { return meta;  }
}