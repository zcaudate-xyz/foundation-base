(ns std.lang.model.spec-lua-test
  (:use code.test)
  (:require [std.lang.model.spec-lua :refer :all]
            [std.lang.base.script :as script]
            [std.lang.base.util :as ut]
            [std.lib :as h]))

(script/script- :lua)

^{:refer std.lang.model.spec-lua/tf-local :added "4.0"}
(fact "a more flexible `var` replacement"
  ^:hidden
  
  (tf-local '(local a 1))
  => '(var* :local a := 1)

  (tf-local '(local a := 1))
  => '(var* :local a := 1)

  (tf-local '(local '[a b] := '[x y]))
  => '(var* :local (quote [a b]) := (quote [x y]))
  
  (tf-local '(local [a b] := [x y]))
  => '(var* :local (quote [a b]) := (unpack [x y]))

  (tf-local '(local '[a b] (hello)))
  => '(var* :local (quote [a b]) := (hello)))

^{:refer std.lang.model.spec-lua/tf-c-ffi :added "4.0"}
(fact "transforms a c ffi block"
  ^:hidden
  
  (tf-c-ffi '(%.c (fn ^{:- [:float]
                        :header true}
                    powf [:float x :float y])
                  (fn ^{:- [:double]
                        :header true}
                    exp [:double x])))
  => '(\\ "[["
       (\\ \\ (!:lang
               {:lang :c}
               (do (fn powf [:float x :float y])
                   (fn exp [:double x]))))
       \\
       "]]"))

^{:refer std.lang.model.spec-lua/lua-map-key :added "3.0"}
(fact "custom lua map key"
  ^:hidden

  (lua-map-key 123 +grammar+ {})
  => "[123]"

  (lua-map-key "123" +grammar+ {})
  => "['123']"
  

  (lua-map-key "abc" +grammar+ {})
  => "abc"

  (lua-map-key :abc +grammar+ {})
  => "abc")

^{:refer std.lang.model.spec-lua/tf-for-object :added "4.0"}
(fact "for object transform"
  ^:hidden
  
  (tf-for-object '(for:object [[k v] obj]
                              [k v]))
  => '(for [[k v] :in (pairs obj)] [k v]))

^{:refer std.lang.model.spec-lua/tf-for-array :added "4.0"}
(fact "for array transform"
  ^:hidden
  
  (tf-for-array '(for:array [[i e] arr]
                            [i e]))
  => '(for [[i e] :in (ipairs arr)] [i e]))

^{:refer std.lang.model.spec-lua/tf-for-iter :added "4.0"}
(fact  "for iter transform"
  ^:hidden
  
  (tf-for-iter '(for:iter [e iter]
                          e))
  => '(for [e :in iter] e))

^{:refer std.lang.model.spec-lua/tf-for-index :added "4.0"}
(fact "for index transform"
  ^:hidden
  
  (tf-for-index '(for:index [i [0 2 10]]
                            i))
  => '(for [i := (quote [0 2 10])] i))

^{:refer std.lang.model.spec-lua/tf-for-return :added "4.0"}
(fact  "for return transform"
  ^:hidden
  
  (tf-for-return '(for:return [[ok err] (call)]
                              {:success (return ok)
                               :error   (return err)}))
  => '(do (var (quote [ok err]) (call))
          (if (not err)
            (return ok)
            (return err))))

^{:refer std.lang.model.spec-lua/tf-for-try :added "4.0"}
(fact "for try transform"
  ^:hidden

  (tf-for-try '(for:try [[ok err] (call (x:callback))]
                        {:success (return ok)
                         :error   (return err)}))
  => '(do (var (quote [ok out]) (pcall (fn [] (return (call (x:callback))))))
          (if ok (do* (var ok := out)
                      (return ok))
              (do* (var err := out)
                   (return err)))))

^{:refer std.lang.model.spec-lua/tf-for-async :added "4.0"}
(fact  "for async transform"
  ^:hidden

  (tf-for-async '(for:async [[ok err] (call (x:callback))]
                            {:success (return ok)
                             :error   (return err)
                             :finally (return true)}))
  => '(ngx.thread.spawn
       (fn []
         (for:try [[ok err] (call (x:callback))]
                  {:success (return ok),
                   :error   (return err)})
         (return true))))

^{:refer std.lang.model.spec-lua/tf-yield :added "4.0"}
(fact "yield transform"
  ^:hidden
  
  (tf-yield '(yield e))
  => '(coroutine.yield e))

^{:refer std.lang.model.spec-lua/tf-defgen :added "4.0"}
(fact "defgen transform"
  ^:hidden
  
  (tf-defgen '(defgen hello [] (yield 2)))
  => '(defn hello [] (return (coroutine.wrap (fn [] (yield 2))))))

^{:refer std.lang.model.spec-lua/lua-module-link :added "4.0"}
(fact "gets the absolute lua based module"
  
  (lua-module-link 'kmi.common {:root-ns 'kmi.hello})
  => "./common"

  (lua-module-link 'kmi.exchange
                   {:root-ns 'kmi :target "src"})
  => "./kmi/exchange")

^{:refer std.lang.model.spec-lua/lua-module-export :added "4.0"}
(fact "outputs the lua module export form"
  ^:hidden
  
  (lua-module-export 'kmi.common {:root-ns 'kmi.hello})
  => '(return (tab)))
