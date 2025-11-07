(ns std.block.layout.bind
  (:require [std.block.layout.common :as common]
            [std.block.layout.estimate :as est]
            [std.block.construct :as construct]
            [std.string :as str]
            [std.lib :as h]))

(defn layout-spec-fn
  [form _]
  
  (cond (list? form)
        (cond (= 'let (first form))
              ())))

(defn layout-transform-fn
  [form]
  form)

(defn layout-default-fn
  [form opts]
  (let [multiline? (est/estimate-multiline form {})
        {:keys [metadata
                spec]
         :or {spec {}}} (if (coll? form)
                          (meta form))
        nopts (assoc opts :spec spec)]
    (cond (not multiline?) (construct/block form)

          (map? form) (common/layout-multiline-hashmap form nopts)

          (set? form) (common/layout-multiline-hashset form nopts)
          
          (vector? form) (common/layout-multiline-vector form nopts)

          (list? form) (common/layout-multiline-custom form nopts)

          :else (construct/block form))))


(comment
  (h/p 
   (layout-main '[{:keys [col-align
                          columns]
                   :as spec}  (merge {:columns 2
                                      :col-align false}
                                     spec)]
                {}))
  (h/p 
   (layout-main '[[{:keys [col-align
                           columns]
                    :as spec}  (merge {:a {:columns 2
                                           :col-align false}
                                       :b {:columns 2
                                           :col-align false}}
                                      spec)]]
                {}))
  
  (h/p 
   (layout-main '[[[[[{:keys [col-align
                              columns]
                       :as spec}  (merge {:columns 2
                                          :col-align false}
                                         spec)]]]]]
                {}))
  
  (h/p 
   (layout-main '(let [allowable   {:allowable 1 :b 2}
                       b   {:a 1 :botherable 2}]
                   (+ a 2))
                {}))
  
  (h/p
   (layout-main '(let ^{:spec {:col-align true}}
                     [{:keys [col-align
                              columns]
                       :as spec}  (merge {:columns 2
                                          :col-align false}
                                         spec)])
                {}))

  (h/p
   (layout-main '(let ^{:spec {:col-align true}}
                     [{:keys [col-align
                                columns]
                         :as spec}  (merge {:columns 2
                                            :col-align false}
                         spec)])
                {})))


;;
;; layout loop
;;

(defn layout-main-loop
  "emits the raw string"
  {:added "4.0"}
  ([form opts]
   (layout-main-loop form opts layout-default-fn))
  ([form {:keys [indents]
          :or {indents 0}
          :as opts}
    default-fn]
   (default-fn form  opts)))

(defn layout-main
  "emits a string based on grammar"
  {:added "4.0"}
  [form & [opts]]
  (binding [common/*layout-fn* layout-main-loop]
    (layout-main-loop form opts)))
