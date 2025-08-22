;; (c) Copyright 2024-2025 validator Contributors. All rights reserved.
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
;;	   Project site: https://github.com/kineticfire-labs/validator/


;; -----------------------------------------------------------------------------
;; Basic Validation Examples
;;
;; A focused demonstration of the basic validation functions from
;; `kineticfire.validator.checks.basic`. This file shows how to:
;;   • validate strings with patterns, length constraints, and custom predicates
;;   • validate keyword-safe strings for naming conventions
;;   • validate numbers with type constraints, bounds, and custom checks
;;   • validate collections with type checking, duplicates, and nil values
;;   • use `result` utilities to collapse or interpret explain-style outputs
;;
;; This file demonstrates only the basic validation checks. For examples of
;; other library components like runner orchestration or result utilities,
;; see the other files in this examples directory.
;;
;; Run from the command line using:
;;   lein basic
;; or explicitly:
;;   lein with-profile +dev run -m kineticfire.validator.examples.basic
;; -----------------------------------------------------------------------------


(ns kineticfire.validator.examples.basic
  (:require
    [kineticfire.validator.checks :as checks]               ;; string?, string-explain, etc.
    [kineticfire.validator.result :as result])              ;; collapse-result
  #?(:clj  (:import (java.util.regex Pattern))
     :cljs (:require [goog.string :as gstring])))


