(ns rt.postgres.grammar.form-vec
  (:require [std.string :as str]
            [std.lib :as h]
            [std.lang.base.emit-common :as emit-common]
            [std.lang.base.emit-data :as emit-data]
            [std.lang.base.emit :as emit]
            [std.lang.base.util :as ut]
            [rt.postgres.grammar.common :as common]))

(declare pg-section)

;;
;; query
;;
    
(defn pg-section-query-pair
  "converts to a pair expression"
  {:added "4.0"}
  ([[sym expr]]
   (let [sym  (cond (keyword? sym)
                    #{(ut/sym-default-str (name sym))}

                    (string? sym)
                    #{sym}

                    :else
                    sym)]
     (if (vector? expr)
       (let [[q & rest] expr
             qalt (common/+pg-query-alias+ ((str/wrap str/lower-case) q))]
         (concat [sym (or qalt q)] rest))
       [sym := (list '% expr)]))))

(defn pg-section-query-set-and
  "sets up the query string only for and"
  {:added "4.0"}
  ([queries grammar mopts]
   (let [l-sym?  (fn [x] (and (or (keyword? x) (symbol? x))
                              (#{"AND" "OR"} (str/upper-case (name x)))))
         block   (loop [queries queries
                        acc []]
                   (cond (empty? queries)
                         acc
                         
                         :else
                         (let [iarr (take-while (comp not l-sym?) queries)
                               more (drop (count iarr) queries)
                               _    (if-not (even? (count iarr))
                                      (h/error "Not even" {:value iarr
                                                           :all queries}))
                               oarr (->> (partition 2 iarr)
                                         (map pg-section-query-pair)
                                         (interpose [\\ :AND \\])
                                         (mapcat identity))]
                           (if (empty? more)
                             (vec (concat acc oarr))
                             (recur (rest more)
                                    (concat acc oarr [(first more)]))))))
         indent emit-common/*indent*
         body (pg-section block grammar mopts)]
     (if (str/multi-line? body)
       (str "("
            (str/indent (str "\n" (str/trim-newlines (str/trim-right body)) ")")
                        (+ indent 2)))
       (str "(" body ")")))))

(defn pg-section-query-set
  "sets up the query string"
  {:added "4.0"}
  ([queries grammar mopts]
   (let [l-sym?  (fn [x] (and (or (keyword? x)
                                  (symbol? x))
                              (#{"OR"} (str/upper-case (name x)))))
         cnt     (h/counter)
         groups  (->> (partition-by (fn [x]
                                      (if (l-sym? x)
                                        (h/inc! cnt)))
                                    queries)
                      (remove (fn [x] (l-sym? (first x)))))]
     (if (= 1 (count groups))
       (pg-section-query-set-and (first groups) grammar mopts)
       (let [body-arr  (->> groups
                            (map (fn [group]
                                   (pg-section-query-set group grammar mopts))))]
         (if (emit-data/emit-singleline-array? body-arr)
           (str/join " OR " body-arr)
           (str (str/join (str (emit-common/newline-indent) "OR " )
                          body-arr))))))))

(defn pg-section-query-map
  "query string"
  {:added "4.0"}
  ([queries grammar mopts]
   (let [body-arr (->> (map pg-section-query-pair queries)
                       (map #(pg-section % grammar mopts)))]
     (if (emit-data/emit-singleline-array? body-arr)
       (str/join " AND " body-arr)
       (str (str/join (str (emit-common/newline-indent) "AND ")
                      body-arr))))))

;;
;; block
;;

(defn pg-section-fn
  "rendering function for a section entry"
  {:added "4.0"}
  ([e grammar mopts]
   (cond (keyword? e)
         (-> (name e)
             (str/upper-case)
             (str/replace #"-" " "))
         
         (map? e)
         (pg-section-query-map e grammar mopts)

         (and (set? e)
              (vector? (first e)))
         (pg-section-query-set (first e) grammar mopts)
         
         (and (vector? e)
              (keyword? (first e)))
         (let [indent emit-common/*indent*
               body (binding [emit-common/*indent* 0]
                      (emit-common/*emit-fn* e grammar mopts))]
           (if (str/multi-line? body)
             (str "("
                  (str/indent (str "\n" (str/trim-newlines (str/trim-right body)) ")")
                              (+ indent 2)))
             (str "(" body ")")))
         
         :else
         (emit-common/*emit-fn* e grammar mopts))))

(comment
  (std.lang/with:no-cache
   (std.lang/ptr-print statsdb.core.exchange/r-xch-service-default))
  
  (std.lang/with:no-cache
   (std.lang/ptr-print statsdb.core.account/email-registration-undo))
  )

(defn pg-section
  "rendering function for entire section"
  {:added "4.0"}
  ([arr grammar mopts]
   (let [args (map #(pg-section-fn % grammar mopts) arr)]
     (reduce (fn [acc s]
               (cond (= acc "") s
                     
                     (or (re-find #"^\s+" s)
                         (re-find #"\s+$" acc))
                     (str acc s)
                     
                     (#{";" "," "\n"} (str (first s)))
                     (str acc s)
                     
                     :else
                     (str acc " " s)))
             ""
             args))))
