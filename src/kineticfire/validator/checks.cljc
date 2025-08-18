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


(ns kineticfire.validator.checks
  (:require [kineticfire.validator.patterns :as patterns])
  (:import (java.util.regex Pattern))
  (:gen-class))


(def ^:const valid-string-as-keyword-pattern (Pattern/compile "^[a-zA-Z][a-zA-Z0-9_-]*$"))


(defn validate-string
  "Validates the string `s` returning boolean 'true' if valid else returns 'false' or `err` if using a form that
  includes `err`.

  By default, `s` is valid string if it is:
    - not 'nil'
    - a string such that '(string? s)' returns 'true'

  The `settings` map argument can provide additional string validation criteria:
    - :nil-ok            → 'true' if a valid string can be 'nil' and 'false' otherwise; defaults to 'false'
    - :min               → minimum number of characters for a valid string; defaults to empty string
    - :max               → maximum number of characters for  valid string; defaults to no maximum value
    - :pat-or-pats-whole → a compiled regex pattern or collection thereof to validate the whole string; defaults to no
                           regex patterns
    - :pat-or-pats-sub   → a compiled regex pattern or collection thereof to validate a substring; defaults to no regex
                           patterns
    - :fn-or-fns         → a function or collection thereof to validate the string, where the function accepts the
                           string as a single argument and must return 'true' if the string is valid or 'false'
                           otherwise; defaults to no function"
  ([s]
   (validate-string s false {}))
  ([s err]
   (validate-string s err {}))
  ([s err settings]
   (if (nil? s)
     (if (:nil-ok settings)
       true
       err)
     (if-not (string? s)
       err
       (let [length (if (or
                          (:min settings)
                          (:max settings))
                      (count s)
                      -1)
             min-valid (if (:min settings)
                         (if (< length (:min settings))
                           false
                           true)
                         true)]
         (if-not min-valid
           err
           (let [max-valid (if (:max settings)
                             (if (> length (:max settings))
                               false
                               true)
                             true)]
             (if-not max-valid
               err
               (let [pat-or-pats-whole-valid (if (:pat-or-pats-whole settings)
                                               (patterns/matches? (:pat-or-pats-whole settings) s)
                                               true)]
                 (if-not pat-or-pats-whole-valid
                   err
                   (let [pat-or-pats-sub-valid (if (:pat-or-pats-sub settings)
                                                 (patterns/contains-match? (:pat-or-pats-sub settings) s)
                                                 true)]
                     (if-not pat-or-pats-sub-valid
                       err
                       (let [fn-or-fns-valid (if (:fn-or-fns settings)
                                               (let [fns (if (sequential? (:fn-or-fns settings))
                                                           (:fn-or-fns settings)
                                                           [(:fn-or-fns settings)])]
                                                 (boolean (every? #(true? (% s)) fns)))
                                               true)]
                         (if-not fn-or-fns-valid
                           err
                           true))))))))))))))


(defn validate-string-as-keyword
  "Validates the string `s` as a string and a keyword, returning boolean 'true' if valid else returns 'false' or `err`
  if using a form that includes `err`.

  By default, `s` is a valid string if it is:
    - not 'nil'
    - a string such that '(string? s)' returns 'true'
    - has a length of at least one character
    - does not contain invalid keyword characters:  colon, forward slash, space, or  leading numeric, dash, or
      underscore characters

  The `settings` map argument can provide additional string validation criteria:
    - :nil-ok            → 'true' if a valid string can be 'nil' and 'false' otherwise; defaults to 'false'
    - :min               → minimum number of characters for a valid string; defaults to 1 and must be at least 1
    - :max               → maximum number of characters for  valid string; defaults to no maximum value
    - :pat-or-pats-whole → a compiled regex pattern or collection thereof to validate the whole string; defaults to no
                           regex patterns
    - :pat-or-pats-sub   → a compiled regex pattern or collection thereof to validate a substring; defaults to no regex
                           patterns
    - :fn-or-fns         → a function or collection thereof to validate the string, where the function accepts the
                           string as a single argument and must return 'true' if the string is valid or 'false'
                           otherwise; defaults to no function"
  ([s]
   (validate-string-as-keyword s false {}))
  ([s err]
   (validate-string-as-keyword s err {}))
  ([s err settings]
   (let [res (validate-string s err settings)]
     (if (and
           (boolean? res)
           res)
       (if (nil? s)
         true
         (if-not (seq (re-find valid-string-as-keyword-pattern s))
           err
           true))
       err))))


(defn validate-number
  "Validates the number `n` returning boolean 'true' if valid else returns 'false' or `err` if using a form that
  includes `err`.

  By default, `n` is a valid number if it is:
    - not 'nil'
    - a number, such that '(number? n)' is 'true'

  The `settings` map argument can provide additional string validation criteria:
    - :nil-ok            → 'true' if a valid number can be 'nil' and 'false' otherwise; defaults to 'false'
    - :type              → the type of number as:  :int (integer), :float (float), :double (double), :decimal (decimal),
                           and :ratio (ratio); defaults to '(number? n)'
    - :min               → minimum value of the number; defaults to no check
    - :max               → maximum value of the number; defaults to no check
    - :fn-or-fns         → a function or collection thereof to validate the number, where the function accepts the
                           number as a single argument and must return 'true' if the number is valid or 'false'
                           otherwise; defaults to no function"
  ([n]
   (validate-number n false {}))
  ([n err]
   (validate-number n err {}))
  ([n err settings]
   (if (nil? n)
     (if (:nil-ok settings)
       true
       err)
     (let [numeric-type (:type settings)
           numeric-type-ok (if-not numeric-type
                             (number? n)
                             (cond
                               (= :int numeric-type) (int? n)
                               (= :float numeric-type) (float? n)
                               (= :double numeric-type) (double? n)
                               (= :decimal numeric-type) (decimal? n)
                               (= :ratio numeric-type) (ratio? n)
                               :else false))]
       (if-not numeric-type-ok
         err
         (let [min-valid (if (:min settings)
                           (if (< n (:min settings))
                             false
                             true)
                           true)]
           (if-not min-valid
             err
             (let [max-valid (if (:max settings)
                               (if (> n (:max settings))
                                 false
                                 true)
                               true)]
               (if-not max-valid
                 err
                 (let [fn-or-fns-valid (if (:fn-or-fns settings)
                                         (let [fns (if (sequential? (:fn-or-fns settings))
                                                     (:fn-or-fns settings)
                                                     [(:fn-or-fns settings)])]
                                           (boolean (every? #(true? (% n)) fns)))
                                         true)]
                   (if-not fn-or-fns-valid
                     err
                     true)))))))))))