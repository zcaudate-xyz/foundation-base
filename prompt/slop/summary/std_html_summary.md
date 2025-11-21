## std.html: A Comprehensive Summary

The `std.html` namespace provides a Clojure-idiomatic way to parse, manipulate, and generate HTML content, leveraging the powerful Jsoup Java library. It allows developers to represent HTML as Clojure data structures (trees), convert between these trees and Jsoup `Node` objects, and perform common HTML operations like parsing, formatting, and CSS-like selection. This module is particularly useful for web scraping, HTML templating, and processing HTML content within Clojure applications.

### Core Concepts:

*   **Jsoup Integration:** The module is built on top of Jsoup, providing access to its robust HTML parsing and manipulation capabilities.
*   **HTML as Data:** HTML structures are represented as Clojure vectors, where the first element is a keyword representing the tag, followed by an optional map of attributes, and then child elements (which can be strings, other vectors, or Jsoup `Node` objects).
*   **Node Objects:** Jsoup `Node` objects (e.g., `Element`, `TextNode`, `Document`) are used internally for efficient HTML manipulation.
*   **Conversion Functions:** A set of functions for seamless conversion between HTML strings, Jsoup `Node` objects, and Clojure tree representations.

### Key Functions:

*   **`node->tree` (multimethod)**:
    *   **Purpose:** Converts a Jsoup `Node` object (or its subclasses like `Element`, `TextNode`, `Document`) into a Clojure tree representation.
    *   **Usage:** `(node->tree (parse "<body><div>hello</div>world</body>"))`
*   **`tree->node`**:
    *   **Purpose:** Converts a Clojure tree representation of HTML into a Jsoup `Element` object.
    *   **Usage:** `(tree->node [:body [:div "hello"] "world"])
*   **`parse`**:
    *   **Purpose:** Parses an HTML string into a Jsoup `Element` object. It intelligently handles different HTML structures (full HTML, body fragments).
    *   **Usage:** `(parse "<body><div>hello</div>world</body>")`
*   **`inline`**:
    *   **Purpose:** Removes all newlines from an HTML string, effectively inlining it.
    *   **Usage:** `(inline (html [:body [:div "hello"] "world"]))
*   **`tighten`**:
    *   **Purpose:** Removes unnecessary newlines and whitespace from HTML elements that contain no internal elements, making the HTML more compact.
    *   **Usage:** `(tighten "<b>\nhello\n</b>")`
*   **`generate`**:
    *   **Purpose:** Generates a formatted HTML string from a Jsoup `Element` object.
    *   **Usage:** `(generate (tree->node [:body [:div "hello"] "world"]))
*   **`html`**:
    *   **Purpose:** A versatile function that converts various representations (string, vector tree, Jsoup `Node`) into a formatted HTML string.
    *   **Usage:** `(html [:body [:div "hello"] "world"])
*   **`html-inline`**:
    *   **Purpose:** Generates an inlined HTML string (without newlines) from a representation.
    *   **Usage:** `(html-inline [:body [:div "hello"] "world"])
*   **`node`**:
    *   **Purpose:** Converts various representations (string, vector tree) into a Jsoup `Node` object.
    *   **Usage:** `(node [:body [:div "hello"] "world"])
*   **`tree`**:
    *   **Purpose:** Converts various representations (string, Jsoup `Node`) into a Clojure tree representation of HTML.
    *   **Usage:** `(tree +content+)`
*   **`IText` Protocol**:
    *   **Purpose:** Defines a `text` method for extracting the text content from Jsoup `Elements` or `Element` objects.
*   **`select`**:
    *   **Purpose:** Applies a CSS selector query to a Jsoup `Element` and returns matching `Elements`.
    *   **Usage:** `(select my-node "div.my-class")`
*   **`select-first`**:
    *   **Purpose:** Applies a CSS selector query and returns the first matching `Element`.
    *   **Usage:** `(select-first my-node "p")`

### Usage Pattern:

This namespace is highly valuable for:
*   **Web Scraping:** Easily parsing HTML from web pages and extracting specific data using CSS selectors.
*   **HTML Templating:** Generating dynamic HTML content from Clojure data.
*   **Content Transformation:** Modifying existing HTML structures programmatically.
*   **Testing:** Creating and asserting against HTML structures in tests.
*   **Data Extraction:** Extracting text or attributes from HTML documents.

By providing a seamless bridge between Clojure data and Jsoup's HTML manipulation capabilities, `std.html` empowers developers to work with HTML content effectively within their Clojure applications.
