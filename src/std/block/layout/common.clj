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
  "TODO"
  {:added "4.0"}
  [s]
  (list 'String/join "\\n"
        (str/split-lines s)))

(defn layout-one-column
  [vals
   {:keys [indents]
    :or {indents 0}
    :as opts}]
  (mapv (fn [arg]
          (*layout-fn* arg opts))
        vals))

(defn layout-two-column
  "layout key vals"
  {:added "4.0"}
  [pairs
   {:keys [col-align]
    :or {col-align true}}
   {:keys [indents]
    :or {indents 0}
    :as opts}]
  (let [pair-keys    (->> (map first pairs)
                          (mapv (fn [arg]
                                  (*layout-fn* arg opts))))
        pair-max-offset  (if col-align
                           (+ 1 indents (apply max (map construct/max-width pair-keys))))
        pair-fn           (cond col-align
                                (fn [i [_ val]]
                                  (let [pair-key  (get pair-keys i)
                                        spaces    (repeat (- pair-max-offset
                                                             indents
                                                             (base/block-width pair-key))
                                                          (construct/space))
                                        pair-val  (*layout-fn* val
                                                               (assoc opts :indents pair-max-offset))]
                                    (concat [pair-key] spaces [pair-val])))
                                
                                :else
                                (fn [i [_ val]]
                                  (let [pair-key  (get pair-keys i)
                                        pair-val  (*layout-fn* val
                                                               (assoc opts
                                                                      :indents (+ (base/block-width pair-key) 1)))]
                                    [pair-key (construct/space) pair-val])))]
    (vec (map-indexed pair-fn pairs))))

(defn layout-n-column-space
  [rows
   {:keys [indents]
    :or {indents 0}
    :as opts}]
  (mapv (fn [row]
          (if (empty? row)
            []
            (:interim
             (reduce  (fn [{:keys [interim
                                   opts]} val]
                        (let [{:keys [indents]} opts
                              prev  (last interim)
                              nindents  (+ indents (base/block-width prev) 1)
                              nopts      (assoc opts :indents nindents)
                              curr       (*layout-fn* val nopts)]
                          {:interim (conj interim (construct/space) curr)
                           :opts    nopts}))
                      {:opts (assoc opts :indents indents)
                       :interim [(*layout-fn* (first row) opts)]}
                      (rest row)))))
        rows))

