(ns code.query.traverse
  (:require [code.query.common :as common]
            [code.query.match.optional :as optional]
            [code.query.match.pattern :as pattern]
            [code.edit :as nav]
            [std.lib.zip :as zip]
            [std.lib :as h]))

(defrecord Position [source pattern op]
  Object
  (toString [pos]
    (str "#pos" {:source (nav/value source)
                 :pattern (zip/right-element pattern)})))

(defmethod print-method Position
  ([v ^java.io.Writer w]
   (.write w (str v))))

(defn pattern-zip
  "creates a clojure.zip pattern"
  {:added "3.0"}
  ([root]
   (zip/zipper root
               (merge {:create-container  list
                       :create-element    identity
                       :is-container?     #(or (seq? %) (vector? %))
                       :is-empty-container? empty?
                       :is-element?       (complement nil?)
                       :list-elements     seq
                       :update-elements   (fn [container children] (with-meta children (meta container)))
                       :add-element       conj}
                      zip/+base+))))

(defn wrap-meta
  "helper for traversing meta tags"
  {:added "3.0"}
  ([f]
   (fn [{:keys [source level] :as pos}]
     (if (not= :meta (nav/tag source))
       (f pos)
       (let [ppos   (if level (update-in pos [:level] inc) pos)
             npos   (f (assoc ppos :source (-> source nav/down nav/right)))]
         (if (:end npos)
           npos
           (assoc npos
                  :source (-> (:source npos) nav/up)
                  :level level)))))))

(defn wrap-delete-next
  "wrapper for deleting next element in the zip"
  {:added "3.0"}
  ([f]
   (fn [{:keys [source pattern next] :as pos}]
     (if next
       (if-let [nsource (nav/right source)]
         (f (-> pos
                (assoc :source nsource
                       :pattern (zip/step-right pattern))
                (dissoc :next)))
         (-> pos
             (assoc :source (nav/up source)
                    :pattern (zip/step-outside pattern))
             (dissoc :next)))
       (f pos)))))

(defn traverse-delete-form
  "traversing deletion form"
  {:added "3.0"}
  ([{:keys [source pattern op] :as pos}]
   (let [sexpr (nav/value source)
         pnode (zip/get pattern)]
     ((:delete-level op) (assoc pos
                                :source  (nav/down source)
                                :pattern (zip/step-inside pattern))))))

(defn traverse-delete-node
  "traversing deletion node"
  {:added "3.0"}
  ([{:keys [source pattern op] :as pos}]
   (cond (and (nav/left-most? source)
              (nav/right-most? source))
         (assoc pos
                :source (nav/up (nav/delete source))
                :pattern (zip/step-outside pattern))

         :else
         ((:delete-level op)
          (assoc pos
                 :source (-> (nav/delete source)
                             (nav/left))
                 :pattern pattern
                 :next true)))))

(defn traverse-delete-level
  "traversing deletion level"
  {:added "3.0"}
  ([{:keys [source pattern op] :as pos}]
   (let [pnode (zip/get pattern)
         sexpr (nav/value source)
         delete? (-> pnode meta :-)]
     (cond (= '& pnode)
           ((:delete-level op) (assoc pos
                                      :source  (nav/right-most source)
                                      :pattern (zip/step-right-most pattern)
                                      :next true))

           delete?
           ((:delete-node op) pos)
           #_(-> ((:delete-node op) pos)
                 (assoc :next true)
                 ((:delete-level op)))

           (or (list? pnode) (vector? pnode))
           (-> pos
               ((:delete-form op))
               (assoc :next true)
               ((:delete-level op)))

           :else
           ((:delete-level op) (assoc pos :next true))))))

(defn prep-insert-pattern
  "helper for insertion"
  {:added "3.0"}
  ([pattern]
   (let [pnode (zip/get pattern)
         {evaluate? :%} (meta pnode)]
     (if evaluate? (eval pnode) (with-meta pnode nil)))))

