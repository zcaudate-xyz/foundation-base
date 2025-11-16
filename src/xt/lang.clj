(ns xt.lang
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require  [[xt.lang.base-macro :as macro]
              [xt.lang.base-iter :as iter :dynamic true]
              [xt.lang.base-repl :as repl :dynamic true]
              [xt.lang.base-lib :as lib   :dynamic true]]
   :header   {:dynamic  true
              :override {:js  xt.lang.override.custom-js
                         :lua xt.lang.override.custom-lua}}})






(comment

  
  (defn intern-macros [lang ns & [library merge-op]]
    (let [library (or library (l/default-library))
          [to from] (if (vector? ns)
                      ns
                      [(h/ns-sym) ns])]
      (std.lang.base.library/wait-mutate!
       library
       (fn [snap]
         (let [book (std.lang.base.library-snapshot/get-book snap lang)
               imports (->> (get-in book [:modules from :fragment])
                            (h/map-vals (fn [e]
                                          (assoc e :module to))))
               new-book (update-in book [:modules to :fragment]
                                   (fn [m]
                                     (doto ((or merge-op (fn [_ new] new))
                                            m imports)
                                       (->> (vreset! out)))))]
           [imports (std.lang.base.library-snapshot/add-book snap new-book)])))))

  (intern-macros :xtalk 'xt.lang.base-macro)

  (def +book+ (l/get-book (l/default-library)
                          :xtalk))

  (def +imports+
    (h/map-vals
     (fn [e]
       (assoc e :module 'xt.lang))
     (get-in +book+ [:modules 'xt.lang.base-macro :fragment])))

  (def +new-book+
    (assoc-in +book+
              [:modules 'xt.lang :fragment]
              +imports+))


  (comment
    (l/link-macros   'xt.lang.base-macro)
    (l/link-all      'xt.lang.base-lib))



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
