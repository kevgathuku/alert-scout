# Understanding `doseq` in `run-once`

This document explains how and why `doseq` is used in the `run-once` function, and when to use it vs other iteration constructs.

## The Code

```clojure
(defn run-once
  [feeds]
  ;; Process all feeds functionally (no mutation)
  (let [results (map process-feed feeds)
        all-alerts (mapcat :alerts results)
        total-items (reduce + 0 (map :item-count results))]

    ;; Perform side effects after data processing
    (doseq [{:keys [feed-id url alerts latest-item]} results]  ;; ← First doseq
      (println (colorize :gray (str "\n→ Checking feed: " feed-id " (" url ")")))
      (doseq [alert alerts]                                     ;; ← Second doseq (nested)
        (emit-alert alert))
      (when latest-item
        (storage/update-checkpoint! feed-id (:published-at latest-item) "data/checkpoints.edn")))

    (println (format-summary all-alerts))
    (println (colorize :gray (str "Processed " total-items " new items across " (count feeds) " feeds\n")))

    {:alerts (vec all-alerts)
     :items-processed total-items}))
```

## What is `doseq`?

`doseq` is Clojure's **side-effects iteration** construct. It:

- Iterates over a sequence
- **Does NOT return a value** (returns `nil`)
- **Used for side effects only** (printing, I/O, mutations)
- Eager (not lazy) - executes immediately

Think of it as a functional `for` loop that you use when you don't care about the return value.

## First `doseq` - Processing Results

```clojure
(doseq [{:keys [feed-id url alerts latest-item]} results]
  (println ...)
  (doseq [alert alerts] ...)
  (when latest-item
    (storage/update-checkpoint! ...)))
```

**What it does:**

- Iterates over each `result` from `(map process-feed feeds)`
- Destructures each result to extract `:feed-id`, `:url`, `:alerts`, `:latest-item`
- Performs side effects for each feed

**Side effects performed:**

1. **Prints** feed status message
2. **Emits** alerts (via nested `doseq`)
3. **Updates** checkpoint in database

**Why `doseq` here?**

- We're doing I/O (printing, database writes)
- We don't need a return value
- We want to execute immediately (not lazily)

### Visual Breakdown

```
results = [{:feed-id "hn" :alerts [...] ...}
           {:feed-id "blog" :alerts [...] ...}]

Iteration 1:
  feed-id = "hn"
  → Print "Checking feed: hn"
  → Emit all alerts for hn
  → Update checkpoint for hn

Iteration 2:
  feed-id = "blog"
  → Print "Checking feed: blog"
  → Emit all alerts for blog
  → Update checkpoint for blog
```

## Second `doseq` - Emitting Alerts

```clojure
(doseq [alert alerts]
  (emit-alert alert))
```

**What it does:**

- **Nested** inside the first `doseq`
- Iterates over all alerts for the current feed
- Calls `emit-alert` for each one

**Why `doseq` here?**

- `emit-alert` is a side-effecting function (prints to console)
- We don't need the return values
- We want each alert printed immediately

### Visual Breakdown

```
Feed "hn" has alerts = [{:user-id "alice" ...}
                        {:user-id "bob" ...}
                        {:user-id "alice" ...}]

Iteration 1: emit-alert {:user-id "alice" ...}  → prints to console
Iteration 2: emit-alert {:user-id "bob" ...}    → prints to console
Iteration 3: emit-alert {:user-id "alice" ...}  → prints to console
```

## Why NOT use `map` here?

You **could** technically use `map`, but it would be wrong:

```clojure
;; BAD - Don't do this!
(map (fn [result]
       (println ...)
       (map emit-alert (:alerts result))  ;; Returns lazy seq, might not execute!
       (storage/update-checkpoint! ...))
     results)
```

**Problems:**

1. **`map` is lazy** - side effects might not execute until you realize the sequence
2. **Unused return value** - `map` returns a sequence you don't need
3. **Unclear intent** - Signals you want a transformed collection, but you don't
4. **Potential bugs** - Lazy sequences can cause side effects to execute at unexpected times

### Example of Lazy Map Problem

```clojure
;; This might NOT print anything!
(map println [1 2 3])
;; => (#object[...] #object[...] #object[...])  ← Returns lazy seq, not realized

;; But this WILL print
(doseq [x [1 2 3]]
  (println x))
;; 1
;; 2
;; 3
;; => nil
```

