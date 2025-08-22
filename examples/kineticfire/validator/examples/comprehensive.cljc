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
;; Comprehensive Library Examples
;;
;; A complete demonstration of the `kineticfire.validator` library showing
;; all major components working together:
;;   • Basic validation checks (string?, number?, collection?, etc.)
;;   • Runner orchestration for multi-step validation
;;   • Result utilities for processing validation outputs
;;   • Pattern helpers for regex-based validation
;;   • Integration patterns and best practices
;;
;; This file provides a high-level overview of the entire library.
;; For detailed examples of specific components, see:
;;   • basic.cljc - focused examples of basic validation checks
;;   • runner.cljc - runner orchestration examples (future)
;;   • result.cljc - result utility examples (future)
;;   • patterns.cljc - pattern helper examples (future)
;;
;; Run from the command line using:
;;   lein examples
;; or explicitly:
;;   lein with-profile +dev run -m kineticfire.validator.examples.comprehensive
;; -----------------------------------------------------------------------------


(ns kineticfire.validator.examples.comprehensive
  (:require
    [kineticfire.validator.checks :as checks]
    [kineticfire.validator.runner :as runner]
    [kineticfire.validator.result :as result]))


(defn demo-validation-workflow []
  (println "

=== Comprehensive Validation Workflow ===")
  
  ;; Example: Validating user registration data
  (let [user-data {:username "john_doe"
                   :email "john@example.com"
                   :age 25
                   :tags ["developer" "clojure"]}]
    
    (println "
Validating user registration data:")
    (println "  Data:" user-data)
    
    ;; Individual field validation
    (println "
Individual field checks:")
    (println "  username valid:" (checks/string-as-keyword? (:username user-data)))
    (println "  email valid:" (checks/string? (:email user-data) {:min 5}))
    (println "  age valid:" (checks/number? (:age user-data) {:type :int :min 0 :max 120}))
    (println "  tags valid:" (checks/collection? (:tags user-data) {:type :vec :min 1}))
    
    ;; Using runner for orchestrated validation
    (println "
Using runner for multi-step validation:")
    (let [validation-steps [
           {:pred #(checks/string-as-keyword? (:username %))
            :code :user/invalid-username
            :msg "Username must be keyword-safe"}
           {:pred #(checks/string? (:email %) {:min 5})
            :code :user/invalid-email  
            :msg "Email must be a string with at least 5 characters"}
           {:pred #(checks/number? (:age %) {:type :int :min 0 :max 120})
            :code :user/invalid-age
            :msg "Age must be an integer between 0 and 120"}
           {:pred #(checks/collection? (:tags %) {:type :vec :min 1})
            :code :user/invalid-tags
            :msg "Tags must be a non-empty vector"}]
          
          result (runner/explain user-data validation-steps)]
      
      (println "  Validation result:" result)
      (println "  Is valid?:" (result/valid? result))
      
      ;; Demonstrate failure case
      (let [bad-data {:username "123bad" :email "" :age -5 :tags nil}
            bad-result (runner/explain bad-data validation-steps {:mode :all})]
        (println "
Bad data validation:")
        (println "  Bad data:" bad-data)
        (println "  Result:" bad-result)
        (println "  Errors:" (result/errors bad-result))))))


(defn demo-result-processing []
  (println "

=== Result Processing Patterns ===")
  
  ;; Show different ways to handle validation results
  (let [good-string "valid-input"
        bad-string 123]
    
    (println "
Different result processing approaches:")
    
    ;; Simple boolean check
    (println "  Simple check - good:" (checks/string? good-string))
    (println "  Simple check - bad:" (checks/string? bad-string))
    
    ;; Explain with error details
    (let [good-result (checks/string-explain good-string)
          bad-result (checks/string-explain bad-string)]
      (println "  Explain good:" good-result)
      (println "  Explain bad:" bad-result)
      
      ;; Collapse to custom error
      (println "  Collapse good:" (result/collapse-result good-result "Custom error"))
      (println "  Collapse bad:" (result/collapse-result bad-result "Custom error"))
      
      ;; Extract specific error info
      (println "  Error details:" (result/errors bad-result)))))


(defn demo-integration-patterns []
  (println "

=== Integration Patterns ===")
  
  (println "
Common integration scenarios:")
  
  ;; API endpoint validation
  (println "
API endpoint validation pattern:")
  (let [api-request {:user-id "abc123"
                     :data [1 2 3]
                     :options {:format "json"}}
        
        validate-request (fn [req]
                          (let [results [(checks/string-explain (:user-id req) {:min 1})
                                        (checks/collection-explain (:data req) {:min 1})
                                        (checks/collection-explain (:options req) {:type :assoc})]]
                            (result/combine-results results)))]
    
    (println "  Request:" api-request)
    (println "  Validation:" (validate-request api-request)))
  
  ;; Configuration validation
  (println "
Configuration validation pattern:")
  (let [config {:port 8080
                :host "localhost"
                :workers 4
                :features ["auth" "logging"]}
        
        validate-config (fn [cfg]
                         (runner/run-all cfg [
                           {:pred #(checks/number? (:port %) {:type :int :min 1 :max 65535})
                            :code :config/invalid-port}
                           {:pred #(checks/string? (:host %) {:min 1})
                            :code :config/invalid-host}
                           {:pred #(checks/number? (:workers %) {:type :int :min 1})
                            :code :config/invalid-workers}
                           {:pred #(checks/collection? (:features %) {:type :vec})
                            :code :config/invalid-features}]))]
    
    (println "  Config:" config)
    (println "  Validation:" (validate-config config))))


#?(:clj
   (defn -main [& _args]
     (demo-validation-workflow)
     (demo-result-processing)
     (demo-integration-patterns)))