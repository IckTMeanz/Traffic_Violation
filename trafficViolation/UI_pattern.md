# Traffic Violation System — UI Rules & Design Guidelines

## 1. Design Philosophy

### Core Style

The system UI should follow a:

* Modern enterprise dashboard style
* Clean and professional layout
* Minimal but data-focused design
* Slight futuristic AI-monitoring feeling
* Easy-to-read information hierarchy

The interface should feel:

* Reliable
* Technical
* Organized
* Fast
* Monitoring-oriented

Avoid:

* Overly playful UI
* Excessive gradients
* Random colors
* Heavy animations
* Inconsistent spacing
* Different component styles across pages

---

# 2. Color System

## Primary Colors

| Token         | Value   | Usage                       |
| ------------- | ------- | --------------------------- |
| primary       | #2563EB | Main actions, active states |
| primary-hover | #1D4ED8 | Hover state                 |
| primary-light | #DBEAFE | Light background            |

## Status Colors

| Token   | Value   | Usage                     |
| ------- | ------- | ------------------------- |
| success | #16A34A | Success state             |
| warning | #F59E0B | Warning state             |
| danger  | #DC2626 | Traffic violation, errors |
| info    | #0EA5E9 | Informational             |

## Neutral Colors

| Token          | Value   |
| -------------- | ------- |
| background     | #F8FAFC |
| card           | #FFFFFF |
| border         | #E2E8F0 |
| text-primary   | #0F172A |
| text-secondary | #64748B |
| sidebar        | #0F172A |

---

# 3. Typography Rules

## Font

Primary font:

* Inter
* System fallback sans-serif

## Typography Scale

| Usage          | Class Recommendation   |
| -------------- | ---------------------- |
| Page Title     | text-3xl font-bold     |
| Section Title  | text-xl font-semibold  |
| Card Title     | text-lg font-semibold  |
| Body Text      | text-base              |
| Secondary Text | text-sm text-slate-500 |
| Table Text     | text-sm                |
| Small Caption  | text-xs                |

## Typography Guidelines

* Use bold only for hierarchy
* Avoid excessive font sizes
* Avoid more than 3 text sizes in one section
* Use muted color for metadata

---

# 4. Spacing System

## Base Unit

Use 4px spacing scale only.

| Tailwind | Size |
| -------- | ---- |
| 1        | 4px  |
| 2        | 8px  |
| 4        | 16px |
| 6        | 24px |
| 8        | 32px |
| 12       | 48px |

## Rules

* Avoid arbitrary spacing values
* Use consistent vertical rhythm
* Large sections should use 32px+ spacing
* Cards should have at least 24px padding

---

# 5. Border Radius

| Component | Radius      |
| --------- | ----------- |
| Buttons   | rounded-xl  |
| Cards     | rounded-2xl |
| Modal     | rounded-3xl |
| Inputs    | rounded-xl  |
| Tables    | rounded-2xl |

Avoid mixing too many border radius styles.

---

# 6. Shadow Rules

| Component  | Shadow    |
| ---------- | --------- |
| Card       | shadow-sm |
| Hover Card | shadow-md |
| Modal      | shadow-xl |
| Dropdown   | shadow-lg |

Guidelines:

* Use soft shadows only
* Avoid dramatic floating effects
* Avoid multiple layered shadows

---

# 7. Layout Rules

## Main App Structure

Standard layout:

* Left sidebar
* Top navigation bar
* Main content area
* Responsive content container

## Content Width

* Use max-width containers where appropriate
* Keep tables full width
* Avoid extremely wide forms

## Grid Rules

Dashboard cards:

* Desktop: 3–4 columns
* Tablet: 2 columns
* Mobile: 1 column

Use:

* gap-4
* gap-6

only.

---

# 8. Component Rules

## Buttons

### Primary Button

Used for:

* Save
* Confirm
* Submit
* Main CTA

Style:

* Filled primary color
* White text
* Rounded-xl

### Secondary Button

Used for:

* Cancel
* Back
* Less important actions

Style:

* Outline button
* Neutral border

### Danger Button

Used for:

