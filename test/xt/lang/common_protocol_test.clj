(ns xt.lang.common-protocol-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.common-protocol :refer :all]))

^{:seedgen/root {:all true, :langs [:js]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-protocol/iface-combine :added "4.1"}
(fact "combines interface vectors without duplicates"
  (!.js
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
  => "NOT VALID.{\"required\":\"exec\",\"actual\":[\"disconnect\"]}")
