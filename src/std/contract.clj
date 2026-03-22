(ns std.contract
  (:require [malli.core :as mc]
            [std.contract.binding :as binding]
            [std.contract.sketch :as sketch]
            [std.contract.type :as type]
            [std.lib.foundation])
  (:refer-clojure :exclude [fn remove]))

(std.lib.foundation/intern-in  [maybe sketch/as:maybe]
              [opt   sketch/as:optional]
              [fn    sketch/func]
              sketch/lax
              sketch/opened
              sketch/tighten
              sketch/closed
              sketch/norm
              sketch/remove
              [as:sketch sketch/from-schema]
              [as:schema sketch/to-schema]

              type/defcase
              type/defmultispec
              type/defspec
              type/spec?
              type/common-spec
              type/multi-spec
              type/valid?

              binding/defcontract

              mc/schema
              mc/schema?)

