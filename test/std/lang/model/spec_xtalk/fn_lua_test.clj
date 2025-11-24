(ns std.lang.model.spec-xtalk.fn-lua-test
  (:use code.test)
  (:require [std.lang.model.spec-xtalk.fn-lua :refer :all]
            [std.lang :as l]
            [std.lib :as h]))

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-del :added "4.0"}
(fact "deletes object"
  (l/emit-as :lua [(lua-tf-x-del '[_ obj])])
  => #"=")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-cat :added "4.0"}
(fact "concatenates"
  (l/emit-as :lua [(lua-tf-x-cat '[_ "a" "b"])])
  => #"\.\.")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-eval :added "4.0"}
(fact "evals"
  (l/emit-as :lua [(lua-tf-x-eval '[_ "1 + 1"])])
  => #"loadstring")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-apply :added "4.0"}
(fact "applies"
  (l/emit-as :lua [(lua-tf-x-apply '[_ f args])])
  => #"unpack")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-err :added "4.0"}
(fact "errors"
  (l/emit-as :lua [(lua-tf-x-err '[_ "msg"])])
  => #"error")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-shell :added "4.0"}
(fact "shell"
  (l/emit-as :lua [(lua-tf-x-shell '[_ "ls" cm])])
  => #"io.popen")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-hash-id :added "4.0"}
(fact "hash id"
  (l/emit-as :lua [(lua-tf-x-hash-id '[_ obj])])
  => #"nil")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-type-native :added "4.0"}
(fact "type native"
  (l/emit-as :lua [(lua-tf-x-type-native '[_ obj])])
  => #"type")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-proto-create :added "4.0"}
(fact "proto create"
  (l/emit-as :lua [(lua-tf-x-proto-create '[_ {}])])
  => #"__index")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-global-has? :added "4.0"}
(fact "global has?"
  (l/emit-as :lua [(lua-tf-x-global-has? '[_ sym])])
  => #"sym")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-global-set :added "4.0"}
(fact "global set"
  (l/emit-as :lua [(lua-tf-x-global-set '[_ sym val])])
  => #"sym")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-global-del :added "4.0"}
(fact "global del"
  (l/emit-as :lua [(lua-tf-x-global-del '[_ sym])])
  => #"sym")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-m-quot :added "4.0"}
(fact "math quot"
  (l/emit-as :lua [(lua-tf-x-m-quot '[_ 1 2])])
  => #"math.floor")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-is-string? :added "4.0"}
(fact "is string?"
  (l/emit-as :lua [(lua-tf-x-is-string? '[_ x])])
  => #"type")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-is-number? :added "4.0"}
(fact "is number?"
  (l/emit-as :lua [(lua-tf-x-is-number? '[_ x])])
  => #"type")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-is-integer? :added "4.0"}
(fact "is integer?"
  (l/emit-as :lua [(lua-tf-x-is-integer? '[_ x])])
  => #"%")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-is-boolean? :added "4.0"}
(fact "is boolean?"
  (l/emit-as :lua [(lua-tf-x-is-boolean? '[_ x])])
  => #"type")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-is-function? :added "4.0"}
(fact "is function?"
  (l/emit-as :lua [(lua-tf-x-is-function? '[_ x])])
  => #"type")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-is-object? :added "4.0"}
(fact "is object?"
  (l/emit-as :lua [(lua-tf-x-is-object? '[_ x])])
  => #"type")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-is-array? :added "4.0"}
(fact "is array?"
  (l/emit-as :lua [(lua-tf-x-is-array? '[_ x])])
  => #"type")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-lu-create :added "4.0"}
(fact "lu create"
  (l/emit-as :lua [(lua-tf-x-lu-create '[_])])
  => #"setmetatable")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-lu-get :added "4.0"}
(fact "lu get"
  (l/emit-as :lua [(lua-tf-x-lu-get '[_ lu obj])])
  => #"tostring")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-lu-set :added "4.0"}
(fact "lu set"
  (l/emit-as :lua [(lua-tf-x-lu-set '[_ lu obj gid])])
  => #"tostring")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-lu-del :added "4.0"}
(fact "lu del"
  (l/emit-as :lua [(lua-tf-x-lu-del '[_ lu obj])])
  => #"tostring")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-bit-and :added "4.0"}
(fact "bit and"
  (l/emit-as :lua [(lua-tf-x-bit-and '[_ a b])])
  => #"bit.band")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-bit-or :added "4.0"}
(fact "bit or"
  (l/emit-as :lua [(lua-tf-x-bit-or '[_ a b])])
  => #"bit.bor")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-bit-lshift :added "4.0"}
(fact "bit lshift"
  (l/emit-as :lua [(lua-tf-x-bit-lshift '[_ a n])])
  => #"bit.lshift")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-bit-rshift :added "4.0"}
(fact "bit rshift"
  (l/emit-as :lua [(lua-tf-x-bit-rshift '[_ a n])])
  => #"bit.rshift")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-bit-xor :added "4.0"}
(fact "bit xor"
  (l/emit-as :lua [(lua-tf-x-bit-xor '[_ a n])])
  => #"bit.bxor")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-arr-clone :added "4.0"}
(fact "arr clone"
  (l/emit-as :lua [(lua-tf-x-arr-clone '[_ arr])])
  => #"unpack")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-arr-slice :added "4.0"}
(fact "arr slice"
  (l/emit-as :lua [(lua-tf-x-arr-slice '[_ arr 0 1])])
  => #"unpack")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-arr-remove :added "4.0"}
(fact "arr remove"
  (l/emit-as :lua [(lua-tf-x-arr-remove '[_ arr i])])
  => #"table.remove")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-arr-push-first :added "4.0"}
(fact "arr push first"
  (l/emit-as :lua [(lua-tf-x-arr-push-first '[_ arr item])])
  => #"table.insert")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-arr-pop-first :added "4.0"}
(fact "arr pop first"
  (l/emit-as :lua [(lua-tf-x-arr-pop-first '[_ arr])])
  => #"table.remove")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-arr-insert :added "4.0"}
(fact "arr insert"
  (l/emit-as :lua [(lua-tf-x-arr-insert '[_ arr idx item])])
  => #"table.insert")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-arr-sort :added "4.0"}
(fact "arr sort"
  (l/emit-as :lua [(lua-tf-x-arr-sort '[_ arr key-fn comp-fn])])
  => #"table.sort")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-str-split :added "4.0"}
(fact "str split"
  (l/emit-as :lua [(lua-tf-x-str-split '[_ s tok])])
  => #"string.gsub")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-str-join :added "4.0"}
(fact "str join"
  (l/emit-as :lua [(lua-tf-x-str-join '[_ s arr])])
  => #"table.concat")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-str-index-of :added "4.0"}
(fact "str index of"
  (l/emit-as :lua [(lua-tf-x-str-index-of '[_ s tok])])
  => #"string.find")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-str-to-fixed :added "4.0"}
(fact "str to fixed"
  (l/emit-as :lua [(lua-tf-x-str-to-fixed '[_ num digits])])
  => #"string.format")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-str-replace :added "4.0"}
(fact "str replace"
  (l/emit-as :lua [(lua-tf-x-str-replace '[_ s tok repl])])
  => #"string.gsub")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-str-trim :added "4.0"}
(fact "str trim"
  (l/emit-as :lua [(lua-tf-x-str-trim '[_ s])])
  => #"string.gsub")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-str-trim-left :added "4.0"}
(fact "str trim left"
  (l/emit-as :lua [(lua-tf-x-str-trim-left '[_ s])])
  => #"string.gsub")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-str-trim-right :added "4.0"}
(fact "str trim right"
  (l/emit-as :lua [(lua-tf-x-str-trim-right '[_ s])])
  => #"string.gsub")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-return-encode :added "4.0"}
(fact "return encode"
  (l/emit-as :lua [(lua-tf-x-return-encode '[_ out id key])])
  => #"cjson.encode")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-return-wrap :added "4.0"}
(fact "return wrap"
  (l/emit-as :lua [(lua-tf-x-return-wrap '[_ f encode-fn])])
  => #"pcall")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-return-eval :added "4.0"}
(fact "return eval"
  (l/emit-as :lua [(lua-tf-x-return-eval '[_ s wrap-fn])])
  => #"loadstring")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-socket-connect :added "4.0"}
(fact "socket connect"
  (l/emit-as :lua [(lua-tf-x-socket-connect '[_ host port opts])])
  => #"socket")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-socket-send :added "4.0"}
(fact "socket send"
  (l/emit-as :lua [(lua-tf-x-socket-send '[_ conn s])])
  => #"send")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-socket-close :added "4.0"}
(fact "socket close"
  (l/emit-as :lua [(lua-tf-x-socket-close '[_ conn])])
  => #"close")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-iter-from-obj :added "4.0"}
(fact "iter from obj"
  (l/emit-as :lua [(lua-tf-x-iter-from-obj '[_ obj])])
  => #"coroutine.wrap")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-iter-from-arr :added "4.0"}
(fact "iter from arr"
  (l/emit-as :lua [(lua-tf-x-iter-from-arr '[_ arr])])
  => #"coroutine.wrap")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-iter-from :added "4.0"}
(fact "iter from"
  (l/emit-as :lua [(lua-tf-x-iter-from '[_ obj])])
  => #"coroutine.wrap")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-iter-eq :added "4.0"}
(fact "iter eq"
  (l/emit-as :lua [(lua-tf-x-iter-eq '[_ it0 it1 eq-fn])])
  => #"for")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-iter-next :added "4.0"}
(fact "iter next"
  (l/emit-as :lua [(lua-tf-x-iter-next '[_ it])])
  => #"it")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-iter-has? :added "4.0"}
(fact "iter has?"
  (l/emit-as :lua [(lua-tf-x-iter-has? '[_ obj])])
  => #"iterator")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-iter-native? :added "4.0"}
(fact "iter native?"
  (l/emit-as :lua [(lua-tf-x-iter-native? '[_ it])])
  => #"type")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-cache :added "4.0"}
(fact "cache"
  (l/emit-as :lua [(lua-tf-x-cache '[_ key])])
  => #"ngx.shared")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-cache-list :added "4.0"}
(fact "cache list"
  (l/emit-as :lua [(lua-tf-x-cache-list '[_ cache])])
  => #"get_keys")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-cache-flush :added "4.0"}
(fact "cache flush"
  (l/emit-as :lua [(lua-tf-x-cache-flush '[_ cache])])
  => #"flush_all")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-cache-get :added "4.0"}
(fact "cache get"
  (l/emit-as :lua [(lua-tf-x-cache-get '[_ cache key])])
  => #"get")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-cache-set :added "4.0"}
(fact "cache set"
  (l/emit-as :lua [(lua-tf-x-cache-set '[_ cache key val])])
  => #"set")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-cache-del :added "4.0"}
(fact "cache del"
  (l/emit-as :lua [(lua-tf-x-cache-del '[_ cache key])])
  => #"delete")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-cache-incr :added "4.0"}
(fact "cache incr"
  (l/emit-as :lua [(lua-tf-x-cache-incr '[_ cache key num])])
  => #"incr")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-thread-spawn :added "4.0"}
(fact "thread spawn"
  (l/emit-as :lua [(lua-tf-x-thread-spawn '[_ thunk])])
  => #"ngx.thread.spawn")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-thread-join :added "4.0"}
(fact "thread join"
  (l/emit-as :lua [(lua-tf-x-thread-join '[_ thread])])
  => #"ngx.thread.wait")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-with-delay :added "4.0"}
(fact "with delay"
  (l/emit-as :lua [(lua-tf-x-with-delay '[_ thunk ms])])
  => #"ngx.thread.spawn")

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-slurp :added "4.0"}
(fact "slurp"
  (comment (l/emit-as :lua [(lua-tf-x-slurp '[_ filename])])
           => nil?))

^{:refer std.lang.model.spec-xtalk.fn-lua/lua-tf-x-spit :added "4.0"}
(fact "spit"
  (comment (l/emit-as :lua [(lua-tf-x-spit '[_ filename s])])
           => nil?))
