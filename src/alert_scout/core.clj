(ns alert-scout.core
  (:require [alert-scout.fetcher :as fetcher]
            [alert-scout.matcher :as matcher]
            [alert-scout.storage :as storage]
            [clojure.string :as str])
  (:import (java.util Date)))


;; --- Load users / rules / feeds / checkpoints ---
(def users (storage/load-edn "data/users.edn"))
(def rules (storage/load-edn "data/rules.edn"))
(def feeds (storage/load-feeds "data/feeds.edn"))
(storage/load-checkpoints! "data/checkpoints.edn")

(def rules-by-user (matcher/rules-by-user rules))

(defn emit-alert [alert]
  (println "ALERT:" (:user-id alert) (:rule-id alert) (:item alert)))

;; --- Fetch, match, emit alerts, update checkpoint ---
(defn run-once
  "Process feeds for new items and emit alerts.
   If no feeds provided, uses the feeds loaded from data/feeds.edn."
  ([]
   (run-once feeds))
  ([feeds]
   (doseq [{:keys [feed-id url]} feeds]
     (let [last-seen (storage/last-seen feed-id)
           items (->> (fetcher/fetch-items feed-id url)
                      (filter #(when-let [ts (:published-at %)]
                                 (or (nil? last-seen) (.after ^Date ts last-seen))))
                      (sort-by :published-at))]
       (doseq [item items]
         (doseq [alert (matcher/match-item rules-by-user item)]
           (emit-alert alert)))
       (when-let [latest (last items)]
         (storage/update-checkpoint! feed-id (:published-at latest) "data/checkpoints.edn"))))))