## Comparing Iteration Constructs

| Construct | Returns | Eager/Lazy | Use Case |
|-----------|---------|------------|----------|
| `map` | Transformed sequence | Lazy | Transform data |
| `mapcat` | Flattened sequence | Lazy | Transform + flatten |
| `reduce` | Single value | Eager | Aggregate data |
| `filter` | Filtered sequence | Lazy | Select data |
| **`doseq`** | **`nil`** | **Eager** | **Side effects only** |
| `for` | Sequence | Lazy | List comprehension |
| `run!` | `nil` | Eager | Side effects (like `doseq`) |

## Functional Design Pattern

The `run-once` function follows a clear pattern:

### Phase 1: Pure Functional Processing (no side effects)

```clojure
(let [results (map process-feed feeds)        ;; ← Pure function
      all-alerts (mapcat :alerts results)     ;; ← Data transformation
      total-items (reduce + 0 ...)]           ;; ← Aggregation
```

**Characteristics:**

- No I/O, no printing, no database writes
- Uses `map`, `mapcat`, `reduce`
- Returns data structures
- Testable and composable

### Phase 2: Side Effects (after data is collected)

```clojure
(doseq [result results]                       ;; ← Side effects
  (println ...)                               ;; ← I/O
  (emit-alert ...)                           ;; ← I/O
  (storage/update-checkpoint! ...))          ;; ← Database write
```

**Characteristics:**

- Uses `doseq` for iteration
- Performs I/O operations
- Does NOT transform data
- Happens AFTER all data is ready

### Why This Pattern?

✅ **Separation of concerns** - Logic separate from side effects
✅ **Testability** - Can test `process-feed` without I/O
✅ **Predictability** - All data computed before any side effects
✅ **Composability** - Pure functions can be combined easily

## Alternative: `run!`

Clojure also has `run!` which is similar to `doseq`:

```clojure
;; Using doseq
(doseq [alert alerts]
  (emit-alert alert))

;; Using run! (equivalent)
(run! emit-alert alerts)
```

**When to use `run!`:**

- Single function call per item
- No destructuring needed
- More concise

**When to use `doseq`:**

- Multiple operations per item
- Need destructuring
- Nested iteration
- More explicit about side effects

## Common Patterns

### Pattern 1: Print each item

```clojure
;; Good
(doseq [x coll]
  (println x))

;; Also good (more concise)
(run! println coll)
```

### Pattern 2: Multiple operations per item

```clojure
;; Good
(doseq [user users]
  (send-email user)
  (log-activity user)
  (update-db user))

;; Can't use run! here (multiple operations)
```

### Pattern 3: Destructuring

```clojure
;; Good
(doseq [{:keys [name email]} users]
  (send-email name email))

;; Would need a wrapper function with run!
(run! (fn [{:keys [name email]}]
        (send-email name email))
      users)
```

### Pattern 4: Nested iteration

```clojure
;; Good
(doseq [feed feeds]
  (doseq [item (:items feed)]
    (process-item item)))

;; Nested run! is less clear
(run! (fn [feed]
        (run! process-item (:items feed)))
      feeds)
```

## Summary

### In `run-once`, `doseq` is used for

1. **First `doseq`**: Iterate over feed results
   - Print feed status
   - Emit alerts (via nested `doseq`)
   - Update checkpoints

2. **Second `doseq`**: Iterate over alerts for current feed
   - Print each alert to console

### Key Takeaways

✅ **Use `doseq` for side effects** (I/O, printing, database writes)
✅ **Use `map`/`filter`/`reduce` for data transformation** (pure functions)
✅ **`doseq` is eager** - executes immediately
✅ **`doseq` returns `nil`** - you don't use its return value
✅ **Separate pure and impure code** - compute data first, then perform side effects

### Mental Model

Think of `doseq` as:

```
"For each item in this collection, do these side effects"
```

Not:

```
"Transform this collection into a new collection"  ← That's map/filter
```

---

**See Also:**

- [Clojure doseq documentation](https://clojuredocs.org/clojure.core/doseq)
- [Functional Programming Style](../CLAUDE.md#functional-programming-style)
