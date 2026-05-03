(ns xtbench.dart.lang.common-protocol-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer :all]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-protocol/iface-combine :added "4.1"}
(fact "combines interface vectors without duplicates"

  (!.dt
    (proto/iface-combine [["connect" "disconnect"]
                          ["disconnect" "exec"]
                          ["connect" "subscribe"]]))
  => ["connect" "disconnect" "exec" "subscribe"])

^{:refer xt.lang.common-protocol/proto-group :added "4.1"}
(fact "pairs the combined protocol surface with the implementation map"

  (!.dt
    (proto/proto-group [["connect"]
                        ["disconnect" "exec"]]
                       {"connect" "connect-fn"
                        "disconnect" "disconnect-fn"
                        "exec" "exec-fn"}))
  => [["connect" "disconnect" "exec"]
      {"connect" "connect-fn"
       "disconnect" "disconnect-fn"
       "exec" "exec-fn"}])

^{:refer xt.lang.common-protocol/proto-spec :added "4.1"}
(fact "merges protocol groups and rejects missing required methods"

  (!.dt
    (proto/proto-spec [[["connect"] {"connect" "connect-fn"}]
                       [["disconnect" "exec"]
                        {"disconnect" "disconnect-fn"
                         "exec" "exec-fn"}]]))
  => {"connect" "connect-fn"
      "disconnect" "disconnect-fn"
      "exec" "exec-fn"}

  (!.dt
    (var invalid nil)
    (try
      (proto/proto-spec [[["disconnect" "exec"]
                          {"disconnect" "disconnect-fn"}]])
      (catch err
        (:= invalid (xt/x:ex-message err))))
    invalid)
  => "Invalid Key")

(comment
  (s/snapto '[xt.lang.common-protocol])
  
  (s/seedgen-langadd '[xt.lang.common-protocol] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.lang.common-protocol] {:lang [:lua :python] :write true}))
