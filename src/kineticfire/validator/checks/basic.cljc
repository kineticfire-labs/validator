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
;; Basic Checks
;;
;; Primitive validation functions for common types like strings, numbers, and
;; collections.
;;
;; Each check comes in two forms:
;;   - `...?` returns a simple true/false
;;   - `...-explain` returns an explain-style map with :valid?, :code, :message,
;;     and optional context (e.g. expected regex, bounds).
;;
;; These checks enforce fundamental constraints (type, length, numeric bounds,
;; regex matches) but do not orchestrate or aggregate results— that’s handled
;; by the runner layer.
;; -----------------------------------------------------------------------------


(ns kineticfire.validator.checks.basic
  (:refer-clojure :exclude [number? string?])
  (:require [clojure.core :as core]
            [kineticfire.validator.patterns :as patterns])
  (:import (java.util.regex Pattern)))


(def ^:const valid-string-as-keyword-pattern (Pattern/compile "^[a-zA-Z][a-zA-Z0-9_-]*$"))


;; -----------------------------------------------------------------------------
;; Basic string checks
;;
;; Boolean predicate:
;;   (checks/string? "abc" {:min 1 :max 10 :nil-ok false ...})
;;   => true | false
;;
;; Explain variant (first failure):
;;   (checks/string-explain "123abc" {:pat-or-pats-whole (re-pattern "^[A-Za-z]+$")})
;;   => {:valid? false
;;       :code :string/regex-whole-failed
;;       :message "Whole-string pattern(s) failed."
;;       :expected {:regex ["^[A-Za-z]+$"]}
;;       :value "123abc"}
;; -----------------------------------------------------------------------------

(defn- ->patterns
  "Normalize a single compiled regex or a collection into a vector of Patterns.
   Returns nil if input is nil."
  ^java.util.List
  [pat-or-pats]
  (when pat-or-pats
    (let [ps (if (sequential? pat-or-pats) pat-or-pats [pat-or-pats])]
      (vec ps))))


(defn- ->fns
  "Normalize a single predicate fn or a collection into a vector of fns.
   Returns nil if input is nil."
  [fn-or-fns]
  (when fn-or-fns
    (let [fs (if (sequential? fn-or-fns) fn-or-fns [fn-or-fns])]
      (vec fs))))


