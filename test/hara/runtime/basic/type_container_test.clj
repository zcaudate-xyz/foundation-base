tahto/runtime/basic/type_container_test.clj:1:(ns tahto.runtime.basic.type-container-test
tahto/runtime/basic/type_container_test.clj:2:  (:require [tahto.runtime.basic.type-container :as container]
tahto/runtime/basic/type_container_test.clj:3:            [tahto.runtime.basic.type-oneshot :as oneshot]
            [lib.docker :as docker])
  (:use code.test))

tahto/runtime/basic/type_container_test.clj:7:^{:refer tahto.runtime.basic.type-container/start-container-process :added "4.0"}
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

tahto/runtime/basic/type_container_test.clj:24:^{:refer tahto.runtime.basic.type-container/start-container :added "4.1"}
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

tahto/runtime/basic/type_container_test.clj:45:^{:refer tahto.runtime.basic.type-container/stop-container :added "4.0"}
(fact "stops the container via docker"
  (with-redefs [docker/stop-container (fn [container]
                                        (:id container))]
    (container/stop-container {:id "test-container"}))
  => "test-container")