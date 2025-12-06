(ns code.framework.test.fact
  (:require [code.framework.common :as common]
            [code.query :as query]
            [std.block.navigate :as nav]))

(defn gather-fact-body
  "helper function for `gather-fact`
   (-> \"(\\n  (+ 1 1) => 2\\n  (long? 3) => true)\"
       nav/parse-string
       nav/down
       (gather-fact-body)
       (docstring/->docstring))
   => \"\\n  (+ 1 1) => 2\\n  (long? 3) => true\""
  {:added "3.0"}
  ([nav]
   (gather-fact-body nav []))
  ([nav output]
   (cond (nil? (nav/block nav)) output

         (and (= :meta (nav/tag nav))
              (-> nav nav/down nav/position-right nav/value (= :hidden)))
         (cond common/*test-full*
               (recur (nav/right* nav) (conj output
                                             (nav/block
                                              (nav/right (nav/down nav)))))

               :else output)

         (query/match nav string?)
         (recur (nav/right* nav)
                (conj output (common/gather-string nav)))

         :else
         (recur (nav/right* nav) (conj output (nav/block nav))))))

(defn gather-fact
  "Make docstring notation out of fact form
   (-> \"^{:refer example/hello-world :added \\\"0.1\\\"}
        (fact \\\"Sample test program\\\"\\n  (+ 1 1) => 2\\n  (long? 3) => true)\"
       (nav/parse-string)
       nav/down nav/right nav/down nav/right
       (gather-fact)
       (update-in [:test] docstring/->docstring))
   => (just-in {:form  'fact
                :ns    'example,
               :var   'hello-world,
                :refer 'example/hello-world
                :added \"0.1\",
                :line  {:row 2, :col 8, :end-row 4, :end-col 21}
                :intro \"Sample test program\",
                :sexp h/form?
                :test  \"\\n  (+ 1 1) => 2\\n  (long? 3) => true\"})"
  {:added "3.0"}
  ([nav]
   (let [nav (if (nil? (nav/tag nav))
               (nav/left nav)
               nav)
         nav (if (= :list (nav/tag nav))
               (nav/down nav)
               nav)]
     (if-let [mta (common/gather-meta nav)]
       (let [nav (if (symbol? (nav/value nav))
                   (nav/right nav)
                   nav)
             exp (and nav (nav/value nav))
           [intro nnav] (if (string? exp)
                          [exp (if (nav/right nav)
                                 (nav/right* nav))]
                          ["" nav])]
         (assoc mta
                :form  (-> nav nav/left nav/value)
                :sexp  (-> nav nav/up nav/value)
                :line  (nav/line-info (nav/up nav))
                :test  (if nnav
                         (gather-fact-body nnav)
                         [])
                :intro intro))))))

(defmethod common/test-frameworks 'midje.sweet
  ([_]
   :fact))
(defmethod common/test-frameworks 'code.test
  ([_]
   :fact))

(defmethod common/analyse-test :fact
  ([type nav]
   (let [root (if (= :root (nav/tag nav))
                (nav/down nav)
                nav)
         fns  (if root
                (query/$* root ['(#{fact comment} | & _)] {:return :zipper :walk :top})
                [])]
     (->> (keep gather-fact fns)
          (reduce (fn [m {:keys [ns var class sexp test intro line form] :as meta}]
                    (-> m
                        (update-in [ns var]
                                   assoc
                                   :ns ns
                                   :var var
                                   :class class
                                   :test  {:path common/*path*
                                           :sexp sexp
                                           :form form
                                           :code test
                                           :line line}
                                   :meta  (apply dissoc meta common/+test-vars+)
                                   :intro intro)))
                  {})))))
