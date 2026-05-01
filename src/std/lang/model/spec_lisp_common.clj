(ns std.lang.model.spec-lisp-common
  (:require [std.lib.collection :as collection]))

(def ^:private +not-found+ ::not-found)
(def ^:private +branch-tags+ #{'if 'elseif 'else})

(defn prepare-top-level
  {:added "4.1"}
  [begin-sym form]
  (if (and (vector? form)
           (:bulk (meta form)))
    (if (= 1 (count form))
      (first form)
      (cons begin-sym form))
    form))

(defn expand-form
  {:added "4.1"}
  [reserved form]
  (cond (collection/form? form)
        (let [op    (first form)]
          (if (= 'br* op)
            (apply list
                   'br*
                   (map (fn [[tag & more]]
                          (if (contains? +branch-tags+ tag)
                            (case tag
                              else (list* tag (map (partial expand-form reserved) more))
                              (list* tag
                                     (expand-form reserved (first more))
                                     (map (partial expand-form reserved) (rest more))))
                            (expand-form reserved (cons tag more))))
                        (rest form)))
            (let [form  (apply list (map (partial expand-form reserved) form))
                  op    (first form)
                  entry (and (symbol? op)
                             (get reserved op))]
              (cond (and entry
                         (= :macro (:emit entry))
                         (:macro entry))
                    (let [expanded ((:macro entry) form)]
                      (if (= expanded form)
                        form
                        (recur reserved expanded)))

                    (and entry
                         (= :hard-link (:emit entry))
                         (:raw entry))
                    (let [expanded (cons (:raw entry) (rest form))]
                      (if (= expanded form)
                        form
                        (recur reserved expanded)))

                    :else
                    form))))

        (vector? form)
        (mapv (partial expand-form reserved) form)

        (map? form)
        (into (empty form)
              (map (fn [[k v]]
                     [(expand-form reserved k)
                      (expand-form reserved v)]))
              form)

        (set? form)
        (set (map (partial expand-form reserved) form))

        :else
        form))

(defn parse-def-assign-bindings
  {:added "4.1"}
  [args]
  (loop [out []
         more args]
    (cond (empty? more)
          out

          (= 2 (count more))
          (conj out [(first more) (second more)])

          (and (<= 3 (count more))
               (= ':= (second more)))
          (recur (conj out [(first more) (nth more 2)])
                 (drop 3 more))

          :else
          (recur (conj out [(first more) (second more)])
                 (drop 2 more)))))

(defn- target-symbols
  [target]
  (cond (symbol? target)
        [target]

        (vector? target)
        (->> target
             (filter symbol?)
             (remove #{'_})
             vec)

        :else
        []))

(defn- ordered-distinct
  [coll]
  (loop [seen #{}
         out  []
         more coll]
    (if-let [x (first more)]
      (if (contains? seen x)
        (recur seen out (rest more))
        (recur (conj seen x) (conj out x) (rest more)))
      out)))

(defn- key-literal
  [k]
  (cond (keyword? k) (name k)
        (symbol? k)  (name k)
        :else        k))

(defn- key-kind
  [raw transformed]
  (cond (or (keyword? raw)
            (string? raw))
        :key

        (and (symbol? raw)
             (nil? (namespace raw)))
        :key

        (number? raw)
        :idx

        (string? transformed)
        :key

        (number? transformed)
        :idx

        :else
        :idx))

(defn- split-try-clauses
  [args]
  (let [[body tail] (split-with #(not (and (collection/form? %)
                                           (#{'catch 'finally} (first %))))
                                args)
        catch       (first (filter #(and (collection/form? %)
                                         (= 'catch (first %)))
                                   tail))
        finally     (first (filter #(and (collection/form? %)
                                         (= 'finally (first %)))
                                   tail))]
    {:body body
     :catch catch
     :finally finally}))

(defn- unpack-form?
  [form]
  (and (collection/form? form)
       (= 'x:unpack (first form))
       (= 2 (count form))))

(defn transform-form
  {:added "4.1"}
   [{:keys [begin
            reserved
            def-form
            lambda-form
            defn-form
            let-form
            while-form
            ternary-form
            try-form
            not-equal-form
            equal-form
            nil-form
            assign-symbol-form
            index-read-form
            index-write-form
            global-symbol
            global-read-form
            global-write-form]
     :as config}
    form]
   (letfn [(begin-form [forms]
             (cond (empty? forms) nil
                   (= 1 (count forms)) (first forms)
                   :else (cons begin forms)))
           (ternary-expr [test then else]
             (if ternary-form
               (ternary-form test then else)
               (list 'if test then else)))
           (var-form? [form]
             (and (collection/form? form)
                  (let [{:keys [emit]} (get reserved (first form))]
                    (= :def-assign emit))))
           (index-prop [prop]
             (cond (symbol? prop)
                   [(key-literal prop) :key]

                   (vector? prop)
                   (let [raw (first prop)
                         key (transform raw)]
                     [key (cond (or (keyword? raw)
                                    (string? raw))
                                :key

                                (number? raw)
                                :idx

                                (string? key)
                                :key

                                (number? key)
                                :idx

                                :else
                                :auto)])

                   :else
                   (let [key (transform prop)]
                     [key (key-kind prop key)])))
          (index-read [obj prop]
            (let [[key kind] (index-prop prop)]
              (index-read-form obj key kind)))
          (index-write [obj prop value]
            (let [[key kind] (index-prop prop)]
              (index-write-form obj key value kind)))
          (global-read [sym]
            (global-read-form global-symbol (key-literal sym)))
          (global-write [sym value]
            (global-write-form global-symbol (key-literal sym) value))
          (assign-target [target value]
            (cond (symbol? target)
                  (assign-symbol-form target value)

                  (and (collection/form? target)
                       (= '!:G (first target)))
                  (global-write (second target) value)

                  (and (collection/form? target)
                       (= '. (first target))
                       (= 1 (count (drop 2 target))))
                  (index-write (transform (second target))
                               (nth target 2)
                               value)

                  :else
                  (list target value)))
          (binding-assignments [target value]
            (let [value (transform value)]
              (cond (symbol? target)
                    [(assign-symbol-form target value)]

                    (vector? target)
                    (let [tmp (gensym "tmp__")]
                      [(let-form [(list tmp value)]
                                 (->> target
                                      (map-indexed (fn [i sym]
                                                     (when (and (symbol? sym)
                                                                (not= sym '_))
                                                       (assign-symbol-form
                                                        sym
                                                        (index-read tmp [i])))))
                                      (remove nil?)
                                      vec))])

                    :else
                    [(assign-target target value)])))
          (transform-var-form [form]
            (->> (parse-def-assign-bindings (rest form))
                 (mapcat (fn [[target value]]
                           (binding-assignments target value)))
                 vec))
          (body-bindings [body]
            (->> body
                 (mapcat (fn [form]
                           (if (var-form? form)
                             (mapcat (fn [[target _]]
                                       (target-symbols target))
                                     (parse-def-assign-bindings (rest form)))
                             [])))
                 ordered-distinct
                 (mapv (fn [sym]
                         (list sym nil)))))
          (transform-body [body]
             (let [bindings    (body-bindings body)
                   body-forms  (mapcat (fn [form]
                                         (if (var-form? form)
                                           (transform-var-form form)
                                           [(transform form)]))
                                       body)
                   body-forms  (->> body-forms
                                    (remove nil?)
                                    vec)]
               (if (seq bindings)
                 [(let-form bindings body-forms)]
                 body-forms)))
          (branch-form [clauses]
            (letfn [(emit-branch [[clause & more]]
                      (when clause
                        (let [[tag test & body] clause
                              branch-body (if (= 'else tag)
                                            (cons test body)
                                            body)
                              body-form (begin-form (transform-body branch-body))]
                          (case tag
                            else body-form
                            (list 'if
                                  (transform test)
                                  body-form
                                  (emit-branch more))))))]
              (emit-branch clauses)))
          (transform [form]
            (cond (collection/form? form)
                   (let [[op & args] form
                         {:keys [emit] :as entry} (get reserved op)
                         special
                          (cond
                            (and (= 1 (count args))
                                 (unpack-form? (first args))
                                 (not (contains? #{'return 'begin 'do 'quote 'fn 'fn:> 'defn 'defgen
                                                    'let 'while 'br* 'try 'do:> 'not= '==
                                                    'nil?}
                                                  op)))
                            (transform (expand-form reserved
                                                    (list 'x:apply op (second (first args)))))

                            :else
                            (case op
                              return (if (= 1 (count args))
                                       (transform (first args))
                                       (begin-form (mapv transform args)))
                              begin  (if (= op begin)
                                       (begin-form (transform-body args))
                                       +not-found+)
                              do     (begin-form (transform-body args))
                              quote  (if (= 1 (count args))
                                       (transform (first args))
                                       +not-found+)
                              fn     (let [[_ maybe-name maybe-args & more] form
                                           named? (symbol? maybe-name)
                                           args   (if named? maybe-args maybe-name)
                                           body   (if named? more (drop 2 form))]
                                       (lambda-form args
                                                    (transform-body body)))
                              fn:>   (let [[_ maybe-name maybe-args & more] form
                                           named? (symbol? maybe-name)
                                           args   (if named? maybe-args maybe-name)
                                           body   (if named? more (drop 2 form))]
                                       (lambda-form args
                                                    (transform-body body)))
                               def    (def-form (second form)
                                                (transform (nth form 2)))
                               defn   (defn-form (second form)
                                                 (nth form 2)
                                                 (transform-body (drop 3 form)))
                              defgen (defn-form (second form)
                                                (nth form 2)
                                                (transform-body (drop 3 form)))
                              let    (let-form
                                      (->> (partition 2 (second form))
                                           (mapv (fn [[sym value]]
                                                   (list sym (transform value)))))
                                      (transform-body (drop 2 form)))
                               while  (while-form (transform (first args))
                                                  (transform-body (rest args)))
                               :?     (ternary-expr (transform (first args))
                                                    (transform (second args))
                                                    (transform (nth args 2)))
                               br*    (branch-form args)
                               try    (let [{:keys [body catch finally]} (split-try-clauses args)]
                                        (try-form (transform-body body)
                                                  (when catch
                                                   {:sym  (second catch)
                                                    :body (transform-body (drop 2 catch))})
                                                 (when finally
                                                   (transform-body (rest finally)))))
                              do:>   (begin-form (transform-body args))
                              not=   (not-equal-form (mapv transform args))
                              ==     (equal-form (mapv transform args))
                              nil?   (nil-form (transform (first args)))
                              +not-found+))
                         emitted
                         (case emit
                          :internal
                          (if (= 1 (count args))
                            (transform (first args))
                            (begin-form (mapv transform args)))

                          :def-assign
                          (begin-form (transform-var-form form))

                          :assign
                          (assign-target (first args)
                                         (transform (second args)))

                          :index
                          (reduce (fn [obj prop]
                                    (index-read obj prop))
                                  (transform (second form))
                                  (drop 2 form))

                           :with-global
                           (global-read (first args))

                           :table
                           (let [entries (if (every? vector? args)
                                           args
                                           (partition 2 args))]
                             (into {}
                                   (map (fn [[k v]]
                                          [(key-literal k)
                                           (transform v)]))
                                   entries))

                           :discard
                           nil

                           +not-found+)]
                    (cond (not= +not-found+ special)
                          special

                          (not= +not-found+ emitted)
                          emitted

                          :else
                          (apply list
                                 (map transform form))))

                  (vector? form)
                  (mapv transform form)

                  (map? form)
                  (into (empty form)
                        (map (fn [[k v]]
                               [(transform k) (transform v)]))
                        form)

                  (set? form)
                  (set (map transform form))

                  (= '!:G form)
                  global-symbol

                  :else
                  form))]
    (transform form)))
