(ns code.gen.core
  (:require [std.block :as b]
            [std.lib :as h]
            [std.string :as str]))

(defn splice-data->blocks
  "Converts a collection of data into a vector of blocks for splicing (~@).
   This is used for function bodies, argument lists, etc."
  [data]
  (when-not (coll? data)
    (throw (Exception. (str "Splicing ~@ substitution requires a collection. Got: " (type data)))))
  
  (->> data
       (map b/block)
       ;; We can decide to join with spaces, or newlines.
       ;; Let's use newlines and indentation for function bodies.
       (interpose (b/newline))
       (apply vector)))


(defn walk-and-substitute
  "Walks the template AST and substitutes data from the bindings map."
  [block bindings]
  (cond
    ;; 1. Base Case: It's a substitution token
    (and (b/token? block) (symbol? (b/value block)))
    (let [val-sym (b/value block)
          val-str (str val-sym)]
      (cond
        ;; 2. Splicing substitution: ~@body
        (str/starts-with? val-str "~@")
        (let [k (keyword (subs val-str 2))
              v (get bindings k)]
          (if (nil? v)
            (throw (Exception. (str "Splicing binding not found for: " k)))
            (splice-data->blocks v)))   ; Returns a vector of blocks

        ;; 3. Single substitution: ~name
        (str/starts-with? val-str "~")
        (let [k (keyword (subs val-str 1))
              v (get bindings k)]
          (if (nil? v)
            (throw (Exception. (str "Binding not found for: " k)))
            (b/block v)))               ; Returns a single block

        ;; 4. Not a substitution, return as-is
        :else block))

    ;; 5. Recursive Case: It's a container
    (b/container? block)
    (let [children (b/children block)
          new-children (->> children
                            ;; Recursively substitute on all children
                            (map #(walk-and-substitute % bindings))
                            
                            ;; This mapcat is the key:
                            ;; - A single block becomes [block]
                            ;; - A spliced vector of blocks [b1 b2 b3] becomes b1 b2 b3
                            (mapcat (fn [x] (if (coll? x) x [x])))
                            (apply vector))]
      ;; Re-create the container, preserving its type (parens, brackets, etc.)
      (b/container new-children (meta block)))

    ;; 6. Other block type (e.g., space, comment, newline), return as-is
    :else block))

(defn load-template
  "TODO"
  {:added "4.0"}
  [resource-path]
  (if-let [url (h/sys:resource resource-path)]
    (-> (slurp url)
        (b/parse-root))
    (throw (Exception. (str "Template file not found on classpath: " resource-path)))))

(defn generate
  "TODO"
  {:added "4.0"}
  [template-path bindings-map & [opts]]
  (let [;; Get the preprocess-fn from opts, default to identity
        preprocess-fn    (get opts :preprocess-fn identity)
        
        ;; Apply the preprocess-fn to the bindings FIRST
        processed-bindings (preprocess-fn bindings-map)

        ;; Extract preprocess-fn from processed-bindings if it's there
        final-bindings (dissoc processed-bindings :preprocess-fn)
        
        ;; Load and substitute as before, but with the processed bindings
        template-root    (load-template template-path)
        generated-root   (walk-and-substitute template-root final-bindings)]
    
    ;; Convert the final, generated AST back into a string
    (b/string generated-root)))

(defn template-generator
  "TODO"
  {:added "4.0"}
  [template-path]
  (fn [bindings-map]
    (generate template-path bindings-map)))

(defn gen-namespace-block
  "TODO"
  {:added "4.0"}
  [ns-sym requires]
  (let [require-blocks (when (seq requires)
                         (->> requires
                              (map (fn [[ns-name alias]]
                                     (b/container [(b/block :token (str ns-name))
                                                   (b/space)
                                                   (b/block :token (str alias))] ; Add space and alias
                                                  {:string-fn (fn [s] (str "[" s "]"))}))) ; Square brackets for `[ns :as alias]`
                              (interpose (b/newline))))]
    (b/container
     (vec (concat
           [(b/block :token "ns") (b/space) (b/block :token (str ns-sym)) (b/newline)]
           (when (seq requires)
             [(b/container (vec (concat
                                 [(b/block :token ":require") (b/newline)]
                                 require-blocks))
                           {:string-fn (fn [s] (str "(" s ")"))})]))) ; Parens for `(:require ...)`
     {:string-fn (fn [s] (str "(" s ")"))}))) ; Parens for `(ns ...)`
