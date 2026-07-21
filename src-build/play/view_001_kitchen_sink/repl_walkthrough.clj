;; repl_walkthrough.clj — Step-through guide for play.view-001-kitchen-sink.build
;;
;; Open this file in Emacs with a Clojure REPL connected (cider-jack-in / lein repl).
;; Evaluate each form one at a time with C-c C-c or C-x C-e.
;; Comments above each form explain what you should see.

(ns play.view-001-kitchen-sink.repl-walkthrough
  (:require [std.lib.env :as env]
            [std.lib.os :as os]
            [std.make :as make]
            [std.make.project :as project]
            [play.view-001-kitchen-sink.build :as build]))

;; -------------------------------------------------------------------------
;; STEP 0: Confirm the build namespace is loaded
;; -------------------------------------------------------------------------

;; You should see the two build specs and the -main var.
(ns-publics 'play.view-001-kitchen-sink.build)

;; -------------------------------------------------------------------------
;; STEP 1: Inspect the make config for the JS target
;; -------------------------------------------------------------------------

;; A make config is a wrapper around an atom.  Deref it to see the resolved
;; configuration map (build dir, sections, default entries, etc.).
@build/VIEW-KITCHEN-SINK-JS

;; -------------------------------------------------------------------------
;; STEP 2: Inspect the Dart target config
;; -------------------------------------------------------------------------

@build/VIEW-KITCHEN-SINK-DART

;; -------------------------------------------------------------------------
;; STEP 3: Build only the JS target
;; -------------------------------------------------------------------------

;; This writes generated files to .build/view-kitchen-sink-js
;; (npm / esbuild are NOT invoked yet — only static files and emitted JS).
(make/build-all build/VIEW-KITCHEN-SINK-JS)

;; -------------------------------------------------------------------------
;; STEP 4: Inspect what the JS build produced
;; -------------------------------------------------------------------------

;; List the generated build directory.
(os/sh "ls" "-la" ".build/view-kitchen-sink-js")

;; The emitted JavaScript module tree should be under src/.
(os/sh "find" ".build/view-kitchen-sink-js/src" "-type" "f")

;; -------------------------------------------------------------------------
;; STEP 5: Build only the Dart target
;; -------------------------------------------------------------------------

;; This writes generated Dart files to .build/view-kitchen-sink-dart.
(make/build-all build/VIEW-KITCHEN-SINK-DART)

;; -------------------------------------------------------------------------
;; STEP 6: Inspect what the Dart build produced
;; -------------------------------------------------------------------------

(os/sh "find" ".build/view-kitchen-sink-dart" "-maxdepth" "3" "-type" "f")

;; -------------------------------------------------------------------------
;; STEP 7: Build both targets (same as `lein run -m play.view-001-kitchen-sink.build`)
;; -------------------------------------------------------------------------

(build/-main)

;; -------------------------------------------------------------------------
;; STEP 8: Run the generated JS project in a browser
;; -------------------------------------------------------------------------

;; Prerequisites: Node.js + npm.
;; The generated Makefile has install / bundle / start targets.
;; Run them from the build directory.

(os/sh "make" "install" {:root ".build/view-kitchen-sink-js"})

(os/sh "make" "bundle" {:root ".build/view-kitchen-sink-js"})

;; Start the static server.  This blocks the REPL, so run it in a separate
;; terminal or use a background process.  Then open http://localhost:8080.
#_(os/sh "make" "start" {:root ".build/view-kitchen-sink-js"})

;; -------------------------------------------------------------------------
;; STEP 9: Run the generated Dart project
;; -------------------------------------------------------------------------

;; Prerequisites: Dart SDK >= 3.6.0.

(os/sh "make" "get" {:root ".build/view-kitchen-sink-dart"})

;; This runs bin/main.dart, which prints the bundle summary.
(os/sh "make" "run" {:root ".build/view-kitchen-sink-dart"})

;; -------------------------------------------------------------------------
;; BONUS: Triggered rebuild after editing app.clj
;; -------------------------------------------------------------------------

;; If you edit play.view-001-kitchen-sink.app and want to rebuild only the
;; targets that declare it as a trigger, use build-triggered.
(project/build-triggered 'play.view-001-kitchen-sink.app)

;; -------------------------------------------------------------------------
;; BONUS: Check whether prerequisites are installed
;; -------------------------------------------------------------------------

(env/program-exists? "node")
(env/program-exists? "npm")
(env/program-exists? "dart")
(ns play.view-001-kitchen-sink.repl-walkthrough)