## std.config: A Comprehensive Summary (including submodules)

The `std.config` module provides a powerful and flexible configuration management system for Clojure applications. It allows for defining configurations using a rich set of directives, resolving values from various sources (system properties, environment variables, project files, external files, resources), and handling sensitive data through encryption. The system is designed to be extensible, supporting different configuration formats (EDN, JSON, YAML, TOML) and secure key management (GPG).

### `std.config` (Main Namespace)

This namespace serves as the primary entry point for the configuration system, aggregating and re-exporting key functionalities from its submodules. It defines the core directives and option types that drive the configuration resolution process.

**Key Re-exported Functions:**

*   From `std.config.global`: `global`.
*   From `std.config.resolve`: `load`, `resolve`, `resolve-key`, `encrypt-text`, `decrypt-text`.
*   From `std.config.secure`: `resolve-key`, `encrypt-text`, `decrypt-text`.

**Core Concepts:**

*   **Directives:** Special vector forms (e.g., `[:env "VAR_NAME"]`, `[:file "path/to/file"]`) that instruct the configuration system how to resolve a value.
*   **Option Keys:** Metadata associated with configuration values (e.g., `:type`, `:secured`, `:key`, `:default`).
*   **Option Types:** Supported data types for resolved content (e.g., `:text`, `:edn`, `:json`, `:yaml`, `:gpg`, `:gpg.public`).

**Key Functions:**

*   **`get-session`, `swap-session`, `clear-session`**: Functions for interacting with the global session atom, which can store dynamic configuration values.
*   **`+directives+`**: A list of all supported configuration directives.
*   **`+opt-keys+`**: A list of supported option keys for configuration values.
*   **`+opt-types+`**: A list of supported types for resolving configuration content.

### `std.config.common` (Common Utilities and Multimethods)

This sub-namespace defines common multimethods that serve as extension points for resolving directives and handling different content types.

**Key Functions:**

*   **`-resolve-directive` (multimethod)**: The primary extension point for implementing new configuration directives. It dispatches based on the first element of a directive vector.
*   **`-resolve-type` (multimethod)**: The primary extension point for handling different content types during resolution (e.g., parsing JSON, YAML).

### `std.config.ext.gpg` (GPG Integration)

This sub-namespace provides integration with GPG (GNU Privacy Guard) for handling encrypted configuration values.

**Key Functions:**

*   **`resolve-type-gpg-public`**: Resolves content to a PGP public key (`org.bouncycastle.openpgp.PGPPublicKey`).
*   **`resolve-type-gpg`**: Resolves content to a PGP key pair (public and private keys).

### `std.config.ext.json` (JSON Integration)

This sub-namespace provides support for resolving configuration values from JSON content.

**Key Functions:**

*   **`resolve-type-json`**: Resolves JSON content into a Clojure map (with keywordized keys).

### `std.config.ext.toml` (TOML Integration)

This sub-namespace provides support for resolving configuration values from TOML content.

**Key Functions:**

*   **`resolve-type-toml`**: Resolves TOML content into a Clojure map (with keywordized keys).

### `std.config.ext.yaml` (YAML Integration)

This sub-namespace provides support for resolving configuration values from YAML content.

**Key Functions:**

*   **`resolve-type-yaml`**: Resolves YAML content into a Clojure map.

### `std.config.global` (Global Configuration Sources)

This sub-namespace provides functions for accessing and consolidating configuration values from various global sources, such as system properties, environment variables, project files, and user home directory files.

**Core Concepts:**

*   **`+session+`**: An atom for storing dynamic, session-specific configuration.
*   **`+cache`**: An atom for caching resolved global configuration values.
*   **`Global` record**: A record type for representing consolidated global configuration.

**Key Functions:**

*   **`global?`**: Checks if an object is a `Global` record.
*   **`global-raw`**: Constructs a `Global` record from a map, applying a key transformation function.
*   **`global-env-raw`**: Retrieves system environment variables as a `Global` record.
*   **`global-properties-raw`**: Retrieves system properties as a `Global` record.
*   **`global-project-raw`**: Retrieves project configuration (from `code.project`) as a `Global` record.
*   **`global-env-file-raw`**: Reads configuration from an `env.edn` file in the current directory.
*   **`global-home-raw`**: Reads configuration from a `global.edn` file in the user's `.hara` directory.
*   **`global-session-raw`**: Retrieves the current session configuration.
*   **`global-all-raw`**: Consolidates all global configuration sources into a single `Global` record.
*   **`global`**: The primary function for retrieving global configuration from a specified source (e.g., `:env`, `:properties`, `:project`, `:all`), with optional caching.

