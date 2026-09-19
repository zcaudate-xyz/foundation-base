# Publishing Foundation Base

Foundation Base publishes the root `xyz.zcaudate/foundation-base` artifact and
the split packages described by `config/packages.edn` to Clojars from the
protected `release` branch.

## Release flow

1. Update the single project version in `project.clj` on `main`. Keep the
   version a non-snapshot release version.
2. Open a normal pull request from the repository's `main` branch to
   `release`. The release preflight checks the repository lineage, Java 21
   build, root packaging, and the complete split-package artifact inventory.
   The normal `main` CI owns the full `code.test` suite; the release gate does
   not rerun it.
3. Merge the pull request without squashing or rebasing. This preserves the
   reviewed `main` parent in the release branch history.
4. Confirm that the merged commit is the current `release` branch head, then
   push the matching version tag:

   ```bash
   version=4.1.6
   git tag "v${version}"
   git push origin "v${version}"
   ```

5. The tag workflow verifies the exact tag/version and release-branch
   relationship, creates an ephemeral Clojars repository configuration, and
   runs the split-package and root publication steps in that order:

   ```bash
   ./lein deploy-clojars
   ./lein deploy-root
   ```

The automated selector is `:clojars`, which publishes the all-package manifest
through Clojars. The existing `:all` selector continues to target the GitHub
Maven repository, while `:public` and `:lang` remain manual subset selectors.
Do not publish overlapping selectors for one immutable version.

## Credentials and recovery

The workflow receives `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` only from the
protected GitHub `clojars` environment. Before the first tag, a repository
administrator must add both names as environment secrets. `CLOJARS_PASSWORD`
should be a scoped Clojars deploy token. Do not put either value in
`project.clj`, an encrypted file, a commit, a log, or an artifact.

Clojars versions are immutable and split publication is not atomic. If the
split-package step fails, inspect which coordinates were accepted before
retrying; do not blindly rerun an immutable production version. The root step
only runs after the split-package step succeeds. If a source or version change
is required, use a new version on `main`, a new release promotion, and a new
tag. If a workflow fails before any artifact is accepted, the same tag can be
rerun after correcting the delivery problem.

## Local split-package snapshot check

Before changing the production workflow, a split-package-only snapshot can be
validated locally. Set `CLOJARS_USERNAME` to the Clojars username and
`CLOJARS_PASSWORD` to a scoped Clojars deploy token in the shell environment;
never put either value in a file tracked by Git or in a command argument.

Temporarily change the project version to `4.1.6-SNAPSHOT`, then run:

```bash
./scripts/release/prepare-clojars-config.sh package
./lein package-clojars

./scripts/release/prepare-clojars-config.sh deploy
./lein deploy-clojars
```

This experiment must not run `lein deploy-root`. Verify representative split
coordinates and confirm that `xyz.zcaudate/foundation-base` was not published.
Afterward restore the production version, remove `config/repositories.edn` and
`target/interim`, and run `git diff --check`.

## Local checks

Use the repository's local Leiningen wrapper when validating a release:

```bash
./lein package-clojars
./lein test
./lein pom
./lein jar
```
