(ns alert-scout.matcher-test
  (:require [clojure.test :refer :all]
            [alert-scout.matcher :as matcher]))

(deftest test-text
  (testing "Extract and normalize text from feed item"
    (is (= "hello world test content"
           (matcher/text {:title "Hello World"
                         :content "Test Content"})))

    (is (= "just title"
           (matcher/text {:title "Just Title"})))

    (is (= ""
           (matcher/text {:title nil :content nil})))))

(deftest test-contains-term?
  (testing "Case-insensitive term matching"
    (is (matcher/contains-term? "hello world" "hello"))
    (is (matcher/contains-term? "hello world" "WORLD"))
    (is (matcher/contains-term? "Rails API" "rails"))
    (is (not (matcher/contains-term? "hello" "world")))))

(deftest test-match-rule-must
  (testing "Rule with only 'must' clauses"
    (let [rule {:must ["rails" "api"]
                :should []
                :must-not []
                :min-should-match 0}
          item {:title "Building Rails API" :content ""}]
      (is (matcher/match-rule? rule item)))

    (let [rule {:must ["rails" "api"]
                :should []
                :must-not []
                :min-should-match 0}
          item {:title "Building Rails" :content ""}]
      (is (not (matcher/match-rule? rule item))))))

(deftest test-match-rule-must-not
  (testing "Rule with 'must-not' clauses"
    (let [rule {:must ["rails"]
                :should []
                :must-not ["test"]
                :min-should-match 0}
          item {:title "Rails Production Deploy" :content ""}]
      (is (matcher/match-rule? rule item)))

    (let [rule {:must ["rails"]
                :should []
                :must-not ["test"]
                :min-should-match 0}
          item {:title "Rails Testing Guide" :content ""}]
      (is (not (matcher/match-rule? rule item))))))

(deftest test-match-rule-should
  (testing "Rule with 'should' and 'min-should-match'"
    (let [rule {:must []
                :should ["docker" "kamal" "deploy"]
                :must-not []
                :min-should-match 2}
          item {:title "Deploy with Kamal and Docker" :content ""}]
      (is (matcher/match-rule? rule item)))

    ;; Only has 1 match (docker), needs 2
    (let [rule {:must []
                :should ["docker" "kamal" "rails"]
                :must-not []
                :min-should-match 2}
          item {:title "Building with Docker" :content ""}]
      (is (not (matcher/match-rule? rule item))))))

(deftest test-match-rule-combined
  (testing "Complex rule with must, should, and must-not"
    (let [rule {:must ["rails"]
                :should ["docker" "kamal" "deploy"]
                :must-not ["test"]
                :min-should-match 1}
          item {:title "Rails Deploy with Docker" :content ""}]
      (is (matcher/match-rule? rule item)))

    ;; Missing 'must' term
    (let [rule {:must ["rails"]
                :should ["docker" "kamal" "deploy"]
                :must-not ["test"]
                :min-should-match 1}
          item {:title "Deploy with Docker" :content ""}]
      (is (not (matcher/match-rule? rule item))))

    ;; Has 'must-not' term
    (let [rule {:must ["rails"]
                :should ["docker" "kamal" "deploy"]
                :must-not ["test"]
                :min-should-match 1}
          item {:title "Rails Deploy Testing" :content ""}]
      (is (not (matcher/match-rule? rule item))))

    ;; Not enough 'should' matches
    (let [rule {:must ["rails"]
                :should ["docker" "kamal" "deploy"]
                :must-not ["test"]
                :min-should-match 1}
          item {:title "Rails Tutorial" :content ""}]
      (is (not (matcher/match-rule? rule item))))))

(deftest test-match-item
  (testing "Generate alerts from matching rules"
    (let [rules-by-user {"kevin" [{:id "rails-api"
                                   :user-id "kevin"
                                   :must ["rails" "api"]
                                   :should []
                                   :must-not ["test"]
                                   :min-should-match 0}]
                         "admin" [{:id "admin-rule"
                                   :user-id "admin"
                                   :must ["admin"]
                                   :should []
                                   :must-not []
                                   :min-should-match 0}]}
          item {:feed-id "hn"
                :title "Building Rails API"
                :content "How to build a Rails API"}]

      (let [alerts (matcher/match-item rules-by-user item)]
        (is (= 1 (count alerts)))
        (is (= "kevin" (:user-id (first alerts))))
        (is (= "rails-api" (:rule-id (first alerts))))
        (is (= item (:item (first alerts)))))))

  (testing "No matches returns empty vector"
    (let [rules-by-user {"kevin" [{:id "python-rule"
                                   :user-id "kevin"
                                   :must ["python"]
                                   :should []
                                   :must-not []
                                   :min-should-match 0}]}
          item {:title "Building Rails API" :content ""}]

      (is (= [] (matcher/match-item rules-by-user item))))))

(deftest test-rules-by-user
  (testing "Group rules by user-id"
    (let [rules [{:id "rule1" :user-id "alice"}
                 {:id "rule2" :user-id "bob"}
                 {:id "rule3" :user-id "alice"}]
          grouped (matcher/rules-by-user rules)]

      (is (= 2 (count grouped)))
      (is (= 2 (count (get grouped "alice"))))
      (is (= 1 (count (get grouped "bob")))))))
