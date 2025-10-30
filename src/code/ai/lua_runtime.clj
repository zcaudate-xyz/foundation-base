;; src/code/ai/lua_runtime.clj
(ns code.ai.lua-runtime
  (:require [std.lang :as l]))

(l/script :lua
  {:macro-only true}

  (defn evaluate-string [lua-string]
    (let [(fn-or-err err) (loadstring lua-string)]
      (if fn-or-err
        (let [(success? result) (pcall fn-or-err)]
          (if success?
            {:status :success :result result}
            {:status :error :message result}))
        {:status :error :message err}))))