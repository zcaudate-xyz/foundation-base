(ns xt.db.runtime.driver
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.protocol.impl.connection-sql :as sql]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(def +registry+
  {:js       {"sqlite"   'js.lib.driver-sqlite
              "postgres" 'js.lib.driver-postgres}
   :python   {"sqlite"   'python.lib.driver-sqlite
              "postgres" 'python.lib.driver-postgres}
   :dart     {"sqlite"   'dart.lib.driver-sqlite}})


(defmacro.xt get-driver
  [type]
  (let [{:keys [lang snapshot emit]} (std.lib/do:prn (l/macro-opts))
        ns-sym (get-in +registry+ [lang type])
        _ (if ns-sym
            (require ns-sym)
            (throw (ex-info "Not found" {:input [lang type]
                                         :options +registry+})))]
    (list (symbol (name ns-sym) "driver"))))
