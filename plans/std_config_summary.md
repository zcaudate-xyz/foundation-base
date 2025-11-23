# std.config Summary

`std.config` is a library for configuration management that supports dynamic resolution, file inclusion, environment variable access, and encryption. It is designed to handle complex configuration scenarios through a directive-based approach.

## 1. Core Concept: Directives

Configurations are defined as EDN data structures. The core power of `std.config` comes from **Directives**, which are special vectors starting with a keyword (e.g., `[:env "HOME"]`). These directives are resolved at runtime.

### 1.1. Supported Directives

*   **System/Environment**:
    *   `[:env "VAR_NAME"]`: Reads an environment variable.
    *   `[:properties "prop.name"]`: Reads a Java system property.
    *   `[:global "path.to.val"]`: Reads from the global configuration map.
    *   `[:project :key]`: Reads from the project configuration.
*   **File/Resource Loading**:
    *   `[:file "path/to/file"]`: Loads content from a file.
    *   `[:resource "path/to/resource"]`: Loads content from the classpath.
    *   `[:include "path"]`: Smart loader that checks project, user home, and resources.
*   **Logic & Control Flow**:
    *   `[:or dir1 dir2]`: Returns the first non-nil resolution.
    *   `[:case val match result ...]`: Conditional switching.
    *   `[:merge map1 map2 ...]`: Merges maps.
    *   `[:format ["fmt" arg1 ...]]`: Formats strings.
    *   `[:str ["a" "b"]]`: Joins strings.
    *   `[:eval '(+ 1 2)]`: Evaluates Clojure code (safe subset/prewalked).
*   **Navigation**:
    *   `[:root [:path]]`: Accesses data from the root of the config.
    *   `[:parent [:path]]`: Accesses data from the parent map.

### 1.2. Options
Directives often accept an options map as the last argument:
*   `:default`: Value to return if resolution fails/is nil.
*   `:type`: Coerce the output (e.g., `:edn`, `:text`, `:key`).
*   `:secured`: Boolean, indicates if the content is encrypted.
*   `:key`: Key for decryption.

## 2. Security

`std.config` supports encrypted configuration values (`std.config.secure`).
*   **Encryption**: `secure/encrypt-text`
*   **Decryption**: `secure/decrypt-text`
*   **Directives**: Values can be marked with `{:secured true}`.

## 3. Usage

### 3.1. Loading a Config
```clojure
(require '[std.config :as config])

;; Load the default config file (config.edn)
(def cfg (config/load))

;; Load a specific file
(def specific-cfg (config/load "path/to/my-config.edn"))
```

### 3.2. Example Configuration (EDN)
```clojure
{:server {:port [:env "PORT" {:default 8080 :type :edn}]
          :host [:or [:env "HOST"] "localhost"]}
 :db     {:password [:file "secrets/db_password" {:secured true}]
          :url      [:format ["jdbc:postgresql://%s:%s/db"
                              [:root [:server :host]]
                              [:root [:server :port]]]}}
 :admins [:include "admins.edn" {:type :edn}]}
```

### 3.3. API
*   `config/load`: Main entry point to load and resolve a configuration file.
*   `config/resolve`: Resolves a specific form or directive.
*   `config/get-session`: Retrieves the global config session.

## 4. Key Files
*   `src/std/config.clj`: Public API.
*   `src/std/config/resolve.clj`: Core resolution logic and directive implementations.
*   `src/std/config/secure.clj`: Encryption/decryption utilities.
