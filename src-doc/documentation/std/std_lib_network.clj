(ns documentation.std-lib-network
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.network` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "API" :link "std.lib.network"}]]

[[:api {:namespace "std.lib.network"}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_network_summary.md
;; sha256: 1a108aeeeb3e5a2900151e3d826151ae4b74dabcc8cbb4f099d219eee1df6f2a
[[:chapter {:title "std.lib.network: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-network-summary-md"}]]

"The `std.lib.network` namespace provides a collection of utility functions for network-related operations in Clojure, primarily focusing on local host information, socket management, and port availability checks. It leverages Java's `java.net` package to offer convenient wrappers for common networking tasks."

"**Key Features and Concepts:**"

"1.  **Local Host Information:**\n    *   `local-host`: Returns the `java.net.Inet4Address` object representing the local host.\n    *   `local-ip`: Retrieves the IP address of the local host as a string.\n    *   `local-hostname`: Returns the full hostname of the local machine.\n    *   `local-shortname`: Extracts the short name (first part) of the local hostname.\n\n2.  **Socket Management:**\n    *   `socket`: Creates a new `java.net.Socket` object, allowing connection to a specified host and port.\n    *   `socket:port`: Gets the remote port number of a connected socket.\n    *   `socket:local-port`: Gets the local port number of a connected socket.\n    *   `socket:address`: Returns the remote IP address of a connected socket.\n    *   `socket:local-address`: Returns the local IP address of a connected socket.\n\n3.  **Port Availability and Waiting:**\n    *   `port:check-available`: Checks if a given port is available for use by attempting to bind a `ServerSocket` to it.\n    *   `port:get-available`: Finds the first available port from a provided list of ports.\n    *   `wait-for-port`: Blocks execution until a specified port on a given host becomes available, with configurable timeout and pause intervals. This is useful for synchronizing services during startup or testing.\n\n4.  **Component Protocol Integration:**\n    *   The `java.net.Socket` class is extended to implement the `std.protocol.component/IComponent` and `std.protocol.component/IComponentQuery` protocols. This allows `Socket` objects to be managed as components within the `foundation-base` ecosystem, enabling standardized lifecycle management (`-start`, `-stop`, `-kill`) and status querying (`-started?`, `-stopped?`, `-remote?`)."

"**Usage and Importance:**"

"`std.lib.network` is a vital module for any application within the `foundation-base` project that needs to interact with network resources. It simplifies common networking tasks, from identifying local machine details to managing socket connections and ensuring port availability. The integration with the `std.protocol.component` allows network resources to be treated as first-class components, promoting a consistent and manageable approach to resource lifecycle within the larger system."
;; END merged documentation: plans/slop/summary/std_lib_network_summary.md
