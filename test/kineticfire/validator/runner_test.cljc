;; (c) Copyright 2024-2025 patterns Contributors. All rights reserved.
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


(ns kineticfire.validator.runner-test
  (:require [clojure.test :refer :all]
            [kineticfire.validator.runner :as runner]
            [kineticfire.validator.checks :as checks]))


(deftest explain-first-all-pass
  (let [v "abc"
        steps [{:pred string? :code :type/not-string :msg "Expected string"}
               {:pred #(<= 2 (count %)) :code :str/too-short :msg "Too short"}]
        res (runner/explain v steps {:mode :first})]
    (is (= {:valid? true :value v} res))))


(deftest explain-first-one-fails
  (let [v "a"                                               ;; too short
        steps [{:pred string? :code :type/not-string :msg "Expected string"}
               {:pred #(<= 2 (count %)) :code :str/too-short :msg "Too short"}]
        res (runner/explain v steps {:mode :first})]
    (is (= false (:valid? res)))
    (is (= :str/too-short (:code res)))
    (is (= "Too short" (:message res)))
    (is (= v (:value res)))
    (is (= [] (:path res)))))                               ; default nil → []


(deftest explain-all-collects-multiple-failures
  (let [v "1"                                           ;; length 1 → too short; starts with digit → not keyword-safe
        steps [{:pred string? :code :type/not-string :msg "Expected string"}
               {:pred #(<= 2 (count %)) :code :str/too-short :msg "Too short"}
               {:pred #(boolean (re-matches #"^[A-Za-z][A-Za-z0-9_-]*$" %))
                :code :str/not-keyword-safe :msg "Not keyword-safe"}]
        res (runner/explain v steps {:mode :all})]
    (is (= false (:valid? res)))
    (is (= v (:value res)))
    (is (= 2 (count (:errors res))))
    (is (= [:str/too-short :str/not-keyword-safe]
           (vec (map :code (:errors res)))))
    (is (= ["Too short" "Not keyword-safe"]
           (vec (map :message (:errors res)))))))


(deftest explain-defaults-for-bare-fns
  (let [v "x"
        ;; one bare fn will fail (needs length >= 2)
        steps [(fn [s] (string? s))
               (fn [s] (<= 2 (count s)))]
        res (runner/run-all v steps {:code :check/failed :msg "Predicate failed"})]
    (is (= false (:valid? res)))
    (is (= 1 (count (:errors res))))
    (is (= :check/failed (-> res :errors first :code)))
    (is (= "Predicate failed" (-> res :errors first :message)))))


(deftest explain-when-guard
  (testing "guard skips step (treated as pass)"
    (let [v {:age 20 :country "CA"}
          steps [{:pred   #(>= % 21)
                  :select :age
                  :when   #(= "US" (:country %))
                  :code   :age/min
                  :msg    "Must be >= 21"}]
          res (runner/explain v steps {:mode :first})]
      (is (= {:valid? true :value v} res))))
  (testing "guard true → predicate executes and fails"
    (let [v {:age 20 :country "US"}
          steps [{:pred   #(>= % 21)
                  :select :age
                  :when   #(= "US" (:country %))
                  :code   :age/min
                  :msg    "Must be >= 21"}]
          res (runner/explain v steps {:mode :first})]
      (is (= false (:valid? res)))
      (is (= :age/min (:code res)))
      (is (= "Must be >= 21" (:message res))))))


(deftest explain-select-and-path
  (let [v {:user {:name "A"}}
        steps [{:pred   #(<= 2 (count %))
                :select #(get-in % [:user :name])
                :path   [:user :name]
                :code   :name/too-short
                :msg    #(str "Name too short: " %)
                :via    :string/len}]
        res (runner/explain v steps {:mode :first :path [:root]})]
    (is (= false (:valid? res)))
    (is (= :name/too-short (:code res)))
    (is (= "Name too short: A" (:message res)))
    (is (= [:root :user :name] (:path res)))
    (is (= :string/len (:via res)))
    (is (= v (:value res)))))


(deftest explain-single-step-non-seq
  (let [v "abc"
        step {:pred string? :code :type/not-string :msg "Expected string"}]
    (is (= {:valid? true :value v}
           (runner/explain v step {:mode :first})))
    (is (= {:valid? true :value v}
           (runner/explain v step {:mode :all})))))


(deftest run-first-and-run-all-wrappers
  (let [v "x"
        steps [{:pred string? :code :type/not-string :msg "Expected string"}
               {:pred #(<= 2 (count %)) :code :str/too-short :msg "Too short"}]]
    (is (= :str/too-short
           (-> (runner/run-first v steps) :code)))
    (is (= [:str/too-short]
           (->> (runner/run-all v steps) :errors (map :code) vec)))))


(deftest message-can-be-fn-or-string
  (let [v "a"
        steps [{:pred #(<= 2 (count %))
                :code :str/too-short
                :msg  #(str "Too short by " (- 2 (count %)))}]
        res (runner/explain v steps {:mode :first})]
    (is (= false (:valid? res)))
    (is (= "Too short by 1" (:message res))))
  (let [v "a"
        steps [{:pred #(<= 2 (count %))
                :code :str/too-short
                :msg  "Too short"}]
        res (runner/explain v steps {:mode :first})]
    (is (= "Too short" (:message res)))))


(deftest all-pass-in-all-mode
  (let [v "abcd"
        steps [{:pred string? :code :type/not-string}
               {:pred #(<= 2 (count %)) :code :str/too-short}
               {:pred #(boolean (re-matches #"^[A-Za-z]+$" %))
                :code :alpha/only}]]
    (is (= {:valid? true :value v}
           (runner/explain v steps {:mode :all})))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; run-checks tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest run-checks-empty-collection
  (testing "Empty collection returns error"
    (let [res (runner/run-checks [])]
      (is (= false (:valid? res)))
      (is (= :checks/no-functions (:code res)))
      (is (= "No validation functions provided." (:message res)))
      (is (= [] (:value res)))))
  (testing "Nil collection returns error"
    (let [res (runner/run-checks nil)]
      (is (= false (:valid? res)))
      (is (= :checks/no-functions (:code res)))
      (is (= "No validation functions provided." (:message res)))
      (is (= nil (:value res))))))


(deftest run-checks-all-boolean-true
  (testing "All functions return true"
    (let [res (runner/run-checks [#(string? "test")
                                  #(number? 42)
                                  #(coll? [1 2 3])])]
      (is (= true res)))))


(deftest run-checks-boolean-false-failure
  (testing "First function returns false"
    (let [res (runner/run-checks [#(string? 42)      ; false
                                  #(number? 42)])]    ; would be true
      (is (= false res))))
  (testing "Later function returns false"
    (let [res (runner/run-checks [#(string? "test")  ; true
                                  #(number? "42")     ; false
                                  #(coll? [1 2 3])])] ; would be true
      (is (= false res)))))


(deftest run-checks-all-explain-valid
  (testing "All explain-style functions return valid"
    (let [res (runner/run-checks [#(do {:valid? true :value "test"})
                                  #(do {:valid? true :value 42})
                                  #(do {:valid? true :value [1 2 3]})])]
      (is (= true res)))))


(deftest run-checks-explain-failure
  (testing "First explain-style function fails"
    (let [error-map {:valid? false :code :test/error :message "Test error" :value 42}
          res (runner/run-checks [#(do error-map)
                                  #(do {:valid? true :value "test"})])]
      (is (= error-map res))))
  (testing "Later explain-style function fails"
    (let [error-map {:valid? false :code :test/error :message "Test error" :value "bad"}
          res (runner/run-checks [#(do {:valid? true :value "test"})
                                  #(do error-map)
                                  #(do {:valid? true :value [1 2 3]})])]
      (is (= error-map res)))))


(deftest run-checks-mixed-boolean-and-explain
  (testing "Mix of boolean and explain-style functions - all pass"
    (let [res (runner/run-checks [#(string? "test")                           ; boolean true
                                  #(do {:valid? true :value 42})              ; explain valid
                                  #(coll? [1 2 3])                            ; boolean true
                                  #(do {:valid? true :value {:a 1}})])]        ; explain valid
      (is (= true res))))
  (testing "Mix of boolean and explain-style functions - boolean fails"
    (let [res (runner/run-checks [#(string? "test")                           ; boolean true
                                  #(number? "42")                             ; boolean false
                                  #(do {:valid? true :value [1 2 3]})])]       ; would be valid
      (is (= false res))))
  (testing "Mix of boolean and explain-style functions - explain fails"
    (let [error-map {:valid? false :code :test/error :message "Test error"}
          res (runner/run-checks [#(string? "test")                           ; boolean true
                                  #(do error-map)                             ; explain invalid
                                  #(coll? [1 2 3])])]                         ; would be true
      (is (= error-map res)))))


(deftest run-checks-with-actual-validation-functions
  (testing "Real validation functions - all pass"
    (let [res (runner/run-checks [#(checks/string? "test" {:min 1})
                                  #(checks/number? 42 {:type :int :min 0})
                                  #(checks/collection? [1 2 3] {:type :vec :min 1})])]
      (is (= true res))))
  (testing "Real validation functions - one fails boolean"
    (let [res (runner/run-checks [#(checks/string? "test" {:min 10})               ; false - too short
                                  #(checks/number? 42 {:type :int :min 0})])]       ; would be true
      (is (= false res))))
  (testing "Real explain-style validation functions - all pass"
    (let [res (runner/run-checks [#(checks/string-explain "test" {:min 1})
                                  #(checks/number-explain 42 {:type :int :min 0})
                                  #(checks/collection-explain [1 2 3] {:type :vec :min 1})])]
      (is (= true res))))
  (testing "Real explain-style validation functions - one fails"
    (let [res (runner/run-checks [#(checks/string-explain "test" {:min 1})   ; valid
                                  #(checks/string-explain "" {:min 1})       ; invalid - too short
                                  #(checks/number-explain 42 {:type :int})])] ; would be valid
      (is (= false (:valid? res)))
      (is (= :string/too-short (:code res)))
      (is (string? (:message res)))
      (is (= "" (:value res))))))


(deftest run-checks-fail-fast-behavior
  (testing "Functions are not called after first failure"
    (let [call-count (atom 0)
          increment-fn #(do (swap! call-count inc) true)
          fail-fn #(do (swap! call-count inc) false)
          pass-fn #(do (swap! call-count inc) true)
          res (runner/run-checks [increment-fn   ; called, returns true
                                  fail-fn        ; called, returns false - stops here
                                  pass-fn])]     ; NOT called due to fail-fast
      (is (= false res))
      (is (= 2 @call-count) "Should only call first 2 functions"))))
