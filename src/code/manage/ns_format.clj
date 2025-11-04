(ns code.manage.ns-format
  (:require [code.framework :as base]
            [code.query :as query]
            [std.block :as block]
            [code.edit :as nav]
            [std.lib.zip :as zip]
            [std.lib :as h]
            [std.string :as str]))

(defonce +key-order+
  {:use 0
   :require 1
   :import 2
   :refer-clojure 3})

(defn first-element
  "returns first element of list"
  {:added "3.0"}
  ([x]
   (cond (sequential? x)
         (first-element (first x))

         (or (symbol? x)
             (string? x)
             (keyword? x))
         x

         :else
         (throw (ex-info "Not Valid" {:input x})))))

(defn key-order
  "gets key order"
  {:added "3.0"}
  ([x]
   (let [x (first-element x)]
     (get +key-order+ x -1))))

(defn replace-nodes
  "replace nodes with new ones"
  {:added "3.0"}
  ([nav nodes]
   (replace-nodes nav nodes nav/right))
  ([nav nodes move]
   (reduce (fn [nav node]
             (let [nav (nav/replace nav node)]
               (if-let [nav (move nav)]
                 nav
                 nav)))
           nav
           nodes)))

(defn sort-nodes
  "sorts nodes"
  {:added "3.0"}
  ([nav order-fn]
   (sort-nodes nav order-fn nav/right))
  ([nav order-fn move]
   (let [nodes (->> (iterate move nav)
                    (take-while nav/block)
                    (map nav/block)
                    (sort-by (comp order-fn block/value)))]
     (replace-nodes nav nodes move))))

(defn merge-nodes
  "merge nodes"
  {:added "3.0"}
  ([arr]
   (merge-nodes arr identity [(block/space)]))
  ([[node-to node-from & more] select spacing]
   (cond (nil? node-from)
         node-to

         :else
         (let [children   (block/children node-to)
               additional (select (block/children node-from))
               merged (assoc node-to :children (concat children spacing additional))]
           (recur (cons merged more)
                  select
                  spacing)))))

(defn merge-eligible
  "merge nodes that are not whitespace"
  {:added "3.0"}
  ([nodes]
   (let [spacing (cons (block/newline)
                       (block/spaces (-> (first nodes)
                                         (block/children)
                                         (first)
                                         (meta)
                                         :end-col)))
         select (fn [children]
                  (->> children
                       (drop-while :whitespace)
                       (rest)
                       (drop-while :whitespace)))
         merged (merge-nodes nodes select spacing)]
     {:count  (dec (count nodes))
      :merged merged})))

(defn merge-forms
  "merge two forms"
  {:added "3.0"}
  ([nav query]
   (query/modify nav
                 query
                 (fn [nav]
                   (let [nodes (->> (iterate nav/right nav)
                                    (take-while nav/block)
                                    (map nav/block))
                         eligible  (->> nodes
                                        (group-by (comp first-element block/value))
                                        (filterv (fn [[_ v]]
                                                   (> (count v) 1)))
                                        (h/map-vals merge-eligible))
                         move-right (fn [nav]
                                      (if-let [nav (nav/right nav)]
                                        nav
                                        nav))
                         result (reduce (fn [{:keys [nav eligible]} node]
                                          (let [k (-> node block/value first-element)]
                                            (cond (and (eligible k)
                                                       (-> (eligible k) :count zero?))
                                                  {:nav (-> nav
                                                            (nav/replace (:merged (eligible k)))
                                                            move-right)
                                                   :eligible (dissoc eligible k)}

                                                  (and (eligible k))
                                                  {:nav (-> nav
                                                            (nav/delete)
                                                            (move-right))
                                                   :eligible (update-in eligible [k :count] dec)}

                                                  :else
                                                  {:nav (move-right nav)
                                                   :eligible eligible})))
                                        {:nav nav :eligible eligible}
                                        nodes)]
                     (:nav result))))))

