(ns std.lang.model.spec-js.jsx
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-data :as data]
            [std.lib :as h]
            [std.string :as str]))

(declare emit-jsx)
(declare emit-jsx-set-params)

(defn jsx-key-fn
  "converts jsx key"
  {:added "4.0"}
  ([k]
   (if (keyword? k)
     (keyword (str/camel-case (name k)))
     k)))

(defn jsx-arr-prep
  "prepares jsx array
 
   (jsx-arr-prep [:div {:className 'a}
                  [:p '(hello 1 2 3)]]
                 js/+grammar+
                 {})
   => '[:div {:className a} [:p (hello 1 2 3)]]"
  {:added "4.0"}
  ([arr grammar mopts]
   (cond (= :% (first arr))
         (if (second arr)
           (let [tag (keyword (emit/emit-main (second arr)
                                              grammar
                                              mopts))]
             (cons tag (drop 2 arr)))
           (h/error "MISSING TAG"))

         (= :<> (first arr))
         (cons :React.Fragment (rest arr))

         :else
         arr)))

(defn jsx-standardise-params
  [params]
  (let [{:keys [class className]} params
        className (or class
                      className)
        value  (if (vector? className)
                 (str/join " " (map h/strn className))
                 className)
        params (if value
                 (assoc params :className value)
                 params)]
    (dissoc params :class)))

(defn jsx-arr-norm
  "normalises the jsx array
 
   (jsx-arr-norm [:div])
   => [:div {} nil]"
  {:added "4.0"}
  ([arr]
   (let [[tag & children] arr
         params? (first children)
         [tag params children] (cond (map? params?)
                                     (let [params (->>  params?
                                                        (h/map-keys jsx-key-fn)
                                                        (jsx-standardise-params))]
                                       [tag params (rest children)])

                                     (set? params?)
                                     [tag params? (rest children)]

                                     :else
                                     [tag {} children])]
     [tag params children])))

(defn emit-jsx-map-params
  "emits jsx map params
   
   (emit-jsx-map-params '{:a (+ 1 2 3) :b \"abc\"}
                        js/+grammar+
                        {})
   => '(\"a={1 + 2 + 3}\" \"b=\\\"abc\\\"\")"
  {:added "4.0"}
  ([params grammar mopts]
   (binding [common/*indent* (+ common/*indent* 2)]
     (cond-> (mapv (fn [[k v]]
                     (let [vstr (cond (string? v)
                                      (emit/emit-main v grammar mopts)

                                      :else
                                      (let [body (emit/emit-main v grammar mopts)
                                            body (if (str/multi-line? body)
                                                   (str/indent-rest body 2)
                                                   body)]
                                        (str "{" body "}")))]
                       (str (name k) "=" vstr)))
                   (dissoc params :.. :#))
       (:# params) (into (flatten (emit-jsx-set-params #{(:# params)} grammar mopts)))

       (:.. params) (conj (str "{..." (emit/emit-main (if (vector?  (:.. params))
                                                        (first (:.. params))
                                                        (:.. params))
                                                      grammar
                                                      mopts)
                               "}"))))))

(defn emit-jsx-set-params
  "emits jsx set params
 
   (emit-jsx-set-params '#{a b c}
                        js/+grammar+
                        {})
   => '((\"a={a}\" \"c={c}\" \"b={b}\"))
 
   (emit-jsx-set-params '#{[a b (:.. c)]}
                        js/+grammar+
                        {})
   => '((\"a={a}\" \"b={b}\") \"{...c}\")"
  {:added "4.0"}
  ([params grammar mopts]
   (let [emit-fn (fn [svar mvar fvar]
                   (let [mvar (merge mvar (zipmap svar svar))
                         mstr (if (not-empty mvar)
                                (emit-jsx-map-params mvar grammar mopts))
                         fstr (if (not-empty fvar)
                                (emit/emit-main (set fvar) grammar mopts))]
                     (filter identity [mstr fstr])))
         is-sym (fn [x]
                  (and (symbol? x)
                       (not (str/starts-with? (name x)
                                              "..."))))]
     (cond (and (= 1 (count params))
                (vector? (first params)))
           (let [groups (data/emit-table-group (first params))
                 groups (map (fn [x]
                               (if (and (seq? x) (= := (first x)))
                                 [(second x) (nth x 2)]
                                 x))
                             groups)
                 svar   (filter is-sym groups)
                 mvar   (->> (filter vector? groups)
                             (into {})
                             (jsx-standardise-params))
                 fvar   (remove (fn [x]
                                  (or (vector? x)
                                      (is-sym x)))
                                groups)]
             (emit-fn svar mvar fvar))

           :else
           (let [svar (filter is-sym params)
                 mvar (apply merge (filter map? params))
                 fvar (remove (fn [x]
                                (or (map? x)
                                    (is-sym x)))
                              params)]
             (emit-fn svar mvar fvar))))))

(defn emit-jsx-params
  "emits jsx params
   
   (emit-jsx-params {:a 1 :b 2}
                    js/+grammar+
                    {})
   => [\" \" \"a={1} b={2}\"]
 
   (emit-jsx-params '#{[a b (:.. c)]}
                    js/+grammar+
                    {})
   => [\" \" \"a={a} b={b} {...c}\"]"
  {:added "4.0"}
  ([params grammar mopts]
   (let [body-arr (cond (empty? params) []

                        (set? params)
                        (flatten (emit-jsx-set-params params grammar mopts))

                        (map? params)
                        (emit-jsx-map-params params grammar mopts)

                        :else (h/error "Not valid input." {:input params}))]
     (cond (empty? body-arr)
           ["" ""]

           (data/emit-singleline-array? body-arr)
           [" "  (str/join " " body-arr)]

           :else
           (let [spc (str (common/newline-indent) "  ")]
             [spc (str/join spc body-arr)])))))

(defn emit-jsx-inner
  "emits the inner blocks for a jsx form"
  {:added "4.0"}
  ([arr grammar mopts]
   (let [[tag params children] (-> arr
                                   (jsx-arr-prep grammar mopts)
                                   (jsx-arr-norm))

         ntag   (name tag)
         ntag   (cond-> ntag
                  (.startsWith ntag "<") (subs 1)
                  (.endsWith ntag ">") (h/lsubs 1))

         [prefix params] (emit-jsx-params params grammar mopts)]
     (if (empty? children)
       (str "<" ntag prefix params "/>")
       (let [end    (str  "</" ntag ">")
             start  (str  "<" ntag prefix params
                          ">")
             indent   common/*indent*
             body-arr (binding [common/*indent* 0]
                        (->> children
                             (mapv (fn [form]
                                     (cond (and (vector? form)
                                                (keyword? (first form)))
                                           (emit-jsx-inner form grammar mopts)

                                           :else (str "{" (emit/emit-main form grammar mopts) "}"))))))
             single?  (data/emit-singleline-array? body-arr)
             body     (if single?
                        (str/join "" body-arr)
                        (str/indent  (str/join "\n" body-arr) (+ 2 indent)))]
         (str start
              (if single? "" "\n")
              body
              (if (or (not single?)
                      (str/multi-line? start))
                (common/newline-indent))
              end))))))

(defn emit-jsx-raw
  "emits the jsx transform
   
   (emit-jsx-raw [:div {:className 'a}
                  [:p '(hello 1 2 3)]]
                 js/+grammar+
                 {})
   => \"(\\n  <div className={a}><p>{hello(1,2,3)}</p></div>)\""
  {:added "4.0"}
  ([arr grammar mopts]
   (binding [common/*indent* (+ common/*indent* 2)]
     (str "("
          (common/newline-indent)
          (emit-jsx-inner arr grammar mopts) ")"))))

(defn emit-jsx
  "can perform addition transformation if [:grammar :jsx] is false
 
   (emit-jsx [:div {:className 'a}
              [:p '(hello 1 2 3)]]
             js/+grammar+
             {:emit {:lang/jsx false}})
   => (std.string/|
       \"React.createElement(\"
       \"  \\\"div\\\",\"
       \"  {\\\"className\\\":a},\"
      \"  React.createElement(\\\"p\\\",{},hello(1,2,3))\"
       \")\")"
  {:added "4.0"}
  ([arr grammar mopts]
   (if (= false (-> mopts :emit :lang/jsx))
     (let [child-fn (fn [children]
                      (cond (or (empty? children)
                                (map? (first children))
                                (set? (first children)))
                            children

                            :else (cons {} children)))
           tf-fn (fn tf-fn
                   [form]
                   (let [tag (first form)]
                     (cond (= :<> tag)
                           (tf-fn (cons 'React.Fragment (rest form)))

                           (= :% tag)
                           (apply list 'React.createElement (second form)
                                  (child-fn (drop 2 form)))

                           (re-find #"^[a-z]" (name tag))
                           (apply list 'React.createElement (name tag)
                                  (child-fn (rest form)))

                           :else
                           (apply list 'React.createElement (symbol (name tag))
                                  (child-fn (rest form))))))
           arr (->> arr
                    (clojure.walk/prewalk
                     (fn walk-fn [form]
                       (try
                         (cond (and (vector? form)
                                    (keyword? (first form)))
                               (tf-fn form)

                               (and (set? form)
                                    (vector? (first form)))
                               (volatile! form)

                               (map? form)
                               (->> form
                                    (jsx-standardise-params)
                                    (h/map-keys (fn [k]
                                                  (h/strn (jsx-key-fn k)))))

                               :else
                               form)
                         (catch Throwable t
                           (h/pl form)))))
                    (clojure.walk/prewalk
                     (fn [form]
                       (if (volatile? form)
                         @form
                         form))))]
       (emit/emit-main arr grammar mopts))
     (emit-jsx-raw arr grammar mopts))))


(comment
  (./create-tests)

  (emit/emit-main (:form (std.lang/ptr-entry js.ext.react/createStoreProvider))
                  (std.lang/grammar:js))

  (emit-jsx [:h1 "hello"]
            (std.lang/grammar:js)
            {:grammar {:jsx false}})


  (emit-jsx
   '[:div
     (js/write [(S.group {"test/FooHello" {:a 10 :b 20}})])]
   (std.lang/grammar:js)
   {:grammar {:jsx false}})


  (emit-jsx
   '[:div
     [:h1 (+ "HELLO " val)]
     [:button {test (fn []
                      (setVal (Math.random {rt.javafx.test/BarHello 1})))}]]
   (std.lang/grammar:js)
   {:grammar {:jsx false}})

  (emit/emit-main '(React.createElement "h1" {} "hello")
                  (std.lang/grammar:js)
                  {:grammar {:jsx false}}))
