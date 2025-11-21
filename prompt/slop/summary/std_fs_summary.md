## std.fs: A Comprehensive Summary (including submodules)

The `std.fs` module provides a comprehensive and idiomatic Clojure interface for interacting with the filesystem, building upon Java's `java.nio.file` package. It offers a rich set of functions for path manipulation, file and directory operations (create, copy, move, delete, list, walk), attribute management, and even watching for filesystem changes. The module aims to simplify common filesystem tasks and provide robust, cross-platform capabilities.

### `std.fs` (Main Namespace)

This namespace serves as the primary entry point for filesystem operations, aggregating and re-exporting key functionalities from its submodules. It also provides utilities for converting between Clojure namespaces and file paths.

**Key Re-exported Functions:**

*   From `std.fs.api`: All file/directory manipulation functions (`create-directory`, `copy`, `delete`, `move`, `select`, `list`, etc.).
*   From `std.fs.path`: All path manipulation functions (`path`, `file-name`, `parent`, `exists?`, `directory?`, `file?`, `input-stream`, `output-stream`, etc.).
*   From `std.fs.attribute`: `attributes`, `set-attributes`.
*   From `std.fs.common`: `option`.

**Key Functions:**

*   **`ns->file`**: Converts a Clojure namespace string (e.g., `"std.fs-test"`) to a file path string (e.g., `"std/fs_test"`).
*   **`file->ns`**: Converts a file path string to a Clojure namespace string.
*   **`read-code`**: Reads a Clojure source file and returns a lazy sequence of its top-level forms.
*   **`get-namespace`**: Extracts the namespace symbol from a sequence of Clojure forms.
*   **`file-namespace`**: Reads the namespace of a given Clojure source file.

### `std.fs.api` (High-Level File Operations)

This sub-namespace provides high-level functions for common filesystem operations, such as creating, copying, moving, and deleting files and directories. It leverages `std.fs.walk` for recursive operations and filtering.

**Key Functions:**

*   **`create-directory`**: Creates a directory, including any necessary parent directories.
*   **`create-symlink`**: Creates a symbolic link.
*   **`create-tmpfile`**: Creates a temporary file.
*   **`create-tmpdir`**: Creates a temporary directory.
*   **`select`**: Selects files and directories within a root path, with options for filtering.
*   **`list`**: Lists files and attributes for a given directory, with options for recursion.
*   **`copy`**: Copies files and directories from a source to a target, preserving directory structure.
*   **`copy-single`**: Copies a single file.
*   **`copy-into`**: Copies selected files from a source into a target directory.
*   **`delete`**: Deletes files and directories, with options for filtering.
*   **`move`**: Moves files and directories.

### `std.fs.archive` (Archive (ZIP/JAR) Manipulation)

This sub-namespace provides functions for creating, opening, and manipulating ZIP and JAR archives, treating them as virtual filesystems.

**Core Concepts:**

*   **`IArchive` Protocol:** Defines methods for interacting with archive filesystems (e.g., `url`, `path`, `list`, `has?`, `archive`, `extract`, `insert`, `remove`, `write`, `stream`).
*   **`ZipFileSystem`:** Extends the `IArchive` protocol to work with Java's `ZipFileSystem`.

**Key Functions:**

*   **`zip-system?`**: Checks if an object is a `ZipFileSystem`.
*   **`create`**: Creates a new ZIP/JAR archive.
*   **`open`**: Opens an existing archive or creates a new one.
*   **`open-and`**: Opens an archive, performs an operation, and then closes it.
*   **`url`**: Returns the URL of an archive.
*   **`path`**: Returns a `Path` object for an entry within an archive.
*   **`list`**: Lists all entries in an archive.
*   **`has?`**: Checks if an archive contains a specific entry.
*   **`archive`**: Adds files to an archive.
*   **`extract`**: Extracts entries from an archive.
*   **`insert`**: Inserts a file into an archive.
*   **`remove`**: Removes an entry from an archive.
*   **`write`**: Writes a stream to an entry within an archive.
*   **`stream`**: Creates an `InputStream` for an entry within an archive.

