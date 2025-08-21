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
;; Runner
;;
;; The orchestration layer: take a value, run it through one or more validation
;; “steps,” and return explain-style results. This namespace does not perform
;; domain-specific validation itself—its role is to execute, aggregate, and
;; structure results from checks.
;; -----------------------------------------------------------------------------


(ns kineticfire.validator.runner)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Step model
;;
;; A step describes *how* to validate a value. You can provide it in either form:
;;
;; 1) Predicate form (short, boolean):
;;      (fn [v] ...)  ; => true/false
;;    Provide metadata or wrap via `mk-step` to attach :code/:msg if you want.
;;
;; 2) Map form (explicit):
;;    {:pred     (fn [v] ...)            ; required, true/false
;;     :code     :some/error             ; keyword, required for failures
;;     :msg      "Human message" | (fn [v] ...) ; optional
;;     :expected any                     ; optional, anything useful to callers
;;     :when     (fn [v] ...)            ; optional guard: only run when true
;;     :select   (fn [v] ...)            ; optional: validate a derived subvalue
;;     :path     [:field]                ; optional extra context appended to opts :path
;;     :via      :string/len             ; optional “which check” label (for traces)
;;    }
;;
;; Runner options:
;;   {:mode :first | :all, :path []}
;;
;; Returns either:
;;   {:valid? true  :value v}
;; or (mode :first)
;;   {:valid? false :code ... :message ... :value v :path [...] :expected ... :via ...}
;; or (mode :all)
;;   {:valid? false :errors [<error> ...] :value v}
;; where each <error> has the same shape as the single error above.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn- truthy->bool
  "Coerce any truthy value to a strict boolean."
  [x]
  (if x true false))


(defn mk-step
  "Normalize a user-provided step into the explicit map form.
   If `s` is a function, you must supply at least a :code (and optionally :msg, etc.)
   via the `defaults` map."
  [s defaults]
  (cond
    (map? s)
    (merge {:when (constantly true)} s)                     ; ensure :when exists
    (fn? s)
    (merge {:pred s
            :when (constantly true)}
           defaults)
    :else
    (throw (ex-info "Step must be a map or a fn" {:got (type s)}))))


(defn- compute-message
  "Resolve a step message: string or (fn [v] ...)."
  [msg v]
  (cond
    (nil? msg) nil
    (fn? msg) (msg v)
    :else msg))


(defn- conj-path
  "Combine runner path with step path (both optional)."
  [runner-path step-path]
  (vec (concat (or runner-path []) (or step-path []))))


(defn- explain-one
  "Run one normalized step against `v`.
   Returns {:valid? true :value v} on pass; error map on fail (with :valid? false)."
  [v {:keys [pred code msg expected when select path via] :as _step} base-path]
  (let [guard-ok? (truthy->bool (when v))
        v* (if select (select v) v)]
    (if (not guard-ok?)
      {:valid? true :value v}                               ; guard says don't evaluate = treat as pass
      (if (truthy->bool (pred v*))
        {:valid? true :value v}
        {:valid?   false
         :code     code
         :message  (compute-message msg v*)
         :expected expected
         :path     (conj-path base-path path)
         :value    v
         :via      via}))))


(defn explain
  "Run one or more validation steps against value `v`.

   steps     : a single step or a collection of steps (see header for shape)
   opts      : {:mode :first | :all, :path [...]}
   defaults  : default step fields (used when a step is just a fn), e.g.
               {:code :my/error :msg \"failed\" :via :my/check}

   Returns:
     - mode :first (default): first failing error map or {:valid? true :value v}
     - mode :all            : {:valid? boolean, :errors [...], :value v}
  "
  ([v steps] (explain v steps {} {}))
  ([v steps opts] (explain v steps opts {}))
  ([v steps {:keys [mode path] :or {mode :first}} defaults]
   (let [steps* (if (sequential? steps) steps [steps])
         ;; normalize all steps so runner logic is uniform
         steps** (map #(mk-step % defaults) steps*)
         run1 #(explain-one v % path)]
     (case mode
       :all
       (let [errs (->> steps** (map run1) (remove :valid?) vec)]
         (if (seq errs)
           {:valid? false :errors errs :value v}
           {:valid? true :value v}))

       ;; :first
       (if-let [err (some (fn [st]
                            (let [r (run1 st)]
                              (when-not (:valid? r) r)))
                          steps**)]
         err
         {:valid? true :value v})))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Convenience helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run-all
  "Fail-accumulating convenience: same as (explain v steps {:mode :all} defaults)."
  ([v steps] (explain v steps {:mode :all} {}))
  ([v steps defaults] (explain v steps {:mode :all} defaults)))


(defn run-first
  "Fail-fast convenience: same as (explain v steps {:mode :first} defaults)."
  ([v steps] (explain v steps {:mode :first} {}))
  ([v steps defaults] (explain v steps {:mode :first} defaults)))
