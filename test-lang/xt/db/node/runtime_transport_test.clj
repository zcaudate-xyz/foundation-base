^{:seedgen/skip true}
(ns xt.db.node.runtime-transport-test
  (:use code.test)
  (:require [clojure.string :as str]
            [lang.core :as l]
            [xt.db.node.runtime :as runtime]))

^{:refer xt.db.node.runtime/sharedworker-connect-transport :added "4.1.8"}
(fact "emits a transport-only SharedWorker client connection helper"
  (let [source (l/emit-ptr runtime/sharedworker-connect-transport)]
    {:connect (str/includes? source "connect_sharedworker")
     :transport (str/includes? source "set_default_transport")
     :kernel (str/includes? source "kernel_init")})
  => {:connect true
      :transport true
      :kernel false})

^{:refer xt.db.node.runtime/sharedworker-connect-state :added "4.1.8"}
(fact "delegates SharedWorker transport setup to the transport helper"
  (let [source (l/emit-ptr runtime/sharedworker-connect-state)]
    {:transport (str/includes? source "sharedworker_connect_transport")
     :direct (str/includes? source "connect_sharedworker")
     :kernel (str/includes? source "kernel_init")})
  => {:transport true
      :direct false
      :kernel true})
