(ns alert-scout.matcher
  (:require [clojure.string :as str]))


(defn text [item]
  (->> [(:title item) (:content item)]
       (remove nil?)
       (str/join " ")
       str/lower-case))


(defn contains-term? [text term]
  (str/includes? text (str/lower-case term)))


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


(defn match-item
  "Return vector of alerts per user for a single item."
  [rules-by-user item]
  (for [[user-id rules] rules-by-user
        rule rules
        :when (match-rule? rule item)]
    {:user-id user-id
     :rule-id (:id rule)
     :item item}))
