(ns std.block.layout.bind
  (:require [std.block.layout.common :as common]
            [std.block.layout.estimate :as est]
            [std.block.construct :as construct]
            [std.string :as str]
            [std.lib :as h]))

(defn layout-spec-fn
  [form is-multiline]
  (if is-multiline
    (cond (list? form)
          (cond (= 'let (first form))
                {:col-from 1
                 :col-start 2}))))

(defn layout-transform-fn
  [form]
  form)

(defn layout-default-fn
  [form opts]
  (let [is-multiline (est/estimate-multiline form {})
        {:keys [metadata
                spec]}  (if (coll? form)
                          (meta form))
        spec  (or spec (layout-spec-fn form is-multiline))
        nopts (assoc opts :spec spec)]
    (cond (not is-multiline) (construct/block form)

          (map? form) (common/layout-multiline-hashmap form nopts)

          (set? form) (common/layout-multiline-hashset form nopts)
          
          (vector? form) (common/layout-multiline-vector form nopts)

          (list? form) (common/layout-multiline-custom form nopts)
          
          :else (construct/block form))))


(comment

  (h/p
   (layout-main
    '(let [a 1 b 2]
       (+ a ))))
  
  (h/p
   (layout-main
    '((form {:keys [indents]
             :or {indents 0}
             :as opts}))))
  
  (h/p
   (layout-main
    '(form {:keys [indents]
            :or {indents 0}
            :as opts})))
  
  (h/p
   (layout-main
    '((((form {:keys [indents]
               :or {indents 0}
               :as opts}))))))
  
  (h/p
   (layout-main
    '(form {:keys [indents]
            :or {indents 0}
            :as opts})))
  
  (h/p
   (layout-main
    '(([form {:keys [indents]
              :or {indents 0}
              :as opts}]))))
  
  (h/p
   (layout-main
    '[[form hello {:keys [indents]
                   :or {indents 0}
                   :as opts}]]))
  
  (h/p
   (layout-main
    '(({:keys [indents]
        :or {indents 0}
        :as opts}))))
  
  (h/p
   (layout-main
    
    (quote
     (#^{:spec {:col-compact false
                :col-break true}}
      ((apply aonther
              t
              t
              oeuo)
       (let [a (let [a 1 b 2]
                 (+ a b))
             (let [a 1 b 2]
               (+ a b))
             2]
         (+ a b))
       (let [a 1 b 2]
         (+ a b)))))))
  
  (h/p
   (layout-main
    
    (quote
     (#^{:spec {:col-compact true
                :col-break true}}
      ((((apply aonther
                t
                t
                oeuo)
         (let [a (let [a 1 b 2]
                   (+ a b)) b 2]
           (+ a b))
         (let [a 1 b 2]
           (+ a b)))))))))
  
  (h/p
   (layout-main
    '([form {:keys [indents]
             :as opts
             :or {indents 0}}]
      (let [[start-sym nindents] (layout-multiline-form-setup form opts)
            start-blocks (list start-sym (construct/space))
            bopts        (assoc opts
                                :spec   {:col-align true
                                         :columns 2}
                                :indents nindents)
            bindings     (*layout-fn* (second form)
                                      bopts)
            aopts        (assoc opts :indents ( + 1 indents))
            arg-spacing  (concat  [(construct/newline)]
                                  (repeat (+ 1 indents) (construct/space)))
            arg-blocks   (->> (drop 2 form)
                              (map (fn [arg]
                                     (*layout-fn* arg aopts)))
                              (join-blocks arg-spacing))]
        (construct/container :list
                             (vec (concat start-blocks
                                          [bindings]
                                          arg-spacing
                                          arg-blocks)))))))
  
  (h/p
   (layout-main
    '(defn layout-with-bindings
       "layout with bindings"
       {:added "4.0"}
       ([form {:keys [indents]
               :or {indents 0}
               :as opts}]
        (let [[start-sym nindents] (layout-multiline-form-setup form opts)
              start-blocks (list start-sym (construct/space))
              bopts        (assoc opts
                                  :spec   {:col-align true
                                           :columns 2}
                                  :indents nindents)
              bindings     (*layout-fn* (second form)
                                        bopts)
              aopts        (assoc opts :indents ( + 1 indents))
              arg-spacing  (concat  [(construct/newline)]
                                    (repeat (+ 1 indents) (construct/space)))
              arg-blocks   (->> (drop 2 form)
                                (map (fn [arg]
                                       (*layout-fn* arg aopts)))
                                (join-blocks arg-spacing))]
          (construct/container :list
                               (vec (concat start-blocks
                                            [bindings]
                                            arg-spacing
                                            arg-blocks)))))))))

(comment
  (h/p 
   (layout-main '[{:keys [col-align
                          columns]
                   :as spec}  (merge {:columns 2
                                      :col-align false}
                   spec)]
                {}))

  (h/p 
   (layout-main '^{:spec {:col-compact true}}
                [{:keys [col-align
                         columns]
                  :as spec}  (merge {:columns 2
                                     :col-align false}
                  spec)]
                ))
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
