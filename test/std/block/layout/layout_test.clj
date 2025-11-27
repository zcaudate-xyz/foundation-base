(ns std.block.layout-test
  (:use code.test)
  (:require [std.block.layout :as bind]
            [std.block.construct :as construct]
            [std.lib :as h]))

^{:refer std.block.layout/layout-hiccup-like :added "4.0"}
(fact "checks if form is hiccup structure"
  ^:hidden
  
  (bind/layout-hiccup-like [:a :b :c])
  => false

  (bind/layout-hiccup-like [:a [:b [:c]]])
  => true

  (bind/layout-hiccup-like [:a {:x 1 :y 2}
                            [:b [:c]]])
  => true
  
  (bind/layout-hiccup-like '[:% hello [:b [:c]]])
  => true

  (bind/layout-hiccup-like '[:<>
                             [:b [:c]]
                             [:b [:c]]])
  => true)

^{:refer std.block.layout/layout-spec-fn :added "4.0"}
(fact "gets the layout spec"
  ^:hidden
  
  (bind/layout-spec-fn '(assoc m :a 1 :b 2) true)
  => {:columns 2, :col-from 1 :col-call true})

^{:refer std.block.layout/layout-annotate-arglist :added "4.0"}
(fact "adds layout metadat to arglists"
  ^:hidden

  (binding [*print-meta* true]
    (pr-str (bind/layout-annotate-arglist
             '[{:keys [a b c d e]
                :as other}])))
  => "[^{:readable-len 10} {:keys ^{:tag :vector, :readable-len 10, :spec {:columns 1}} [a b c d e], :as other}]")

^{:refer std.block.layout/layout-annotate-bindings :added "4.0"}
(fact"adds layout metadata to bindings"
  ^:hidden

  (binding [*print-meta* true]
    (pr-str (bind/layout-annotate-bindings
             '[{:keys [a b c d e]
                :as other} {:a 1 :b 2}])))
  => "[^{:readable-len 10} {:keys ^{:tag :vector, :readable-len 10, :spec {:columns 1}} [a b c d e], :as other} {:a 1, :b 2}]")

^{:refer std.block.layout/layout-annotate-fn-named :added "4.0"}
(fact "adds layout metadata to named functions"
  ^:hidden
  
  (binding [*print-meta* true]
    (pr-str (bind/layout-annotate-fn-named
             '(defn hello
                ([{:keys [a b c d e]
                   :as other}])))))
  => "(defn hello ([^{:readable-len 10} {:keys ^{:tag :vector, :readable-len 10, :spec {:columns 1}} [a b c d e], :as other}]))"

  (binding [*print-meta* true]
    (pr-str (bind/layout-annotate-fn-named
             '(defn hello
                "docstring"
                {:a 1}
                [{:keys [a b c d e]
                    :as other}]))))
  => "(defn hello \"docstring\" {:a 1} [^{:readable-len 10} {:keys ^{:tag :vector, :readable-len 10, :spec {:columns 1}} [a b c d e], :as other}])")

^{:refer std.block.layout/layout-annotate-fn-anon :added "4.0"}
(fact "adds layout metadata to `fn` calls"
  ^:hidden
  
  (binding [*print-meta* true]
    (pr-str (bind/layout-annotate-fn-anon
             '(fn [{:keys [a b c d e]
                    :as other}]))))
  => "(fn [^{:readable-len 10} {:keys ^{:tag :vector, :readable-len 10, :spec {:columns 1}} [a b c d e], :as other}])")

^{:refer std.block.layout/layout-annotate :added "4.0"}
(fact "adds metadata annotation to form"
  ^:hidden
  
  (binding [*print-meta* true]
    (pr-str (bind/layout-annotate '(let [{:keys [a b c d e]
                                          :as other}
                                         b 2]))))
  => "(let [^{:readable-len 10} {:keys ^{:tag :vector, :readable-len 10, :spec {:columns 1}} [a b c d e], :as other} b])")

^{:refer std.block.layout/layout-default-fn :added "4.0"}
(fact "the default function for level 1 transformation"

  (construct/rep
   (bind/layout-default-fn [1 2 3] {}))
  => [1 2 3])