(defn string?
  "Boolean predicate that validates a string `s` with optional `settings`.

   Settings:
     :nil-ok            boolean  allow nil as valid; optional, defaults to 'false'
     :min               integer  minimum length (inclusive); optional, defaults to no check
     :max               integer  maximum length (inclusive); optional, defaults to no check
     :pat-or-pats-whole Pattern | Collection<Pattern>
                         all patterns must match the WHOLE string; optional, defaults to no check
     :pat-or-pats-sub   Pattern | Collection<Pattern>
                         all patterns must match as a SUBSTRING somewhere; optional, defaults to no check
     :fn-or-fns         (fn [s] ...) | Collection<(fn [s] ...)>; each must return true; optional, defaults to no check

   Returns true iff all configured checks pass."
  ([s] (string? s {}))
  ([s {:keys [nil-ok min max pat-or-pats-whole pat-or-pats-sub fn-or-fns]
       :or   {nil-ok false}}]
   (cond
     ;; nil handling
     (nil? s) nil-ok

     ;; type check
     (not (core/string? s)) false

     :else
     (let [len (count s)]
       (and
         ;; length min/max (only when provided)
         (if (some? min) (<= min len) true)
         (if (some? max) (<= len max) true)

         ;; whole-string regex (all must match)
         (let [ps (->patterns pat-or-pats-whole)]
           (if ps (patterns/matches-whole? ps s) true))

         ;; substring regex (all must match somewhere)
         (let [ps (->patterns pat-or-pats-sub)]
           (if ps (patterns/matches-substr? ps s) true))

         ;; custom predicates (all must return true)
         (let [fs (->fns fn-or-fns)]
           (if fs (every? #(boolean (% s)) fs) true)))))))


(defn string-explain
  "Explain-style validator for strings. Returns either
     {:valid? true :value s}
   or the first failure as
     {:valid? false
      :code <kw> :message <str>
      :value s
      :expected <optional-map>}

   Supported settings are identical to `string?`."
  ([s] (string-explain s {}))
  ([s {:keys [nil-ok min max pat-or-pats-whole pat-or-pats-sub fn-or-fns]
       :or   {nil-ok false}}]
   (cond
     ;; nil handling
     (nil? s)
     (if nil-ok
       {:valid? true :value s}
       {:valid? false :code :string/nil :message "Value is nil." :value s})

     ;; type check
     (not (core/string? s))
     {:valid?  false
      :code    :type/not-string
      :message (str "Expected string, got " (some-> s class .getName) ".")
      :value   s}

     :else
     (let [len (count s)
           whole (->patterns pat-or-pats-whole)
           sub (->patterns pat-or-pats-sub)
           fns (->fns fn-or-fns)]
       (cond
         (and (some? min) (< len min))
         {:valid?   false
          :code     :string/too-short
          :message  (str "String shorter than min length " min ".")
          :expected {:min min}
          :value    s}

         (and (some? max) (> len max))
         {:valid?   false
          :code     :string/too-long
          :message  (str "String longer than max length " max ".")
          :expected {:max max}
          :value    s}

         (and whole (not (patterns/matches-whole? whole s)))
         {:valid?   false
          :code     :string/regex-whole-failed
          :message  "Whole-string pattern(s) failed."
          :expected {:regex (mapv #(.pattern ^java.util.regex.Pattern %) whole)}
          :value    s}

         (and sub (not (patterns/matches-substr? sub s)))
         {:valid?   false
          :code     :string/regex-substr-failed
          :message  "Substring pattern(s) failed."
          :expected {:regex (mapv #(.pattern ^java.util.regex.Pattern %) sub)}
          :value    s}

         (and fns (not (every? #(boolean (% s)) fns)))      ;; <- boolean, not true?
         {:valid?  false
          :code    :string/predicate-failed
          :message "Custom predicate(s) returned false."
          :value   s}

         :else
         {:valid? true :value s})))))


(defn- clamp-min>=1
  "Ensure :min is at least 1; if :min missing, default to 1."
  [settings]
  (let [m (:min settings)]
    (-> settings
        (assoc :min (if (some? m) (max 1 m) 1)))))


(defn string-as-keyword?
  "Boolean predicate: is `s` a valid string that can be converted to a Clojure keyword?

   Settings (same as `string?`), with differences:
     - :nil-ok defaults to false (nil is invalid unless explicitly allowed)
     - :min defaults to 1 (and is clamped to at least 1)

   Returns true/false."
  ([s] (string-as-keyword? s {}))
  ([s settings]
   (let [settings* (-> settings
                       (update :nil-ok #(if (nil? %) false %))
                       (clamp-min>=1))]
     (and
       ;; must be a valid string by base rules
       (string? s settings*)

       ;; if nil is allowed and s is nil, treat as valid and skip regex
       (or (nil? s)
           (boolean (re-matches valid-string-as-keyword-pattern s)))))))


(defn string-as-keyword-explain
  "Explain-style validator for keyword-safe strings.
   Returns {:valid? true :value s} on success, else first failure map.

   Differences from `string-explain`:
     - Enforces min length >= 1 by default (clamps provided :min).
     - Adds a keyword-safe whole-string regex step."
  ([s] (string-as-keyword-explain s {}))
  ([s settings]
   (let [settings* (-> settings
                       (update :nil-ok #(if (nil? %) false %))
                       (clamp-min>=1))
         base (string-explain s settings*)]
     (if (not (:valid? base))
       base
       ;; base string checks passed (or nil allowed and s is nil)
       (if (nil? s)
         {:valid? true :value s}
         (if (re-matches valid-string-as-keyword-pattern s)
           {:valid? true :value s}
           {:valid?   false
            :code     :string/not-keyword-safe
            :message  (str "Not keyword-safe: " s)
            :expected {:regex [(.pattern valid-string-as-keyword-pattern)]}
            :value    s}))))))


(defn- match-number-type?
  "Return true if `n` matches the requested number type keyword."
  [n t]
  (case t
    :int (integer? n)                                       ; covers Long/BigInt
    :float (instance? Float n)                              ; specifically a Float
    :double (double? n)                                     ; Double
    :decimal (instance? BigDecimal n)                       ; BigDecimal
    :ratio (ratio? n)                                       ; clojure.lang.Ratio
    (boolean (core/number? n))))                            ; default: any number


(defn number?
  "Boolean predicate that validates a number `n` with optional `settings`.

   Settings:
     :nil-ok    boolean  (default false) allow nil as valid
     :type      one of #{:int :float :double :decimal :ratio}; default: any number
     :min       minimum value (inclusive)
     :max       maximum value (inclusive)
     :fn-or-fns (fn [n] ...) | Collection<(fn [n] ...)>; each must return truthy

   Returns true iff all configured checks pass."
  ([n] (number? n {}))
  ([n {:keys [nil-ok type min max fn-or-fns]
       :or   {nil-ok false}}]
   (cond
     (nil? n) nil-ok
     (not (core/number? n)) false
     :else
     (and
       ;; type constraint (if provided)
       (if (some? type) (match-number-type? n type) true)
       ;; min/max (if provided)
       (if (some? min) (<= min n) true)
       (if (some? max) (<= n max) true)
       ;; custom predicates (all must be truthy)
       (let [fs (->fns fn-or-fns)]
         (if fs (every? #(boolean (% n)) fs) true))))))


(defn number-explain
  "Explain-style validator for numbers. Returns either
     {:valid? true :value n}
   or the first failure as
     {:valid? false
      :code <kw> :message <str>
      :value n
      :expected <optional-map>}.

   Settings identical to `number?`."
  ([n] (number-explain n {}))
  ([n {:keys [nil-ok type min max fn-or-fns]
       :or   {nil-ok false}}]
   (cond
     ;; nil handling
     (nil? n)
     (if nil-ok
       {:valid? true :value n}
       {:valid? false :code :number/nil :message "Value is nil." :value n})

     ;; type check (must be a number first)
     (not (core/number? n))
     {:valid?  false
      :code    :type/not-number
      :message (str "Expected number, got " (some-> n class .getName) ".")
      :value   n}

     ;; type constraint (if provided)
     (and (some? type) (not (match-number-type? n type)))
     {:valid?   false
      :code     :number/wrong-type
      :message  (str "Number is not of requested type " type ".")
      :expected {:type type}
      :value    n}

     ;; min/max
     (and (some? min) (< n min))
     {:valid?   false
      :code     :number/too-small
      :message  (str "Number is smaller than min " min ".")
      :expected {:min min}
      :value    n}

     (and (some? max) (> n max))
     {:valid?   false
      :code     :number/too-large
      :message  (str "Number is larger than max " max ".")
      :expected {:max max}
      :value    n}

     ;; custom predicates (first failure)
     :else
     (let [fs (->fns fn-or-fns)]
       (cond
         (and fs (not (every? #(boolean (% n)) fs)))
         {:valid?  false
          :code    :number/predicate-failed
          :message "Custom predicate(s) returned false."
          :value   n}

         :else
         {:valid? true :value n})))))

(defn- match-collection-type?
  "Return true if `c` matches the requested collection type keyword."
  [c t]
  (case t
    :vec (vector? c)
    :list (list? c)
    :set (set? c)
    :map (map? c)
    :seq (sequential? c)
    :assoc (associative? c)
    (boolean (coll? c))))


(defn- has-duplicates?
  "Return true if collection has duplicate values."
  [c]
  (when (and (coll? c) (not (map? c)) (not (set? c)))
    (not= (count c) (count (distinct c)))))


(defn- has-nil-values?
  "Return true if collection contains nil values."
  [c]
  (when (coll? c)
    (if (map? c)
      (or (some nil? (keys c)) (some nil? (vals c)))
      (some nil? c))))


(defn collection?
  "Boolean predicate that validates a collection `c` with optional `settings`.

   Settings:
     :nil-ok        boolean  allow nil as valid (default: false)
     :min           integer  minimum count (inclusive, optional)
     :max           integer  maximum count (inclusive, optional)
     :type          keyword  collection type constraint (optional)
                             one of #{:vec :list :set :map :seq :assoc}
     :duplicates-ok boolean  allow duplicate values (default: true)
     :nil-value-ok  boolean  allow nil values within collection (default: true)
     :fn-or-fns     (fn [c] ...) | Collection<(fn [c] ...)>; each must return truthy

   Returns true iff all configured checks pass."
  ([c] (collection? c {}))
  ([c {:keys [nil-ok min max type duplicates-ok nil-value-ok fn-or-fns]
       :or   {nil-ok false duplicates-ok true nil-value-ok true}}]
   (cond
     (nil? c) nil-ok
     (not (coll? c)) false
     :else
     (let [cnt (count c)]
       (and
         ;; type constraint (if provided)
         (if (some? type) (match-collection-type? c type) true)
         ;; min/max (if provided)
         (if (some? min) (<= min cnt) true)
         (if (some? max) (<= cnt max) true)
         ;; duplicates check
         (if duplicates-ok true (not (has-duplicates? c)))
         ;; nil values check
         (if nil-value-ok true (not (has-nil-values? c)))
         ;; custom predicates (all must be truthy)
         (let [fs (->fns fn-or-fns)]
           (if fs (every? #(boolean (% c)) fs) true)))))))


(defn collection-explain
  "Explain-style validator for collections. Returns either
     {:valid? true :value c}
   or the first failure as
     {:valid? false
      :code <kw> :message <str>
      :value c
      :expected <optional-map>}.

   Settings identical to `collection?`."
  ([c] (collection-explain c {}))
  ([c {:keys [nil-ok min max type duplicates-ok nil-value-ok fn-or-fns]
       :or   {nil-ok false duplicates-ok true nil-value-ok true}}]
   (cond
     ;; nil handling
     (nil? c)
     (if nil-ok
       {:valid? true :value c}
       {:valid? false :code :collection/nil :message "Value is nil." :value c})

     ;; type check (must be a collection first)
     (not (coll? c))
     {:valid?  false
      :code    :type/not-collection
      :message (str "Expected collection, got " (some-> c class .getName) ".")
      :value   c}

     ;; type constraint (if provided)
     (and (some? type) (not (match-collection-type? c type)))
     {:valid?   false
      :code     :collection/wrong-type
      :message  (str "Collection is not of requested type " type ".")
      :expected {:type type}
      :value    c}

     ;; min/max
     :else
     (let [cnt (count c)]
       (cond
         (and (some? min) (< cnt min))
         {:valid?   false
          :code     :collection/too-small
          :message  (str "Collection smaller than min count " min ".")
          :expected {:min min}
          :value    c}

         (and (some? max) (> cnt max))
         {:valid?   false
          :code     :collection/too-large
          :message  (str "Collection larger than max count " max ".")
          :expected {:max max}
          :value    c}

         (and (not duplicates-ok) (has-duplicates? c))
         {:valid?  false
          :code    :collection/duplicates-found
          :message "Collection contains duplicate values."
          :value   c}

         (and (not nil-value-ok) (has-nil-values? c))
         {:valid?  false
          :code    :collection/nil-values-found
          :message "Collection contains nil values."
          :value   c}

         ;; custom predicates (first failure)
         :else
         (let [fs (->fns fn-or-fns)]
           (cond
             (and fs (not (every? #(boolean (% c)) fs)))
             {:valid?  false
              :code    :collection/predicate-failed
              :message "Custom predicate(s) returned false."
              :value   c}

             :else
             {:valid? true :value c})))))))