(defn layout-n-column-pad
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
                                           padding
                                           (first padding))
                              nindents   (+ indents pad)
                              nopts      (assoc opts :indents nindents)
                              curr       (*layout-fn* val nopts)]
                          {:interim (vec (concat interim
                                                 (repeat (- pad
                                                            (base/block-width prev))
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
  [rows
   {:keys [col-align
           col-pad]
    :or {col-align true}}
   {:keys [indents]
    :or {indents 0}
    :as opts}]
  (let [rows      (mapv vec rows)
        max-cols  (apply max (map count rows))]
    (reduce (fn [{:keys [interim
                         opts]}])
            {:opts (assoc opts :indents indents)
             :interim []}
            rows)))

(defn layout-multiline-form-setup
  "TODO"
  {:added "4.0"}
  ([form {:keys [indents]
          :or {indents 0}
          :as opts}]
   (let [start-sym (*layout-fn* (first form)
                              opts)
         nindents (+ indents 2 (construct/max-width start-sym))]
     [start-sym nindents])))

(defn layout-multiline-form
  "layout standard multiline forms"
  {:added "4.0"}
  ([form {:keys [indents]
          :as opts}]
   (let [[start-sym nindents] (layout-multiline-form-setup form opts)
         start-blocks (list start-sym (construct/space))
         arg-spacing  (concat  [(construct/newline)]
                               (repeat nindents (construct/space)))
         arg-blocks   (->> (rest form)
                           (map (fn [arg]
                                  (*layout-fn* arg
                                               (assoc opts :indents nindents))))
                           (join-blocks arg-spacing))]
     (construct/container :list
                          (vec (concat start-blocks
                                       arg-blocks))))))

(defn layout-multiline-paired
  "layout standard paired inputs"
  {:added "4.0"}
  ([form {:keys [indents
                 spec]
          :or {indents 0}
          :as opts}]
   (let [{:keys [pair-from
                 col-align]
          :or {pair-from 2
               col-align true}} spec
         [start-sym nindents] (layout-multiline-form-setup form opts)
         start-blocks (list start-sym (construct/space))
         arg-initial  (take (dec pair-from)
                            (rest form))
         arg-spacing  (concat  [(construct/newline)]
                               (repeat nindents (construct/space)))
         arg-blocks   (->> arg-initial
                           (map (fn [arg]
                                  (*layout-fn* arg
                                               (assoc opts :indents nindents))))
                           (join-blocks arg-spacing))
         pairs        (->> (drop pair-from form)
                           (partition 2))
         pair-blocks  (->> (layout-two-column pairs
                                            spec
                                            (assoc opts :indents nindents))
                           (join-block-arrays arg-spacing))]
     (construct/container :list
                          (vec (concat start-blocks
                                       arg-blocks
                                       (if (empty? arg-initial)
                                         []
                                         arg-spacing)
                                       pair-blocks))))))

(defn layout-multiline-hashmap
  "layouts the hashmap"
  {:added "4.0"}
  ([m    {:keys [indents
                 spec]
          :or {indents 0}
          :as opts}]
   (let [{:keys [col-align
                 columns]
          :or {columns 2
               col-align true}} spec
         nindents  (+ indents 1)
         arg-spacing  (concat  [(construct/newline)]
                               (repeat nindents (construct/space)))
         pair-blocks  (cond (== columns 2)
                                (layout-two-column m spec (assoc opts :indents nindents))
                                
                                :else
                                (layout-two-column m spec (assoc opts :indents nindents)))]
     (construct/container :map
                          (vec (join-block-arrays arg-spacing pair-blocks))))))


(defn layout-multiline-hashset
  ([m    {:keys [indents
                 spec]
          :or {indents 0}
          :as opts}]
   (let [{:keys [col-align
                 columns]
          :or {columns 2
               col-align true}} spec
         nindents  (+ indents 1)
         arg-spacing  (concat  [(construct/newline)]
                               (repeat nindents (construct/space)))
         pair-blocks  (cond (== columns 2)
                            (layout-two-column m spec (assoc opts :indents nindents))
                                
                            :else
                            (layout-two-column m spec (assoc opts :indents nindents)))]
     (construct/container :map
                          (vec (join-block-arrays arg-spacing pair-blocks))))))



(comment
  (take-nth 2 [1 2 3 4 5])
  
  [start-blocks
   form-rest]     
  end-blocks  (mapcat (fn [i]
                        (concat  [(construct/newline)]
                                 (repeat nindents (construct/space))
                                 [(*layout-fn* (second form)
                                               (assoc opts :indents nindents))]))
                      (drop 2 form)))


(comment
  (format-multiline-string
   "hel\n\n\nhello")
  (String/join
   "\n"
   ["hel" "" "" "hello"])

  (base/block-info
   (construct/block '(+ 1 "\n\n" 3)))



  (base/block-string
   (construct/block "\\n"))

  (base/block-info
   (format-multiline-string
    (construct/block "hel\n\n\nhello")))

  (construct/block
   )


  (base/block-info
   (construct/block (str/split-lines
                     "hel\n\n\nhello")))

  (base/block-info
   (construct/block #_(str/split-lines
                       "hel\n\n\nhello")
                    ["hel" "" "" "hello"]))

  (map base/block-height
       (base/block-children
        (construct/block #_(str/split-lines
                            "hel\n\n\nhello")
                         ["hel" "" "" "hello"])))

  (base/block-info
   (construct/block ""))

  (base/block-children
   (construct/block #_(str/split-lines
                       "hel\n\n\nhello")
                    ["hel" "" "" "hello"]))

  )
