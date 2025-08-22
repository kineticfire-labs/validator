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
;; Patterns
;;
;; Helpers for working with compiled regex patterns in validation contexts.
;; Provides functions to check whether a string matches whole patterns,
;; contains substring matches, or to return which patterns failed.
;; This namespace does not define domain checks itself—it supplies the
;; reusable pattern-matching utilities that other checks build on.
;; -----------------------------------------------------------------------------


(ns kineticfire.validator.patterns)


(defn matches-whole?
  "True if all compiled pattern(s) in `pat-or-pats` match the whole string `s`.
   Returns boolean."
  [pat-or-pats s]
  (let [patterns (if (sequential? pat-or-pats)
                   pat-or-pats
                   [pat-or-pats])]
    (every? #(re-matches % s) patterns)))


(defn matches-whole-yes
  "Return a vector of patterns that DID match the whole string `s`.
   If none matched, returns []."
  [pat-or-pats s]
  (let [patterns (if (sequential? pat-or-pats)
                   pat-or-pats
                   [pat-or-pats])]
    (vec (filter #(re-matches % s) patterns))))


(defn matches-whole-not
  "Return a vector of patterns that did NOT match the whole string `s`.
   If all matched, returns []."
  [pat-or-pats s]
  (let [patterns (if (sequential? pat-or-pats)
                   pat-or-pats
                   [pat-or-pats])]
    (vec (remove #(re-matches % s) patterns))))


(defn matches-substr?
  "True if all compiled pattern(s) in `pat-or-pats` match a substring in `s`.
   Returns boolean."
  [pat-or-pats s]
  (let [patterns (if (sequential? pat-or-pats)
                   pat-or-pats
                   [pat-or-pats])]
    (every? #(re-find % s) patterns)))


(defn matches-substr-yes
  "Return a vector of patterns that DID match at least one substring in `s`.
   If none matched, returns []."
  [pat-or-pats s]
  (let [patterns (if (sequential? pat-or-pats)
                   pat-or-pats
                   [pat-or-pats])]
    (vec (filter #(re-find % s) patterns))))


(defn matches-substr-not
  "Return a vector of patterns that did NOT match any substring in `s`.
   If all matched, returns []."
  [pat-or-pats s]
  (let [patterns (if (sequential? pat-or-pats)
                   pat-or-pats
                   [pat-or-pats])]
    (vec (remove #(re-find % s) patterns))))