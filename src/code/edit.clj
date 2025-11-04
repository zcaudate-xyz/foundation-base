(ns code.edit
  (:require [std.block.base :as base]
            [std.block.construct :as construct]
            [std.block.type :as type]
            [std.block.parse :as parse]
            [std.lib.zip :as zip]
            [std.string :as str]
            [std.lib :as h])
  (:refer-clojure :exclude [next replace type]))

(defn nav-template
  "helper function for importing vars"
  {:added "3.0"}
  ([sym var]
   `(defn ~sym
      ([~'zip]
       (~sym ~'zip :right))
      ([~'zip ~'step]
       (if-let [~'elem (zip/get ~'zip)]
         (~(h/var-sym var) ~'elem))))))

(h/template-vars [nav-template]
  (block?       base/block?)
  (expression?  base/expression?)
  (type         base/block-type)
  (tag          base/block-tag)
  (string       base/block-string)
  (length       base/block-length)
  (width        base/block-width)
  (height       base/block-height)
  (prefixed     base/block-prefixed)
  (suffixed     base/block-suffixed)
  (verify       base/block-verify)
  (value        base/block-value)
  (value-string base/block-value-string)
  (children     base/block-children)
  (info         base/block-info)
  (void?        type/void-block?)
  (space?       type/space-block?)
  (linebreak?   type/linebreak-block?)
  (linespace?   type/linespace-block?)
  (eof?         type/eof-block?)
  (comment?     type/comment-block?)
  (token?       type/token-block?)
  (container?   type/container-block?)
  (modifier?    type/modifier-block?))

(defn left-anchor
  "calculates the length to the last newline"
  {:added "3.0"}
  ([nav]
   (cond (and (zip/at-outside-most? nav)
              (zip/at-left-most? nav))
         0

         :else
         (loop [total 0
                [lblock & more] (:left nav)]
           (cond (and (nil? lblock)
                      (zip/at-outside-most? nav))
                 total

                 (nil? lblock)
                 (let [pnav (zip/step-outside nav)]
                   (+ total
                      (if-let [elem (zip/right-element pnav)]
                        (base/block-prefixed elem)
                        0)
                      (left-anchor pnav)))

                 (zero? (base/block-height lblock))
                 (recur (long (+ total (base/block-width lblock)))
                        more)

                 (base/container? lblock)
                 (+ total (base/block-width lblock))

                 :else total)))))

(defn update-step-left
  "updates the step position to the left"
  {:added "3.0"}
  ([nav lblock]
   (update-in nav
              [:position]
              (fn [[row col]]
                (let [rows    (base/block-height lblock)
                      cols    (base/block-width lblock)]
                  (if (pos? rows)
                    [(- row rows) (left-anchor nav)]
                    [row (- col cols)]))))))

(defn update-step-right
  "updates the step position to the right"
  {:added "3.0"}
  ([nav block]
   (-> (update-in nav
                  [:position]
                  (fn [[row col]]
                    (let [rows (base/block-height block)
                          cols (base/block-width block)]
                      (if (pos? rows)
                        [(+ row rows) cols]
                        [row (+ col cols)])))))))

(defn update-step-inside
  "updates the step position to within a block"
  {:added "3.0"}
  ([nav block]
   (update-in nav
              [:position]
              (fn [[row col]]
                [row (+ col (base/block-prefixed block))]))))

(defn update-step-inside-left
  "updates the step position to within a block
   (-> {:position [0 3]}
       (update-step-inside-left (construct/block #{})))
   => {:position [0 2]}"
  {:added "3.0"}
  ([nav block]
   (update-in nav
              [:position]
              (fn [[row col]]
                [row (- col (base/block-suffixed block))]))))

(defn update-step-outside
  "updates the step position to be outside a block"
  {:added "3.0"}
  ([nav left-blocks]
   (update-in nav
              [:position]
              (fn [[row col]]
                (let [lrows   (reduce (fn [total block]
                                        (+ total (base/block-height block)))
                                      0
                                      left-blocks)]
                  (cond (zero? lrows)
                        [row (- col
                                (base/block-prefixed (zip/right-element nav))
                                (apply + (map base/block-width left-blocks)))]

                        :else
                        [(- row lrows)
                         (left-anchor nav)]))))))

(defn display-navigator
  "displays a string representing the navigator"
  {:added "3.0"}
  ([nav]
   (let [[row col] (:position nav)]
     (format "<%d,%d> %s"
             row
             col
             (apply str (zip/status nav))))))

(defn navigator
  "creates a navigator for the block"
  {:added "3.0"}
  ([block]
   (navigator block {}))
  ([block opts]
   (zip/zipper (construct/block block)
               (merge zip/+base+
                      {:create-container        construct/empty
                       :create-element          construct/block
                       :cursor                  (construct/cursor)
                       :is-container?           base/container?
                       :is-empty-container?     (comp empty? base/block-children)
                       :is-element?             base/block?
                       :list-elements           base/block-children
                       :add-element             construct/add-child
                       :update-elements         base/replace-children
                       :update-step-left        update-step-left
                       :update-step-right       update-step-right
                       :update-step-inside      update-step-inside
                       :update-step-inside-left update-step-inside-left
                       :update-step-outside     update-step-outside
                       :update-delete-left      update-step-left
                       :update-delete-right     (fn [zip elem] zip)
                       :update-insert-left      update-step-right
                       :update-insert-right     (fn [zip elem] zip)})
               {:tag :navigator
                :prefix ""
                :display display-navigator
                :position [0 0]})))

(defn navigator?
  "checks if object is navigator"
  {:added "3.0"}
  ([obj]
   (and (instance? std.lib.zip.Zipper obj)
        (= (:tag obj) :navigator))))

(defn from-status
  "constructs a navigator from a given status"
  {:added "3.0"}
  ([block]
   (zip/from-status block
                    navigator)))

(defn parse-string
  "creates a navigator from string"
  {:added "3.0"}
  ([string]
   (from-status (parse/parse-string string))))

(defn parse-root
  "parses the navigator from root string"
  {:added "3.0"}
  ([string]
   (navigator (parse/parse-root string))))

(defn parse-first
  "parses the navigator from root string"
  {:added "3.0"}
  ([string]
   (navigator (parse/parse-first string))))

(defn parse-root-status
  "parses string and creates a navigator from status"
  {:added "3.0"}
  ([string]
   (from-status (parse/parse-root string))))

(defn root-string
  "returns the top level string"
  {:added "3.0"}
  ([nav]
   (->> nav
        (zip/step-outside-most)
        (zip/step-left-most)
        (zip/right-elements)
        (map base/block-string)
        (apply str))))



(defn left-expression
  "returns the expression on the left"
  {:added "3.0"}
  ([nav]
   (->> (:left nav)
        (filter base/expression?)
        first)))

(defn left-expressions
  "returns all expressions on the left"
  {:added "3.0"}
  ([nav]
   (->> (zip/left-elements nav)
        (filter base/expression?))))

(defn right-expression
  "returns the expression on the right"
  {:added "3.0"}
  ([nav]
   (->> (zip/right-elements nav)
        (filter base/expression?)
        first)))

(defn right-expressions
  "returns all expressions on the right"
  {:added "3.0"}
  ([nav]
   (->> (zip/right-elements nav)
        (filter base/expression?))))

(defn top
  "moves to the top"
  {:added "3.0"}
  ([nav]
   (zip/step-outside-most nav)))

(defn left
  "moves to the left expression"
  {:added "3.0"}
  ([nav]
   (zip/find-left nav base/expression?)))

(defn left-most
  "moves to the left-most expression"
  {:added "3.0"}
  ([nav]
   (if (nil? (left nav))
     nav
     (recur (left nav)))))

(defn left-most?
  "checks if navigator is at left-most"
  {:added "3.0"}
  ([nav]
   (nil? (left nav))))

(defn right
  "moves to the expression on the right"
  {:added "3.0"}
  ([nav]
   (zip/find-right nav base/expression?)))

(defn right-most
  "moves to the right-most expression"
  {:added "3.0"}
  ([nav]
   (if (nil? (right nav))
     nav
     (recur (right nav)))))

(defn right-most?
  "checks if navigator is at right-most"
  {:added "3.0"}
  ([nav]
   (nil? (right nav))))

(defn up
  "navigates outside of the form"
  {:added "3.0"}
  ([nav]
   (zip/step-outside nav)))

(defn down
  "navigates into the form"
  {:added "3.0"}
  ([nav]
   (if (zip/can-step-inside? nav)
     (zip/step-inside nav))))

(defn right*
  "navigates to right element, including whitespace"
  {:added "3.0"}
  ([nav]
   (zip/step-right nav)))

(defn left*
  "navigates to left element, including whitespace"
  {:added "3.0"}
  ([nav]
   (zip/step-left nav)))

(defn block
  "returns the current block"
  {:added "3.0"}
  ([nav]
   (zip/get nav)))

(defn prev
  "moves to the previous expression"
  {:added "3.0"}
  ([nav]
   (zip/find-prev nav base/expression?)))

(defn next
  "moves to the next expression"
  {:added "3.0"}
  ([nav]
   (zip/find-next nav base/expression?)))

(defn find-next
  "moves tot the next token"
  {:added "3.0"}
  ([nav pred]
   (zip/find-next nav pred)))

(defn find-next-token
  "moves tot the next token"
  {:added "3.0"}
  ([nav data]
   (zip/find-next nav (fn [block]
                        (= data (base/block-value block))))))

(defn prev-anchor
  "moves to the previous newline"
  {:added "3.0"}
  ([nav]
   (if-let [nav (and nav
                     (zip/find-prev (zip/step-left nav)
                                    type/linebreak-block?))]
     (zip/step-right nav)
     (-> nav
         (zip/step-outside-most)
         (zip/step-left-most)))))

(defn next-anchor
  "moves to the next newline"
  {:added "3.0"}
  ([nav]
   (if-let [nav (and nav (zip/find-next nav type/linebreak-block?))]
     (zip/step-right nav))))

(defn left-token
  "moves to the left token"
  {:added "3.0"}
  ([nav]
   (zip/find-left nav type/token-block?)))

(defn left-most-token
  "moves to the left-most token"
  {:added "3.0"}
  ([nav]
   (if (nil? (left-token nav))
     nav
     (recur (left-token nav)))))

(defn right-token
  "moves to the right token"
  {:added "3.0"}
  ([nav]
   (zip/find-right nav type/token-block?)))

(defn right-most-token
  "moves to the right-most token"
  {:added "3.0"}
  ([nav]
   (if (nil? (right-token nav))
     nav
     (recur (right-token nav)))))

(defn prev-token
  "moves to the previous token"
  {:added "3.0"}
  ([nav]
   (zip/find-prev nav type/token-block?)))

(defn next-token
  "moves to the next token"
  {:added "3.0"}
  ([nav]
   (zip/find-next nav type/token-block?)))

(defn position-left
  "moves the cursor to left expression"
  {:added "3.0"}
  ([nav]
   (cond (base/expression? (zip/right-element nav))
         nav

         (empty? (left-expressions nav))
         (zip/step-left-most nav)

         :else
         (left nav))))

(defn position-right
  "moves the cursor the right expression"
  {:added "3.0"}
  ([nav]
   (cond (base/expression? (zip/right-element nav))
         nav

         (empty? (right-expressions nav))
         (zip/step-right-most nav)

         :else
         (right nav))))

(defn tighten-left
  "removes extra spaces on the left"
  {:added "3.0"}
  ([nav]
   (loop [nav  (position-right nav)]
     (let [elem (zip/left-element nav)]
       (cond (nil? elem)
             nav

             (type/void-block? elem)
             (recur (zip/delete-left nav))

             (type/comment-block? elem)
             (zip/insert-left nav (construct/newline))

             :else
             (zip/insert-left nav (construct/space)))))))

(defn tighten-right
  "removes extra spaces on the right"
  {:added "3.0"}
  ([nav]
   (loop [nav (position-left nav)]
     (let [elem (zip/right-element (zip/step-right nav))]
       (cond (nil? elem)
             (if (type/void-block? (zip/right-element nav))
               (zip/delete-right nav)
               nav)

             (type/void-block? elem)
             (-> (zip/step-right nav)
                 (zip/delete-right)
                 (zip/step-left)
                 (recur))

             :else
             (-> (zip/step-right nav)
                 (zip/insert-right (construct/space))
                 (zip/step-left)))))))

(defn tighten
  "removes extra spaces on both the left and right"
  {:added "3.0"}
  ([nav]
   (-> nav
       (tighten-left)
       (tighten-right))))

(defn level-empty?
  "checks if current container has expressions"
  {:added "3.0"}
  ([nav]
   (empty? (filter base/expression? (zip/current-elements nav)))))

(defn insert-empty
  "inserts an element into an empty container"
  {:added "3.0"}
  ([nav data]
   (-> nav
       (zip/step-left-most)
       (zip/insert-right data))))

(defn insert-token-to-right
  "inserts an element to the right"
  {:added "3.0"}
  ([nav data]
   (cond (level-empty? nav)
         (insert-empty nav data)

         :else
         (let [nav (position-right nav)]
           (-> nav
               (zip/step-right)
               (zip/insert-right data)
               (zip/insert-right (construct/space))
               (zip/step-left))))))

(defn insert-token-to-left
  "inserts an element to the left"
  {:added "3.0"}
  ([nav data]
   (cond (level-empty? nav)
         (insert-empty nav data)

         :else
         (let [nav (position-left nav)]
           (-> nav
               (zip/insert-left data)
               (zip/insert-left (construct/space)))))))

(defn insert
  "inserts an element and moves"
  {:added "3.0"}
  ([nav data]
   (cond (level-empty? nav)
         (insert-empty nav data)

         :else
         (let [nav (position-left nav)]
           (-> nav
               (zip/step-right)
               (zip/insert-left (construct/space))
               (zip/insert-right data))))))

(defn insert-all
  "inserts all expressions into the block"
  {:added "3.0"}
  ([nav [data & more :as arr]]
   (reduce insert nav arr)))

(defn insert-newline
  "insert newline/s into the block"
  {:added "3.0"}
  ([nav]
   (insert-newline nav 1))
  ([nav num]
   (reduce zip/insert-left nav
           (take num (repeatedly (fn [] (construct/newline)))))))

(defn insert-space
  "insert space/s into the block"
  {:added "3.0"}
  ([nav]
   (insert-space nav 1))
  ([nav num]
   (reduce zip/insert-left nav
           (take num (repeatedly (fn [] (construct/space)))))))

(defn delete-left
  "deletes left of the current expression"
  {:added "3.0"}
  ([nav]
   (cond (level-empty? nav)
         (tighten-left nav)

         :else
         (loop [nav (position-right nav)]
           (let [elem (zip/left-element nav)]
             (cond (nil? elem)
                   nav

                   (base/expression? elem)
                   (zip/delete-left nav)

                   :else
                   (recur (zip/delete-left nav))))))))

(defn delete-right
  "deletes right of the current expression"
  {:added "3.0"}
  ([nav]
   (cond (level-empty? nav)
         (tighten-right nav)

         :else
         (loop [nav (position-left nav)]
           (let [elem (zip/right-element (zip/step-right nav))]
             (cond (nil? elem)
                   (if (type/void-block? (zip/right-element nav))
                     (zip/delete-right nav)
                     nav)

                   (type/void-block? elem)
                   (-> nav
                       (zip/step-right)
                       (zip/delete-right)
                       (zip/step-left)
                       (recur))

                   (type/void-block? (zip/right-element nav))
                   (-> nav
                       (zip/delete-right)
                       (zip/delete-right))

                   :else
                   (-> nav
                       (zip/step-right)
                       (zip/delete-right)
                       (zip/step-left))))))))

(defn delete
  "deletes the current element"
  {:added "3.0"}
  ([nav]
   (cond (level-empty? nav)
         (tighten-right nav)

         :else
         (loop [nav (-> nav
                        (position-right)
                        (zip/delete-right))]
           (let [elem (zip/right-element nav)]
             (cond (nil? elem)
                   nav

                   (base/expression? elem)
                   nav

                   :else
                   (recur (zip/delete-right nav))))))))

(defn backspace
  "the reverse of insert"
  {:added "3.0"}
  ([nav]
   (let [nav (-> nav
                 (tighten-left)
                 (delete))]
     (if-let [pnav (left nav)]
       pnav
       nav))))

(defn replace
  "replaces an element at the cursor"
  {:added "3.0"}
  ([nav data]
   (cond (level-empty? nav)
         (throw (ex-info "Replace failed - level empty." {:nav nav}))

         :else
         (let [nav (position-right nav)]
           (cond (base/expression? (zip/right-element nav))
                 (zip/replace-right nav data)

                 :else
                 (throw (ex-info "Replace failed - no element" {:nav nav})))))))

(defn replace-splice
  "replaces an element at the cursor"
  {:added "3.0"}
  ([nav data]
   (cond (level-empty? nav)
         (throw (ex-info "Replace failed - level empty." {:nav nav}))

         :else
         (let [nav (position-right nav)]
           (cond (base/expression? (zip/right-element nav))
                 (delete
                  (reduce (fn [nav item]
                            (insert-token-to-right nav item))
                          nav
                          (reverse data)))
                 
                 :else
                 (throw (ex-info "Replace failed - no element" {:nav nav})))))))

(defn swap
  "replaces an element at the cursor"
  {:added "3.0"}
  ([nav f]
   (cond (level-empty? nav)
         (throw (ex-info "Replace failed - level empty." {:nav nav}))

         :else
         (let [nav  (position-right nav)
               prev (value nav)]
           (cond (base/expression? (zip/right-element nav))
                 (zip/replace-right nav (f prev))

                 :else
                 (throw (ex-info "Replace failed - no element" {:nav nav})))))))

(defn update-children
  "replaces the current children"
  {:added "3.0"}
  ([nav children]
   (let [elem (zip/right-element nav)
         nelem (base/replace-children elem children)]
     (zip/replace-right nav nelem))))

(defn line-info
  "returns the line info for the current block"
  {:added "3.0"}
  ([nav]
   (let [block (zip/get nav)
         
         [y x] (:position nav)
         height (base/block-height block)
         width  (base/block-width block)
         [row col] [(inc y) (inc x)]]
     {:row row
      :col col
      :end-row (+ row height)
      :end-col (if (zero? height)
                 (+ col width)
                 (inc width))})))
