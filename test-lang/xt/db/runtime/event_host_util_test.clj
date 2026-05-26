(ns xt.db.runtime.event-host-util-test
  (:require [xt.db.runtime.event-host-util :as event-host-util])
  (:use code.test))

^{:refer xt.db.runtime.event-host-util/request-event :added "4.1.4"}
(fact "returns canonical xt.db event names"
  [(event-host-util/request-event {"db/sync" {"Entry" []}})
   (event-host-util/request-event {"db/remove" {"Entry" ["id-1"]}})
   (event-host-util/request-event {"db/query" {"Entry" []}})]
  => ["db/sync" "db/remove" nil])
