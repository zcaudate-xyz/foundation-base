## std.print: A Comprehensive Summary (including submodules)

The `std.print` module provides a comprehensive set of utilities for formatted output in Clojure, focusing on enhancing readability and presentation in various contexts, including console, reports, and animated displays. It builds upon ANSI escape codes for terminal styling and offers flexible formatting options for different data types and structures.

### `std.print` (Main Namespace)

This namespace serves as the primary entry point for formatted printing, aggregating and re-exporting key functionalities from its submodules. It provides high-level functions for printing various types of reports and styled output.

**Key Re-exported Functions:**

*   From `std.concurrent.print`: `print`, `println`, `with-out-str`, `prn`.
*   From `std.print.base.report`: `print-header`, `print-row`, `print-title`, `print-subtitle`, `print-column`, `print-summary`, `print-tree-graph`.

### `std.print.ansi` (ANSI Styling)

This sub-namespace provides functions for generating ANSI escape codes to style text in terminal environments, including colors, highlights, and text attributes.

**Core Concepts:**

*   **`+colors+`, `+highlights+`, `+attributes+`**: Maps defining ANSI codes for various text properties.
*   **`+lookup+`**: A reverse lookup map for ANSI codes.

**Key Functions:**

*   **`encode-raw`**: Encodes raw ANSI modifier codes to a string.
*   **`encode`**: Encodes ANSI characters for modifiers (e.g., `:bold`, `:red`).
*   **`style`**: Styles text with ANSI modifiers.
*   **`style:remove`**: Removes ANSI formatting from a string.
*   **`define-ansi-forms` (macro)**: Defines helper functions for common ANSI styles (e.g., `blue`, `on-white`, `bold`).

### `std.print.base.animate` (Animation)

This sub-namespace provides utilities for displaying ASCII animations in the console.

**Key Functions:**

*   **`print-animation`**: Outputs an animated ASCII file or a sequence of frames.

### `std.print.base.report` (Report Generation)

This sub-namespace provides functions for generating structured reports, including headers, titles, rows, columns, and summaries.

**Key Functions:**

*   **`print-header`**: Prints a report header.
*   **`print-title`**: Prints a report title.
*   **`print-subtitle`**: Prints a report subtitle.
*   **`print-row`**: Prints a row of data.
*   **`print-column`**: Prints a column of data.
*   **`print-summary`**: Prints a summary of results.
*   **`print-tree-graph`**: Prints a tree-like graph.

### `std.print.format` (General Formatting)

This sub-namespace aggregates various formatting utilities from its submodules, providing a unified interface for common formatting tasks.

**Key Functions:**

*   From `std.print.format.chart`: `bar-graph`, `tree-graph`, `sparkline`, `table`, `table:parse`.
*   From `std.print.format.common`: `pad`, `pad:left`, `pad:right`, `pad:center`, `justify`, `border`, `indent`.
*   From `std.print.format.report`: `report:bold`, `report:column`, `report:header`, `report:row`, `report:title`.
*   From `std.print.format.time`: `t`, `t:ms`, `t:ns`, `t:time`.

### `std.print.format.chart` (Chart Formatting)

This sub-namespace provides functions for formatting various types of ASCII charts and tables for console output.

**Key Functions:**

*   **`lines:bar-graph`**: Formats an ASCII bar graph as lines.
*   **`bar-graph`**: Constructs an ASCII bar graph.
*   **`sparkline`**: Formats a sparkline.
*   **`tree-graph`**: Returns a string representation of a tree graph.
*   **`table-basic:format`**: Generates a basic ASCII table.
*   **`table-basic:parse`**: Reads a basic ASCII table from a string.
*   **`table`**: Generates a formatted ASCII table.
*   **`table:parse`**: Parses a formatted ASCII table.

### `std.print.format.common` (Common Formatting Utilities)

This sub-namespace provides fundamental utilities for padding, justifying, indenting, and bordering text.

**Key Functions:**

*   **`pad`, `pad:left`, `pad:right`, `pad:center`**: Functions for padding text.
*   **`justify`**: Justifies text to a given alignment.
*   **`indent`**: Indents lines of text.
*   **`pad:lines`**: Creates new lines of spaces.
*   **`border`**: Formats a border around lines of text.

### `std.print.format.report` (Report Formatting)

This sub-namespace provides functions for formatting elements within reports, such as rows, headers, and titles.

**Key Functions:**

*   **`lines:elements`**: Lays out an array of elements as rows.
*   **`lines:row-basic`**: Lays out raw elements for a row.
*   **`lines:row`**: Lays out row elements with colors and results.
*   **`report:header`**: Formats a report header.
*   **`report:row`**: Formats a report row.
*   **`report:title`**: Formats a report title.
*   **`report:bold`**: Formats text in bold.
*   **`report:column`**: Formats a report column.

### `std.print.format.time` (Time Formatting)

This sub-namespace provides functions for formatting time and duration values into human-readable strings, with optional styling.

**Key Functions:**

*   **`t:time`**: Formats a timestamp to time only.
*   **`t:ms`**: Converts milliseconds to a human-readable duration.
*   **`t:ns`**: Converts nanoseconds to a human-readable duration.
*   **`t:text`**: Formats nanoseconds to a string.
*   **`t:style`**: Sets the color for a time duration.
*   **`t`**: Formats nanoseconds to a styled string.

### `std.print.progress` (Progress Indicators)

This sub-namespace provides utilities for displaying progress indicators in the console, including progress bars and spinners.

**Key Functions:**

*   **`replace-center`**: Replaces the center of a string with text.
*   **`progress-bar-string`**: Converts a progress percentage to a progress bar string.
*   **`progress-spinner-string`**: Converts progress to a spinner string.
*   **`progress-eta`**: Calculates the estimated time left.
*   **`progress`**: Creates a progress structure.
*   **`progress-string`**: Creates a string representation of progress.
*   **`progress-update`**: Updates the progress meter.
*   **`progress-test`**: Demonstrates progress indicators.

### Usage Pattern:

The `std.print` module is essential for:
*   **User Feedback:** Providing clear and informative output to users in console applications.
*   **Debugging and Logging:** Enhancing the readability of debug messages and log entries.
*   **Reporting:** Generating structured reports and summaries.
*   **Visualizations:** Creating simple ASCII charts and progress indicators.

By offering a rich set of formatting and presentation tools, `std.print` improves the user experience and makes console-based applications more engaging and informative.