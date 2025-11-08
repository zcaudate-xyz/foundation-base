(ns std.block.value
  (:require [std.protocol.block :as protocol.block]
            [std.block.base :as base]))

(defn apply-modifiers
  "applys the modifiers within a container
 
   (apply-modifiers [(construct/uneval)
                     (construct/uneval)
                     1 2 3])
   => [3]"
  {:added "3.0"}
  ([blocks]
   (loop [out []
          modifiers ()
          [block & more :as blocks] blocks]
     (cond (empty? blocks)
           out

           (base/modifier? block)
           (recur out (cons block modifiers) more)

           (empty? modifiers)
           (recur (conj out block) modifiers more)

           :else
           (let [modifier (first modifiers)
                 out (base/block-modify modifier out block)]
             (recur out (rest modifiers) more))))))

(defn child-values
  "returns child values of a container
 
   (child-values (parse/parse-string \"[1 #_2 3]\"))
   => [1 3]"
  {:added "3.0"}
  ([block]
   (->> (base/block-children block)
        (keep (fn [block]
                (if (or (base/expression? block)
                        (base/modifier? block))
                  block)))
        (apply-modifiers)
        (mapv base/block-value))))

(defn root-value
  "returns the value of a :root block
 
   (root-value (parse/parse-root \"1 2 3\"))
   => '(do 1 2 3)"
  {:added "3.0"}
  ([block]
   (apply list 'do (child-values block))))

(defn from-value-string
  "reads value from value-string
 
   (from-value-string (parse/parse-string \"(+ 1 1)\"))
   => '(+ 1 1)"
  {:added "3.0"}
  ([block]
   (-> (base/block-value-string block)
       (read-string))))

(defn list-value
  "returns the value of an :list block
 
   (list-value (parse/parse-string \"(+ 1 1)\"))
   => '(+ 1 1)"
  {:added "3.0"}
  ([block]
   (apply list (child-values block))))

(defn map-value
  "returns the value of an :map block
 
   (map-value (parse/parse-string \"{1 2 3 4}\"))
   => {1 2, 3 4}
 
   (map-value (parse/parse-string \"{1 2 3}\"))
   => (throws)"
  {:added "3.0"}
  ([block]
   (apply hash-map (child-values block))))

(defn set-value
  "returns the value of an :set block
 
   (set-value (parse/parse-string \"#{1 2 3 4}\"))
   => #{1 4 3 2}"
  {:added "3.0"}
  ([block]
   (set (child-values block))))

(defn vector-value
  "returns the value of an :vector block
 
   (vector-value (parse/parse-string \"[1 2 3 4]\"))
   => [1 2 3 4]"
  {:added "3.0"}
  ([block]
   (vec (child-values block))))

(defn deref-value
  "returns the value of a :deref block
 
   (deref-value (parse/parse-string \"@hello\"))
   => '(deref hello)"
  {:added "3.0"}
  ([block]
   (let [[obj] (child-values block)]
     (list 'deref obj))))

(defn meta-value
  "returns the value of a :meta block
 
   ((juxt meta identity)
    (meta-value (parse/parse-string \"^:dynamic {:a 1}\")))
   => [{:dynamic true} {:a 1}]
 
   ((juxt meta identity)
    (meta-value (parse/parse-string \"^String {:a 1}\")))
   => [{:tag 'String} {:a 1}]"
  {:added "3.0"}
  ([block]
   (let [[meta obj] (child-values block)]
     (with-meta obj (cond (map? meta)
                          meta

                          (keyword? meta)
                          {meta true}

                          :else
                          {:tag meta})))))

(defn quote-value
  "returns the value of a :quote block
 
   (quote-value (parse/parse-string \"'hello\"))
   => '(quote hello)"
  {:added "3.0"}
  ([block]
   (let [[obj] (child-values block)]
     (list 'quote obj))))

(defn var-value
  "returns the value of a :var block
 
   (var-value (parse/parse-string \"#'hello\"))
   => '(var hello)"
  {:added "3.0"}
  ([block]
   (let [[obj] (child-values block)]
     (list 'var obj))))

(defn hash-keyword-value
  "returns the value of a :hash-keyword block
 
   (hash-keyword-value (parse/parse-string \"#:hello{:a 1 :b 2}\"))
   => #:hello{:b 2, :a 1}"
  {:added "3.0"}
  ([block]
   (let [[kw map] (child-values block)]
     (read-string (str "#" kw map)))))

(defn select-value
  "returns the value of a :select block
 
   (select-value (parse/parse-string \"#?(:clj hello)\"))
   => '(? {:clj hello})"
  {:added "3.0"}
  ([block]
   (let [[selection] (child-values block)]
     (list '? (apply hash-map selection)))))

(defn select-splice-value
  "returns the value of a :select-splice block
 
   (select-splice-value (parse/parse-string \"#?@(:clj hello)\"))
   => '(?-splicing {:clj hello})"
  {:added "3.0"}
  ([block]
   (let [[selection] (child-values block)]
     (list '?-splicing (apply hash-map selection)))))

(defn unquote-value
  "returns the value of a :unquote block
 
   (unquote-value (parse/parse-string \"~hello\"))
   => '(unquote hello)"
  {:added "3.0"}
  ([block]
   (let [[obj] (child-values block)]
     (list 'unquote obj))))

(defn unquote-splice-value
  "returns the value of a :unquote-splice block
 
   (unquote-splice-value (parse/parse-string \"~@hello\"))
   => '(unquote-splicing hello)"
  {:added "3.0"}
  ([block]
   (let [[obj] (child-values block)]
     (list 'unquote-splicing obj))))

(def ^:dynamic *container-values*
  {:root           {:value root-value}
   :fn             {:value from-value-string}
   :list           {:value list-value}
   :map            {:value map-value}
   :set            {:value set-value}
   :vector         {:value vector-value}
   :deref          {:value deref-value}
   :meta           {:value meta-value}
   :quote          {:value quote-value}
   :syntax         {:value from-value-string}
   :var            {:value var-value}
   :hash-keyword   {:value hash-keyword-value}
   :hash-token     {:value from-value-string}
   :hash-meta      {:value meta-value}
   :hash-eval      {:value from-value-string}
   :select         {:value select-value}
   :select-splice  {:value select-splice-value}
   :unquote        {:value unquote-value}
   :unquote-splice {:value unquote-splice-value}})
