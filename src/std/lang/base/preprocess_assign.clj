(ns std.lang.base.preprocess-assign
  (:require [std.lang.base.emit-helper :as helper]
            [std.lang.base.preprocess-base :as preprocess-base]
            [std.lang.base.util :as ut]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]))

(defn process-inline-assignment
  "prepares the form for inline assignment"
  {:added "4.0"}
  [form modules mopts & [unwrapped]]
  (let [[_ bind-form & rdecl] (reverse form)
        [f & args] bind-form
        [sym-ns sym-id] (ut/sym-pair f)
        {:keys [module]} mopts
        f-module (or (if (= '- sym-ns) (:id module))
                     (get (:link module) sym-ns)
                     (if (get modules sym-ns) sym-ns))
        _ (or f-module
              (f/error "Cannot resolve Module." {:input f
                                                 :current module
                                                 :modules (keys modules)}))
        _ (or (get-in modules [f-module :code sym-id])
              (f/error "Code entry not found:" {:input f
                                                :form form}))]
    (concat (reverse rdecl)
             [(with-meta (cons (cond-> (ut/sym-full f-module sym-id)
                                 (not unwrapped) (volatile!))
                              args)
                {:assign/inline true})])))

(def ^:private +tail-block-heads+
  '#{do do* do:> when when-not let let*})

(defn- rebuild-form
  [form head items]
  (with-meta (apply list head items)
    (meta form)))

(defn- rewrite-tail-return
  [form target]
  (cond (not (collection/form? form))
        (list := target form)

        (= 'return (first form))
        (list := target (second form))

        (+tail-block-heads+ (first form))
        (let [[head & items] form]
          (case head
            (let let*)
            (let [[bindings & body] items]
              (rebuild-form form head
                            (concat [bindings]
                                    (butlast body)
                                    [(rewrite-tail-return (last body) target)])))
            (rebuild-form form head
                          (concat (butlast items)
                                  [(rewrite-tail-return (last items) target)]))))

        (= 'if (first form))
        (let [[head test then else] form]
          (rebuild-form form head
                        [test
                         (rewrite-tail-return then target)
                         (rewrite-tail-return else target)]))

        (= 'if-not (first form))
        (let [[head test then else] form]
          (rebuild-form form head
                        [test
                         (rewrite-tail-return then target)
                         (rewrite-tail-return else target)]))

        (= 'cond (first form))
        (let [[head & clauses] form]
          (rebuild-form form head
                        (mapcat (fn [[pred expr]]
                                  [pred (rewrite-tail-return expr target)])
                                (partition 2 clauses))))

        (= 'try (first form))
        (let [[head & items] form
              [body handlers] (split-with (fn [x]
                                            (not (and (collection/form? x)
                                                      (#{'catch 'finally} (first x)))))
                                          items)
              body (concat (butlast body)
                           [(rewrite-tail-return (last body) target)])
              handlers (map (fn [h]
                              (if (= 'catch (first h))
                                (rebuild-form h 'catch
                                              (concat (butlast (rest h))
                                                      [(rewrite-tail-return (last h) target)]))
                                h))
                            handlers)]
          (rebuild-form form head
                        (concat body handlers)))

        :else
        (list := target form)))

(defn- assignment-target
  [form grammar]
  (case (get-in grammar [:reserved (first form) :emit])
    :assign
    (let [[_ target value] form]
      (when value
        {:declare? false
         :target target
         :value value}))

    :def-assign
    (let [args (helper/emit-typed-args (rest form) grammar)]
      (when (= 1 (count args))
        (let [{:keys [symbol value]} (first args)]
          (when value
            {:declare? true
             :target symbol
             :value value}))))

    nil))

(defn process-template-assignment
  "rewrites rewrite-block xtalk macros in assignment position"
  {:added "4.1"}
  [form grammar mopts]
  (let [form (or (when-let [{:keys [emit macro]} (get-in grammar [:reserved (first form)])]
                   (when (and (= :macro emit)
                              (when-let [op (:op (get-in grammar [:reserved (first form)]))]
                                (.startsWith (name op) "var-"))
                              macro)
                     (let [expanded (binding [preprocess-base/*macro-form* form
                                              preprocess-base/*macro-grammar* grammar
                                              preprocess-base/*macro-opts* mopts]
                                      (macro form))]
                       (when (assignment-target expanded grammar)
                         (with-meta expanded
                           (merge (meta form) (meta expanded)))))))
                 form)]
    (when-let [{:keys [declare? target value]} (assignment-target form grammar)]
      (when (and (collection/form? value)
                 (symbol? (first value)))
          (let [{:keys [emit macro op-spec]} (get-in grammar [:reserved (first value)])]
            (when (and (= :macro emit)
                       (:allow-blocks op-spec)
                       macro)
            (let [expanded (binding [preprocess-base/*macro-form* value
                                     preprocess-base/*macro-grammar* grammar
                                     preprocess-base/*macro-opts* mopts]
                             (macro value))
                  rewritten (rewrite-tail-return expanded target)]
              (cond-> (if declare?
                        (with-meta (list 'do
                                         (with-meta (apply list (concat (butlast form) [nil]))
                                           (meta form))
                                         rewritten)
                          (meta form))
                        (with-meta rewritten
                          (meta form)))
                true (vary-meta assoc :assign/template-default true)))))))))

(defn protect-reserved-head
  [form]
  (with-meta (cons (volatile! (first form))
                   (rest form))
    (meta form)))
