(ns xt.lang.common-protocol-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer :all]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-protocol/iface-combine :added "4.1"}
(fact "combines interface vectors without duplicates"

  (!.js
    (proto/iface-combine [["connect" "disconnect"]
                          ["disconnect" "exec"]
                          ["connect" "subscribe"]]))
  => ["connect" "disconnect" "exec" "subscribe"]

  (!.lua
    (proto/iface-combine [["connect" "disconnect"]
                          ["disconnect" "exec"]
                          ["connect" "subscribe"]]))
  => ["connect" "disconnect" "exec" "subscribe"]

  (!.py
    (proto/iface-combine [["connect" "disconnect"]
                          ["disconnect" "exec"]
                          ["connect" "subscribe"]]))
  => ["connect" "disconnect" "exec" "subscribe"])

^{:refer xt.lang.common-protocol/proto-group :added "4.1"}
(fact "pairs the combined protocol surface with the implementation map"

  (!.js
    (proto/proto-group [["connect"]
                        ["disconnect" "exec"]]
                       {"connect" "connect-fn"
                        "disconnect" "disconnect-fn"
                        "exec" "exec-fn"}))
  => [["connect" "disconnect" "exec"]
      {"connect" "connect-fn"
       "disconnect" "disconnect-fn"
       "exec" "exec-fn"}]

  (!.lua
    (proto/proto-group [["connect"]
                        ["disconnect" "exec"]]
                       {"connect" "connect-fn"
                        "disconnect" "disconnect-fn"
                        "exec" "exec-fn"}))
  => [["connect" "disconnect" "exec"]
      {"connect" "connect-fn"
       "disconnect" "disconnect-fn"
       "exec" "exec-fn"}]

  (!.py
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

  (!.js
    (proto/proto-spec [[["connect"] {"connect" "connect-fn"}]
                       [["disconnect" "exec"]
                        {"disconnect" "disconnect-fn"
                         "exec" "exec-fn"}]]))
  => {"connect" "connect-fn"
      "disconnect" "disconnect-fn"
      "exec" "exec-fn"}

  (!.js
    (var invalid nil)
    (try
      (proto/proto-spec [[["disconnect" "exec"]
                          {"disconnect" "disconnect-fn"}]])
      (catch err
        (:= invalid (xt/x:ex-message err))))
    invalid)
  => "Invalid Key"

  (!.lua
    (proto/proto-spec [[["connect"] {"connect" "connect-fn"}]
                       [["disconnect" "exec"]
                        {"disconnect" "disconnect-fn"
                         "exec" "exec-fn"}]]))
  => {"connect" "connect-fn"
      "disconnect" "disconnect-fn"
      "exec" "exec-fn"}

  (!.lua
    (var invalid nil)
    (try
      (proto/proto-spec [[["disconnect" "exec"]
                          {"disconnect" "disconnect-fn"}]])
      (catch err
        (:= invalid (xt/x:ex-message err))))
    invalid)
  => "Invalid Key"

  (!.py
    (proto/proto-spec [[["connect"] {"connect" "connect-fn"}]
                       [["disconnect" "exec"]
                        {"disconnect" "disconnect-fn"
                         "exec" "exec-fn"}]]))
  => {"connect" "connect-fn"
      "disconnect" "disconnect-fn"
      "exec" "exec-fn"}

  (!.py
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
