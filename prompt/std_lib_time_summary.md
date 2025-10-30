## std.lib.time: A Comprehensive Summary

The `std.lib.time` namespace provides a set of utility functions for working with time measurements in Clojure, offering high-resolution timing, human-readable formatting, and benchmarking capabilities. It leverages `System/nanoTime` and `System/currentTimeMillis` for precise measurements and includes functions for parsing time strings.

**Key Features and Concepts:**

1.  **High-Resolution Time Measurement:**
    *   `system-ns`: Returns the system's current time in nanoseconds (from `System/nanoTime`).
    *   `system-ms`: Returns the system's current time in milliseconds (from `System/currentTimeMillis`).
    *   `time-ns`: Returns the current time in nanoseconds using `hara.lib.foundation.Clock/currentTimeNanos` for potentially more consistent or specialized timing.
    *   `time-us`: Returns the current time in microseconds using `hara.lib.foundation.Clock/currentTimeMicros`.
    *   `time-ms`: Returns the current time in milliseconds using `hara.lib.foundation.Clock/currentTimeMillis`).

2.  **Time Formatting:**
    *   `format-ms`: Converts a duration in milliseconds into a human-readable string format (e.g., "02h 46m 40s").
    *   `format-ns`: Converts a duration in nanoseconds into a human-readable string format, scaling to nanoseconds, microseconds, milliseconds, seconds, kiloseconds, megaseconds, or gigaseconds.

3.  **Time Parsing:**
    *   `parse-ms`: Parses a string representation of time (e.g., "1s", "0.5h") into its equivalent duration in milliseconds.
    *   `parse-ns`: Parses a string representation of time (e.g., "2ns", "0.3s") into its equivalent duration in nanoseconds.

4.  **Elapsed Time Calculation:**
    *   `elapsed-ms`: Calculates the time elapsed in milliseconds since a given starting millisecond timestamp, with an option to format the output.
    *   `elapsed-ns`: Calculates the time elapsed in nanoseconds since a given starting nanosecond timestamp, with an option to format the output.

5.  **Benchmarking Macros:**
    *   `bench-ns`: A macro to measure the execution time of a code block in nanoseconds. It supports options for formatting, disabling garbage collection during measurement, and running the block multiple times.
    *   `bench-ms`: A macro to measure the execution time of a code block in milliseconds, with similar options to `bench-ns`.

**Usage and Importance:**

The `std.lib.time` module is essential for performance analysis, logging, and any application logic that requires precise timing or human-friendly time representations. Its benchmarking macros provide a convenient way to profile code execution, while the formatting and parsing functions facilitate user interaction and data processing involving time durations. This module contributes to the `foundation-base` project by offering robust and flexible tools for managing time-related aspects of applications.
