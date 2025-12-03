(ns rt.basic.type-oneshot-test
  (:use code.test)
  (:require [rt.basic.type-oneshot :as p]
            [std.lang.model.spec-lua :as spec-lua]
            [std.lang.model.spec-r :as spec-r]
            [std.lang.model.spec-js :as spec-js]
            [std.lang.model.spec-python :as spec-py]
            [rt.basic.impl.process-lua :as lua]
            [rt.basic.impl.process-r :as r]
            [rt.basic.impl.process-js :as js]
            [rt.basic.impl.process-python :as py]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.library :as lib]
            [std.lang.base.util :as ut]
            [std.lang.base.emit-prep-lua-test :as prep]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.impl-entry :as entry]
            [std.lang.base.book :as book]
            [std.json :as json]
            [std.lib :as h]))

(def +library-ext+
  (doto (lib/library:create
         {:snapshot (snap/snapshot {:lua {:id :lua
                                          :book prep/+book-min+}})})
    (lib/install-book! (dissoc spec-lua/+book+ :parent))
    (lib/install-book! (dissoc spec-r/+book+ :parent))
    (lib/install-book! (dissoc spec-js/+book+ :parent))
    (lib/install-book! (dissoc spec-py/+book+ :parent))
    (lib/install-module! :lua 'L.util
                         {:require '[[L.core :as u]]
                          :import '[["cjson" :as cjson]]})
    (lib/add-entry-single!
     (entry/create-code-base
      '(defn sub-fn
         [a b]
         (return ((u/identity-fn u/sub) a b)))
      {:lang :lua
       :namespace (h/ns-sym)
       :module 'L.util
       :line 1}
      {}))
    (lib/add-entry-single!
     (entry/create-code-base
      '(defn add-fn
         [a b]
         (return ((u/identity-fn u/add) a (-/sub-fn b 0))))
      {:lang :lua
       :namespace (h/ns-sym)
       :module 'L.util
       :line 2}
      {}))))

(def +ptr-identity+
  (ut/lang-pointer :lua
                   {:module 'L.core
                    :id 'identity-fn
                    :section :code
                    :library +library-ext+}))

(def +ptr+
  (ut/lang-pointer :lua
                   {:module 'L.util
                    :id 'add-fn
                    :section :code
                    :library +library-ext+}))

^{:refer rt.basic.type-oneshot/invoke-ptr-oneshot.compile :adopt true :added "4.0"}
(fact "compiles the pointer body"
  ^:hidden
  
  (ptr/with:input []
    (-> (p/rt-oneshot {:lang :lua
                       :layout :full})
        (p/invoke-ptr-oneshot +ptr+ [1 2])))
  => (std.string/|
   "local function L_core___identity_fn(x)"
   "  return x"
   "end"
   ""
   "local function L_util___sub_fn(a,b)"
   "  return L_core___identity_fn(function (x,y)"
   "    return x - y"
   "  end)(a,b)"
   "end"
   ""
   "local function L_util___add_fn(a,b)"
   "  return L_core___identity_fn(function (x,y)"
   "    return x + y"
   "  end)(a,L_util___sub_fn(b,0))"
   "end"
   ""
   "return L_util___add_fn(1,2)")

  (ptr/with:raw
   (ptr/with:input []
     (-> (p/rt-oneshot {:lang :lua
                        :layout :flat})
         (p/invoke-ptr-oneshot +ptr-identity+ [1]))))
  => string?)

^{:refer rt.basic.type-oneshot/sh-exec :added "4.0"}
(fact "basic function for executing a shell process"
  ^:hidden
  
  (p/sh-exec ["python" "-c"] "print(1 + 1)" {})
  => "2"
  
  (p/sh-exec ["luajit" "-e"] "print(1 + 1)" {})
  => "2"

  (p/sh-exec ["r" "-s" "-e"] "1 + 1" {})
  => "[1] 2")

