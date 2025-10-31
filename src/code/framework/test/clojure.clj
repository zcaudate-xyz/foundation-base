(ns code.framework.test.clojure
  (:require [code.framework.common :as common]
            [code.query :as query]
            [std.block :as block]
            [code.edit :as nav]))

(defn gather-is-form
  "Make docstring notation out of is form
   (-> (nav/parse-string \"(is (= 1 1))\")
       (gather-is-form)
       (docstring/->docstring))
   => \"1\\n  => 1\"
 
   (-> (nav/parse-string \"(is (boolean? 4))\")
       (gather-is-form)
       (docstring/->docstring))
   => \"(boolean? 4)\\n  => true\""
  {:added "3.0"}
  ([nav]
   (let [nav (-> nav nav/down nav/right)]
     (cond (query/match nav '(= _ _))
           (let [nav (-> nav nav/down nav/right)]
             [(nav/block nav)
              (block/newline)
              (block/space)
              (block/space)
              (block/block '=>)
              (block/space)
              (nav/block (nav/right nav))])

           :else
           [(nav/block nav)
            (block/newline)
            (block/space)
            (block/space)
            (block/block '=>)
            (block/space)
            (block/block true)]))))

(defn gather-deftest-body
  "helper function for `gather-deftest`
 
   (-> \"(\\n  (is (= 1 1))^:hidden\\n  (is (identical? 2 4)))\"
       (nav/parse-string)
       (nav/down)
       (gather-deftest-body)
       (docstring/->docstring))
   => \"\\n  1\\n  => 1\""
  {:added "3.0"}
  ([nav]
   (gather-deftest-body nav []))
  ([nav output]
   (cond (nil? (nav/block nav)) output

         (and (= :meta (nav/tag nav))
              (-> nav nav/down nav/position-right nav/value (= :hidden)))
         output

         (query/match nav string?)
         (recur (nav/right* nav)
                (conj output (common/gather-string nav)))

         (query/match nav 'is)
         (recur (nav/right* nav) (vec (concat output (gather-is-form nav))))

         :else
         (recur (nav/right* nav) (conj output (nav/block nav))))))

(defn gather-deftest
  "Make docstring notation out of deftest form
 
   (-> \"^{:refer example/hello-world :added \\\"0.1\\\"}
        (deftest hello-world-test\\n  (is (= 1 1))\\n  (is (identical? 2 4)))\"
       (nav/parse-string)
       nav/down nav/right nav/down nav/right nav/right
       (gather-deftest)
       (update-in [:test] docstring/->docstring))
   => (contains '{:refer example/hello-world
                  :ns example,
                 :var hello-world,
                  :added \"0.1\",
                  :line {:row 2, :col 8, :end-row 4, :end-col 25},
                  :test \"1\\n  => 1\\n  (identical? 2 4)\\n  => true\"})"
  {:added "3.0"}
  ([nav]
   (if-let [mta (common/gather-meta nav)]
     (assoc mta
            :line (nav/line-info (nav/up nav))
            :test (gather-deftest-body nav)))))

(defmethod common/test-frameworks 'clojure.test
  ([_]
   :clojure))

(defmethod common/analyse-test :clojure
  ([type nav]
   (let [fns  (query/$* nav ['(deftest _ | & _)] {:return :zipper :walk :top})]
     (->> (keep gather-deftest fns)
          (reduce (fn [m {:keys [ns var class test intro line] :as meta}]
                    (-> m
                        (update-in [ns var]
                                   assoc
                                   :ns ns
                                   :var var
                                   :class class
                                   :test {:path common/*path*
                                          :code test
                                          :line line}
                                   :meta (apply dissoc meta common/+test-vars+)
                                   :intro (or intro ""))))
                  {})))))
