## std.dom: A Comprehensive Summary (including submodules)

The `std.dom` module provides a powerful and flexible framework for building and managing user interfaces (UIs) in Clojure, inspired by concepts from React and other declarative UI libraries. It introduces a "code-as-data" approach to UI definition, allowing developers to describe UI structures as Clojure data (DOMs) which can then be rendered, updated, and manipulated efficiently. The module supports component-based development, local state management, reactive updates, and a robust diffing/patching mechanism for optimized UI rendering.

### `std.dom` (Main Namespace)

This namespace serves as the primary entry point for the DOM system, aggregating and re-exporting key functionalities from its submodules. It provides a high-level interface for creating, manipulating, and interacting with DOM structures.

**Key Re-exported Functions:**

*   From `std.dom.common`: `dom-attach`, `dom-children`, `dom-compile`, `dom-create`, `dom-detach`, `dom-item`, `dom-item?`, `dom-equal?`, `dom-metaclass`, `dom-metaprops`, `dom-metatype`, `dom-split-props`, `dom-top`, `dom-trigger`, `dom-vector?`, `dom?`, `dom-assert`.
*   From `std.dom.component`: `defcomp`, `dom-state-handler`.
*   From `std.dom.diff`: `dom-diff`, `dom-ops`.
*   From `std.dom.event`: `event-handler`, `handle-event`.
*   From `std.dom.find`: `dom-match?`, `dom-find`, `dom-find-all`.
*   From `std.dom.impl`: `dom-init`, `dom-remove`, `dom-render`, `dom-rendered`, `dom-replace`.
*   From `std.dom.item`: `item-constructor`, `item-setters`, `item-getters`, `item-access`, `item-create`, `item-props-set`, `item-props-delete`, `item-update`, `item-cleanup`.
*   From `std.dom.local`: `local`, `local-dom`, `local-dom-state`, `local-parent`, `local-parent-state`, `dom-send-local`, `dom-set-local`.
*   From `std.dom.react`: `react`, `dom-set-state`.
*   From `std.dom.type`: `metaclass`, `metaclass-add`, `metaclass-remove`, `metaprops`, `metaprops-add`, `metaprops-remove`, `metaprops-install`.
*   From `std.dom.update`: `dom-apply`, `dom-update`.

### `std.dom.common` (Core DOM Structure and Utilities)

This sub-namespace defines the fundamental `Dom` record, its properties, and common operations for creating, formatting, and comparing DOM nodes.

**Core Concepts:**

*   **`Dom` Record:** The central data structure representing a UI node. It contains `tag`, `props`, `item` (the actual rendered UI element), `parent`, `handler`, `shadow` (for components), `cache`, and `extra` metadata.
*   **Metaclass/Metatype/Metaprops:** A system for classifying and describing different types of UI elements (e.g., `:dom/element`, `:dom/component`, `:dom/value`).
*   **DOM Tree:** DOM nodes form a tree structure, with `parent` and implicit child relationships.

**Key Functions:**

*   **`dom?`**: Checks if an object is a `Dom` record.
*   **`dom-metaprops`, `dom-metatype`, `dom-metaclass`**: Accessors for metadata about a DOM node's type.
*   **`component?`, `element?`, `value?`**: Predicates for checking the metatype of a DOM node.
*   **`dom-item`, `dom-item?`**: Accessors for the actual rendered UI element associated with a DOM node.
*   **`dom-top`**: Returns the top-most ancestor of a DOM node.
*   **`dom-split-props`**: Splits properties into different categories (e.g., event handlers, regular props).
*   **`props-apply`**: Applies a function to properties, potentially recursing into nested DOMs.
*   **`dom-new`**: Creates a new `Dom` record.
*   **`dom-children`, `children->props`**: Functions for managing children within DOM props.
*   **`dom-create`**: Creates a `Dom` record from a tag, props, and children.
*   **`dom-format`**: Formats a `Dom` record for printing (e.g., `[:- :tag {:prop val} child]`).
*   **`dom-tags-equal?`, `dom-props-equal?`, `dom-equal?`**: Functions for comparing DOM nodes.
*   **`dom-clone`**: Creates a shallow copy of a DOM node.
*   **`dom-vector?`**: Checks if a vector represents a valid DOM structure.
*   **`dom-compile`**: Compiles a nested vector structure into a tree of `Dom` records.
*   **`dom-attach`, `dom-detach`**: Attaches or detaches an event handler to a DOM node.
*   **`dom-trigger`**: Triggers an event, propagating it up the DOM hierarchy.
*   **`dom-assert`**: Asserts that required properties are present.

