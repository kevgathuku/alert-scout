(ns alert-scout.storage
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))


(defn load-edn [path]
  (with-open [r (java.io.PushbackReader. (io/reader path))]
    (edn/read {:eof nil} r)))


(defn save-edn! [path data]
  (spit path (pr-str data)))


(def checkpoints (atom {}))


(defn load-checkpoints! [path]
  (reset! checkpoints (or (load-edn path) {})))


(defn save-checkpoints! [path]
  (save-edn! path @checkpoints))

(defn last-seen [feed-id]
  (get @checkpoints feed-id))


(defn update-checkpoint! [feed-id ts path]
  (swap! checkpoints assoc feed-id ts)
  (save-checkpoints! path))


;; --- Feed management ---

(defn load-feeds
  "Load feeds from an EDN file. Returns a vector of feed maps with :feed-id and :url."
  [path]
  (or (load-edn path) []))


(defn save-feeds!
  "Save feeds to an EDN file."
  [path feeds]
  (save-edn! path feeds))


(defn add-feed!
  "Add a new feed to the feeds file. Returns the updated feeds vector."
  [path feed-id url]
  (let [feeds (load-feeds path)
        new-feed {:feed-id feed-id :url url}
        updated-feeds (conj feeds new-feed)]
    (save-feeds! path updated-feeds)
    updated-feeds))


(defn remove-feed!
  "Remove a feed by feed-id from the feeds file. Returns the updated feeds vector."
  [path feed-id]
  (let [feeds (load-feeds path)
        updated-feeds (vec (remove #(= (:feed-id %) feed-id) feeds))]
    (save-feeds! path updated-feeds)
    updated-feeds))


(defn get-feed
  "Get a specific feed by feed-id."
  [path feed-id]
  (let [feeds (load-feeds path)]
    (first (filter #(= (:feed-id %) feed-id) feeds))))
