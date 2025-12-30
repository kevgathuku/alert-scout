# Clojure Iteration Constructs: A Practical Guide

**When to use `for`, `map`, `doseq`, and `run!`**

*A self-reference guide based on real-world usage in Alert Scout*

---

## The Problem

You're working with a collection in Clojure and need to iterate over it. You reach for... wait, which one?

- `for`?
- `map`?
- `doseq`?
- `run!`?

The official documentation exists, but it's often unclear **when** to use each construct. This guide breaks down the differences with practical examples, so you can make the right choice every time.

## The Quick Answer

Here's the decision tree:

```
Do you want to TRANSFORM data?
├─ Yes → Are you building a collection with complex logic?
│        ├─ Yes → use `for` (list comprehension)
│        └─ No  → use `map` (simple transformation)
└─ No  → You're doing SIDE EFFECTS
         └─ Are you calling a single function on each item?
            ├─ Yes → use `run!` (concise)
            └─ No  → use `doseq` (flexible)
```

Now let's dive deeper.

---

## The Four Constructs

### 1. `map` - Transform Collections

**Purpose:** Transform each element of a collection into something else.

**Returns:** A lazy sequence of transformed values.

**When to use:**
- You want to convert one collection into another
- You're applying a transformation function
- You need the result for further processing

**Characteristics:**
- **Lazy** - only computes values when needed
- **Returns a sequence** - the transformed collection
- **Pure** - no side effects expected

#### Example from Alert Scout

```clojure
;; Transform feed entries into normalized item maps
(defn fetch-items
  [{:keys [feed-id url]}]
  (map #(entry->item % feed-id)
       (.getEntries ^SyndFeed (fetch-feed url))))

;; Transform raw results into item counts
(let [results (map process-feed feeds)
      total-items (reduce + 0 (map :item-count results))]
  ...)
```

**What's happening:**
- `map` transforms each feed entry into a normalized item
- `map :item-count` extracts the count from each result
- Both return new sequences

#### More Examples

```clojure
;; Transform numbers to their squares
(map #(* % %) [1 2 3 4])
;; => (1 4 9 16)

;; Extract user IDs
(map :user-id users)
;; => ("alice" "bob" "charlie")

;; Transform with a function
(map str/upper-case ["hello" "world"])
;; => ("HELLO" "WORLD")
```

#### Important: Laziness

```clojure
;; This creates a lazy sequence but doesn't execute
(def squares (map #(do (println "Computing" %)
                       (* % %))
                  [1 2 3]))
;; => No output yet!

;; Realizing the sequence triggers computation
(doall squares)
;; Computing 1
;; Computing 2
;; Computing 3
;; => (1 4 9)
```

**Key Insight:** `map` is lazy, so side effects inside `map` can surprise you. If you're doing side effects, you probably want `doseq` or `run!` instead.

---

### 2. `for` - List Comprehensions

**Purpose:** Build a collection with complex iteration logic (filters, multiple bindings, let bindings).

**Returns:** A lazy sequence.

**When to use:**
- You need complex iteration with multiple sequences
- You want to filter while iterating
- You need `let` bindings inside the iteration
- You're translating a mathematical set notation

**Characteristics:**
- **Lazy** - only computes when realized
- **Returns a sequence** - the generated collection
- **Declarative** - reads like "for each X in Y, produce Z"

#### Example from Alert Scout

```clojure
;; Build a collection of alerts from matching rules
(defn match-item
  [rules-by-user item]
  (for [[user-id rules] rules-by-user
        rule rules
        :when (match-rule? rule item)]
    {:user-id user-id
     :rule-id (:id rule)
     :item item}))
```

**What's happening:**
- Iterates over each `[user-id rules]` pair
- For each user, iterates over their rules
- `:when` filters to only matching rules
- Produces a map for each match

This would be much messier with `map`:

```clojure
;; Using map (harder to read)
(mapcat (fn [[user-id rules]]
          (keep (fn [rule]
                  (when (match-rule? rule item)
                    {:user-id user-id
                     :rule-id (:id rule)
                     :item item}))
                rules))
        rules-by-user)
```

