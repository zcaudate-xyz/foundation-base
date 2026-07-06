# Contributing

Read the full [`CONTRIBUTING.md`](https://github.com/zcaudate-xyz/foundation-base/blob/main/CONTRIBUTING.md) before proposing a change.

## Core workflow

```bash
git clone git@github.com:zcaudate-xyz/foundation-base.git
cd foundation-base
lein deps
lein test :only <namespace-test>
```

## A useful contribution includes

1. a focused implementation or documentation change;
2. targeted tests or verification commands;
3. updated guides and generated-documentation source when behaviour changes;
4. linked examples when adding or changing generated output;
5. a pull-request description listing prerequisites and known limitations.

## Documentation changes

The two top-level entry points intentionally mirror each other:

- [`README.md`](https://github.com/zcaudate-xyz/foundation-base/blob/main/README.md)
- [`src-doc/documentation/main_index.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-doc/documentation/main_index.clj)

The Wiki source pages live under [`wiki/`](https://github.com/zcaudate-xyz/foundation-base/tree/main/wiki) so changes can be reviewed through normal pull requests before synchronization to GitHub Wiki.

## Example contributions

When adding an example, include:

- authored source;
- build definition;
- tests where available;
- generated repository;
- prerequisites;
- reproduction command;
- maturity status.

See [Examples](Examples) for the current structure.

## Useful links

- [Open issues](https://github.com/zcaudate-xyz/foundation-base/issues)
- [Pull requests](https://github.com/zcaudate-xyz/foundation-base/pulls)
- [Task-oriented guides](https://github.com/zcaudate-xyz/foundation-base/tree/main/guides)
