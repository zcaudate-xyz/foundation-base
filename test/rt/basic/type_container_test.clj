(ns rt.basic.type-container-test
  (:require [rt.basic.type-container :as container]
            [rt.basic.type-oneshot :as oneshot])
  (:use code.test))

^{:refer rt.basic.type-container/start-container :added "4.1"}
(fact "start-container preserves explicit container exec"
  ^:hidden

  (with-redefs [oneshot/rt-oneshot-setup (fn [& _]
                                           [:erlang
                                            {:container {:image "erlang:27-alpine"}}
                                            ["escript"]])
                container/start-container-process (fn [_lang config _port _rt]
                                                    config)]
    (select-keys
     (container/start-container
      :erlang
      {:container {:image "erlang:27-alpine"
                   :exec ["sh" "-c"]}
       :bootstrap (fn [& _] "echo ok")}
      1234
      {:runtime :basic})
     [:exec :image]))
  => {:exec ["sh" "-c"]
      :image "erlang:27-alpine"})
