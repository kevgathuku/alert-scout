(ns scripts.strip-html-from-content
  "Script to retroactively strip HTML from content files.

   Usage from REPL:
     (require '[scripts.strip-html-from-content :as strip])
     (strip/process-all-content-files!)

   Or run specific file:
     (strip/process-edn-file! (clojure.java.io/file \"content/2026-01-21/185751.edn\"))"
  (:require [clojure.java.io :as io]
            [alert-scout.fetcher :as fetcher]))

(defn- html->text
  "Extract plain text from HTML content using Jsoup.
   Delegates to fetcher's implementation."
  [html]
  (#'fetcher/html->text html))

(defn strip-html-from-excerpt
  "Strip HTML tags from excerpt :text field."
  [excerpt]
  (update excerpt :text html->text))

(defn strip-html-from-alert
  "Strip HTML from alert's :item :content and all :excerpts."
  [alert]
  (-> alert
      (update-in [:item :content] html->text)
      (update :excerpts #(mapv strip-html-from-excerpt %))))

(defn process-edn-file!
  "Process a single EDN file, stripping HTML from content and excerpts.
   Returns the filename processed."
  [f]
  (let [alerts (read-string (slurp f))
        updated (mapv strip-html-from-alert alerts)]
    (spit f (pr-str updated))
    (.getName f)))

(defn find-edn-files
  "Find all .edn files recursively in directory."
  [dir]
  (->> (io/file dir)
       file-seq
       (filter #(.endsWith (.getName %) ".edn"))))

(defn process-all-content-files!
  "Process all EDN files in content/ directory.
   Returns vector of processed filenames."
  ([]
   (process-all-content-files! "content"))
  ([dir]
   (let [files (find-edn-files dir)]
     (println (str "Processing " (count files) " files..."))
     (let [results (mapv process-edn-file! files)]
       (println (str "Done. Processed " (count results) " files."))
       results))))
