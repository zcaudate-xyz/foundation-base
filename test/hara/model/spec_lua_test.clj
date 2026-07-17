(ns hara.model.spec-lua-test
  (:require [hara.lang :as l]
             [hara.lang.script :as script]
             [hara.common.util :as ut]
             [hara.model.spec-lua :refer :all]
             [hara.model.spec-lua.variant-nginx :as nginx])
  (:use code.test))

(script/script- :lua)

^{:refer hara.model.spec-lua/tf-counter :added "4.1"}
(fact "compound assignment lowers to valid lua assignments"
  [(l/emit-as :lua '[(:+= a 2)])
   (l/emit-as :lua '[(:-= a 2)])
   (l/emit-as :lua '[(:*= a 2)])
   (l/emit-as :lua '[(:+= circle.x 20)])]
  => ["a = (a + 2)"
      "a = (a - 2)"
      "a = (a * 2)"
      "circle.x = (circle.x + 20)"])

(fact "try/catch lowers to a pcall wrapper"
  (let [out (l/emit-as :lua
                       '[(try
                           (throw (x:ex-new "boom" {:a 1}))
                           (catch e
                             (x:print (x:ex-data e))))])]
    [(boolean (re-find #"pcall\(function \(\)" out))
     (boolean (re-find #"error\(\{\['__type__'\]='xt\.exception'" out))
     (boolean (re-find #"local e =" out))
     (boolean (re-find #"e\['data'\]" out))
     (boolean (re-find #"print" out))])
  => [true true true true true])

(fact "try/finally preserves outer return flow"
  (let [out (l/emit-as :lua
                       '[(defn demo []
                           (try
                             (return 1)
                             (finally
                               (x:print "done"))))])]
    [(boolean (re-find #"pcall\(function \(\)" out))
     (boolean (re-find #"print\('done'\)" out))
     (boolean (re-find #"return lua_try_result__" out))
     (boolean (re-find #"error\(lua_try_error__" out))])
  => [true true false false])

(fact "for async transform for nginx"

  (nginx/tf-for-async '(for:async [[ok err] (call (x:callback))]
                                   {:success (return ok)
                                    :error   (return err)
                                    :finally (return true)}))
  => '(ngx.thread.spawn
       (fn []
         (for:try [[ok err] (call (x:callback))]
                  {:success (return ok),
                   :error   (return err)})
         (return true))))

^{:refer hara.model.spec-lua/lua-tf-incby :added "4.1"}
(fact "transforms incby forms")

^{:refer hara.model.spec-lua/lua-tf-decby :added "4.1"}
(fact "transforms decby forms")

^{:refer hara.model.spec-lua/lua-tf-mulby :added "4.1"}
(fact "transforms mulby forms")

^{:refer hara.model.spec-lua/lua-tf-local :added "4.1"}
(fact "transforms local forms")

^{:refer hara.model.spec-lua/lua-tf-c-ffi :added "4.1"}
(fact "transforms c-ffi forms")

^{:refer hara.model.spec-lua/lua-map-key :added "3.0"}
(fact "custom lua map key"

  (lua-map-key 123 +grammar+ {})
  => "[123]"

  (lua-map-key "123" +grammar+ {})
  => "['123']"


  (lua-map-key "abc" +grammar+ {})
  => "abc"

  (lua-map-key :abc +grammar+ {})
  => "abc")

^{:refer hara.model.spec-lua/lua-tf-for-object :added "4.1"}
(fact "transforms for:object loops")

^{:refer hara.model.spec-lua/lua-tf-for-array :added "4.1"}
(fact "transforms for:array loops")

^{:refer hara.model.spec-lua/lua-tf-for-iter :added "4.1"}
(fact "transforms for:iter loops")

^{:refer hara.model.spec-lua/lua-tf-for-index :added "4.1"}
(fact "transforms for:index loops")

^{:refer hara.model.spec-lua/lua-tf-for-return :added "4.1"}
(fact "transforms for:return loops")

^{:refer hara.model.spec-lua/lua-tf-for-async :added "4.1"}
(fact "transforms for:async loops")

^{:refer hara.model.spec-lua/lua-tf-yield :added "4.1"}
(fact "transforms yield forms")

^{:refer hara.model.spec-lua/lua-tf-throw :added "4.1"}
(fact "transforms throw forms")

^{:refer hara.model.spec-lua/lua-tf-defgen :added "4.1"}
(fact "transforms defgen forms")

^{:refer hara.model.spec-lua/lua-tf-prototype-create :added "4.1"}
(fact "creates prototypes")

^{:refer hara.model.spec-lua/lua-tf-prototype-method :added "4.1"}
(fact "calls prototype methods")

^{:refer hara.model.spec-lua/lua-module-link :added "4.0"}
(fact "gets the absolute lua based module"

  (lua-module-link 'kmi.common {:root-ns 'kmi.hello})
  => "./common"

  (lua-module-link 'kmi.exchange
                   {:root-ns 'kmi :target "src"})
  => "./kmi/exchange")

^{:refer hara.model.spec-lua/lua-module-export :added "4.0"}
(fact "outputs the lua module export form"

  (lua-module-export 'kmi.common {:root-ns 'kmi.hello})
  => '(return (tab)))

^{:refer hara.model.spec-lua/variant-meta :added "4.1"}
(fact "provides lua variant metadata")

^{:refer hara.model.spec-lua/variant-grammar :added "4.1"}
(fact "provides lua variant grammar")

^{:refer hara.model.spec-lua/lua-vector :added "4.1"}
(fact "emits lua vectors")


^{:refer hara.model.spec-lua/lua-emit-input-rest :added "4.1"}
(fact "emits Lua's anonymous varargs marker"
  (lua-emit-input-rest {:symbol 'args} nil nil) => "...")
