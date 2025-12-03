(ns std.lib.zip
  (:require [std.lib.foundation :as h]
            [std.lib.walk :as walk])
  (:refer-clojure :exclude [find get]))

(declare display-zipper)

(def ^:dynamic *handler* nil)

(defonce +types+ (atom {}))

(defn register-type
  "registers a zip type"
  {:added "4.0"}
  [type context]
  (swap! +types+ assoc type context))

(defn unregister-type
  "unregisters a zip type"
  {:added "4.0"}
  [type context]
  (swap! +types+ dissoc type))

(def +nil-handler+
  {:at-inside-most h/NIL
   :at-left-most h/NIL
   :at-right-most h/NIL
   :at-inside-most-left h/NIL
   :at-outside-most h/NIL})

(defrecord Zipper [context prefix display]
  Object
  (toString [{:keys [parent]
              :as obj}]
    (str (or prefix "#zip")
         (if display
           (display obj)
           (display-zipper obj)))))

(defmethod print-method Zipper
  ([v ^java.io.Writer w]
   (.write w (str v))))

(defn check-context
  "checks that the zipper contains valid functions"
  {:added "3.0"}
  ([context]
   (let [missing (->> [:create-container
                       :create-element
                       :cursor
                       :is-container?
                       :is-empty-container?
                       :is-element?
                       :list-elements
                       :update-elements
                       :add-element
                       :at-left-most?
                       :at-right-most?
                       :at-inside-most?
                       :at-inside-most-left?
                       :at-outside-most?]
                      (remove context))]
     (if (seq missing)
       (throw (ex-info "Missing keys." {:keys missing}))
       context))))

(defn check-optional
  "checks that the meta contains valid functions"
  {:added "3.0"}
  ([context]
   (let [missing (->> [:update-step-left
                       :update-step-right
                       :update-step-inside
                       :update-step-inside-left
                       :update-step-outside
                       :update-delete-left
                       :update-delete-right
                       :update-insert-left
                       :update-insert-right
                       :wrap-data
                       :unwrap-data]
                      (remove context))]
     (if (seq missing)
       (throw (ex-info "Missing keys." {:keys missing}))
       context))))

(defn zipper?
  "checks to see if an object is a zipper"
  {:added "3.0"}
  ([x]
   (instance? Zipper x)))

(defn zipper
  "constructs a zipper"
  {:added "3.0"}
  ([root context]
   (zipper root context {}))
  ([root context opts]
   (let [{:keys [wrap-data]
          :or {wrap-data identity}} context]
     (map->Zipper (merge opts
                         {:depth   0
                          :left    ()
                          :right   (list (wrap-data root))
                          :context (check-context context)})))))

(defn unwrap-element
  "unwraps an element"
  {:added "4.0"}
  ([{:keys [context]
     :as zip}
    element]
   (let [{:keys [unwrap-data]
          :or {unwrap-data identity}} context]
     (unwrap-data element))))

(defn left-element
  "element directly left of current position"
  {:added "3.0"}
  ([zip]
   (first (:left zip))))

(defn right-element
  "element directly right of current position"
  {:added "3.0"}
  ([zip]
   (first (:right zip))))

(defn left-elements
  "all elements left of current position"
  {:added "3.0"}
  ([zip]
   (reverse (:left zip))))

(defn right-elements
  "all elements right of current position"
  {:added "3.0"}
  ([zip]
   (:right zip)))

(defn current-elements
  "all elements left and right of current position"
  {:added "3.0"}
  ([{:keys [left right] :as zip}]
   (concat (reverse left) right)))

(defn is
  "checks zip given a predicate"
  {:added "3.0"}
  ([zip pred]
   (is zip pred :right))
  ([zip pred step]
   (let [elem (first (clojure.core/get zip step))]
     (try
       (or  (= elem pred)

            (if (and (ifn? pred)
                     (not (coll? pred)))
              (pred elem))

            (zero? (compare elem pred)))
       (catch Throwable t
         false)))))

(defn get
  "gets the value of the zipper"
  {:added "3.0"}
  ([zip]
   (get zip identity :right))
  ([zip arg]
   (if (or (= arg :left)
           (= arg :right))
     (get zip identity arg)
     (get zip arg :right)))
  ([zip func step]
   (let [elem (first (clojure.core/get zip step))]
     (func elem))))

