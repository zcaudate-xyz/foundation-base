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