### `std.config.resolve` (Directive Resolution Engine)

This sub-namespace implements the core logic for resolving configuration directives and content from various sources. It uses a recursive prewalk approach to process nested directives.

**Core Concepts:**

*   **Directive Resolution:** Directives are processed recursively, allowing for complex, dynamic configuration.
*   **Path Tracking:** `*current*` and `*path*` dynamic vars track the current resolution context for error reporting.
*   **Error Handling:** Provides `ex-config` for creating informative exceptions during resolution.

**Key Functions:**

*   **`ex-config`**: Creates a configuration-specific exception.
*   **`walk`, `prewalk`**: Modified versions of `clojure.walk` functions for directive resolution.
*   **`directive?`**: Checks if a form is a configuration directive.
*   **`resolve-directive`**: Resolves a single directive, handling errors.
*   **`resolve`**: The main function for resolving a configuration form, recursively processing all directives. It can also bind a security key for decryption.
*   **`resolve-type`**: Dispatches to `common/-resolve-type` to convert content to a specific type (e.g., `:edn`, `:json`).
*   **`resolve-select`**: Selects specific keys or paths from resolved content.
*   **`resolve-content`**: Resolves content based on options like `:type`, `:secured`, `:key`, `:default`, `:select`.
*   **`resolve-directive-properties`**: Resolves values from system properties.
*   **`resolve-directive-env`**: Resolves values from environment variables.
*   **`resolve-directive-project`**: Resolves values from project metadata.
*   **`resolve-directive-format`**: Formats values into a string using `clojure.core/format`.
*   **`resolve-directive-str`**: Joins values into a string.
*   **`resolve-directive-or`**: Returns the first non-nil resolved value from a list of options.
*   **`resolve-directive-case`**: Implements a `case` statement for configuration values.
*   **`resolve-directive-error`**: Throws a configuration error.
*   **`resolve-directive-merge`**: Merges multiple configuration maps, supporting different merge strategies (`:default`, `:nil`, `:nested`, `:nested-nil`).
*   **`resolve-directive-eval`**: Evaluates a Clojure form within the configuration context.
*   **`resolve-map`**: Resolves values from a map using a path.
*   **`resolve-directive-root`**: Resolves a directive relative to the root of the current configuration.
*   **`resolve-directive-parent`**: Resolves a directive relative to the parent of the current configuration.
*   **`resolve-directive-global`**: Resolves a directive from the global configuration map.
*   **`resolve-path`**: Resolves a path string.
*   **`resolve-directive-file`**: Resolves content from a local file.
*   **`resolve-directive-resource`**: Resolves content from a classpath resource.
*   **`resolve-directive-include`**: Resolves content from a file or resource, searching in multiple locations.
*   **`load`**: Loads and resolves a configuration file (defaults to `config.edn`).

### `std.config.secure` (Secure Configuration)

This sub-namespace provides functionalities for encrypting and decrypting sensitive configuration values, typically using AES encryption.

**Core Concepts:**

*   **Master Key:** Uses a master key (from `+master-key+` or `*key*` dynamic var) for encryption/decryption.
*   **Key Resolution:** `resolve-key` handles various key formats (string, map, `java.security.Key`).
*   **Encryption/Decryption:** Leverages `std.lib.security` for cryptographic operations and `std.lib.encode` for Base64/hex encoding.

**Key Functions:**

*   **`+master-key+`**: The default master key, typically loaded from global configuration.
*   **`+master-defaults+`**: Default settings for the encryption algorithm (AES, RAW format, secret mode).
*   **`*key*`**: A dynamic var holding the currently active security key.
*   **`resolve-key`**: Resolves a key from various inputs into a `java.security.Key` object.
*   **`decrypt-text`**: Decrypts an encrypted string (Base64 or hex encoded) using a specified key.
*   **`encrypt-text`**: Encrypts a plain text string and returns its Base64 or hex encoded representation.

### Usage Pattern:

The `std.config` module is essential for managing application configurations in a flexible, dynamic, and secure manner. It's particularly useful for:
*   **Environment-Specific Configurations:** Easily switching configurations based on deployment environment (dev, test, prod).
*   **Sensitive Data Handling:** Storing API keys, passwords, and other secrets securely.
*   **Modular Configuration:** Breaking down large configurations into smaller, manageable files.
*   **Dynamic Value Resolution:** Resolving values from various runtime sources (environment, system properties).
*   **Extensible Formats:** Supporting different configuration file formats.

By providing a powerful and extensible configuration system, `std.config` enables developers to build applications that are easily configurable and adaptable to different operational contexts.