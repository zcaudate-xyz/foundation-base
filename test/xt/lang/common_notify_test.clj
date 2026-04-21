(ns xt.lang.common-notify-test
  (:require [net.http :as http]
            [std.json :as json]
            [std.lang :as l]
            [std.lang.interface.type-notify :as interface]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]]})

(l/script- :r
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-notify/notify-defaults :added "4.0"}
(fact "creates the ceremony for webpages"

  (notify/notify-defaults {:type :webpage})
  => '{:host window.location.hostname,
       :port window.location.port,
       :scheme (:? (== window.location.protocol "https:") "https" "http"),
       :type :webpage})

^{:refer xt.lang.common-notify/notify-ceremony :added "4.0"}
(fact "creates the ceremony in order to get the port and method type"

  (notify/notify-ceremony (assoc (l/rt :js)
                                 :type :basic))
  => [(:id (l/rt :js))
      (:socket-port (l/default-notify))
      :js :socket
      "127.0.0.1"
      {}])

^{:refer xt.lang.common-notify/notify-ceremony-rt :added "4.0"}
(fact "gets the rt for the current ceremony")

^{:refer xt.lang.common-notify/wait-on-call :added "4.0"}
(fact "generic wait-on-helper for oneshots"

  (str (notify/wait-on-call
       (fn []
         (http/post (str "http://127.0.0.1:" (:http-port (l/default-notify)))
                    {:body (std.json/write
                            {:id notify/*override-id*
                             :type "raw"
                             :value "hello"})}))))
  => "\nhello")

^{:refer xt.lang.common-notify/wait-on-fn :added "4.0"}
(fact "wait-on helper for in runtime calls")

^{:refer xt.lang.common-notify/wait-on :added "4.0"}
(fact "sets up a code context and waits for oneshot notification"

  (notify/wait-on :js
    (repl/notify 1))
  => 1

  (notify/wait-on :lua
    (repl/notify 1))
  => 1


  (notify/wait-on :python
    (repl/notify 1))
  => 1

  (notify/wait-on :r
    (repl/notify 1))
  => 1)

^{:refer xt.lang.common-notify/captured :added "4.0"
  :setup [(notify/captured:clear-all)]}
(fact "gets captured results"

  (do (notify/wait-on :js
        (repl/capture {:from "js"})
        (repl/notify 1))
      (notify/captured :js))
  => [{"from" "js"}]

  (do (notify/wait-on :lua
        (repl/capture {:from "lua"})
        (repl/notify 1))
      (notify/captured :lua))
  => [{"from" "lua"}]

  (do (notify/wait-on :python
        (repl/capture {:from "python"})
        (repl/notify 1))
      (notify/captured :python))
  => [{"from" "python"}]

  (do (notify/wait-on :r
        (repl/capture {:from "r"})
        (repl/notify 1))
      (notify/captured :r))
  => [{"from" "r"}])

^{:refer xt.lang.common-notify/captured:count :added "4.0"}
(fact "gets the captured count for rt")

^{:refer xt.lang.common-notify/captured:clear :added "4.0"}
(fact "clears captured items for rt")

^{:refer xt.lang.common-notify/captured:clear-all :added "4.0"}
(fact "clears all captured items")



^{:refer xt.lang.common-notify/capture-key-match? :added "4.1"}
(fact "TODO")