### `std.dom.component` (Component-Based Development)

This sub-namespace provides the foundation for defining reusable UI components, supporting different component types (static, local, reactive) and lifecycle hooks.

**Core Concepts:**

*   **Component Types:** `:static` (purely functional, no internal state), `:local` (manages local state), `:react` (reactive to external state changes).
*   **Lifecycle Hooks:** `pre-render`, `post-render`, `pre-update`, `post-update`, `pre-remove`, `post-remove`, `wrap-template`.
*   **Shadow DOM:** Components maintain a "shadow DOM" (`:shadow` field) which is their rendered output, allowing for efficient diffing.

**Key Functions:**

*   **`dom-component?`**: Checks if a DOM node represents a component.
*   **`component-options`**: Prepares component options by merging mixins and template functions.
*   **`component-install`**: Installs a component definition into the `metaprops` registry.
*   **`defcomp` (macro)**: Defines a new UI component.
*   **`dom-render-component`**: Renders a component, executing its template and rendering its shadow DOM.
*   **`child-components`**: Collects child components within a DOM tree.
*   **`dom-remove-component`**: Removes a rendered component.
*   **`dom-ops-component`**: Generates diff operations for components.
*   **`dom-apply-component`**: Applies diff operations to a component.
*   **`dom-replace-component`**: Replaces a component with a new one.
*   **`dom-state-handler`**: A generic handler for updating component local state.

### `std.dom.diff` (DOM Diffing Algorithm)

This sub-namespace implements a diffing algorithm to efficiently compare two DOM trees and generate a minimal set of operations (an "edit script") to transform one into the other. This is crucial for optimizing UI updates.

**Core Concepts:**

*   **Edit Script:** A list of operations (e.g., `[:set key new-val old-val]`, `[:delete key old-val]`, `[:update key ops]`, `[:list-insert index items]`, `[:list-remove index count]`, `[:replace new-dom old-dom]`).
*   **Keyed Lists:** Supports diffing lists of DOMs using a `:dom/key` property for efficient element tracking.

**Key Functions:**

*   **`dom-ops` (multimethod)**: Generates diff operations for a given tag and old/new properties.
*   **`diff-list-element`**: Diffs individual elements within a list.
*   **`diff-list-elements`**: Diffs elements in two lists.
*   **`diff-list-dom`**: Diffs lists of DOMs using `:dom/key`.
*   **`diff-list`**: Generates diff operations for lists (both keyed and unkeyed).
*   **`diff-props-element`**: Diffs individual properties within a map.
*   **`diff-props`**: Generates diff operations for property maps.
*   **`dom-ops-default`**: Default implementation for `dom-ops`.
*   **`dom-diff`**: The main function for computing the difference between two DOM trees.

### `std.dom.event` (Event Handling)

This sub-namespace provides a standardized mechanism for handling UI events, including event propagation and dispatching to appropriate handlers.

**Key Functions:**

*   **`event-params`**: Converts event input into a standardized map.
*   **`event-handler`**: Finds the most appropriate event handler by traversing the DOM hierarchy.
*   **`handle-local`**: Handles events specifically for local components.
*   **`handle-event`**: The main function for handling an event, dispatching it to the correct handler based on its ID and type.

### `std.dom.find` (DOM Traversal and Querying)

This sub-namespace provides functions for searching and matching DOM nodes within a DOM tree.

**Key Functions:**

*   **`dom-match?`**: Checks if a DOM node's property matches a given value or predicate.
*   **`dom-find-props`**: Recursively searches for a DOM node within properties.
*   **`dom-find`**: Finds the first matching DOM node in a tree.
*   **`dom-find-all-props`**: Recursively finds all matching DOM nodes within properties.
*   **`dom-find-all`**: Finds all matching DOM nodes in a tree.

