---
name: google-stitch-prompt
description: Generate a complete, screen-by-screen Google Stitch UI-generation prompt for an app or feature. Trigger whenever the user asks for a "google stitch prompt", "stitch prompt", "stitch UI prompt", a prompt to feed Google Stitch, or a full screen list to generate a UI. Produces a grouped, numbered breakdown of every screen/sheet/dialog/notification plus a summary table and total count, in the exact house format below.
---

# Google Stitch Prompt Generator

When the user asks for a Google Stitch prompt, produce a **complete, exhaustive list of every screen, bottom sheet, dialog, and notification** for the app or feature they name — formatted exactly like the template below. The output is meant to be pasted straight into Google Stitch, so it must be self-contained and cover the full user journey (100% of screens), never a partial sketch.

**Target platform: mobile app designs.** These prompts generate **native mobile (phone) app** screens — so state it explicitly at the top of the output ("… to generate as **mobile app designs** in Google Stitch"), and design for mobile throughout: a phone-sized portrait viewport, bottom navigation (not a sidebar), FABs, bottom sheets/modals rather than side panels, thumb-reachable actions, and responsive data tables that scroll horizontally on a narrow screen with a frozen first column. Only design for tablet/desktop/web if the user explicitly asks.

## How to build it

1. **Scope it fully.** List every distinct UI surface — primary screens, modal bottom sheets, confirmation dialogs, empty/loading states worth noting, and even system-UI mockups (e.g. notifications). Don't collapse variants that a designer would need to see separately. Aim for total coverage of the journey, not a highlight reel.
2. **Group by flow**, each group led by an emoji + short section title (e.g. `🔐 Auth Flow`, `🏠 Main App`, `📅 Calendar`, `⚙️ Settings`). If the app has a bottom nav, state the tabs on the Main App group line: `🏠 Main App (Bottom Nav: Home · Calendar · Qaza · Stats · Settings)`.
3. **Number screens sequentially across the whole document** (1, 2, 3, …) — numbering does not restart per group.
4. For **each screen**: bold the screen name on its own line, then a concise description of the layout top-to-bottom and its key components/states. For dense screens, use a short bulleted list of the main regions. Name concrete components (cards, chips, FAB, toggles, dropdowns, charts, status indicators, avatars) and any color-coded semantics.
5. **Modals/sheets/dialogs**: label what triggers them ("Triggered by tapping any prayer card…", "Modal from Bulk Update") and describe their content and actions.
6. **Non-screens** (notifications, widgets): include them as mockups and say so explicitly ("Not a screen, but design it as a notification mockup: …").
7. End with a **Summary Table** — columns `# | Screen | Nav Location` — one row per numbered item, then a closing one-line count: "That's N screens/components covering the full user journey".
8. Keep the voice terse and design-brief-like — fragments over full sentences, present tense, no filler.

