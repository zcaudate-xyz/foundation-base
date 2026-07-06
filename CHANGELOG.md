# Changelog

Notable user-facing changes to Foundation Base are recorded here.

This repository predates this changelog structure, so earlier releases have not yet been reconstructed from Git history. New entries should describe changes that affect installation, public APIs, generated output, supported runtimes, documentation, or contributor workflows.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versions should follow the version declared in `project.clj`.

## [Unreleased]

### Added

- A contributor onboarding guide covering setup, targeted tests, documentation generation, language targets, runtimes, and pull-request expectations.
- Clear repository interaction paths for library consumers, language-tooling users, explorers, contributors, and documentation authors.

### Changed

- Reworked the repository README to explain the purpose and scope of Foundation Base before installation details.
- Mirrored the README identity, navigation, repository map, and quick-start paths in `src-doc/documentation/main_index.clj`.
- Updated the generated-site title and subtitle to match the repository positioning.
- Updated the getting-started guide to use the current project version and current onboarding model.

### Removed

- Placeholder widget-template release notes and example repository links that were unrelated to Foundation Base.

## Release process notes

When preparing a release:

1. move relevant entries from `Unreleased` into a versioned section;
2. add the release date in `YYYY-MM-DD` format;
3. update comparison links when the corresponding tag exists;
4. verify the dependency coordinate and version against `project.clj`;
5. include migration notes for public API or generated-output changes.
