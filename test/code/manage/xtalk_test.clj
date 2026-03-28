(ns code.manage.xtalk-test
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [code.manage.xtalk :as xtalk])
  (:use code.test))

(fact "xtalk audit exposes the extended xtalk categories"
  (set/subset? #{:xtalk-ws :xtalk-notify :xtalk-service}
               (set (xtalk/xtalk-categories)))
  => true)

(fact "xtalk audit exposes websocket and service symbols"
  (set/subset? #{'x:ws-connect
                 'x:ws-send
                 'x:ws-close
                 'x:notify-socket
                 'x:client-basic
                 'x:client-ws
                 'x:server-basic
                 'x:server-ws}
               (set (xtalk/xtalk-symbols)))
  => true)

(fact "xtalk facade exposes inventory and scaffold functions"
  [(fn? xtalk/inventory-entries)
   (fn? xtalk/generate-xtalk-ops)
   (fn? xtalk/scaffold-xtalk-grammar-tests)
   (fn? xtalk/separate-runtime-tests)
   (fn? xtalk/scaffold-runtime-template)
   (fn? xtalk/runtime-test-ns)
   (fn? xtalk/support-matrix)
   (fn? xtalk/visualize-support)]
  => [true true true true true true true true])


^{:refer code.manage.xtalk/xtalk-categories :added "4.1"}
(fact "TODO")

^{:refer code.manage.xtalk/xtalk-op-map :added "4.1"}
(fact "TODO")

^{:refer code.manage.xtalk/xtalk-symbols :added "4.1"}
(fact "TODO")

^{:refer code.manage.xtalk/installed-languages :added "4.1"}
(fact "TODO")

^{:refer code.manage.xtalk/audit-languages :added "4.1"}
(fact "TODO")

^{:refer code.manage.xtalk/feature-status :added "4.1"}
(fact "TODO")

^{:refer code.manage.xtalk/support-matrix :added "4.1"}
(fact "TODO")

^{:refer code.manage.xtalk/missing-by-language :added "4.1"}
(fact "TODO")

^{:refer code.manage.xtalk/missing-by-feature :added "4.1"}
(fact "TODO")

^{:refer code.manage.xtalk/visualize-support :added "4.1"}
(fact "TODO")