(defn demo-string []
  (println "

=== String validation ===")
  (let [good-string "hello@example.com"
        short-string "hi"
        long-string "this-is-a-very-long-string-that-exceeds-limits"
        non-string 42
        email-pattern #?(:clj (re-pattern "^[^@]+@[^@]+\\.[^@]+$")
                         :cljs (re-pattern "^[^@]+@[^@]+\\.[^@]+$"))
        has-uppercase #?(:clj (re-pattern "[A-Z]")
                         :cljs (re-pattern "[A-Z]"))]

    (println "
Basic string checks:")
    (println "  string? good:" (checks/string? good-string))
    (println "  string? number:" (checks/string? non-string))
    (println "  string? nil (default):" (checks/string? nil))
    (println "  string? nil (allowed):" (checks/string? nil {:nil-ok true}))

    (println "
Length constraints:")
    (println "  min 5:" (checks/string? good-string {:min 5}))
    (println "  min 20:" (checks/string? good-string {:min 20}))
    (println "  max 10:" (checks/string? short-string {:max 10}))
    (println "  max 10:" (checks/string? long-string {:max 10}))

    (println "
Pattern matching:")
    (println "  email pattern (whole):" (checks/string? good-string {:pat-or-pats-whole email-pattern}))
    (println "  email pattern (whole):" (checks/string? "not-an-email" {:pat-or-pats-whole email-pattern}))
    (println "  has uppercase (sub):" (checks/string? "Hello" {:pat-or-pats-sub has-uppercase}))
    (println "  has uppercase (sub):" (checks/string? "hello" {:pat-or-pats-sub has-uppercase}))

    (println "
Custom predicates:")
    (println "  starts with 'h':" (checks/string? good-string {:fn-or-fns #(.startsWith % "h")}))
    (println "  starts with 'z':" (checks/string? good-string {:fn-or-fns #(.startsWith % "z")}))

    (println "
Explain examples:")
    (println "  explain non-string:" (checks/string-explain non-string))
    (println "  explain too short:" (checks/string-explain short-string {:min 5}))
    (println "  explain pattern fail:" (checks/string-explain "bad-email" {:pat-or-pats-whole email-pattern}))

    (println "
Collapse results:")
    (println "  collapse (good) ->" 
             (result/collapse-result (checks/string-explain good-string) "Invalid string"))
    (println "  collapse (bad)  ->" 
             (result/collapse-result (checks/string-explain non-string) "Invalid string"))))


(defn demo-string-as-keyword []
  (println "

=== String-as-keyword validation ===")
  (let [valid-kw "valid-keyword"
        valid-underscore "valid_keyword" 
        invalid-start "123invalid"
        invalid-chars "invalid@keyword"
        empty-string ""
        too-short-nil nil]

    (println "
Basic keyword-safe checks:")
    (println "  valid keyword:" (checks/string-as-keyword? valid-kw))
    (println "  valid with underscore:" (checks/string-as-keyword? valid-underscore))
    (println "  starts with number:" (checks/string-as-keyword? invalid-start))
    (println "  invalid characters:" (checks/string-as-keyword? invalid-chars))
    (println "  empty string:" (checks/string-as-keyword? empty-string))
    (println "  nil (default):" (checks/string-as-keyword? too-short-nil))
    (println "  nil (allowed):" (checks/string-as-keyword? too-short-nil {:nil-ok true}))

    (println "
Length constraints (min defaults to 1):")
    (println "  'a' with default min:" (checks/string-as-keyword? "a"))
    (println "  'ab' with min 3:" (checks/string-as-keyword? "ab" {:min 3}))
    (println "  'abc' with min 3:" (checks/string-as-keyword? "abc" {:min 3}))

    (println "
Explain examples:")
    (println "  explain invalid start:" (checks/string-as-keyword-explain invalid-start))
    (println "  explain invalid chars:" (checks/string-as-keyword-explain invalid-chars))
    (println "  explain too short:" (checks/string-as-keyword-explain "a" {:min 3}))

    (println "
Collapse results:")
    (println "  collapse (good) ->" 
             (result/collapse-result (checks/string-as-keyword-explain valid-kw) "Invalid keyword"))
    (println "  collapse (bad)  ->" 
             (result/collapse-result (checks/string-as-keyword-explain invalid-start) "Invalid keyword"))))


(defn demo-number []
  (println "

=== Number validation ===")
  (let [int-val 42
        float-val 3.14
        ratio-val 1/3
        big-decimal (bigdec "123.456")
        non-number "not-a-number"
        large-num 1000
        small-num -5]

    (println "
Basic number checks:")
    (println "  number? int:" (checks/number? int-val))
    (println "  number? float:" (checks/number? float-val))
    (println "  number? ratio:" (checks/number? ratio-val))
    (println "  number? string:" (checks/number? non-number))
    (println "  number? nil (default):" (checks/number? nil))
    (println "  number? nil (allowed):" (checks/number? nil {:nil-ok true}))

    (println "
Type constraints:")
    (println "  42 as :int:" (checks/number? int-val {:type :int}))
    (println "  42 as :float:" (checks/number? int-val {:type :float}))
    (println "  3.14 as :float:" (checks/number? float-val {:type :float}))
    (println "  3.14 as :int:" (checks/number? float-val {:type :int}))

    (println "
Value bounds:")
    (println "  42 with min 0:" (checks/number? int-val {:min 0}))
    (println "  42 with min 50:" (checks/number? int-val {:min 50}))
    (println "  42 with max 100:" (checks/number? int-val {:max 100}))
    (println "  42 with max 10:" (checks/number? int-val {:max 10}))

    (println "
Custom predicates:")
    (println "  even number:" (checks/number? int-val {:fn-or-fns even?}))
    (println "  odd number:" (checks/number? int-val {:fn-or-fns odd?}))
    (println "  positive:" (checks/number? int-val {:fn-or-fns pos?}))
    (println "  negative:" (checks/number? small-num {:fn-or-fns neg?}))

    (println "
Explain examples:")
    (println "  explain non-number:" (checks/number-explain non-number))
    (println "  explain wrong type:" (checks/number-explain float-val {:type :int}))
    (println "  explain too small:" (checks/number-explain int-val {:min 50}))
    (println "  explain predicate fail:" (checks/number-explain int-val {:fn-or-fns odd?}))

    (println "
Collapse results:")
    (println "  collapse (good) ->" 
             (result/collapse-result (checks/number-explain int-val) "Invalid number"))
    (println "  collapse (bad)  ->" 
             (result/collapse-result (checks/number-explain non-number) "Invalid number"))))


(defn demo-collection []
  (println "

=== Collection checks ===")
  (let [vec-data [1 2 3 4]
        list-data '(a b c)
        set-data #{:x :y :z}
        map-data {:name "John" :age 30}
        bad-data "not-a-collection"
        duplicates [1 2 2 3]
        with-nils [1 nil 3]]

    (println "
Basic collection checks:")
    (println "  collection? vec:" (checks/collection? vec-data))
    (println "  collection? list:" (checks/collection? list-data))
    (println "  collection? set:" (checks/collection? set-data))
    (println "  collection? string:" (checks/collection? bad-data))
    (println "  collection? nil (default):" (checks/collection? nil))
    (println "  collection? nil (allowed):" (checks/collection? nil {:nil-ok true}))

    (println "
Type-specific checks:")
    (println "  vector as :vec:" (checks/collection? vec-data {:type :vec}))
    (println "  vector as :list:" (checks/collection? vec-data {:type :list}))
    (println "  map as :assoc:" (checks/collection? map-data {:type :assoc}))

    (println "
Size constraints:")
    (println "  vec with min 3:" (checks/collection? vec-data {:min 3}))
    (println "  vec with max 3:" (checks/collection? vec-data {:max 3}))

    (println "
Duplicate and nil value checks:")
    (println "  duplicates allowed:" (checks/collection? duplicates))
    (println "  duplicates forbidden:" (checks/collection? duplicates {:duplicates-ok false}))
    (println "  nil values allowed:" (checks/collection? with-nils))
    (println "  nil values forbidden:" (checks/collection? with-nils {:nil-value-ok false}))

    (println "
Explain examples:")
    (println "  explain bad type:" (checks/collection-explain bad-data))
    (println "  explain too large:" (checks/collection-explain vec-data {:max 3}))
    (println "  explain duplicates:" (checks/collection-explain duplicates {:duplicates-ok false}))

    ;; Collapse example
    (println "
Collapse result:")
    (println "  collapse (good) ->" 
             (result/collapse-result (checks/collection-explain vec-data) "Invalid collection"))
    (println "  collapse (bad)  ->" 
             (result/collapse-result (checks/collection-explain bad-data) "Invalid collection"))))


(defn demo-map-entry []
  (println "

=== Map entry validation ===")
  (let [user-data {:name "Alice"
                   :age 28
                   :profile {:email "alice@example.com"
                             :settings {:theme "dark"}}
                   :tags ["clojure" "programming"]
                   :active true
                   :title nil}
        empty-map {}
        bad-data "not-a-map"]

    (println "
Basic map entry checks:")
    (println "  name exists:" (checks/map-entry? user-data :name))
    (println "  missing key:" (checks/map-entry? user-data :missing))
    (println "  missing key (not required):" (checks/map-entry? user-data :missing {:key-required false}))
    (println "  nil map:" (checks/map-entry? nil :key))
    (println "  nil map (allowed):" (checks/map-entry? nil :key {:nil-ok true}))
    (println "  non-map:" (checks/map-entry? bad-data :key))

    (println "
Nested key navigation:")
    (println "  nested email:" (checks/map-entry? user-data [:profile :email]))
    (println "  deep nested theme:" (checks/map-entry? user-data [:profile :settings :theme]))
    (println "  missing nested:" (checks/map-entry? user-data [:profile :missing]))
    (println "  partial path missing:" (checks/map-entry? user-data [:missing :email]))

    (println "
Nil value handling:")
    (println "  nil value (default):" (checks/map-entry? user-data :title))
    (println "  nil value (allowed):" (checks/map-entry? user-data :title {:nil-value-ok true}))

    (println "
Type constraints:")
    (println "  name as :string:" (checks/map-entry? user-data :name {:type :string}))
    (println "  age as :number:" (checks/map-entry? user-data :age {:type :number}))
    (println "  active as :boolean:" (checks/map-entry? user-data :active {:type :boolean}))
    (println "  tags as :col:" (checks/map-entry? user-data :tags {:type :col}))
    (println "  tags as :vec:" (checks/map-entry? user-data :tags {:type :vec}))
    (println "  profile as :map:" (checks/map-entry? user-data :profile {:type :map}))
    (println "  name as :number (wrong):" (checks/map-entry? user-data :name {:type :number}))

    (println "
Custom predicates:")
    (println "  name starts with 'A':" (checks/map-entry? user-data :name {:fn-or-fns #(.startsWith % "A")}))
    (println "  age over 18:" (checks/map-entry? user-data :age {:fn-or-fns #(>= % 18)}))
    (println "  tags has >1 item:" (checks/map-entry? user-data :tags {:fn-or-fns #(> (count %) 1)}))
    (println "  email contains @:" (checks/map-entry? user-data [:profile :email] {:fn-or-fns #(.contains % "@")}))

    (println "
Combined constraints:")
    (println "  age (number, >18):" 
             (checks/map-entry? user-data :age {:type :number :fn-or-fns #(>= % 18)}))
    (println "  tags (vec, >1 item):" 
             (checks/map-entry? user-data :tags {:type :vec :fn-or-fns #(> (count %) 1)}))

    (println "
Explain examples:")
    (println "  explain missing key:" (checks/map-entry-explain user-data :missing))
    (println "  explain nil value:" (checks/map-entry-explain user-data :title))
    (println "  explain wrong type:" (checks/map-entry-explain user-data :name {:type :number}))
    (println "  explain predicate fail:" (checks/map-entry-explain user-data :age {:fn-or-fns #(< % 18)}))
    (println "  explain non-map:" (checks/map-entry-explain bad-data :key))

    (println "
Collapse results:")
    (println "  collapse (good) ->" 
             (result/collapse-result (checks/map-entry-explain user-data :name) "Invalid entry"))
    (println "  collapse (bad)  ->" 
             (result/collapse-result (checks/map-entry-explain user-data :missing) "Invalid entry"))))


#?(:clj
   (defn -main [& _args]
     (demo-string)
     (demo-string-as-keyword)
     (demo-number)
     (demo-collection)
     (demo-map-entry)))