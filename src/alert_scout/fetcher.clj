(ns alert-scout.fetcher
  (:import
   (java.net URL)
   (com.rometools.rome.io SyndFeedInput XmlReader)))

(defn fetch-feed
  "Fetch RSS/Atom feed and return SyndFeed."
  [url]
  (with-open [reader (XmlReader. (URL. url))]
    (.build (SyndFeedInput.) reader)))


(defn entry->item
  "Normalize feed entry to a Clojure map."
  [^com.rometools.rome.feed.synd.SyndEntry entry feed-id]
  {:feed-id feed-id
   :item-id (or (.getUri entry) (.getLink entry))
   :title (.getTitle entry)
   :link (.getLink entry)
   :published-at (or (.getPublishedDate entry)
                     (.getUpdatedDate entry))
   :content (or (some-> entry .getContents first .getValue)
                (some-> entry .getDescription .getValue))
   :categories (mapv #(.getName %) (.getCategories entry))})


(defn fetch-items
  "Fetch all new items for a feed, normalized."
  [feed-id url]
  (map #(entry->item % feed-id)
       (.getEntries (fetch-feed url))))