### `std.dom.impl` (DOM Rendering and Manipulation)

This sub-namespace provides the core implementation for rendering, removing, and replacing DOM nodes, acting as the interface to the underlying UI toolkit.

**Key Functions:**

*   **`dom-render` (multimethod)**: Renders a DOM node into an actual UI element.
*   **`dom-render-default`**: Default implementation for `dom-render`, which constructs the UI element using `item-constructor` and sets its properties using `item-setters`.
*   **`dom-init`**: Renders a DOM node if it hasn't been rendered yet.
*   **`dom-rendered`**: Renders a DOM form and returns the actual UI element.
*   **`dom-remove` (multimethod)**: Removes a rendered UI element from the display.
*   **`dom-remove-default`**: Default implementation for `dom-remove`.
*   **`dom-replace` (multimethod)**: Replaces one rendered UI element with another.
*   **`dom-replace-default`**: Default implementation for `dom-replace`.

### `std.dom.invoke` (DOM Invocation and Extension)

This sub-namespace provides a mechanism for extending the DOM system with new UI element types and components through `std.protocol.invoke/-invoke-intern`.

**Key Functions:**

*   **`invoke-intern-dom`**: A multimethod for defining new DOM element types (`:value`, `:element`) and components (`:react`, `:local`, `:static`).

### `std.dom.item` (UI Element Abstraction)

This sub-namespace provides an abstraction layer for interacting with the actual UI elements (e.g., JavaFX nodes, HTML elements). It defines how to construct, set properties, get properties, and clean up these elements.

**Key Functions:**

*   **`item-constructor` (multimethod)**: Returns the constructor function for a UI element based on its tag.
*   **`item-setters` (multimethod)**: Returns a map of setter functions for a UI element's properties.
*   **`item-getters` (multimethod)**: Returns a map of getter functions for a UI element's properties.
*   **`item-access`**: Accesses a property of a UI element using its getter.
*   **`item-create`**: Creates a new UI element.
*   **`item-props-update` (multimethod)**: Updates properties of a UI element based on diff operations.
*   **`item-props-set` (multimethod)**: Sets properties of a UI element.
*   **`item-props-delete` (multimethod)**: Deletes properties from a UI element.
*   **`item-update`**: Applies a list of diff operations to a UI element.
*   **`item-set-list` (multimethod)**: Updates a list property of a UI element.
*   **`item-cleanup` (multimethod)**: Cleans up a UI element when it's removed.

### `std.dom.local` (Local State Management)

This sub-namespace provides a mechanism for managing local, mutable state within UI components, allowing components to have their own internal state that can be updated reactively.

**Core Concepts:**

*   **Local State:** Components can declare local state, which is managed by an atom.
*   **Watches and Triggers:** Changes to local state can trigger watches and events.

**Key Functions:**

*   **`local-dom`**: Returns the nearest local component DOM in the hierarchy.
*   **`local-dom-state`**: Returns the state atom of the nearest local component.
*   **`local-parent`, `local-parent-state`**: Accessors for the parent local component and its state.
*   **`dom-ops-local`**: Generates diff operations for local component properties.
*   **`local-watch-create`, `local-watch-add`, `local-watch-remove`**: Functions for managing watches on local state.
*   **`local-trigger-add`, `local-trigger-remove`**: Functions for managing event triggers.
*   **`local-split-props`**: Splits properties into watchable and triggerable categories.
*   **`local-set`**: Sets local state properties and manages watches/triggers.
*   **`dom-send-local`**: Sends local events up the DOM hierarchy.
*   **`dom-apply-local`**: Applies diff operations to a local component.
*   **`localized-watch`**: Sets up initial watches and triggers for a local component.
*   **`dom-set-local`**: Sets a specific local state value.
*   **`localized-handler`**: Creates a handler for local component events.
*   **`localized-pre-render`, `localized-wrap-template`, `localized-pre-remove`**: Lifecycle hooks for local components.
*   **`localized`**: A mixin map for local components.
*   **`local`**: Accesses local state within a component.

