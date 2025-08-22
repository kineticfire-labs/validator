# Style Guide

This defines code style for Clojure, ClojureScript, and Babashka for consumption by Claude Code.

# Source

Downloaded from [GitHub bbatsov/clojure-style-guide](https://github.com/bbatsov/clojure-style-guide) on 2025-08-21 and
licensed under a [Creative Commons Attribution 3.0 Unported License](https://creativecommons.org/licenses/by/3.0/deed.en_US).

The original style guide was modified by summarizing the content to create this document for consumption by Claude Code. 

# Guiding Principles

* Write code for **people first**; machines are incidental.
* Prefer **clarity, simplicity, and consistency** over cleverness.
* Be consistent within project and file; deviate only with clear justification.
* Do **not** break backward compatibility just to satisfy style.

# Source Code Layout & Organization

* **Max line length is 120 characters.**
* Use **spaces**, not tabs; **2-space** indentation for bodies (`def`, `let`, `loop`, `when`, etc.).
* Vertically **align function arguments** when split across lines; use **one-space indent** when name is on a line alone.
* Vertically **align `let` bindings**; vertically **align map keys**.
* Use **Unix line endings**; **end files with a newline**; **no trailing whitespace**.
* Use **one blank line between top-level forms**; no blank lines **inside** defs except to group `let`/`cond` pairs.
* Use **bracket spacing**: space before `(` `{` `[` only if text precedes; no space right after opening or before closing.
* Do **not** use commas in sequential literals; commas in maps are **optional** for readability.
* **Gather trailing parens** on one line; exception: rich `comment` blocks.
* **One file per namespace** and **one namespace per file**.

# Namespace Declarations

* Avoid **single-segment** namespaces; keep segments **≤ 5**.
* Start each ns with a comprehensive `ns` form: **`:refer-clojure`**, **`:require`**, **`:import`** (in that order).
* Put each dependency on its **own line** and **sort** requires/imports.
* Prefer `:require :as` over `:refer`; avoid `:use`.
* Use **idiomatic aliases** (e.g., `clojure.string` → `str`) and keep aliases **consistent** project-wide.
* Derive aliases systematically; drop redundant `core`/`clj` parts; use dotted aliases if needed for uniqueness.

# Naming

* Use **lisp-case** for functions/vars; **CapitalCase** for protocols/records/types (keep acronyms uppercase).
* Predicates **end with `?`**; side-effecting/unsafe ops **end with `!`**.
* Use `a->b` style (e.g., `f->c`) for conversions; use `*earmuffs*` for dynamic vars.
* Do **not** special-case constants (no `MAX_SIZE`); ordinary `def` with clear names.
* Use `_` (or `_name`) for unused bindings/args; adopt idiomatic short names (`pred`, `coll`, `xs`, `m`, `k`, `v`, etc.).

# Functions

* Optional newline after `defn` name; keep **short one-liners** on a single line when clear.
* **Align arities** vertically; **order arities** from fewest to most args.
* Keep functions **≤ 10 LOC** (ideally < 5); keep **≤ 3–4 positional params**.
* Prefer **pre/post conditions** over body checks when appropriate.

# Idioms

* Avoid `require`/`refer` at runtime code; keep them in `ns`.
* Avoid **forward references**; if necessary, use `declare` sparingly.
* Prefer **higher-order fns** (`map`, `reduce`, etc.) over `loop/recur` when clear.
* Do **not** `def` vars inside functions; do **not** shadow `clojure.core` names.
* Use `alter-var-root` to change var values; avoid redefining with `def`.
* Use **nil punning**: test sequences with `seq`, not `empty?`.
* Prefer `vec` over `into []` when converting to vector.
* Use `boolean` to coerce truthiness when needed.
* Prefer `when` over single-branch `if`; use `if-let`/`when-let`, `if-not`/`when-not`.
* Use `not=` instead of `(not (= ...))`.
* Prefer `printf` over `(print (format ...))`.
* Use variadic comparisons: `(< a b c)`.
* In fn literals: use `%` for single arg; `%1`, `%2` for multiple.
* Avoid **useless anonymous wrappers**; avoid multiple forms in `#(...)` (use `fn`/`do`).
* Prefer anonymous fns over `complement`/`comp`/`partial` unless they **clearly** improve readability (transducers are an exception).
* Prefer threading macros `->` / `->>` over deep nesting; omit unnecessary parens; align steps vertically.
* In `cond`, use `:else`; use `condp` when predicate is fixed; use `case` for constant tests.
* Use sets as predicates; use `inc`/`dec`; use `pos?`/`neg?`/`zero?`.
* Prefer `list*` over nested `cons`.
* Prefer **sugared Java interop** forms.
* Use compact metadata for true flags: `^:private`; mark private with `defn-` / `^:private`.
* Access private vars in tests with `@#'ns/var`.
* Attach metadata to the **intended target** (var vs value) deliberately.

# Data Structures

* Do **not** use lists for generic storage unless you need a list.
* Prefer **keywords as map keys**.
* Prefer **literal** collection syntax; for sets, use literal only with **compile-time constants**.
* Avoid **index-based access**; use keywords/functions/seq ops.
* Leverage **collections and keywords as functions**.
* Avoid **transients** unless in performance-critical paths.
* Avoid raw **Java collections/arrays** except for interop/perf primitives.

# Types & Records

* Use generated constructors `->Type` and `map->Type`; **do not** use interop `Type.`.
* Add **custom constructors** (e.g., `make-foo`) for validation; **do not** override auto-generated ones.
* `map->` exists for **records**, not `deftype`.

# Mutation

* **Refs**: wrap I/O with `io!`; prefer `alter` over `ref-set`; keep transactions **small**; avoid mixing short/long tx on same ref.
* **Agents**: use `send` for CPU-bound; `send-off` for blocking.
* **Atoms**: avoid atom updates inside STM tx; prefer `swap!` over `reset!`.

# Math & Strings

* Prefer `clojure.math` over `java.lang.Math` interop.
* Prefer `clojure.string` over Java string interop.

# Exceptions & Resources

* Reuse **standard exception types** (`IllegalArgumentException`, etc.).
* Prefer `with-open` over `finally` for resource management.

# Macros

* **Do not** write a macro if a function suffices.
* Write **usage examples first**; split complex macros into helpers.
* Keep macros as **syntactic sugar**; delegate logic to functions.
* Prefer **syntax-quote**; consider `:style/indent` metadata for editor support.

# Common Metadata

* Use `:added`, `:changed`, `:deprecated`, `:superseded-by`, `:see-also`, `:no-doc` where appropriate.
* Consider adding inverse `:supersedes` on new APIs.

# Comments

* Strive for **self-explanatory code**; comment only when needed.
* Use semicolons by convention: `;;;;` headings, `;;;` top-level, `;;` inline above code, `;` rare margin notes.
* Capitalize and punctuate sentences; **space after `;`**.
* Avoid **superfluous** comments; keep comments **up-to-date**.
* Prefer `#_` to comment out forms; refactor rather than explain bad code.
* Place annotations **above** code; format as `TODO:`/`FIXME:`/`OPTIMIZE:`/`HACK:`/`REVIEW:`; indent continuations; sign/date when useful.

# Documentation (Docstrings)

* Prefer **docstrings** over `:doc` metadata when available.
* First line is a **concise, capitalized sentence** summary.
* You may use **Markdown** in docstrings.
* Backtick positional args and var refs; link with `[[ns/var]]`.
* Write correct grammar; indent multi-line docstrings by **two spaces**; no leading/trailing whitespace.
* Place function docstrings **after the name**, not after the arg vector.
* In `defprotocol`, place method docstrings **after** the arg vector.

# Testing

* Put tests under `test/yourproject/...`; name test namespaces `yourproject.something-test`.
* Name tests with `deftest` as `something-test`.

# Library Organization

* Choose clear **coordinates** (`groupId/artifactId`); avoid name conflicts.
* **Minimize dependencies**; small utility > heavy dependency.
* Keep core library **tool-agnostic**; provide integrations separately.

# Tools

* Run linters/formatters (e.g., **clj-kondo**, **cljfmt/cljstyle/zprint**) to enforce style automatically.

# Ethos

* **Be functional**; mutate sparingly.
* **Be consistent**; apply common sense.