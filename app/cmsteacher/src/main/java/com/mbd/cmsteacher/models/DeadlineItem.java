package com.mbd.cmsteacher.models;

/**
 * DeadlineItem.java
 * Plain data model for a single "Academic Deadlines" row
 * in HomeFragment's sidebar RecyclerView.
 *
 * urgency: "HIGH" → red/secondary left border
 *          "MED"  → gold/accent left border
 *          "LOW"  → grey/outline left border
 */
public class DeadlineItem {

    public static final String URGENCY_HIGH = "HIGH";
    public static final String URGENCY_MED  = "MED";
    public static final String URGENCY_LOW  = "LOW";

    private final String title;
    private final String subtitle;   // e.g. "Batch 2024 • Architecture"
    private final String dueLabel;   // e.g. "Due in 4 Hours"
    private final String urgency;    // HIGH / MED / LOW
    private final int    iconResId;  // drawable resource for the leading icon

    public DeadlineItem(String title, String subtitle,
                        String dueLabel, String urgency, int iconResId) {
        this.title     = title;
        this.subtitle  = subtitle;
        this.dueLabel  = dueLabel;
        this.urgency   = urgency;
        this.iconResId = iconResId;
    }

    public String getTitle()     { return title;     }
    public String getSubtitle()  { return subtitle;  }
    public String getDueLabel()  { return dueLabel;  }
    public String getUrgency()   { return urgency;   }
    public int    getIconResId() { return iconResId; }
}