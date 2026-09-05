(ns lang.core-test
  (:require [lang.core :as l]
            [lang.common.book :as book]
            [lang.common.book-meta :as meta]
            [lang.base.emit-helper :as helper]
            [lang.base.grammar :as grammar]
            [lang.core.impl-entry :as entry]
            [lang.core.library :as lib]
            [lang.core.library-snapshot :as snap]
            [lang.core.pointer :as ptr]
            [lang.core.runtime :as rt]
            [lang.base.util :as ut]
            [std.lib.context.space :as space]
            [std.lib.template :as template])
  (:use code.test))

(def +book+
  (book/book {:lang :lua
              :meta (meta/book-meta {:module-export  (fn [{:keys [as]} opts]
                                                       (template/$ (return ~as)))
                                     :module-import  (fn [name {:keys [as]} opts]
                                                       (template/$ (var ~as := (require ~(str name)))))
                                     :has-ptr        (fn [ptr]
                                                       (list 'not= (ut/sym-full ptr) nil))
                                     :teardown-ptr   (fn [ptr]
                                                       (list := (ut/sym-full ptr) nil))})
              :grammar (grammar/grammar :lua
                         (grammar/build)
                         helper/+default+)}))

(def +library-ext+
  (doto (lib/library:create
         {:snapshot (snap/snapshot {:lua {:id :lua
                                          :book +book+}})})
    (lib/install-module! :lua 'L.core {})
    (lib/install-module! :lua 'L.util
                         {:require '[[L.core :as u]]
                          :import '[["cjson" :as cjson]]})
    (lib/add-entry-single!
     (entry/create-macro
      '(defmacro add [x y] `(+ ~x ~y))
      {:lang :lua
       :namespace 'L.core
       :module 'L.core}))))

(def +ptr+
  (ut/lang-pointer :lua
                   {:module 'L.core
                    :id 'add
                    :section :fragment
                    :library +library-ext+}))

(fact "CANARY test"
  (l/with:print [] (l/emit-as :lua '[(:= a 1)]))
  => "a = 1")

^{:refer lang.core/rt:space :added "4.1"}
(fact "rt:space"
  (with-redefs [space/space (fn [ns]
                              [:space ns])
                ut/lang-context (fn [lang]
                                  [:context lang])
                std.lib.context.space/space:rt-get (fn [space ctx]
                                                     [space ctx])]
    (l/rt:space :lua 'hello.core))
  => [[:space 'hello.core]
      [:context :lua]])

^{:refer lang.core/get-entry :added "4.1"}
(fact "get-entry"
  (l/get-entry +ptr+) => book/book-entry?)

^{:refer lang.core/as-lua :added "4.1"}
(fact "as-lua"
  (l/as-lua []) => []
  (l/as-lua [1 2 [] 4]) => [1 2 [] 4]
  (l/as-lua [1 nil 2 nil]) => [1 nil 2]
  (l/as-lua {:a [] :b nil}) => {:a []})

^{:refer lang.core/rt:invoke :added "4.0"}
(fact "rt:invoke"
  (with-redefs [ut/lang-rt (fn [ns lang]
                              [ns lang])
                l/ptr (fn [lang m]
                        [lang m])
                std.lib.context.pointer/rt-invoke-ptr (fn [rt ptr code]
                                                        [rt ptr code])]
    (l/rt:invoke 'hello.core :lua '(+ 1 2)))
  => [['hello.core :lua]
      [:lua {:module 'hello.core}]
      '(+ 1 2)])

^{:refer lang.core/force-reload :added "4.1"}
(fact "force-reload"
  (let [purged (atom [])]
    (with-redefs [l/default-library (fn []
                                      :library)
                  l/get-book (fn [_ _]
                               :book)
                  std.lib.deps/deps-ordered (fn [_ _]
                                              [])
                  l/lib:purge (fn [ns]
                                (swap! purged conj ns))]
      [(l/force-reload 'hello.core :lua)
       @purged]))
  => [nil []])
