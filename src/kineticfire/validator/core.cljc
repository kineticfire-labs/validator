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


(defn valid-string?
  ([s]
   (valid-string? s false {}))
  ([s err]
   (valid-string? s err {}))
  ([s err settings]
   (if (nil? s)
     (if (:nil-ok settings)
       true
       err)
     (if-not (string? s)
       err
       (let [length (if (or
                          (:min settings)
                          (:max settings))
                      (count s)
                      -1)
             min-valid (if (and
                             (> length -1)
                             (:min settings)
                             (< length (:min settings)))
                         false
                         true)]
         (if-not min-valid
           err
           (let [max-valid (if (and
                                 (> length -1)
                                 (:max settings)
                                 (> length (:max settings)))
                             false
                             true)]
             (if-not max-valid
               err
               true))))))))
;; todo pat-or-pats-whole, pat-or-pats-sub
;; todo fn-or-fns



