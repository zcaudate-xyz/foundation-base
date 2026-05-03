(ns xtbench.dart.lang.spec-link-test
  (:use code.test)
  (:require [clojure.set :as set]
            [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-link :as spec-link]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-link/x:socket-connect :added "4.1"}
(fact "connects sockets and forwards the connection to callbacks"

  (notify/wait-on :dart
    (var connect-fn
         (fn [host port opts cb]
           (return
            (spec-link/x:socket-connect host port opts cb))))
    (connect-fn  "127.0.0.1"
                 (@! (:socket-port (l/default-notify)))
                 {}
                 (fn [err conn]
                   (do (repl/notify "OK")
                       (spec-link/x:socket-close conn)))))
  => "OK")

^{:refer xt.lang.spec-link/x:notify-http :added "4.1"}
(fact "posts encoded values through fetch"

  (notify/wait-on-call
   (fn [] (!.dt
            (var notify-fn
                 (fn [host port value id key opts]
                   (return
                    (spec-link/x:notify-http host port value id key opts))))
            (notify-fn "127.0.0.1" (@! (:http-port (l/default-notify)))
                       "hello"
                       (@! notify/*override-id*)
                       nil
                       {}))))
  => "hello")

(comment
  
  (s/seedgen-benchadd '[xt.lang.spec-link] {:lang [:python] :write true})
  (s/seedgen-benchadd '[xt.lang.spec-link] {:lang [:dart] :write true})
  (s/seedgen-benchadd '[xt.lang.spec-link] {:lang [:r] :write true})
  
  (s/seedgen-langadd 'xt.lang.spec-link {:lang [:lua] :write true})

  (s/seedgen-langadd 'xt.lang.spec-link {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.spec-link {:lang [:lua :python] :write true}))
