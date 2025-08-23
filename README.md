# validator
[![Powered by KineticFire Labs](https://img.shields.io/badge/Powered_by-KineticFire_Labs-CDA519?link=https%3A%2F%2Flabs.kineticfire.com%2F)](https://labs.kineticfire.com/)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Clojars Project](https://img.shields.io/clojars/v/com.kineticfire/validator.svg)](https://clojars.org/com.kineticfire/validator)
<p></p>

Streamlined data validation for [Clojure](https://clojure.org/), [ClojureScript](https://clojurescript.org/), and [Babashka](https://babashka.org/) with zero dependencies, comprehensive validation functions, and clear error reporting that keeps your code readable and maintainable.

# Contents
1. [Motivation](#motivation)
2. [Installation](#installation)
3. [Usage](#usage)
4. [Documentation](#documentation)
5. [License](#license)


# Motivation

Data validation often leads to complex, deeply nested code that becomes difficult to read and maintain. Traditional validation approaches result in code that "marches to the right" with multiple nested conditionals, making it hard to understand validation logic and error handling.

The *validator* library solves this by providing a simple, consistent API for data validation that keeps your code linear and readable:

```clojure
;; Before: Complex, nested validation
(defn validate-user [user]
  (cond
    (not (string? (:name user)))
    {:error "Name must be a string"}
    
    (< (count (:name user)) 1)
    {:error "Name cannot be empty"}
    
    (not (and (string? (:email user)) (re-find #"@" (:email user))))
    {:error "Email must be valid"}
    
    (not (and (number? (:age user)) (>= (:age user) 0)))
    {:error "Age must be a positive number"}
    
    :else
    {:valid true :data user}))

;; After: Clean, readable validation with validator
(ns my-app.validation
  (:require [kineticfire.validator.checks :as checks]
            [kineticfire.validator.result :as result]))

(defn validate-user [user]
  (let [name-check (checks/string-explain (:name user) {:min 1})
        email-check (checks/string-explain (:email user) {:pat-or-pats-sub #"@"})
        age-check (checks/number-explain (:age user) {:type :int :min 0})]
    (if (and (:valid? name-check) (:valid? email-check) (:valid? age-check))
      {:valid true :data user}
      {:error "Validation failed" :details [name-check email-check age-check]})))
```

Key benefits:
- **Clear, consistent API** across all validation functions
- **Detailed error reporting** with codes, messages, and context
- **Zero dependencies** - works anywhere Clojure runs
- **Cross-platform** - supports Clojure, ClojureScript, and Babashka
- **Composable validation** - combine simple checks into complex workflows


# Installation

The *validator* library can be installed from [Clojars](https://clojars.org/com.kineticfire/validator) using
one of the following methods:

## Leiningen/Boot

```
[com.kineticfire/validator "1.0.0"]
```

## Clojure CLI/deps.edn

```
com.kineticfire/validator {:mvn/version "1.0.0"}
```

## Gradle

```
implementation("com.kineticfire:validator:1.0.0")
```

## Maven

```
<dependency>
  <groupId>com.kineticfire</groupId>
  <artifactId>validator</artifactId>
  <version>1.0.0</version>
</dependency>
```

# Usage

## Basic Validation

Require the main validation namespace and start validating data:

```clojure
(ns my-app.core
  (:require [kineticfire.validator.checks :as checks]))

;; Boolean validation - returns true/false
(checks/string? "hello")
;;=> true

(checks/string? "hello" {:min 10})
;;=> false

(checks/number? 42 {:type :int :min 0 :max 100})
;;=> true

;; Explain validation - returns detailed results
(checks/string-explain "hi" {:min 5})
;;=> {:valid? false
;;    :code :string/too-short
;;    :message "String shorter than min length 5."
;;    :expected {:min 5}
;;    :value "hi"}

(checks/number-explain 150 {:max 100})
;;=> {:valid? false
;;    :code :number/too-large
;;    :message "Number is larger than max 100."
;;    :expected {:max 100}
;;    :value 150}

;; Collection validation
(checks/collection? [1 2 3] {:type :vec :min 2})
;;=> true

;; Map entry validation (nested key support)
(checks/map-entry? {:user {:name "Alice"}} [:user :name] {:type :string})
;;=> true
```

## Runner Orchestration

For complex validation workflows, use the runner to orchestrate multiple validation steps:

```clojure
(ns my-app.validation
  (:require [kineticfire.validator.checks :as checks]
            [kineticfire.validator.runner :as runner]))

;; Define validation steps
(def user-validation-steps
  [{:pred #(checks/string-as-keyword? (:username %))
    :code :user/invalid-username
    :msg "Username must be keyword-safe"}
   
   {:pred #(checks/string? (:email %) {:min 5})
    :code :user/invalid-email
    :msg "Email must be at least 5 characters"}
   
   {:pred #(checks/number? (:age %) {:type :int :min 13 :max 120})
    :code :user/invalid-age
    :msg "Age must be between 13 and 120"}])

;; Run validation (fail-fast mode)
(runner/explain user-data user-validation-steps)
;;=> {:valid? true :value user-data}  ; or first error

;; Run validation (collect all errors)
(runner/explain user-data user-validation-steps {:mode :all})
;;=> {:valid? false 
;;    :errors [{:code :user/invalid-email ...} {:code :user/invalid-age ...}]
;;    :value user-data}
```

## Result Processing

Process and transform validation results for your application needs:

```clojure
(ns my-app.results
  (:require [kineticfire.validator.checks :as checks]
            [kineticfire.validator.result :as result]))

(let [validation-result (checks/string-explain "test" {:min 10})]
  ;; Check if valid
  (result/valid? validation-result)
  ;;=> false
  
  ;; Collapse to simple true/error
  (result/collapse-result validation-result "Invalid input")
  ;;=> "Invalid input"
  
  ;; Extract error details
  (result/errors validation-result)
  ;;=> [{:code :string/too-short :message "String shorter than min length 10."}])
```

## Control Flow Integration (Optional)

**Note**: The [clojure-control-flow](https://github.com/kineticfire-labs/clojure-control-flow) library is **NOT required** to use the validator library, but can be helpful for managing complex validation workflows and preventing deeply nested validation code.

```clojure
;; Option 1: Standard approach (works perfectly with validator)
(defn validate-registration [form-data]
  (let [username-result (checks/string-as-keyword-explain (:username form-data) {:min 3})]
    (if (:valid? username-result)
      (let [email-result (checks/string-explain (:email form-data) {:pat-or-pats-whole #"^[^@]+@[^@]+\.[^@]+$"})]
        (if (:valid? email-result)
          (let [age-result (checks/number-explain (:age form-data) {:type :int :min 13})]
            (if (:valid? age-result)
              {:valid? true :data form-data}
              age-result))
          email-result))
      username-result)))

;; Option 2: With optional clojure-control-flow (for enhanced readability)
(require '[kineticfire.control-flow :refer [continue->]])

(defn validate-registration [form-data]
  (continue-> form-data
              #(get-in % [:validation :valid?] true)  ; continue condition
              (validate-username)
              (validate-email) 
              (validate-password)
              (validate-age)
              (validate-terms)))

(defn validate-username [data]
  (let [result (checks/string-as-keyword-explain (:username data) {:min 3})]
    (assoc data :validation result)))

(defn validate-email [data]
  (let [result (checks/string-explain (:email data) {:pat-or-pats-whole #"^[^@]+@[^@]+\.[^@]+$"})]
    (assoc data :validation result)))
```

The validator library works perfectly on its own. For developers who prefer enhanced control flow, the optional integration with clojure-control-flow can help prevent "marching to the right" in complex validation scenarios.

## Examples

Comprehensive examples demonstrating all library features are available in the `examples/` directory:

- Run basic validation examples: `lein basic`
- Run comprehensive integration examples: `lein examples`
- View example source: `examples/kineticfire/validator/examples/`

# Documentation

## Basic Checks (`kineticfire.validator.checks`)

The core validation functions that form the foundation of the library. Each function comes in two forms: a boolean predicate (returns `true`/`false`) and an explain variant (returns detailed result maps).

### string?

```clojure
(string? s)
(string? s settings)
```

Boolean predicate that validates a string with optional constraints.

**Settings Map:**
- `:nil-ok` - boolean, allow nil as valid (default: false)
- `:min` - integer, minimum length inclusive (optional)
- `:max` - integer, maximum length inclusive (optional)
- `:pat-or-pats-whole` - Pattern or collection of Patterns, all must match whole string (optional)
- `:pat-or-pats-sub` - Pattern or collection of Patterns, all must match as substring (optional)
- `:fn-or-fns` - function or collection of functions, each must return truthy (optional)

```clojure
(string? "hello")
;;=> true

(string? "hello" {:min 3 :max 10})
;;=> true

(string? "hi" {:min 5})
;;=> false

(string? "test@example.com" {:pat-or-pats-whole #"^[^@]+@[^@]+\.[^@]+$"})
;;=> true

(string? "Hello World" {:fn-or-fns #(.startsWith % "Hello")})
;;=> true
```

### string-explain

```clojure
(string-explain s)
(string-explain s settings)
```

Explain-style validator for strings. Returns `{:valid? true :value s}` on success, or detailed error map on failure.

**Error Codes:**
- `:string/nil` - Value is nil
- `:type/not-string` - Expected string, got different type
- `:string/too-short` - String shorter than minimum length
- `:string/too-long` - String longer than maximum length
- `:string/regex-whole-failed` - Whole-string pattern match failed
- `:string/regex-substr-failed` - Substring pattern match failed
- `:string/predicate-failed` - Custom predicate returned false

```clojure
(string-explain "hello")
;;=> {:valid? true :value "hello"}

(string-explain "hi" {:min 5})
;;=> {:valid? false
;;    :code :string/too-short
;;    :message "String shorter than min length 5."
;;    :expected {:min 5}
;;    :value "hi"}

(string-explain 42)
;;=> {:valid? false
;;    :code :type/not-string
;;    :message "Expected string, got java.lang.Long."
;;    :value 42}
```

### string-as-keyword?

```clojure
(string-as-keyword? s)
(string-as-keyword? s settings)
```

Boolean predicate that validates if a string can be safely converted to a Clojure keyword. Uses the same settings as `string?` but enforces additional constraints: `:min` defaults to 1 and is clamped to at least 1, and the string must match the keyword-safe pattern `^[a-zA-Z][a-zA-Z0-9_-]*$`.

```clojure
(string-as-keyword? "valid-keyword")
;;=> true

(string-as-keyword? "valid_keyword_123")
;;=> true

(string-as-keyword? "123invalid")
;;=> false

(string-as-keyword? "invalid@keyword")
;;=> false

(string-as-keyword? "")
;;=> false
```

### string-as-keyword-explain

```clojure
(string-as-keyword-explain s)
(string-as-keyword-explain s settings)
```

Explain-style validator for keyword-safe strings. Additional error code: `:string/not-keyword-safe` for strings that don't match the keyword pattern.

```clojure
(string-as-keyword-explain "valid-keyword")
;;=> {:valid? true :value "valid-keyword"}

(string-as-keyword-explain "123invalid")
;;=> {:valid? false
;;    :code :string/not-keyword-safe
;;    :message "Not keyword-safe: 123invalid"
;;    :expected {:regex ["^[a-zA-Z][a-zA-Z0-9_-]*$"]}
;;    :value "123invalid"}
```

### number?

```clojure
(number? n)
(number? n settings)
```

Boolean predicate that validates a number with optional type and range constraints.

**Settings Map:**
- `:nil-ok` - boolean, allow nil as valid (default: false)
- `:type` - keyword, one of #{:int :float :double :decimal :ratio} (optional)
- `:min` - minimum value inclusive (optional)
- `:max` - maximum value inclusive (optional)  
- `:fn-or-fns` - function or collection of functions, each must return truthy (optional)

```clojure
(number? 42)
;;=> true

(number? 42 {:type :int :min 0 :max 100})
;;=> true

(number? 3.14 {:type :float})
;;=> false  ; 3.14 is a Double, not Float

(number? 150 {:max 100})
;;=> false

(number? 8 {:fn-or-fns even?})
;;=> true
```

### number-explain

```clojure
(number-explain n)
(number-explain n settings)
```

Explain-style validator for numbers with detailed error reporting.

**Error Codes:**
- `:number/nil` - Value is nil
- `:type/not-number` - Expected number, got different type
- `:number/wrong-type` - Number is not of requested type
- `:number/too-small` - Number smaller than minimum
- `:number/too-large` - Number larger than maximum
- `:number/predicate-failed` - Custom predicate returned false

```clojure
(number-explain 42)
;;=> {:valid? true :value 42}

(number-explain 3.14 {:type :int})
;;=> {:valid? false
;;    :code :number/wrong-type
;;    :message "Number is not of requested type :int."
;;    :expected {:type :int}
;;    :value 3.14}

(number-explain "42")
;;=> {:valid? false
;;    :code :type/not-number
;;    :message "Expected number, got java.lang.String."
;;    :value "42"}
```

### collection?

```clojure
(collection? c)
(collection? c settings)
```

Boolean predicate that validates a collection with type, size, and content constraints.

**Settings Map:**
- `:nil-ok` - boolean, allow nil as valid (default: false)
- `:min` - integer, minimum count inclusive (optional)
- `:max` - integer, maximum count inclusive (optional)
- `:type` - keyword, one of #{:vec :list :set :map :seq :assoc} (optional)
- `:duplicates-ok` - boolean, allow duplicate values (default: true)
- `:nil-value-ok` - boolean, allow nil values within collection (default: true)
- `:fn-or-fns` - function or collection of functions, each must return truthy (optional)

```clojure
(collection? [1 2 3])
;;=> true

(collection? [1 2 3] {:type :vec :min 2 :max 5})
;;=> true

(collection? [1 2 2 3] {:duplicates-ok false})
;;=> false

(collection? [1 nil 3] {:nil-value-ok false})
;;=> false

(collection? #{:a :b :c} {:type :set})
;;=> true
```

### collection-explain

```clojure
(collection-explain c)
(collection-explain c settings)
```

Explain-style validator for collections with comprehensive error reporting.

**Error Codes:**
- `:collection/nil` - Value is nil
- `:type/not-collection` - Expected collection, got different type
- `:collection/wrong-type` - Collection is not of requested type
- `:collection/too-small` - Collection smaller than minimum count
- `:collection/too-large` - Collection larger than maximum count
- `:collection/duplicates-found` - Collection contains duplicate values
- `:collection/nil-values-found` - Collection contains nil values
- `:collection/predicate-failed` - Custom predicate returned false

```clojure
(collection-explain [1 2 3])
;;=> {:valid? true :value [1 2 3]}

(collection-explain [1 2 3 4] {:max 3})
;;=> {:valid? false
;;    :code :collection/too-large
;;    :message "Collection larger than max count 3."
;;    :expected {:max 3}
;;    :value [1 2 3 4]}

(collection-explain "not-a-collection")
;;=> {:valid? false
;;    :code :type/not-collection
;;    :message "Expected collection, got java.lang.String."
;;    :value "not-a-collection"}
```

### map-entry?

```clojure
(map-entry? m k-or-ks)
(map-entry? m k-or-ks settings)
```

Boolean predicate that validates a map entry (key-value pair) with support for nested key paths and value constraints.

**Parameters:**
- `m` - the map to validate the entry in
- `k-or-ks` - single key or key sequence for navigation (e.g., `:name` or `[:user :profile :name]`)

**Settings Map:**
- `:nil-ok` - boolean, allow map to be nil (default: false)
- `:key-required` - boolean, require key path to exist (default: true)
- `:nil-value-ok` - boolean, allow value at key to be nil (default: false)
- `:type` - keyword, one of #{:string :number :boolean :keyword :col :vec :set :map :fn} (optional)
- `:fn-or-fns` - function or collection of functions, each must return truthy (optional)

```clojure
(map-entry? {:name "Alice"} :name)
;;=> true

(map-entry? {:user {:profile {:name "Bob"}}} [:user :profile :name])
;;=> true

(map-entry? {:name "Alice"} :name {:type :string})
;;=> true

(map-entry? {:tags ["clojure"]} :tags {:type :vec})
;;=> true

(map-entry? {:count 42} :count {:type :number :fn-or-fns pos?})
;;=> true
```

### map-entry-explain

```clojure
(map-entry-explain m k-or-ks)
(map-entry-explain m k-or-ks settings)
```

Explain-style validator for map entries with detailed navigation and validation error reporting.

**Error Codes:**
- `:map-entry/nil-map` - Map is nil
- `:map-entry/not-map` - Expected map, got different type
- `:map-entry/key-not-found` - Key path not found in map
- `:map-entry/nil-value` - Value at key path is nil
- `:map-entry/wrong-type` - Value is not of requested type
- `:map-entry/predicate-failed` - Custom predicate returned false

```clojure
(map-entry-explain {:name "Alice"} :name)
;;=> {:valid? true :value "Alice"}

(map-entry-explain {:name "Alice"} :missing)
;;=> {:valid? false
;;    :code :map-entry/key-not-found
;;    :message "Key path :missing not found in map."
;;    :expected {:key-path :missing}
;;    :value {:name "Alice"}}

(map-entry-explain {:name "Alice"} :name {:type :number})
;;=> {:valid? false
;;    :code :map-entry/wrong-type
;;    :message "Value is not of requested type :number."
;;    :expected {:type :number}
;;    :value {:name "Alice"}}
```

## Runner (`kineticfire.validator.runner`)

The orchestration layer for executing multiple validation steps and aggregating results. Provides flexible validation workflows with different execution modes.

### explain

```clojure
(explain v steps)
(explain v steps opts)
(explain v steps opts defaults)
```

Run one or more validation steps against a value with configurable execution modes.

**Parameters:**
- `v` - value to validate
- `steps` - single step or collection of steps
- `opts` - options map `{:mode :first | :all, :path [...]}`
- `defaults` - default step fields for function-only steps

**Step Forms:**
```clojure
;; Function step (requires defaults for error info)
#(pos? %)

;; Map step (explicit)
{:pred #(pos? %)
 :code :number/not-positive
 :msg "Number must be positive"
 :expected {:min 0}
 :when #(number? %)        ; optional guard
 :select :count            ; optional value selector
 :path [:user]             ; optional path context
 :via :validation/step-1}  ; optional step identifier
```

```clojure
;; Simple validation
(explain 42 #(pos? %) {} {:code :invalid :msg "Failed"})
;;=> {:valid? true :value 42}

;; Multi-step validation (fail-fast mode)
(explain {:name "Alice" :age 25}
         [{:pred #(string? (:name %)) :code :name/invalid}
          {:pred #(number? (:age %)) :code :age/invalid}])
;;=> {:valid? true :value {:name "Alice" :age 25}}

;; Multi-step validation (collect all errors)  
(explain {:name 123 :age "invalid"}
         [{:pred #(string? (:name %)) :code :name/invalid}
          {:pred #(number? (:age %)) :code :age/invalid}]
         {:mode :all})
;;=> {:valid? false
;;    :errors [{:valid? false :code :name/invalid ...}
;;             {:valid? false :code :age/invalid ...}]
;;    :value {:name 123 :age "invalid"}}
```

### run-all

```clojure
(run-all v steps)
(run-all v steps defaults)
```

Convenience function equivalent to `(explain v steps {:mode :all} defaults)`. Collects all validation errors rather than stopping at the first failure.

```clojure
(run-all user-data validation-steps)
;;=> {:valid? false :errors [...] :value user-data}
```

### run-first

```clojure
(run-first v steps)
(run-first v steps defaults)
```

Convenience function equivalent to `(explain v steps {:mode :first} defaults)`. Stops at the first validation failure (fail-fast mode).

```clojure
(run-first user-data validation-steps)
;;=> {:valid? false :code ... :message ... :value user-data}
```

## Result Processing (`kineticfire.validator.result`)

Utilities for interpreting, transforming, and combining validation results into formats suitable for your application.

### collapse-result

```clojure
(collapse-result result err)
```

Collapse an explain map into `true` on success, or return the provided error value on failure.

```clojure
(collapse-result {:valid? true :value "test"} "Invalid")
;;=> true

(collapse-result {:valid? false :code :error} "Invalid")
;;=> "Invalid"
```

### collapse-results

```clojure
(collapse-results results err)
```

Collapse a sequence of explain maps. Returns `true` if all are valid, otherwise returns the provided error value.

```clojure
(collapse-results [{:valid? true} {:valid? true}] "Invalid")
;;=> true

(collapse-results [{:valid? true} {:valid? false}] "Invalid")  
;;=> "Invalid"
```

### combine-results

```clojure
(combine-results results)
```

Combine multiple explain results into a single aggregated map with overall validity and collected errors.

```clojure
(combine-results [{:valid? true :value "a"}
                  {:valid? false :code :error :message "Failed"}])
;;=> {:valid? false
;;    :errors [{:code :error :message "Failed"}]}
```

### valid?

```clojure
(valid? result)
```

Shorthand to check validity from an explain result. Always returns a boolean.

```clojure
(valid? {:valid? true})
;;=> true

(valid? {:valid? false})
;;=> false

(valid? nil)
;;=> false
```

### errors

```clojure
(errors result-or-results)
```

Extract error details (codes and messages) from an explain result or collection of results.

```clojure
(errors {:valid? false :code :test/failed :message "Test failed"})
;;=> [{:code :test/failed :message "Test failed"}]

(errors [{:valid? true} {:valid? false :code :error :message "Failed"}])
;;=> [{:code :error :message "Failed"}]
```

## Pattern Utilities (`kineticfire.validator.patterns`)

Internal utilities for regex pattern matching used by the string validation functions. These are primarily for advanced use cases and internal library operations.

The pattern utilities provide helper functions for matching regular expressions against strings in both whole-string and substring modes, supporting the flexible pattern matching capabilities used by `string?` and `string-explain`.

# License

The *validator* project is released under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)