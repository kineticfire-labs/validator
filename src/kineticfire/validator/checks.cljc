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


;; An aggregator for all checks at kineticfire.validator.checks.*

(ns kineticfire.validator.checks
  (:refer-clojure :exclude [string? number?])
  (:require [kineticfire.validator.checks.basic :as basic]))


;; 'basic' re-imports
(def string?        basic/string?)
(def string-explain basic/string-explain)

(def string-as-keyword?        basic/string-as-keyword?)
(def string-as-keyword-explain basic/string-as-keyword-explain)

(def number?        basic/number?)
(def number-explain basic/number-explain)