^{:refer std.block.layout/layout-main :added "4.0"}
(fact "performs the main layout"
  ^:hidden

  (construct/get-lines
   (bind/layout-main '(+ 1 2 3)))
  => ["(+ 1 2 3)"]
  

  (construct/get-lines
   (bind/layout-main '(let [a   {:a 1 :b 2}
                            b   {:a 1 :b 2}]
                        (+ a 2))
                     ))
  => ["(let [a {:a 1 :b 2}"
      "      b {:a 1 :b 2}]"
      "  (+ a 2))"]


  (construct/get-lines
   (bind/layout-main '(let [allowable   {:allowable 1 :b 2}
                            b   {:a 1 :botherable 2}]
                        (+ a 2))))
  => ["(let [allowable {:allowable 1 :b 2}"
      "      b         {:a 1 :botherable 2}]"
      "  (+ a 2))"]
  
  (construct/get-lines
   (bind/layout-main '(assoc m :key1 val1 :key2 val2
                             :key3 (+ a 2))))
  => ["(assoc m"
      "       :key1 val1"
      "       :key2 val2"
      "       :key3 (+ a 2))"]

  (construct/get-lines
   (bind/layout-main '(hash-map :key1 val1 :key2 val2
                                :key3 (+ a 2))))
  => ["(hash-map :key1 val1"
      "          :key2 val2"
      "          :key3 (+ a 2))"]


  (construct/get-lines
   (bind/layout-main '{:a1 {:b1-data-long0 1
                            :b1-data-long1 2}
                       :a2 {:b2-data-long0 3
                            :b2-data-long1 4}}))
  => ["{:a1 {:b1-data-long0 1"
      "      :b1-data-long1 2}"
      " :a2 {:b2-data-long0 3"
      "      :b2-data-long1 4}}"]

  (construct/get-lines
   (bind/layout-main '[[{:a1 {:b1-data-long0 1
                              :b1-data-long1 2}
                         :a2-long {:b2-data-long0 {:c2-data-long0 6
                                                   :c2-data 5}
                                   :b2-data-long1 4}}]]))
  => ["[[{:a1 {:b1-data-long0 1"
      "        :b1-data-long1 2}"
      "   :a2-long {:b2-data-long0 {:c2-data 5 :c2-data-long0 6}"
      "             :b2-data-long1 4}}]]"]
  

  (construct/get-lines
   (bind/layout-main
    '(let [foo-bind     [1 2 3]
           {:keys [a ab abc]
            :as data}   (merge {:a1 {:b1-data 1
                                     :b1-data-long1 2}
                                :a2 {:b2 3
                                     :b2-long1 4}}
                               spec)
           foo-bind     [1 2 3]]
       (+ 1 2 3))))
  => ["(let [foo-bind          [1 2 3]"
      "      {:keys [a ab abc]"
      "       :as data}        (merge"
      "                         {:a1 {:b1-data 1 :b1-data-long1 2}"
      "                          :a2 {:b2 3 :b2-long1 4}}"
      "                         spec)"
      "      foo-bind          [1 2 3]]"
      "  (+ 1 2 3))"]
  
  (construct/get-lines
   (bind/layout-main
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
                                            arg-blocks))))))))
  => ["(defn layout-with-bindings"
      "  \"layout with bindings\""
      "  {:added \"4.0\"}"
      "  ([form {:keys [indents]"
      "          :or {indents 0}"
      "          :as opts}]"
      "   (let [[start-sym"
      "          nindents]   (layout-multiline-form-setup form"
      "                                                   opts)"
      "         start-blocks (list start-sym"
      "                            (construct/space))"
      "         bopts        (assoc opts"
      "                             :spec    {:col-align true :columns 2}"
      "                             :indents nindents)"
      "         bindings     (*layout-fn* (second form)"
      "                                   bopts)"
      "         aopts        (assoc opts"
      "                             :indents (+ 1 indents))"
      "         arg-spacing  (concat"
      "                       [(construct/newline)]"
      "                       (repeat (+ 1 indents)"
      "                               (construct/space)))"
      "         arg-blocks   (->>"
      "                       (drop 2 form)"
      "                       (map"
      "                        (fn [arg]"
      "                          (*layout-fn* arg aopts)))"
      "                       (join-blocks arg-spacing))]"
      "     (construct/container"
      "      :list"
      "      (vec"
      "       (concat start-blocks"
      "               [bindings]"
      "               arg-spacing"
      "               arg-blocks))))))"])


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


^{:refer std.block.layout/layout-annotate-svg-path :added "4.0"}
(fact "parses the d string in svg path for better formatting"

  (bind/layout-annotate-svg-path [:path {:d "M 10 10 L 20 20"}])
  => (contains [:path (contains {:d ["M" "10" "10" "L" "20" "20"]})]))
