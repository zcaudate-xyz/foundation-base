(ns xt.lang
  (:require [std.lang :as l]
            [std.lib.collection :as collection]
            [std.lib.env :as env]))

(l/script :xtalk
  {:require  [[xt.lang.common-spec :as macro]
              [xt.lang.common-iter :as iter :dynamic true]
              [xt.lang.common-repl :as repl :dynamic true]
              [xt.lang.common-lib :as lib :dynamic true]]
   :header   {:dynamic  true
              :override {:js  xt.lang.override.custom-js
                         :lua xt.lang.override.custom-lua}}})






(comment

  
  (defn intern-macros [lang ns & [library merge-op]]
    (let [library (or library (l/default-library))
          [to from] (if (vector? ns)
                      ns
                      [(env/ns-sym) ns])]
      (std.lang.base.library/wait-mutate!
       library
       (fn [snap]
         (let [book (std.lang.base.library-snapshot/get-book snap lang)
               imports (->> (get-in book [:modules from :fragment])
                            (collection/map-vals (fn [e]
                                          (assoc e :module to))))
               new-book (update-in book [:modules to :fragment]
                                   (fn [m]
                                     (doto ((or merge-op (fn [_ new] new))
                                            m imports)
                                       (->> (vreset! out)))))]
           [imports (std.lang.base.library-snapshot/add-book snap new-book)])))))

  (intern-macros :xtalk 'xt.lang.common-spec)

  (def +book+ (l/get-book (l/default-library)
                          :xtalk))

  (def +imports+
    (collection/map-vals
     (fn [e]
       (assoc e :module 'xt.lang))
     (get-in +book+ [:modules 'xt.lang.common-spec :fragment])))

  (def +new-book+
    (assoc-in +book+
              [:modules 'xt.lang :fragment]
              +imports+))


  (comment
    (l/link-macros   'xt.lang.common-spec)
    (l/link-all      'xt.lang.common-lib))



  (defn.xt hello
    []
    (return (+ 1 2 )))
  
  (comment
    [xt.lang - mutable -> runtime
     std.lib - language -> tooling
     std.lang - tooling -> spec
     ]
    
    (./create-tests)
    (l/rt:module-purge :xtalk))

  )
