(ns derztunes.cli-test
  (:require [clojure.test :refer [deftest is]]
            [derztunes.cli :as cli]))

(deftest test-parse-flags
  (is (= (cli/parse-flags ["-conf" "derztunes.edn" "-sync"] {})
         {"-conf" "derztunes.edn" "-sync" true})))
