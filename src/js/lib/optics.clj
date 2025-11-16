(ns js.lib.optics
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [get set remove chars filter finnd nth]))

(l/script :js
  {:import [["optics-ts/standalone" :as [* Optics]]
            ["optics-ts/standalone" :as [* Optics]]]})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "Optics"
                                   :tag "js"}]
  [compose
   get
   preview
   collect
   set
   modify
   remove
   appendTo
   at
   atKey
   chars
   elems
   eq
   filter
   find
   guard
   indexed
   iso
   lens
   nth
   optional
   partsOf
   pick
   pipe
   prependTo
   prop
   reread
   rewrite
   to
   valueOr
   when
   words])
