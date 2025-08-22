;; (c) Copyright 2024-2025 checks Contributors. All rights reserved.
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


(ns kineticfire.validator.checks.basic-test
  (:require [clojure.test :refer :all]
            [kineticfire.validator.checks.basic :as basic])
  (:import (java.util.regex Pattern)))


;; ---------------------------------------------------------------------------
;; string? predicate
;; ---------------------------------------------------------------------------


(deftest string?-nil-handling
  (testing "nil is invalid by default"
    (is (false? (basic/string? nil))))
  (testing "nil is valid when :nil-ok true"
    (is (true? (basic/string? nil {:nil-ok true})))))


(deftest string?-type-check
  (testing "non-string returns false regardless of :nil-ok"
    (is (false? (basic/string? 42)))
    (is (false? (basic/string? 42 {:nil-ok true})))))


(deftest string?-length-bounds
  (testing ":min enforces minimum length"
    (is (false? (basic/string? "ab" {:min 3})))
    (is (true? (basic/string? "abc" {:min 3}))))
  (testing ":max enforces maximum length"
    (is (true? (basic/string? "abc" {:max 3})))
    (is (false? (basic/string? "abcd" {:max 3})))))


(deftest string?-regex-whole
  (let [alpha (re-pattern "^[A-Za-z]+$")]
    (testing "whole-string pattern must match (single)"
      (is (true? (basic/string? "abc" {:pat-or-pats-whole alpha})))
      (is (false? (basic/string? "abc123" {:pat-or-pats-whole alpha}))))
    (testing "whole-string patterns must all match (collection)"
      (let [alpha-num (re-pattern "^[A-Za-z0-9]+$")]
        (is (true? (basic/string? "abc123" {:pat-or-pats-whole [alpha-num]})))
        (is (false? (basic/string? "abc123" {:pat-or-pats-whole [alpha]})))))))


(deftest string?-regex-substr
  (let [digits (re-pattern "[0-9]+")
        letters (re-pattern "[A-Za-z]+")]
    (testing "substring pattern must be found"
      (is (true? (basic/string? "a123b" {:pat-or-pats-sub digits})))
      (is (false? (basic/string? "----" {:pat-or-pats-sub digits}))))
    (testing "all substring patterns must be found (collection)"
      (is (true? (basic/string? "abc123" {:pat-or-pats-sub [digits letters]})))
      (is (false? (basic/string? "123---" {:pat-or-pats-sub [digits letters]}))))))


