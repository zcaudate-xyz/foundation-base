(ns std.block.layout.common
  (:require [std.lib.foundation :as h]
            [std.lib.collection :as c]
            [std.block.construct :as construct]
            [std.block.check :as check]
            [std.block.type :as type]
            [std.block.base :as base]
            [std.string :as str]))

(def ^:dynamic *layout-fn*
  (fn [form _]
    (construct/block form)))

(defn join-blocks
  "joins blocks together with a spacing array"
  {:added "4.0"}
  [spacing blocks]
  (if (empty? blocks)
    ()
    (reduce (fn [output block]
              (concat output spacing [block]))
            [(first blocks)]
            (rest blocks))))

(defn join-block-arrays
  "joins block-arrays together with a spacing array"
  {:added "4.0"}
  [spacing block-arrays]
  (if (empty? block-arrays)
    ()
    (reduce (fn [output array]
              (concat output spacing array))
            (first block-arrays)
            (rest block-arrays))))

(defn format-multiline-string
  "makes a multiline string into a form"
  {:added "4.0"}
  [s]
  (list 'String/join "\\n"
        (str/split-lines s)))

(defn layout-single-row
  "layouts a row"
  {:added "4.0"}
  [row {:keys [spec
               indents]
        :or {indents 0}
        :as opts}]
  (if (empty? row)
    []
    (:interim
     (reduce  (fn [{:keys [interim
                           opts]} val]
                (let [{:keys [col-compact]} spec
                      {:keys [indents]} opts
                      width-fn (if (= col-compact false)
                                 construct/max-width
                                 construct/last-width)
                      prev  (last interim)
                      nindents   (+ indents (width-fn prev indents) 1)
                      nopts      (assoc opts :indents nindents)
                      curr       (*layout-fn* val nopts)]
                  {:interim (conj interim (construct/space) curr)
                   :opts    nopts}))
              {:opts (assoc opts :indents indents)
               :interim [(*layout-fn* (first row) opts)]}
              (rest row)))))

(defn layout-one-column
  "layout for one column"
  {:added "4.0"}
  [vals {:keys [indents]
         :or {indents 0}
         :as opts}]
  (mapv (fn [arg]
          (*layout-fn* arg opts))
        vals))

