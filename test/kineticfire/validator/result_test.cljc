;; (c) Copyright 2024-2025 patterns Contributors. All rights reserved.
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.


;; KineticFire Labs: https://labs.kineticfire.com/
;;	   project site: https://github.com/kineticfire-labs/validator/


(ns kineticfire.validator.result-test
  (:require [clojure.test :refer :all]
            [kineticfire.validator.result :as result]))


(deftest collapse-result-test
  (testing "returns true on valid result"
    (is (= true (result/collapse-result {:valid? true} "ERR"))))
  (testing "returns provided err on invalid result (string)"
    (is (= "Bad input"
           (result/collapse-result {:valid? false :code :x :message "nope"} "Bad input"))))
  (testing "returns provided err on invalid result (map)"
    (let [err {:error "Bad input" :id 42}]
      (is (= err (result/collapse-result {:valid? false} err))))))


(deftest collapse-results-test
  (testing "all valid -> true"
    (is (= true (result/collapse-results
                  [{:valid? true} {:valid? true}] "ERR"))))
  (testing "any invalid -> return provided err"
    (is (= "E1" (result/collapse-results
                  [{:valid? true}
                   {:valid? false :code :oops}
                   {:valid? true}]
                  "E1"))))
  (testing "empty results -> true"
    (is (= true (result/collapse-results [] "ERR")))))


(deftest combine-results-test
  (testing "no failures -> valid? true and empty errors"
    (is (= {:valid? true :errors []}
           (result/combine-results [{:valid? true} {:valid? true}]))))
  (testing "single failure -> valid? false and one error entry"
    (is (= {:valid? false
            :errors [{:code :a :message "A failed"}]}
           (result/combine-results [{:valid? true}
                                    {:valid? false :code :a :message "A failed"}
                                    {:valid? true}]))))
  (testing "multiple failures -> collect all error entries in order"
    (is (= {:valid? false
            :errors [{:code :a :message "A failed"}
                     {:code :b :message "B failed"}]}
           (result/combine-results [{:valid? false :code :a :message "A failed"}
                                    {:valid? true}
                                    {:valid? false :code :b :message "B failed"}])))))


(deftest valid?-test
  (is (true?  (result/valid? {:valid? true})))
  (is (false? (result/valid? {:valid? false})))
  (is (false? (result/valid? {})))) ; defensive: missing flag treated as falsey


(deftest errors-test
  (testing "single valid result -> nil"
    (is (nil? (result/errors {:valid? true}))))
  (testing "single invalid result -> vector with one {:code :message}"
    (is (= [{:code :a :message "A failed"}]
           (result/errors {:valid? false :code :a :message "A failed"}))))
  (testing "collection: mix of valid/invalid -> flattened vector of error maps"
    (is (= [{:code :a :message "A failed"}
            {:code :b :message "B failed"}]
           (vec (result/errors [{:valid? false :code :a :message "A failed"}
                                {:valid? true}
                                {:valid? false :code :b :message "B failed"}])))))
  (testing "collection: all valid -> empty vector"
    (is (= [] (vec (result/errors [{:valid? true} {:valid? true}]))))))