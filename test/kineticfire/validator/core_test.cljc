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
;;	   project site: https://github.com/kineticfire-labs/validator/


(ns kineticfire.validator.core-test
  (:require [clojure.test :refer :all]
            [kineticfire.validator.core :as validator]))


(defn perform-valid-string?-test
  [s err settings expected]
  (let [actual (cond
                 settings (validator/valid-string? s err settings)
                 err (validator/valid-string? s err)
                 :else (validator/valid-string? s))]
    (is (= actual expected))))


(deftest valid-string?-test
  ;;
  ;; [s] form
  (testing "[s]: invalid: s is nil"
    (perform-valid-string?-test nil nil nil false))
  (testing "[s]: invalid: s is not a string"
    (perform-valid-string?-test 1 nil nil false))
  (testing "[s]: valid: empty string"
    (perform-valid-string?-test "" nil nil true))
  (testing "[s]: valid: string"
    (perform-valid-string?-test "hello" nil nil true))
  ;;
  ;; [s err] form
  (let [err "Invalid string"]
    (testing "[s err]: invalid: s is nil"
      (perform-valid-string?-test nil err nil err))
    (testing "[s err]: invalid: s is not a string"
      (perform-valid-string?-test 1 err nil err))
    (testing "[s err]: valid: empty string"
      (perform-valid-string?-test "" err nil true))
    (testing "[s err]: valid: string"
      (perform-valid-string?-test "hello" err nil true)))
  ;;
  ;; [s err settings] form
  (let [err "Invalid string"]
    ;; - w/ empty settings
    (testing "[s err settings]: invalid: s is nil"
      (perform-valid-string?-test nil err {} err))
    (testing "[s err settings]: invalid: s is not a string"
      (perform-valid-string?-test 1 err {} err))
    (testing "[s err settings]: valid: empty string"
      (perform-valid-string?-test "" err {} true))
    (testing "[s err settings]: valid: string"
      (perform-valid-string?-test "hello" err {} true))
    ;; - w/ settings define
    (let [settings {:min 1
                    :max 5}]
      (testing "[s err settings]: invalid: s is nil, :nil-ok 'false'"
        (perform-valid-string?-test nil err {:nil-ok false} err))
      (testing "[s err settings]: valid: s is nil, :nil-ok 'true'"
        (perform-valid-string?-test nil err {:nil-ok true} true))
      (testing "[s err settings]: invalid: s is not a string"
        (perform-valid-string?-test 1 err settings err))
      (testing "[s err settings]: valid: empty string"
        (perform-valid-string?-test "" err {} true))
      (testing "[s err settings]: invalid: empty string, less than min"
        (perform-valid-string?-test "" err settings err))
      (testing "[s err settings]: invalid: string, greater than max"
        (perform-valid-string?-test "hello there" err settings err))
      )

    )

  )
;; todo: finish

;; (def pattern (re-pattern "^[0-9]+$")) ; matches only digits

;(def digits (re-pattern "^[0-9]+$"))
;(def letters (re-pattern "^[a-zA-Z]+$"))
;(def alnum (re-pattern "^[a-zA-Z0-9]+$"))
;
;(matches? digits "12345")      ;; => true
;(matches? [digits alnum] "12345") ;; => true
;(matches? [digits letters] "12345") ;; => false