### `std.dom.mock` (Mock UI Elements)

This sub-namespace provides mock implementations of UI elements for testing and development purposes, allowing DOM structures to be rendered and manipulated without a real UI toolkit.

**Key Functions:**

*   **`mock?`**: Checks if an object is a mock UI element.
*   **`mock-format`**: Formats a mock UI element for printing.
*   **`item-props-delete-mock`, `item-props-set-mock`, `item-set-list-mock`**: Custom property manipulation functions for mock elements.

### `std.dom.react` (Reactive State Management)

This sub-namespace provides a reactive state management system for UI components, allowing components to automatically re-render when their dependencies (atoms) change.

**Core Concepts:**

*   **`*react*`**: A dynamic var (volatile atom) that collects all atoms accessed within a reactive component's render function.
*   **Watches:** Components automatically add watches to their dependent atoms, triggering re-renders on change.

**Key Functions:**

*   **`reactive-pre-render`**: Sets up the reactive context for a component.
*   **`reactive-wrap-template`**: Wraps a component's template function to track atom dependencies.
*   **`reactive-pre-remove`**: Cleans up reactive watches.
*   **`reactive`**: A mixin map for reactive components.
*   **`react`**: Accesses a value from an atom, registering it as a dependency for the current reactive component.
*   **`dom-set-state`**: Sets a state value in an atom, potentially triggering reactive updates.

### `std.dom.type` (DOM Type System)

This sub-namespace defines the type system for DOM nodes, including metaclasses and metaprops, which describe the characteristics and behavior of different UI elements.

**Core Concepts:**

*   **Metaclass:** A classification of UI elements (e.g., `:dom/value`, `:dom/element`, `:dom/component`).
*   **Metaprops:** Metadata associated with a specific UI tag (e.g., `:mock/label`), including its metaclass, metatype, constructor, and property definitions.

**Key Functions:**

*   **`+metaclass+`, `+metaprops-tag+`**: Atoms storing metaclass and metaprops definitions.
*   **`metaclass`, `metaclass-remove`, `metaclass-add`**: Functions for managing metaclass definitions.
*   **`metaprops`, `metaprops-add`, `metaprops-remove`, `metaprops-install`**: Functions for managing metaprops definitions.

### `std.dom.update` (DOM Update and Patching)

This sub-namespace applies the diff operations generated by `std.dom.diff` to a rendered DOM tree, efficiently updating the UI.

**Key Functions:**

*   **`dom-apply` (multimethod)**: Applies a list of diff operations to a DOM node.
*   **`update-set`**: Applies a `:set` operation to properties.
*   **`update-list-insert`, `update-list-remove`, `update-list-update`, `update-list-append`, `update-list-drop`**: Functions for applying list-specific diff operations.
*   **`update-list`**: Applies list diff operations to a property.
*   **`update-props-delete`, `update-props-update`**: Functions for applying property-specific diff operations.
*   **`update-props`**: Applies diff operations to a property map.
*   **`dom-apply-default`**: Default implementation for `dom-apply`.
*   **`dom-update`**: Updates a DOM node to match a new DOM node by computing and applying diffs.
*   **`dom-refresh`**: Refreshes a DOM node, typically used for components.

### Usage Pattern:

The `std.dom` module is essential for building dynamic and interactive user interfaces in Clojure. It provides:
*   **Declarative UI:** Define UI as data, making it easier to reason about and manipulate.
*   **Component-Based Architecture:** Promote reusability and modularity in UI development.
*   **Efficient Updates:** Diffing and patching algorithms minimize UI re-renders.
*   **Reactive Programming:** Automatic UI updates in response to state changes.
*   **Local State Management:** Encapsulate component-specific state.
*   **Extensibility:** A protocol-driven design allows for integration with various UI toolkits (e.g., JavaFX, HTML/JS).

By offering a comprehensive set of tools for UI definition, rendering, and management, `std.dom` empowers developers to create sophisticated user experiences within the `foundation-base` ecosystem.