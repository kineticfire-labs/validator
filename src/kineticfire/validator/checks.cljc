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
;; Checks (Aggregator)
;;
;; Central namespace that re-exports all available validation checks from
;; `kineticfire.validator.checks.*`. This provides a single, consistent entry
;; point for consumers of the library, so they don’t need to know which specific
;; sub-namespace a given check belongs to.
;;
;; This namespace does not add new logic—it only aggregates and re-exposes
;; functions like `string?`, `number?`, `string-explain`, etc.
;; -----------------------------------------------------------------------------


(ns kineticfire.validator.checks
  (:refer-clojure :exclude [string? number?])
  (:require [kineticfire.validator.checks.basic :as basic]))


;; -----------------------------------------------------------------------------
;; 'basic' re-imports

(def string?        basic/string?)
(def string-explain basic/string-explain)
(def string-as-keyword?        basic/string-as-keyword?)
(def string-as-keyword-explain basic/string-as-keyword-explain)
(def number?        basic/number?)
(def number-explain basic/number-explain)
(def collection?        basic/collection?)
(def collection-explain basic/collection-explain)