### `std.fs.attribute` (File Attributes)

This sub-namespace provides functions for reading and setting various file attributes, including permissions, ownership, and timestamps.

**Core Concepts:**

*   **`FileAttribute`:** Java's representation of a file attribute.
*   **POSIX File Permissions:** Supports POSIX-style file permissions (e.g., `rwxr-xr-x`).

**Key Functions:**

*   **`to-mode-string`**: Converts numeric file modes (e.g., "455") to string representations (e.g., "r--r-xr-x").
*   **`to-mode-number`**: Converts string file modes to numeric representations.
*   **`to-permissions`**: Converts numeric file modes to a set of `PosixFilePermission` keywords.
*   **`from-permissions`**: Converts a set of `PosixFilePermission` keywords to numeric file modes.
*   **`owner`**: Returns the owner of a file.
*   **`lookup-owner`**: Looks up a user principal by name.
*   **`set-owner`**: Sets the owner of a file.
*   **`lookup-group`**: Looks up a group principal by name.
*   **`attr`**: Creates a `FileAttribute` object.
*   **`attr-value`**: Adjusts attribute values for input (e.g., converting owner name to `UserPrincipal`).
*   **`map->attr-array`**: Converts a Clojure map of attributes to a `FileAttribute` array.
*   **`attrs->map`**: Converts a map of `FileAttribute` objects to a Clojure map.
*   **`attributes`**: Retrieves all attributes for a given path.
*   **`set-attributes`**: Sets attributes for a given path.

### `std.fs.common` (Filesystem Common Utilities)

This sub-namespace defines common constants and utility functions used across the `std.fs` module, such as system-specific separators, OS detection, and option handling.

**Key Functions:**

*   **`*cwd*`, `*sep*`, `*os*`, `*home*`, `*tmp-dir*`**: Dynamic vars for common system properties.
*   **`*no-follow*`**: Default `LinkOption` for not following symbolic links.
*   **`*system*`**: Detects the operating system type (`:dos` or `:unix`).
*   **`option`**: Retrieves `java.nio.file.CopyOption`, `StandardOpenOption`, etc., by keyword.
*   **`pattern`**: Converts a glob-like string (e.g., `*.clj`) to a `java.util.regex.Pattern`.
*   **`tag-filter`**: Adds a `:tag` to a filter map.
*   **`characterise-filter`**: Converts various filter inputs (string, regex, function, map) into a standardized filter map.

### `std.fs.interop` (Filesystem Interoperability)

This sub-namespace provides interoperability with `clojure.java.io`, extending the `IOFactory` protocol for `java.nio.file.Path` objects.

**Key Functions:**

*   Extends `java.nio.file.Path` to `clojure.java.io/IOFactory`, allowing `Path` objects to be used directly with `clojure.java.io/input-stream` and `clojure.java.io/output-stream`.

### `std.fs.path` (Path Manipulation)

This sub-namespace provides functions for creating, manipulating, and querying `java.nio.file.Path` objects, offering a robust and platform-agnostic way to work with file paths.

**Key Functions:**

