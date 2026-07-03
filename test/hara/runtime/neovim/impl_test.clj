(ns hara.runtime.neovim.impl-test
  (:require [hara.lang :as h]
            [hara.runtime.neovim.impl :as impl]
            [std.lib.env :as env])
  (:use code.test))

(fact:global {:skip (not (env/program-exists? "nvim"))})

^{:refer hara.runtime.neovim.impl/neovim-exec :added "4.1"}
(fact "resolves the neovim executable"
  (impl/neovim-exec)
  => string?)

^{:refer hara.runtime.neovim.impl/lua-eval-wrap :added "4.1"}
(fact "wraps lua code for json serialization"
  (let [wrapped (impl/lua-eval-wrap "return 42")]
    [(boolean (re-find #"pcall" wrapped))
     (boolean (re-find #"vim\.json\.encode" wrapped))
     (boolean (re-find #"return 42" wrapped))])
  => [true true true])

^{:refer hara.runtime.neovim.impl/start-neovim :added "4.1"}
(fact "starts and stops a neovim process"
  (let [rt (-> (impl/neovim:create {})
               (impl/start-neovim))
        result [(boolean (:process rt))
                (boolean (:output rt))
                (boolean (:unpacker rt))
                (number? @(:msgid rt))]
        _ (impl/stop-neovim rt)]
    result)
  => [true true true true])

^{:refer hara.runtime.neovim.impl/raw-eval-neovim :added "4.1"}
(fact "evaluates lua code via msgpack-rpc"
  (let [rt (impl/neovim {})]
    (try
      [(impl/raw-eval-neovim rt "return 1 + 2 + 3")
       (number? (impl/raw-eval-neovim rt "return vim.api.nvim_create_buf(false, true)"))]
      (finally
        (impl/stop-neovim rt))))
  => [6 true])

^{:refer hara.runtime.neovim.impl/raw-eval-neovim :added "4.1"}
(fact "propagates lua errors"
  (let [rt (impl/neovim {})]
    (try
      (impl/raw-eval-neovim rt "error('hello error')")
      (catch clojure.lang.ExceptionInfo e
        (:error (ex-data e)))
      (finally
        (impl/stop-neovim rt))))
  => #"hello error")

^{:refer hara.runtime.neovim.impl/invoke-ptr-neovim :added "4.1"}
(fact "invokes a pointer through the neovim runtime"
  (let [rt (impl/neovim {})]
    (try
      (number? (impl/invoke-ptr-neovim
                rt
                (h/ptr :lua {:module (ns-name *ns*)})
                ['(+ 1 2 3)]))
      (finally
        (impl/stop-neovim rt))))
  => true)

^{:refer hara.runtime.neovim.impl/neovim:create :added "4.1"}
(fact "creates a neovim runtime record"
  (let [rt (impl/neovim:create {})]
    [(boolean rt)
     (= :neovim (:tag rt))])
  => [true true])

^{:refer hara.runtime.neovim.impl/neovim :added "4.1"}
(fact "creates and starts a neovim runtime"
  (let [rt (impl/neovim {})]
    (try
      (boolean rt)
      (finally
        (impl/stop-neovim rt))))
  => true)


^{:refer hara.runtime.neovim.impl/value->clj :added "4.1"}
(fact "converts msgpack values to clojure data"
  (let [msgpack (org.msgpack.MessagePack.)
        roundtrip (fn [v]
                    (let [out (java.io.ByteArrayOutputStream.)
                          packer (.createPacker msgpack out)]
                      (.write packer v)
                      (.flush packer)
                      (let [in (java.io.ByteArrayInputStream. (.toByteArray out))
                            unpacker (.createUnpacker msgpack in)]
                        (impl/value->clj (.read unpacker org.msgpack.type.Value)))))]
    [(roundtrip nil)
     (roundtrip true)
     (roundtrip 42)
     (roundtrip 3.14)
     (roundtrip "hello")
     (roundtrip [1 2 3])
     (roundtrip {"a" 1 "b" 2})])
  => [nil true 42 3.14 "hello" [1 2 3] {"a" 1, "b" 2}])

^{:refer hara.runtime.neovim.impl/pack-request :added "4.1"}
(fact "packs a msgpack-rpc request"
  (let [msgpack (org.msgpack.MessagePack.)
        params (doto (java.util.ArrayList.) (.add 42))
        bytes (impl/pack-request 1 "test" params)
        in (java.io.ByteArrayInputStream. bytes)
        unpacker (.createUnpacker msgpack in)]
    (impl/value->clj (.read unpacker org.msgpack.type.Value)))
  => [0 1 "test" [42]])

^{:refer hara.runtime.neovim.impl/stop-neovim :added "4.1"}
(fact "returns the runtime when stopping"
  (let [rt (impl/neovim:create {})]
    (identical? (impl/stop-neovim rt) rt))
  => true)

^{:refer hara.runtime.neovim.impl/next-msgid :added "4.1"}
(fact "increments the message id counter"
  (let [rt (assoc (impl/neovim:create {}) :msgid (atom 0))]
    [(impl/next-msgid rt)
     (impl/next-msgid rt)
     @(:msgid rt)])
  => [1 2 2])

^{:refer hara.runtime.neovim.impl/send-request :added "4.1"}
(fact "sends a msgpack-rpc request and returns the raw result"
  (let [rt (impl/neovim {})
        params (doto (java.util.ArrayList.)
                 (.add (impl/lua-eval-wrap "return 1 + 2 + 3"))
                 (.add (java.util.ArrayList.)))
        result (try
                 (impl/send-request rt "nvim_exec_lua" params)
                 (finally
                   (impl/stop-neovim rt)))]
    [(string? result)
     (boolean (re-find #"\"value\":6" result))])
  => [true true])

^{:refer hara.runtime.neovim.impl/rt-neovim-string :added "4.1"}
(fact "produces the runtime string representation"
  (impl/rt-neovim-string {:id :foo})
  => "#rt.neovim[:foo]")

^{:refer hara.runtime.neovim.impl/neovim-shared:create :added "4.1"}
(fact "creates a shared neovim runtime client"
  (let [shared (impl/neovim-shared:create {})]
    [(boolean shared)
     (= :hara/rt.neovim (-> shared :client :type))
     (:temp shared)])
  => [true true true])