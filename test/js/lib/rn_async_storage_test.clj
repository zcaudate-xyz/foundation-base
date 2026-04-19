(ns js.lib.rn-async-storage-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.lib.rn-async-storage :as store]
             [js.core :as j]
             [xt.lang.common-repl :as repl]]})

(fact:global
  {:setup    [(l/rt:restart)
              (l/rt:scaffold :js)
              (notify/wait-on :js
                (:= (!:G window)  (require "window"))
                (var LocalStorageCtor
                     (. (require "node-localstorage")
                        LocalStorage))
                (j/assign (!:G window)
                          {:localStorage (new LocalStorageCtor
                                             ".localstorage")})
                (repl/notify true))]
   :teardown [(l/rt:stop)]})


^{:refer js.lib.rn-async-storage/getJSON :added "4.0" :unchecked true
  :setup [(j/<! (store/clear))]}
(fact "gets the json data structure"
  ^:hidden
  
  (j/<! (store/setJSON "hello" {:a 1}))
  => nil
  
  (j/<! (store/getJSON "hello"))
  => {"a" 1})

^{:refer js.lib.rn-async-storage/setJSON :added "4.0" :unchecked true}
(fact "sets the json structure")

^{:refer js.lib.rn-async-storage/mergeJSON :added "4.0" :unchecked true
  :setup [(j/<! (store/setJSON "hello" {:a 1}))]}
(fact "merges json data on the same key"
  ^:hidden

  (j/<! (store/mergeJSON "hello" {:b 2}))
  => nil

  (j/<! (store/getJSON "hello"))
  => {"a" 1, "b" 2})
