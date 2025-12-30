(ns alert-scout.matcher
  (:require [clojure.string :as str]
            [alert-scout.excerpts :as excerpts]))

(defn text [item]
  (->> [(:title item) (:content item)]
       (remove nil?)
       (str/join " ")
       str/lower-case))

(defn contains-term? [text term]
  (str/includes? (str/lower-case text) (str/lower-case term)))

(defn match-rule?
  "Match a simplified string-based rule against a feed item."
  [{:keys [must should must-not min-should-match]
    :or {should [] min-should-match 0}}
   item]
  (let [t (text item)]
    (and
     (every? #(contains-term? t %) must)
     (not-any? #(contains-term? t %) must-not)
     (>= (count (filter #(contains-term? t %) should))
         min-should-match))))

(defn rules-by-user [rules]
  (group-by :user-id rules))

(defn get-matched-terms
  "Extract terms that actually matched from rule.

  Args:
    rule - Rule map with :must, :should, :must-not fields
    item - FeedItem to check terms against

  Returns vector of matched terms (lowercase for consistency)."
  [rule item]
  (let [t (text item)
        must-terms (or (:must rule) [])
        should-terms (or (:should rule) [])
        must-matches (filter #(contains-term? t %) must-terms)
        should-matches (filter #(contains-term? t %) should-terms)]
    (vec (distinct (concat must-matches should-matches)))))

(defn match-item
  "Return vector of alerts per user for a single item.

  Each alert includes excerpts showing where matched terms appear."
  [rules-by-user item]
  (for [[user-id rules] rules-by-user
        rule rules
        :when (match-rule? rule item)
        :let [matched-terms (get-matched-terms rule item)
              item-excerpts (excerpts/generate-excerpts-for-item item matched-terms)]]
    {:user-id user-id
     :rule-id (:id rule)
     :item item
     :excerpts item-excerpts}))
