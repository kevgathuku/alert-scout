# Email Notifications - Design Document

This document outlines the design and implementation plan for adding email notification capabilities to Alert Scout.

## Overview

Email notifications will allow users to receive alerts via email in a digest format, reducing the need for manual REPL sessions while preventing email fatigue through thoughtful batching and scheduling.

**Key Principle**: No immediate notifications - minimum frequency is **daily digest** to prevent email spam and fatigue.

## 1. Data Model & Storage

### User Notification Preferences

**Location**: `data/user-preferences.edn` (initial) or SQLite (future)

```clojure
{:user-id "alice"
 :email "alice@example.com"
 :notifications {:enabled true
                 :frequency :daily         ;; :daily, :weekly only
                 :digest-time "09:00"      ;; Local time
                 :timezone "America/New_York"
                 :quiet-hours {:start "22:00" :end "08:00"}
                 :max-alerts-per-email 50
                 :format :html}            ;; :html or :text
 :rule-preferences {"urgent-rule" {:enabled true}
                    "low-priority" {:enabled false}}}
```

**Fields:**

- `enabled` - Master switch for email notifications
- `frequency` - `:daily` (default) or `:weekly`
- `digest-time` - When to send (24-hour format)
- `timezone` - User timezone for proper scheduling
- `quiet-hours` - Don't send during these hours (optional)
- `max-alerts-per-email` - Truncate if exceeded, with "view more" link
- `format` - Email format preference
- `rule-preferences` - Per-rule overrides (enable/disable specific rules)

### Alert Queue (SQLite Database)

**Database**: `alert-scout.db`

**Table: `alert_queue`**

```sql
CREATE TABLE alert_queue (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id TEXT NOT NULL,
  rule_id TEXT NOT NULL,
  feed_id TEXT NOT NULL,
  item_id TEXT NOT NULL,
  item_title TEXT NOT NULL,
  item_link TEXT NOT NULL,
  item_published_at TEXT,
  item_content TEXT,
  created_at TEXT NOT NULL,
  sent_at TEXT,
  status TEXT NOT NULL DEFAULT 'pending',
  delivery_attempt INTEGER DEFAULT 0,
  next_retry_at TEXT,
  error_message TEXT,

  INDEX idx_user_status (user_id, status),
  INDEX idx_created_at (created_at),
  INDEX idx_next_retry (status, next_retry_at)
);
```

**Alert States:**

- `pending` - Queued, awaiting digest send
- `sending` - Currently being sent
- `sent` - Successfully delivered
- `failed` - Delivery failed permanently
- `bounced` - Email bounced
- `retry` - Scheduled for retry

**Table: `email_history`**

```sql
CREATE TABLE email_history (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id TEXT NOT NULL,
  sent_at TEXT NOT NULL,
  alert_count INTEGER NOT NULL,
  subject TEXT NOT NULL,
  status TEXT NOT NULL,
  error_message TEXT,

  INDEX idx_user_sent (user_id, sent_at)
);
```

### Why SQLite?

✅ **Reliable persistence** - Survive process restarts
✅ **Efficient queries** - Find pending alerts by user/time
✅ **Audit trail** - Track delivery history
✅ **Atomic operations** - Prevent duplicate sends
✅ **Simple** - No separate database server needed
✅ **Retry logic** - Track failed sends and retry attempts

## 2. Scheduling & Timing

### Frequency Options

**Daily Digest** (default)

- Send once per day at user's specified time
- Aggregate all alerts from previous 24 hours
- Default time: 9:00 AM user's timezone

**Weekly Digest**

- Send once per week (e.g., Monday morning)
- Aggregate all alerts from previous 7 days
- Default: Monday 9:00 AM user's timezone

### Scheduler Implementation

**Option A: Cron Job** (recommended for MVP)

```bash
# Run digest sender every hour
0 * * * * cd /path/to/alert-scout && lein run -m alert-scout.email/send-digests
```

The sender checks each user's preferences and sends if:

- Current time matches user's digest-time (±30 min window)
- Respects quiet hours
- Has pending alerts for that user

**Option B: Systemd Timer** (for production)

```ini
[Unit]
Description=Alert Scout Email Digest Sender

[Timer]
OnCalendar=hourly
Persistent=true

[Install]
WantedBy=timers.target
```

**Option C: Internal Scheduler** (future)

- Background thread in long-running process
- More complex but more flexible

### Timezone Handling

- Store user timezone in preferences
- Convert digest-time to UTC for comparison
- Use `clj-time` or Java 8 Time API
- Handle DST transitions properly

## 3. Email Delivery Infrastructure

### SMTP Configuration

**Location**: `data/email-config.edn` (not in git)

