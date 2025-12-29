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