(defn is-container?
  "checks if node on either side is a container
 
   (-> (vector-zip [1 2 3])
       (is-container? :right))
   => true
 
   (-> (vector-zip [1 2 3])
       (is-container? :left))
   => false"
  {:added "3.0"}
  ([zip]
   (is-container? zip :right))
  ([{:keys [context] :as zip} step]
   (is zip (-> context :is-container?) step)))

(defn is-empty-container?
  "check if current container is empty
 
   (-> (vector-zip [])
       (is-empty-container?))
   => true"
  {:added "3.0"}
  ([zip]
   (is-empty-container? zip :right))
  ([{:keys [context] :as zip} step]
   (is zip (-> context :is-empty-container?) step)))

(defn at-left-most?
  "check if at left-most point of a container"
  {:added "3.0"}
  ([zip]
   (empty? (:left zip))))

(defn at-right-most?
  "check if at right-most point of a container"
  {:added "3.0"}
  ([zip]
   (empty? (:right zip))))

(defn at-inside-most?
  "check if at inside-most point of a container"
  {:added "3.0"}
  ([zip]
   (or (empty? (:right zip))
       (not (is-container? zip :right)))))

(defn at-inside-most-left?
  "check if at inside-most left point of a container"
  {:added "3.0"}
  ([zip]
   (or (empty? (:left zip))
       (not (is-container? zip :left)))))

(defn at-outside-most?
  "check if at outside-most point of the tree"
  {:added "3.0"}
  ([zip]
   (nil? (:parent zip))))

(defonce +base+
  {:cursor               '|
   :at-left-most?        at-left-most?
   :at-right-most?       at-right-most?
   :at-inside-most?      at-inside-most?
   :at-inside-most-left? at-inside-most-left?
   :at-outside-most?     at-outside-most?})

(defn seq-zip
  "constructs a sequence zipper"
  {:added "3.0"}
  ([root]
   (seq-zip root nil))
  ([root opts]
   (zipper root
           (merge {:create-container    list
                   :create-element      identity
                   :is-container?       seq?
                   :is-empty-container? empty?
                   :is-element?         (complement nil?)
                   :list-elements       identity
                   :update-elements     (fn [container new-elements] (apply list new-elements))
                   :add-element         (fn [container element] (concat container [element]))}
                  +base+)
           opts)))

(defn vector-zip
  "constructs a vector based zipper"
  {:added "3.0"}
  ([root]
   (vector-zip root nil))
  ([root opts]
   (zipper root
           (merge {:create-container  vector
                   :create-element    identity
                   :is-container?     vector?
                   :is-empty-container? empty?
                   :is-element?         (complement nil?)
                   :list-elements     seq
                   :update-elements   (fn [_ new-elements] (vec new-elements))
                   :add-element       conj}
                  +base+)
           opts)))

(defn list-child-elements
  "lists elements of a container"
  {:added "3.0"}
  ([zip]
   (list-child-elements zip :right))
  ([{:keys [context] :as zip} direction]
   (let [elem (first (clojure.core/get zip direction))
         check-fn  (:is-container? context)
         list-fn   (:list-elements context)]
     (if (check-fn elem)
       (list-fn elem)
       (throw (ex-info "Not a org." {:element elem}))))))

(defn update-child-elements
  "updates elements of a container"
  {:added "3.0"}
  ([zip child-elements]
   (update-child-elements zip child-elements :right))
  ([{:keys [context] :as zip} child-elements direction]
   (update-in zip
              [direction]
              (fn [elements]
                (let [check-fn  (:is-container? context)
                      update-fn (:update-elements context)
                      old   (first elements)
                      _     (if-not (check-fn old)
                              (throw (ex-info "Not a container." {:element old})))
                      new   (update-fn old
                                       child-elements)]
                  (cons new (rest elements)))))))

