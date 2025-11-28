

(comment
  (h/p
   (layout-main
    '[:p {:hello world
          :hello1 world}
      [:% -/hoeuoeu
       {:hello world
        :hello1 world}]
      [:% -/hoeuoeu
       {:a 1 :b 2}]
      [:a 
       [:y {:hello world
            :hello1 world}
        [:hello]]]]
    
    ))
  
  
  (h/p
   (layout-main
    '(case a
       (:text :hold :bull :bilu) (oeuo)
       2 (oeuoeuoeu)
       oeuoeu)))

  (h/p
   (layout-main
    '(cond-> a
       pred (oeueo :assoc ue)
       2 (oeuoeuoeu))))
  
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
     (#^{:spec {:col-break true}}
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
     (#^{:spec {:col-break true}}
      ((apply aonther
              t
              t
              oeuo)
       (let [^{:spec {:col-align true
                      :columns 1}}
             #{a (let [a 1 b 2]
                   (+ a b))
               (let [a 1 b 2]
                 (+ a b c))
               2}
             (let [a 1 b 2]
               (+ a b))]
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
           (+ a b))))))))))



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
