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
;; Example Usage
;;
;; A simple demonstration of how to consume the `kineticfire.validator` library.
;; This file shows how to:
;;   • call high-level `checks` functions (e.g., `string?`, `string-explain`)
;;   • use `runner` to orchestrate multiple validation steps
;;   • use `result` utilities to collapse or interpret explain-style outputs
;;
;; It is meant for development and documentation purposes only. This namespace
;; is not shipped or used by the library itself.
;;
;; Run from the command line using:
;;   lein example
;; or explicitly:
;;   lein with-profile +dev run -m kineticfire.validator.examples.example
;; -----------------------------------------------------------------------------


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


(defn demo-collection []
  (println "

=== Collection checks ===")
  (let [vec-data [1 2 3 4]
        list-data '(a b c)
        set-data #{:x :y :z}
        map-data {:name "John" :age 30}
        bad-data "not-a-collection"
        duplicates [1 2 2 3]
        with-nils [1 nil 3]]

    (println "
Basic collection checks:")
    (println "  collection? vec:" (checks/collection? vec-data))
    (println "  collection? string:" (checks/collection? bad-data))
    (println "  collection? nil (default):" (checks/collection? nil))
    (println "  collection? nil (allowed):" (checks/collection? nil {:nil-ok true}))

    (println "
Type-specific checks:")
    (println "  vector as :vec:" (checks/collection? vec-data {:type :vec}))
    (println "  vector as :list:" (checks/collection? vec-data {:type :list}))
    (println "  map as :assoc:" (checks/collection? map-data {:type :assoc}))

    (println "
Size constraints:")
    (println "  vec with min 3:" (checks/collection? vec-data {:min 3}))
    (println "  vec with max 3:" (checks/collection? vec-data {:max 3}))

    (println "
Duplicate and nil value checks:")
    (println "  duplicates allowed:" (checks/collection? duplicates))
    (println "  duplicates forbidden:" (checks/collection? duplicates {:duplicates-ok false}))
    (println "  nil values allowed:" (checks/collection? with-nils))
    (println "  nil values forbidden:" (checks/collection? with-nils {:nil-value-ok false}))

    (println "
Explain examples:")
    (println "  explain bad type:" (checks/collection-explain bad-data))
    (println "  explain too large:" (checks/collection-explain vec-data {:max 3}))
    (println "  explain duplicates:" (checks/collection-explain duplicates {:duplicates-ok false}))

    ;; Collapse example
    (println "
Collapse result:")
    (println "  collapse (good) ->" 
             (result/collapse-result (checks/collection-explain vec-data) "Invalid collection"))
    (println "  collapse (bad)  ->" 
             (result/collapse-result (checks/collection-explain bad-data) "Invalid collection"))))


#?(:clj
   (defn -main [& _args]
     (demo-basic)
     (demo-runner)
     (demo-collection)))
