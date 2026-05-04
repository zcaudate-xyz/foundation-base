(ns hara.runtime.basic.impl-annex.process-erlang-test
  (:require [clojure.string :as str]
            [hara.runtime.basic.impl-annex.process-erlang :refer :all]
            [hara.lang :as l])
  (:use code.test))

^{:refer hara.runtime.basic.impl-annex.process-erlang/default-body-transform :added "4.1"}
(fact "applies return-transform for erlang"
  (default-body-transform '[1 2 3] {})
  => '[1 2 3]

  (default-body-transform '[1 2 3] {:bulk true})
  => 3)

^{:refer hara.runtime.basic.impl-annex.process-erlang/erlang-basic-client-forms :added "4.1"}
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
