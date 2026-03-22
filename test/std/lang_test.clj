(ns std.lang-test
  (:require [std.lang :as l]
            [std.lang.base.book :as book]
            [std.lang.base.book-meta :as meta]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.impl-entry :as entry]
            [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.runtime :as rt]
            [std.lang.base.util :as ut]
            [std.lib.context.space :as space]
            [std.lib.template])
  (:use code.test))

(def +book+
  (book/book {:lang :lua
              :meta (meta/book-meta {:module-export  (fn [{:keys [as]} opts]
                                                       (std.lib.template/$ (return ~as)))
                                     :module-import  (fn [name {:keys [as]} opts]
                                                       (std.lib.template/$ (var ~as := (require ~(str name)))))
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
  ^:hidden
  (l/with:print [] (l/emit-as :lua '[(:= a 1)]))
  => "a = 1")

^{:refer std.lang/rt:invoke :added "4.0"}
(fact "rt:invoke"
  "difficult to test without dependency injection")

^{:refer std.lang/rt:space :added "4.1"}
(fact "rt:space"
  "difficult to test due to test runner's context isolation")

^{:refer std.lang/get-entry :added "4.1"}
(fact "get-entry"
  (l/get-entry +ptr+) => book/book-entry?)

^{:refer std.lang/as-lua :added "4.1"}
(fact "as-lua"
  (l/as-lua []) => {}
  (l/as-lua [1 2 [] 4]) => [1 2 {} 4]
  (l/as-lua {:a []}) => {:a {}})

^{:refer std.lang/force-reload :added "4.1"}
(fact "force-reload"
  "difficult to test without dependency injection")
