(ns rt.basic.type-basic-test
  (:require [rt.basic.impl.process-js :as js]
            [rt.basic.impl.process-lua :as lua]
            [rt.basic.server-basic :as server]
            [rt.basic.type-basic :refer :all]
            [rt.basic.type-bench :as bench]
            [rt.basic.type-container :as container]
            [std.lib.component :as component])
  (:use code.test))

^{:refer rt.basic.type-basic/start-basic :added "4.0"}
(fact "starts the basic rt"

  (with-redefs [server/start-server (fn [& _] {:port 1234})
                server/wait-ready (fn [& _] true)
                container/start-container (fn [& _] {})
                bench/start-bench (fn [& _] {})
                rt.basic.type-oneshot/rt-oneshot-setup (fn [& _] [nil {} nil])
                rt.basic.type-common/get-options (fn [& _] {})]
    (start-basic (rt-basic:create {:lang :test :id "test-start" :program nil :make nil :exec nil})))
  => map?)

^{:refer rt.basic.type-basic/default-container-backup? :added "4.1"}
(fact "container backup defaults to true unless explicitly disabled"

  [(default-container-backup? nil)
   (default-container-backup? "false")
   (default-container-backup? "0")]
  => [true false false])

^{:refer rt.basic.type-basic/local-exec-available? :added "4.1"}
(fact "local-exec-available? checks the first command token"

  (with-redefs [rt.basic.type-common/program-exists? (fn [s] (= s "php"))]
    [(local-exec-available? ["php" "-r"])
     (local-exec-available? "php")
     (local-exec-available? ["python3" "-c"])
     (local-exec-available? nil)])
  => [true true false false])

^{:refer rt.basic.type-basic/start-basic :added "4.1"}
(fact "start-basic falls back to container when local exec is unavailable and backup is enabled"

  (with-redefs [server/start-server (fn [& _] {:port 1234})
                server/wait-ready (fn [& _] true)
                container/start-container (fn [& args] {:container-args args})
                bench/start-bench (fn [& _] (throw (ex-info "bench should not start" {})))
                rt.basic.type-common/get-options (fn [& _] {})
                rt.basic.type-oneshot/rt-oneshot-setup (fn [& _]
                                                         [:php
                                                          {:container {:image "foundation-base/rt-basic-php:latest"}
                                                           :container-backup true}
                                                          ["php" "-r"]])
                rt.basic.type-common/program-exists? (fn [_] false)]
    (-> (start-basic {:lang :php :id "test-start" :program nil :make nil :exec nil :runtime :basic})
        :container
         :container-args
         count))
  => 4)

^{:refer rt.basic.type-basic/start-basic :added "4.1"}
(fact "start-basic preserves explicit container exec over runtime exec"

  (with-redefs [server/start-server (fn [& _] {:port 1234})
                server/wait-ready (fn [& _] true)
                container/start-container (fn [& [_lang config _port _rt]]
                                            config)
                bench/start-bench (fn [& _] (throw (ex-info "bench should not start" {})))
                rt.basic.type-common/get-options (fn [& _] {})
                rt.basic.type-oneshot/rt-oneshot-setup (fn [& _]
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

^{:refer rt.basic.type-basic/start-basic :added "4.1"}
(fact "start-basic merges process bench defaults into the bench runtime"

  (let [captured (atom nil)]
    (with-redefs [server/start-server (fn [& _] {:port 1234})
                  server/wait-ready (fn [& _] true)
                  container/start-container (fn [& _] (throw (ex-info "container should not start" {})))
                  bench/start-bench (fn [_lang config _port _rt]
                                      (reset! captured config)
                                      {})
                  rt.basic.type-common/get-options (fn [& _] {})
                  rt.basic.type-oneshot/rt-oneshot-setup (fn [& _]
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

^{:refer rt.basic.type-basic/stop-basic :added "4.0"}
(fact "stops the basic rt"

  (with-redefs [server/start-server (fn [& _] {:port 1234})
                server/wait-ready (fn [& _] true)
                container/start-container (fn [& _] {})
                bench/start-bench (fn [& _] {})
                rt.basic.type-oneshot/rt-oneshot-setup (fn [& _] [nil {} nil])
                rt.basic.type-common/get-options (fn [& _] {})]
    (stop-basic
     (start-basic
      (rt-basic:create {:lang :test :id "test-start" :program nil :make nil :exec nil}))))
  => map?)

^{:refer rt.basic.type-basic/raw-eval-basic :added "4.0"}
(fact "raw eval for basic rt"

  (with-redefs [server/get-server (fn [& _] {:raw-eval (fn [_ _ _] :ok)})]
    (raw-eval-basic {:id "test-eval" :lang :test} "1 + 1"))
  => :ok)

^{:refer rt.basic.type-basic/invoke-ptr-basic :added "4.0"}
(fact "invoke for basic rt")

^{:refer rt.basic.type-basic/rt-basic-string :added "4.0"}
(fact "string for basic rt"

  (with-redefs [server/get-server (fn [& _] {:port 1234 :type :server :count (atom 1)})]
    (rt-basic-string {:id "test-string" :lang :test}))
  => string?)

^{:refer rt.basic.type-basic/rt-basic-port :added "4.0"}
(fact "return the basic port of the rt"

  (with-redefs [server/get-server (fn [& _] {:port 1234})]
    (rt-basic-port {:id "test-port" :lang :test}))
  => 1234)

^{:refer rt.basic.type-basic/rt-basic:create :added "4.0"}
(fact "creates a basic rt"

  (with-redefs [rt.basic.type-common/get-options (fn [& _] {})]
    (rt-basic:create {:lang :test}))
  => map?)

^{:refer rt.basic.type-basic/rt-basic :added "4.0"}
(fact "creates and starts a basic rt"

  (with-redefs [server/start-server (fn [& _] {:port 1234})
                server/wait-ready (fn [& _] true)
                container/start-container (fn [& _] {})
                bench/start-bench (fn [& _] {})
                rt.basic.type-oneshot/rt-oneshot-setup (fn [& _] [nil {} nil])
                server/stop-server (fn [& _] {})]
    (def +rt+ (rt-basic {:lang :js :id "test-start" :program nil :make nil :exec nil}))

    (component/stop +rt+))
  => map?)
