(ns rt.postgres.grammar.form-let
  (:require [clojure.set]
            [rt.postgres.grammar.common :as common]
            [rt.postgres.grammar.tf :as tf]
            [std.lang :as l]
            [std.lang.base.emit-common :as emit-common]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.library-snapshot :as snap]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.walk :as walk]
            [std.string.case :as case]))

(def ^:dynamic *input-syms* nil)

(defn pg-tf-let-block
  "transforms a let block call"
  {:added "4.0"}
  ([[_ {:keys [name declare catch]} & body]]
   (let [hform (if name [(list \\ (list \| "<<" name ">>"))] [])
         dform (if (not-empty declare)
                 [:declare
                  \\ (list \| (apply list 'do (map #(cons 'var %) declare)))
                  \\])
         bform [:begin
                \\
                (list \| (apply list 'do body))]]
     `(do ~@hform
          ~(vec (concat dform
                        bform
                        (when catch
                          [\\ (apply list 'do* catch)])
                        [\\ :end]))))))

(defn pg-tf-let-assign
  "create assignment statments for let form"
  {:added "4.0"}
  [[e v]]
  (let [data-fn (fn [e]
                  (let [assign-fn (:assign/fn (meta v))]
                    (cond (false? assign-fn)
                          [v]
                          
                          assign-fn
                          [(let [result  (assign-fn e)
                                 {:keys [snapshot] :as mopts} (l/macro-opts)
                                 book (snap/get-book snapshot :postgres)]
                             (first (preprocess/to-staging result
                                                           (:grammar book)
                                                           (:modules book)
                                                           mopts)))]
                          
                          (not (vector? v))
                          [(list := e v)]

                          
                          (= [e :into] (take 2 (reverse v)))
                          [v]
                          
                          :else
                          (if (some #(= % e) v)
                            [v]
                            [(conj v :into e)]))))
        js-val-fn (fn [esym]
                    (let [estr (str esym)
                          estr (if (re-find #"^\w-\w+" estr)
                                 (subs 2 estr)
                                 estr)]
                      (case/snake-case estr)))]
    (cond (= '_ e)    [v]
          (symbol? e) (data-fn e)
          (collection/form? e) (data-fn (last e))
          (set? e)    (mapv (fn [ei]
                              (cond (symbol? ei)
                                    `(:= ~ei (:-> ~v ~(tf/pg-js-idx ei)))
                                    
                                    (collection/form? ei)
                                    `(:= ~(last ei) (~@(butlast ei)
                                                     (:->> ~v ~(tf/pg-js-idx (last ei)))))
                                    
                                    :else (f/error "Not Allowed" {:value ei})))
                            e)
          (vector? e)  (vec
                        (map-indexed (fn [i ei]
                                       (cond (symbol? ei)
                                             `(:= ~ei (:-> ~v ~i))
                                             
                                             (collection/form? ei)
                                             `(:= ~(last ei) (~@(butlast ei)
                                                              (:->> ~v ~i)))
                                                 
                                             :else (f/error "Not Allowed" {:value ei})))
                                     e))
          :else (f/error "Not Allowed" {:value e}))))

(defn pg-tf-let-check-body
  "checks if variables are in scope"
  {:added "4.0"}
  ([dsyms body]
   (let [not-checked (volatile! #{})
         _ (if *input-syms*
             (vswap! *input-syms* clojure.set/union dsyms))
         collect-syms (fn [form]
                        (walk/walk:find
                         (fn [x]
                           (and (symbol? x)
                                (nil? (namespace x))))
                         form))
         check-fn (fn check-fn [csyms x]
                    (cond (collection/form? x)
                          (let [f (first x)]
                            (cond (= 'let f)
                                  (let [bindings   (second x)
                                        inner-body (drop 2 x)
                                        pairs      (partition 2 bindings)
                                        inner-syms (reduce (fn [acc [b v]]
                                                             (check-fn csyms v)
                                                             (clojure.set/union acc (collect-syms b)))
                                                           csyms
                                                           pairs)]
                                    (run! (fn [v] (check-fn inner-syms v)) inner-body))
                                  
                                  :else
                                  (run! (fn [v] (check-fn csyms v)) x)))
                          
                          (vector? x)
                          (run! (fn [v] (check-fn csyms v)) x)

                          (symbol? x)
                          (if (and (re-find #"^\w-\w+" (str x))
                                   (not (re-find #"^\w-ret$" (str x)))
                                   (not (get csyms x))
                                   (not (if *input-syms* (get @*input-syms* x))))
                            (vswap! not-checked conj x))
                          
                          (coll? x)
                          (run! (fn [v] (check-fn csyms v)) x)))]
     (run! (fn [v] (check-fn dsyms v)) body)
     (if (not-empty @not-checked)
       (f/error "Unknown symbols in form" {:symbols @not-checked
                                           :dsyms dsyms})))))

(defn pg-tf-let
  "creates a let form"
  {:added "4.0"}
  ([[_ bindings & body]]
   (let [pairs (map vec (partition 2 bindings))
         declp (map first pairs)
         [declu dsyms] (loop [dsyms '#{_}
                              [form & more :as forms] declp
                              out []]
                         
                         (if (empty? forms)
                           [out dsyms]
                           (let [csyms (walk/walk:find
                                        (fn [x]
                                          (and (symbol? x)
                                               (nil? (namespace x))))
                                        form)]
                             (recur (clojure.set/union dsyms csyms)
                                    more
                                    (if (and (symbol? form)
                                             (get dsyms form))
                                      out
                                      (conj out form))))))
         _     (pg-tf-let-check-body dsyms (vec (concat (map second pairs) body)))
         decl  (mapcat (fn dec-fn [e]
                         (cond (= '_ e)    []
                               (symbol? e) [(list :jsonb e)]
                               (collection/form? e) [(if (= '++ (first e))
                                              (apply list :% (concat (drop 2 e) [(second e)]))
                                              (apply list
                                                     (reduce (fn [acc ei]
                                                               (if (and (symbol? ei)
                                                                        (namespace ei))
                                                                 (conj acc :%  ei)
                                                                 (conj acc ei)))
                                                             []
                                                             e)))]
                               (set? e)    (mapcat dec-fn e)
                               (vector? e) (mapcat dec-fn e)
                               :else (f/error "Not Allowed" {:value e})))
                       declu)
         deqs  (mapcat pg-tf-let-assign pairs)]
     (pg-tf-let-block (concat [nil {:declare (sort-by last decl)}]
                              deqs
                              body)))))

(defn pg-do-block
  "emits a block with let usage"
  {:added "4.0"}
  ([[_ form] grammar mopts]
   (binding [*input-syms* (or *input-syms* (volatile! #{}))]
     (emit-common/*emit-fn* 
      (common/block-do-block form)
      grammar
      mopts))))

(defn pg-do-suppress
  "emits a suppress block with let ussage"
  {:added "4.0"}
  ([[_ form] grammar mopts]
   (binding [*input-syms* (or *input-syms* (volatile! #{}))]
     (emit-common/*emit-fn* 
      (common/block-do-suppress form)
      grammar
      mopts))))

(defn pg-loop-block
  "creates a loop block"
  {:added "4.0"}
  ([[_ & forms] grammar mopts]
   (binding [*input-syms* (or *input-syms* (volatile! #{}))]
     (emit-common/*emit-fn* 
      (apply common/block-loop-block forms)
      grammar
      mopts))))

(defn pg-while-block
  "creates a while block"
  {:added "4.0"}
  ([[_ condition & forms] grammar mopts]
   (binding [*input-syms* (or *input-syms* (volatile! #{}))]
     (emit-common/*emit-fn*
      (apply common/block-while-block condition forms)
      grammar
      mopts))))

(defn pg-case-block
  "creates a case block"
  {:added "4.0"}
  ([[_ & forms] grammar mopts]
   (binding [*input-syms* (or *input-syms* (volatile! #{}))]
     (emit-common/*emit-fn* 
      (apply common/block-case-block forms)
      grammar
      mopts))))

