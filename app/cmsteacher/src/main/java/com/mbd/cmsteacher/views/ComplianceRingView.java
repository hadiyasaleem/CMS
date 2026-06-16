package com.mbd.cmsteacher.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.mbd.cmsteacher.R;

/**
 * ComplianceRingView
 * ─────────────────────────────────────────────────────────────────────
 * Custom View that draws the 90% compliance arc ring from the HTML ref 1
 * "Faculty Integrity Standards" card.
 *
 * Renders:
 *   • Light grey track circle (full 360°)
 *   • Accent-gold progress arc  (sweepAngle = percentage × 3.6)
 *   • Centre label: large "90%" + small "COMPLIANCE" caption
 *
 * Attributes (all optional, set programmatically):
 *   setPercentage(float)  — 0–100, default 90
 *   setTrackColor(int)
 *   setProgressColor(int)
 *   setTextColor(int)
 *
 * Usage in XML:
 *   <com.mbd.cmsteacher.views.ComplianceRingView
 *       android:id="@+id/compliance_ring"
 *       android:layout_width="120dp"
 *       android:layout_height="120dp" />
 * ─────────────────────────────────────────────────────────────────────
 */
public class ComplianceRingView extends View {

    // ── Configurable state ────────────────────────────────────────────
    private float percentage    = 90f;
    private int   trackColor    = 0xFFE2E2E2;   // surface_variant
    private int   progressColor = 0xFFB18000;   // accent_gold
    private int   labelColor    = 0xFF002147;   // scholar_navy
    private int   captionColor  = 0xFF44474D;   // on_surface_variant

    // ── Paint objects ─────────────────────────────────────────────────
    private final Paint trackPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint captionPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ── Drawing primitives ────────────────────────────────────────────
    private final RectF arcBounds = new RectF();
    private float strokeWidth;

    // ─────────────────────────────────────────────────────────────────

    public ComplianceRingView(Context context) {
        super(context);
        init(context);
    }

    public ComplianceRingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ComplianceRingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    // ── Initialisation ────────────────────────────────────────────────

    private void init(Context context) {
        // Track paint — thin grey ring
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setColor(trackColor);

        // Progress paint — gold arc, round caps
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(progressColor);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        // Large percentage label — Newsreader Bold if available
        labelPaint.setStyle(Paint.Style.FILL);
        labelPaint.setColor(labelColor);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        try {
            Typeface newsreader = ResourcesCompat.getFont(context, R.font.newsreader_bold);
            if (newsreader != null) labelPaint.setTypeface(newsreader);
        } catch (Exception ignored) {
            labelPaint.setTypeface(Typeface.DEFAULT_BOLD);
        }

        // Caption text — Public Sans regular
        captionPaint.setStyle(Paint.Style.FILL);
        captionPaint.setColor(captionColor);
        captionPaint.setTextAlign(Paint.Align.CENTER);
        try {
            Typeface publicSans = ResourcesCompat.getFont(context, R.font.public_sans);
            if (publicSans != null) captionPaint.setTypeface(publicSans);
        } catch (Exception ignored) { /* default */ }
    }

    // ── Drawing ───────────────────────────────────────────────────────

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        // Stroke width = 6% of the smaller dimension (matches HTML stroke-width="6" on r=42 of 100)
        strokeWidth = Math.min(w, h) * 0.06f;

        float halfStroke = strokeWidth / 2f;
        float padding    = halfStroke + getPaddingLeft();

        arcBounds.set(padding, padding, w - padding, h - padding);

        trackPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeWidth(strokeWidth);

        // Scale text sizes relative to view size
        labelPaint.setTextSize(Math.min(w, h) * 0.22f);
        captionPaint.setTextSize(Math.min(w, h) * 0.09f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth()  / 2f;
        float cy = getHeight() / 2f;

        // ── Track (full circle) ───────────────────────────────────────
        trackPaint.setColor(trackColor);
        canvas.drawOval(arcBounds, trackPaint);

        // ── Progress arc ──────────────────────────────────────────────
        // Start at top (-90°); sweep clockwise by (percentage / 100 × 360)
        float sweep = (percentage / 100f) * 360f;
        progressPaint.setColor(progressColor);
        canvas.drawArc(arcBounds, -90f, sweep, false, progressPaint);

        // ── Centre label ──────────────────────────────────────────────
        // Percentage text
        String pctText = (int) percentage + "%";
        float textOffset = (labelPaint.descent() + labelPaint.ascent()) / 2f;
        canvas.drawText(pctText, cx, cy - textOffset - captionPaint.getTextSize() * 0.6f, labelPaint);

        // Caption
        String caption = "COMPLIANCE";
        canvas.drawText(caption, cx, cy + Math.abs(labelPaint.ascent()) * 0.4f, captionPaint);
    }

    // ── Public API ────────────────────────────────────────────────────

    /** Set the compliance percentage (0–100). Triggers a redraw. */
    public void setPercentage(float percentage) {
        this.percentage = Math.max(0f, Math.min(100f, percentage));
        invalidate();
    }

    public float getPercentage() { return percentage; }

    public void setTrackColor(int color) {
        this.trackColor = color;
        invalidate();
    }

    public void setProgressColor(int color) {
        this.progressColor = color;
        invalidate();
    }

    public void setTextColor(int color) {
        this.labelColor = color;
        labelPaint.setColor(color);
        invalidate();
    }
}