# `std.lib.resource` - A Lifecycle Management Library

The `std.lib.resource` library provides a robust and standardized way to manage the lifecycle of stateful components, such as database connections, thread pools, or external service clients. It ensures that resources are initialized, accessed, and shut down in a predictable and controlled manner.

## Core Concepts

The library is built around a few key concepts: the **Resource Spec**, **Modes**, and **Lifecycle Functions**.

### 1. The Resource Spec

A resource spec is a Clojure map that defines everything about a resource. It's the central point of configuration. You define a spec using the `res/res-spec-add` function.

A minimal spec looks like this:

```clojure
(res/res-spec-add
 {:type :my-app/database  ; A unique keyword identifying this resource type
  :instance {:create (fn [config] (connect-to-db (:connection-string config)))
             :setup  (fn [conn] (run-migrations conn))
             :teardown (fn [conn] (close-connection conn))}})
```

- **`:type`**: A unique keyword that acts as the ID for this type of resource.
- **`:instance`**: A map containing the core lifecycle functions.

### 2. Lifecycle Functions

These functions, defined within the `:instance` map of a spec, control how a resource is created, prepared, and destroyed.

- **`:create`**: The first function to be called. It receives the user-provided configuration map. Its job is to create and return the raw resource instance (e.g., the database connection object).
- **`:setup`**: This function is called immediately after `:create`. It receives the instance returned by `:create` and is used for any additional initialization steps (e.g., running database migrations, starting a thread pool). The value it returns is what the user ultimately receives when they request the resource.
- **`:teardown`**: This function is called when the resource is stopped (via `res:stop`). It receives the instance and should perform all necessary cleanup actions (e.g., closing connections, shutting down threads).

### 3. Modes

Modes determine the scope and uniqueness of a resource instance. The mode is defined in the spec and controls how many instances of a resource can exist and how they are identified.

- **`:global`** (Default): Only **one** instance of this resource can exist across the entire application. It's a singleton.
- **`:namespace`**: One instance of the resource can exist **per namespace**. This is useful for resources that are specific to a certain module of your code.
- **`:shared`**: Allows **multiple, named instances** of the resource to co-exist. Each instance is identified by a unique key (e.g., an `:id`). This is perfect for managing connections to multiple databases of the same type.

You configure the mode in the spec like this:

```clojure
(res/res-spec-add
 {:type :my-app/multi-db
  :mode {:default :shared  ; Set the mode to :shared
         :key :id}        ; Specify that the instance key is found under the :id key
  :instance {...}})
```

## Main API Functions

While the library has many functions, these are the ones you'll use most often.

- `(res/res-spec-add spec)`: Defines and registers a new resource spec.
- `(res/res resource-type selector-config [create-config])`: The primary function for accessing a resource. It's idempotent.
    - `resource-type`: The keyword identifying the resource (e.g., `:example/cache`).
    - `selector-config`: A map used to identify the specific instance. For `:shared` resources, this must contain the key specified in the spec (e.g., `{:id :users}`). For other modes, it can be an empty map `{}`.
    - `create-config` (Optional): A map of configuration that is passed to the `:create` function *only when a new instance is being created*. This argument is ignored if the resource is already running.
- `(res/res:start resource-type config)`: Explicitly starts a resource. Throws an error if it's already running.
- `(res/res:stop resource-type config)`: Explicitly stops a resource by calling its `:teardown` function.
- `(res/res:exists? resource-type config)`: Returns `true` if the specified resource instance is currently active.
- `(res/res:restart resource-type config)`: A convenience function that calls `res:stop` and then `res:start`.

## Practical Example: An In-Memory Cache

Here is a complete example of defining and using a simple, multi-instance, in-memory cache.

```clojure
(ns example.cache-demo
  (:require [std.lib.resource :as res]
            [std.lib.atom :as at]))

;; 1. DEFINE the resource spec for our cache.
;; It will be a simple atom, managed in :shared mode.
(res/res-spec-add
 {:type :example/cache
  :mode {:default :shared
         :key :id}
  :instance {:create (fn [config]
                       (println "Creating cache with initial data:" (:initial-data config))
                       (atom (:initial-data config)))
             :setup (fn [cache-atom]
                      (println "Setting up cache (atom hash:" (hash cache-atom) ")")
                      cache-atom) ; Nothing to do, just return it.
             :teardown (fn [cache-atom]
                         (println "Tearing down cache (atom hash:" (hash cache-atom) ")")
                         (reset! cache-atom {}))}}) ; Clear the cache on teardown.

;; 2. START and USE a cache for user sessions.
(println "\n--- Managing User Cache ---")
(def user-cache (res/res :example/cache {:id :users} {:initial-data {:user1 "token-a"}}))
(println "User cache content:" @user-cache)

(at/atom:set user-cache [:user2] "token-b")
(println "User cache updated:" @user-cache)

;; Requesting it again returns the SAME instance.
(def user-cache-again (res/res :example/cache {:id :users}))
(println "Is it the same instance?" (identical? user-cache user-cache-again))


;; 3. START and USE a separate cache for product data.
(println "\n--- Managing Product Cache ---")
(def product-cache (res/res :example/cache {:id :products} {:initial-data {"prod1" 99.99}}))
(println "Product cache content:" @product-cache)
(println "User cache is unaffected:" @user-cache)


;; 4. STOP the resources.
(println "\n--- Stopping Caches ---")
(res/res:stop :example/cache {:id :users})
(res/res:stop :example/cache {:id :products})

(println "\nUser cache exists after stop?" (res/res:exists? :example/cache {:id :users}))
(println "Final state of original user-cache atom:" @user-cache)
```

### Example Output

```
--- Managing User Cache ---
Creating cache with initial data: {:user1 "token-a"}
Setting up cache (atom hash: 12345)
User cache content: {:user1 "token-a"}
User cache updated: {:user1 "token-a", :user2 "token-b"}
Is it the same instance? true

--- Managing Product Cache ---
Creating cache with initial data: {"prod1" 99.99}
Setting up cache (atom hash: 67890)
Product cache content: {"prod1" 99.99}
User cache is unaffected: {:user1 "token-a", :user2 "token-b"}

--- Stopping Caches ---
Tearing down cache (atom hash: 12345)
Tearing down cache (atom hash: 67890)

User cache exists after stop? false
Final state of original user-cache atom: {}
```
