# Foundation application backbone papers

This directory contains two LaTeX papers describing how `xt.substrate`, `xt.db.node` and `postgres.core` can be used as a tested data and RPC backbone for AI-assisted web, mobile and edge applications.

- `business-analyst.tex` explains the delivery model, programme controls and business value.
- `senior-developer.tex` explains the architecture for developers familiar with PostgreSQL, TypeScript, mobile, edge and native ecosystems but not necessarily Clojure or Lisp.

Both papers include diagrams and comparisons with REST/OpenAPI, GraphQL, gRPC, ORMs, backend-as-a-service platforms, CQRS/event sourcing, local-first frameworks and a direct Bun/Rust rewrite. The Bun/Rust section is an architectural comparison rather than a claim about an identified Foundation rewrite branch.

## Install dependencies

On macOS, the installer uses TinyTeX. On Debian or Ubuntu, it installs TeX Live and the Inter/Roboto fonts:

```bash
docs/application-backbone/install-deps.sh
```

## Build

From the repository root:

```bash
make -C docs/application-backbone
```

Build one paper:

```bash
make -C docs/application-backbone business-analyst
make -C docs/application-backbone senior-developer
```

## Publish locally

Build the PDFs and stage a clean distribution directory under `target/documents/application-backbone/`:

```bash
make -C docs/application-backbone publish
```

The staged files are:

```text
target/documents/application-backbone/
├── INDEX.md
├── README.md
├── business-analyst.pdf
└── senior-developer.pdf
```

## Continuous integration

`.github/workflows/application-backbone-docs.yml` runs when these sources or the workflow change. It:

1. installs XeLaTeX, `latexmk` and the required fonts;
2. builds both papers with warnings treated as LaTeX build failures where applicable;
3. stages the publish directory;
4. uploads the complete directory as the `foundation-application-backbone-pdfs` workflow artifact.

The workflow also supports manual dispatch.

## Cleanup

Remove intermediate files while retaining PDFs:

```bash
make -C docs/application-backbone clean
```

Remove generated PDFs and the staged distribution directory:

```bash
make -C docs/application-backbone distclean
```
