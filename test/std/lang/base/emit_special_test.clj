(ns std.lang.base.emit-special-test
  (:require [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.emit-prep-lua-test :as prep]
            [std.lang.base.emit-special :refer :all]
            [std.lang.base.impl-entry :as entry]
            [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.model.spec-lua :as lua]
            [std.lib.env])
  (:use code.test))

(def +library-ext+
  (doto (lib/library:create
         {:snapshot (snap/snapshot {:lua {:id :lua
                                          :book prep/+book-min+}})})
    (lib/install-module! :lua 'L.util
                         {:require '[[L.core :as u]]
                          :import  '[["cjson" :as cjson]]
                          :export '[MODULE]})
    (lib/add-entry-single!
     (entry/create-code-base
      '(defn sub-fn
         [a b]
         (return (fn:> ((u/identity-fn u/sub) a b))))
      {:lang :lua
       :namespace (std.lib.env/ns-sym)
       :module 'L.util}
      {}))
    (lib/add-entry-single!
     (entry/create-code-base
      '(defn add-fn
         [a b]
         (return (fn:> ((u/identity-fn u/add) a (-/sub-fn b 0)))))
      {:lang :lua
       :namespace (std.lib.env/ns-sym)
       :module 'L.util}
      {}))))

^{:refer std.lang.base.emit-special/emit-with-module-all-ids :added "4.0"}
(fact "emits the module given snapshot in opts"
  ^:hidden
  
  (set
   (emit-with-module-all-ids (lib/get-snapshot +library-ext+)
                             :lua
                             'L.util
                             identity))
  => '#{add-fn sub-fn})

^{:refer std.lang.base.emit-special/emit-with-module :added "4.0"}
(fact "emits the module given snapshot in opts"
  ^:hidden
  
  (emit-with-module nil nil '(module :lua L.util)
                    (:grammar prep/+book-min+)
                    {:snapshot (lib/get-snapshot +library-ext+)})
  => "{\"sub_fn\":L.util/sub-fn,\"add_fn\":L.util/add-fn}"


  (binding [common/*emit-fn* test-special-loop]
    (emit-with-module nil nil '(module :lua L.util)
                      (:grammar prep/+book-min+)
                      {:snapshot (lib/get-snapshot +library-ext+)
                       :module (lib/get-module +library-ext+ :lua 'L.util)
                       :layout :full}))
  => "{\"sub_fn\":L_util____sub_fn,\"add_fn\":L_util____add_fn}")

^{:refer std.lang.base.emit-special/emit-with-preprocess :added "4.0"}
(fact "emits an eval form"

  (emit-with-preprocess '(L.core/sub 1 2)
                        (:grammar prep/+book-min+)
                        {:lang :lua
                         :module   (lib/get-module +library-ext+ :lua 'L.core)
                         :snapshot (lib/get-snapshot +library-ext+)})
  => "(- 1 2)")


^{:refer std.lang.base.emit-special/emit-with-eval :added "4.0"}
(fact "emits an eval form"
  ^:hidden
  
  (emit-with-eval nil nil
                  '(!:eval (+ 1 2 3))
                  (:grammar prep/+book-min+)
                  {:lang :lua
                   :module   (lib/get-module +library-ext+ :lua 'L.util)
                   :snapshot (lib/get-snapshot +library-ext+)})
  => "6"
  
  (emit-with-eval nil nil
                  '(!:eval '(+ 1 2 3))
                  (:grammar prep/+book-min+)
                  {:lang :lua
                   :module   (lib/get-module +library-ext+ :lua 'L.util)
                   :snapshot (lib/get-snapshot +library-ext+)})
  => "(+ 1 2 3)"

  (binding [common/*emit-fn* test-special-loop]
    (emit-with-eval nil nil
                    '(!:eval '(+ 1 2 3))
                    (:grammar prep/+book-min+)
                    {:lang :lua
                     :module   (lib/get-module +library-ext+ :lua 'L.util)
                     :snapshot (lib/get-snapshot +library-ext+)}))
  => "1 + 2 + 3")

^{:refer std.lang.base.emit-special/emit-with-deref :added "4.0"}
(fact "emits an embedded var"
  ^:hidden

  (def hello 1)
  
  (emit-with-deref nil nil '(!:deref (var hello))
                   (:grammar prep/+book-min+)
                   {:lang :lua
                    :module   (lib/get-module +library-ext+ :lua 'L.util)
                    :snapshot (lib/get-snapshot +library-ext+)})
  => "1"

  (def world '(L.core/sub 1 2))
  
  (emit-with-deref nil nil '(!:deref (var world))
                   (:grammar prep/+book-min+)
                   {:lang :lua
                    :module   (lib/get-module +library-ext+ :lua 'L.util)
                    :snapshot (lib/get-snapshot +library-ext+)})
  => "(- 1 2)")

^{:refer std.lang.base.emit-special/emit-with-lang :added "4.0"}
(fact "emits an embedded eval"
  ^:hidden
  
  (emit-with-lang nil nil '(!:lang {:lang :lua} (+ @1 2 3))
                  (:grammar prep/+book-min+)
                  {:snapshot (lib/get-snapshot +library-ext+)
                   })
  => "(+ (!:eval 1) 2 3)"

  (binding [common/*emit-fn* test-special-loop]
    (emit-with-lang nil nil '(!:lang {:lang :lua} (+ @1 2 3))
                    (:grammar prep/+book-min+)
                    {:snapshot (lib/get-snapshot +library-ext+)}))
  => "1 + 2 + 3")

^{:refer std.lang.base.emit-special/test-special-loop :added "4.0"}
(fact "test step for special ops"
  ^:hidden
  
  (test-special-loop '(!:lang {:lang :lua} (+ @1 2 3))
                           (:grammar prep/+book-min+)
                           {:snapshot (lib/get-snapshot +library-ext+)})
  => "(+ (!:eval 1) 2 3)")

^{:refer std.lang.base.emit-special/test-special-emit :added "4.0"}
(fact "test function for special ops"
  ^:hidden
  
  (test-special-emit '(!:lang {:lang :lua} (+ @1 2 3))
                       (:grammar prep/+book-min+)
                       {:snapshot (lib/get-snapshot +library-ext+)})
  => "1 + 2 + 3")
