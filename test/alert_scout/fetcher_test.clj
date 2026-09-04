(ns alert-scout.fetcher-test
  "Tests for RSS/Atom feed fetching with error handling using Remus.

   These tests use mocking to avoid real network requests:
   - No external HTTP calls are made
   - Tests run quickly and reliably
   - Error handling logic is validated without network dependencies"
  (:require [clojure.test :refer :all]
            [alert-scout.fetcher :as fetcher]
            [remus]))

(deftest test-extract-http-status
  (testing "Extracts HTTP status from Remus exception message"
    (let [e (Exception. "Non-200 status code, status: 429, url: https://example.com")
          status (#'fetcher/extract-http-status e)]
      (is (= 429 status))))

  (testing "Extracts HTTP 404 from exception message"
    (let [e (Exception. "Non-200 status code, status: 404, url: https://example.com")
          status (#'fetcher/extract-http-status e)]
      (is (= 404 status))))

  (testing "Extracts HTTP 500 from exception message"
    (let [e (Exception. "Non-200 status code, status: 500, url: https://example.com")
          status (#'fetcher/extract-http-status e)]
      (is (= 500 status))))

  (testing "Returns nil for exception with no status"
    (let [e (Exception. "Connection timeout")
          status (#'fetcher/extract-http-status e)]
      (is (nil? status))))

  (testing "Returns nil for exception with no message"
    (let [e (Exception.)
          status (#'fetcher/extract-http-status e)]
      (is (nil? status)))))

(deftest test-fetch-items-error-handling
  (testing "fetch-items! returns empty list when fetch-feed! returns nil"
    ;; Mock fetch-feed! to return nil (simulating error)
    (with-redefs [fetcher/fetch-feed! (constantly nil)]
      (let [feed {:feed-id "test-feed"
                  :url "https://example.com/rss.xml"}]
        ;; Should return empty list, not throw
        (is (= [] (fetcher/fetch-items! feed)))))))

(deftest test-fetch-feed!-error-handling
  (testing "fetch-feed! returns nil when Remus throws HTTP 429"
    ;; Mock remus/parse-url to throw exception
    (with-redefs [remus/parse-url (fn [_] (throw (Exception. "Non-200 status code, status: 429, url: https://example.com/rss.xml")))]
      (is (nil? (fetcher/fetch-feed! "https://example.com/rss.xml")))))

  (testing "fetch-feed! returns nil when Remus throws HTTP 404"
    (with-redefs [remus/parse-url (fn [_] (throw (Exception. "Non-200 status code, status: 404, url: https://example.com/rss.xml")))]
      (is (nil? (fetcher/fetch-feed! "https://example.com/rss.xml")))))

  (testing "fetch-feed! returns nil when Remus throws HTTP 500"
    (with-redefs [remus/parse-url (fn [_] (throw (Exception. "Non-200 status code, status: 500, url: https://example.com/rss.xml")))]
      (is (nil? (fetcher/fetch-feed! "https://example.com/rss.xml")))))

  (testing "fetch-feed! returns result when Remus succeeds"
    (with-redefs [remus/parse-url (fn [_] {:response {:status 200}
                                           :feed {:entries []}})]
      (let [result (fetcher/fetch-feed! "https://example.com/rss.xml")]
        (is (some? result))
        (is (map? result)))))

  (testing "fetch-feed! catches exceptions and returns nil"
    (with-redefs [remus/parse-url (fn [_] (throw (Exception. "Network timeout")))]
      (is (nil? (fetcher/fetch-feed! "https://example.com/rss.xml"))))))

(deftest test-entry->item-empty-link
  (testing "entry->item returns nil when link is empty string"
    (let [entry {:title "Test"
                 :link ""
                 :uri "https://example.com/item/1"}
          result (fetcher/entry->item entry "test-feed")]
      (is (nil? result))))

  (testing "entry->item returns nil when link is nil"
    (let [entry {:title "Test"
                 :link nil
                 :uri "https://example.com/item/1"}
          result (fetcher/entry->item entry "test-feed")]
      (is (nil? result))))

  (testing "entry->item returns nil when link is missing"
    (let [entry {:title "Test"
                 :uri "https://example.com/item/1"}
          result (fetcher/entry->item entry "test-feed")]
      (is (nil? result)))))

(deftest test-fetch-items-filters-invalid-entries
  (testing "fetch-items! filters out entries with empty links"
    (let [valid-entry {:title "Valid"
                       :link "https://example.com/valid"
                       :published-date #inst "2026-09-04"}
          invalid-entry {:title "Invalid"
                         :link ""
                         :published-date #inst "2026-09-04"}
          mock-feed {:response {:status 200}
                     :feed {:entries [valid-entry invalid-entry]}}]
      (with-redefs [fetcher/fetch-feed! (constantly mock-feed)]
        (let [items (fetcher/fetch-items! {:feed-id "test" :url "https://example.com/rss"})]
          (is (= 1 (count items)))
          (is (= "Valid" (:title (first items)))))))))
