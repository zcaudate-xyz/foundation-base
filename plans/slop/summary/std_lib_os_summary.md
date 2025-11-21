## std.lib.os: A Comprehensive Summary

The `std.lib.os` namespace provides a comprehensive set of utilities for interacting with the underlying operating system. It offers functionalities for executing shell commands, managing processes, interacting with `tmux` sessions, handling clipboard operations, and providing OS-specific notifications and audio feedback. This module is crucial for tasks requiring system-level integration, automation, and cross-platform compatibility within the `foundation-base` project.

**Key Features and Concepts:**

1.  **OS and Environment Information:**
    *   **`*native-compile*`**: A dynamic var indicating if the code is being compiled natively (e.g., with GraalVM).
    *   **`native? []`**: Checks if the current execution environment is a GraalVM native image.
    *   **`os []`**: Returns the name of the operating system (e.g., "Mac OS X", "Linux").
    *   **`os-arch []`**: Returns the architecture of the operating system (e.g., "x86_64").

2.  **Shell Command Execution (`sh` and helpers):**
    *   **`sh-wait [process]`**: Waits for a `java.lang.Process` to complete.
    *   **`sh-output [process]`**: Returns a map containing the exit code, standard output, and standard error of a completed process.
    *   **`sh-write [process content]`**: Writes string or byte content to the standard input stream of a running process.
    *   **`sh-read [process]`**: Reads available data from the standard output stream of a running process.
    *   **`sh-error [process]`**: Reads available data from the standard error stream of a running process.
    *   **`sh-close [process]`**: Closes the output stream of a process.
    *   **`sh-exit [process]`**: Destroys (terminates) the process.
    *   **`sh-kill [process]`**: Forcefully destroys the process.
    *   **`sh [cmd & args]`**: The primary function for executing shell commands. It can take a command string or a map of options. It supports:
        *   `args`: Command arguments.
        *   `print`: Whether to print the command.
        *   `wait`: Whether to wait for the command to complete.
        *   `trim`: Whether to trim whitespace from output.
        *   `wrap`: Whether to wrap output in `h/wrapped`.
        *   `output`: Whether to capture output.
        *   `inherit`: Whether to inherit IO from the parent process.
        *   `root`: Working directory.
        *   `ignore-errors`: Whether to ignore non-zero exit codes.
        *   `env`: Environment variables.
        *   `async`: Whether to run the command asynchronously (returns a `CompletableFuture`).
    *   **`sys:wget-bulk [root-url root-path files & [args]]`**: Downloads multiple files using `wget` in parallel, returning a `CompletableFuture` that completes when all downloads are done.

3.  **`tmux` Integration:**
    *   **`tmux-with-args [cmd opts]`**: Helper to construct `tmux` command arguments from options like `root` and `env`.
    *   **`tmux:kill-server []`**: Kills the `tmux` server.
    *   **`tmux:list-sessions []`**: Lists all active `tmux` session names.
    *   **`tmux:has-session? [session]`**: Checks if a `tmux` session exists.
    *   **`tmux:new-session [session & [opts]]`**: Creates a new detached `tmux` session.
    *   **`tmux:kill-session [session]`**: Kills a `tmux` session.
    *   **`tmux:list-windows [session]`**: Lists all window names within a `tmux` session.
    *   **`tmux:has-window? [session key]`**: Checks if a window exists within a `tmux` session.
    *   **`tmux:run-command [session key cmd]`**: Runs a command within a specific `tmux` window.
    *   **`tmux:new-window [session key & [opts]]`**: Opens a new window in a `tmux` session.
    *   **`tmux:kill-window [session key]`**: Kills a `tmux` window.

4.  **OS Interaction and Feedback:**
    *   **`say [& phrase]`**: Uses the system's text-to-speech utility (`say` on macOS) to speak a phrase.
    *   **`os-notify [title message]`**: Sends an OS-level notification (using `alerter` on macOS, `notify-send` on Linux).
    *   **`os-run [& commands]`**: Runs commands in a new OS-specific terminal tab (using `ttab` on macOS).
    *   **`beep []`**: Initiates a system beep sound.
    *   **`clip [s]`**: Copies a string to the system clipboard.
    *   **`clip:nil [s]`**: Copies a string to the clipboard and returns `nil`.
    *   **`paste []`**: Pastes a string from the system clipboard.

5.  **URL Encoding/Decoding:**
    *   **`url-encode [s]`**: URL-encodes a string using UTF-8.
    *   **`url-decode [s]`**: URL-decodes a string using UTF-8.

6.  **Process Component Integration:**
    *   `java.lang.Process` is extended to implement `std.protocol.component/IComponent` and `std.protocol.component/IComponentQuery`, allowing shell processes to be managed as components with lifecycle methods (`-start`, `-stop`, `-kill`) and status queries (`-started?`, `-stopped?`).

**Overall Importance:**

The `std.lib.os` module is a critical utility for the `foundation-base` project, enabling deep integration with the operating system for various automation, development, and deployment tasks. Its key contributions include:

*   **System Automation:** Provides powerful tools for scripting and automating system-level operations, such as file manipulation, process management, and external tool invocation.
*   **Cross-Platform Compatibility:** Offers abstractions and conditional logic to handle OS-specific differences, aiming for a more consistent experience across different operating systems.
*   **Enhanced Developer Experience:** Utilities like `tmux` integration, OS notifications, and clipboard operations streamline development workflows and improve productivity.
*   **Robust Process Management:** The `sh` function and its helpers provide fine-grained control over external processes, including asynchronous execution, input/output handling, and error management.
*   **Component-Based Resource Management:** Integrating `java.lang.Process` with the component protocol allows shell commands to be treated as managed resources, simplifying their lifecycle.

By offering these comprehensive OS interaction capabilities, `std.lib.os` significantly enhances the `foundation-base` project's ability to build powerful development tools and manage its complex, multi-language ecosystem.
