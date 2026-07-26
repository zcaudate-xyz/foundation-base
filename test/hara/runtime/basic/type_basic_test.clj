tahto/runtime/basic/type_basic_test.clj:1:(ns tahto.runtime.basic.type-basic-test
tahto/runtime/basic/type_basic_test.clj:2:  (:require [tahto.runtime.basic.impl.process-js :as js]
tahto/runtime/basic/type_basic_test.clj:3:            [tahto.runtime.basic.impl.process-lua :as lua]
tahto/runtime/basic/type_basic_test.clj:4:            [tahto.runtime.basic.server-basic :as server]
tahto/runtime/basic/type_basic_test.clj:5:            [tahto.runtime.basic.type-basic :refer :all]
tahto/runtime/basic/type_basic_test.clj:6:            [tahto.runtime.basic.type-bench :as bench]
tahto/runtime/basic/type_basic_test.clj:7:            [tahto.runtime.basic.type-container :as container]
            [std.lib.component :as component])
  (:use code.test))

tahto/runtime/basic/type_basic_test.clj:11:^{:refer tahto.runtime.basic.type-basic/start-basic :added "4.0"}
(fact "starts the basic rt"

  (with-redefs [server/start-server (fn [& _] {:port 1234})
                server/wait-ready (fn [& _] true)
                container/start-container (fn [& _] {})
                bench/start-bench (fn [& _] {})
tahto/runtime/basic/type_basic_test.clj:18:                tahto.runtime.basic.type-oneshot/rt-oneshot-setup (fn [& _] [nil {} nil])
tahto/runtime/basic/type_basic_test.clj:19:                tahto.runtime.basic.type-common/get-options (fn [& _] {})]
    (start-basic (rt-basic:create {:lang :test :id "test-start" :program nil :make nil :exec nil})))
  => map?)

tahto/runtime/basic/type_basic_test.clj:23:^{:refer tahto.runtime.basic.type-basic/default-container-backup? :added "4.1"}
(fact "container backup defaults to true unless explicitly disabled"

  [(default-container-backup? nil)
   (default-container-backup? "false")
   (default-container-backup? "0")]
  => [true false false])

tahto/runtime/basic/type_basic_test.clj:31:^{:refer tahto.runtime.basic.type-basic/local-exec-available? :added "4.1"}
(fact "local-exec-available? checks the first command token"

tahto/runtime/basic/type_basic_test.clj:34:  (with-redefs [tahto.runtime.basic.type-common/program-exists? (fn [s] (= s "php"))]
    [(local-exec-available? ["php" "-r"])
     (local-exec-available? "php")
     (local-exec-available? ["python3" "-c"])
     (local-exec-available? nil)])
  => [true true false false])

tahto/runtime/basic/type_basic_test.clj:41:^{:refer tahto.runtime.basic.type-basic/start-basic :added "4.1"
  :id test-start-basic-container-fallback}
(fact "start-basic falls back to container when local exec is unavailable and backup is enabled"

  (with-redefs [server/start-server (fn [& _] {:port 1234})
                server/wait-ready (fn [& _] true)
                container/start-container (fn [& args] {:container-args args})
                bench/start-bench (fn [& _] (throw (ex-info "bench should not start" {})))
tahto/runtime/basic/type_basic_test.clj:49:                tahto.runtime.basic.type-common/get-options (fn [& _] {})
tahto/runtime/basic/type_basic_test.clj:50:                tahto.runtime.basic.type-oneshot/rt-oneshot-setup (fn [& _]
                                                         [:php
                                                          {:container {:image "ghcr.io/zcaudate-xyz/foundation-base/rt-basic-php:latest"}
                                                           :container-backup true}
                                                          ["php" "-r"]])
tahto/runtime/basic/type_basic_test.clj:55:                tahto.runtime.basic.type-common/program-exists? (fn [_] false)]
    (-> (start-basic {:lang :php :id "test-start" :program nil :make nil :exec nil :runtime :basic})
        :container
         :container-args
         count))
  => 4)

tahto/runtime/basic/type_basic_test.clj:62:^{:refer tahto.runtime.basic.type-basic/start-basic :added "4.1"
  :id test-start-basic-explicit-container-exec}
(fact "start-basic preserves explicit container exec over runtime exec"

  (with-redefs [server/start-server (fn [& _] {:port 1234})
                server/wait-ready (fn [& _] true)
                container/start-container (fn [& [_lang config _port _rt]]
                                            config)
                bench/start-bench (fn [& _] (throw (ex-info "bench should not start" {})))
tahto/runtime/basic/type_basic_test.clj:71:                tahto.runtime.basic.type-common/get-options (fn [& _] {})
tahto/runtime/basic/type_basic_test.clj:72:                tahto.runtime.basic.type-oneshot/rt-oneshot-setup (fn [& _]
                                                         [:erlang
                                                          {:container {:image "erlang:27-alpine"}}
                                                          ["escript"]])]
    (select-keys
     (:container
      (start-basic {:lang :erlang
                    :id "test-explicit-container-exec"
                    :runtime :basic
                    :container {:image "erlang:27-alpine"
                                :exec ["sh" "-c"]}}))
     [:exec :image]))
  => {:exec ["sh" "-c"]
      :image "erlang:27-alpine"})

