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
  (:require [kineticfire.validator.checks.basic :as basic]))


;; 'basic' re-imports
(def validate-string basic/validate-string)
;;(def validate-string-explain basic/validate-string-explain)
(def validate-string-as-keyword basic/validate-string-as-keyword)
;;(def validate-string-as-keyword-explain basic/validate-string-as-keyword-explain)
(def validate-number basic/validate-number)
;;(def validate-number-explain basic/validate-number-explain)



;;todo
;(checks/string? "abc")
;;; => true/false
;
;(checks/string-explain "abc")
;;; => {:valid? false
;;;     :code   :string/not-keyword-safe
;;;     :message "Not keyword-safe: abc"}




;todo
;Recommended naming split
;
;Boolean predicate (pure check):
;kineticfire.validator.checks/string? → true | false
;
;Rich/explain result:
;kineticfire.validator.checks/string-explain → {:valid? ... :code ... :message ... ...}
;
;Collapsed “legacy” result (true or caller-provided error):
;kineticfire.validator.core/validate-string → true | <err-collapsed>