```clojure
{:smtp {:host "smtp.gmail.com"
        :port 587
        :user "alerts@alertscout.com"
        :pass "..." ;; Or use env var
        :tls true}
 :from {:address "alerts@alertscout.com"
        :name "Alert Scout"}
 :reply-to "noreply@alertscout.com"
 :rate-limit {:max-per-hour 100
              :max-per-day 1000}}
```

**Environment Variables** (for sensitive data):

```bash
SMTP_PASSWORD=your-password
EMAIL_FROM_ADDRESS=alerts@alertscout.com
```

### Email Service Options

**Phase 1: Direct SMTP** (MVP)

- Use Clojure's `postal` library
- Works with any SMTP server
- Simple, no external dependencies

**Phase 2: Email Service** (production)

- **SendGrid** - Reliable, good deliverability, 100 free emails/day
- **AWS SES** - Very cheap ($0.10/1000 emails)
- **Mailgun** - Developer-friendly API
- **Postmark** - Excellent for transactional emails

Benefits of email services:

- Better deliverability (SPF/DKIM configured)
- Bounce handling
- Analytics/tracking
- Templates
- Dedicated IP addresses

## 4. Alert Queue Management

### Queue Processing Flow

```
1. Alert Generated (run-once)
   ↓
2. Insert into SQLite queue (status: pending)
   ↓
3. Scheduler runs (hourly cron job)
   ↓
4. Check each user's preferences
   ↓
5. Is it time to send digest? (digest-time ± window)
   ↓ YES
6. Query pending alerts for user
   ↓
7. Aggregate & format digest email
   ↓
8. Send email via SMTP
   ↓
9. Mark alerts as sent (update sent_at, status)
   ↓
10. Record in email_history
```

### Retry Logic

**Failure Handling:**

1. First failure: Retry in 5 minutes (`status: retry`, increment `delivery_attempt`)
2. Second failure: Retry in 30 minutes
3. Third failure: Retry in 2 hours
4. After 3 attempts: Mark as `failed`, log error

**Exponential Backoff Formula:**

```
next_retry = now + (5 minutes * 2^attempt)
```

**Maximum Attempts:** 3

**Permanent Failures:**

- Invalid email address → Disable notifications for user
- Authentication failed → Alert admin
- Server unavailable → Retry

### Deduplication

Prevent duplicate sends:

- Check `sent_at` is null before sending
- Use database transaction for status update
- Lock mechanism if running multiple scheduler instances

## 5. Email Templates

### Template System

**Location**: `resources/email-templates/`

```
resources/email-templates/
  ├── digest-html.mustache
  ├── digest-text.mustache
  ├── welcome.html
  └── partials/
      ├── header.html
      ├── footer.html
      └── alert-item.html
```

Use **Mustache** or **Selmer** for templating.

### Digest Email Structure

**Subject Line:**

```
[Alert Scout] {count} new alerts - {date}
```

Examples:

- `[Alert Scout] 5 new alerts - Dec 29, 2025`
- `[Alert Scout] 15 new alerts - Week of Dec 23`

**Email Body (HTML):**

```html
<!DOCTYPE html>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
    /* Mobile-friendly, dark mode support */
  </style>
</head>
<body>
  <div class="container">
    <!-- Header -->
    <h1>Alert Scout Digest</h1>
    <p>Hi {{user-name}}, you have {{alert-count}} new alerts</p>

    <!-- Summary -->
    <div class="summary">
      <h2>Summary</h2>
      <ul>
        <li>HN: 8 alerts</li>
        <li>Blog: 2 alerts</li>
      </ul>
    </div>

    <!-- Alerts grouped by feed -->
    {{#feeds}}
    <h2>{{feed-name}}</h2>
    {{#alerts}}
    <div class="alert-item">
      <h3><a href="{{link}}">{{title}}</a></h3>
      <p class="meta">
        Rule: {{rule-name}} | Published: {{published-at}}
      </p>
      <p>{{excerpt}}</p>
    </div>
    {{/alerts}}
    {{/feeds}}

    <!-- Footer -->
    <div class="footer">
      <p>
        <a href="{{preferences-link}}">Manage Preferences</a> |
        <a href="{{unsubscribe-link}}">Unsubscribe</a>
      </p>
      <p>Alert Scout | your-address-here</p>
    </div>
  </div>
</body>
</html>
```

**Email Body (Plain Text):**

```
Alert Scout Digest
==================

Hi {{user-name}}, you have {{alert-count}} new alerts

SUMMARY
-------
- HN: 8 alerts
- Blog: 2 alerts

HACKER NEWS
-----------

[1] Deploying Rails with Docker and Kamal
    Rule: rails-deployment | Published: Dec 29, 2025 15:30
    https://news.ycombinator.com/item?id=123456

[2] Building APIs with Rails 8
    Rule: rails-api | Published: Dec 29, 2025 14:20
    https://news.ycombinator.com/item?id=123457

---
Manage Preferences: https://alertscout.com/preferences?token=...
Unsubscribe: https://alertscout.com/unsubscribe?token=...

Alert Scout | your-address-here
```

