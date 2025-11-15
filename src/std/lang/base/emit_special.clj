(ns std.lang.base.emit-special
  (:require [std.string :as str]
            [std.lib :as h]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.emit-data :as data]
            [std.lang.base.book :as book]))

(defn emit-with-module-all-ids
  "emits the module given snapshot in opts"
  {:added "4.0"}
  [snapshot lang module-id allow]
  (let [book   (book/book-from snapshot lang)
        module (book/get-module book module-id)

        #_#_#_#_
        export-sym (get-in module [:export :as])
        _   (if (not export-sym)
              (h/error "No Export for Module" {:module module-id
                                               :export (get module :export)}))
        ids (->> (:code module) 
                 (vals)
                 (sort-by (juxt :priority :time :line))
                 (filter (fn [{:keys [op id]}]
                           (and (allow op))))
                 (map :id))]
    ids))

(defn emit-with-module
  "emits the module given snapshot in opts"
  {:added "4.0"}
  [_ _ [_ lang module-id] grammar {:keys [snapshot] :as mopts}]
  (let [lang      (or lang
                      (:lang mopts))
        module-id (or module-id
                      (-> mopts :module :id))
        {:keys [key-fn allow]}  (helper/get-options grammar [:default :module])
        token-opts  (get-in grammar [:token :symbol])
        
        module-key-fn (fn [id]
                        (->> (name id)
                             (replace (:replace token-opts))
                             (apply str)
                             key-fn))
        ids     (emit-with-module-all-ids snapshot lang module-id allow)
        args    (mapv (fn [id]
                        [(module-key-fn id)
                         (symbol (name module-id)
                                 (name id))])
                      ids)]
    (data/emit-table nil nil (cons 'tab args) grammar mopts)))

;;
;; EVAL and LANG
;;

(defn emit-with-preprocess
  "emits an eval form
 
   (emit-with-preprocess '(L.core/sub 1 2)
                         (:grammar prep/+book-min+)
                         {:lang :lua
                          :module   (lib/get-module +library-ext+ :lua 'L.core)
                          :snapshot (lib/get-snapshot +library-ext+)})
   => \"(- 1 2)\""
  {:added "4.0"}
  [form grammar {:keys [lang snapshot] :as mopts}]
  (let [book    (book/book-from snapshot lang)
        [body]  (-> (preprocess/to-input form)
                    (preprocess/to-staging grammar
                                           (:modules book)
                                           mopts))]
    (common/*emit-fn* body grammar mopts)))

(defn emit-with-eval
  "emits an eval form"
  {:added "4.0"}
  [_ _ [_ form] grammar {:keys [lang snapshot] :as mopts}]
  (emit-with-preprocess (eval form) grammar mopts))

(defn emit-with-deref
  "emits an embedded var"
  {:added "4.0"}
  [_ _ [_ form] grammar mopts]
  (if (= 'var (first form))
    (emit-with-preprocess (deref (eval form)) grammar mopts)
    (h/error "Needs to be a clojure.lang.Var")))

(defn emit-with-lang
  "emits an embedded eval"
  {:added "4.0"}
  [_ _ form grammar {:keys [snapshot] :as mopts}]
  (let [[_ {:keys [lang module] :as opts
            :or {module (h/ns-sym)}}
         body] form
        book   (or (book/book-from snapshot lang)
                   (h/error "Lang not found." {:lang lang
                                               :available (keys snapshot)}))
        {:keys [grammar]} book
        module (book/get-module book module)
        mopts  (merge (assoc mopts :book book :module module)
                      opts)
        [body] (-> (preprocess/to-input body)
                   (preprocess/to-staging grammar
                                          (:modules book)
                                          mopts))]
    (common/*emit-fn* body grammar mopts)))

;;
;; TESTING
;;

(defn test-special-loop
  "test step for special ops"
  {:added "4.0"}
  [form grammar mopts]
  (common/emit-common-loop form
                           grammar
                           mopts
                           (assoc common/+emit-lookup+
                                  :data data/emit-data)
                           (fn [key form grammar mopts]
                             (common/emit-op key form grammar mopts
                                             {:quote data/emit-quote
                                              :table data/emit-table
                                              :with-module emit-with-module
                                              :with-lang emit-with-lang
                                              :with-eval emit-with-eval
                                              :with-deref emit-with-deref}))))

(defn test-special-emit
  "test function for special ops"
  {:added "4.0"}
  [form grammar mopts]
  (binding [common/*emit-fn* test-special-loop]
    (test-special-loop form grammar mopts)))

(comment
  (./create-tests)
  )