(deftest string?-custom-predicates
  (testing "single predicate"
    (is (true? (basic/string? "abc" {:fn-or-fns #(>= (count %) 3)})))
    (is (false? (basic/string? "ab" {:fn-or-fns #(>= (count %) 3)}))))
  (testing "collection of predicates (all must pass)"
    (is (true?
          (basic/string? "abc"
                         {:fn-or-fns [#(>= (count %) 3)
                                      #(re-find #"[a-c]" %)]})))
    (is (false?
          (basic/string? "ab"
                         {:fn-or-fns [#(>= (count %) 3)
                                      #(re-find #"[a-c]" %)]})))))


;; ---------------------------------------------------------------------------
;; string-explain (first failure)
;; ---------------------------------------------------------------------------

(deftest string-explain-nil-handling
  (testing "nil without :nil-ok -> error"
    (let [res (basic/string-explain nil)]
      (is (false? (:valid? res)))
      (is (= :string/nil (:code res)))
      (is (= "Value is nil." (:message res)))
      (is (nil? (:value res)))))
  (testing "nil with :nil-ok -> valid"
    (is (= {:valid? true :value nil}
           (basic/string-explain nil {:nil-ok true})))))


(deftest string-explain-type-check
  (let [res (basic/string-explain 42)]
    (is (false? (:valid? res)))
    (is (= :type/not-string (:code res)))
    (is (string? (:message res)))                           ; message includes type info
    (is (= 42 (:value res)))))


(deftest string-explain-length
  (testing "too short"
    (let [res (basic/string-explain "ab" {:min 3})]
      (is (false? (:valid? res)))
      (is (= :string/too-short (:code res)))
      (is (= {:min 3} (:expected res)))))
  (testing "too long"
    (let [res (basic/string-explain "abcd" {:max 3})]
      (is (false? (:valid? res)))
      (is (= :string/too-long (:code res)))
      (is (= {:max 3} (:expected res))))))


(deftest string-explain-regex-whole
  (let [alpha (re-pattern "^[A-Za-z]+$")
        res (basic/string-explain "abc123" {:pat-or-pats-whole alpha})]
    (is (false? (:valid? res)))
    (is (= :string/regex-whole-failed (:code res)))
    (is (= "Whole-string pattern(s) failed." (:message res)))
    ;; expected contains the pattern strings
    (is (= [(.pattern alpha)]
           (get-in res [:expected :regex])))))


(deftest string-explain-regex-substr
  (let [digits (re-pattern "[0-9]+")
        res (basic/string-explain "----" {:pat-or-pats-sub digits})]
    (is (false? (:valid? res)))
    (is (= :string/regex-substr-failed (:code res)))
    (is (= "Substring pattern(s) failed." (:message res)))
    (is (= [(.pattern digits)]
           (get-in res [:expected :regex])))))


(deftest string-explain-custom-predicate
  (let [res (basic/string-explain "ab" {:fn-or-fns #(>= (count %) 3)})]
    (is (false? (:valid? res)))
    (is (= :string/predicate-failed (:code res)))
    (is (= "Custom predicate(s) returned false." (:message res)))))

(deftest string-explain-success
  (let [alpha (re-pattern "^[A-Za-z]+$")
        res (basic/string-explain "Abc"
                                  {:min               1
                                   :max               5
                                   :pat-or-pats-whole alpha
                                   :fn-or-fns         #(Character/isUpperCase ^char (.charAt % 0))})]
    (is (= {:valid? true :value "Abc"} res))))


;; -----------------------------------------------------------------------------
;; string-as-keyword?  (boolean predicate)
;; -----------------------------------------------------------------------------

(deftest string-as-keyword?-nil-handling
  (testing "nil invalid by default"
    (is (false? (basic/string-as-keyword? nil))))
  (testing "nil valid when :nil-ok true"
    (is (true? (basic/string-as-keyword? nil {:nil-ok true})))))


(deftest string-as-keyword?-type-and-length
  (testing "non-string -> false"
    (is (false? (basic/string-as-keyword? 42))))
  (testing "min defaults to 1 (empty string invalid)"
    (is (false? (basic/string-as-keyword? "")))
    (is (true?  (basic/string-as-keyword? "a"))))
  (testing ":min is clamped to at least 1"
    (is (false? (basic/string-as-keyword? "" {:min 0})))
    (is (true?  (basic/string-as-keyword? "a" {:min 0}))))
  (testing ":max works via base string checks"
    (is (true?  (basic/string-as-keyword? "ab" {:max 2})))
    (is (false? (basic/string-as-keyword? "abc" {:max 2})))))


(deftest string-as-keyword?-regex-rules
  (testing "must start with a letter"
    (is (false? (basic/string-as-keyword? "1abc")))
    (is (true?  (basic/string-as-keyword? "a1bc"))))
  (testing "allows letters, digits, underscore, and hyphen after first char"
    (is (true?  (basic/string-as-keyword? "Abc_123-xyz")))
    (is (true?  (basic/string-as-keyword? "z-9_"))))
  (testing "disallows colon, slash, and spaces"
    (is (false? (basic/string-as-keyword? "ab:c")))
    (is (false? (basic/string-as-keyword? "ab/c")))
    (is (false? (basic/string-as-keyword? "ab c")))))


;; -----------------------------------------------------------------------------
;; string-as-keyword-explain (first failure)
;; -----------------------------------------------------------------------------

(deftest string-as-keyword-explain-nil-handling
  (testing "nil without :nil-ok -> error"
    (let [res (basic/string-as-keyword-explain nil)]
      (is (false? (:valid? res)))
      (is (= :string/nil (:code res)))
      (is (= "Value is nil." (:message res)))
      (is (nil? (:value res)))))
  (testing "nil with :nil-ok -> valid"
    (is (= {:valid? true :value nil}
           (basic/string-as-keyword-explain nil {:nil-ok true})))))


(deftest string-as-keyword-explain-type-and-length
  (testing "non-string -> type error"
    (let [res (basic/string-as-keyword-explain 42)]
      (is (false? (:valid? res)))
      (is (= :type/not-string (:code res)))
      (is (string? (:message res)))))
  (testing "min defaults to 1 (empty string -> too-short)"
    (let [res (basic/string-as-keyword-explain "")]
      (is (false? (:valid? res)))
      (is (= :string/too-short (:code res)))
      (is (= {:min 1} (:expected res))))))


(deftest string-as-keyword-explain-regex
  (testing "not keyword-safe (starts with digit)"
    (let [res (basic/string-as-keyword-explain "1abc")]
      (is (false? (:valid? res)))
      (is (= :string/not-keyword-safe (:code res)))
      (is (re-find #"Not keyword-safe" (:message res)))
      ;; expected contains the keyword-safe regex pattern string
      (is (= [(.pattern basic/valid-string-as-keyword-pattern)]
             (get-in res [:expected :regex])))))
  (testing "keyword-safe passes"
    (is (= {:valid? true :value "Abc_123"}
           (basic/string-as-keyword-explain "Abc_123")))))


(deftest string-as-keyword-explain-respects-max-and-fns
  (testing ":max error bubbles from base string checks"
    (let [res (basic/string-as-keyword-explain "abcd" {:max 3})]
      (is (false? (:valid? res)))
      (is (= :string/too-long (:code res)))))
  (testing "custom predicate failure returned"
    (let [res (basic/string-as-keyword-explain "ab"
                                               {:fn-or-fns #(>= (count %) 3)})]
      (is (false? (:valid? res)))
      (is (= :string/predicate-failed (:code res))))))


;; -----------------------------------------------------------------------------
;; number? predicate
;; -----------------------------------------------------------------------------

(deftest number?-nil-handling
  (testing "nil is invalid by default"
    (is (false? (basic/number? nil))))
  (testing "nil is valid when :nil-ok true"
    (is (true? (basic/number? nil {:nil-ok true})))))


(deftest number?-type-check
  (testing "non-number returns false"
    (is (false? (basic/number? "42")))))


(deftest number?-type-option
  (testing ":int matches integers (Long/BigInt)"
    (is (true?  (basic/number? 1 {:type :int})))
    (is (true?  (basic/number? 1N {:type :int})))
    (is (false? (basic/number? 1.0 {:type :int}))))
  (testing ":float matches java.lang.Float only"
    (is (true?  (basic/number? (float 1.0) {:type :float})))
    (is (false? (basic/number? 1.0  {:type :float}))))
  (testing ":double matches Double"
    (is (true?  (basic/number? 1.0 {:type :double})))
    (is (false? (basic/number? (float 1.0) {:type :double}))))
  (testing ":decimal matches BigDecimal"
    (is (true?  (basic/number? (bigdec "1.0") {:type :decimal})))
    (is (false? (basic/number? 1.0 {:type :decimal}))))
  (testing ":ratio matches clojure.lang.Ratio"
    (is (true?  (basic/number? 22/7 {:type :ratio})))
    (is (false? (basic/number? 3 {:type :ratio})))))


(deftest number?-min-max
  (testing ":min is inclusive"
    (is (true?  (basic/number? 10 {:min 10})))
    (is (false? (basic/number? 9  {:min 10}))))
  (testing ":max is inclusive"
    (is (true?  (basic/number? 10 {:max 10})))
    (is (false? (basic/number? 11 {:max 10}))))
  (testing "both :min and :max"
    (is (true?  (basic/number? 5 {:min 1 :max 10})))
    (is (false? (basic/number? 0 {:min 1 :max 10})))
    (is (false? (basic/number? 11 {:min 1 :max 10})))))


(deftest number?-custom-predicates
  (testing "single predicate"
    (is (true?  (basic/number? 4 {:fn-or-fns #(even? %)})))
    (is (false? (basic/number? 5 {:fn-or-fns #(even? %)}))))
  (testing "collection of predicates (all must pass)"
    (is (true?
          (basic/number? 8 {:fn-or-fns [#(pos? %)
                                        #(zero? (mod % 2))]})))
    (is (false?
          (basic/number? -2 {:fn-or-fns [#(pos? %)
                                         #(zero? (mod % 2))]})))))


;; -----------------------------------------------------------------------------
;; number-explain (first failure)
;; -----------------------------------------------------------------------------

(deftest number-explain-nil-handling
  (testing "nil without :nil-ok -> error"
    (let [res (basic/number-explain nil)]
      (is (false? (:valid? res)))
      (is (= :number/nil (:code res)))
      (is (= "Value is nil." (:message res)))
      (is (nil? (:value res)))))
  (testing "nil with :nil-ok -> valid"
    (is (= {:valid? true :value nil}
           (basic/number-explain nil {:nil-ok true})))))


(deftest number-explain-type-check
  (let [res (basic/number-explain "42")]
    (is (false? (:valid? res)))
    (is (= :type/not-number (:code res)))
    (is (string? (:message res)))
    (is (= "42" (:value res)))))


(deftest number-explain-type-option
  (testing "wrong requested type -> error with expected"
    (let [res (basic/number-explain 1.0 {:type :int})]
      (is (false? (:valid? res)))
      (is (= :number/wrong-type (:code res)))
      (is (= {:type :int} (:expected res)))))
  (testing "correct requested type -> ok"
    (is (= {:valid? true :value 1.0}
           (basic/number-explain 1.0 {:type :double})))))


(deftest number-explain-min-max
  (testing "too small -> :number/too-small"
    (let [res (basic/number-explain 9 {:min 10})]
      (is (false? (:valid? res)))
      (is (= :number/too-small (:code res)))
      (is (= {:min 10} (:expected res)))))
  (testing "too large -> :number/too-large"
    (let [res (basic/number-explain 11 {:max 10})]
      (is (false? (:valid? res)))
      (is (= :number/too-large (:code res)))
      (is (= {:max 10} (:expected res)))))
  (testing "bounds inclusive -> ok"
    (is (= {:valid? true :value 10}
           (basic/number-explain 10 {:min 10 :max 10})))))


(deftest number-explain-custom-predicate
  (testing "predicate failure reported"
    (let [res (basic/number-explain 5 {:fn-or-fns #(even? %)})]
      (is (false? (:valid? res)))
      (is (= :number/predicate-failed (:code res)))
      (is (= "Custom predicate(s) returned false." (:message res)))))
  (testing "success"
    (is (= {:valid? true :value 6}
           (basic/number-explain 6 {:fn-or-fns [#(pos? %) #(zero? (mod % 3))]})))))


;; ---------------------------------------------------------------------------
;; collection? predicate
;; ---------------------------------------------------------------------------

(deftest collection?-nil-handling
  (testing "nil is invalid by default"
    (is (false? (basic/collection? nil))))
  (testing "nil is valid when :nil-ok true"
    (is (true? (basic/collection? nil {:nil-ok true})))))


(deftest collection?-type-check
  (testing "non-collection returns false"
    (is (false? (basic/collection? 42)))
    (is (false? (basic/collection? "string")))
    (is (false? (basic/collection? true))))
  (testing "collections return true"
    (is (true? (basic/collection? [])))
    (is (true? (basic/collection? '())))
    (is (true? (basic/collection? #{})))
    (is (true? (basic/collection? {})))))


(deftest collection?-type-constraint
  (testing ":vec type constraint"
    (is (true? (basic/collection? [1 2 3] {:type :vec})))
    (is (false? (basic/collection? '(1 2 3) {:type :vec}))))
  (testing ":list type constraint"
    (is (true? (basic/collection? '(1 2 3) {:type :list})))
    (is (false? (basic/collection? [1 2 3] {:type :list}))))
  (testing ":set type constraint"
    (is (true? (basic/collection? #{1 2 3} {:type :set})))
    (is (false? (basic/collection? [1 2 3] {:type :set}))))
  (testing ":map type constraint"
    (is (true? (basic/collection? {:a 1} {:type :map})))
    (is (false? (basic/collection? [1 2 3] {:type :map}))))
  (testing ":seq type constraint"
    (is (true? (basic/collection? [1 2 3] {:type :seq})))
    (is (true? (basic/collection? '(1 2 3) {:type :seq})))
    (is (false? (basic/collection? #{1 2 3} {:type :seq}))))
  (testing ":assoc type constraint"
    (is (true? (basic/collection? {:a 1} {:type :assoc})))
    (is (true? (basic/collection? [1 2 3] {:type :assoc})))
    (is (false? (basic/collection? '(1 2 3) {:type :assoc})))))


(deftest collection?-count-bounds
  (testing ":min enforces minimum count"
    (is (false? (basic/collection? [1 2] {:min 3})))
    (is (true? (basic/collection? [1 2 3] {:min 3}))))
  (testing ":max enforces maximum count"
    (is (true? (basic/collection? [1 2 3] {:max 3})))
    (is (false? (basic/collection? [1 2 3 4] {:max 3}))))
  (testing "empty collections and :min 0"
    (is (true? (basic/collection? [] {:min 0})))
    (is (false? (basic/collection? [] {:min 1})))))


(deftest collection?-duplicates
  (testing "duplicates allowed by default"
    (is (true? (basic/collection? [1 2 2 3])))
    (is (true? (basic/collection? [1 2 2 3] {:duplicates-ok true}))))
  (testing "duplicates forbidden when :duplicates-ok false"
    (is (false? (basic/collection? [1 2 2 3] {:duplicates-ok false})))
    (is (true? (basic/collection? [1 2 3 4] {:duplicates-ok false}))))
  (testing "maps not checked for duplicates"
    (is (true? (basic/collection? {:a 1 :b 1} {:duplicates-ok false}))))
  (testing "sets cannot have duplicates"
    (is (true? (basic/collection? #{1 2 3} {:duplicates-ok false})))))


(deftest collection?-nil-values
  (testing "nil values allowed by default"
    (is (true? (basic/collection? [1 nil 3])))
    (is (true? (basic/collection? [1 nil 3] {:nil-value-ok true}))))
  (testing "nil values forbidden when :nil-value-ok false"
    (is (false? (basic/collection? [1 nil 3] {:nil-value-ok false})))
    (is (true? (basic/collection? [1 2 3] {:nil-value-ok false}))))
  (testing "nil values in maps"
    (is (false? (basic/collection? {:a nil} {:nil-value-ok false})))
    (is (false? (basic/collection? {nil :b} {:nil-value-ok false})))
    (is (true? (basic/collection? {:a 1} {:nil-value-ok false})))))


(deftest collection?-custom-predicates
  (testing "single predicate"
    (is (true? (basic/collection? [2 4 6] {:fn-or-fns #(every? even? %)})))
    (is (false? (basic/collection? [1 2 3] {:fn-or-fns #(every? even? %)}))))
  (testing "multiple predicates"
    (is (true? (basic/collection? [2 4 6] {:fn-or-fns [#(every? even? %) #(> (count %) 2)]})))
    (is (false? (basic/collection? [2 4] {:fn-or-fns [#(every? even? %) #(> (count %) 2)]})))))


;; ---------------------------------------------------------------------------
;; collection-explain
;; ---------------------------------------------------------------------------

(deftest collection-explain-nil-handling
  (testing "nil invalid by default"
    (let [res (basic/collection-explain nil)]
      (is (false? (:valid? res)))
      (is (= :collection/nil (:code res)))
      (is (= "Value is nil." (:message res)))))
  (testing "nil valid when allowed"
    (is (= {:valid? true :value nil}
           (basic/collection-explain nil {:nil-ok true})))))


(deftest collection-explain-type-check
  (testing "non-collection type error"
    (let [res (basic/collection-explain 42)]
      (is (false? (:valid? res)))
      (is (= :type/not-collection (:code res)))
      (is (re-find #"Expected collection" (:message res))))))


(deftest collection-explain-type-constraint
  (testing "wrong type error"
    (let [res (basic/collection-explain [1 2 3] {:type :map})]
      (is (false? (:valid? res)))
      (is (= :collection/wrong-type (:code res)))
      (is (= "Collection is not of requested type :map." (:message res)))
      (is (= {:type :map} (:expected res))))))


(deftest collection-explain-count-bounds
  (testing "too small error"
    (let [res (basic/collection-explain [1 2] {:min 3})]
      (is (false? (:valid? res)))
      (is (= :collection/too-small (:code res)))
      (is (= "Collection smaller than min count 3." (:message res)))
      (is (= {:min 3} (:expected res)))))
  (testing "too large error"
    (let [res (basic/collection-explain [1 2 3 4] {:max 3})]
      (is (false? (:valid? res)))
      (is (= :collection/too-large (:code res)))
      (is (= "Collection larger than max count 3." (:message res)))
      (is (= {:max 3} (:expected res))))))


(deftest collection-explain-duplicates
  (testing "duplicates found error"
    (let [res (basic/collection-explain [1 2 2 3] {:duplicates-ok false})]
      (is (false? (:valid? res)))
      (is (= :collection/duplicates-found (:code res)))
      (is (= "Collection contains duplicate values." (:message res))))))


(deftest collection-explain-nil-values
  (testing "nil values found error"
    (let [res (basic/collection-explain [1 nil 3] {:nil-value-ok false})]
      (is (false? (:valid? res)))
      (is (= :collection/nil-values-found (:code res)))
      (is (= "Collection contains nil values." (:message res))))))


(deftest collection-explain-custom-predicates
  (testing "predicate failure"
    (let [res (basic/collection-explain [1 3 5] {:fn-or-fns #(every? even? %)})]
      (is (false? (:valid? res)))
      (is (= :collection/predicate-failed (:code res)))
      (is (= "Custom predicate(s) returned false." (:message res)))))
  (testing "success"
    (is (= {:valid? true :value [2 4 6]}
           (basic/collection-explain [2 4 6] {:fn-or-fns #(every? even? %)})))))


(deftest collection-explain-success
  (testing "valid collection"
    (is (= {:valid? true :value [1 2 3]}
           (basic/collection-explain [1 2 3]))))
  (testing "valid collection with all constraints"
    (is (= {:valid? true :value [2 4 6]}
           (basic/collection-explain [2 4 6] {:type :vec :min 2 :max 5 :duplicates-ok false :nil-value-ok false})))))
