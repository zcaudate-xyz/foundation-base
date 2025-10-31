(ns code.doc.parse
  (:require [clojure.java.io :as io]
            [code.query :as query]
            [code.edit :as nav]
            [code.doc.parse.check :as checks]
            [std.block :as block]))

(def ^:dynamic *spacing* 2)
(def ^:dynamic *indentation* 0)
(def ^:dynamic *namespace* nil)

(declare parse-file parse-facts-form)

(defn parse-ns-form
  "converts a ns zipper into an element
 
   (-> (nav/parse-string \"(ns example.core)\")
       (parse-ns-form))
   => {:type :ns-form
       :indentation 0
       :meta {}
       :ns 'example.core
       :code \"(ns example.core)\"}"
  {:added "3.0"}
  ([nav]
   {:type :ns-form
    :indentation *indentation*
    :ns   (-> nav nav/value second)
    :code (nav/string nav)
    :meta (if (-> nav nav/tag (= :meta))
            (-> (nav/block nav)
                :children
                first
                block/value)
            {})}))

(defn code-form
  "converts a form zipper into a code string
 
   (-> (nav/parse-string \"(fact (+ 1 1) \\n => 2)\")
       (code-form 'fact))
   => \"(+ 1 1) \\n => 2\""
  {:added "3.0"}
  ([nav symbol]
   (let [s ^String (nav/string nav)]
     (-> (.substring s 1 (dec (.length s)))
         (.replaceFirst (str symbol "(\\s+)?") "")))))

(defn parse-fact-form
  "convert a fact zipper into an element
 
   (-> (nav/parse-string \"(fact (+ 1 1) \\n => 2)\")
       (parse-fact-form))
   => {:type :test :indentation 2 :code \"(+ 1 1) \\n => 2\"}"
  {:added "3.0"}
  ([nav]
   {:type :test
    :indentation (+ *indentation* *spacing*)
    :code (code-form nav "fact")}))

(defn parse-facts-form
  "converts a facts zipper into an element
 
   (-> (nav/parse-string \"(facts (+ 1 1) \\n => 2)\")
       (parse-facts-form))
   => {:type :test :indentation 2 :code \"(+ 1 1) \\n => 2\"}"
  {:added "3.0"}
  ([nav]
   {:type :test
    :indentation (+ *indentation* *spacing*)
    :code (code-form nav "facts")}))

(defn parse-comment-form
  "convert a comment zipper into an element
 
   (-> (nav/parse-string \"(comment (+ 1 1) \\n => 2)\")
       (parse-comment-form))
   => {:type :block :indentation 2 :code \"(+ 1 1) \\n => 2\"}"
  {:added "3.0"}
  ([nav]
   {:type :block
    :indentation (+ *indentation* *spacing*)
    :code (code-form nav "comment")}))

(defn is-code-form
  "converts the `is` form into a string
 
   (is-code-form (nav/parse-string \"(is (= (+ 1 1) 2))\"))
   => \"(+ 1 1)\\n=> 2\"
 
   (is-code-form (nav/parse-string \"(is true)\"))
   => \"true\""
  {:added "3.0"}
  ([nav]
   (let [nav* (-> nav nav/down nav/right)]
     (cond (query/match nav* '(= _ _))
           (let [first  (-> nav* nav/down nav/right)
                 second (-> first nav/right)]
             (str (nav/string first)
                  "\n=> "
                  (nav/string second)))

           :else
           (code-form nav "is")))))

(defn parse-is-form
  "converts an `is` form into an element
   (-> (nav/parse-string \"(is (= (+ 1 1) 2))\")
       (parse-is-form))
   => {:type :block, :indentation 2, :code \"(+ 1 1)\\n=> 2\"}"
  {:added "3.0"}
  ([nav]
   {:type :block
    :indentation (+ *indentation* *spacing*)
    :code (is-code-form nav)}))

(defn parse-deftest-form
  "converts the `deftest` form into an element
 
   (-> (nav/parse-string \"(deftest hello)\")
       (parse-deftest-form))
   => {:type :test, :source {:row 1, :col 1, :end-row 1, :end-col 16}}"
  {:added "3.0"}
  ([nav]
   {:type :test
    :source (nav/line-info nav)}))

(defn parse-paragraph
  "converts a string zipper into an element
 
   (-> (nav/parse-string \"\\\"this is a paragraph\\\"\")
       (parse-paragraph))
   => {:type :paragraph :text \"this is a paragraph\"}"
  {:added "3.0"}
  ([nav]
   {:type :paragraph
    :text (nav/value nav)}))

(defn parse-directive
  "converts a directive zipper into an element
 
   (-> (nav/parse-string \"[[:chapter {:title \\\"hello world\\\"}]]\")
       (parse-directive))
   => {:type :chapter :title \"hello world\"}
 
   (binding [*namespace* 'example.core]
     (-> (nav/parse-string \"[[:ns {:title \\\"hello world\\\"}]]\")
         (parse-directive)))
   => {:type :ns, :title \"hello world\", :ns 'example.core}"
  {:added "3.0"}
  ([nav]
   (let [tloc       (-> nav nav/down nav/down)
         tag        (-> tloc nav/value)
         attributes (-> tloc nav/right nav/value)
         directive  (merge {:type tag} attributes)]
     (if (= :ns tag)
       (assoc directive :ns *namespace*)
       directive))))

(defn parse-attribute
  "coverts an attribute zipper into an element
 
   (-> (nav/parse-string \"[[{:title \\\"hello world\\\"}]]\")
       (parse-attribute))
   => {:type :attribute, :title \"hello world\"}"
  {:added "3.0"}
  ([nav]
   (let [attributes (-> nav nav/down nav/down nav/value)]
     (assoc attributes :type :attribute))))

(defn parse-code-directive
  "coverts an code directive zipper into an element
 
   (-> (nav/parse-string \"[[:code {:language :ruby} \\\"1 + 1 == 2\\\"]]\")
       (parse-code-directive))
   => {:type :block, :indentation 0 :code \"1 + 1 == 2\" :language :ruby}"
  {:added "3.0"}
  ([nav]
   (-> (parse-directive nav)
       (assoc :type :block
              :indentation *indentation*
              :code (-> nav
                        nav/down
                        nav/down
                        nav/right
                        nav/right
                        nav/value)))))

(defn parse-whitespace
  "coverts a whitespace zipper into an element
 
   (-> (nav/parse-root \"1 2 3\")
       (nav/down)
       (nav/right*)
       (parse-whitespace))
   => {:type :whitespace, :code [\" \"]}"
  {:added "3.0"}
  ([nav]
   {:type :whitespace
    :code [(nav/string nav)]}))

(defn parse-code
  "coverts a code zipper into an element
 
   (-> (nav/parse-root \"(+ 1 1) (+ 2 2)\")
       (nav/down)
       (parse-code))
   => {:type :code, :indentation 0, :code [\"(+ 1 1)\"]}"
  {:added "3.0"}
  ([nav]
   {:type :code
    :indentation *indentation*
    :code [(nav/string nav)]}))

(defn wrap-meta
  "if form is meta, then go down a level"
  {:added "3.0"}
  ([f]
   (fn [nav]
     (if (= :meta (nav/tag nav))
       (f (-> nav nav/down nav/right))
       (f nav)))))

(defn wrap-line-info
  "helper to add the line information to the parser (used by `parse-loop`)"
  {:added "3.0"}
  ([f]
   (fn [nav]
     (assoc (f nav) :line (nav/line-info nav)))))

(defn parse-single
  "parse a single zloc"
  {:added "3.0"}
  ([nav]
   (cond (checks/whitespace? nav)
         (parse-whitespace nav)

         (checks/ns? nav)
         (parse-ns-form nav)

         (checks/is? nav)
         ((wrap-meta parse-is-form) nav)

         (checks/deftest? nav)
         ((wrap-meta parse-deftest-form) nav)

         (checks/fact? nav)
         ((wrap-meta parse-fact-form) nav)

         (checks/facts? nav)
         ((wrap-meta parse-facts-form) nav)

         (checks/comment? nav)
         ((wrap-meta parse-comment-form) nav)

         (checks/directive? nav)
         ((wrap-meta parse-directive) nav)

         (checks/attribute? nav)
         ((wrap-meta parse-attribute) nav)

         (checks/code-directive? nav)
         ((wrap-meta parse-code-directive) nav)

         (checks/paragraph? nav)
         (parse-paragraph nav)

         :else (parse-code nav))))

(declare parse-loop)

(defn merge-current
  "if not whitespace, then merge output"
  {:added "3.0"}
  ([output current]
   (cond (nil? current) output

         (= :whitespace (:type current)) output

         :else (conj output current))))

(defn parse-inner
  "parses the inner form of the fact and comment function"
  {:added "3.0"}
  ([nav f output current opts]
   (let [sub (binding [*indentation* (+ *indentation* *spacing*)]
               (parse-loop (f nav) opts))]
     (apply conj (merge-current output current) sub))))

(defn parse-loop
  "the main loop for the parser
 
   (-> (nav/parse-root \"(ns example.core) 
                        [[:chapter {:title \\\"hello\\\"}]] 
                        (+ 1 1) 
                        (+ 2 2)\")
       (nav/down)
       (parse-loop {}))
 
   => (contains-in
       [{:type :ns-form,
        :indentation 0,
         :ns 'example.core,
         :code \"(ns example.core)\"}
        {:type :chapter, :title \"hello\"}
        {:type :code, :indentation 0, :code (contains [\"(+ 1 1)\"
                                                       \"(+ 2 2)\"]
                                                      :gaps-ok)}])"
  {:added "3.0"}
  ([nav opts] (parse-loop nav opts nil []))
  ([nav opts current output]
   (cond (nil? (nav/block nav))
         (merge-current output current)

         :else
         (let [element ((wrap-line-info parse-single) nav)]
           (cond (= (:type current) :attribute)
                 (if (not= :whitespace (:type element))
                   (recur (nav/right* nav) opts (merge current element) output)
                   (recur (nav/right* nav) opts current output))

                 (nil? (:type element))
                 (throw (Exception. (str "element should have a type " element (:type element))))

                 (and (#{:code :whitespace} (:type element))
                      (= (:type current) :code))
                 (recur (nav/right* nav)
                        opts
                        (update-in current [:code] #(apply conj % (:code element)))
                        output)

                 :else
                 (case (:type element)
                   :ns-form    (binding [*namespace* (:ns element)]
                                 (parse-loop (nav/right* nav)
                                             opts
                                             element
                                             (merge-current output current)))
                   :attribute  (recur (nav/right* nav) opts element
                                      (merge-current output current))
                   :file       (recur (nav/right* nav) opts nil
                                      (apply conj output (parse-file (:src element) opts)))
                   :facts      (recur (nav/right* nav) opts nil
                                      (parse-inner nav
                                                   #(-> % nav/down nav/right)
                                                   output
                                                   current
                                                   opts))
                   :deftest    (recur (nav/right* nav) opts nil
                                      (parse-inner nav
                                                   #(-> % nav/down nav/right nav/right)
                                                   output current
                                                   opts))
                   (recur (nav/right* nav) opts element (merge-current output current))))))))

(defn parse-file
  "parses the entire file"
  {:added "3.0"}
  ([file {:keys [root] :as opts}]
   (let [path (if (:root opts)
                (str (:root opts) "/" file)
                file)]
     (with-open [input (io/input-stream path)]
       (parse-loop (-> (nav/parse-root (slurp path))
                       (nav/down))
                   opts)))))
