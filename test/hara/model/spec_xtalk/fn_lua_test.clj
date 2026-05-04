(ns hara.model.spec-xtalk.fn-lua-test
  (:require [hara.lang :as l]
            [hara.model.spec-lua.variant-nginx :as nginx]
            [hara.model.spec-xtalk.fn-lua :refer :all])
  (:use code.test))

^{:refer hara.model.spec-xtalk.fn-lua/+lua-promise+ :added "4.1"}
(fact "async run emits a coroutine start"
  (l/emit-as :lua [(lua-tf-x-async-run '[_ thunk])])
  => #"coroutine\.resume\(coroutine\.create\(thunk\)\)")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-del :added "4.0"}
(fact "deletes object"
  (l/emit-as :lua [(lua-tf-x-del '[_ obj])])
  => #"=")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-cat :added "4.0"}
(fact "concatenates"
  (lua-tf-x-cat '[_ "a" "b"])
  => '(cat "a" "b"))

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-eval :added "4.0"}
(fact "evals"
  (l/emit-as :lua [(lua-tf-x-eval '[_ "1 + 1"])])
  => #"loadstring")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-apply :added "4.0"}
(fact "applies"
  (l/emit-as :lua [(lua-tf-x-apply '[_ f args])])
  => #"unpack")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-err :added "4.0"}
(fact "errors"
  (l/emit-as :lua [(lua-tf-x-err '[_ "msg"])])
  => #"error")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-ex-native? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-ex-new :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-ex-message :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-ex-data :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-hash-id :added "4.0"}
(fact "hash id"
  (l/emit-as :lua [(lua-tf-x-hash-id '[_ obj])])
  => #"nil")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-type-native :added "4.0"}
(fact "type native"
  (l/emit-as :lua [(lua-tf-x-type-native '[_ obj])])
  => #"type")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-has-key? :added "4.1"}
(fact "has key"
  (l/emit-as :lua [(lua-tf-x-has-key? '[_ obj "k" nil])])
  => #"\['k'\]")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-global-has? :added "4.0"}
(fact "global has?"
  (l/emit-as :lua [(lua-tf-x-global-has? '[_ sym])])
  => #"sym")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-global-set :added "4.0"}
(fact "global set"
  (l/emit-as :lua [(lua-tf-x-global-set '[_ sym val])])
  => #"sym")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-global-del :added "4.0"}
(fact "global del"
  (l/emit-as :lua [(lua-tf-x-global-del '[_ sym])])
  => #"sym")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-m-mod :added "4.1"}
(fact "math mod"
  (l/emit-as :lua [(lua-tf-x-m-mod '[_ 10 3])])
  => #"%")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-m-quot :added "4.0"}
(fact "math quot"
  (l/emit-as :lua [(lua-tf-x-m-quot '[_ 1 2])])
  => #"math.floor")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-is-string? :added "4.0"}
(fact "is string?"
  (l/emit-as :lua [(lua-tf-x-is-string? '[_ x])])
  => #"type")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-is-number? :added "4.0"}
(fact "is number?"
  (l/emit-as :lua [(lua-tf-x-is-number? '[_ x])])
  => #"type")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-is-integer? :added "4.0"}
(fact "is integer?"
  (l/emit-as :lua [(lua-tf-x-is-integer? '[_ x])])
  => #"%")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-is-boolean? :added "4.0"}
(fact "is boolean?"
  (l/emit-as :lua [(lua-tf-x-is-boolean? '[_ x])])
  => #"type")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-is-function? :added "4.0"}
(fact "is function?"
  (l/emit-as :lua [(lua-tf-x-is-function? '[_ x])])
  => #"type")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-is-object? :added "4.0"}
(fact "is object?"
  (l/emit-as :lua [(lua-tf-x-is-object? '[_ x])])
  => #"type")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-is-array? :added "4.0"}
(fact "is array?"
  (l/emit-as :lua [(lua-tf-x-is-array? '[_ x])])
  => #"type")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-lu-create :added "4.0"}
(fact "lu create"
  (l/emit-as :lua [(lua-tf-x-lu-create '[_])])
  => #"setmetatable")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-lu-get :added "4.0"}
(fact "lu get"
  (l/emit-as :lua [(lua-tf-x-lu-get '[_ lu obj])])
  => #"tostring")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-lu-set :added "4.0"}
(fact "lu set"
  (l/emit-as :lua [(lua-tf-x-lu-set '[_ lu obj gid])])
  => #"tostring")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-lu-del :added "4.0"}
(fact "lu del"
  (l/emit-as :lua [(lua-tf-x-lu-del '[_ lu obj])])
  => #"tostring")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-bit-and :added "4.0"}
(fact "bit and"
  (l/emit-as :lua [(lua-tf-x-bit-and '[_ a b])])
  => #"bit.band")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-bit-or :added "4.0"}
(fact "bit or"
  (l/emit-as :lua [(lua-tf-x-bit-or '[_ a b])])
  => #"bit.bor")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-bit-lshift :added "4.0"}
(fact "bit lshift"
  (l/emit-as :lua [(lua-tf-x-bit-lshift '[_ a n])])
  => #"bit.lshift")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-bit-rshift :added "4.0"}
(fact "bit rshift"
  (l/emit-as :lua [(lua-tf-x-bit-rshift '[_ a n])])
  => #"bit.rshift")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-bit-xor :added "4.0"}
(fact "bit xor"
  (l/emit-as :lua [(lua-tf-x-bit-xor '[_ a n])])
  => #"bit.bxor")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-arr-clone :added "4.0"}
(fact "arr clone"
  (l/emit-as :lua [(lua-tf-x-arr-clone '[_ arr])])
  => #"unpack")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-arr-slice :added "4.0"}
(fact "arr slice"
  (l/emit-as :lua [(lua-tf-x-arr-slice '[_ arr 0 1])])
  => #"unpack")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-arr-remove :added "4.0"}
(fact "arr remove"
  (l/emit-as :lua [(lua-tf-x-arr-remove '[_ arr i])])
  => #"table.remove")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-arr-push-first :added "4.0"}
(fact "arr push first"
  (l/emit-as :lua [(lua-tf-x-arr-push-first '[_ arr item])])
  => #"table.insert")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-arr-pop-first :added "4.0"}
(fact "arr pop first"
  (l/emit-as :lua [(lua-tf-x-arr-pop-first '[_ arr])])
  => #"table.remove")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-arr-insert :added "4.0"}
(fact "arr insert"
  (l/emit-as :lua [(lua-tf-x-arr-insert '[_ arr idx item])])
  => #"table.insert")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-arr-sort :added "4.0"}
(fact "arr sort"
  (l/emit-as :lua [(lua-tf-x-arr-sort '[_ arr key-fn comp-fn])])
  => #"table.sort")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-str-split :added "4.0"}
(fact "str split"
  (l/emit-as :lua [(lua-tf-x-str-split '[_ s tok])])
  => #"string.gsub")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-str-join :added "4.0"}
(fact "str join"
  (l/emit-as :lua [(lua-tf-x-str-join '[_ s arr])])
  => #"table.concat")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-str-index-of :added "4.0"}
(fact "str index of"
  (l/emit-as :lua [(lua-tf-x-str-index-of '[_ s tok])])
  => #"string.find")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-str-to-fixed :added "4.0"}
(fact "str to fixed"
  (l/emit-as :lua [(lua-tf-x-str-to-fixed '[_ num digits])])
  => #"string.format")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-str-replace :added "4.0"}
(fact "str replace"
  (l/emit-as :lua [(lua-tf-x-str-replace '[_ s tok repl])])
  => #"string.gsub")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-str-trim :added "4.0"}
(fact "str trim"
  (l/emit-as :lua [(lua-tf-x-str-trim '[_ s])])
  => #"string.gsub")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-str-trim-left :added "4.0"}
(fact "str trim left"
  (l/emit-as :lua [(lua-tf-x-str-trim-left '[_ s])])
  => #"string.gsub")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-str-trim-right :added "4.0"}
(fact "str trim right"
  (l/emit-as :lua [(lua-tf-x-str-trim-right '[_ s])])
  => #"string.gsub")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-return-encode :added "4.0"}
(fact "return encode"
  (l/emit-as :lua [(lua-tf-x-return-encode '[_ out id key])])
  => #"cjson.encode"

  (l/emit-as :lua [(lua-tf-x-return-encode '[_ out id key])])
  => #"\['return'\]")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-return-wrap :added "4.0"}
(fact "return wrap"
  (l/emit-as :lua [(lua-tf-x-return-wrap '[_ f encode-fn])])
  => #"pcall")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-return-eval :added "4.0"}
(fact "return eval"
  (l/emit-as :lua [(lua-tf-x-return-eval '[_ s wrap-fn])])
  => #"loadstring"

  (l/emit-as :lua [(lua-tf-x-return-eval '[_ s wrap-fn])])
  => #(not (re-find #"unpack\(load_fn\(s\)\)" %)))

^{:refer hara.model.spec-lua.variant-nginx/lua-tf-x-socket-connect :added "4.1"}
(fact "nginx socket connect"
  (let [out (l/emit-as :lua.nginx [(nginx/lua-tf-x-socket-connect '[_ host port opts cb])])]
    [(boolean (re-find #"ngx\.socket\.tcp\(\)" out))
     (boolean (re-find #"conn:connect\(host,\s*port\)" out))
     (boolean (re-find #"return cb\(err,\s*nil\)" out))
     (boolean (re-find #"return cb\(nil,\s*conn\)" out))])
  => [true true true true])

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-socket-send :added "4.0"}
(fact "socket send"
  (l/emit-as :lua [(lua-tf-x-socket-send '[_ conn s])])
  => #"send")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-socket-close :added "4.0"}
(fact "socket close"
  (l/emit-as :lua [(lua-tf-x-socket-close '[_ conn])])
  => #"close")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-iter-from-obj :added "4.0"}
(fact "iter from obj"
  (l/emit-as :lua [(lua-tf-x-iter-from-obj '[_ obj])])
  => #"coroutine.wrap")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-iter-from-arr :added "4.0"}
(fact "iter from arr"
  (l/emit-as :lua [(lua-tf-x-iter-from-arr '[_ arr])])
  => #"coroutine.wrap")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-iter-from :added "4.0"}
(fact "iter from"
  (l/emit-as :lua [(lua-tf-x-iter-from '[_ obj])])
  => #"coroutine.wrap")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-iter-eq :added "4.0"}
(fact "iter eq"
  (l/emit-as :lua [(lua-tf-x-iter-eq '[_ it0 it1 eq-fn])])
  => #"for")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-iter-next :added "4.0"}
(fact "iter next"
  (l/emit-as :lua [(lua-tf-x-iter-next '[_ it])])
  => #"it")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-iter-has? :added "4.0"}
(fact "iter has?"
  (l/emit-as :lua [(lua-tf-x-iter-has? '[_ obj])])
  => #"iterator")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-iter-native? :added "4.0"}
(fact "iter native?"
  (l/emit-as :lua [(lua-tf-x-iter-native? '[_ it])])
  => #"type")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-with-delay :added "4.0"}
(fact "with delay"
  (l/emit-as :lua [(lua-tf-x-with-delay '[_ ms thunk])])
  => #"socket.sleep"

  (l/emit-as :lua.nginx [(nginx/lua-tf-x-with-delay '[_ ms thunk])])
  => #"ngx.thread.spawn")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-pwd :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-shell :added "4.0"}
(fact "shell"
  (l/emit-as :lua [(lua-tf-x-shell '[_ "ls" cm])])
  => #"io.popen")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-file-resolve :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-file-slurp :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-lua/lua-tf-x-file-spit :added "4.1"}
(fact "TODO")