### Template Variables

- `{{user-name}}` - User's name or ID
- `{{alert-count}}` - Number of alerts
- `{{date}}` - Human-readable date
- `{{feeds}}` - Array of feed objects
- `{{feed-name}}` - Feed display name
- `{{alerts}}` - Array of alert objects per feed
- `{{title}}`, `{{link}}`, `{{excerpt}}` - Item data
- `{{rule-name}}` - Which rule matched
- `{{published-at}}` - When item was published
- `{{preferences-link}}` - Link to preferences (with auth token)
- `{{unsubscribe-link}}` - One-click unsubscribe

## 6. New Components/Namespaces

### `alert-scout.email`

**Responsibilities:**

- Send emails via SMTP
- Render templates
- Generate HTML from markdown
- Handle multipart (HTML + text) emails

**Key functions:**

```clojure
(defn send-email! [to subject body-html body-text])
(defn render-digest [user alerts])
(defn send-digest! [user-id])
```

### `alert-scout.queue`

**Responsibilities:**

- Queue alert for delivery
- Query pending alerts
- Mark alerts as sent/failed
- Retry failed sends

**Key functions:**

```clojure
(defn enqueue-alert! [alert user-id])
(defn pending-alerts-for-user [user-id])
(defn mark-as-sent! [alert-ids])
(defn mark-as-failed! [alert-id error])
(defn alerts-to-retry [])
```

### `alert-scout.scheduler`

**Responsibilities:**

- Check which users need digest sent
- Respect user preferences (time, timezone)
- Aggregate alerts into digest
- Trigger email send

**Key functions:**

```clojure
(defn users-ready-for-digest [])
(defn send-digests! [])
(defn should-send-digest? [user-prefs current-time])
```

### `alert-scout.db`

**Responsibilities:**

- SQLite connection management
- Schema creation/migration
- CRUD operations
- Transaction handling

**Key functions:**

```clojure
(defn init-db! [])
(defn migrate! [])
(defn insert-alert! [alert-data])
(defn query [sql params])
```

## 7. Integration with Existing Code

### Modified: `alert-scout.core/run-once`

```clojure
(defn run-once [feeds]
  ;; ... existing code ...

  ;; NEW: Queue alerts for email if user has notifications enabled
  (doseq [alert all-alerts]
    (when (email-enabled? (:user-id alert))
      (queue/enqueue-alert! alert (:user-id alert))))

  ;; ... return results ...
  )
```

### New CLI Commands

```bash
# Send digests now (for testing)
lein run -m alert-scout.email/send-digests

# Send test email to user
lein run -m alert-scout.email/send-test-email alice

# Preview digest in browser
lein run -m alert-scout.email/preview-digest alice > /tmp/digest.html
```

## 8. Testing & Preview

### Testing Requirements

**Email Preview**

- Render digest to HTML file
- Open in browser for visual inspection
- Test with different alert counts

**Dry Run Mode**

```clojure
;; Log email instead of sending
(with-dry-run
  (email/send-digest! "alice"))
```

**Mock SMTP for Tests**

- Use in-memory mock for unit tests
- Verify email was "sent" without actual SMTP

**Test Data**

- Generate sample alerts
- Test with 0, 1, 10, 50, 100 alerts
- Test with missing data (nil titles, etc.)

### Monitoring

**Metrics to Track:**

- Emails sent per day
- Delivery success rate
- Bounce rate
- Average alerts per digest
- Users with notifications enabled

**Logging:**

- Log every email sent (to, subject, status)
- Log all failures with full error
- Log retry attempts

**Alerts for Admins:**

- High bounce rate (>5%)
- Many failed sends (>10 in hour)
- SMTP authentication failures

## 9. User Experience

### Default Settings

**New User Defaults:**

- Notifications: Disabled (opt-in)
- Frequency: Daily
- Time: 9:00 AM
- Timezone: Detect from system or ask
- Format: HTML

### Preference Management

**CLI Commands** (Phase 1):

```bash
# Enable notifications
lein run -m alert-scout.prefs/enable alice

# Set frequency
lein run -m alert-scout.prefs/set-frequency alice weekly

# Set digest time
lein run -m alert-scout.prefs/set-time alice 18:00

# Disable specific rule
lein run -m alert-scout.prefs/disable-rule alice low-priority
```

**Web UI** (Phase 2):

- Simple form to update preferences
- One-click unsubscribe link
- Test email button

### Email Quality Standards

✅ **Mobile-friendly** - Responsive design, touch targets
✅ **Dark mode support** - `@media (prefers-color-scheme: dark)`
✅ **Fast loading** - Inline CSS, no external images
✅ **Accessible** - Semantic HTML, alt text, screen reader friendly
✅ **Clear hierarchy** - Easy to scan
✅ **Actionable** - Clear CTAs (Read more, Manage preferences)