*   **`normalise`**: Normalizes a path string, handling `~` for home directory and resolving relative paths.
*   **`path`**: Creates a `java.nio.file.Path` object from various inputs (string, vector, URI, URL, File).
*   **`path?`**: Checks if an object is a `Path`.
*   **`section`**: Creates a `Path` object without normalization.
*   **`to-file`**: Converts a `Path` to a `java.io.File`.
*   **`file-name`**: Returns the last element of a path.
*   **`file-system`**: Returns the `FileSystem` for a path.
*   **`nth-segment`, `segment-count`**: Accesses path segments.
*   **`parent`**: Returns the parent path.
*   **`root`**: Returns the root path.
*   **`relativize`**: Creates a relative path between two paths.
*   **`subpath`**: Returns a sub-sequence of path segments.
*   **`file-suffix`**: Returns the file extension as a keyword.
*   **`directory?`, `executable?`, `set-executable`, `permissions`, `typestring`, `exists?`, `hidden?`, `file?`, `link?`, `readable?`, `writable?`, `empty-directory?`**: Predicates and functions for querying file/directory properties.
*   **`input-stream`, `output-stream`**: Opens `InputStream` or `OutputStream` for a path.
*   **`read-all-lines`, `read-all-bytes`**: Reads file content.
*   **`file`**: Returns a `java.io.File` object.
*   **`last-modified`**: Returns the last modified timestamp.
*   **`write-into`**: Writes an `InputStream` to a path.
*   **`write-all-bytes`**: Writes a byte array to a file.

### `std.fs.walk` (Filesystem Walking)

This sub-namespace provides a powerful and flexible mechanism for traversing filesystem trees, allowing for custom actions to be performed on files and directories, and supporting complex filtering rules.

**Core Concepts:**

*   **`FileVisitor`:** Implements Java's `java.nio.file.FileVisitor` interface.
*   **Filter System:** Supports `include` and `exclude` filters based on patterns or custom functions.
*   **Hooks:** Allows defining `pre` and `post` hooks for directories, and a `file` hook for files.

**Key Functions:**

*   **`match-single`**: Matches a single filter against a path.
*   **`match-filter`**: Matches a path against multiple include/exclude filters.
*   **`visit-directory-pre`, `visit-directory-post`, `visit-file`, `visit-file-failed`**: Helper functions for `FileVisitor` callbacks.
*   **`visitor`**: Constructs a `FileVisitor` instance.
*   **`walk`**: The main function for traversing a filesystem tree, applying filters and hooks.

### `std.fs.watch` (Filesystem Watching)

This sub-namespace provides functionality for watching filesystem directories for changes (create, delete, modify events), allowing applications to react to filesystem events in real-time.

**Core Concepts:**

*   **`WatchService`:** Leverages Java's `java.nio.file.WatchService` for event notification.
*   **`Watcher` record:** Encapsulates the state of a filesystem watcher.
*   **Event Kinds:** Supports `ENTRY_CREATE`, `ENTRY_DELETE`, `ENTRY_MODIFY` events.
*   **Filters:** Allows filtering events by filename patterns.

**Key Functions:**

*   **`pattern`**: Creates a regex pattern from a glob-like string.
*   **`register-entry`**: Registers a path with a `WatchService`.
*   **`register-sub-directory`**: Recursively registers subdirectories for watching.
*   **`register-path`**: Registers a file or directory for watching.
*   **`process-event`**: Processes a single watch event.
*   **`run-watcher`**: The main loop for processing watch events.
*   **`start-watcher`**: Starts a filesystem watcher in a separate thread.
*   **`stop-watcher`**: Stops a filesystem watcher.
*   **`watcher`**: Creates a `Watcher` record.
*   **`watch-callback`**: Creates a callback function for watch events.
*   **`add-io-watch`, `list-io-watch`, `remove-io-watch`**: Functions for managing watches on `java.io.File` objects, extending `std.protocol.watch/IWatch`.

### Usage Pattern:

The `std.fs` module is essential for any Clojure application that needs to interact with the local filesystem. It provides:
*   **Robust File I/O:** A consistent and powerful API for file and directory operations.
*   **Cross-Platform Compatibility:** Built on `java.nio.file`, ensuring consistent behavior across different operating systems.
*   **Advanced Features:** Support for file attributes, symbolic links, and archive manipulation.
*   **Event-Driven Filesystem Interaction:** Real-time monitoring of filesystem changes.
*   **Code Analysis:** Utilities for reading and parsing Clojure source files.

By offering a comprehensive and well-designed filesystem API, `std.fs` simplifies complex filesystem tasks and enables the `foundation-base` project to manage its code and resources effectively.