(defn can-step-left?
  "check if can step left from current status
 
   (-> (from-status '[1 2 [3 | 4]])
       (can-step-left?))
   => true
 
   (-> (from-status '[1 2 [| 3 4]])
       (can-step-left?))
   => false"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (not ((:at-left-most? context) zip))))

(defn can-step-right?
  "check if can step right from current status
 
   (-> (from-status '[1 2 [3 | 4]])
       (can-step-right?))
   => true
 
   (-> (from-status '[1 2 [3 4 |]])
       (can-step-right?))
   => false"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (not ((:at-right-most? context) zip))))

(defn can-step-inside?
  "check if can step down from current status"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (not ((:at-inside-most? context) zip))))

(defn can-step-inside-left?
  "check if can step left inside a container"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (not ((:at-inside-most-left? context) zip))))

(defn can-step-outside?
  "check if can step up from current status"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (not ((:at-outside-most? context) zip))))

(defn step-left
  "step left from current status"
  {:added "3.0"}
  ([{:keys [left context right] :as zip}]
   (cond ((:at-left-most? context) zip)
         (if-let [zip-fn (get-in *handler* [:step :at-left-most])]
           (zip-fn zip)
           zip)

         :else
         (let [elem (first left)]
           (-> zip
               (assoc :left (rest left))
               (assoc :right (cons elem right))
               (h/call (:update-step-left context) elem)))))
  ([zip n]

 
   (nth (iterate step-left zip) n)))

(defn step-right
  "step right from current status"
  {:added "3.0"}
  ([{:keys [left right context] :as zip}]
   (cond ((:at-right-most? context) zip)
         (if-let [zip-fn (get-in *handler* [:step :at-right-most])]
           (zip-fn zip)
           zip)

         :else
         (let [elem (first right)
               res (-> zip
                       (assoc :left  (cons elem left))
                       (assoc :right (rest right))
                       (h/call (:update-step-right context) elem))]
           res)))
  ([zip n]
   (nth (iterate step-right zip) n)))

(defn step-inside
  "step down from current status"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (cond ((:at-right-most? context) zip)
         (if-let [zip-fn (get-in *handler* [:step :at-right-most])]
           (zip-fn zip)
           zip)

         ((:at-inside-most? context) zip)
         (if-let [zip-fn (get-in *handler* [:step :at-inside-most])]
           (zip-fn zip)
           zip)

         :else
         (let [elem     (right-element zip)
               children (list-child-elements zip :right)]
           (-> zip
               (assoc :depth (inc (:depth zip))
                      :left ()
                      :right children
                      :parent zip)
               (h/call (:update-step-inside context) elem)))))
  ([zip n]
   (nth (iterate step-inside zip) n)))

(defn step-inside-left
  "steps into the form on the left side"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (cond ((:at-left-most? context) zip)
         (if-let [zip-fn (get-in *handler* [:step :at-left-most])]
           (zip-fn zip)
           zip)

         ((:at-inside-most-left? context) zip)
         (if-let [zip-fn (get-in *handler* [:step :at-inside-most-left])]
           (zip-fn zip)
           zip)

         :else
         (let [elem     (left-element zip)
               children (list-child-elements zip :left)
               {:keys [left right depth]} zip
               parent (assoc zip
                             :left (rest left)
                             :right (cons (first left) right))]
           (-> zip
               (assoc :depth (inc  depth) :left (reverse children) :right () :parent parent)
               (h/call (:update-step-inside-left context) elem)))))
  ([zip n]
   (nth (iterate step-inside-left zip) n)))

(defn step-outside
  "step out to the current container"
  {:added "3.0"}
  ([zip]
   (let [{:keys [context left right parent depth]} zip]
     (cond ((:at-outside-most? context) zip)
           (if-let [zip-fn (get-in *handler* [:step :at-outside-most])]
             (zip-fn zip)
             zip)

           :else
           (let [elements (concat (reverse left) right)
                 body  {:left   (:left parent)
                        :right  (:right parent)
                        :parent (:parent parent)
                        :depth  (dec depth)}]
             (cond-> (merge zip body)
               (:changed? zip)  (update-child-elements elements)
               :then (h/call (:update-step-outside context) left))))))
  ([zip n]
   (nth (iterate step-outside zip) n)))

(defn step-outside-right
  "the right of the current container"
  {:added "3.0"}
  ([zip]
   (-> zip
       (step-outside)
       (step-right)))
  ([zip n]
   (nth (iterate step-outside-right zip) n)))

(defn step-left-most
  "step to left-most point of current container"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (if ((:at-left-most? context) zip)
     zip
     (recur (step-left zip)))))

