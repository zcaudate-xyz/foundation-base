(ns hara.runtime.basic.type-container-test
  (:require [hara.runtime.basic.type-container :as container]
            [hara.runtime.basic.type-oneshot :as oneshot]
            [lib.docker :as docker])
  (:use code.test))

^{:refer hara.runtime.basic.type-container/start-container-process :added "4.0"}
(fact "builds container config and starts the docker runtime"
  (with-redefs [lib.docker/start-runtime (fn [_rt container]
                                           container)]
    (select-keys
     (container/start-container-process
      :erlang
      {:image "erlang:27-alpine"
       :exec ["escript"]
       :bootstrap (fn [_port _opts] "echo ok")}
      1234
      {})
     [:image :cmd :remove]))
  => {:image "erlang:27-alpine"
      :cmd ["escript" "echo ok"]
      :remove true})

^{:refer hara.runtime.basic.type-container/start-container :added "4.1"}
(fact "start-container preserves explicit container exec"

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

^{:refer hara.runtime.basic.type-container/stop-container :added "4.0"}
(fact "stops the container via docker"
  (with-redefs [docker/stop-container (fn [container]
                                        (:id container))]
    (container/stop-container {:id "test-container"}))
  => "test-container")