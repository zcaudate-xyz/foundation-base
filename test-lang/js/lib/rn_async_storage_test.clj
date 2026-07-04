(ns js.lib.rn-async-storage-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.lib.rn-async-storage :as store]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]]})

(fact:global
  {:setup    [(l/rt:restart)
              (l/rt:scaffold :js)
              (notify/wait-on :js
                (:= (!:G window)  (require "window"))
                (var LocalStorageCtor
                     (. (require "node-localstorage")
                        LocalStorage))
                (xtd/obj-assign (!:G window)
                                {:localStorage (new LocalStorageCtor
                                                   ".localstorage")})
                (repl/notify true))]
   :teardown [(l/rt:stop)]})


^{:refer js.lib.rn-async-storage/getJSON :added "4.0" :unchecked true
  :setup [(notify/wait-on :js
            (. (store/clear)
               (then (fn [result]
                       (repl/notify result)))))]}
(fact "gets the json data structure"

  (notify/wait-on :js
    (. (store/setJSON "hello" {:a 1})
       (then (fn [result]
               (repl/notify result)))))
  => nil

  (notify/wait-on :js
    (. (store/getJSON "hello")
       (then (fn [result]
               (repl/notify result)))))
  => {"a" 1})

^{:refer js.lib.rn-async-storage/setJSON :added "4.0" :unchecked true}
(fact "sets the json structure")

^{:refer js.lib.rn-async-storage/mergeJSON :added "4.0" :unchecked true
  :setup [(notify/wait-on :js
            (. (store/setJSON "hello" {:a 1})
               (then (fn [result]
                       (repl/notify result)))))]}
(fact "merges json data on the same key"

  (notify/wait-on :js
    (. (store/mergeJSON "hello" {:b 2})
       (then (fn [result]
               (repl/notify result)))))
  => nil

  (notify/wait-on :js
    (. (store/getJSON "hello")
       (then (fn [result]
               (repl/notify result)))))
  => {"a" 1, "b" 2})
