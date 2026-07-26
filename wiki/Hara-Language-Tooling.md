Tahto-Language-Tooling.md:1:# Tahto Language Tooling

`tahto.core` is a language-oriented templating and code-generation system. It stores code in a reusable Clojure representation, emits it through target grammars, and can connect the emitted code to runtime adapters.

## Smallest example

```clojure
(require '[tahto.core :as l])

(l/emit-as :js '(+ 1 2 3))
;; => "1 + 2 + 3"
```

## Core concepts

| Concept | Role |
|---|---|
| Book | Stores language identity, grammar, modules, entries, and inheritance |
| Grammar | Defines target syntax, operators, blocks, data forms, and special emit behaviour |
| Script | Connects a Clojure namespace to a target language context |
| Module | Groups generated entries and dependencies |
| Pointer | Represents generated functions and values from the Clojure side |
| Runtime | Evaluates or hosts generated code |

## Walkthroughs

- [Basic authoring source](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-doc/walkthrough/std_lang_00_basic.clj)
- [Multiple-language source](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-doc/walkthrough/std_lang_01_multi.clj)
- [Live evaluation source](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-doc/walkthrough/std_lang_02_live.clj)
Tahto-Language-Tooling.md:30:- [Published Tahto introduction](https://zcaudate.xyz/foundation-base/tahto/introduction.html)
Tahto-Language-Tooling.md:31:- [Published Tahto comparison](https://zcaudate.xyz/foundation-base/tahto/comparison.html)

## Related areas

Tahto-Language-Tooling.md:35:- `tahto.model` — target language specifications
Tahto-Language-Tooling.md:36:- `tahto.runtime` and `rt.*` — runtime adapters
Tahto-Language-Tooling.md:37:- `tahto.typed` — typed xtalk analysis and target declarations
Tahto-Language-Tooling.md:38:- `tahto.common` — shared emit, grammar, preprocess, and rewrite behaviour
- `xt.*` — portable libraries built on the language tooling

## Learn through generated projects

Continue with [Examples](Examples) to compare the authored Clojure source, build definitions, and generated repositories.
