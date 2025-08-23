# Style Guide

This defines code style for Clojure, ClojureScript, and Babashka for consumption by Claude Code.

Styles defined in this document takes precedence over other style documents to include `style-guide-bbatsov.md`.

## Styles

- Exceptions must be truly exceptional and rarely thrown. 
   - Anticipate error cases through design and handle them explicitly in code. Prefer returning an error value; when 
     additional feedback is valuable (for user clarity, program reasoning, or debugging), throw a map containing keys 
     such as `:code` (keyword defining the error) and optionally `:reason` (human-readable description).
- Keep side effects at the edges: implement only the minimal logic in impure functions (I/O, mutation, time, randomness)
  and immediately delegate to pure functions; prefer pure functions because they’re easier to reason about and test.
- Annotate side-effecting functions: Any function that performs I/O, mutation, time, randomness, etc. MUST be declared 
  `^:impure` (e.g., `(defn ^:impure run-shell-command ...)`).
- Impurity is transitive: Any function that calls a `^:impure` function MUST also be annotated `^:impure`.
