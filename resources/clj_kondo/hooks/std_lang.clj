(ns clj-kondo.hooks.hara.lang
  (:require [clj-kondo.hooks-api :as api]))



(defn script-hook
  [call]
  (let [children (:children call)
        config-map-node (nth children 2)
        config-map      (some-> config-map-node api/sexpr)]
    (if (and (map? config-map)
             (contains? config-map :require))
      ;; We have a map containing :require. Let's build a do block with multiple requires.
      (let [reqs (:require config-map)]  
        ;; 'reqs' is presumably a vector of libspecs, e.g. [[js.core :as j] [js.react :as r] ...]
        (api/list-node
          (cons (api/token-node 'do)
                (for [libspec reqs]
                  ;; Each 'libspec' is typically `[some.ns :as alias ...]` or a bare symbol `some.ns`.
                  ;; We'll extract the first item of the vector as the real namespace symbol.
                  (let [ns-sym (if (vector? libspec)
                                 (first libspec)
                                 libspec)]
                    (api/list-node
                      [(api/token-node 'require)
                       (api/list-node
                         [(api/token-node 'quote)
                          (api/token-node ns-sym)])])))))))
      ;; Otherwise, if there's no map or no :require, just return (do) so we do nothing
      (api/list-node [(api/token-node 'do)]))))

(comment
  (api/sexpr (api/coerce '(def a 1)))
  
  (api/token-node 'a/b)
  (into {}
        (api/list-node [(api/token-node 'a)
                        (api/token-node 'b)
                        (api/token-node 'c)])))
