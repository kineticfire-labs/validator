;; (c) Copyright 2024-2025 checks Contributors. All rights reserved.
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


(ns kineticfire.validator.checks.basic-test
  (:require [clojure.test :refer :all]
            [kineticfire.validator.checks.basic :as basic]))


(defn perform-validate-string-test
  [s err settings expected]
  (let [actual (cond
                 settings (basic/validate-string s err settings)
                 err (basic/validate-string s err)
                 :else (basic/validate-string s))]
    (is (= actual expected))))


;(deftest validate-string-test
;  ;;
;  ;; [s] form
;  (testing "[s]: invalid: s is nil"
;    (perform-validate-string-test nil nil nil false))
;  (testing "[s]: invalid: s is not a string"
;    (perform-validate-string-test 1 nil nil false))
;  (testing "[s]: valid: empty string"
;    (perform-validate-string-test "" nil nil true))
;  (testing "[s]: valid: string"
;    (perform-validate-string-test "hello" nil nil true))
;  ;;
;  ;; [s err] form
;  (let [err "Invalid string"]
;    (testing "[s err]: invalid: s is nil"
;      (perform-validate-string-test nil err nil err))
;    (testing "[s err]: invalid: s is not a string"
;      (perform-validate-string-test 1 err nil err))
;    (testing "[s err]: valid: empty string"
;      (perform-validate-string-test "" err nil true))
;    (testing "[s err]: valid: string"
;      (perform-validate-string-test "hello" err nil true)))
;  ;;
;  ;; [s err settings] form
;  (let [err "Invalid string"]
;    ;; - w/ empty settings
;    (testing "[s err settings]: invalid: s is nil"
;      (perform-validate-string-test nil err {} err))
;    (testing "[s err settings]: invalid: s is not a string"
;      (perform-validate-string-test 1 err {} err))
;    (testing "[s err settings]: valid: empty string"
;      (perform-validate-string-test "" err {} true))
;    (testing "[s err settings]: valid: string"
;      (perform-validate-string-test "hello" err {} true))
;    ;; - w/ settings define
;    (let [digits-whole (re-pattern "[0-9]+$")
;          letters-whole (re-pattern "[a-zA-Z]+$")
;          alnum-whole (re-pattern "[a-zA-Z0-9]+$")
;          digits-sub (re-pattern "[0-9]+")
;          digits2-sub (re-pattern "[0-9]+")
;          letters-sub (re-pattern "[a-zA-Z]+")]
;      (testing "[s err settings]: invalid: s is nil, :nil-ok 'false'"
;        (perform-validate-string-test nil err {:nil-ok false} err))
;      (testing "[s err settings]: valid: s is nil, :nil-ok 'true'"
;        (perform-validate-string-test nil err {:nil-ok true} true))
;      (testing "[s err settings]: invalid: s is not a string"
;        (perform-validate-string-test 1 err {:min 1, :max 5} err))
;      (testing "[s err settings]: valid: empty string"
;        (perform-validate-string-test "" err {:min 0, :max 5} true))
;      (testing "[s err settings]: invalid: empty string, less than min"
;        (perform-validate-string-test "" err {:min 1, :max 5} err))
;      (testing "[s err settings]: invalid: string, greater than max"
;        (perform-validate-string-test "hello there" err {:min 1, :max 5} err))
;      ;; - pattern whole string
;      (testing "[s err settings]: invalid: string, pattern whole string fail"
;        (perform-validate-string-test "hello" err {:pat-or-pats-whole digits-whole} err))
;      (testing "[s err settings]: valid: string, pattern whole string"
;        (perform-validate-string-test "hello" err {:pat-or-pats-whole letters-whole} true))
;      (testing "[s err settings]: invalid: string, pattern whole string as collection fail"
;        (perform-validate-string-test "hello" err {:pat-or-pats-whole [digits-whole]} err))
;      (testing "[s err settings]: valid: string, pattern whole string as collection"
;        (perform-validate-string-test "hello" err {:pat-or-pats-whole [letters-whole]} true))
;      (testing "[s err settings]: invalid: string, 2 patterns whole string as collection fail"
;        (perform-validate-string-test "hello" err {:pat-or-pats-whole [digits-whole letters-whole]} err))
;      (testing "[s err settings]: valid: string, 2 patterns whole string as collection"
;        (perform-validate-string-test "hello" err {:pat-or-pats-whole [alnum-whole letters-whole]} true))
;      ;; - pattern substring
;      (testing "[s err settings]: invalid: string, pattern substring fail"
;        (perform-validate-string-test "hello" err {:pat-or-pats-sub digits-sub} err))
;      (testing "[s err settings]: valid: string, pattern substring"
;        (perform-validate-string-test "hel123lo" err {:pat-or-pats-sub digits-sub} true))
;      (testing "[s err settings]: invalid: string, pattern substring as collection fail"
;        (perform-validate-string-test "hello" err {:pat-or-pats-sub [digits-sub]} err))
;      (testing "[s err settings]: valid: string, pattern whole string as collection"
;        (perform-validate-string-test "hel123lo" err {:pat-or-pats-sub [digits-sub]} true))
;      (testing "[s err settings]: invalid: string, 2 patterns substring as collection fail"
;        (perform-validate-string-test "hello" err {:pat-or-pats-sub [digits-sub digits2-sub]} err))
;      (testing "[s err settings]: valid: string, 2 patterns substring as collection"
;        (perform-validate-string-test "hel123lo" err {:pat-or-pats-sub [digits-sub letters-sub]} true))
;      ;; - fn
;      (testing "[s err settings]: invalid: one fn fail"
;        (perform-validate-string-test "hello" err {:fn-or-fns #(if (= "hello" %)
;                                                                 false
;                                                                 true)} err))
;      (testing "[s err settings]: valid: one fn"
;        (perform-validate-string-test "hi" err {:fn-or-fns #(if (= "hello" %)
;                                                              false
;                                                              true)} true))
;      (testing "[s err settings]: invalid: one fn as collection fail"
;        (perform-validate-string-test "hello" err {:fn-or-fns [#(if (= "hello" %)
;                                                                  false
;                                                                  true)]} err))
;      (testing "[s err settings]: valid: one fn as collection "
;        (perform-validate-string-test "hi" err {:fn-or-fns [#(if (= "hello" %)
;                                                               false
;                                                               true)]} true))
;      (testing "[s err settings]: invalid: two fn as collection fail"
;        (perform-validate-string-test "hello" err {:fn-or-fns [#(if (= "hello" %)
;                                                                  false
;                                                                  true)
;                                                               #(if (= "howdy" %)
;                                                                  false
;                                                                  true)]} err))
;      (testing "[s err settings]: valid: two fn as collection "
;        (perform-validate-string-test "hi" err {:fn-or-fns [#(if (= "hello" %)
;                                                               false
;                                                               true)
;                                                            #(if (= "howdy" %)
;                                                               false
;                                                               true)]} true)))))
;
;
;(defn perform-validate-string-as-keyword-test
;  [s err settings expected]
;  (let [actual (cond
;                 settings (checks/validate-string-as-keyword s err settings)
;                 err (checks/validate-string-as-keyword s err)
;                 :else (checks/validate-string-as-keyword s))]
;    (is (= actual expected))))
;
;
;(deftest validate-string-as-keyword-test
;  ;;
;  ;; [s] form
;  (testing "[s]: invalid: s is nil"
;    (perform-validate-string-as-keyword-test nil nil nil false))
;  (testing "[s]: invalid: s is not a string"
;    (perform-validate-string-as-keyword-test 1 nil nil false))
;  (testing "[s]: valid: empty string"
;    (perform-validate-string-as-keyword-test "" nil nil false))
;  (testing "[s]: valid: string"
;    (perform-validate-string-as-keyword-test "hello" nil nil true))
;  ;;
;  ;; [s err] form
;  (let [err "Invalid string"]
;    (testing "[s err]: invalid: s is nil"
;      (perform-validate-string-as-keyword-test nil err nil err))
;    (testing "[s err]: invalid: s is not a string"
;      (perform-validate-string-as-keyword-test 1 err nil err))
;    (testing "[s err]: valid: empty string"
;      (perform-validate-string-as-keyword-test "" err nil err))
;    (testing "[s err]: valid: string"
;      (perform-validate-string-as-keyword-test "hello" err nil true)))
;  ;;
;  ;; [s err settings] form
;  (let [err "Invalid string"]
;    ;; - w/ empty settings
;    (testing "[s err settings]: invalid: s is nil"
;      (perform-validate-string-as-keyword-test nil err {} err))
;    (testing "[s err settings]: invalid: s is not a string"
;      (perform-validate-string-as-keyword-test 1 err {} err))
;    (testing "[s err settings]: valid: empty string"
;      (perform-validate-string-as-keyword-test "" err {} err))
;    (testing "[s err settings]: valid: string"
;      (perform-validate-string-as-keyword-test "hello" err {} true))
;    ;; - w/ settings define
;    (let [digits-whole (re-pattern "[0-9]+$")
;          letters-whole (re-pattern "[a-zA-Z]+$")
;          alnum-whole (re-pattern "[a-zA-Z0-9]+$")
;          digits-sub (re-pattern "[0-9]+")
;          digits2-sub (re-pattern "[0-9]+")
;          letters-sub (re-pattern "[a-zA-Z]+")]
;      (testing "[s err settings]: invalid: s is nil, :nil-ok 'false'"
;        (perform-validate-string-as-keyword-test nil err {:nil-ok false} err))
;      (testing "[s err settings]: valid: s is nil, :nil-ok 'true'"
;        (perform-validate-string-as-keyword-test nil err {:nil-ok true} true))
;      (testing "[s err settings]: invalid: s is not a string"
;        (perform-validate-string-as-keyword-test 1 err {:min 1, :max 5} err))
;      (testing "[s err settings]: valid: empty string"
;        (perform-validate-string-as-keyword-test "" err {:min 0, :max 5} err))
;      (testing "[s err settings]: invalid: empty string, less than min"
;        (perform-validate-string-as-keyword-test "" err {:min 1, :max 5} err))
;      (testing "[s err settings]: invalid: string, greater than max"
;        (perform-validate-string-as-keyword-test "hello there" err {:min 1, :max 5} err))
;      ;; - pattern whole string
;      (testing "[s err settings]: invalid: string, pattern whole string fail"
;        (perform-validate-string-as-keyword-test "hello" err {:pat-or-pats-whole digits-whole} err))
;      (testing "[s err settings]: valid: string, pattern whole string"
;        (perform-validate-string-as-keyword-test "hello" err {:pat-or-pats-whole letters-whole} true))
;      (testing "[s err settings]: invalid: string, pattern whole string as collection fail"
;        (perform-validate-string-as-keyword-test "hello" err {:pat-or-pats-whole [digits-whole]} err))
;      (testing "[s err settings]: valid: string, pattern whole string as collection"
;        (perform-validate-string-as-keyword-test "hello" err {:pat-or-pats-whole [letters-whole]} true))
;      (testing "[s err settings]: invalid: string, 2 patterns whole string as collection fail"
;        (perform-validate-string-as-keyword-test "hello" err {:pat-or-pats-whole [digits-whole letters-whole]} err))
;      (testing "[s err settings]: valid: string, 2 patterns whole string as collection"
;        (perform-validate-string-as-keyword-test "hello" err {:pat-or-pats-whole [alnum-whole letters-whole]} true))
;      ;; - pattern substring
;      (testing "[s err settings]: invalid: string, pattern substring fail"
;        (perform-validate-string-as-keyword-test "hello" err {:pat-or-pats-sub digits-sub} err))
;      (testing "[s err settings]: valid: string, pattern substring"
;        (perform-validate-string-as-keyword-test "hel123lo" err {:pat-or-pats-sub digits-sub} true))
;      (testing "[s err settings]: invalid: string, pattern substring as collection fail"
;        (perform-validate-string-as-keyword-test "hello" err {:pat-or-pats-sub [digits-sub]} err))
;      (testing "[s err settings]: valid: string, pattern whole string as collection"
;        (perform-validate-string-as-keyword-test "hel123lo" err {:pat-or-pats-sub [digits-sub]} true))
;      (testing "[s err settings]: invalid: string, 2 patterns substring as collection fail"
;        (perform-validate-string-as-keyword-test "hello" err {:pat-or-pats-sub [digits-sub digits2-sub]} err))
;      (testing "[s err settings]: valid: string, 2 patterns substring as collection"
;        (perform-validate-string-as-keyword-test "hel123lo" err {:pat-or-pats-sub [digits-sub letters-sub]} true))
;      ;; - fn
;      (testing "[s err settings]: invalid: one fn fail"
;        (perform-validate-string-as-keyword-test "hello" err {:fn-or-fns #(if (= "hello" %)
;                                                                            false
;                                                                            true)} err))
;      (testing "[s err settings]: valid: one fn"
;        (perform-validate-string-as-keyword-test "hi" err {:fn-or-fns #(if (= "hello" %)
;                                                                         false
;                                                                         true)} true))
;      (testing "[s err settings]: invalid: one fn as collection fail"
;        (perform-validate-string-as-keyword-test "hello" err {:fn-or-fns [#(if (= "hello" %)
;                                                                             false
;                                                                             true)]} err))
;      (testing "[s err settings]: valid: one fn as collection "
;        (perform-validate-string-as-keyword-test "hi" err {:fn-or-fns [#(if (= "hello" %)
;                                                                          false
;                                                                          true)]} true))
;      (testing "[s err settings]: invalid: two fn as collection fail"
;        (perform-validate-string-as-keyword-test "hello" err {:fn-or-fns [#(if (= "hello" %)
;                                                                             false
;                                                                             true)
;                                                                          #(if (= "howdy" %)
;                                                                             false
;                                                                             true)]} err))
;      (testing "[s err settings]: valid: two fn as collection "
;        (perform-validate-string-as-keyword-test "hi" err {:fn-or-fns [#(if (= "hello" %)
;                                                                          false
;                                                                          true)
;                                                                       #(if (= "howdy" %)
;                                                                          false
;                                                                          true)]} true))
;      ;; - keyword
;      (testing "[s err settings]: invalid: keyword is 1 digit number"
;        (perform-validate-string-as-keyword-test "1" err {} err))
;      (testing "[s err settings]: invalid: keyword is 2 digit number"
;        (perform-validate-string-as-keyword-test "12" err {} err))
;      (testing "[s err settings]: invalid: keyword has leading dash"
;        (perform-validate-string-as-keyword-test "-abc" err {} err))
;      (testing "[s err settings]: invalid: keyword has leading underscore"
;        (perform-validate-string-as-keyword-test "_abc" err {} err))
;      (testing "[s err settings]: invalid: keyword has a space"
;        (perform-validate-string-as-keyword-test "abc def" err {} err))
;      (testing "[s err settings]: invalid: keyword has a colon"
;        (perform-validate-string-as-keyword-test "abc:def" err {} err))
;      (testing "[s err settings]: invalid: keyword has a forward slash"
;        (perform-validate-string-as-keyword-test "abc/def" err {} err))
;      (testing "[s err settings]: valid: keyword single character"
;        (perform-validate-string-as-keyword-test "a" err {} true))
;      (testing "[s err settings]: valid: keyword multi character"
;        (perform-validate-string-as-keyword-test "ab" err {} true)))))
;
;
;(defn perform-validate-number-test
;  [n err settings expected]
;  (let [actual (cond
;                 settings (checks/validate-number n err settings)
;                 err (checks/validate-number n err)
;                 :else (checks/validate-number n))]
;    (is (= actual expected))))
;
;(deftest validate-number-test
;  ;;
;  ;; [n] form
;  (testing "[s]: invalid: n is nil"
;    (perform-validate-number-test nil nil nil false))
;  (testing "[s]: invalid: n is not a number"
;    (perform-validate-number-test "1" nil nil false))
;  (testing "[s]: valid: number"
;    (perform-validate-number-test 1 nil nil true))
;  ;;
;  ;; [n err] form
;  (let [err "Invalid number"]
;    (testing "[n err]: invalid: n is nil"
;      (perform-validate-number-test nil err nil err))
;    (testing "[n err]: invalid: n is not a number"
;      (perform-validate-number-test "1" err nil err))
;    (testing "[n err]: valid: number"
;      (perform-validate-number-test 1 err nil true)))
;  ;;
;  ;; [n err settings] form
;  (let [err "Invalid number"]
;    ;; - w/ empty settings
;    (testing "[n err settings]: invalid: n is nil"
;      (perform-validate-number-test nil err {} err))
;    (testing "[n err settings]: invalid: n is not a number"
;      (perform-validate-number-test "1" err {} err))
;    (testing "[n err settings]: valid: number"
;      (perform-validate-number-test 1 err {} true))
;    ;; - w/ settings define
;    (testing "[n err settings]: invalid: set ':nil-ok' to 'false', n is nil"
;      (perform-validate-number-test nil err {:nil-ok false} err))
;    (testing "[n err settings]: valid: set ':nil-ok' to 'true', n is nil"
;      (perform-validate-number-test nil err {:nil-ok true} true))
;    ;; - w/ type
;    (testing "[n err settings]: invalid: set ':type' to ':int'"
;      (perform-validate-number-test 1.0 err {:type :int} err))
;    (testing "[n err settings]: valid: set ':type' to ':int'"
;      (perform-validate-number-test 1 err {:type :int} true))
;    (testing "[n err settings]: invalid: set ':type' to ':float'"
;      (perform-validate-number-test 123456789 err {:type :float} err))
;    (testing "[n err settings]: valid: set ':type' to ':float'"
;      (perform-validate-number-test 1.0 err {:type :float} true))
;    (testing "[n err settings]: invalid: set ':type' to ':double'"
;      (perform-validate-number-test (bigdec "1e400") err {:type :double} err))
;    (testing "[n err settings]: valid: set ':type' to ':double'"
;      (perform-validate-number-test 1.23456789012345 err {:type :double} true))
;    (testing "[n err settings]: invalid: set ':type' to ':decimal'"
;      (perform-validate-number-test 1/3 err {:type :decimal} err))
;    (testing "[n err settings]: valid: set ':type' to ':decimal'"
;      (perform-validate-number-test 0.1M err {:type :decimal} true))
;    (testing "[n err settings]: invalid: set ':type' to ':ratio'"
;      (perform-validate-number-test 1 err {:type :ratio} err))
;    (testing "[n err settings]: valid: set ':type' to ':ratio'"
;      (perform-validate-number-test 1/3 err {:type :ratio} true))))