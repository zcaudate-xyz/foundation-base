(ns lua.core
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str])
  (:refer-clojure :exclude [abs assert byte format load max min
                            print remove sort time type slurp spit]))

(l/script :lua
  {:macro-only true
   :bundle  {:json     {:default [["cjson"  :as cjson]]}
             :ffi      {:default [["ffi"    :as ffi]]}
             :mustache {:default [["lustache" :as lustache]]}}
   :export [MODULE]})

(def$.lua cjson cjson)

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base ["cjson"]
                                   :tag "lua"}]
  [[json-encode encode]
   [json-decode decode]])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base ["table"]
                                   :tag "lua"}]
  [insert
   remove
   sort])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base []
                                   :tag "lua"}]
  [assert
   collectgarbage
   dofile
   error
   _G
   getfenv
   getmetatable
   ipairs
   load
   loadfile
   loadstring
   
   pairs
   pcall
   print
   rawequal
   rawget
   rawset
   select
   setfenv
   setmetatable
   tonumber
   tostring
   type
   unpack
   _VERSION
   xpcall])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "coroutine"
                                   :tag "lua"}]
  [[cr-wrap   wrap]
   yield
   [coroutine create]
   [cr-resume resume]
   [cr-status status]
   [cr-running running]])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "os"
                                   :tag "lua"}]
  [date
   time
   clock
   getenv
   difftime
   tmpname
   setlocale
   
   [os-exit exit]
   [os-rename rename]
   [os-remove remove]
   [os-execute execute]])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "io"
                                   :tag "lua"}]
  [[io-close close]
   [io-flush flush]
   [io-input input]
   [io-lines lines]
   [io-open open]
   [io-output output]
   [io-popen popen]
   [io-read read]
   stderr
   stdin
   stdout
   tmpfile
   [io-type type]
   [io-write write]])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "string"
                                   :tag "lua"}]
  [[substring sub]
   [refind find]
   format
   rep
   byte
   gsub
   lower
   upper
   match
   gmatch])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "math"
                                   :tag "lua"}]
  [abs
   acos
   asin
   atan
   atan2
   ceil
   cos
   cosh
   deg
   exp
   floor
   fmod
   frexp
   huge
   ldexp
   log
   log10
   max
   min
   #_modf
   pi
   pow
   rad
   random
   randomseed
   sin
   sinh
   sqrt
   tan
   tanh])

(def$.lua ffi ffi)

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ffi"
                                   :prefix "ffi-"
                                   :tag "lua"}]
  [cdef
   load
   new
   typeof
   cast
   metatype
   gc
   sizeof
   alignof
   offsetof
   istype
   errno
   string
   copy
   fill
   abi
   os
   arch])

(defmacro.lua render
  "renders a template"
  {:added "4.0"}
  [template content]
  (h/$ (. lustache (render ~template ~content))))

(def$.lua ffi:new ffi.new)

(defmacro.lua ffi:C
  "allow ffi calls"
  {:added "4.0"}
  [func & args]
  (h/$ ((. ffi C ~(symbol (name func))) ~@args)))

(defn.lua reduce-iter
  "reduces iterate"
  {:added "4.0"}
  [iter f init]
  (var '[acc v] '[init nil])
  (for [v :in iter]
    (var '[res terminate] (f acc v))
    (:= acc res)
    (if terminate (return acc)))
  (return acc))

(defn.lua slurp
  "slurps the output from path"
  {:added "4.0"}
  [path]
  (var lines (-/io-lines path))
  (var body  (-/reduce-iter lines
                             (fn [body line]
                               (return (cat body "\n" line)))
                             (lines)))
  (return body))

(defn.lua spit
  "spits the output from path"
  {:added "4.0"}
  [path body]
  (var file (-/io-open path "w"))
  (-/io-output file)
  (-/io-write body)
  (-/io-close file)
  (return true))

(def.lua MODULE (!:module))