## 10. Legal & Compliance

### Required Elements (CAN-SPAM Act)

✅ **Unsubscribe link** - One-click, honored immediately
✅ **Physical address** - In footer
✅ **Clear sender** - "Alert Scout" clearly identified
✅ **Accurate subject** - No deceptive subjects
✅ **Transactional nature** - These are service emails

### GDPR Compliance

✅ **Opt-in required** - Don't send without user consent
✅ **Data export** - User can export their alert history
✅ **Data deletion** - User can delete all data
✅ **Privacy policy** - Link in footer

### Unsubscribe Handling

**One-click unsubscribe:**

```
https://alertscout.com/unsubscribe?token=<signed-token>&user=alice
```

When clicked:

1. Validate token (prevent abuse)
2. Set `notifications.enabled = false`
3. Show confirmation page
4. Stop sending immediately

**Preferences link** (with auth token):

```
https://alertscout.com/preferences?token=<signed-token>&user=alice
```

More flexible - user can adjust frequency, disable specific rules, etc.

## 11. Implementation Phases

### Phase 1: MVP (Daily Digests Only)

**Scope:**

- Daily digest only
- SQLite queue
- Plain text emails via SMTP
- Manual cron job
- CLI preference management

**Deliverables:**

1. SQLite schema
2. Queue management functions
3. Email sender
4. Plain text template
5. Scheduler script
6. Basic tests

**Time Estimate:** 2-3 days

### Phase 2: Polish

**Scope:**

- HTML email templates
- Weekly digest option
- Retry logic
- Better error handling
- Email preview command

**Deliverables:**

1. HTML templates (Mustache)
2. Retry mechanism
3. Monitoring/logging
4. Documentation

**Time Estimate:** 1-2 days

### Phase 3: Production Ready

**Scope:**

- Email service integration (SendGrid)
- Web-based preferences UI
- Bounce handling
- Rate limiting
- Comprehensive testing

**Deliverables:**

1. SendGrid integration
2. Web UI for preferences
3. Bounce webhook handler
4. Load tests

**Time Estimate:** 3-4 days

## 12. Dependencies

### New Libraries

```clojure
;; In project.clj
:dependencies [
  ;; ... existing ...
  [com.draines/postal "2.0.5"]           ;; SMTP client
  [org.clojure/java.jdbc "0.7.12"]       ;; JDBC for SQLite
  [org.xerial/sqlite-jdbc "3.44.1.0"]    ;; SQLite driver
  [clj-time "0.15.2"]                    ;; Timezone handling
  [de.ubercode.clostache/clostache "1.4.0"]  ;; Mustache templates
  [hiccup "1.0.5"]                       ;; HTML generation (optional)
]
```

### Configuration Files

```
data/
  ├── email-config.edn         # SMTP settings (gitignored)
  ├── user-preferences.edn     # User email preferences
  └── alert-scout.db           # SQLite database (gitignored)

resources/
  └── email-templates/
      ├── digest-html.mustache
      ├── digest-text.mustache
      └── partials/
```

## 13. Open Questions & Decisions

### To Decide

1. **Template engine**: Mustache, Selmer, or Hiccup?
   - Recommendation: Mustache (simple, logic-less)

2. **Email service**: Direct SMTP or SendGrid?
   - Recommendation: Start with SMTP, migrate to SendGrid

3. **Preferences storage**: EDN file or SQLite?
   - Recommendation: EDN for now, SQLite later

4. **Authentication for preferences**: Signed tokens or login?
   - Recommendation: Signed tokens (simpler)

5. **Digest grouping**: By feed, by rule, or chronological?
   - Recommendation: By feed (easier to scan)

6. **Alert limit**: What happens with >50 alerts?
   - Recommendation: Show first 50, link to "View all online"

7. **Timezone detection**: Ask user or detect from system?
   - Recommendation: Default to system, allow override

## 14. Success Metrics

### Launch Goals

- ✅ 0 failed sends in first week
- ✅ <1% bounce rate
- ✅ >80% open rate (for engaged users)
- ✅ <5% unsubscribe rate
- ✅ Emails delivered within 5 minutes of scheduled time

### Monitoring

- Track delivery rate
- Monitor queue size
- Alert on failures
- Log user feedback

## 15. Future Enhancements

**Beyond Initial Release:**

- Push notifications (mobile)
- Slack/Discord webhooks
- SMS notifications (for critical alerts)
- Smart scheduling (send when user is most likely to read)
- AI-powered digest summarization
- Email threading (group related alerts)
- Priority/importance scoring
- Archive/search old alerts online

---

**Document Version:** 1.0
**Last Updated:** December 29, 2025
**Status:** Design Phase
