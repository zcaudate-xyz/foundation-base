(ns js.lib.rn-async-storage-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[js.lib.rn-async-storage :as store]
             [js.core :as j]
             [xt.lang.base-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :js)
             (notify/wait-on :js
               (:= (!:G window)  (require "window"))
               (:= LocalStorage  (. (require "node-localstorage")
                                    LocalStorage))
               (j/assign (!:G window)
                         {:localStorage (new LocalStorage ".localstorage")})
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