tahto/runtime/basic/type_basic_test.clj:87:^{:refer tahto.runtime.basic.type-basic/start-basic :added "4.1"
  :id test-start-basic-process-bench-defaults}
(fact "start-basic merges process bench defaults into the bench runtime"

  (let [captured (atom nil)]
    (with-redefs [server/start-server (fn [& _] {:port 1234})
                  server/wait-ready (fn [& _] true)
                  container/start-container (fn [& _] (throw (ex-info "container should not start" {})))
                  bench/start-bench (fn [_lang config _port _rt]
                                      (reset! captured config)
                                      {})
tahto/runtime/basic/type_basic_test.clj:98:                  tahto.runtime.basic.type-common/get-options (fn [& _] {})
tahto/runtime/basic/type_basic_test.clj:99:                  tahto.runtime.basic.type-oneshot/rt-oneshot-setup (fn [& _]
                                                           [:js
                                                            {:bench {:shell {:env {"NODE_PATH" "/tmp/node_modules"}}}}
                                                            ["node" "-e"]])]
      (start-basic {:lang :js
                    :id "test-bench-config"
                    :runtime :basic
                    :bench {:host "127.0.0.1"}})
      (select-keys @captured [:host :shell])))
  => {:host "127.0.0.1"
      :shell {:env {"NODE_PATH" "/tmp/node_modules"}}})

tahto/runtime/basic/type_basic_test.clj:111:^{:refer tahto.runtime.basic.type-basic/stop-basic :added "4.0"}
(fact "stops the basic rt"

  (with-redefs [server/start-server (fn [& _] {:port 1234})
                server/wait-ready (fn [& _] true)
                container/start-container (fn [& _] {})
                bench/start-bench (fn [& _] {})
tahto/runtime/basic/type_basic_test.clj:118:                tahto.runtime.basic.type-oneshot/rt-oneshot-setup (fn [& _] [nil {} nil])
tahto/runtime/basic/type_basic_test.clj:119:                tahto.runtime.basic.type-common/get-options (fn [& _] {})]
    (stop-basic
     (start-basic
      (rt-basic:create {:lang :test :id "test-start" :program nil :make nil :exec nil}))))
  => map?)

tahto/runtime/basic/type_basic_test.clj:125:^{:refer tahto.runtime.basic.type-basic/raw-eval-basic :added "4.0"}
(fact "raw eval for basic rt"

  (with-redefs [server/get-server (fn [& _] {:raw-eval (fn [_ _ _] :ok)})]
    (raw-eval-basic {:id "test-eval" :lang :test} "1 + 1"))
  => :ok)

tahto/runtime/basic/type_basic_test.clj:132:^{:refer tahto.runtime.basic.type-basic/invoke-ptr-basic :added "4.0"}
(fact "invoke for basic rt")

tahto/runtime/basic/type_basic_test.clj:135:^{:refer tahto.runtime.basic.type-basic/rt-basic-string :added "4.0"}
(fact "string for basic rt"

  (with-redefs [server/get-server (fn [& _] {:port 1234 :type :server :count (atom 1)})]
    (rt-basic-string {:id "test-string" :lang :test}))
  => string?)

tahto/runtime/basic/type_basic_test.clj:142:^{:refer tahto.runtime.basic.type-basic/rt-basic-port :added "4.0"}
(fact "return the basic port of the rt"

  (with-redefs [server/get-server (fn [& _] {:port 1234})]
    (rt-basic-port {:id "test-port" :lang :test}))
  => 1234)

tahto/runtime/basic/type_basic_test.clj:149:^{:refer tahto.runtime.basic.type-basic/rt-basic:create :added "4.0"}
(fact "creates a basic rt"

tahto/runtime/basic/type_basic_test.clj:152:  (with-redefs [tahto.runtime.basic.type-common/get-options (fn [& _] {})]
    (rt-basic:create {:lang :test}))
  => map?)

tahto/runtime/basic/type_basic_test.clj:156:^{:refer tahto.runtime.basic.type-basic/rt-basic :added "4.0"}
(fact "creates and starts a basic rt"

  (with-redefs [server/start-server (fn [& _] {:port 1234})
                server/wait-ready (fn [& _] true)
                container/start-container (fn [& _] {})
                bench/start-bench (fn [& _] {})
tahto/runtime/basic/type_basic_test.clj:163:                tahto.runtime.basic.type-oneshot/rt-oneshot-setup (fn [& _] [nil {} nil])
                server/stop-server (fn [& _] {})]
    (def +rt+ (rt-basic {:lang :js :id "test-start" :program nil :make nil :exec nil}))

    (component/stop +rt+))
  => map?)