(defn layout-two-column
  "layout for 2 column bindings and :key val combinations"
  {:added "4.0"}
  [pairs
   {:keys [col-align
           col-compact]
    :or {col-compact false
         col-align true}
    :as spec}
   {:keys [indents]
    :or {indents 0}
    :as opts}]
  (let [width-fn     (if col-compact
                       base/block-width
                       #(construct/max-width % indents))
        pair-keys    (->> (map first pairs)
                          (mapv (fn [arg]
                                  (*layout-fn* arg opts))))
        pair-max-offset  (if col-align
                           (+ 1 
                              (apply max (map width-fn pair-keys))))
        pair-fn           (cond col-align
                                (fn [i [_ val]]
                                  (let [pair-key  (get pair-keys i)
                                        spaces    (repeat (- pair-max-offset
                                                             (construct/line-width pair-key indents))
                                                          (construct/space))
                                        pair-val  (*layout-fn* val
                                                               (assoc opts
                                                                      :indents
                                                                      (+ pair-max-offset indents)))
                                        row    (concat [pair-key] spaces [pair-val])]
                                    row))
                                
                                :else
                                (fn [i [_ val]]
                                  (let [pair-key  (get pair-keys i)
                                        nopts     (assoc opts :indents (+ indents (construct/line-width
                                                                                   pair-key
                                                                                   indents)
                                                                          1)) 
                                        pair-val  (*layout-fn* val nopts)]
                                    [pair-key (construct/space) pair-val])))]
    (vec (map-indexed pair-fn pairs))))

#_
(comment

  _ (std.lib/prn
      [pair-max-offset
       indents
       (construct/max-width pair-key)
       (construct/max-width pair-key indents)
       (base/block-width pair-key)]
      row)
  
  (std.lib/pl
   (std.block.layout.bind/layout-main
    '^{:spec {:col-start 2}}
    (let [foo-bind     [1 2 3]
          {:keys [a ab abc]
           :as data}   (merge {:a1 {:b1-data-long0 1
                                    :b1-data-long1 2}
                               :a2 {:b2-data-long0 3
                                    :b2-data-long1 4}}
           spec)
          foo-bind     [1 2 3]]
      (+ 1 2 3))
    )))

(defn layout-n-column-space
  "layouts a set of columns with single space"
  {:added "4.0"}
  [rows {:keys [indents]
         :or {indents 0}
         :as opts}]
  (mapv #(layout-single-row % opts) rows))

(defn layout-n-column-pad
  "layouts a set of columns with single space"
  {:added "4.0"}
  [rows
   {:keys [col-pad]}
   {:keys [indents]
    :or {indents 0}
    :as opts}]
  (mapv (fn [row]
          (if (empty? row)
            []
            (:interim
             (reduce  (fn [{:keys [interim
                                   padding
                                   opts]} val]
                        (let [{:keys [indents]} opts
                              prev       (last interim)
                              pad        (if (integer? padding)
                                           (+ 1 padding)
                                           (+ 1 (first padding)))
                              nindents   (+ indents pad)
                              nopts      (assoc opts :indents nindents)
                              curr       (*layout-fn* val nopts)]
                          {:interim (vec (concat interim
                                                 (repeat (- pad
                                                            (construct/line-width prev nindents))
                                                         (construct/space))
                                                 [curr]))
                           :padding (if (integer? padding)
                                      padding
                                      (rest padding))
                           :opts    nopts}))
                      {:opts (assoc opts :indents indents)
                       :padding col-pad
                       :interim [(*layout-fn* (first row) opts)]}
                      (rest row)))))
        rows))

(defn layout-n-column-align
  "layout code based on n columns"
  {:added "4.0"}
  [rows
   {:keys [col-compact
           col-align
           col-pad]
    :or {col-align true}}
   {:keys [indents]
    :or {indents 0}
    :as opts}]
  (let [
        rows       (mapv vec (filter not-empty rows))
        row-counts (mapv count rows)
        max-cols   (apply max row-counts)
        opts (assoc opts :indents indents)]
    (:interim
     (reduce (fn [{:keys [interim
                          is-valid
                          opts]}
                  i]
               (let [{:keys [indents]} opts
                     width-fn     (if col-compact
                                    base/block-width
                                    #(construct/max-width % indents))
                     nis-valid   (map (fn [row]
                                        (< i (count row)))
                                      rows)
                     max-padding  (->> (map (fn [arr is-valid]
                                              (if is-valid
                                                (last arr)))
                                            interim
                                            is-valid)
                                       (filter identity)
                                       (map width-fn)
                                       (apply max (if col-pad col-pad 0))
                                       (+ 1))
                     max-offset   (+ max-padding indents)]
                 {:interim (mapv  (fn [arr row is-valid]
                                    (cond (not is-valid) arr
                                          :else
                                          (let [spaces    (repeat (- max-padding
                                                                     (construct/line-width (last arr)
                                                                                           indents))
                                                                  (construct/space))
                                                elem  (*layout-fn* (get row i)
                                                                   (assoc opts :indents max-offset))
                                                narr  (conj (apply conj arr spaces)
                                                            elem)]
                                            narr)))
                                  interim
                                  rows
                                  nis-valid)
                  :is-valid nis-valid
                  :opts (assoc opts :indents (+ indents 1 max-offset))}))
             {:opts opts
              :is-valid (repeat (count rows) true)
              :interim  (mapv (fn [row]
                                [(*layout-fn* (first row) opts)])
                              rows)}
             (range 1 max-cols)))))

(defn layout-n-column
  "layout for arbitrary column formatting"
  {:added "4.0"}
  [rows spec opts]
  (let [{:keys [col-pad col-align]}  spec]
    (cond col-align
          (layout-n-column-align rows spec opts)

          col-pad
          (layout-n-column-pad rows spec opts)

          :else
          (layout-n-column-space rows opts))))

(defn layout-pair-blocks
  "layout-pair-blocks"
  {:added "4.0"}
  [form {:keys [indents
                spec]
         :or {indents 0}
         :as opts}]
  (let [{:keys [col-align]
         :or {col-align true}} spec
        pair-spacing (concat  [(construct/newline)]
                              (repeat indents (construct/space)))
        pairs        (->> form
                          (partition-all 2))
        pair-blocks  (layout-two-column pairs spec opts)]
    [pair-blocks pair-spacing]))

(defn layout-multiline-form-setup
  "helper function to prep multiline form"
  {:added "4.0"}
  ([form {:keys [indents
                 spec]
          :or {indents 0}
          :as opts}]
   (let [{:keys [col-compact]} spec
         width-fn (if (= col-compact false)
                    construct/max-width
                    construct/last-width)
         start-sym (*layout-fn* (first form)
                                opts)
         nindents (+ indents 1 (width-fn start-sym indents))]
     [start-sym nindents])))

(defn layout-multiline-custom
  "layout standard paired inputs"
  {:added "4.0"}
  ([form {:keys [indents
                 spec]
          :or {indents 0}
          :as opts}]
   (let [{:keys [col-from
                 col-compact
                 columns
                 col-align
                 col-start]
          :as spec}  (merge {:columns 1
                             :col-from 1
                             :col-align true}
                            spec)
         
         [start-sym
          nindents]      (layout-multiline-form-setup form
                                                      (assoc opts :indents (inc indents)))
         initial-blocks  (layout-single-row (take col-from (rest form))
                                            (assoc opts :indents nindents))
         
         
         rindents     (if col-start
                        (+ indents col-start)
                        nindents)
         rspacing     (concat  [(construct/newline)]
                               (repeat rindents (construct/space)))
         ropts        (assoc opts :indents rindents :spec spec)
         
         rarr         (drop (inc col-from) form)
         row-blocks   (cond (== columns 1)
                            (layout-one-column   (seq rarr) ropts)
                            
                            (== columns 2)
                            (layout-two-column   (partition-all 2 (seq rarr)) spec ropts)
                            
                            :else
                            (layout-n-column (partition-all columns (seq rarr)) spec ropts))
         join-fn (if (= 1 columns) join-blocks join-block-arrays)
         _ (std.lib/prn form
                        spec
                        initial-blocks
                        col-compact)]
     (construct/container
      :list
      (vec (concat [start-sym]
                   (if (empty? initial-blocks)
                     []
                     (if col-compact
                       [(construct/space)]
                       (if (< 0 (base/block-height start-sym))
                         (repeat (+ 2 (- (construct/max-width start-sym  indents)
                                         (construct/last-width start-sym indents)))
                                 (construct/space))
                         [(construct/space)])))
                   initial-blocks
                   (if (and (nil? col-start)
                            (= col-from 0))
                     [(construct/space)]
                     (if (not-empty row-blocks)
                       rspacing))
                   (join-fn rspacing row-blocks)))))))

(comment
  (if (false? col-compact)
    (std.lib/do:prn
      (repeat (apply + (map base/block-width (conj start-sym initial-blocks)))
              (construct/space)))
    )
  )

(defn layout-multiline-paired
  "layout standard paired inputs"
  {:added "4.0"}
  ([form {:keys [indents
                 spec]
          :or {indents 0}
          :as opts}]
   (layout-multiline-custom form (assoc-in opts [:spec :columns] 2))))

(defn layout-multiline-hashmap
  "layouts the hashmap"
  {:added "4.0"}
  ([m    {:keys [indents
                 spec]
          :or {indents 0}
          :as opts}]
   (let [{:keys [col-align
                 columns]
          :as spec}  (merge {:columns 2
                             :col-align false}
                            spec)
         nindents     (+ indents 1)
         arg-spacing  (concat  [(construct/newline)]
                               (repeat nindents (construct/space)))        
         nopts        (-> opts
                          (assoc :indents nindents
                                 :spec spec))
         row-blocks   (cond (== columns 1)
                            (layout-one-column (mapcat identity (seq m)) spec nopts)
                            
                            (== columns 2)
                            (layout-two-column m spec nopts)
                            
                            :else
                            (layout-n-column (->> (mapcat identity (seq m))
                                                  (partition-all columns))
                                             spec
                                             nopts))
         join-fn (if (= 1 columns) join-blocks join-block-arrays)]
     (construct/container :map (vec (join-fn arg-spacing row-blocks))))))

;;
;;
;;

(defn layout-by-columns
  "layout data using columns"
  {:added "4.0"}
  ([arr indents-start {:keys [indents
                              spec]
                       :or {indents 0}
                       :as opts}]
   (let [{:keys [col-align
                 columns]
          :as spec}   (merge {:columns (if (< (count arr) 2)
                                         (count arr)
                                         2)
                              :col-align true}
                             spec)
         nindents     (+ indents indents-start)
         arg-spacing  (concat  [(construct/newline)]
                               (repeat nindents (construct/space)))
         nopts       (-> opts
                         (assoc :indents nindents
                                :spec spec))
         row-blocks  (cond (== columns 1)
                           (layout-one-column   (seq arr) nopts)

                           (== columns 2)
                           (layout-two-column   (partition-all 2 (seq arr)) spec nopts)

                           :else
                           (layout-n-column (partition-all columns (seq arr)) spec nopts))
         join-fn (if (= 1 columns) join-blocks join-block-arrays)]
     (vec (join-fn arg-spacing row-blocks)))))

(defn layout-by-rows
  "layout data using rows"
  {:added "4.0"}
  ([arr indents-start {:keys [indents
                              spec]
                      :or {indents 0}
                      :as opts}]
   (let [{:keys [row-wrap
                 row-len]} (merge {:row-len 50}
                                  spec)
         iindents     (+ indents indents-start)
         row-spacing  (concat  [(construct/newline)]
                               (repeat iindents (construct/space)))
         iopts       (-> opts
                         (assoc :indents iindents :spec spec))]
     (if (empty? arr)
       []
       (let [init-elem (*layout-fn* (first arr) opts)]
         (:interim
          (reduce  (fn [{:keys [interim
                                length
                                opts]} val]
                     (let [{:keys [indents]} opts
                           prev       (last interim)
                           nlength    (+ length (base/block-width prev) 1)
                           nindents   (+ indents nlength)
                           nopts      (assoc opts :indents nindents)
                           curr       (*layout-fn* val nopts)]
                       (cond (> row-len nlength)
                             {:interim (conj interim (construct/space) curr)
                              :length  nlength
                              :opts    nopts}

                             :else
                             {:interim (conj (apply conj interim row-spacing)
                                             curr)
                              :length 0
                              :opts   iopts})))
                   {:opts iopts
                    :length  0
                    :interim [init-elem]}
                   (rest arr))))))))


(defn layout-by
  "general layout function"
  {:added "4.0"}
  [arr indents-start {:keys [spec]
                      :as opts}]
  (let [{:keys [row-wrap]} spec]
    (cond row-wrap
          (layout-by-rows arr indents-start opts)

          :else
          (layout-by-columns arr indents-start opts))))

(defn layout-multiline-hashset
  "layouts the hashset"
  {:added "4.0"}
  ([arr    {:keys [indents]
            :or {indents 0}
            :as opts}]
   (let [children (layout-by-columns arr 2 opts)]
     (construct/container :set children))))

(defn layout-multiline-vector
  "layouts the vector"
  {:added "4.0"}
  ([arr    {:keys [indents]
            :or {indents 0}
            :as opts}]
   (let [children (layout-by-columns arr 1 opts)]
     (construct/container :vector children))))

;;
;; maybe replacable
;;

(defn layout-with-bindings
  "layout with bindings"
  {:added "4.0"}
  ([form {:keys [indents]
          :or {indents 0}
          :as opts}]
   (let [[start-sym nindents] (layout-multiline-form-setup form
                                                           (assoc opts :indents (inc indents)))
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
                                       arg-blocks))))))


(defn layout-multiline-form
  "layout standard multiline forms"
  {:added "4.0"}
  ([form {:keys [indents]
          :or {indents 0}
          :as opts}]
   (let [[start-sym nindents] (layout-multiline-form-setup form
                                                           (assoc opts :indents (inc indents)))
         start-blocks (list start-sym (construct/space))
         arg-spacing  (concat  [(construct/newline)]
                               (repeat nindents (construct/space)))
         nopts        (assoc opts :indents nindents)
         arg-blocks   (->> (rest form)
                           (map (fn [arg]
                                  (*layout-fn* arg nopts)))
                           (join-blocks arg-spacing))]
     (construct/container :list
                          (vec (concat start-blocks
                                       arg-blocks))))))