#### More Examples

```clojure
;; Cartesian product with filter
(for [x [1 2 3]
      y [4 5 6]
      :when (even? (+ x y))]
  [x y])
;; => ([1 5] [2 4] [2 6] [3 5])

;; Multiple bindings with let
(for [user users
      :let [email (:email user)
            domain (second (str/split email #"@"))]
      :when (= domain "example.com")]
  (:name user))
;; => ("Alice" "Bob")

;; Nested iteration
(for [feed feeds
      item (:items feed)]
  (process-item feed item))
```

#### `for` vs `map`

| Feature | `for` | `map` |
|---------|-------|-------|
| Multiple sequences | ✅ Easy (`for [x xs, y ys]`) | ❌ Needs `mapcat` |
| Filtering | ✅ Built-in (`:when`) | ❌ Needs separate `filter` |
| Let bindings | ✅ Built-in (`:let`) | ❌ Needs nested `let` |
| Simplicity | Complex logic | Simple transformations |
| Readability | Better for complex cases | Better for simple cases |

**Rule of thumb:** Use `for` when you need `:when`, `:let`, or multiple sequences. Use `map` for simple one-to-one transformations.

---

### 3. `doseq` - Imperative Iteration for Side Effects

**Purpose:** Execute side effects for each element in a collection.

