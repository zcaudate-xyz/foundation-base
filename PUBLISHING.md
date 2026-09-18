# Publishing Foundation Base

Foundation Base publishes the root `xyz.zcaudate/foundation-base` artifact to
Clojars from the protected `release` branch.

## Release flow

1. Update the single project version in `project.clj` on `main`. Keep the
   version a non-snapshot release version.
2. Open a normal pull request from the repository's `main` branch to
   `release`. The release preflight checks the repository lineage, Java 21
   build, package generation, and the `code.test` suite.
3. Merge the pull request without squashing or rebasing. This preserves the
   reviewed `main` parent in the release branch history.
4. Confirm that the merged commit is the current `release` branch head, then
   push the matching version tag:

   ```bash
   version=4.1.5
   git tag "v${version}"
   git push origin "v${version}"
   ```

5. The tag workflow verifies the exact tag/version and release-branch
   relationship, reruns the release checks, and runs:

   ```bash
   lein deploy-root
   ```

## Credentials and recovery

The workflow receives `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` only from the
protected GitHub `clojars` environment. Before the first tag, a repository
administrator must add both names as environment secrets. `CLOJARS_PASSWORD`
should be a scoped Clojars deploy token. Do not put either value in
`project.clj`, an encrypted file, a commit, a log, or an artifact.

Clojars versions are immutable. If a workflow fails before publication, rerun
the same tag workflow after correcting the delivery problem. Do not retag or
overwrite a version that has already been published. A source or version
change requires a new version on `main`, a new release promotion, and a new
tag.

## Local checks

Use the repository's local Leiningen wrapper when validating a release:

```bash
./lein test
./lein pom
./lein jar
```
