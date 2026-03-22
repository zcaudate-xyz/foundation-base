(ns std.lib.atom
  (:require [std.lib.collection :as c]
            [std.lib.foundation :as h]))

(defn update-diff
  "updates a diff in a sub nesting"
  {:added "4.0"}
  [m path f & args]
  (let [out (volatile! nil)
        curr (update-in m path (fn [prev]
                                 (let [[diff curr] (apply f prev args)
                                       _ (vreset! out diff)]
                                   curr)))]
    [@out curr]))

(defn swap-return!
  "returns output and new state of atom"
  {:added "3.0" :style/indent 1}
  ([atm f]
   (swap-return! atm f false))
  ([atm f state?]
   (let [output (volatile! nil)
         new (swap! atm (fn [v]
                          (let [[return new] (f v)
                                _ (vreset! output return)]
                            new)))]
     (if state?
       [@output new]
       @output))))

(defn atom:keys
  "lists the nested keys of an atom"
  {:added "3.0"}
  ([state path]
   (keys (get-in (deref state) path))))

(defn atom:get
  "gets all the nested keys within an atom"
  {:added "3.0"}
  ([state path]
   (get-in (deref state) path)))

(defn atom:mget
  "gets all the nested keys within an atom"
  {:added "3.0"}
  ([state paths]
   (let [m (deref state)]
     (mapv #(get-in m %) paths))))

(defn atom-put-fn
  "constructs the output and next state for a put operation"
  {:added "4.0"}
  [prev path m]
  (let [sprev (get-in prev path)
        scurr (c/merge-nested sprev m)]
    (if (empty? path)
      [[sprev scurr] scurr]
      [[sprev scurr] (assoc-in prev path scurr)])))

(defn atom:put
  "puts an entry into the atom"
  {:added "3.0"}
  ([state path m]
   (swap-return! state
     (fn [prev] (atom-put-fn prev path m)))))

(defn atom-reduce-fn
  "helper function for mutations on atom"
  {:added "3.0"}
  ([prev f tx entries]
   (let [[curr arr] (reduce (fn [[prev arr] [path val]]
                              (let [old (get-in prev path)
                                    new (f val old)]
                                [(if (empty? path)
                                   new
                                   (assoc-in prev path new))
                                 (conj arr [path val old new])]))
                            [prev []]
                            entries)
         ret    (map (fn [[path val old new]]
                       (tx path val old new))
                     arr)]
     [ret curr])))

(defn atom-set-fn
  "constructs the output and next state for a set operation"
  {:added "4.0"}
  [prev paths]
  (atom-reduce-fn prev
                  (fn [v _] v)
                  (fn [path _ old new] [path old new])
                  paths))

(defn atom:set
  "sets the entries given a set of inputs"
  {:added "3.0"}
  ([state path m & more]
   (let [paths (cons [path m] (partition 2 more))]
     (swap-return! state
       (fn [prev] (atom-set-fn prev paths))))))

(defn atom-set-keys-fn
  "constructs the output and next state for a set-keys operation"
  {:added "4.0"}
  [prev path m]
  (let [paths (reduce-kv (fn [arr k v]
                           (conj arr [(conj (vec path) k) v]))
                         []
                         m)]
    (atom-set-fn prev paths)))

(defn atom:set-keys
  "sets the entries given a set of inputs"
  {:added "3.0"}
  ([state path m]
   (swap-return! state
     (fn [prev] (atom-set-keys-fn prev path m)))))

(defn atom:set-changed
  "figure out what has changed in set"
  {:added "3.0"}
  ([outputs]
   (let [changed (->> (mapv (fn [[path old new]]
                              [path (if (not= old new)
                                        [:+ new])])
                            outputs)
                      (filter (comp not-empty second))
                      (reduce (fn [out [path changed]]
                                (if (map? changed)
                                  (update-in out path c/merge-nested changed)
                                  (assoc-in out path (second changed))))
                              {}))]
     (if (empty? changed)
       [:no-change]
       [:changed changed]))))

(defn atom:put-changed
  "figure out what has changed in put operation"
  {:added "3.0"}
  ([[old new]]
   (let [changed (c/diff:changed new old)]
     (if (empty? changed)
       [:no-change]
       [:changed changed]))))

(defn atom-swap-fn
  "constructs the output and next state for a swap operation"
  {:added "4.0"}
  [prev paths]
  (atom-reduce-fn prev (fn [f old] (f old))
                  (fn [path _ old new] [path old new])
                  paths))

(defn ^{:style/indent 1}
  atom:swap
  "swaps entries atomically given function"
  {:added "3.0"}
  ([state path f & more]
   (let [paths (cons [path f] (partition 2 more))]
     (swap-return! state
       (fn [prev]
         (atom-swap-fn prev paths))))))

(defn atom-delete-fn
  "constructs the output and next state for a delete operation"
  {:added "4.0"}
  [prev paths]
  (atom-reduce-fn prev
                  (fn [k old] (dissoc old k))
                  (fn [path k old _new] [(conj path k) (get old k)])
                  (map (juxt (comp vec butlast) last) paths)))

(defn atom:delete
  "deletes individual enties from path"
  {:added "3.0"}
  ([state path & more]
   (let [paths (cons path more)]
     (swap-return! state
       (fn [prev] (atom-delete-fn prev paths))))))

(defn atom:clear
  "clears the previous entry"
  {:added "3.0"}
  ([state path]
   (swap-return! state
                 (fn [prev]
                   (if (empty? path)
                     [prev {}]
                     [(get-in prev path)
                      (c/dissoc-nested prev path)])))))

(defn atom-batch-fn
  "constructs the output and next state for a batch operation"
  {:added "4.0"}
  [prev cmds]
  (atom-reduce-fn prev
                  (fn [[cmd k & args] old]
                    (case cmd
                      :set    (assoc old k (first args))
                      :put    (update old k
                                      (fn [x]
                                        (if (map? x)
                                          (c/merge-nested x (first args))
                                          (first args))))
                      :swap   (apply update old k args)
                      :delete (dissoc old k)))
                  (fn [path [_ k] old new]
                    [(conj path k) (get old k) (get new k)])
                  (map (juxt (comp vec butlast second)
                             #(update-in % [1] last))
                       (map vec cmds))))

(defn atom:batch
  "performs a batched operation given keys"
  {:added "3.0"}
  ([state cmds]
   (swap-return! state
     (fn [prev] (atom-batch-fn prev cmds)))))

(defn atom:cursor
  "adds a cursor to the atom to swap on any change"
  {:added "3.0"}
  ([ref selector]
   (atom:cursor ref selector (str (h/sid))))
  ([ref selector key]
   (let [getter  (fn [m] (get-in m selector))
         setter  (fn [m v] (assoc-in m selector v))
         initial (getter @ref)
         cursor  (atom initial)]
     (add-watch ref key (fn [_ _ _ v]
                          (let [cv (getter v)]
                            (if (not= cv @cursor)
                              (reset! cursor cv)))))
     (add-watch cursor key (fn [_ _ _ v]
                             (swap! ref setter v)))
     cursor)))

(defn atom:derived
  "constructs an atom derived from other atoms"
  {:added "3.0"}
  ([atoms f]
   (atom:derived atoms f (str (h/sid))))
  ([atoms f key]
   (let [cache     (volatile! (map deref atoms))
         derived-fn #(apply f @cache)
         derived  (atom (derived-fn))]
     (doseq [atom atoms]
       (add-watch atom key
                  (fn [_ _ _ _]
                    (let [results (map deref atoms)]
                      (when (not= @cache results)
                        (vreset! cache results)
                        (reset! derived (derived-fn)))))))
     derived)))
