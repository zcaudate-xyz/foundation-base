## std.image: A Comprehensive Summary (including submodules)

The `std.image` module provides a comprehensive and extensible framework for image processing in Clojure. It abstracts away the complexities of underlying image representations (like Java's AWT `BufferedImage`) and offers a unified API for reading, writing, manipulating, and displaying images. The module emphasizes a "code-as-data" approach, allowing image properties and operations to be expressed as Clojure data structures.

### `std.image` (Main Namespace)

This namespace serves as the primary entry point for image operations, aggregating and re-exporting key functionalities from its submodules. It defines core image concepts and provides high-level functions for common image tasks.

**Core Concepts:**

*   **`IRepresentation` Protocol:** The central abstraction for image data, defining methods for accessing image channels, size, model, and raw data, as well as creating subimages.
*   **`ITransfer` Protocol:** Defines methods for converting image data between different formats (e.g., byte-gray, int-argb) and writing images to sinks.
*   **`ISize` Protocol:** Defines methods for accessing image width and height.
*   **Default Settings:** Dynamic vars (`*default-type*`, `*default-model*`, `*default-view*`) for configuring default image handling.

**Key Functions:**

*   **`default-type`**: Sets or retrieves the default image type (e.g., `BufferedImage`).
*   **`default-model`**: Sets or retrieves the default image model (e.g., `:int-argb`).
*   **`default-view`**: Sets or retrieves the default display view (e.g., `:awt`).
*   **`image?`**: Checks if an object is an image (implements `IRepresentation`).
*   **`image-channels`**: Returns the raw channel data of an image.
*   **`image-size`**: Returns the size (width, height) of an image.
*   **`image-model`**: Returns the color model of an image.
*   **`image-data`**: Returns the raw pixel data of an image.
*   **`size?`**: Checks if an object represents an image size.
*   **`height`, `width`**: Returns the height or width of an image or size object.
*   **`subimage`**: Extracts a rectangular subimage.
*   **`blank`**: Creates a blank image of a specified size and model.
*   **`read`**: Reads an image from a source (e.g., file path) into a specified model and type.
*   **`to-byte-gray`**: Converts an image to a byte-gray representation.
*   **`to-int-argb`**: Converts an image to an int-argb representation.
*   **`write`**: Writes an image to a sink (e.g., file path).
*   **`image`**: Creates an image from size, model, and data.
*   **`coerce`**: Converts an image to a different type or model.
*   **`display-class`**: Shows which types can be displayed.
*   **`display`**: Displays an image using a specified viewer.

### `std.image.awt` (AWT Integration)

This sub-namespace provides the concrete implementation for handling images using Java's Abstract Window Toolkit (AWT) `BufferedImage`. It extends the `std.image.protocol` to `BufferedImage` and provides AWT-specific I/O and display functionalities.

**Key Functions:**

*   Extends `BufferedImage` to `IRepresentation` and `ITransfer`.
*   **`image` (multimethod)**: Creates a `BufferedImage` from image data.
*   **`blank` (multimethod)**: Creates a blank `BufferedImage`.
*   **`read` (multimethod)**: Reads an image into a `BufferedImage`.
*   **`display` (multimethod)**: Displays a `BufferedImage` in an AWT/Swing window.
*   **`display-class` (multimethod)**: Returns `#{BufferedImage}`.

### `std.image.awt.common` (AWT Common Utilities)

This sub-namespace provides helper functions for working with AWT `BufferedImage` objects, including extracting size, model, and data, and performing conversions.

**Key Functions:**

*   **`type-lookup`, `name-lookup`**: Maps between AWT `BufferedImage` types and `std.image` model labels.
*   **`image-size`**: Returns the size of a `BufferedImage`.
*   **`image-model`**: Returns the `std.image` model of a `BufferedImage`.
*   **`image-data`**: Returns the raw pixel data of a `BufferedImage`.
*   **`image-channels`**: Returns the channel data of a `BufferedImage`.
*   **`subimage`**: Extracts a subimage from a `BufferedImage`.
*   **`image-to-byte-gray`**: Converts a `BufferedImage` to byte-gray data.
*   **`image-to-int-argb`**: Converts a `BufferedImage` to int-argb data.
*   **`image`**: Creates a `BufferedImage` from `std.image` data.

### `std.image.awt.display` (AWT Image Display)

This sub-namespace provides functionality for displaying `BufferedImage` objects in a Swing `JFrame`.

**Key Functions:**

*   **`create-viewer`**: Creates a Swing `JFrame` and `JComponent` for displaying images.
*   **`display`**: Displays a `BufferedImage` in the created viewer, with options for channel selection.

### `std.image.awt.io` (AWT Image I/O)

This sub-namespace provides functions for reading and writing `BufferedImage` objects using `javax.imageio.ImageIO`.

**Key Functions:**

*   **`providers`**: Lists available `ImageIO` service providers (readers, writers, transcoders).
*   **`supported-formats`**: Lists supported image formats (e.g., PNG, JPEG).
*   **`awt->int-argb`**: Converts an indexed or custom `BufferedImage` to `TYPE_INT_ARGB`.
*   **`read`**: Reads an image from an input source into a `BufferedImage`.
*   **`write`**: Writes a `BufferedImage` to an output sink in a specified format.

### `std.image.awt.rendering` (AWT Rendering Hints)

This sub-namespace provides utilities for configuring rendering hints for Java2D `Graphics2D` objects, allowing control over image quality and performance.

**Key Functions:**

*   **`hint-lookup`, `hint-keys`, `hint-values`**: Maps between keyword representations of rendering hints and their AWT `RenderingHints` constants.
*   **`hint-options`**: Lists available options for rendering hints.
*   **`hints`**: Creates a map of `RenderingHints` objects from keyword options.

### `std.image.base` (Base Image Representation)

This sub-namespace defines a generic, data-oriented `Image` record that serves as a common representation for image data, independent of specific AWT `BufferedImage` implementations. It extends the `std.image.protocol` to this `Image` record.

**Key Functions:**

*   **`Image` record**: A record that holds `model`, `size`, and `data` for an image.
*   **`image`**: Creates an `Image` record.
*   **`read` (multimethod)**: Reads an image into an `Image` record.
*   **`blank` (multimethod)**: Creates a blank `Image` record.
*   **`display` (multimethod)**: Displays an `Image` record.
*   **`display-class` (multimethod)**: Returns `#{std.image.base.Image}`.

### `std.image.base.common` (Base Image Common Utilities)

This sub-namespace provides fundamental operations for manipulating the generic `Image` record, including channel creation, copying, subimaging, and conversions between different standard image types (color, grayscale).

**Key Functions:**

*   **`create-channels`**: Creates raw data arrays for image channels based on a model.
*   **`empty`**: Creates an empty `Image` record.
*   **`copy`**: Creates a copy of an `Image` record.
*   **`subimage`**: Extracts a subimage from an `Image` record.
*   **`display-standard-data`**: Converts raw image data (arrays) into Clojure vectors for display.
*   **`standard-color-data->standard-gray`**: Converts standard color data to standard grayscale data.
*   **`standard-color->standard-gray`**: Converts a standard color image to a standard grayscale image.
*   **`standard-gray->standard-color`**: Converts a standard grayscale image to a standard color image.
*   **`standard-type->standard-type`**: Converts between standard image types (color/gray).
*   **`mask-value`**: Extracts a value from an integer using a bitmask.
*   **`shift-value`**: Shifts a value by a specified number of bits.
*   **`retrieve-single`**: Retrieves a single channel's data from an image.
*   **`slice`**: Extracts a single channel (e.g., red, green, blue, alpha) from an image as a new grayscale image.
*   **`retrieve-all`**: Retrieves all channel data from an image.
*   **`color->standard-color`**: Converts a color image to a standard color image.
*   **`gray->standard-gray`**: Converts a grayscale image to a standard grayscale image.
*   **`type->standard-type`**: Converts an image to a standard type (color or gray).
*   **`set-single-val`**: Calculates a single pixel value from channel inputs.
*   **`set-single`**: Sets a single channel's data.
*   **`set-all`**: Sets all channels' data according to a model.
*   **`convert-base`**: Converts an image between different models via standard intermediate types.
*   **`convert`**: Converts an image to a specified model.
*   **`base-map`**: Returns the base map representation of an image.

### `std.image.base.display` (Base Image Display)

This sub-namespace provides functions for rendering generic `Image` records as ASCII art strings, suitable for console output.

**Key Functions:**

*   **`render-string`**: Renders rows of pixel values into an ASCII string using a gradient table.
*   **`byte-gray->rows`**: Converts byte-gray data into rows of pixel values.
*   **`render-byte-gray`**: Renders a byte-gray image as an ASCII string.
*   **`int-argb->rows`**: Converts int-argb data into rows of grayscale pixel values.
*   **`render-int-argb`**: Renders an int-argb image as an ASCII string.
*   **`render`**: Renders an image (potentially slicing a channel) into an ASCII string.
*   **`display`**: Prints an ASCII representation of an image to the console.
*   **`animate`**: Animates a sequence of images in the console.

### `std.image.base.display.gradient` (ASCII Gradient Generation)

This sub-namespace provides utilities for generating ASCII character gradients, used by `std.image.base.display` to render images as text.

**Key Functions:**

*   **`ramp-dark`, `ramp-light`**: Predefined strings of characters for dark and light gradients.
*   **`create-lookup`**: Creates a lookup map for characters based on a range.
*   **`create-single`**: Creates a single-character gradient map.
*   **`create-multi`**: Creates a multi-character gradient map (for wider pixels).
*   **`lookup-char`**: Looks up a character from a gradient table based on a numeric value.

### `std.image.base.model` (Image Color Models)

This sub-namespace defines a comprehensive set of image color models, specifying how pixel data is organized and interpreted.

**Core Concepts:**

*   **`model-lookup`**: A map defining various image models (e.g., `:int-argb`, `:byte-gray`, `:3-byte-rgb`), including their type (`:color`, `:gray`), metadata (channel types, spans), and data access definitions.
*   **Channel Access:** Defines how to extract and combine individual color channels (alpha, red, green, blue) from raw pixel data.

**Key Functions:**

*   **`create-model`**: Creates an image model definition from a label.
*   **`*defaults*`**: Default color and grayscale models.
*   **`model-inv-table`**: Creates an inverse access table for setting data within an image.
*   **`model`**: Creates an image model, allowing for overwrites of predefined models.

### `std.image.base.size` (Image Size Utilities)

This sub-namespace provides utilities for working with image dimensions (width and height).

**Key Functions:**

*   Extends `IPersistentVector` and `IPersistentMap` to `ISize`.
*   **`size->map`**: Converts a size representation (vector or map) to a map with `:width` and `:height`.
*   **`length`**: Calculates the total number of pixels (width * height).

### `std.image.base.util` (Base Image Low-Level Utilities)

This sub-namespace provides low-level utility functions for bit manipulation, array conversions, and generating code for packing/unpacking pixel data.

**Key Functions:**

*   **`type-lookup`**: Maps Java primitive types to their properties (size, array functions, unchecked functions, aset functions).
*   **`int->bytes`, `bytes->int`**: Converts between integers and byte arrays (for ARGB values).
*   **`array-fn`**: Returns the appropriate primitive array constructor function based on element size.
*   **`mask`**: Generates a bitmask.
*   **`form-params`**: Returns bitmasks and start positions for packing/unpacking values.
*   **`<<form`, `<<fn`, `<<`**: Functions for generating code and applying functions to pack multiple values into a single integer.
*   **`>>form`, `>>fn`, `>>`**: Functions for generating code and applying functions to unpack a single integer into multiple values.
*   **`byte-argb->byte-gray`, `int-argb->byte-gray`**: Converts ARGB data to grayscale.
*   **`byte-gray->int-argb`**: Converts grayscale data to ARGB.

### `std.image.protocol` (Image Protocols)

This sub-namespace defines the core protocols that abstract image representation, manipulation, and display, allowing for extensible image handling.

**Key Protocols:**

*   **`ISize`**: Defines `-width` and `-height`.
*   **`IRepresentation`**: Defines `-channels`, `-size`, `-model`, `-data`, `-subimage`.
*   **`ITransfer`**: Defines `-to-byte-gray`, `-to-int-argb`, `-write`.
*   **`ITransform`**: Defines `-op` for image transformations.
*   **Multimethods:** `-image`, `-blank`, `-read`, `-display`, `-display-class`.

### Usage Pattern:

The `std.image` module is essential for any Clojure application that needs to perform image processing tasks. It provides:
*   **Unified API:** A consistent way to interact with images regardless of their underlying representation.
*   **Extensibility:** New image types and operations can be added by extending protocols.
*   **Data-Oriented Approach:** Image data and models are represented as Clojure data, facilitating manipulation.
*   **Low-Level Control:** Access to raw pixel data and bit manipulation utilities.
*   **Debugging and Visualization:** ASCII rendering for quick inspection of image content.

By offering a comprehensive and extensible image processing framework, `std.image` empowers developers to integrate image functionalities seamlessly into their Clojure applications.