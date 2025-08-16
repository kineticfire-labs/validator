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


(ns kineticfire.validator.patterns
  (:gen-class))


(defn matches?
  "Returns 'true' if the compiled pattern(s) in `pat-or-pats` match the whole string `s` and 'false' otherwise.  The
  argument `pat-or-pats` may be a compiled pattern or a collection thereof."
  [pat-or-pats s]
  (let [patterns (if (sequential? pat-or-pats)
                   pat-or-pats
                   [pat-or-pats])]
    (every? #(re-matches % s) patterns)))


(defn contains-match?
  "Returns 'true' if the compiled pattern(s) in `pat-or-pats` match a substring in string `s` and 'false' otherwise.
  The argument `pat-or-pats` may be a compiled pattern or a collection thereof."
  [pat-or-pats s]
  (let [patterns (if (sequential? pat-or-pats)
                   pat-or-pats
                   [pat-or-pats])]
    (every? #(re-find % s) patterns)))