^{:refer rt.basic.type-oneshot/raw-eval-oneshot :added "4.0"}
(fact "evaluates a raw statement with oneshot"
  ^:hidden

  (-> (p/rt-oneshot {:lang :lua})
      (p/raw-eval-oneshot "print(1 + 1)"))
  => "2"

  (-> (p/rt-oneshot {:lang :r})
      (p/raw-eval-oneshot "1 + 1"))
  => "[1] 2")

^{:refer rt.basic.type-oneshot/invoke-ptr-oneshot :added "4.0"}
(fact "gets the oneshow invoke working"
  ^:hidden
  
  (-> (p/rt-oneshot {:lang :lua})
      (p/invoke-ptr-oneshot +ptr-identity+ [123])
      (ptr/with:raw)
      (json/read))
  => {"value" 123, "type" "data"}
  
  (-> (p/rt-oneshot {:lang :lua
                     :program :luajit})
      (p/invoke-ptr-oneshot +ptr-identity+ [123]))
  => 123

  (-> (p/rt-oneshot {:lang :lua
                     :program :luajit
                     :layout :full})
      (p/invoke-ptr-oneshot +ptr-identity+ ['(error "Hello")])
      (ptr/with:raw)
      (json/read))
  => {"value" "[string \"local function L_core___identity_fn(x)...\"]:5: Hello", "type" "error"}
  

  (-> (p/rt-oneshot {:lang :lua
                     :program :luajit
                     :layout :full})
      (p/invoke-ptr-oneshot +ptr+ [10 20])
      (ptr/with:raw)
      (json/read))
  => {"value" 30, "type" "data"})

^{:refer rt.basic.type-oneshot/rt-oneshot-setup :added "4.0"}
(fact "helper function for preparing oneshot params"
  ^:hidden
  
  (p/rt-oneshot-setup :lua :resty {} nil)
  => vector?)

^{:refer rt.basic.type-oneshot/rt-oneshot:create :added "4.0"}
(fact "creates a oneshot runtime"
  ^:hidden
  
  (->> (p/rt-oneshot:create {:lang :lua
                             :program :luajit})
       (into {}))
  => map?)

^{:refer rt.basic.type-oneshot/rt-oneshot :added "4.0"
  :setup [(defn rt-oneshot
            [lang & [opts]]
            [(-> (p/rt-oneshot (merge {:lang lang} opts))
                 (p/invoke-ptr-oneshot (ut/lang-pointer lang {:library +library-ext+})
                                       ['(+ 1 2 3 4)])
                 (ptr/with:input))
             (-> (p/rt-oneshot (merge {:lang lang} opts))
                 (p/invoke-ptr-oneshot (ut/lang-pointer lang {:library +library-ext+})
                                       ['(+ 1 2 3 4)])
                 (ptr/with:raw)
                 (json/read))
             (-> (p/rt-oneshot (merge {:lang lang} opts))
                 (p/invoke-ptr-oneshot (ut/lang-pointer lang {:library +library-ext+})
                                       ['(+ 1 2 3 4)]))])]}
(fact "creates a oneshot runtime"
  ^:hidden

  (rt-oneshot :r)
  => ["(function (){\n  return(1 + 2 + 3 + 4);\n})()"
      {"key" nil, "id" nil, "value" 10, "type" "data"}
      10]

  (rt-oneshot :lua)
  => ["return 1 + 2 + 3 + 4"
      {"value" 10, "type" "data"}
      10]

  (rt-oneshot :js)
  => ["(function (){\n  return 1 + 2 + 3 + 4;\n})()"
      {"return" "number", "value" 10, "type" "data"}
      10]

  (rt-oneshot :python)
  => [(std.string/|
       "def OUT_FN():"
       "  import traceback"
       "  err = None"
       "  try:"
       "    return 1 + 2 + 3 + 4"
       "  except Exception:"
       "    err = traceback.format_exc()"
       "  raise Exception(err)"
       "globals()[\"OUT\"] = OUT_FN()")
      {"key" nil, "id" nil, "value" 10, "type" "data"}
      10])

(comment
  (./import)
  (./create-tests))
