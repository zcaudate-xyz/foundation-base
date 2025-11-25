# Spaces as 3D Virtual Worlds

This document explores how the `std.lib.context` library (Registry, Space, Pointer) can be applied to architect 3D virtual worlds, such as games, simulations, or metaverses.

## Concept Mapping

The core primitives of `std.lib.context` map naturally to the hierarchical structure of a 3D simulation.

| `std.lib.context` | 3D World Concept | Description |
| :--- | :--- | :--- |
| **Space** | **World / Zone / Level** | Represents a distinct, contained environment (e.g., "Lobby", "Dungeon Level 1"). It holds the state of that world. |
| **Context** | **Engine / Backend** | The underlying technology or protocol used to drive the world (e.g., `:unity`, `:unreal`, `:webgl`, `:minecraft`). |
| **Runtime (Rt)** | **Session / Connection** | The active instance connecting the application to the engine. This could be a WebSocket connection to a game server, a JNI link to a physics engine, or a headless simulation loop. |
| **Pointer** | **Entity / Object Reference** | A handle to an entity within the world (e.g., Player ID `123`, Object `Tree_55`). Dereferencing it retrieves its current state (position, health). |

## Architecture

### 1. The Registry: Defining Engines
The registry is used to define the types of 3D engines available to the application.

```clojure
(ns world.registry
  (:require [std.lib.context.registry :as reg]))

;; Define a Three.js based context (running, say, in a browser via MCP or remote)
(reg/registry-install :three.js
  {:rt {:default {:resource :world.rt.threejs/connect
                  :config   {:host "localhost" :port 8080}}}})

;; Define a Unity context
(reg/registry-install :unity
  {:rt {:local   {:resource :world.rt.unity/local-connect}
        :remote  {:resource :world.rt.unity/remote-connect}}})
```

### 2. The Space: Instantiating a World
A `Space` represents a specific instance of a world. You might have multiple spaces running simultaneously (e.g., different shards or dungeon instances).

```clojure
(ns world.instance
  (:require [std.lib.context.space :as space]))

;; Create a space for "Zone A"
(def zone-a (space/space-create {:namespace "world.zone-a"}))

;; Configure Zone A to use the Three.js engine
(space/space-context-set zone-a :three.js :default {:config {:scene "forest_map.json"}})

;; Start the world (connects to the engine, loads the map)
(space/space-rt-start zone-a :three.js)
```

### 3. Pointers: Manipulating Entities
Pointers allow you to write Clojure code that manipulates objects inside the 3D world without worrying about the underlying network protocols or engine specifics.

```clojure
(ns world.entity
  (:require [std.lib.context.pointer :as ptr]))

;; Define a pointer to a specific entity in Zone A
(def player-1 (ptr/pointer {:context zone-a
                            :id      "player_123"
                            :type    :avatar}))

;; Dereferencing fetches current state (e.g., position, health)
(deref player-1)
;; => {:pos [10.5 0.0 5.2] :health 100 :state :idle}

;; Custom operations can be defined via IContext protocols
;; or simple helper functions that dispatch on the pointer
(defn move! [entity [x y z]]
  (ptr/invoke entity :move {:x x :y y :z z}))

(move! player-1 [12.0 0.0 6.0])
```

## Advanced Use Cases

### A. Seamless "Metaverse" Transitioning
Because `std.lib.context` separates the *Pointer* (reference) from the *Runtime* (connection), you can implement seamless transitions.
1.  User is in `Space A` (Lobby).
2.  User walks through a portal.
3.  Application initializes `Space B` (Game World).
4.  Client connects to `Space B`'s runtime.
5.  State is transferred.

### B. "Twin" Simulations (Digital Twins)
You can run two Spaces representing the same physical object but in different contexts.
*   `Space Real`: Connected to IoT sensors on a physical robot.
*   `Space Sim`: Connected to a physics simulation of that robot.
*   Pointers can be synchronized: `(move! sim-bot (deref real-bot))`.

### C. The "God Mode" Console
Using `std.lib.context`'s ability to manage multiple contexts within a single space, you can overlay different views:
*   **Context :render** -> Unreal Engine (High fidelity graphics)
*   **Context :physics** -> PhysX (Wireframe physics debug)
*   **Context :ai** -> Python/PyTorch (Behavior trees)

A single `Pointer` to an entity could be dereferenced in different contexts to see different aspects of it:
```clojure
(binding [*runtime* (space/space-rt-get zone-a :render)]
  @player-1) ;; => {:mesh "hero.fbx" ...}

(binding [*runtime* (space/space-rt-get zone-a :physics)]
  @player-1) ;; => {:velocity [0 1 0] :collider ...}
```

## Example: REPL-Driven Level Design
Imagine a workflow where you use the Clojure REPL to build a level live.

```clojure
(defn build-tower [height]
  (dotimes [i height]
    (let [pos [0 (* i 2.5) 0]
          block (ptr/pointer {:context *current-space*
                              :type    :structure.block
                              :pos     pos})]
      (ptr/invoke block :spawn)
      (Thread/sleep 100))))

;; "Live code" a tower into the running 3D world
(build-tower 10)
```