(defn wrap-insert-next
  "wrapper for insertion"
  {:added "3.0"}
  ([f]
   (fn [{:keys [source pattern next] :as pos}]
     (if-not next
       (f pos)
       (let [nsource  (nav/right source)
             npattern (zip/step-right pattern)]
         (cond (and nsource npattern)
               (f (-> pos
                      (assoc :source nsource
                             :pattern npattern)
                      (dissoc :next)))

               (and npattern (not= '& (zip/get npattern)))
               (let [inserts (->> (iterate zip/step-right npattern)
                                  (take-while zip/get)
                                  (map prep-insert-pattern))
                     nsource (reduce nav/insert-right source (reverse inserts))]
                 (-> pos
                     (assoc :source  (nav/up nsource)
                            :pattern (zip/step-outside pattern))
                     (dissoc :next)))

               :else
               (-> pos
                   (assoc :source (nav/up source) :pattern (zip/step-outside pattern))
                   (dissoc :next))))))))

(comment
  (source
   (traverse (nav/parse-string "(hello)")
             '(^:- hello))))

(defn traverse-insert-form
  "traversing insertion form"
  {:added "3.0"}
  ([{:keys [source pattern op] :as pos}]
   (let [sexpr (nav/value source)
         pnode (zip/get pattern)]
     ((:insert-level op) (assoc pos
                                :source (nav/down source)
                                :pattern (zip/step-inside pattern))))))

(defn traverse-insert-node
  "traversing insertion node"
  {:added "3.0"}
  ([{:keys [source pattern op] :as pos}]
   ((:insert-level op)
    (let [val (prep-insert-pattern pattern)]
      (assoc pos
             :source (nav/insert-left source val)
             :next true)))))

(defn traverse-insert-level
  "traversing insertion level"
  {:added "3.0"}
  ([{:keys [source pattern op] :as pos}]
   (let [pnode (zip/get pattern)
         sexpr (nav/value source)
         insert? (-> pnode meta :+)]
     (cond (= '& pnode)
           ((:insert-level op) (assoc pos
                                      :source  (nav/right-most source)
                                      :pattern (zip/step-right-most pattern)
                                      :next true))

           insert?
           ((:insert-node op) pos)

           (or (list? pnode) (vector? pnode))
           (-> pos
               ((:insert-form op))
               (assoc :next true)
               ((:insert-level op)))

           :else
           ((:insert-level op) (assoc pos :next true))))))

(defn wrap-cursor-next
  "wrapper for locating cursor"
  {:added "3.0"}
  ([f]
   (fn [{:keys [source pattern next end] :as pos}]
     (cond end pos

           next
           (let [nsource (nav/right source)
                 npattern (zip/step-right pattern)]
             (cond (and nsource npattern)
                   (f (-> pos
                          (assoc :source nsource :pattern npattern)
                          (dissoc :next)))

                   npattern
                   (f (-> pos
                          (assoc :source source :pattern npattern)
                          (dissoc :next)))

                   (nil? nsource)
                   (-> pos
                       (assoc :source (nav/up source) :pattern (zip/step-outside pattern))
                       (update-in [:level] dec)
                       (dissoc :next))))
           :else (f pos)))))

(defn traverse-cursor-form
  "traversing cursor form"
  {:added "3.0"}
  ([{:keys [source pattern op] :as pos}]
   (let [sexpr (nav/value source)
         pnode (zip/get pattern)
         pos   (update-in pos [:level] inc)]
     (cond (empty? pnode)
           pos

           :else
           ((:cursor-level op) (assoc pos
                                      :source (nav/down source)
                                      :pattern (zip/step-inside pattern)))))))

(defn traverse-cursor-level
  "traversing cursor level"
  {:added "3.0"}
  ([{:keys [source pattern op] :as pos}]
   (let [sexpr (nav/value source)
         pnode (zip/get pattern)]
     (cond (= '| pnode)
           ((:cursor-level op) (assoc pos :end true))

           (= '& pnode)
           ((:cursor-level op) (assoc pos
                                      :source  (nav/right-most source)
                                      :pattern (zip/step-right-most pattern)
                                      :next true))

           (and (or (list? pnode) (vector? pnode))
                (not (empty? pnode)))
           (-> pos
               ((:cursor-form op))
               (assoc :next true)
               ((:cursor-level op)))

           :else
           ((:cursor-level op) (assoc pos :next true))))))

(defn count-elements
  "counting elements"
  {:added "3.0"}
  ([pattern]
   (let [sum (atom 0)]
     (h/postwalk (fn [x] (swap! sum inc))
                 pattern)
     @sum)))

(defn traverse
  "basic traverse functions
   (source
    (traverse (nav/parse-string \"^:a (+ () 2 3)\")
              '(+ () 2 3)))
   => '(+ () 2 3)
 
   (source
    (traverse (nav/parse-string \"^:a (hello)\")
              '(hello)))
   => '(hello)"
  {:added "3.0"}
  ([source pattern]
   (let [pseq    (optional/pattern-seq pattern)
         lookup   (h/map-juxt [common/prepare-deletion identity] pseq)
         p-dels   (->> (nav/value source)
                       ((pattern/pattern-matches (common/prepare-deletion pattern))))
         p-del   (case  (count p-dels)
                   0 (throw (ex-info "Needs to have a match."
                                     {:matches p-dels
                                      :source (nav/value source)
                                      :pattern pattern}))
                   1 (first p-dels)
                   (->> p-dels
                        (sort-by count-elements)
                        (last)))
         p-match (get lookup p-del)
         p-ins   (common/prepare-insertion p-match)
         op-del  {:delete-form  (wrap-meta traverse-delete-form)
                  :delete-level (wrap-delete-next traverse-delete-level)
                  :delete-node  traverse-delete-node}
         del-pos (-> (map->Position {:source  source
                                     :pattern (pattern-zip p-del)
                                     :op op-del})
                     ((:delete-form op-del)))
         op-ins  {:insert-form  (wrap-meta traverse-insert-form)
                  :insert-level (wrap-insert-next traverse-insert-level)
                  :insert-node  traverse-insert-node}
         ins-pos (-> del-pos
                     (assoc :pattern (pattern-zip p-ins)
                            :op op-ins)
                     ((:insert-form op-ins)))
         p-cursor (common/remove-items common/deletion? p-match)]
     (if (= p-cursor p-ins)
       ins-pos
       (let [op-cursor {:cursor-form  (wrap-meta traverse-cursor-form)
                        :cursor-level (wrap-cursor-next traverse-cursor-level)}
             cursor-pos (-> ins-pos
                            (assoc :pattern (pattern-zip p-cursor)
                                   :op op-cursor
                                   :level 0)
                            ((:cursor-form op-cursor)))]
         cursor-pos)))))

(defn source
  "retrives the source of a traverse
 
   (source
    (traverse (nav/parse-string \"()\")
              '(^:+ hello)))
   => '(hello)"
  {:added "3.0"}
  ([pos]
   (-> pos :source nav/value)))

(comment
  (def source (nav/parse-string "(#|hello)"))

  (-> (nav/parse-string "(#|hello)")
      (nav/delete source))

  (nav/delete *source))

(comment
  (source
   (traverse (nav/parse-string "(hello)")
             '(^:- hello)))
  (source
   (traverse (nav/parse-string "(hello)")
             '(hello)))

  (source
   (traverse (nav/parse-string "(hello)")
             '(^:- hello)))

  (source
   (traverse (nav/parse-string "((hello))")
             '((^:- hello))))

  (./reset ['std.block])
  (./run ['std.block]))
