# validator
[![Powered by KineticFire Labs](https://img.shields.io/badge/Powered_by-KineticFire_Labs-CDA519?link=https%3A%2F%2Flabs.kineticfire.com%2F)](https://labs.kineticfire.com/)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Clojars Project](https://img.shields.io/clojars/v/com.kineticfire/validator.svg)](https://clojars.org/com.kineticfire/validator)
<p></p>

A simple validation library suitable for [Clojure](https://clojure.org/), [ClojureScript](https://clojurescript.org/), and
[Babashka](https://babashka.org/).

# Contents
1. [Motivation](#motivation)
2. [Installation](#installation)
3. [Usage](#usage)
4. [Documentation](#documentation)
5. [License](#license)


# Motivation

The *validator* library provides streamlined data validation functionality without dependencies.  The library focuses on
(1) reducing friction for data validation in a simple and intuitive manner and (2) increasing the code maintainability
(e.g., the ease with which software can be understood and modified) of validation code, especially with code marching to 
the right column for numerous validation operations.


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

Require the namespace in the `project.clj`, `bb.edn`, or similar file:
```clojure
(ns the-project.core
  (:require [kineticfire.validator :as validator]))
```

Call a function from the *validator* library:
```clojure
(validator/todo? x)
```
todo

# Documentation
todo

# License
The *validator* project is released under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)


