# `std.scheduler` Guide

`std.scheduler` is a system for managing concurrent, scheduled tasks (programs) using a `runner` abstraction. It handles lifecycle, spawning, and scaling of these tasks.

## Core Concepts

- **Runner**: The container for all scheduling logic. It has its own thread pools.
- **Program**: A definition of a task, including its logic (`main-fn`), state (`create-fn`), and frequency (`interval`).
- **Spawn**: An executing instance of a program. A single program (like "send-email") might have multiple spawns (workers).

## Usage

### Scenarios

#### 1. Periodic "Cron" Job

**Scenario: Run a cleanup task every minute.**

```clojure
(require '[std.scheduler :as scheduler]
         '[std.lib :as h])

(def runner (scheduler/runner {:id "system-runner"}))

(def cleanup-job
  {:id :cleanup
   :interval 60000 ;; 60s
   :create-fn (constantly {:deleted 0}) ;; Initial state
   :main-fn (fn [args timestamp]
              ;; Perform cleanup logic
              (println "Cleaning up at" timestamp)
              {:count 1}) ;; Return value updates state if merge-fn is defined
   })

(scheduler/install runner cleanup-job)
(scheduler/spawn runner :cleanup) ;; Start one instance
```

#### 2. Worker Pool Pattern

**Scenario: A job queue that needs multiple workers processing in parallel.**

You can define a program and then spawn it multiple times.

```clojure
(def worker-job
  {:id :worker
   :interval 100 ;; Check queue every 100ms
   :main-fn (fn [_ _]
              (when-let [job (pop-queue!)]
                (process job)))
   ;; Optional: Define limits
   :min 1
   :max 5})

(scheduler/install runner worker-job)

;; Scale up
(dotimes [_ 3]
  (scheduler/spawn runner :worker))
```

#### 3. Dynamic Scaling

**Scenario: Increase workers during high load.**

You can programmatically spawn or unspawn instances based on metrics.

```clojure
(defn check-load [runner]
  (let [q-size (get-queue-size)]
    (cond
      (> q-size 100) (scheduler/spawn runner :worker)
      (< q-size 10)  (scheduler/unspawn runner :worker))))
```

`unspawn` removes one instance, respecting the policy (e.g., kill the `:last` created one).

#### 4. Manual Triggering

**Scenario: Force execution immediately (e.g., via Admin UI).**

You don't need to wait for the interval.

```clojure
(scheduler/trigger runner :cleanup)
```

#### 5. Managing State

**Scenario: Accumulating results.**

The `merge-fn` allows a program to update its `:state` atom based on the return value of `main-fn`.

```clojure
(def stats-job
  {:id :stats
   :interval 1000
   :create-fn (constantly 0)
   :main-fn (fn [_ _] (rand-int 10))
   :merge-fn (fn [old-state new-val]
               (+ old-state new-val))})

;; Access state
(scheduler/get-state runner :stats)
```
