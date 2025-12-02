(ns test-manage-doc
  (:require [code.doc.link.manage :refer [link-manage]]
            [code.manage :as manage]
            [std.lib :as h]))

(defn test-link-manage []
  (let [interim {:articles {:test {:elements [{:type :manage
                                               :task 'missing
                                               :args [['code.doc] {:print {:result false :summary false} :return :summary}]}]}}}
        result (link-manage interim :test)
        elements (get-in result [:articles :test :elements])]
    (println "Elements:" elements)
    (if (and (= 1 (count elements))
             (= :block (:type (first elements)))
             (string? (:code (first elements))))
      (println "SUCCESS: Link manage replaced element with block.")
      (println "FAILURE: Element not replaced correctly."))))

(test-link-manage)
