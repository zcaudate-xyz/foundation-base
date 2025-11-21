## std.time: A Comprehensive Summary

The `std.time` module in `foundation-base` provides a comprehensive and extensible framework for handling time-related data in Clojure. It focuses on abstracting various Java time representations (e.g., `java.util.Date`, `java.util.Calendar`, `java.time.Instant`, `java.time.ZonedDateTime`, `Long` milliseconds) into a unified system. This is achieved through a set of protocols for instants, durations, and representations, along with multimethods for coercion, formatting, and parsing. The module aims to provide flexible and type-agnostic time manipulation capabilities.

The module is organized into several sub-namespaces:

### `std.time.coerce`

This namespace handles the conversion of time-related objects between different types, including timezones and instants.

*   **`coerce-zone [value opts]`**: Coerces a timezone object from one type to another (e.g., `String` to `java.util.TimeZone`). It handles `nil` values by defaulting to the system's timezone.
*   **`coerce-instant [value opts]`**: Coerces an instant object between various Java time types (e.g., `Long` to `ZonedDateTime`, `Clock` to `Calendar`). It supports conversion to/from `Long` milliseconds and map representations.

### `std.time.common`

This namespace provides common definitions and utility functions used across the `std.time` module, including default settings and helper functions for `java.util.Calendar`.

*   **`*default-type*`**: A dynamic var holding the default time representation type (initially `clojure.lang.PersistentArrayMap`).
*   **`*default-timezone*`**: A dynamic var holding the default timezone string (initially `nil`, defaulting to local timezone).
*   **`+default-keys+`**: A list of common time components (millisecond, second, minute, hour, day, month, year) used for map and vector representations.
*   **`+zero-values+`**: A map of zero/default values for time components.
*   **`+default-fns+`**: A map linking time component keywords to their respective protocol functions (e.g., `:millisecond` to `protocol.time/-millisecond`).
*   **`calendar [date timezone]`**: Creates a `java.util.Calendar` instance from a `java.util.Date` and `java.util.TimeZone`.
*   **`default-type [& [cls]]`**: Getter/setter for `*default-type*`.
*   **`local-timezone []`**: Returns the ID of the system's default timezone.
*   **`default-timezone [& [tz]]`**: Getter/setter for `*default-timezone*`.

### `std.time.data`

This namespace primarily serves to load and initialize various time-related extensions by requiring other sub-namespaces. It acts as a central point for ensuring all necessary time data structures and formatters are available.

*   It requires namespaces for `java.text.SimpleDateFormat`, `java.sql.Timestamp`, `java.util.Calendar`, `java.util.Date`, `Long`, `Map`, `java.util.TimeZone`, `java.time.format.DateTimeFormatter`, `java.time.Clock`, `java.time.Instant`, `java.time.ZonedDateTime`, and `java.time.ZoneId`.

### `std.time.duration`

This namespace provides functionalities for working with time durations, including calculations for adjusting days based on years and months, and converting duration maps to fixed millisecond lengths.

*   **`forward-year-array`, `forward-month-array`, `backward-year-array`, `backward-month-array`**: Arrays used for calculating days in years and months, considering leap years and month lengths.
*   **`variable-keys`**: Keys representing variable-length durations (e.g., `:months`, `:years`).
*   **`ms-length`**: A list of time units and their equivalent milliseconds, used for fixed-length duration conversions.
*   **`adjust-options`**: A map containing options for adjusting days based on year and month, including prediction functions for leap years.
*   **`adjust-year-days [years rep]`**: Calculates the number of days to adjust based on a number of years and a time representation.
*   **`adjust-month-days [years months rep]`**: Calculates the number of days to adjust based on a number of months and a time representation.
*   **`adjust-days [duration-map rep]`**: Calculates the total number of days to adjust based on a duration map (containing `:years` and `:months`) and a time representation.
*   **`to-fixed-length [m]`**: Converts a duration map (containing fixed units like `:days`, `:hours`) to its equivalent length in milliseconds.
*   **`map-to-length [d rep]`**: Converts a duration map `d` to its total length in milliseconds, considering variable units like `:months` and `:years` based on a time representation `rep`.
*   **`protocol.time/-from-length` extension**: Extends the default implementation to convert a long duration into a map of time units.
*   **`protocol.time/IDuration` extension**: Extends `clojure.lang.APersistentMap` to implement `IDuration`, allowing duration maps to be converted to lengths.

### `std.time.format`

This namespace provides functions for formatting time objects into strings and parsing strings into time objects, supporting various Java formatters.

*   **`+format-cache+`, `+parse-cache+`**: Atoms used for caching formatter and parser instances for performance.
*   **`cache [cache constructor ks flag]`**: A helper function to manage caching of formatter/parser instances.
*   **`format [t pattern & [opts]]`**: Formats a time object `t` into a string according to a `pattern` and optional `opts` (including caching). It uses the appropriate formatter based on the type of `t`.
*   **`parse [s pattern & [opts]]`**: Parses a string `s` into a time object according to a `pattern` and optional `opts` (including caching and specifying the target type).

