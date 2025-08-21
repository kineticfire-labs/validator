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
;; Result
;;
;; Utilities for interpreting, collapsing, and combining validator outputs.
;; This namespace does not perform validation itself—it transforms explain-style
;; results into simpler booleans, caller-provided errors, or aggregated maps.
;; -----------------------------------------------------------------------------


(ns kineticfire.validator.result)


(defn collapse-result
  "Collapse an explain map into true on success, else return `err`."
  [result err]
  (if (:valid? result)
    true
    err))


(defn collapse-results
  "Collapse a sequence of explain maps. Returns true if all valid, else the first err."
  [results err]
  (if (every? :valid? results)
    true
    err))


(defn combine-results
  "Combine multiple explain results into a single aggregated map."
  [results]
  {:valid?  (every? :valid? results)
   :errors  (mapv #(select-keys % [:code :message]) (remove :valid? results))})


(defn valid?
  "Shorthand to check validity from an explain result. Always returns a boolean."
  [result]
  (true? (:valid? result)))


(defn errors
  "Extract errors (codes/messages) from an explain result or collection of them."
  [result-or-results]
  (cond
    (map? result-or-results)    (when-not (:valid? result-or-results)
                                  [(select-keys result-or-results [:code :message])])
    (sequential? result-or-results) (mapcat errors result-or-results)))

