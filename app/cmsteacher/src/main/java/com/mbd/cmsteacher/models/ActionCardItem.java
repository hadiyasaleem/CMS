package com.mbd.cmsteacher.models;

/**
 * ActionCardItem.java
 * Plain data model for a single faculty dashboard action card.
 * Used by ActionCardsAdapter in HomeFragment's 2-column grid.
 */
public class ActionCardItem {

    private final String title;
    private final String subtitle;
    private final int    iconResId;

    public ActionCardItem(String title, String subtitle, int iconResId) {
        this.title     = title;
        this.subtitle  = subtitle;
        this.iconResId = iconResId;
    }

    public String getTitle()     { return title;     }
    public String getSubtitle()  { return subtitle;  }
    public int    getIconResId() { return iconResId; }
}