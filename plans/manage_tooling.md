# Code Manage Tooling Guide

The `code.manage` namespace provides a suite of tools for maintaining, analyzing, and transforming the codebase. These tools are accessible via `lein manage`.

## Usage

```bash
lein manage <command> <target> [options]
```

*   **target**: Usually a namespace (e.g., `'[code.manage]'`) or a set of namespaces.
*   **options**: An EDN map of options (e.g., `'{:write false :print {:item true}}'`).

## Commands

### Information & Analysis

*   `analyse`: Analyzes source or test files.
    *   Usage: `lein manage analyse '[code.manage]'`
*   `vars`: Lists variables in a namespace.
    *   Usage: `lein manage vars '[code.manage]'`
*   `docstrings`: Checks for docstrings.
    *   Usage: `lein manage docstrings '[code.manage]'`
*   `find-usages`: Finds usages of a specific var.
    *   Usage: `lein manage find-usages '[code.manage]' "{:var 'code.framework/analyse}"`
*   `require-file`: Requires a file and returns public vars.
    *   Usage: `lein manage require-file '[code.manage]'`

### Test Coverage & Quality

*   `missing`: Lists functions missing unit tests.
    *   Usage: `lein manage missing '[code.manage]'`
*   `todos`: Lists tests marked with TODO.
    *   Usage: `lein manage todos '[code.manage]'`
*   `incomplete`: Lists functions that are missing tests or have TODOs.
    *   Usage: `lein manage incomplete '[code.manage]'`
*   `orphaned`: Lists tests that do not have a corresponding source function.
    *   Usage: `lein manage orphaned '[code.manage]'`
*   `in-order`: Checks if tests are in the same order as source functions.
    *   Usage: `lein manage in-order '[code.manage]'`
*   `unchecked`: Lists tests that do not contain assertions (`=>`).
    *   Usage: `lein manage unchecked '[code.manage]'`
*   `commented`: Lists tests that are commented out.
    *   Usage: `lein manage commented '[code.manage]'`
*   `pedantic`: Suggests improvements (combines missing, todos, unchecked, commented).
    *   Usage: `lein manage pedantic '[code.manage]'`
*   `unclean`: Finds source code with top-level comments (often indicating commented-out code).
    *   Usage: `lein manage unclean '[code.manage]'`

### Search & Locate

*   `locate-code`: Locates source code based on a query.
    *   Usage: `lein manage locate-code '[code.manage]' '{:query [comment]}'`
*   `locate-test`: Locates test code based on a query.
    *   Usage: `lein manage locate-test '[code.manage]' '{:query [comment]}'`
*   `grep`: Greps for a string or pattern.
    *   Usage: `lein manage grep '[code.manage]' '{:query "manage"}'`

### Transformation & Maintenance
**Note:** Use `{:write false}` to preview changes.

*   `import`: Imports unit tests as docstrings.
    *   Usage: `lein manage import '[code.manage]'`
*   `purge`: Removes docstrings and meta from file.
    *   Usage: `lein manage purge '[code.manage]'`
*   `scaffold`: Creates test scaffolds for new or existing namespaces.
    *   Usage: `lein manage scaffold '[code.manage]'`
*   `create-tests`: Scaffolds and arranges tests.
    *   Usage: `lein manage create-tests '[code.manage]'`
*   `arrange`: Reorders tests to match source function order.
    *   Usage: `lein manage arrange '[code.manage]'`
*   `grep-replace`: Replaces text matching a pattern.
    *   Usage: `lein manage grep-replace '[code.manage]' '{:query "old" :replace "new"}'`
*   `refactor-code`: Refactors code based on edits.
    *   Usage: `lein manage refactor-code '[code.manage]' ...`
*   `refactor-test`: Refactors tests.
*   `ns-format`: Formats `ns` forms (sorts requires, imports, etc.).
    *   Usage: `lein manage ns-format '[code.manage]'`

## Options

Common options include:

*   `:write`: `true` to apply changes, `false` (default for some) to preview.
*   `:print`: Map to control output verbosity (e.g., `{:item true :result true :summary true :function true}`).
*   `:query`: For search commands.
