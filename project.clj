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


(defproject com.kineticfire/validator "1.0.0-SNAPSHOT"
  :description "Clojure/ClojureScript/Babashka validation library"
  :url "https://github.com/kineticfire-labs/validator/"
  :license {:name "Apache License, Version 2.0"
            :url  "https://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [org.clojure/clojurescript "1.11.132"]]
  :plugins [[lein-cljsbuild "1.1.8"]]
  :cljsbuild {:builds [{:id           "main"
                        :source-paths ["src"]
                        :compiler     {:output-to     "target/validator.js"
                                       :optimizations :advanced
                                       :pretty-print  false}}]}
  :repositories [["releases" {:url   "https://repo.clojars.org"
                              :creds :gpg}]]
  :main ^:skip-aot validator.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :test    {:dependencies [[com.kineticfire/collections "2.1.1"]]}
             :dev {:source-paths ["examples"]}}
  :source-paths ["src"]
  :aliases {"example" ["with-profile" "+dev" "run" "-m" "kineticfire.validator.examples.example"]})
