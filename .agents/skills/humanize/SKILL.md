---
name: humanize
description: Rewrite or draft prose (report chapters, documentation, requirement descriptions, summaries) so it reads like a person who understands the system actually wrote it, not like generated boilerplate. Use this whenever producing report/document content of any real length — FYDP/SRS chapters, functional requirement descriptions, README sections, executive summaries — especially content meant to be read and graded by a human (an advisor, a reviewer, a teammate). Trigger on requests to "humanize," "make this sound natural," "less AI-sounding," or any time you're about to write more than a paragraph of explanatory prose for a document.
---

# Humanize

Technical documentation written by an LLM has a recognizable texture: it hedges everything, reaches for the grandest available synonym, and arranges its points in suspiciously even triplets. None of that makes the content more correct — it just makes a reader (especially one grading a report) suspicious. This skill is a checklist to run your own draft against before calling a section done.

## Why this matters here

A report a real team is submitting under their own names needs to sound like people who lived through the project wrote it — including its rough edges, its actual tradeoffs, and its specific numbers — not like a template filled in by a paraphrasing engine. Graders and advisors read a lot of these; the generic ones are easy to spot and get marked down for it, regardless of whether the underlying facts are correct.

## The checklist

**1. Kill the tells.** Scan for these words/phrases and cut or replace them unless they're doing real work: *leverage, robust, seamless, streamline, holistic, delve, furthermore, it is important to note that, in today's fast-paced world, plays a crucial/vital role, a testament to, at the end of the day*. Plain verbs beat inflated ones — "uses" not "leverages," "handles" not "seamlessly handles."

**2. Break the triplet reflex.** If you notice you've written three parallel clauses, three bullet examples, or three-adjective strings ("fast, reliable, and scalable") more than once in a section, vary it — two things, four things, one thing explained in more depth. Rule-of-three is a tell precisely because it's so easy to generate and so rarely how people actually talk.

**3. Prefer one well-chosen specific over three vague abstractions.** "Reduces login time" is a template. "Cuts the fee-challan process from a 20-minute office visit to a 10-second screen load" is what someone who watched the old process actually noticed. If you don't have the specific, say the general thing once and move on — don't pad it with adjectives to disguise the vagueness.

**4. Vary sentence length on purpose.** A paragraph of uniformly medium-length sentences reads like a text generator smoothing everything to the same register. Follow a longer, clause-heavy sentence with a short one. Let a sentence run on when the thought actually runs on.

**5. Let it have a point of view.** Real technical writing admits what was hard, what was a judgment call, what's still rough. "We initially tried X; it broke under Y, so we switched to Z" reads as authored. "The system was carefully designed to ensure optimal performance" reads as filler. If a design decision had a real tradeoff, name the tradeoff — don't just assert the choice was good.

**6. Don't over-hedge or over-qualify.** "In most cases, it can generally be said that the system typically tends to perform reasonably well" is five hedges deep and says nothing. State the claim; if it has a real exception, state the exception once, specifically.

**7. Cut throat-clearing.** Don't open a paragraph by restating what the paragraph is about to do ("This section will discuss..."). Just discuss it. Don't close by restating what it just said either, unless it's an actual chapter/report summary section where that's the genre convention.

**8. Match register to the document's actual voice.** A college FYDP report, an internal design doc, and a user-facing README don't sound the same. Read a paragraph you've already written elsewhere in the same document and match its level of formality and its typical sentence rhythm, rather than defaulting to one generic "professional" tone for everything.

## How to use this while drafting

Don't treat this as a final proofreading pass bolted onto generated text — write with these habits from the first draft, then do one quick pass afterward specifically hunting for the tells in item 1 and the triplet reflex in item 2, since those two are the easiest to produce on autopilot and the fastest for a reader to notice.
