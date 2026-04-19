(ns rt.basic.impl-annex.process-erlang-test
  (:require [clojure.string :as str]
            [rt.basic.impl-annex.process-erlang :refer :all]
            [std.lang :as l])
  (:use code.test))

^{:refer rt.basic.impl-annex.process-erlang/default-body-transform :added "4.1"}
(fact "applies return-transform for erlang"
  (default-body-transform '[1 2 3] {})
  => '[1 2 3]

  (default-body-transform '[1 2 3] {:bulk true})
  => 3)

^{:refer rt.basic.impl-annex.process-erlang/erlang-basic-client-forms :added "4.1"}
(fact "builds erlang basic client forms from the erlang emitter"
  [(-> (erlang-basic-client-forms "127.0.0.1" 4567)
       count)
   (-> (default-basic-client 4567 {:host "127.0.0.1"})
       (str/includes? "main(_) ->"))
   (-> (default-basic-client 4567 {:host "127.0.0.1"})
       (str/includes? "loop(Sock) ->"))
   (-> (default-basic-client 4567 {:host "127.0.0.1"})
       (str/includes? "HOST_PLACEHOLDER"))
   (-> (l/emit-as :erlang
                  (erlang-basic-client-forms "127.0.0.1" 4567))
       (str/includes? "eval_code(S) ->"))]
  => [4 true true false true])