(defn ns:merge-forms
  "merges in the `ns` form"
  {:added "3.0"}
  ([nav]
   (merge-forms nav [{:or ['ns 'env/init]}
                     '|
                     {:left {:left 'ns}}])))

(defn ns:sort-forms
  "sorts in the `ns` form"
  {:added "3.0"}
  ([nav]
   (query/modify nav
                 [{:or ['ns 'env/init]}
                  '|
                  {:left {:left 'ns}}]
                 (fn [nav]
                   (sort-nodes nav key-order nav/right)))))

(defn use:merge-forms
  "merge in the `ns` :use form"
  {:added "3.0"}
  ([nav]
   (query/modify nav
                 [{:or ['ns 'env/init]}
                  '|
                  {:first :use}]
                 (fn [nav]
                   (let [[sym & all] (nav/value nav)]
                     (nav/replace nav (cons sym (sort (set all)))))))))

(defn require:vectify-forms
  "changes :require forms into a vectors"
  {:added "3.0"}
  ([nav]
   (query/modify nav
                 [{:or ['ns 'env/init]}
                  {:first :require}
                  '|
                  {:or [list? symbol?]}]
                 (fn [nav]
                   (let [form (nav/value nav)]
                     (cond (symbol? form)
                           (nav/replace nav [form])

                           (list? form)
                           (nav/replace nav (vec form))))))))

(defn require:expand-shorthand
  "expands shorthand :require forms into a vectors"
  {:added "3.0"}
  ([nav]
   (query/modify nav
                 [{:or ['ns 'env/init]}
                  {:first :require}
                  '|
                  vector?
                  {:or   [symbol? vector?]
                   :left {:not keyword?}}]

                 (fn [nav]
                   (let [{:keys [row col]} (meta (nav/block nav))
                         [nsp & more] (nav/value nav)

                         nodes (->> more
                                    (mapv (fn [x]
                                            (cond (vector? x)
                                                  (update-in x [0] (comp symbol #(str nsp "." %)))

                                                  :else
                                                  [(symbol (str nsp "." x))])))
                                    (sort))
                         edits (reduce (fn [nav node]
                                         (let [nav (-> nav
                                                       (nav/insert-token-to-right node)
                                                       (nav/right)
                                                       (zip/insert-left (block/newline)))]
                                           (apply zip/insert-left nav (repeat (dec col) (block/space)))))
                                       (nav/replace nav (first nodes))
                                       (rest nodes))]
                     edits)))))

(defn require:merge-forms
  "merge :require forms that are identical"
  {:added "3.0"}
  ([nav]
   (merge-forms nav [{:or ['ns 'env/init]}
                     {:first :require}
                     '|
                     {:left {:is :require}}])))

(defn import:listify-forms
  "changes :import forms into a lists"
  {:added "3.0"}
  ([nav]
   (query/modify nav
                 [{:or ['ns 'env/init]}
                  {:first :import}
                  '|
                  {:or [symbol? vector?]}]
                 (fn [nav]
                   (let [form (nav/value nav)]
                     (cond (symbol? form)
                           (let [arr (str/split (str form) #"\.")
                                 cls (last arr)
                                 nsp (butlast arr)]

                             (nav/replace nav
                                          (list (symbol (str/join "." nsp))
                                                (symbol cls))))

                           (vector? form)
                           (nav/replace nav (apply list form))

                           :else
                           nav))))))

(defn import:merge-forms
  "merge :import forms that are identical"
  {:added "3.0"}
  ([nav]
   (merge-forms nav [{:or ['ns 'env/init]}
                     {:first :import}
                     '|
                     {:left {:is :import}}])))

(defn import:merge-form-entries
  "merge :import form entries that are identical"
  {:added "3.0"}
  ([nav]
   (query/modify nav
                 [{:or ['ns 'env/init]}
                  {:first :import}
                  '|
                  list?]
                 (fn [nav]
                   (let [[sym & all] (nav/value nav)
                         nav (nav/replace nav (cons sym (sort (set all))))]
                     nav)))))

(defn all:sort-forms
  "sorts the entries by alphabetical order"
  {:added "3.0"}
  ([nav]
   (let [sym-fn (fn [x] (if (sequential? x) (first x) x))]
     (query/modify nav
                   [{:or ['ns 'env/init]}
                    {:first {:or [:import :require :use]}}
                    '|
                    {:left {:left-most true}}]
                   (fn [nav]
                     (sort-nodes nav sym-fn nav/right))))))

(defn ns-format
  "top-level ns-format form"
  {:added "3.0"}
  ([ns params lookup project]
   (let [edits [ns:merge-forms
                ns:sort-forms
                use:merge-forms
                require:vectify-forms
                require:expand-shorthand
                require:merge-forms
                import:listify-forms
                import:merge-forms
                import:merge-form-entries
                all:sort-forms]]
     (base/refactor-code ns (assoc params :edits edits) lookup project))))

(comment

  (ns:merge-forms)

  (code.manage/refactor-code
   'code.manage.ns-format
   {:edits [require:expand-shorthand]
    :print {:function true}})

  (code.manage/refactor-code
   '[hara]
   {:edits [;ns:merge-forms
            ;ns:sort-forms
            ;use:merge-forms
            ;require:vectify-forms
            require:expand-shorthand
            ;require:merge-forms
            ;import:listify-forms
            ;import:merge-forms
            ;import:merge-form-entries
            ;all:sort-forms
            ]
    :print {:function true}})

  (code.manage/lint '[std.fs]
                    {:write true}))
