(ns std.lang.model.spec-js.qml
  (:require [std.lang.base.emit-common :as common]
            [std.lang.base.util :as ut]
            [std.lib.collection]
            [std.lib.foundation]
            [std.string.common]))

;;
;; nodes start with :qml/<NAME>, else will be inlined as a statement
;; - var gets compiled to `property`
;; - fn gets compiled to `function`
;; - do block on properties is a js block
;; - array on properties is an array
;;

(defn qml-props?
  "checks if data is a qml prop"
  {:added "4.0"}
  [item]
  (or (map? item)
      (and (set? item)
           (= 1 (count item))
           (vector? (first item)))))

(defn qml-container?
  "checks if item is a qml container"
  {:added "4.0"}
  [item]
  (and (vector? item)
       (keyword? (first item))
       (= "qml" (namespace (first item)))))

(defn classify-props
  "classifies props"
  {:added "4.0"}
  [props classify-fn]
  (let [pairs (cond (map? props)
                    props

                    (set? props)
                    (->> (first props)
                         (partition 2))

                    :else (std.lib.foundation/error "Not Allowed" {:input props}))]
    (mapv (fn [[k prop]]
            [k (classify-fn prop)])
          pairs)))

(defn classify-container
  "classifies a container"
  {:added "4.0"}
  [[tag props & children] classify-fn]
  (let [title (if (= "qml" (namespace tag))
                (name tag)
                (std.lib.foundation/error "Not a valid qml tag: " tag))
        [props children] (if (qml-props? props)
                           [props children]
                           [{} children])]
    {:type  :qml/container
     :title title
     :props (classify-props props classify-fn)
     :children (mapv classify-fn children)}))

(defn classify
  "classifies the data structure"
  {:added "4.0"}
  [val]
  (if (qml-container? val)
    (classify-container val classify)
    {:type :qml/value
     :value val}))


;;
;; emit script
;;

(declare emit-node)

(defn emit-value
  "emits a value string"
  {:added "4.0"}
  [{:keys [value]} grammar mopts]
  (cond (std.lib.collection/form? value)
        (case (first value)
          %  (str " {"
                  (common/with-indent [2]
                    (str (common/newline-indent)
                         (common/*emit-fn* (cons 'do (rest value))
                                           grammar
                                           mopts)))
                  (common/newline-indent)
                  "}")
          var (common/*emit-fn* (cons 'property (rest value))
                                (assoc-in grammar [:default :common :assign] ":")
                                mopts)
          fn  (common/*emit-fn* (cons 'fn.inner (rest value))
                                grammar
                                mopts)
          (common/*emit-fn* value
                            grammar
                            mopts))
        
        :else
        (common/*emit-fn* value
                          grammar
                          mopts)))

(defn emit-container
  "emits a container string"
  {:added "4.0"}
  [{:keys [title props children]} grammar mopts]
  (str title " {"
       (common/with-indent [2]
         (str (common/newline-indent)
              (->> (concat (mapv (fn [[k node]]
                                   (str (std.lib.foundation/strn k) ": " (emit-node node grammar mopts)))
                                 props)
                           (mapv #(emit-node %  grammar mopts) children))
                   (std.string.common/join (common/newline-indent)))))
       (common/newline-indent)
       "}"))

(defn emit-node
  "emits either container or value string"
  {:added "4.0"}
  [{:keys [type value] :as node} grammar mopts]
  (case type
    :qml/container (emit-container node grammar mopts)
    :qml/value (emit-value node grammar mopts)))

(defn emit-qml
  "emits a qml string
 
   (l/with:emit
    (qml/emit-qml [:qml/Window #{[:a 1 :b [:qml/Item]]}
                   '(fn hello [] (+ 1 2))]
                  js/+grammar+
                  {}))
   (std.string/|
   \"Window {\"
    \"  a: 1\"
    \"  b: Item {\"
    \"    \"
    \"  }\"
    \"  hello(){\"
    \"    1 + 2;\"
    \"  }\"
    \"}\")"
  {:added "4.0"}
  [form grammar mopts]
  (let [tree (classify form)]
    (emit-node tree grammar mopts)))

(comment
  (./import))


