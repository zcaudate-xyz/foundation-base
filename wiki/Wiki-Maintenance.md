# Wiki Maintenance

GitHub Wikis are stored in a separate Git repository. For Foundation Base, the Wiki remote is:

```text
git@github.com:zcaudate-xyz/foundation-base.wiki.git
```

The Wiki must first be enabled in the repository settings and initialized by creating its first page in the GitHub interface. Until that happens, the separate Wiki repository returns 404.

## Source of truth

Reviewable Wiki page source lives in the main repository under:

```text
wiki/
```

This allows page additions and edits to go through normal branches and pull requests.

## Publish reviewed pages

After merging Wiki page changes and initializing the GitHub Wiki, run:

```bash
lein wiki
```

or call the script directly:

```bash
bash bin/publish-wiki
```

The script:

1. clones the separate Wiki repository into a temporary directory;
2. copies the Markdown pages from `wiki/`;
3. commits only when content changed;
4. pushes the updated Wiki pages.

It does not delete unrelated pages already present in the live Wiki.

## Alternate remote

To publish to another Wiki clone or test remote:

```bash
WIKI_REMOTE=git@github.com:owner/repository.wiki.git bash bin/publish-wiki
```

## Editing policy

Prefer editing the files under `wiki/` and publishing them through the script. Direct edits in the GitHub Wiki interface may be overwritten the next time the corresponding source page is synchronized.
