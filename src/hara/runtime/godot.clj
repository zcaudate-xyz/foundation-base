(ns hara.runtime.godot
  (:require [lib.godot.bench :as bench]
            [hara.runtime.godot.client :as client]
            [std.lib.foundation :as f]))

(f/intern-in client/client:create
             client/godot
             client/godot:create
             client/godot-shared:create
             client/raw-eval-godot
             client/invoke-ptr-godot

             bench/bench-start
             bench/bench-stop
             bench/start-godot-server
             bench/stop-godot-server
             bench/all-godot-ports)

(def +init+ client/+init+)
