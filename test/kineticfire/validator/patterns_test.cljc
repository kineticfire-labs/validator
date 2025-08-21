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


(ns kineticfire.validator.patterns-test
  (:require [clojure.test :refer :all]
            [kineticfire.validator.patterns :as patterns]
            [kineticfire.collections.collection :as coll]))


(deftest matches-whole?-test
  (let [digits (re-pattern "[0-9]+$")
        letters (re-pattern "[a-zA-Z]+$")
        alnum (re-pattern "[a-zA-Z0-9]+$")]
    (testing "one pattern, pass"
      (is (= (patterns/matches-whole? digits "123") true)))
    (testing "one pattern, fail"
      (is (= (patterns/matches-whole? digits "123a") false)))
    (testing "one pattern collection, pass"
      (is (= (patterns/matches-whole? [digits] "123") true)))
    (testing "one pattern collection, fail"
      (is (= (patterns/matches-whole? [digits] "123abc") false)))
    (testing "two patterns, pass"
      (is (= (patterns/matches-whole? [digits alnum] "123abc") false)))
    (testing "two patterns, one fail"
      (is (= (patterns/matches-whole? [digits letters] "123abc") false)))))


(deftest matches-whole-yes-test
  (let [digits  (re-pattern "[0-9]+$")
        letters (re-pattern "[a-zA-Z]+$")
        alnum   (re-pattern "[a-zA-Z0-9]+$")]
    (testing "one pattern, match"
      (let [actual     (patterns/matches-whole-yes digits "123")
            actual-str (mapv #(.pattern %) actual)]
        (is (= 1 (count actual)))
        (is (true? (coll/contains-value? actual-str (.pattern digits))))))
    (testing "one pattern, no match"
      (let [actual (patterns/matches-whole-yes digits "abc")]
        (is (= 0 (count actual)))))
    (testing "one pattern as collection, match"
      (let [actual     (patterns/matches-whole-yes [digits] "123")
            actual-str (mapv #(.pattern %) actual)]
        (is (= 1 (count actual)))
        (is (true? (coll/contains-value? actual-str (.pattern digits))))))
    (testing "one pattern as collection, no match"
      (let [actual (patterns/matches-whole-yes [digits] "abc")]
        (is (= 0 (count actual)))))
    (testing "two patterns, one match"
      (let [actual     (patterns/matches-whole-yes [digits letters] "123")
            actual-str (mapv #(.pattern %) actual)]
        (is (= 1 (count actual)))
        (is (true? (coll/contains-value? actual-str (.pattern digits))))
        (is (false? (coll/contains-value? actual-str (.pattern letters))))))
    (testing "two patterns, two match"
      (let [actual     (patterns/matches-whole-yes [digits alnum] "123")
            actual-str (mapv #(.pattern %) actual)]
        (is (= 2 (count actual)))
        (is (true? (coll/contains-value? actual-str (.pattern digits))))
        (is (true? (coll/contains-value? actual-str (.pattern alnum))))))))


(deftest matches-whole-not-test
  (let [digits (re-pattern "[0-9]+$")
        digits2 (re-pattern "[0-9]+$")
        letters (re-pattern "[a-zA-Z]+$")]
    (testing "one pattern, one match"
      (let [actual (patterns/matches-whole-not digits "123")]
        (is (= (count actual) 0))))
    (testing "one pattern, no match"
      (let [actual (patterns/matches-whole-not digits "abc")
            actual-str (mapv #(.pattern %) actual)]
        (is (= (count actual) 1))
        (is (true? (coll/contains-value? actual-str (.pattern digits))))))
    (testing "one pattern as collection, one match"
      (let [actual (patterns/matches-whole-not [digits] "123")]
        (is (= (count actual) 0))))
    (testing "one pattern as collection, no match"
      (let [actual (patterns/matches-whole-not [digits] "abc")
            actual-str (mapv #(.pattern %) actual)]
        (is (= (count actual) 1))
        (is (true? (coll/contains-value? actual-str (.pattern digits))))))
    (testing "two patterns, one no match"
      (let [actual (patterns/matches-whole-not [digits letters] "123")
            actual-str (mapv #(.pattern %) actual)]
        (is (= (count actual) 1))
        (is (true? (coll/contains-value? actual-str (.pattern letters))))))
    (testing "two patterns, two no match"
      (let [actual (patterns/matches-whole-not [digits digits2] "abc")
            actual-str (mapv #(.pattern %) actual)]
        (is (= (count actual) 2))
        (is (true? (coll/contains-value? actual-str (.pattern digits))))
        (is (true? (coll/contains-value? actual-str (.pattern digits2))))))))


(deftest matches-substr?-test
  (let [digits (re-pattern "[0-9]+")
        letters (re-pattern "[a-zA-Z]+")]
    (testing "one pattern, pass"
      (is (= (patterns/matches-substr? digits "ab123cd") true)))
    (testing "one pattern, fail"
      (is (= (patterns/matches-substr? digits "abc") false)))
    (testing "one pattern collection, pass"
      (is (= (patterns/matches-substr? [digits] "ab123cd") true)))
    (testing "one pattern collection, fail"
      (is (= (patterns/matches-substr? [digits] "abc") false)))
    (testing "two patterns, pass"
      (is (= (patterns/matches-substr? [digits letters] "123abc") true)))
    (testing "two patterns, one fail"
      (is (= (patterns/matches-substr? [digits letters] "123") false)))))


(deftest matches-substr-yes-test
  (let [digits  (re-pattern "[0-9]+")
        letters (re-pattern "[a-zA-Z]+")]
    (testing "one pattern, match"
      (let [actual     (patterns/matches-substr-yes digits "ab123cd")
            actual-str (mapv #(.pattern %) actual)]
        (is (= 1 (count actual)))
        (is (true? (coll/contains-value? actual-str (.pattern digits))))))
    (testing "one pattern, no match"
      (let [actual (patterns/matches-substr-yes digits "abc")]
        (is (= 0 (count actual)))))
    (testing "one pattern as collection, match"
      (let [actual     (patterns/matches-substr-yes [digits] "ab123cd")
            actual-str (mapv #(.pattern %) actual)]
        (is (= 1 (count actual)))
        (is (true? (coll/contains-value? actual-str (.pattern digits))))))
    (testing "one pattern as collection, no match"
      (let [actual (patterns/matches-substr-yes [digits] "abc")]
        (is (= 0 (count actual)))))
    (testing "two patterns, one match"
      (let [actual     (patterns/matches-substr-yes [digits letters] "123")
            actual-str (mapv #(.pattern %) actual)]
        (is (= 1 (count actual)))
        (is (true? (coll/contains-value? actual-str (.pattern digits))))
        (is (false? (coll/contains-value? actual-str (.pattern letters))))))
    (testing "two patterns, two match"
      (let [actual     (patterns/matches-substr-yes [digits letters] "abc123")
            actual-str (mapv #(.pattern %) actual)]
        (is (= 2 (count actual)))
        (is (true? (coll/contains-value? actual-str (.pattern digits))))
        (is (true? (coll/contains-value? actual-str (.pattern letters))))))))


(deftest matches-substr-test
  (let [digits (re-pattern "[0-9]+")
        digits2 (re-pattern "[0-9]+")
        letters (re-pattern "[a-zA-Z]+")]
    (testing "one pattern, match"
      (let [actual (patterns/matches-substr-not digits "ab123cd")]
        (is (= (count actual) 0))))
    (testing "one pattern, no match"
      (let [actual (patterns/matches-substr-not digits "abc")
            actual-str (mapv #(.pattern %) actual)]
        (is (= (count actual) 1))
        (is (true? (coll/contains-value? actual-str (.pattern digits))))))
    (testing "one pattern as collection, match"
      (let [actual (patterns/matches-substr-not [digits] "123")]
        (is (= (count actual) 0))))
    (testing "one pattern as collection, no match"
      (let [actual (patterns/matches-substr-not [digits] "abc")
            actual-str (mapv #(.pattern %) actual)]
        (is (= (count actual) 1))
        (is (true? (coll/contains-value? actual-str (.pattern digits))))))
    (testing "two patterns, one match"
      (let [actual (patterns/matches-substr-not [digits letters] "123")
            actual-str (mapv #(.pattern %) actual)]
        (is (= (count actual) 1))
        (is (true? (coll/contains-value? actual-str (.pattern letters))))))
    (testing "two patterns, two matches"
      (let [actual (patterns/matches-substr-not [digits digits2] "abc")
            actual-str (mapv #(.pattern %) actual)]
        (is (= (count actual) 2))
        (is (true? (coll/contains-value? actual-str (.pattern digits))))
        (is (true? (coll/contains-value? actual-str (.pattern digits2))))))))