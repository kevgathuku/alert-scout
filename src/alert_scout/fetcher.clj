(ns alert-scout.fetcher
  (:import
   (java.net URL)
   (com.rometools.rome.io SyndFeedInput XmlReader)
   (com.rometools.rome.feed.synd SyndFeed SyndEntry SyndContent SyndCategory)))

(defn fetch-feed
  "Fetch RSS/Atom feed and return SyndFeed."
  [url]
  (with-open [reader (XmlReader. (URL. url))]
    (.build (SyndFeedInput.) reader)))

(defn entry->item
  "Normalize feed entry to a Clojure map."
  [^SyndEntry entry feed-id]
  (let [get-content-value (fn [^SyndContent c] (when c (.getValue c)))
        contents (some-> entry .getContents first)
        description (.getDescription entry)]
    {:feed-id feed-id
     :item-id (or (.getUri entry) (.getLink entry))
     :title (.getTitle entry)
     :link (.getLink entry)
     :published-at (or (.getPublishedDate entry)
                       (.getUpdatedDate entry))
     :content (or (get-content-value contents)
                  (get-content-value description))
     :categories (mapv #(.getName ^SyndCategory %) (.getCategories entry))}))

(defn fetch-items
  "Fetch all new items for a feed, normalized.
   Takes a Feed map with :feed-id and :url keys."
  [{:keys [feed-id url]}]
  (map #(entry->item % feed-id)
       (.getEntries ^SyndFeed (fetch-feed url))))
