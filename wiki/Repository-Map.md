# Repository Map

Foundation Base contains several related systems in one repository.

## Namespace families

| Area | Purpose |
|---|---|
| `std.*` | Standard libraries and reusable infrastructure |
| `code.*` | Testing, documentation, source queries, maintenance, project tooling, and analysis |
| `hara.*` | Language authoring, grammar-driven code generation, target models, and typing |
| `rt.*` | Runtime adapters and execution environments |
| `xt.*` | Portable libraries and application layers built with the language tooling |

## Main directories

| Path | Purpose |
|---|---|
| `src/` | Main Clojure source |
| `src-lang/` | Language-oriented source |
| `src-extra/` | Optional or additional integrations |
| `test/` | Main tests |
| `test-lang/` | Language and runtime tests |
| `src-build/` | Walkthroughs, demos, build definitions, and generated projects |
| `src-doc/` | Generated-documentation source |
| `guides/` | Task-oriented Markdown guides |
| `config/publish/` | Documentation site configuration |
| `wiki/` | Reviewable source for GitHub Wiki pages |

## Where to look first

- Standard utilities: [`src/std`](https://github.com/zcaudate-xyz/foundation-base/tree/main/src/std)
- Developer tools: [`src/code`](https://github.com/zcaudate-xyz/foundation-base/tree/main/src/code)
- Language tooling: [Hara docs](https://zcaudate.xyz/foundation-base/hara/index.html)
- Generated examples: [`src-build/play`](https://github.com/zcaudate-xyz/foundation-base/tree/main/src-build/play)
- Walkthroughs: [`src-build/walkthrough`](https://github.com/zcaudate-xyz/foundation-base/tree/main/src-build/walkthrough)
- Documentation sources: [`src-doc/documentation`](https://github.com/zcaudate-xyz/foundation-base/tree/main/src-doc/documentation)

## Maturity labels

Documentation should identify major areas as:

- **Stable** — relied upon by production systems or other repositories;
- **Usable** — functional and tested, but APIs may change;
- **Experimental** — research, prototypes, incomplete targets, or environment-specific integrations.