* Delete
* Reject
* Remove

Style:

* Red background
* White text

---

## Cards

Cards should:

* Use white background
* Have subtle border
* Use rounded-2xl
* Use consistent padding
* Never feel crowded

Cards are the primary visual container of the system.

---

## Inputs

Input rules:

* Height >= 44px
* Rounded-xl
* Visible focus state
* Soft border
* Clear placeholder text

Focus state:

* Border primary color
* Subtle ring

---

## Tables

Tables should:

* Be easy to scan quickly
* Use compact spacing
* Highlight important violations clearly
* Support responsive overflow

### Violation Highlighting

| Severity | Style        |
| -------- | ------------ |
| High     | Red badge    |
| Medium   | Yellow badge |
| Low      | Blue badge   |

---

# 9. Icon Rules

## Icon Library

Use:

* Lucide icons only

## Standard Sizes

| Usage        | Size |
| ------------ | ---- |
| Inline icon  | 16px |
| Button icon  | 18px |
| Sidebar icon | 20px |
| Feature icon | 24px |

Avoid mixing icon styles from multiple libraries.

---

# 10. Animation Rules

## Transition Duration

Standard:

* 200ms
* ease-in-out

## Allowed Animations

* Hover elevation
* Fade in
* Scale slightly on hover
* Modal fade/slide
* Sidebar transition

Avoid:

* Bouncing effects
* Long animations
* Flashing effects
* Excessive motion

The UI should feel responsive and stable.

---

# 11. Dashboard Design Rules

Dashboard pages should prioritize:

* Information hierarchy
* Quick scanning
* Real-time monitoring feeling
* Clear statistics
* Action visibility

## Recommended Dashboard Sections

* Summary cards
* Recent violations
* Detection analytics
* Charts
* Camera/device status
* User activity

---

# 12. Traffic Violation Visual Language

## Important Violations

Use:

* Red accent
* Warning badge
* Strong contrast

Examples:

* No helmet
* Using phone
* Carrying 3 people

## AI Detection Areas

AI-related UI sections can use:

* Blue accent
* Slight futuristic style
* Technical labels
* Detection confidence badge

---

# 13. Dark Mode Rules

Dark mode should:

* Reduce eye strain
* Maintain readability
* Preserve hierarchy

Avoid pure black backgrounds.

Recommended:

| Token       | Value   |
| ----------- | ------- |
| dark-bg     | #020617 |
| dark-card   | #0F172A |
| dark-border | #1E293B |
| dark-text   | #F8FAFC |

---

# 14. Responsive Rules

## Mobile Priority

Important pages must:

* Stack vertically
* Keep actions accessible
* Maintain readable tables
* Preserve dashboard clarity

## Breakpoints

| Device  | Width        |
| ------- | ------------ |
| Mobile  | < 640px      |
| Tablet  | 640px–1024px |
| Desktop | > 1024px     |

---

# 15. Recommended Frontend Stack

## UI Stack

* React
* Tailwind CSS
* shadcn/ui
* Lucide React
* Framer Motion

## Chart Library

Recommended:

* Recharts

---

# 16. AI Coding Prompt Style

When generating UI using AI tools, always specify:

* enterprise dashboard
* clean spacing
* rounded-2xl cards
* modern monitoring system
* shadcn/ui style
* minimal professional interface
* responsive layout
* traffic violation management system

Example prompt:

"Create a responsive traffic violation dashboard page using React, Tailwind and shadcn/ui. Use enterprise dashboard style with clean spacing, rounded-2xl cards, modern tables, analytics cards and AI monitoring feeling."

---

# 17. UI Consistency Checklist

Before finishing any page, verify:

* Same spacing system
* Same border radius
* Same typography hierarchy
* Same button style
* Same icon library
* Same card structure
* Same animation speed
* Same color usage
* Responsive layout works
* Dark mode compatibility

---

# 18. Final Principle

A professional UI is not created by:

* More colors
* More effects
* More animations

A professional UI is created by:

* Consistency
* Good spacing
* Strong hierarchy
* Predictable interactions
* Clean layout
* Readability
* Reusable components