(defn step-right-most
  "step to right-most point of current container"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (if ((:at-right-most? context) zip)
     zip
     (recur (step-right zip)))))

(defn step-inside-most
  "step to at-inside-most point of current container"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (if ((:at-inside-most? context) zip)
     zip
     (recur (step-inside zip)))))

(defn step-inside-most-left
  "steps all the way inside to the left side"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (if ((:at-inside-most-left? context) zip)
     zip
     (recur (step-inside-left zip)))))

(defn step-outside-most
  "step to outside-most point of the tree"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (if ((:at-outside-most? context) zip)
     zip
     (recur (step-outside zip)))))

(defn step-outside-most-right
  "step to outside-most point of the tree to the right"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (if ((:at-outside-most? context) zip)
     zip
     (step-right (step-outside-most zip)))))

(defn step-end
  "steps status to container directly at end"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (-> zip
       (step-outside-most)
       (step-right-most)
       (step-inside-most-left))))

(defn insert-left
  "insert element/s left of the current status"
  {:added "3.0"}
  ([{:keys [context] :as zip} data]
   (let [create-fn (:create-element context)
         elem (create-fn data)]
     (-> zip
         (update-in [:left] #(cons elem %))
         (assoc :changed? true)
         (h/call (:update-insert-left context) elem))))
  ([{:keys [context] :as zip} data & more]
   (apply insert-left (insert-left zip data) more)))

(defn insert-right
  "insert element/s right of the current status"
  {:added "3.0"}
  ([{:keys [context] :as zip} data]
   (let [create-fn (:create-element context)
         elem (create-fn data)]
     (-> zip
         (update-in [:right] #(cons elem %))
         (assoc :changed? true)
         (h/call (:update-insert-right context) elem))))
  ([{:keys [context] :as zip} data & more]
   (apply insert-right (insert-right zip data) more)))

(defn delete-left
  "delete element/s left of the current status"
  {:added "3.0"}
  ([{:keys [context left] :as zip}]
   (cond ((:at-left-most? context) zip)
         (if-let [zip-fn (get-in *handler* [:delete :at-left-most])]
           (zip-fn zip)
           zip)

         :else
         (let [elem (first left)]
           (-> zip
               (update-in [:left] rest)
               (assoc :changed? true)
               (h/call (:update-delete-left context) elem)))))
  ([{:keys [context] :as zip} n]
   (nth (iterate delete-left zip) n)))

(defn delete-right
  "delete element/s right of the current status"
  {:added "3.0"}
  ([{:keys [context right] :as zip}]
   (cond ((:at-right-most? context) zip)
         (if-let [zip-fn (get-in *handler* [:delete :at-right-most])]
           (zip-fn zip)
           zip)

         :else
         (let [elem (first right)]
           (-> zip
               (update-in [:right] rest)
               (assoc :changed? true)
               (h/call (:update-delete-right context) elem)))))
  ([{:keys [context] :as zip} n]
   (nth (iterate delete-right zip) n)))

(defn replace-left
  "replace element left of the current status"
  {:added "3.0"}
  ([zip data]
   (-> zip
       (delete-left)
       (insert-left data))))

(defn replace-right
  "replace element right of the current status"
  {:added "3.0"}
  ([zip data]
   (-> zip
       (delete-right)
       (insert-right data))))

(defn hierarchy
  "replace element right of the current status"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (loop [out []
          zip (step-outside zip)]
     (if ((:at-outside-most? context) zip)
       (conj out zip)
       (recur (conj out zip) (step-outside zip))))))

(defn at-end?
  "replace element right of the current status"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (and ((:at-right-most? context) zip)
        (->> (hierarchy zip)
             (map step-right)
             (every? (:at-right-most? context))))))

(defn surround
  "nests elements in current block within another container"
  {:added "3.0"}
  ([{:keys [context parent left] :as zip}]
   (let [list-fn    (:list-elements context)
         update-fn  (:update-elements context)
         add-fn     (:add-element context)
         empty-elem ((:create-container context))
         new-elem   (->> (update-fn empty-elem (current-elements zip))
                         (add-fn empty-elem))]
     (cond (nil? parent)
           (let [elem (list-fn new-elem)]
             (-> zip
                 (assoc :left ()
                        :right elem
                        :changed? true)
                 (h/call (:update-step-outside context) left)))

           :else
           (-> (step-outside zip)
               (replace-right new-elem)
               (step-inside))))))

