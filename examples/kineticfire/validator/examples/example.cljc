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


;; run with:
;;    lein example
;; or:
;;    lein with-profile +dev run -m kineticfire.validator.examples.example


(ns kineticfire.validator.examples.example
  (:require
    [kineticfire.validator.checks :as checks]               ;; string?, string-explain, etc.
    [kineticfire.validator.runner :as runner]               ;; explain, run-first, run-all
    [kineticfire.validator.result :as result])              ;; collapse-result
  #?(:clj  (:import (java.util.regex Pattern))
     :cljs (:require [goog.string :as gstring])))


(def ^:private alpha-whole
  #?(:clj  (re-pattern "^[A-Za-z]+$")
     :cljs (re-pattern "^[A-Za-z]+$")))


(def ^:private has-digits
  #?(:clj  (re-pattern "[0-9]+")
     :cljs (re-pattern "[0-9]+")))


(defn demo-basic []
  (println "\n\n=== Basic checks ===")
  (let [ok "Hello"
        bad "123"
        opts {:min 1 :max 10}
        ok? (checks/string? ok opts)
        bad? (checks/string? bad opts)
        expl1 (checks/string-explain ok opts)
        expl2 (checks/string-explain bad (merge opts {:pat-or-pats-whole alpha-whole}))]

    (println "\nString checks:")
    (println "  string? ok:" ok?)
    (println "  string? bad:" bad?)
    (println "  string-explain ok:" expl1)
    (println "  string-explain bad (alpha-whole):" expl2)

    ;; Collapse an explain result to true/err:
    (println "\nCollapse result:")
    (println "  collapse-result (ok)   ->"
             (result/collapse-result expl1 "Invalid string"))
    (println "  collapse-result (bad)  ->"
             (result/collapse-result expl2 "Invalid string"))))


(defn demo-runner []
  (println "\n\n=== Runner examples ===\n")
  (let [v1 "abc123"
        v2 "!!!"
        steps
        [;; must be a string, min 2
         {:pred #(checks/string? % {:min 2})
          :code :string/min
          :msg  "Min length 2"}

         ;; must contain digits (substring)
         {:pred #(checks/string? % {:pat-or-pats-sub has-digits})
          :code :string/digits
          :msg  "Must contain digits"}

         ;; whole-string alpha only (will fail for v1)
         {:pred #(checks/string? % {:pat-or-pats-whole alpha-whole})
          :code :string/alpha
          :msg  "Must be all letters"}]]

    ;; Fail-fast (first failure)
    (println "  run-first on v1:" (runner/run-first v1 steps))
    (println "  run-first on v2:" (runner/run-first v2 steps))

    ;; Accumulate all failures
    (println "  run-all   on v1:" (runner/run-all v1 steps))
    (println "  run-all   on v2:" (runner/run-all v2 steps))

    ;; You can still collapse a runner/explain result if you want a simple outcome:
    (println "\nCollapse result:")
    (println "  collapse (run-first v2) ->"
             (result/collapse-result (runner/run-first v2 steps) "Invalid value"))))


#?(:clj
   (defn -main [& _args]
     (demo-basic)
     (demo-runner)))
