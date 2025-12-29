(ns alert-scout.storage-test
  (:require [clojure.test :refer :all]
            [alert-scout.storage :as storage]
            [alert-scout.schemas :as schemas]
            [clojure.java.io :as io])
  (:import (java.util Date)))

(def test-feeds-path "test-resources/test-feeds.edn")
(def test-users-path "test-resources/test-users.edn")
(def test-rules-path "test-resources/test-rules.edn")

(defn create-test-dir []
  (.mkdirs (io/file "test-resources")))

(defn cleanup-test-files []
  (doseq [path [test-feeds-path test-users-path test-rules-path]]
    (when (.exists (io/file path))
      (io/delete-file path))))

(use-fixtures :each
  (fn [f]
    (create-test-dir)
    (cleanup-test-files)
    (f)
    (cleanup-test-files)))

(deftest test-load-feeds-validation
  (testing "Valid feeds load successfully"
    (spit test-feeds-path "[{:feed-id \"hn\" :url \"https://news.ycombinator.com/rss\"}]")
    (let [feeds (storage/load-feeds test-feeds-path)]
      (is (= 1 (count feeds)))
      (is (= "hn" (:feed-id (first feeds))))))

  (testing "Invalid feeds throw validation error"
    (spit test-feeds-path "[{:feed-id \"\" :url \"\"}]")
    (is (thrown-with-msg? Exception #"Invalid feeds"
                          (storage/load-feeds test-feeds-path))))

  (testing "Empty file returns empty vector"
    (spit test-feeds-path "[]")
    (is (= [] (storage/load-feeds test-feeds-path)))))

(deftest test-add-feed
  (testing "Add valid feed to empty file"
    (spit test-feeds-path "[]")
    (let [updated (storage/add-feed! test-feeds-path "test" "https://example.com/rss")]
      (is (= 1 (count updated)))
      (is (= "test" (:feed-id (first updated))))))

  (testing "Add feed to existing feeds"
    (spit test-feeds-path "[{:feed-id \"hn\" :url \"https://news.ycombinator.com/rss\"}]")
    (let [updated (storage/add-feed! test-feeds-path "blog" "https://blog.example.com/rss")]
      (is (= 2 (count updated)))
      (is (some #(= "blog" (:feed-id %)) updated))))

  (testing "Adding invalid feed throws error"
    (spit test-feeds-path "[]")
    (is (thrown? Exception
                 (storage/add-feed! test-feeds-path "" "")))))

(deftest test-remove-feed
  (testing "Remove existing feed"
    (spit test-feeds-path "[{:feed-id \"hn\" :url \"https://news.ycombinator.com/rss\"}
                            {:feed-id \"blog\" :url \"https://blog.example.com/rss\"}]")
    (let [updated (storage/remove-feed! test-feeds-path "hn")]
      (is (= 1 (count updated)))
      (is (= "blog" (:feed-id (first updated))))))

  (testing "Remove non-existent feed returns unchanged list"
    (spit test-feeds-path "[{:feed-id \"hn\" :url \"https://news.ycombinator.com/rss\"}]")
    (let [updated (storage/remove-feed! test-feeds-path "nonexistent")]
      (is (= 1 (count updated)))
      (is (= "hn" (:feed-id (first updated)))))))

(deftest test-get-feed
  (testing "Get existing feed"
    (spit test-feeds-path "[{:feed-id \"hn\" :url \"https://news.ycombinator.com/rss\"}
                            {:feed-id \"blog\" :url \"https://blog.example.com/rss\"}]")
    (let [feed (storage/get-feed test-feeds-path "blog")]
      (is (= "blog" (:feed-id feed)))
      (is (= "https://blog.example.com/rss" (:url feed)))))

  (testing "Get non-existent feed returns nil"
    (spit test-feeds-path "[{:feed-id \"hn\" :url \"https://news.ycombinator.com/rss\"}]")
    (is (nil? (storage/get-feed test-feeds-path "nonexistent")))))

(deftest test-load-users-validation
  (testing "Valid users load successfully"
    (spit test-users-path "[{:id \"alice\" :email \"alice@example.com\"}]")
    (let [users (storage/load-users test-users-path)]
      (is (= 1 (count users)))
      (is (= "alice" (:id (first users))))))

  (testing "Invalid email throws validation error"
    (spit test-users-path "[{:id \"alice\" :email \"not-an-email\"}]")
    (is (thrown-with-msg? Exception #"Invalid users"
                          (storage/load-users test-users-path)))))

(deftest test-load-rules-validation
  (testing "Valid rules load successfully"
    (spit test-rules-path "[{:id \"test-rule\" :user-id \"alice\" :must [\"rails\"]}]")
    (let [rules (storage/load-rules test-rules-path)]
      (is (= 1 (count rules)))
      (is (= "test-rule" (:id (first rules))))))

  (testing "Invalid rule throws validation error"
    (spit test-rules-path "[{:id \"\" :user-id \"alice\"}]")
    (is (thrown-with-msg? Exception #"Invalid rules"
                          (storage/load-rules test-rules-path)))))

(deftest test-checkpoint-management
  (testing "Checkpoint operations"
    (let [checkpoint-path "test-resources/test-checkpoints.edn"]
      (try
        ;; Create empty file first
        (spit checkpoint-path "{}")

        ;; Initialize
        (storage/load-checkpoints! checkpoint-path)

        ;; Initially no checkpoint
        (is (nil? (storage/last-seen "hn")))

        ;; Update checkpoint
        (let [now (Date.)]
          (storage/update-checkpoint! "hn" now checkpoint-path)
          (is (= now (storage/last-seen "hn"))))

        ;; Update another feed
        (let [now2 (Date.)]
          (storage/update-checkpoint! "blog" now2 checkpoint-path)
          (is (= now2 (storage/last-seen "blog"))))

        (finally
          (when (.exists (io/file checkpoint-path))
            (io/delete-file checkpoint-path)))))))