(defn root-element
  "accesses the top level node"
  {:added "3.0"}
  ([zip]
   (-> zip
       (step-outside-most)
       (step-left-most)
       (right-element))))

(defn status
  "returns the form with the status showing"
  {:added "3.0"}
  ([{:keys [context] :as zip}  & [no-cursor]]
   (->> (if no-cursor
          zip
          (insert-left zip (:cursor context)))
        (step-outside-most)
        (current-elements)
        (apply list)
        (unwrap-element zip))))

(defn status-string
  "returns the string form of the status"
  {:added "3.0"}
  ([zip]
   (->> (status zip)
        (apply pr-str))))

(defn display-zipper
  "displays the zipper"
  {:added "4.0"}
  [{:keys [parent context] :as zip}]
  #_(dissoc zip :context :prefix :display :changed?)
  (cond-> zip
      :then  (dissoc :context :prefix :display :changed?)
      parent (assoc :parent
                    (root-element
                     
                     (replace-right parent
                                    ((:update-elements context)
                                     (first (:right parent))
                                     [((:create-element context)
                                       '...)]))))))

(defn step-next
  "step status through the tree in depth first order"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (cond (nil? zip) nil
         (and (can-step-inside? zip)
              (not (is-empty-container? zip)))
         (let [zip (step-inside zip)
               check-fn (:is-element? context)]
           (if (check-fn (right-element zip))
             zip
             (recur (step-right zip))))

         (can-step-right? (step-right zip))
         (let [zip (step-right zip)]
           (if ((:is-element? context) (right-element zip))
             zip
             (recur zip)))

         :else
         (loop [zip (step-outside-right zip)]
           (cond ((:at-outside-most? context) zip)
                 nil

                 ((:is-element? context) (right-element zip))
                 zip

                 (can-step-right? (step-right zip))
                 (step-next (step-right zip))

                 :else
                 (recur (step-outside-right zip)))))))

(defn step-prev
  "step status in reverse through the tree in depth first order"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (cond (nil? zip) nil

         ((:at-outside-most? context) zip)
         nil

         (can-step-left? zip)
         (cond (can-step-inside-left? zip)
               (recur (step-inside-left zip))

               ((:is-element? context) (left-element zip))
               (step-left zip)

               :else
               (recur (step-left zip)))

         :else
         (step-outside zip))))

(defn find
  "helper function for the rest of the `find` series"
  {:added "3.0"}
  ([zip move pred]
   (->> (iterate move zip)
        (drop 1)
        (take-while right-element)
        (filter #(try (pred (right-element %))
                      (catch Throwable t)))
        (first))))

(defn find-left
  "steps status left to search predicate"
  {:added "3.0"}
  ([zip pred]
   (binding [*handler* (assoc-in *handler*
                                 [:step :at-left-most]
                                 (fn [_] nil))]
     (find zip step-left pred))))

(defn find-right
  "steps status right to search for predicate"
  {:added "3.0"}
  ([zip pred]
   (binding [*handler* (assoc-in *handler*
                                 [:step :at-right-most]
                                 (fn [_] nil))]
     (find zip step-right pred))))

(defn find-next
  "step status through the tree in depth first order to the first matching element"
  {:added "3.0"}
  ([zip pred]
   (find zip step-next pred)))

(defn find-prev
  "step status through the tree in reverse order to the last matching element"
  {:added "3.0"}
  ([zip pred]
   (find zip step-prev pred)))

(defn from-status
  "returns a zipper given a data structure with | as the status"
  {:added "3.0"}
  ([data]
   (from-status data vector-zip))
  ([data zipper-fn]
   (let [{:keys [context] :as zip} (zipper-fn data)]
     (if-let [nzip (-> zip
                       (find-next #(zero? (compare (:cursor context) %))))]
       (delete-right nzip)
       zip))))

(defn form-zip-wrap
  "wraps nils for the zipper"
  {:added "4.0"}
  [form]
  (walk/prewalk
   (fn [x]
     (if (nil? x)
       (h/wrapped x nil nil pr-str)
       x))
   form))

(defn form-zip-unwrap
  "unwraps nils for the zipper"
  {:added "4.0"}
  [form]
  (walk/prewalk
   (fn [x]
     (if (h/wrapped? x)
       @x
       x))
   form))

(defn form-zip
  "creates a form zip"
  {:added "4.0"}
  ([root]
   (form-zip root nil))
  ([root opts]
   (zipper root
           (merge {:wrap-data         form-zip-wrap
                   :unwrap-data       form-zip-unwrap
                   :create-container  (fn [] '()) ; Default to an empty list for new containers
                   :create-element    identity
                   :is-container?       (fn [x] (or (list? x) (vector? x) (map? x) (set? x)))
                   :is-empty-container? empty?
                   :is-element?         (complement nil?)
                   :list-elements     seq
                   :update-elements   (fn [container new-elements]
                                        ;; Reconstructs the container, preserving its original type
                                        (cond
                                          (list? container) (apply list new-elements)
                                          (vector? container) (vec new-elements)
                                          (map? container) (into {} new-elements)
                                          (set? container) (into #{} new-elements)
                                          :else (throw (ex-info "Unsupported container type for update" {:type (type container)}))))
                   :add-element       (fn [container element]
                                        ;; Adds an element, preserving container type.
                                        ;; Note: conj behavior varies by collection type (front for lists, end for vectors).
                                        (cond
                                          (list? container) (concat container [element])
                                          (vector? container) (conj container element)
                                          (map? container) (conj container element)
                                          (set? container) (conj container element)
                                          :else (throw (ex-info "Unsupported container type for add" {:type (type container)}))))}
                  +base+)
           opts)))

(defn prewalk
  "emulates std.lib.walk/prewalk behavior with zipper"
  {:added "3.0"}
  ([{:keys [context] :as zip} f]
   (let [elem (right-element zip)
         zip  (replace-right zip (f elem))]
     (cond (can-step-inside? zip)
           (loop [zip  (step-inside zip)]
             (let [zip (-> (prewalk zip f)
                           (step-right))]
               (cond (can-step-right? zip)
                     (recur zip)

                     :else
                     (step-outside zip))))
           :else zip))))

(defn postwalk
  "emulates std.lib.walk/postwalk behavior with zipper"
  {:added "3.0"}
  ([zip f]
   (let [zip (cond (can-step-inside? zip)
                   (loop [zip (step-inside zip)]
                     (let [zip  (postwalk zip f)]
                       (cond (can-step-right? zip)
                             (recur (step-right zip))

                             :else
                             (step-outside zip))))

                   :else zip)]
     (if (can-step-right? zip)
       (let [elem (right-element zip)]
         (replace-right zip (f elem)))
       zip))))

(defn matchwalk
  "performs a match at each level"
  {:added "3.0"}
  ([zip matchers f]
   (matchwalk zip matchers f matchwalk {}))
  ([zip [pred & more :as matchers] f matchwalk {:keys [move-right
                                                       can-move-right?]
                                                :as opts}]
   (let [zip (if (try (pred zip)
                      (catch Throwable t))
               (cond (empty? more)
                     (f zip)

                     (can-step-inside? zip)
                     (step-outside (matchwalk (step-inside zip) more f matchwalk opts))

                     :else
                     zip)
               zip)
         zip (if (can-step-inside? zip)
               (step-outside (matchwalk (step-inside zip) matchers f matchwalk opts))
               zip)
         zip  (if ((or can-move-right?
                       can-step-right?) zip)
                (matchwalk ((or move-right
                                step-right) zip) matchers f matchwalk opts)
                zip)]
     zip)))

(defn levelwalk
  "performs a match at the same level"
  {:added "3.0"}
  ([zip [pred] f]
   (levelwalk zip [pred] f levelwalk {}))
  ([zip [pred] f levelwalk {:keys [move-right
                                   can-move-right?]
                            :as opts}]
   (let [zip  (if (try (pred zip)
                       (catch Throwable t))
                (f zip)
                zip)
         zip  (if ((or can-move-right?
                       can-step-right?) zip)
                (levelwalk ((or move-right
                                step-right) zip) [pred] f levelwalk opts)
                zip)]
     zip)))
