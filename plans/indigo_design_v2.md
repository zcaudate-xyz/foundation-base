# Indigo Platform - High Level Design & UI Specification

**Status:** Draft / Template
**Based on:** `https://maple-saint-22074549.figma.site` (Screenshot provided)
**Goal:** To define the layout, screens, and functionality for the new Indigo "System Explorer" interface.

---

## 1. Global Layout Architecture

The application uses a **resizable pane-based layout** optimized for high-density information display. The interface is divided into four primary regions:

1.  **Top Navigation Bar:** Global context and controls.
2.  **Space View (Main Canvas):** The primary visualization area (Top-Left).
3.  **Event Stream (Activity Log):** Real-time system feed (Bottom-Left).
4.  **Inspector (Context Panel):** Details for selected items (Right Column).

**[User Input Required]:**
*   *Should the layout state (pane sizes, visibility) persist between sessions?*
*   *Are there other layout modes (e.g., "Focus Mode" hiding the stream)?*

---

## 2. Component Specifications

### 2.1. Top Navigation Bar
**Location:** Top of the viewport.
**Height:** Fixed (e.g., 40-50px).

**Visual Elements:**
*   **App Logo/Name:** "indigo"
*   **Menus:** File, Workspace, Apps.
*   **Breadcrumbs:** Context navigation (e.g., `default / user`).
*   **Right Controls:**
    *   Search (`Cmd+K`)
    *   Settings (Gear icon)
    *   Connection Status (Green dot + "Connected")

**Functionality:**
*   **Workspace Switching:** Ability to switch between different projects or environments.
*   **Search:** Global command palette to find files, actors, or settings.

**[User Input Required]:**
*   *What specific items go in the "File" and "Apps" menus?*
*   *What does the "default / user" breadcrumb represent? (Namespace / Actor? Project / Branch?)*

---

### 2.2. Space View
**Location:** Central / Top-Left pane.
**Purpose:** The primary workspace for visualization.
**Screenshot Context:** Currently shows an empty dark void with a toolbar.

**Toolbar Controls:**
*   Dropdown: "Space View" (View selector?)
*   Label: `— user` (Current scope?)
*   Icons: Grid view, Expand/Maximize.

**Potential Functionality (To be defined):**
*   **Graph Visualization:** Display nodes/actors and their relationships.
*   **3D World:** A 3D render of the `std.lib.context` "World".
*   **Canvas:** Infinite canvas for organizing logic blocks.

**[User Input Required]:**
*   *What is explicitly rendered here? (Nodes, Code, 3D Objects?)*
*   *What interactions are supported? (Drag-and-drop, Pan/Zoom, Selection box?)*

---

### 2.3. Event Stream
**Location:** Bottom-Left pane.
**Purpose:** Real-time visibility into system execution, tests, and message passing.

**Visual Structure:**
*   **Tabs/Filters:** `all`, `repl`, `invoke`, `test`.
*   **Search Bar:** Filter events by text.
*   **List View:**
    *   **Timestamp:** `15:40:55.027`
    *   **Type Indicator:** Icon + Color (Yellow for Test, Blue for Invoke, Red for Error).
    *   **Source:** Namespace/Actor (e.g., `test • greet • user`).
    *   **Message:** "Running test: test-greet-valid" or "Validation failed...".
    *   **Status/Result:** "✓ Test passed (12ms)".

**Functionality:**
*   **Live Updates:** Auto-scrolls as new events arrive.
*   **Filtering:** Toggle visibility of specific event types.
*   **Interaction:** Clicking an event might highlight the relevant Actor in the Space View or open details in the Inspector.

**[User Input Required]:**
*   *Can users replay events from here?*
*   *Does "repl" imply an interactive command line input at the bottom?*

---

### 2.4. Inspector
**Location:** Right vertical pane (Full height below nav).
**Purpose:** Detailed view and editing of the currently selected context.
**Screenshot Context:** Shows "Select an actor to inspect" (Empty state).

**Functionality:**
*   **Context Sensitive:** Content changes based on selection (Actor, Event, File).
*   **Tabs:** Likely needed for different aspects (State, Config, Source, Metrics).

**[User Input Required]:**
*   *What data is editable vs. read-only?*
*   *If an Actor is selected, do we see its live internal state (Atom values)?*

---

## 3. Data & Connectivity

**Backend:** `indigo.server`
**Communication:** likely WebSocket for the Event Stream.

**[User Input Required]:**
*   *Does the frontend need to poll for "Space View" updates, or is it pushed via the same socket?*
*   *How are "commands" sent back to the system (e.g., running a test from the UI)?*
