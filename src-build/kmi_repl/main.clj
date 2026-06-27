(ns kmi-repl.main
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[kmi.lang.runtime :as rt]
             [kmi.lang.runtime.eval :as eval]
             [xt.lang.spec-base :as xt]]
   :static {:export false}})

(def.js CONSOLE (!:G console))
(def.js PROCESS (!:G process))

(def.js repl-state
  {"runtime" (rt/empty-runtime)})

(defn.js process-line
  "evaluates a single line of input and prints the result"
  {:added "4.1"}
  [line]
  (var console CONSOLE)
  (var out (rt/eval-string (xt/x:get-key -/repl-state "runtime") line))
  (if (eval/errorp out)
    (console.error (xt/x:get-key out "error"))
    (console.log (eval/get-value out)))
  (xt/x:set-key -/repl-state "runtime" (eval/get-runtime out))
  (console.log "kmi>"))

(defn.js on-data
  "handles a stdin data chunk"
  {:added "4.1"}
  [chunk]
  (var text (+ chunk ""))
  (var lines (text.split "\n"))
  (xt/for:array [line lines]
    (when (not= line "")
      (-/process-line line))))

^{:tag "kmi-repl.main"}
(defrun.js -main
  (var console CONSOLE)
  (var process PROCESS)
  (console.log "KMI Node REPL")
  (console.log "kmi>")
  (var stdin (. process stdin))
  (stdin.on "data" -/on-data))
