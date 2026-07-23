<!-- See CONTRIBUTING.md -> Commit conventions for message structure. -->

## Summary

<!-- What changed and why. Link the tracking issue: Refs #123 or Fixes #123. -->

## Affected areas

<!-- Namespaces, language targets, docs pages, or CI workflows touched. -->

## Checklist

- [ ] Commits are scoped (one concern per commit) and use subsystem-prefixed summaries
- [ ] Generated output (`test-lang/xtbench/**`, `packages-gen/**`, `public/`) is committed separately from the source/generator change
- [ ] Targeted tests run and passing (`lein test :only ...`), listed below
- [ ] Bench files regenerated when a seed changed (`lein seedgen benchadd ...`), and both canonical and generated target tests pass
- [ ] Documentation updated (`src-doc/`, `guides/`, README) where behaviour changed
- [ ] Tracking issue linked

## Verification

<!-- Commands run and their results, e.g.:
lein test :only std.lib.collection-test
-->

## Known limitations / follow-ups

<!-- Optional. -->
