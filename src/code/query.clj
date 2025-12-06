(ns code.query
  (:require [code.query.common :as common]
            [code.query.compile :as compile]
            [code.query.match :as match]
            [code.query.traverse :as traverse]
            [code.query.walk :as walk]
            [std.block.navigate :as nav])
  (:refer-clojure :exclude [find]))

(defn match
  "matches the source code
   (match (nav/parse-string \"(+ 1 1)\") '(symbol? _ _))
   => false
 
   (match (nav/parse-string \"(+ 1 1)\") '(^:% symbol? _ _))
   => true
 
   (match (nav/parse-string \"(+ 1 1)\") '(^:%- symbol? _ | _))
   => true
 
   (match (nav/parse-string \"(+ 1 1)\") '(^:%+ symbol? _ _))
   => false"
  {:added "3.0"}
  ([zloc selector]
   (let [match-fn (-> selector
                      (compile/expand-all-metas)
                      (common/prepare-deletion)
                      (match/compile-matcher))]
     (try (match-fn zloc)
          (catch Throwable t false)))))

(defn traverse
  "uses a pattern to traverse as well as to edit the form
 
   (nav/value
    (traverse (nav/parse-string \"^:a (+ () 2 3)\")
              '(+ () 2 3)))
   => '(+ () 2 3)
 
   (nav/value
    (traverse (nav/parse-string \"()\")
              '(^:&+ hello)))
   => '(hello)
 
   (nav/value
    (traverse (nav/parse-string \"()\")
              '(+ 1 2 3)))
   => (throws)
 
   (nav/value
    (traverse (nav/parse-string \"(defn hello \\\"world\\\" {:a 1} [])\")
              '(defn ^:% symbol? ^:?%- string? ^:?%- map? ^:% vector? & _)))
   => '(defn hello [])"
  {:added "3.0"}
  ([zloc pattern]
   (let [pattern (compile/expand-all-metas pattern)]
     (:source (traverse/traverse zloc pattern))))
  ([zloc pattern func]
   (let [pattern (compile/expand-all-metas pattern)
         {:keys [level source]} (traverse/traverse zloc pattern)
         nsource (func source)]
     (if (or (nil? level) (= level 0))
       nsource
       (nth (iterate nav/up nsource) level)))))

(defn select
  "selects all patterns from a starting point
   (map nav/value
        (select (nav/parse-root \"(defn hello [] (if (try))) (defn hello2 [] (if (try)))\")
                '[defn if try]))
   => '((defn hello  [] (if (try)))
        (defn hello2 [] (if (try))))"
  {:added "3.0"}
  ([zloc selectors] (select zloc selectors nil))
  ([zloc selectors opts]
   (let [[match-map [cidx ctype cform]] (compile/prepare selectors)
         match-fn (match/compile-matcher match-map)
         walk-fn (case (:walk opts)
                   :top walk/levelwalk
                   walk/matchwalk)]
     (let [atm  (atom [])]
       (walk-fn zloc
                [match-fn]
                (fn [zloc]
                  (swap! atm conj
                         (if (= :form ctype)
                           (:source (traverse/traverse zloc cform))
                           zloc))
                  zloc)
                opts)
       (if (:first opts)
         (first @atm)
         @atm)))))

(defn modify
  "modifies location given a function
   (nav/string
    (modify (nav/parse-root \"^:a (defn hello3) (defn hello)\") ['(defn | _)]
            (fn [zloc]
              (nav/insert-token-to-left zloc :hello))))
   => \"^:a (defn :hello hello3) (defn :hello hello)\""
  {:added "3.0"}
  ([zloc selectors func] (modify zloc selectors func nil))
  ([zloc selectors func opts]
   (let [[match-map [cidx ctype cform]] (compile/prepare selectors)
         match-fn (match/compile-matcher match-map)
         walk-fn (case (:walk opts)
                   :top walk/levelwalk
                   walk/matchwalk)]
     (walk-fn zloc
              [match-fn]
              (fn [zloc]
                (if (= :form ctype)
                  (let [{:keys [level source]} (traverse/traverse zloc cform)
                        nsource (func source)]

                    (if (or (nil? level) (= level 0))
                      nsource
                      (nth (iterate nav/up nsource) level)))
                  (func zloc)))
              opts))))

(defn context-zloc
  "gets the context for loading forms"
  {:added "3.0"}
  ([context]
   (cond (nav/navigator? context)
         context

         (string? context)
         (nav/parse-root (slurp context))

         (vector? context) context

         (map? context)
         (-> (cond (:source context)
                   (:source context)

                   (:file context)
                   (nav/parse-root (slurp (:file context)))

                   (:string context)
                   (nav/parse-root (:string context))

                   :else (throw (ex-info "keys can only be either :file or :string" context))))
         :else (throw (ex-info "context can only be a string or map" {:value context})))))

(defn wrap-vec
  "helper for dealing with vectors"
  {:added "3.0"}
  ([f]
   (fn [res opts]
     (if (vector? res)
       (mapv #(f % opts) res)
       (f res opts)))))

(defn wrap-return
  "decides whether to return a string, zipper or sexp representation`"
  {:added "3.0"}
  ([f]
   (fn [res {:keys [return] :as opts}]
     (case return
       :string (nav/string (f res opts))
       :zipper res
       :value  (nav/value (f res opts))))))

(defn $*
  "helper function for `$`"
  {:added "3.0"}
  ([context path & [func? opts?]]
   (let [zloc (context-zloc context)
         [func opts] (cond (nil? func?) [nil opts?]
                           (map? func?) [nil func?]
                           :else [func? opts?])
         results     (cond func
                           (modify zloc path func opts)
                           
                           :else
                           (select zloc path opts))
         opts         (merge {:return (if func :zipper :value)} opts)]
     ((-> (fn [res opts] res)
          wrap-return
          wrap-vec) results opts))))

(defmacro $
  "select and manipulation of clojure source code
 
   ($ {:string \"(defn hello1) (defn hello2)\"}
      [(defn _ ^:%+ (keyword \"oeuoeuoe\"))])
   => '[(defn hello1 :oeuoeuoe) (defn hello2 :oeuoeuoe)]
 
   ($ {:string \"(defn hello1) (defn hello2)\"}
      [(defn _ | ^:%+ (keyword \"oeuoeuoe\"))])
   => '[:oeuoeuoe :oeuoeuoe]
 
   (->> ($ {:string \"(defn hello1) (defn hello2)\"}
           [(defn _ | ^:%+ (keyword \"oeuoeuoe\"))]
           {:return :string}))
   => [\":oeuoeuoe\" \":oeuoeuoe\"]
 
   ($ (nav/parse-root \"a b c\") [{:is a}])
   => '[a]"
  {:added "3.0"}
  ([context path & args]
   `($* ~context (quote ~path) ~@args)))

(comment
  ($ {:string "(defn hello1) (defn hello2)"}
     [(defn _ ^:%+ (keyword "oeuoeuoe"))])
  => '[(defn hello1 :oeuoeuoe) (defn hello2 :oeuoeuoe)]

  ($ {:string "(defn hello1) (defn hello2)"}
     [(defn _ | ^:%+ (keyword "oeuoeuoe"))])
  => '[:oeuoeuoe :oeuoeuoe]

  (->> ($ {:string "(defn hello1) (defn hello2)"}
          [(defn _ | ^:%+ (keyword "oeuoeuoe"))]
          {:return :string}))
  => [":oeuoeuoe" ":oeuoeuoe"]
  (./reset '[std.block])
  (./run '[std.block]))