### Applying an existing design system
If the app already has a design language (e.g. this repo's "Academic Ledger" navy/gold/crimson identity in `design.md`), fold it in: name the palette, typography, and signature components in the descriptions so Stitch renders on-brand. If no design system is given and the user hasn't specified one, keep descriptions style-neutral (layout + components only) unless they ask for a specific aesthetic — don't invent a brand.

### Before generating
If the user names an app you don't have details for, make reasonable, conventional assumptions about its screens and state them briefly — don't stop to interrogate. Only ask a clarifying question if the app's core purpose is genuinely ambiguous. Prefer over-covering (more screens) to under-covering.

## Output format template

Reproduce this structure exactly (emoji section headers, bold screen names, sequential numbering, trailing summary table + count):

```
Here's a complete list of all screens for the <App Name> app to generate as mobile app designs in Google Stitch:

🔐 <Section Title>
1. <Screen Name>
<One-line-or-bulleted description of layout + components + states.>

2. <Screen Name>
<Description.>

🏠 Main App (Bottom Nav: <Tab> · <Tab> · <Tab> · <Tab> · <Tab>)
3. <Screen Name>
- <Region / component 1>
- <Region / component 2>
- <Key states or color semantics>

4. <Modal / Bottom Sheet Name>
Triggered by <trigger>. <Content + selectable options + actions.>

… (continue numbering across all groups: Calendar, feature areas, Settings, etc.)

🔔 <Section Title>
N. <Notification / System-UI Mockup Name>
Not a screen, but design it as a <mockup type>: <elements + quick actions>.

📋 Summary Table
# | Screen | Nav Location
1 | <Screen> | <Auth flow / Bottom nav / Modal from X / System UI>
2 | <Screen> | <…>
… | … | …
N | <Screen> | <…>

That's N screens/components covering the full user journey.
```

## Reference example (the house style — match this exactly)

The following is a gold-standard example of the expected output for a "Salat Tracker" app. Match its grouping, numbering, density, trigger annotations, notification-mockup handling, and the closing summary table + count.

```
Here's a complete list of all screens for the Salat Tracker app to generate in Google Stitch:

🔐 Auth Flow
1. Splash Screen
App logo centered on screen, brief loading indicator, auto-navigates to Sign-In or Home.

2. Sign-In Screen
Google Sign-In button prominently centered, app name/logo above, minimal background with Islamic geometric pattern accent.

🏠 Main App (Bottom Nav: Home · Calendar · Qaza · Stats · Settings)
3. Home Screen (Today)
- Today's date + Hijri date at top
- Current streak badge + highest streak
- 5 prayer cards (Fajr, Dhuhr, Asr, Maghrib, Isha) each showing name, time, and current status chip
- FAB or button to go to Bulk Update

4. Prayer Status Bottom Sheet
Triggered by tapping any prayer card on Home or Calendar detail. Shows prayer name + date, then 5 selectable status options: Congregation / Individual / Qaza Pending / Qaza Completed / Menstruation — each as a tappable chip or radio row.

📅 Calendar
5. Calendar Screen
- Month grid view
- Each day cell has colored dot indicators (green = all complete, yellow = partial, red = qaza, purple = menstruation, grey = not recorded)
- Month navigation arrows
- Tapping a day opens Day Detail

6. Day Detail Bottom Sheet
Shows all 5 prayers for a selected past date with their statuses, each editable via the Prayer Status Bottom Sheet.

🕌 Qaza
7. Qaza Screen
- Summary cards at top: total pending Qaza count per prayer type (Fajr: 3, Dhuhr: 7, etc.)
- Overall backlog number
- List of pending Qaza entries with quick-complete action buttons
- Option to mark completed

📊 Analytics / Dashboard
8. Analytics Screen
- Period toggle: Week / Month / Year
- Metrics cards: Congregation %, Individual %, Missed/Qaza count
- Streak chart (line or bar)
- Prayer-wise breakdown bar chart (one bar per prayer type)

✏️ Bulk Update
9. Bulk Update Screen
- Range selector tabs: Day / Week / Month / Custom
- Custom date range picker (start–end date)
- Prayer type multi-select checkboxes (Fajr, Dhuhr, Asr, Maghrib, Isha)
- Status selector (same 5 statuses)
- Confirmation button with summary ("Apply CONGREGATION to all 5 prayers for 7 days")

10. Bulk Update Confirmation Dialog
Modal overlay summarizing the operation with Cancel / Confirm actions.

⚙️ Settings
11. Settings Screen
- Location section: latitude/longitude fields or "Use Device Location" toggle
- Calculation Method dropdown (AlAdhan methods)
- Per-prayer reminder toggles with offset minutes input
- Account section: signed-in user avatar + email, Sign Out button

🔔 Notification
12. Prayer Reminder Notification (System UI)
Not a screen, but design it as a notification mockup: app icon, prayer name ("Asr Prayer Time"), time, and a "Mark as Prayed" quick action button.

📋 Summary Table
# | Screen | Nav Location
1 | Splash | Auth flow
2 | Sign-In | Auth flow
3 | Home / Today | Bottom nav
4 | Prayer Status Bottom Sheet | Modal from Home/Calendar
5 | Calendar | Bottom nav
6 | Day Detail Bottom Sheet | Modal from Calendar
7 | Qaza | Bottom nav
8 | Analytics Dashboard | Bottom nav
9 | Bulk Update | From Home/Calendar
10 | Bulk Update Confirmation Dialog | Modal from Bulk Update
11 | Settings | Bottom nav
12 | Notification Mockup | System UI

That's 12 screens/components covering the full user journey.
```
