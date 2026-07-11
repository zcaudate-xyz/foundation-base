(ns jvm.chisel.emit
  "SystemVerilog emission and Verilator lint for `jvm.chisel` module builders.

   Thin wrappers around `jvm.chisel.internal/emit-system-verilog` (the circt
   `ChiselStage`) plus an optional `verilator --lint-only` check. Emission is
   pure (no simulation), so these are cheap to run and give an inspectable
   netlist for any verified module."
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [jvm.chisel.internal :as in]))

(defn system-verilog
  "Return the SystemVerilog source for `builder` (a `jvm.chisel/module` thunk) as
   a string. Optional `chisel-args`/`firtool-opts` are passed to `ChiselStage`."
  ([builder] (in/emit-system-verilog builder))
  ([builder chisel-args firtool-opts]
   (in/emit-system-verilog builder chisel-args firtool-opts)))

(defn spit-system-verilog
  "Emit `builder` to `path` (creating parents). Returns `path`."
  [builder path]
  (io/make-parents path)
  (spit path (system-verilog builder))
  path)

(defn lint!
  "Run `verilator --lint-only` on the SV file at `path` with `--top-module top`.
   Returns the `clojure.java.shell/sh` map `{:exit :out :err}`. Throws
   `ex-info` on non-zero exit unless `:throw? false`. `extra-args` is a seq of
   additional verilator flags (e.g. `[\"-Wno-fatal\"]`)."
  [path top & {:keys [throw? extra-args] :or {throw? true}}]
  (let [args (concat ["verilator" "--lint-only" "--top-module" top]
                     (or extra-args [])
                     [path])
        res  (apply sh/sh args)]
    (when (and throw? (not (zero? (:exit res))))
      (throw (ex-info (str "verilator lint failed for " path)
                      (assoc res :path path :top top))))
    res))