**Returns:** `nil` (you're not using the return value).

**When to use:**
- You're doing I/O (printing, file writes, network calls)
- You're mutating state (atoms, databases)
- You need multiple operations per item
- You need destructuring or complex bindings

**Characteristics:**
- **Eager** - executes immediately
- **Returns nil** - signals "I'm doing side effects"
- **Imperative** - like a `for` loop in other languages

#### Example from Alert Scout

```clojure
;; Perform side effects for each feed result
(doseq [{:keys [feed-id url alerts latest-item]} results]
  (println (colorize :gray (str "\n→ Checking feed: " feed-id " (" url ")")))
  (run! emit-alert alerts)
  (when latest-item
    (storage/update-checkpoint! feed-id (:published-at latest-item) "data/checkpoints.edn")))
```

**What's happening:**
- **Destructures** each result to get relevant fields
- **Prints** feed status (I/O side effect)
- **Emits** alerts (more I/O)
- **Updates** database checkpoint (mutation side effect)
- **Returns** `nil` (we don't care about the return value)

#### Why Not `map`?

You could technically use `map`, but it's wrong:

```clojure
;; BAD - Don't do this!
(map (fn [{:keys [feed-id url alerts latest-item]} result]
       (println ...)           ;; Side effect in map!
       (run! emit-alert alerts)
       (storage/update-checkpoint! ...))
     results)
;; => Returns a lazy sequence of nils that might not execute!
```

**Problems:**
1. **Lazy** - Side effects might not run until sequence is realized
2. **Wrong semantics** - `map` signals "transform data", but you're not
3. **Wastes memory** - Builds an unnecessary sequence of `nil` values
4. **Unclear intent** - Confuses future readers

#### The Lazy Map Trap

This is a classic Clojure gotcha:

```clojure
;; This might NOT print anything!
(map println [1 2 3])
;; => (#object[...] #object[...] #object[...])

;; You created a lazy sequence, but didn't realize it
;; The side effects haven't run yet!

;; Force realization to execute side effects (still wrong)
(doall (map println [1 2 3]))
;; 1
;; 2
;; 3
;; => (nil nil nil)  ← wasteful!

;; The right way
(doseq [x [1 2 3]]
  (println x))
;; 1
;; 2
;; 3
;; => nil  ← clean!
```

#### More Examples

```clojure
;; Print each user
(doseq [user users]
  (println (:name user)))

;; Multiple operations per item
(doseq [order orders]
  (send-confirmation-email order)
  (update-inventory order)
  (log-transaction order))

;; Destructuring with complex logic
(doseq [{:keys [name email role]} users]
  (when (= role "admin")
    (send-admin-notification name email)))

;; Nested iteration
(doseq [feed feeds]
  (println "Processing" (:feed-id feed))
  (doseq [item (:items feed)]
    (save-to-db item)))
```

#### `doseq` Supports Advanced Features

```clojure
;; Multiple bindings (cartesian product)
(doseq [x [1 2 3]
        y [4 5 6]]
  (println x "*" y "=" (* x y)))
;; 1 * 4 = 4
;; 1 * 5 = 5
;; ... etc

;; :let for intermediate values
(doseq [user users
        :let [email (:email user)
              domain (second (str/split email #"@"))]]
  (println (:name user) "is at" domain))

;; :when for filtering
(doseq [user users
        :when (= (:role user) "admin")]
  (send-admin-email user))
```

---

### 4. `run!` - Concise Side Effects

**Purpose:** Apply a single side-effecting function to each element.

**Returns:** `nil`.

**When to use:**
- You're calling one function per item
- You don't need destructuring
- You want concise code

**Characteristics:**
- **Eager** - executes immediately
- **Returns nil** - signals side effects
- **Concise** - one-liner for simple cases

#### Example from Alert Scout

```clojure
;; Before: verbose doseq
(doseq [alert alerts]
  (emit-alert alert))

;; After: concise run!
(run! emit-alert alerts)
```

Both do exactly the same thing, but `run!` is more concise when you're just calling a single function.

#### More Examples

```clojure
;; Print each item
(run! println [1 2 3])
;; 1
;; 2
;; 3
;; => nil

;; Send email to each user
(run! send-email users)

;; Save each document
(run! save-to-db documents)

;; Close each connection
(run! #(.close %) connections)
```

#### `run!` vs `doseq`

| Feature | `run!` | `doseq` |
|---------|--------|---------|
| Single function call | ✅ Concise | ⚠️ Verbose |
| Multiple operations | ❌ Need wrapper | ✅ Natural |
| Destructuring | ❌ Need wrapper | ✅ Built-in |
| Nested iteration | ⚠️ Less clear | ✅ Clear |
| Anonymous functions | ✅ Works well | ✅ Works well |

**Rule of thumb:** Use `run!` for simple "apply this function to everything" cases. Use `doseq` when you need more flexibility.

#### When You Can't Use `run!`

```clojure
;; Multiple operations - can't use run!
(doseq [user users]
  (send-email user)      ;; Operation 1
  (log-activity user)    ;; Operation 2
  (update-db user))      ;; Operation 3

;; Destructuring - awkward with run!
(doseq [{:keys [name email]} users]
  (send-email name email))

;; With run! you'd need a wrapper
(run! (fn [{:keys [name email]}]
        (send-email name email))
      users)
;; Longer than doseq!

;; Conditional logic - doseq is clearer
(doseq [user users]
  (when (active? user)
    (send-notification user)))

;; With run! you need the logic in the function
(run! (fn [user]
        (when (active? user)
          (send-notification user)))
      users)
```

---

## The Complete Comparison

### Summary Table

| Construct | Returns | Eager/Lazy | Primary Use | Side Effects? |
|-----------|---------|------------|-------------|---------------|
| **`map`** | Transformed sequence | Lazy | Transform data | ❌ No (pure) |
| **`for`** | Generated sequence | Lazy | Complex collection building | ❌ No (pure) |
| **`doseq`** | `nil` | Eager | Multiple side effects per item | ✅ Yes |
| **`run!`** | `nil` | Eager | Single function call per item | ✅ Yes |

### Decision Guide

#### Question 1: Are you transforming data or doing side effects?

**Transforming data:**
- You're creating a new collection
- You need the result for further processing
- The function is pure (no I/O, no mutations)

→ Go to Question 2

**Side effects:**
- You're doing I/O (printing, files, network)
- You're mutating state (atoms, databases)
- You don't need the return value

→ Go to Question 3

#### Question 2: Transforming Data - Simple or Complex?

**Simple transformation:**
- One function applied to each element
- No filtering or complex logic
- Example: `(map inc [1 2 3])` → `(2 3 4)`

→ **Use `map`**

**Complex transformation:**
- Multiple sequences (cartesian products)
- Filtering with `:when`
- Intermediate bindings with `:let`
- Example: `(for [x xs, y ys, :when (< x y)] [x y])`

→ **Use `for`**

#### Question 3: Side Effects - Single Function or Multiple Operations?

**Single function:**
- Just calling one function per item
- No destructuring needed
- Example: `(run! println items)`

→ **Use `run!`**

**Multiple operations:**
- Several side effects per item
- Need destructuring
- Conditional logic
- Nested iteration

→ **Use `doseq`**

---

## Real-World Patterns from Alert Scout

### Pattern 1: Pure Data Processing

```clojure
;; Phase 1: Pure functional processing (no side effects)
(let [results (map process-feed feeds)              ;; map: transform feeds to results
      all-alerts (mapcat :alerts results)           ;; mapcat: flatten all alerts
      total-items (reduce + 0 (map :item-count results))] ;; map + reduce: aggregate
  ...)
```

**Tools used:**
- `map` for transformations
- `mapcat` for transform + flatten
- `reduce` for aggregation

**Why?**
- Separates data processing from I/O
- Testable (pure functions)
- Composable (can reuse `process-feed`)

### Pattern 2: Side Effects After Data is Ready

```clojure
;; Phase 2: Side effects (after all data is computed)
(doseq [{:keys [feed-id url alerts latest-item]} results]
  (println (colorize :gray (str "\n→ Checking feed: " feed-id)))
  (run! emit-alert alerts)
  (when latest-item
    (storage/update-checkpoint! feed-id (:published-at latest-item))))
```

**Tools used:**
- `doseq` for complex side effects
- `run!` for simple side effects (nested)

**Why?**
- All data is computed first
- Side effects happen last
- Clear separation of concerns

### Pattern 3: Complex Collection Building

```clojure
;; Generate alerts from matching rules
(for [[user-id rules] rules-by-user
      rule rules
      :when (match-rule? rule item)]
  {:user-id user-id
   :rule-id (:id rule)
   :item item})
```

**Tools used:**
- `for` with multiple bindings
- `:when` for filtering

**Why?**
- Clearer than nested `map`/`filter`
- Declarative (reads like English)
- Easy to understand the cartesian product

---

## Common Pitfalls

### Pitfall 1: Side Effects in `map`

```clojure
;; WRONG - side effects in lazy map
(map (fn [user]
       (send-email user)  ;; When does this execute?
       user)
     users)

;; RIGHT - explicit side effects
(doseq [user users]
  (send-email user))
```

**Why it's wrong:**
- `map` is lazy - email might not send until sequence is realized
- Unclear when side effects happen
- Wastes memory building sequence

### Pitfall 2: Using `doseq` for Transformation

```clojure
;; WRONG - trying to build a collection with doseq
(def results (atom []))
(doseq [x data]
  (swap! results conj (process x)))
@results

;; RIGHT - use map
(def results (map process data))
```

**Why it's wrong:**
- Mutation is unnecessary
- Less functional
- Harder to test and reason about

### Pitfall 3: Ignoring `for` Capabilities

```clojure
;; VERBOSE - nested map with filter
(mapcat (fn [user]
          (map (fn [order]
                 {:user (:name user)
                  :order-id (:id order)})
               (filter :completed (:orders user))))
        users)

;; CLEAN - use for with :when
(for [user users
      order (:orders user)
      :when (:completed order)]
  {:user (:name user)
   :order-id (:id order)})
```

**Why `for` is better:**
- Reads top to bottom
- `:when` is clearer than `filter`
- Less nesting

### Pitfall 4: Overusing `run!`

```clojure
;; AWKWARD - run! with complex wrapper
(run! (fn [{:keys [name email]}]
        (when (active? email)
          (send-email name email)
          (log-activity name)))
      users)

;; CLEANER - doseq for multiple operations
(doseq [{:keys [name email]} users]
  (when (active? email)
    (send-email name email)
    (log-activity name)))
```

**When to use what:**
- `run!` for simple cases: `(run! println items)`
- `doseq` for anything more complex

---

## Mental Models

### `map` - The Transformer

Think of `map` as a **factory assembly line**:
- Input: Raw materials (collection)
- Process: Transform each item
- Output: Finished products (new collection)

```
[1 2 3] --[map inc]--> [2 3 4]
  ↑         ↑            ↑
Input    Transform    Output
```

### `for` - The Generator

Think of `for` as a **blueprint or recipe**:
- Instructions: "For each X, for each Y, if condition, make Z"
- Declarative: Describes what you want
- Lazy: Only builds when needed

```
for [x [1 2], y [3 4]]  →  Build: (3 4 6 8)
  ↑           ↑              ↑
Pattern    Ranges        Generation
```

### `doseq` - The Worker

Think of `doseq` as a **task executor**:
- Input: List of tasks (collection)
- Process: Execute each task's side effects
- Output: Nothing (`nil`) - the work is done

```
[task1 task2 task3] --[doseq execute]--> nil (but tasks completed)
         ↑                  ↑                        ↑
     Work items         Execute               Work is done
```

### `run!` - The Command

Think of `run!` as a **broadcast command**:
- Input: Targets (collection)
- Command: Single function to apply
- Output: Nothing (`nil`) - command executed

```
[item1 item2 item3] --[run! save]--> nil (all saved)
        ↑                 ↑                 ↑
    Targets          Command          Completed
```

---

## Quick Reference Card

### When To Use What

```clojure
;; Transform one collection to another
(map function collection)                    ;; ✅ Use map

;; Complex collection building (filter, multiple seqs)
(for [x xs, y ys, :when (pred x y)] ...)   ;; ✅ Use for

;; Side effects - single function per item
(run! function collection)                   ;; ✅ Use run!

;; Side effects - multiple operations per item
(doseq [item collection]                     ;; ✅ Use doseq
  (operation1 item)
  (operation2 item))

;; Aggregation (many → one)
(reduce function init collection)            ;; ✅ Use reduce

;; Filter (select subset)
(filter predicate collection)                ;; ✅ Use filter

;; Transform + flatten
(mapcat function collection)                 ;; ✅ Use mapcat
```

### Common Combinations

```clojure
;; Filter then transform
(->> collection
     (filter pred)
     (map transform))

;; Transform multiple collections in parallel
(map function coll1 coll2 coll3)

;; Nested iteration for side effects
(doseq [outer outers]
  (doseq [inner (:inners outer)]
    (process outer inner)))

;; Build complex nested structure
(for [outer outers
      inner (:inners outer)
      :when (valid? inner)]
  (combine outer inner))
```

---

## Conclusion

The key to choosing the right iteration construct is understanding **intent**:

- **`map`** says: "I'm transforming data"
- **`for`** says: "I'm building a collection with complex logic"
- **`doseq`** says: "I'm executing side effects (and I need flexibility)"
- **`run!`** says: "I'm executing side effects (single function, keep it simple)"

**The golden rules:**

1. **Pure functions?** → `map` or `for`
2. **Side effects?** → `doseq` or `run!`
3. **Simple transformation?** → `map`
4. **Complex generation?** → `for`
5. **Single function call?** → `run!`
6. **Multiple operations?** → `doseq`

Remember: In functional programming, we separate **data transformation** from **side effects**. Use `map`/`for` to transform data into the shape you want, then use `doseq`/`run!` to perform I/O or mutations once all the data is ready.

This separation makes code more testable, composable, and easier to reason about.

---

## See Also

- [Official doseq documentation](https://clojuredocs.org/clojure.core/doseq)
- [Official map documentation](https://clojuredocs.org/clojure.core/map)
- [Official for documentation](https://clojuredocs.org/clojure.core/for)
- [Official run! documentation](https://clojuredocs.org/clojure.core/run!)
- [Functional Programming Patterns](../CLAUDE.md#functional-programming-style)
- [Understanding doseq in run-once](./doseq-explanation.md)

---

*Last updated: 2025-12-30*
