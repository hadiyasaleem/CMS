# Timetable import draft — NOT yet inserted, for review only

Source PDFs (effective date will be **2026-09-27**, today, not the 05/10/2026 printed on the PDFs, per your instruction):
- TIME TABLE BS 1ST SEMESTER 2026-2030 MORNING
- TIME TABLE BS 3rd SEMESTER 2025-2029 MORNING
- TIME TABLE BS 5th SEMESTER 2024-2028 MORNING
- TIME TABLE BS 7TH SEMESTER 2023-2027 MORNING
- TIME TABLE-01-10-25-Evening (BBA/English/IT evening, semesters 1/3/5/7 + Islamic Studies & English "intake" evening 5th/7th)

Day pattern (per the PDFs' own note):
- **1st & 5th semester (morning)**: Monday, Tuesday, Wednesday — same subjects meet all 3 days at the same period.
- **3rd & 7th semester (morning)**: Thursday, Friday, Saturday — same subjects meet all 3 days at the same period.
- **Evening (all semesters)**: Monday to Thursday, but split into two 2-day patterns — **Mon+Tue** carry one subject in a slot, **Wed+Thu** carry a *different* subject in the same slot. So each evening subject only meets twice a week.

Morning period times (used for every morning grid below):
| Period | Time |
|---|---|
| 1 | 08:00–08:40 |
| 2 | 08:40–09:20 |
| 3 | 09:20–10:00 |
| 4 | 10:00–10:40 |
| BREAK | 10:40–11:00 (not stored) |
| 5 | 11:00–11:40 |
| 6 | 11:40–12:20 |
| 7 | 12:20–13:00 |

Evening period times:
| Period | Time |
|---|---|
| 0 (zero) | 12:20–13:00 (all blank in the PDFs — skipped) |
| 1 | 13:00–13:40 |
| 2 | 13:40–15:05 |
| 3 | 15:05–16:30 |
| 4 | 16:30–17:55 |

---

## ⚠️ Open issues to resolve before I generate INSERTs

1. **Room R#34 (Zoology, "Hostel Block") does not exist in the `rooms` table.** Rooms table stops at R#33 for Hostel Block. Zoology's morning periods (1st/3rd/5th/7th) all use R#34. Do you want me to add it, or is this a different, unrecorded building?
2. **Duplicate/near-duplicate teacher rows** in `teachers` — I can't tell which email is "the" one to use for a name printed on the timetable:
   - "Azhar Iqbal" → `azhar.iqbal@ggcmbdin.edu.pk` **and** `azhar@ggcmbdin.edu.pk`
   - "Adnan Sagheer/Saghir" → `adnan.s@ggcmbdin.edu.pk`, `adnan.saghir@ggcmbd.edu.pk`, and separately `m.adnan@ggcmbdin.edu.pk` ("Muhammad Adnan") — the PDFs use both "Adnan Saghir" and "Prof. Ubaid" and plain "Adnan sb" in different places, so this needs your call per row.
   - "Iqra Noor" → `iqra.noor@ggcmbdin.edu.pk` **and** `iqranoor@ggcmbdin.edu.pk`
   - "Jawad Haider" → `jawad.haider@ggcmbd.edu.pk` **and** `jawad@ggcmbdin.edu.pk`
   Please tell me which one is canonical for each (or if one of each pair should be deleted).
3. Some cells in the PDFs are genuinely **merged lectures** shared across two sections (e.g. Chemistry 1st's "Functional English" merged with Urdu 1st in R#18; Math 1st's "Functional English" merged with Zoology 1st in R#34; Pol.Science/Urdu 1st share GCCE-101/GISL-101 in R#16). These need `period_sessions` rows linking both sessions to one `timetable_periods` row instead of two separate rows. I'll do that once room/teacher questions above are settled.
4. Several teacher names on the PDFs are only a surname/nickname ("Prof. Asad sb", "Miss Sana") with no department context — I matched these by name against the 50 teachers already loaded, but a few are genuinely ambiguous and I've left them blank below (marked `?`) rather than guess wrong.
5. One PDF apparently has a **typo in the row header** ("MATH-401" used twice in 7th-semester Math for two different subjects — Theory of Approximation & Splines, and Set Theory). Your `session_subjects` table already has this fixed as `MATH-401` and `MATH-404` — I'll use the DB's course codes, not the PDF's, wherever they conflict.

---

## Session → semester → shift map (for your sanity check)

| Session | Dept | Semester now | Shift(s) |
|---|---|---|---|
| chem_2026 / eng_2026 / isl_2026 / it_2026 / math_2026 / pol_2026 / urdu_2026 / zoo_2026 | — | 1 | eng/isl/it/pol/urdu/zoo = BOTH (morning+evening cohorts), chem/math = MORNING only |
| eng_2025 / isl_2025 / it_2025 / math_2025 / pol_2025 / urdu_2025 / zoo_2025 | — | 3 | same pattern (no Chemistry 3rd-sem grid was printed) |
| chem_2024 / eng_2024 / isl_2024 / it_2024 / phy_2024 / math_2024 / pol_2024 / urdu_2024 / zoo_2024 | — | 5 | phy = MORNING only, others as above |
| chem_2023 / eng_2023 / isl_2023 / it_2023 / math_2023 / pol_2023 / zoo_2023 | — | 7 | no Urdu 7th or Physics 7th grid was printed |
| bba_2026 / bba_2025 / bba_2024 / bba_2023 | BBA | 1/3/5/7 | EVENING only |

Evening also carries "Intake" sections layered onto **isl_2026 (5th intake, printed as its own untitled block)** and **eng (7th intake / 5th intake "English(Intake)")** — the PDF labels these oddly ("TIME TABLE B.S 5TH INTAKE SEMESTER EVENING (2026-2028)" then an untitled table, then "7th INTAKE SEMESTER EVENING (2025-2027)"). I need you to confirm which `academic_sessions` row these "Intake" cohorts belong to — they don't obviously match the 2023/2024/2025/2026 sessions already loaded (the labelled years 2026-2028 and 2025-2027 don't line up with isl/eng's existing start_years). **Please clarify this one specifically — I don't want to guess and create a duplicate/wrong session.**

---

## Draft content (subject / teacher / room, in PDF reading order — day/time expansion happens after you approve this layer)

### MORNING — 1st Semester (chem/eng/isl/it/math/pol/urdu/zoo _2026), Mon/Tue/Wed, all 3 days identical

**Chemistry** (chem_2026) — room R#29 (Hostel Block)
1. Chem-116 & 117L — Fundamental Concepts of Chemical Bonding + Lab — teacher `?`
2. NSW-106 — Science Wonders Around Us (NS) — teacher `?`
3. Chem-101 & 102L — Physical Chemistry + Lab — teacher `?`
4. GISL-101 — Islamic Studies Compulsory — teacher `?`
5. GENG-101 — Functional English — **merged with Urdu-1st, room R#18**
6. GCCE-101 — Civics and Community Engagement — teacher `?`
(period 7 blank in the PDF)

**English** (eng_2026) — room R#22 (BS Block)
1. ELL-102 — Literary Forms and Movement — Prof. Faryad
2. GENG-101 — Functional English — Prof. Ikram
3. ELL-101 — Introduction to Literary Studies — Prof. Sajid
4. GCCE-101 — Civics and Community Engagement — teacher `?`
5. NPH-102 — Fundamentals of Human Nutrition (NS) — Prof. Asif
6. GISL-101 — Islamic Studies Compulsory — teacher `?`

**Isl. Studies** (isl_2026) — room R#20 (BS Block)
1. GCCE-101 — Civics and Community Engagement — teacher `?`
2. BSIS-100 — Introduction to the Topics of the Quran — Prof. Amjad Butt
3. GISL-101 — Islamic Studies Compulsory — Prof. Hasnain
4. BSIS-101 — Study of Seerah of the Holy Prophet (PBUH) — Prof. Dr Azhar
5. GENG-101 — Functional English — Prof. Majid
6. NIS-105 — Natural Science — Prof. Dr. Qualab

**IT** (it_2026) — room R#01 (E-Rozgar)
1. MD-101 — Math Deficiency — teacher `?`
2. GE-194 — Pak. Studies — teacher `?`
3. GENG-101 — Functional English — teacher `?`
4. GE-169 — Applied Physics — Prof. Dr. Adil Mubeen
5. GE-160 — Intro to ICT — Prof. Ubaid
6. CC-110 — Digital Logic Design (+lab) — Prof. Faiyaz
7. GE-196 — Islamic Studies — teacher `?`

**Math** (math_2026) — room R#28 (Hostel Block)
1. MATH-101 — Single Variable Calculus — Prof. Dr. Abdul Manan
2. GENG-101 — Functional English — **merged with Zoology-1st, room R#34** — Prof. Faryad
3. SS-101 — Introduction to Economics (SS) — Prof. Ansar
4. AHPH-100 — Philosophy (AH) — teacher `?`
5. GQR-101 — Quantitative Reasoning-I — Prof. Khurram
6. GICP-101 — Ideology and Constitution of Pakistan — teacher `?`
(GE-194 Pak.Studies is in this session's subject list but doesn't appear on this row of the grid — presumably taught with another section; flagging, not guessing)

**Pol. Science** (pol_2026) — room R#16 (BS Block)
1. PS-102 — Political Science: Elements and Contemporary Discourses — Prof. Saqib Gulzar
2. PS-101 — Political Science: History and Foundational Philosophies — Prof. Afrasiab
3. GCCE-101 — Civics and Community Engagement — Prof. Mansha
4. NDM-120 — Fundamentals of Disaster Management — Prof. Waqas
5. GENG-101 — Functional English — teacher `?`
6. GISL-101 — Islamic Studies Compulsory — teacher `?`

**Urdu** (urdu_2026) — room R#18 (BS Block)
1. BSU-101 — Urdu Language: Formation and Evolution — Prof. Dr Amanullah Mohsin
2. NHPY-110 — What is Science? (NS) — Prof. Haroon
3. GCCE-101 — Civics and Community Engagement — **merged with Pol.Science-1st, room R#16** — Prof. Mansha
4. GENG-101 — Functional English — teacher `?`
5. BSU-102 — Poetic Genres: Introduction & Understanding — Miss Razia
6. GISL-101 — Islamic Studies Compulsory — **merged with Pol.Science-1st, room R#16**

**Zoology** (zoo_2026) — room **R#34** (Hostel Block — not yet in `rooms`, see issue #1)
1. Zool-103 & 104L — Cell Biology & Lab — Prof. Kamran
2. GENG-101 — Functional English — Prof. Faryad
3. Zool-101 & 102L — Animal Diversity-I & Lab — Prof. Waqas
4. GISL-101 — Islamic Studies Compulsory — **merged with Chemistry-1st, room R#29**
5. NZ-116 & 117 — Environmental Biology & Lab (NS) — Prof. Dr. Qualab
6. GCCE-101 — Civics and Community Engagement — **merged with Chemistry-1st, room R#29**

*(3rd, 5th, 7th morning grids and the evening BBA/English/IT/Isl.Ed/English-intake grids are transcribed and ready — I stopped after 1st semester to get your answers on the 5 issues above first, since they affect how I structure every remaining row, especially the merges and the "Intake" sessions. Once confirmed I'll finish the rest of the draft and then generate the actual INSERT statements for your final approval — nothing gets written to the DB until you explicitly say go.)*
