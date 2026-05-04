(ns hara.model.spec-lua-try-test
  (:require [hara.lang :as l]
            [hara.model.spec-lua :as spec-lua])
  (:use code.test))

^{:refer hara.model.spec-lua/+grammar+ :added "4.1"}
(fact "lua throw emits native lua errors for strings and structured exceptions"
  [(l/emit-as :lua '[(throw "boom")])
   (l/emit-as :lua '[(throw (x:ex "boom" {:a 1}))])]
  => ["error('boom',0)"
      "error({['__type__']='xt.exception',message='boom',data={a=1}},0)"])

^{:refer hara.model.spec-lua.rewrite/lua-rewrite-stage :added "4.1"}
(fact "lua try/catch lowers to a pcall wrapper for xt exceptions"
  (let [out (l/emit-as :lua
                       '[(try
                           (throw (x:ex "boom" {:a 1}))
                           (catch e
                             (x:print (x:ex-message e))
                             (x:print (x:ex-data e))))])]
    [(boolean (re-find #"pcall\(function \(\)" out))
     (boolean (re-find #"error\(\{\['__type__'\]='xt\.exception'.*,0\)" out))
     (boolean (re-find #"'xt\.exception' == .*?\['__type__'\]" out))
     (boolean (re-find #"local e =" out))
     (boolean (re-find #"e\['message'\]" out))
     (boolean (re-find #"e\['data'\]" out))
     (boolean (re-find #"print" out))])
  => [true true true true true true true])

^{:refer hara.model.spec-lua.rewrite/lua-rewrite-stage :added "4.1"}
(fact "lua sync catch preserves plain thrown payloads through pcall"
  (let [out (l/emit-as :lua
                       '[(try
                           (throw "boom")
                           (catch e
                              (x:print (x:ex-data e))))])]
    [(boolean (re-find #"local e = lua_try_body_value__" out))
     (boolean (re-find #"error\('boom',0\)" out))
     (boolean (re-find #"error\(lua_try_value__" out))])
  => [true true true])

^{:refer hara.model.spec-lua.rewrite/lua-rewrite-stage :added "4.1"}
(fact "lua try/finally preserves outer return flow after the pcall rewrite"
  (let [out (l/emit-as :lua
                       '[(defn demo []
                           (try
                            (return 1)
                            (finally
                              (x:print "done"))))])]
    [(boolean (re-find #"pcall\(function \(\)" out))
      (boolean (re-find #"print\('done'\)" out))
      (boolean (re-find #"return lua_try_value__" out))
      (boolean (re-find #"error\(lua_try_value__" out))])
  => [true true true true])
