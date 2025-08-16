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


(ns kineticfire.validator.core
  (:gen-class))


(def ^:const string-min 0)
(def ^:const string-max Integer/MAX_VALUE)
(def ^:const string-nil-ok false)


(defn- val1-if-not-nil-else-val2
  [val1 val2]
  (if (nil? val1)
    val2
    val1))


(defn valid-string?
  [data settings]
  (let [{:keys [min-in max-in nil-ok-in pattern fn]} settings
        min (val1-if-not-nil-else-val2 min-in string-min)
        max (val1-if-not-nil-else-val2 max-in string-max)
        nil-ok (val1-if-not-nil-else-val2 nil-ok-in string-nil-ok)]
    (if (nil? data)
      (if nil-ok
        true
        false)
      (if-not (string? data)
        false
        (let [length (count data)]
          (if (< length min)
            false
            (if (> length max)
              false
              true)))))
    ))



