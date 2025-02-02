(ns derztunes.migrate-test
  (:require [clojure.test :refer [deftest is testing]]
            [derztunes.migrate :as migrate]))

(deftest pending-migrations
  (testing "No migrations"
    (let [desired [{:migration/name "001.sql"}]
          applied []]
      (is (= (migrate/pending-migrations desired applied)
             [{:migration/name "001.sql"}]))))
  (testing "Some migrations"
    (let [desired [{:migration/name "001.sql"}
                   {:migration/name "002.sql"}
                   {:migration/name "003.sql"}]
          applied [{:migration/name "001.sql"}]]
      (is (= (migrate/pending-migrations desired applied)
             [{:migration/name "002.sql"}
              {:migration/name "003.sql"}]))))
  (testing "All migrations"
    (let [desired [{:migration/name "001.sql"}
                   {:migration/name "002.sql"}
                   {:migration/name "003.sql"}]
          applied [{:migration/name "001.sql"}
                   {:migration/name "002.sql"}
                   {:migration/name "003.sql"}]]
      (is (= (migrate/pending-migrations desired applied)
             []))))
  (testing "Correct order"
    (let [desired [{:migration/name "003.sql"}
                   {:migration/name "001.sql"}
                   {:migration/name "002.sql"}]
          applied [{:migration/name "001.sql"}]]
      (is (= (migrate/pending-migrations desired applied)
             [{:migration/name "002.sql"}
              {:migration/name "003.sql"}])))))
