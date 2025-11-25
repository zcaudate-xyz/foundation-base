(ns std.block.scheme-demo-block
  (:require [std.block.scheme :as scheme]
            [std.block.construct :as construct]
            [std.block.base :as base]))

(defn test-construct-block []
  (println "\n=== Constructed Block Test ===")
  ;; Construct: (+ 10 (* 2 5))
  (let [block (construct/block
               ['+ 10 (construct/block ['* 2 5])])]
    (println "Constructed Block String:" (base/block-string block))
    (scheme/visualize-run block)))

(defn -main []
  (test-construct-block))
