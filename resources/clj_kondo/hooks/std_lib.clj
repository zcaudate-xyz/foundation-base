(ns clj-kondo.hooks.std-lib
  (:require [clj-kondo.hooks-api :as api]))

(defn intern-in-hook
  [call]
  (let [[_ & inputs] (:children call)  ; skip macro symbol itself
        inputs  (map api/sexpr inputs)
        syms    (filter symbol? inputs)
        vecs    (filter vector? inputs)]
    (api/list-node
     (concat
      [(api/token-node 'do)]
      (for [s syms]
        (api/list-node [(api/token-node 'def)
                        (api/token-node (symbol (name s)))
                        (api/token-node s)]))
      (for [v vecs]
        (api/list-node [(api/token-node 'def)
                        (api/token-node (first v))
                        (api/token-node (second v))]))))))

(defn intern-all-hook
  [call]
  (let [[_ & inputs] (:children call)   ; skip macro symbol itself
        inputs  (map api/sexpr inputs)
        syms    (filter symbol? inputs)]
    (api/list-node
     (concat
      [(api/token-node 'do)]
      (for [s syms]
        (api/list-node [(api/token-node 'use)
                        (api/token-node s)]))))))

(comment
  (api/sexpr (api/coerce '(def a 1)))
  
  (api/token-node 'a/b)
  (into {}
        (api/list-node [(api/token-node 'a)
                        (api/token-node 'b)
                        (api/token-node 'c)])))
