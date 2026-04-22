# Foundation Symbol Index

This document describes the symbol index for the Foundation library, which provides fast searchable access to all functions, vars, macros, protocols, types, and multimethods.

## Overview

The Foundation library contains **10,665 symbols** across **814 namespaces** in **1,057 files**. The symbol index creates a searchable SQLite database for quick lookups.

| Statistic | Count |
|-----------|-------|
| Files indexed | 1,057 |
| Total symbols | 10,665 |
| Namespaces | 814 |
| Functions | 7,922 (74%) |
| Vars | 1,957 (18%) |
| Macros | 277 (3%) |
| Protocols | 263 (2%) |
| Types | 142 (1%) |
| Multimethods | 104 (1%) |

## Quick Start

### Build the Index

```bash
# Build/update the index
make index

# Force rebuild (delete and recreate)
make index-force
```

### Query the Index

```bash
# Search for symbols
make search QUERY=emit
make search QUERY=emit KIND=function
make search QUERY=emit KIND=function NAMESPACE=std.lang

# Get symbol details
make symbol NAME=std.lang/emit

# Show statistics
make index-stats

# List all namespaces
make list-namespaces
```

## Command Reference

### `./bin/foundation-index`

The main CLI tool for index operations:

```bash
# Build/update the index
./bin/foundation-index index

# Force rebuild
./bin/foundation-index index --force

# Search for symbols
./bin/foundation-index search <query> [kind] [namespace] [limit]

# Get symbol details
./bin/foundation-index get <qualified-name>

# Show file outline
./bin/foundation-index outline <file-path>

# Show statistics
./bin/foundation-index stats

# List namespaces
./bin/foundation-index list-namespaces
```

### Search Examples

```bash
# Find all "emit" functions
./bin/foundation-index search emit function

# Find "compile" in std.lang namespace
./bin/foundation-index search compile function std.lang

# Search with limit
./bin/foundation-index search book function bb.lang 10
```

### Get Symbol Examples

```bash
# Get details about a specific function
./bin/foundation-index get std.lang/emit
./bin/foundation-index get bb.lang.base.emit-assign/emit-def-assign
```

## Database Location

The index is stored at:

```
.clojure-mcp/symbol-index.db
```

This is a SQLite database containing:
- Symbol metadata (name, kind, namespace, file, line)
- Docstrings
- Arglists (function arguments)
- File hashes (for incremental updates)

## Makefile Targets

| Target | Description |
|--------|-------------|
| `make index` | Build/update the symbol index |
| `make index-force` | Force rebuild (delete existing first) |
| `make index-stats` | Show index statistics |
| `make search QUERY=...` | Search for symbols |
| `make symbol NAME=...` | Get symbol details |
| `make list-namespaces` | List all namespaces |

## Integration with Main Project

When used from the main `gw-v2` project root, the index is auto-detected at:

```
cache/foundation/.clojure-mcp/symbol-index.db
```

The main project provides a wrapper script at:

```
./src-training/scripts/foundation-index
```

This script automatically finds and uses the foundation index database.

## Key Namespaces

| Namespace | Purpose |
|-----------|---------|
| `std.lang` | Main transpilation API |
| `std.lang.base.emit` | Code emission |
| `std.lang.base.compile` | Compilation |
| `std.lang.base.book` | Book management |
| `rt.postgres` | PostgreSQL DSL |
| `rt.postgres.entity` | Entity framework |
| `bb.lang.*` | Book compilation |
| `std.lib` | Core utilities |

## Troubleshooting

### No results found
- Check spelling of symbol names
- Use partial matches (e.g., "emit" matches "emit-def-assign")
- Try without filters first

### Slow queries
- Use `limit` parameter to reduce results
- Filter by `kind` or `namespace`

### Database not found
```bash
# Build the index
make index
```

### Index out of date
```bash
# Force rebuild
make index-force
```
