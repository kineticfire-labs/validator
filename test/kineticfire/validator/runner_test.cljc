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
            [kineticfire.validator.runner :as runner]))


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
