(ns derztunes.cli-test
  (:require [clojure.test :refer [deftest is testing]]
            [derztunes.cli :as cli]))

(deftest test-parse-flags
  (testing "Flags with values"
    (is (= (cli/parse-flags ["-conf" "derztunes.edn"])
           {"-conf" "derztunes.edn"})))
  (testing "Flags without values"
    (is (= (cli/parse-flags ["-migrate"])
           {"-migrate" true}))
    (is (= (cli/parse-flags ["-migrate" "-sync"])
           {"-migrate" true "-sync" true})))
  (testing "Flags with and without values"
    (is (= (cli/parse-flags ["-migrate" "-conf" "derztunes.edn"])
           {"-migrate" true "-conf" "derztunes.edn"}))
    (is (= (cli/parse-flags ["-conf" "derztunes.edn" "-migrate"])
           {"-conf" "derztunes.edn" "-migrate" true}))
    (is (= (cli/parse-flags ["-migrate" "-conf" "derztunes.edn" "-sync"])
           {"-migrate" true "-conf" "derztunes.edn" "-sync" true}))))
