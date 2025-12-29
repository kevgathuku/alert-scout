# Malli Schema Examples

This document demonstrates the benefits of using Malli schemas in Alert Scout.

## Quick Start

```clojure
;; In your REPL
(require '[alert-scout.schemas :as schemas])
(require '[malli.generator :as mg])
```

## 1. Runtime Validation - Catch Errors Early

### Before Malli
```clojure
;; Silent failure - typo in key name
(def bad-feed {:feed-idd "hn" :url "https://example.com"})
;; Later in the code...
(:feed-id bad-feed) ;=> nil (subtle bug!)
```

### With Malli
```clojure
;; Immediate validation error
(schemas/validate schemas/Feed {:feed-idd "hn" :url "https://example.com"})
;=> Execution error (ExceptionInfo): Validation failed
;   {:errors {:feed-id ["missing required key"]}}
```

## 2. Clear Error Messages

### Example: Invalid Feed Data
```clojure
;; Empty feed-id
(schemas/explain schemas/Feed {:feed-id "" :url "https://example.com"})
;=> {:feed-id ["should have at least 1 characters"]}

;; Missing URL
(schemas/explain schemas/Feed {:feed-id "hn"})
;=> {:url ["missing required key"]}

;; Both issues
(schemas/explain schemas/Feed {:feed-id ""})
;=> {:feed-id ["should have at least 1 characters"]
;    :url ["missing required key"]}
```

### Example: Invalid Rule
```clojure
(schemas/explain schemas/Rule
  {:id "test-rule"
   :user-id 123  ;; Should be string!
   :min-should-match "two"})  ;; Should be int!
;=> {:user-id ["should be a string"]
;    :min-should-match ["should be an integer"]}
```

### Example: Invalid User Email
```clojure
(schemas/explain schemas/User
  {:id "alice"
   :email "not-an-email"})
;=> {:email ["should match regex"]}
```

## 3. Self-Documenting Code

Schemas serve as inline documentation:

```clojure
;; Just look at the schema to understand the data structure
schemas/FeedItem
;=> [:map
;    [:feed-id [:string {:min 1}]]
;    [:item-id [:string {:min 1}]]
;    [:title [:string {:min 1}]]
;    [:link [:string {:min 1}]]
;    [:published-at [:maybe inst?]]
;    [:content {:optional true} [:maybe :string]]
;    [:categories {:optional true} [:vector :string]]]
```

## 4. Protection at Boundaries

### Loading Data from Files
```clojure
;; If data/feeds.edn contains invalid data:
;; [{:feed-id "" :url ""}]

(require '[alert-scout.storage :as storage])
(storage/load-feeds "data/feeds.edn")
;=> Execution error (ExceptionInfo): Invalid feeds in data/feeds.edn
;   {:path "data/feeds.edn"
;    :errors {:feed-id ["should have at least 1 characters"]
;             :url ["should have at least 1 characters"]}}
```

### Adding Invalid Feed
```clojure
;; Try to add feed with empty URL
(storage/add-feed! "data/feeds.edn" "test" "")
;=> Execution error (ExceptionInfo): Validation failed
;   {:errors {:url ["should have at least 1 characters"]}}

;; File is NOT corrupted - validation prevented the write!
```

## 5. Generative Testing

Generate valid test data automatically:

```clojure
(require '[malli.generator :as mg])

;; Generate a valid feed
(mg/generate schemas/Feed)
;=> {:feed-id "8Zi", :url "YmG"}

;; Generate multiple feeds
(mg/sample schemas/Feed 3)
;=> ({:feed-id "N", :url ""}
;    {:feed-id "6", :url "Bi"}
;    {:feed-id "qBu2t", :url "f"})

;; Generate a complete rule
(mg/generate schemas/Rule)
;=> {:id "D7z"
;    :user-id "pI"
;    :must ["9w" "h"]
;    :should []
;    :must-not ["tKS"]
;    :min-should-match -1}

;; Use in tests
(deftest test-process-feed
  (let [feed (mg/generate schemas/Feed)
        result (process-feed feed)]
    (is (schemas/valid? schemas/ProcessFeedResult result))))
```

## 6. Development Workflow Benefits

### REPL-Driven Development
```clojure
;; Working on a new feature, quickly validate your data
(def my-alert {:user-id "kevin"
               :rule-id "rails"
               :item {...}})

;; Does it match the schema?
(schemas/valid? schemas/Alert my-alert)
;=> true

;; If not, what's wrong?
(schemas/explain schemas/Alert my-alert)
;=> {:item {:feed-id ["missing required key"]}}
```

### Refactoring Safety
```clojure
;; Changed your mind about data structure?
;; Update the schema first, then run validation
;; to find all places that need updating

;; Old schema
[:map [:name :string]]

;; New schema (renamed field)
[:map [:full-name :string]]

;; Run validation on existing data to find issues
(schemas/validate new-schema old-data)
;=> Shows exactly what needs to change
```

## 7. Optional vs Strict Validation

You can choose when to validate:

```clojure
;; Strict validation at boundaries (files, external data)
(defn load-feeds [path]
  (schemas/validate-feeds (load-edn path)))

;; Optional validation during development
(defn process-feed [feed]
  ;; During dev, check schema
  (when *assert*
    (schemas/validate schemas/Feed feed))
  ;; Process...
  )

;; Turn off validation in production for performance
;; (set! *assert* false)
```

## 8. Real-World Examples from Alert Scout

### Validating Feed Items from External RSS
```clojure
(defn fetch-items [feed-id url]
  (let [items (parse-rss url)]
    ;; Validate each item - catches malformed RSS early!
    (map #(schemas/validate-feed-item %) items)))
```

### Validating Configuration
```clojure
;; Validate all config on startup
(def rules (schemas/validate-rules (load-edn "data/rules.edn")))
(def users (schemas/validate-users (load-edn "data/users.edn")))
(def feeds (schemas/validate-feeds (load-edn "data/feeds.edn")))

;; If any config is invalid, app won't start
;; Better to fail fast than corrupt data!
```

## Performance Considerations

Malli is fast, but validation has a cost:

```clojure
;; Validate once at boundaries
(defn load-config []
  (validate-once data))  ;; Good

;; Don't validate in tight loops
(doseq [item items]
  (validate item)        ;; Bad - slow
  (process item))

;; Instead, validate the collection once
(let [validated-items (validate [:vector schema] items)]
  (doseq [item validated-items]
    (process item)))     ;; Good - fast
```

## Summary

Malli provides:
- ✅ Runtime validation with clear error messages
- ✅ Self-documenting schemas
- ✅ Protection at system boundaries
- ✅ Generative testing support
- ✅ Better REPL development experience
- ✅ Refactoring safety
- ✅ Optional/configurable validation

The small overhead is worth the safety and developer experience improvements!
