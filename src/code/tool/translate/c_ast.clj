(ns code.tool.translate.c-ast
  (:require [std.fs :as fs]
            [std.lib.os]
            [std.make :as make :refer [def.make]]))

(def +root-dir+
  ".build/code.tool.c-ast")

(def.make C_AST
  {:tag       "code.tool.c-ast"
   :build     +root-dir+
   :hooks     {}
   :default   []})

(defn initialise
  []
  (std.lib.os/sh {:args ["clang" "--version"]}))

(defn translate-ast
  ([input-file]
   (translate-ast input-file nil))
  ([input-file output-file]
   (make/build-all C_AST)
   (let [res (std.lib.os/sh {:args ["clang" "-Xclang" "-ast-dump=json" "-fsyntax-only" (str (fs/path input-file))]
                    :wrap false})]
     (if output-file
       (spit output-file res))
     res)))
