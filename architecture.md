# Indigo Architecture

This document describes the architecture of the `indigo` development environment, a real-time, interactive Clojure development environment.

## Vision: An Operating System for the Clojure Environment

The `indigo` environment can be thought of as an operating system built on top of the Clojure runtime. It provides a foundational layer of services and tools for interacting with the underlying environment, including:

*   **Process Management:** The ability to start, stop, and interact with long-running processes, such as the file system watcher.
*   **Inter-Process Communication:** An event bus for real-time communication between different parts of the system.
*   **A User Interface:** A web-based frontend for interacting with the system and its tools.
*   **A Set of Core Utilities:** A collection of tools for inspecting and manipulating the running system.

## Overview

The `indigo` environment is a web-based tool that allows developers to interact with a live Clojure process. It provides a number of tools for inspecting and manipulating the running system, including:

*   A **REPL** that streams output to the frontend.
*   A **file system watcher** that notifies the frontend of changes to the codebase.
*   A **code templating system** for generating new code from predefined templates.
*   A **resource explorer** for browsing and inspecting the available resources in the system.
*   A **var inspector** for looking up the source code, documentation, and tests for a given var.

## Architecture

The `indigo` environment is built on a **real-time, event-driven architecture**. The backend is a Clojure server that uses a `std.concurrent.bus`-based event bus to stream events to the frontend. The frontend is a `std.lang` application that connects to the server's event bus and displays the received events in a simple log.

### Backend

The backend is a single Clojure process that runs a `http-kit`-based web server. The server exposes two main endpoints:

*   A `/events` SSE endpoint that streams events from the event bus to the frontend.
*   A set of MCP endpoints for calling the various tools.

The backend is also responsible for:

*   Starting and stopping the event bus.
*   Starting and stopping the file system watcher.
*   Compiling and serving the `std.lang` frontend.

### Frontend

The frontend is a `std.lang` application that is compiled to JavaScript and served by the backend. The frontend connects to the `/events` SSE endpoint and displays the received events in a simple log. The frontend is also responsible for providing a UI for the various tools, but this has not yet been implemented.

### Event Bus

The event bus is the heart of the `indigo` environment. It is a `std.concurrent.bus`-based pub/sub system that allows the various backend tools to communicate with the frontend in real time. The event bus supports multiple topics, and the frontend can subscribe to any or all of these topics. The following topics are currently in use:

*   `:repl`: for streaming the output of the REPL.
*   `:watcher`: for sending notifications from the file system watcher.
*   `:templates`: for sending the rendered output of the code templating tool.

## Tools

The `indigo` environment provides a number of tools for interacting with the running system. These tools are all implemented as MCP services and can be called from the frontend.

*   **`eval-repl`:** Evaluates a Clojure expression and streams the output to the `:repl` topic on the event bus.
*   **`explore-resources`:** Returns a list of all available resources in the system.
*   **`inspect-resource`:** Returns the details of a specific resource.
*   **`apply-template`:** Applies a code template and streams the rendered output to the `:templates` topic on the event bus.
*   **`list-vars-and-tests`:** Returns a list of all vars in a given namespace and their associated tests.
*   **`inspect-var`:** Returns the source code, documentation, and tests for a given var.