### `std.time.long`

This namespace provides extensions for `java.lang.Long` to integrate it into the `std.time` protocol system, treating `Long` values as milliseconds since the epoch.

*   **`long-meta`**: A map defining the metadata for `Long` as a time instant, including its base type, and map conversion proxies.
*   **`protocol.time/-time-meta` extension**: Extends `Long` to return `long-meta`.
*   **`protocol.time/IInstant` extension**: Extends `Long` to implement `IInstant`, defining `-to-long`, `-has-timezone?`, `-get-timezone`, and `-with-timezone`.
*   **`protocol.time/IDuration` extension**: Extends `Long` to implement `IDuration`, defining `-to-length`.
*   **`protocol.time/-from-long` extension**: Extends `Long` to return the `Long` value itself.
*   **`protocol.time/-now` extension**: Extends `Long` to return the current time in milliseconds.
*   **`protocol.time/-from-length` extension**: Extends `Long` to return the `Long` value itself.

### `std.time.map`

This namespace provides functionalities for converting time objects to and from map representations, allowing for easy manipulation of individual time components. It also extends `clojure.lang.PersistentArrayMap` and `clojure.lang.PersistentHashMap` to be time instants.

*   **`to-map [t opts & [ks]]`**: Converts a time object `t` into a map representation, extracting specified keys (`ks`) and including timezone and long value.
*   **`from-map [m opts fill]`**: Converts a map representation `m` back into a time object of a specified type, filling in missing values with `fill`.
*   **`with-timezone [m tz]`**: Adds or changes the timezone of a map-based time representation.
*   **`arraymap-meta`**: Metadata for `PersistentArrayMap` as a time instant.
*   **`protocol.time/-time-meta` extension**: Extends `PersistentArrayMap` to return `arraymap-meta`.
*   **`protocol.time/IInstant` extension**: Extends `PersistentArrayMap` to implement `IInstant`.
*   **`protocol.time/IRepresentation` extension**: Extends `PersistentArrayMap` to implement `IRepresentation`, allowing access to individual time components.
*   **`protocol.time/-from-long` extension**: Extends `PersistentArrayMap` to create a map representation from a long.
*   **`protocol.time/-now` extension**: Extends `PersistentArrayMap` to create a map representation of the current time.
*   **`hashmap-meta`**: Metadata for `PersistentHashMap` as a time instant.
*   **`protocol.time/-time-meta` extension**: Extends `PersistentHashMap` to return `hashmap-meta`.
*   **`protocol.time/-from-long` extension**: Extends `PersistentHashMap` to create a map representation from a long.
*   **`protocol.time/-now` extension**: Extends `PersistentHashMap` to create a map representation of the current time.
*   **`protocol.time/IInstant` extension**: Extends `PersistentHashMap` to implement `IInstant`.
*   **`protocol.time/IRepresentation` extension**: Extends `PersistentHashMap` to implement `IRepresentation`.

### `std.time.vector`

This namespace provides functionalities for converting time objects to and from vector representations, allowing for ordered access to time components.

*   **`to-vector [t opts ks]`**: Converts a time object `t` into a vector representation, extracting specified keys (`ks`) in order. It supports `:all` and keyword-based selection for `ks`.

### `std.time.zone`

This namespace provides utilities for working with timezones, including mapping offsets to timezone IDs and generating offset strings.

*   **`by-offset`**: A map that groups timezone IDs by their raw offset in milliseconds.
*   **`pad-zeros [s]`**: Pads a single-digit string with a leading zero.
*   **`generate-offsets []`**: Generates a sequence of common time offsets (e.g., "00:00", "00:15") and their millisecond equivalents.
*   **`by-string-offset`**: A map that maps string representations of offsets (e.g., "+00:00", "-05:00", "Z") to their millisecond values.

**Overall Importance:**

The `std.time` module is a critical component for any application within the `foundation-base` project that deals with dates, times, or durations. Its key contributions include:

*   **Unified Time Handling:** Provides a consistent API for working with diverse Java time types, simplifying time manipulation logic.
*   **Flexibility and Extensibility:** The protocol-based design allows for easy integration of new time representations and custom behaviors.
*   **Timezone Awareness:** Comprehensive support for timezones ensures accurate calculations and displays across different geographical regions.
*   **Human-Readable Formatting:** Tools for formatting and parsing time strings enhance user interaction and data exchange.
*   **Duration Management:** Capabilities for calculating and converting time durations are essential for scheduling, logging, and performance analysis.

By offering a robust and flexible time management system, `std.time` significantly contributes to the `foundation-base` project's ability to handle complex temporal data accurately and efficiently.
