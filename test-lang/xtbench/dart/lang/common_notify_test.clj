(ns xtbench.dart.lang.common-notify-test
  (:require [net.http :as http]
            [std.json :as json]
            [hara.lang :as l]
            [hara.lang.base.type-notify :as interface]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-notify/notify-ceremony :added "4.0"}
(fact "creates the ceremony in order to get the port and method type"

  (notify/notify-ceremony (assoc (l/rt :dart)
                                 :type :basic))
  => [(:id (l/rt :dart))
      (:socket-port (l/default-notify))
      :dart :socket
      "127.0.0.1"
      {}])

^{:refer xt.lang.common-notify/wait-on :added "4.0"}
(fact "sets up a code context and waits for oneshot notification"

  (notify/wait-on :dart
    (repl/notify 1))
  => 1)

(comment
  (s/seedgen-benchadd 'xt.lang.common-notify {:lang [:r] :write true})
  (s/seedgen-benchadd 'xt.lang.common-notify {:lang [:dart] :write true})
  (s/seedgen-langadd 'xt.lang.common-notify {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-notify {:lang [:lua :python] :write true}))
