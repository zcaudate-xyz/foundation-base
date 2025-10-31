(ns code.query.match
  (:require [code.query.match.pattern :as pattern]
            [code.edit :as nav]
            [std.block.base :as base]
            [std.lib.zip :as zip]
            [std.lib :as h]))

(defrecord Matcher [f]
  clojure.lang.IFn
  (invoke [this nav]
    (f nav)))

(defn matcher
  "creates a matcher
 
   ((matcher string?) \"hello\")
   => true"
  {:added "3.0"}
  ([f]
   (Matcher. f)))

(defn matcher?
  "checks if element is a matcher
 
   (matcher? (matcher string?))
   => true"
  {:added "3.0"}
  ([x]
   (instance? Matcher x)))

(defn p-fn
  "takes a predicate function to check the state of the zipper
   ((p-fn (fn [nav]
            (-> nav (nav/tag) (= :symbol))))
    (nav/parse-string \"defn\"))
   => true"
  {:added "3.0"}
  ([template]
   (Matcher. (fn [nav]
               (template nav)))))

(defn p-not
  "checks if node is the inverse of the given matcher
 
   ((p-not (p-is 'if)) (nav/parse-string \"defn\"))
   => true
 
   ((p-not (p-is 'if)) (nav/parse-string \"if\"))
   => false"
  {:added "3.0"}
  ([matcher]
   (Matcher. (fn [nav]
               (not (matcher nav))))))

(defn p-is
  "checks if node is equivalent, does not meta into account
   ((p-is 'defn) (nav/parse-string \"defn\"))
   => true
 
   ((p-is '^{:a 1} defn) (nav/parse-string \"defn\"))
   => true
 
   ((p-is 'defn) (nav/parse-string \"is\"))
   => false
 
   ((p-is '(defn & _)) (nav/parse-string \"(defn x [])\"))
   => false"
  {:added "3.0"}
  ([template]
   (Matcher. (fn [nav]
               (and nav
                    (if (fn? template)
                      (template (nav/value nav))
                      (= template (nav/value nav))))))))

(defn p-equal-loop
  "helper function for `p-equal`
 
   ((p-equal [1 2 3]) (nav/parse-string \"[1 2 3]\"))
   => true
 
   ((p-equal (list 'defn)) (nav/parse-string \"(defn)\"))
   => true
 
   ((p-equal '(defn)) (nav/parse-string \"(defn)\"))
   => true"
  {:added "3.0"}
  ([expr template]
   (and (or (= (meta expr) (meta template))
            (and (empty? (meta expr))
                 (empty? (dissoc (meta template) :line :column))))
        (cond (or (list? expr) (vector? expr))
              (and (= (count expr) (count template))
                   (every? true? (map p-equal-loop expr template)))

              (set? expr)
              (and (= (count expr) (count template))
                   (every? true? (map p-equal-loop
                                      (sort expr)
                                      (sort template))))

              (map? expr)
              (and (= (count expr) (count template))

                   (every? true? (map p-equal-loop
                                      (sort (keys expr))
                                      (sort (keys template))))
                   (every? true? (map p-equal-loop
                                      (map #(get expr %) (keys expr))
                                      (map #(get template %) (keys expr)))))

              :else (= expr template)))))

(defn p-equal
  "checks if the node is equivalent, takes meta into account
   ((p-equal '^{:a 1} defn) (nav/parse-string \"defn\"))
   => false
 
   ((p-equal '^{:a 1} defn) (nav/parse-string \"^{:a 1} defn\"))
   => true
 
   ((p-equal '^{:a 1} defn) (nav/parse-string \"^{:a 2} defn\"))
   => false"
  {:added "3.0"}
  ([template]
   (Matcher. (fn [nav]
               (let [expr (nav/value nav)]
                 (p-equal-loop expr template))))))

(defn p-meta
  "checks if meta is the same
   ((p-meta {:a 1}) (nav/down (nav/parse-string \"^{:a 1} defn\")))
   => true
 
   ((p-meta {:a 1}) (nav/down (nav/parse-string \"^{:a 2} defn\")))
   => false"
  {:added "3.0"}
  ([template]
   (Matcher. (fn [nav]
               (let [mloc (nav/up nav)]
                 (and (= :meta (nav/tag mloc))
                      (= (nav/value (nav/down mloc)) template)))))))

(defn p-type
  "check on the type of element
   ((p-type :symbol) (nav/parse-string \"defn\"))
   => true
 
   ((p-type :symbol) (-> (nav/parse-string \"^{:a 1} defn\") nav/down nav/right))
   => true"
  {:added "3.0"}
  ([template]
   (Matcher. (fn [nav]
               (-> nav nav/tag (= template))))))

(defn p-form
  "checks if it is a form with the symbol as the first element
   ((p-form 'defn) (nav/parse-string \"(defn x [])\"))
   => true
   ((p-form 'let) (nav/parse-string \"(let [])\"))
   => true"
  {:added "3.0"}
  ([template]
   (Matcher. (fn [nav]
               (and (-> nav nav/tag (= :list))
                    (-> nav nav/down nav/value (= template)))))))

(defn p-pattern
  "checks if the form matches a particular pattern
   ((p-pattern '(defn ^:% symbol? & _)) (nav/parse-string \"(defn ^{:a 1} x [])\"))
   => true
 
   ((p-pattern '(defn ^:% symbol? ^{:% true :? true} string? []))
    (nav/parse-string \"(defn ^{:a 1} x [])\"))
   => true"
  {:added "3.0"}
  ([template]
   (Matcher. (fn [nav]
               (let [mf (pattern/pattern-fn template)]
                 (-> nav nav/value mf))))))

(defn p-code
  "checks if the form matches a string in the form of a regex expression
   ((p-code #\"defn\") (nav/parse-string \"(defn ^{:a 1} x [])\"))
   => true"
  {:added "3.0"}
  ([template]
   (Matcher. (fn [nav]
               (cond (h/regexp? template)
                     (if (->> nav nav/string (re-find template))
                       true false))))))

(defn p-and
  "takes multiple predicates and ensures that all are correct
   ((p-and (p-code #\"defn\")
           (p-type :token)) (nav/parse-string \"(defn ^{:a 1} x [])\"))
   => false
 
   ((p-and (p-code #\"defn\")
           (p-type :list)) (nav/parse-string \"(defn ^{:a 1} x [])\"))
   => true"
  {:added "3.0"}
  ([& matchers]
   (Matcher. (fn [nav]
               (->> (map (fn [m] (m nav)) matchers)
                    (every? true?))))))

(defn p-or
  "takes multiple predicates and ensures that at least one is correct
   ((p-or (p-code #\"defn\")
          (p-type :token)) (nav/parse-string \"(defn ^{:a 1} x [])\"))
   => true
 
   ((p-or (p-code #\"defn\")
          (p-type :list)) (nav/parse-string \"(defn ^{:a 1} x [])\"))
   => true"
  {:added "3.0"}
  ([& matchers]
   (Matcher. (fn [nav]
               (->> (map (fn [m] (m nav)) matchers)
                    (some true?))))))

(declare p-parent p-child p-first p-last p-nth p-nth-left p-nth-right
         p-nth-ancestor p-nth-contains p-ancestor p-contains
         p-sibling p-left p-right p-right-of p-left-of p-right-most p-left-most)

(defn compile-matcher
  "creates a matcher given a datastructure declaring the actual template
 
   ((compile-matcher {:is 'hello}) (nav/parse-string \"hello\"))
   => true"
  {:added "3.0"}
  ([template]
   (cond (-> template meta :-) (p-is template)
         (-> template meta :%) (compile-matcher {:pattern template})
         (symbol? template)    (compile-matcher {:form template})
         (fn? template)        (compile-matcher {:is template})
         (list? template)      (compile-matcher {:pattern template})
         (h/regexp? template)     (compile-matcher {:code template})
         (vector? template)    (apply p-and (map compile-matcher template))
         (set? template)       (apply p-or (map compile-matcher template))
         (h/hash-map? template)
         (apply p-and
                (map (fn [[k v]]
                       (case k
                         :fn           (p-fn v)
                         :not          (p-not (compile-matcher v))
                         :is           (p-is v)
                         :or           (apply p-or (map compile-matcher v))
                         :and          (apply p-and (map compile-matcher v))
                         :equal        (p-equal v)
                         :type         (p-type v)
                         :meta         (p-meta v)
                         :form         (p-form v)
                         :pattern      (p-pattern v)
                         :code         (p-code v)
                         :parent       (p-parent v)
                         :child        (p-child v)
                         :first        (p-first v)
                         :last         (p-last v)
                         :nth          (p-nth v)
                         :nth-left     (p-nth-left v)
                         :nth-right    (p-nth-right v)
                         :nth-ancestor (p-nth-ancestor v)
                         :nth-contains (p-nth-contains v)
                         :ancestor     (p-ancestor v)
                         :contains     (p-contains v)
                         :sibling      (p-sibling v)
                         :left         (p-left v)
                         :right        (p-right v)
                         :left-of      (p-left-of v)
                         :right-of     (p-right-of v)
                         :left-most    (p-left-most v)
                         :right-most   (p-right-most v)))
                     template))
         :else (compile-matcher {:is template}))))

(defn p-parent
  "checks that the parent of the element contains a certain characteristic
   ((p-parent 'defn) (-> (nav/parse-string \"(defn x [])\") nav/next nav/next))
   => true
 
   ((p-parent {:parent 'if}) (-> (nav/parse-string \"(if (= x y))\") nav/down nav/next nav/next))
   => true
 
   ((p-parent {:parent 'if}) (-> (nav/parse-string \"(if (= x y))\") nav/down))
   => false"
  {:added "3.0"}
  ([template]
   (let [template (if (symbol? template) {:form template} template)
         m-fn (compile-matcher template)]
     (Matcher. (fn [nav]
                 (if-let [parent (nav/up nav)]
                   (and (not= (nav/value nav)
                              (nav/value parent))
                        (m-fn parent))))))))

(defn p-child
  "checks that there is a child of a container that has a certain characteristic
   ((p-child {:form '=}) (nav/parse-string \"(if (= x y))\"))
   => true
 
   ((p-child '=) (nav/parse-string \"(if (= x y))\"))
   => false"
  {:added "3.0"}
  ([template]
   (let [template (if (symbol? template) {:is template} template)
         m-fn (compile-matcher template)]
     (Matcher. (fn [nav]
                 (if-let [child (nav/down nav)]
                   (->> child
                        (iterate nav/right)
                        (take-while identity)
                        (map m-fn)
                        (some identity)
                        nil? not)
                   false))))))

(defn p-first
  "checks that the first element of the container has a certain characteristic
   ((p-first 'defn) (-> (nav/parse-string \"(defn x [])\")))
   => true
 
   ((p-first 'x) (-> (nav/parse-string \"[x y z]\")))
   => true
 
   ((p-first 'x) (-> (nav/parse-string \"[y z]\")))
   => false"
  {:added "3.0"}
  ([template]
   (let [template (if (symbol? template) {:is template} template)
         m-fn (compile-matcher template)]
     (Matcher. (fn [nav]
                 (if-let [child (nav/down nav)]
                   (m-fn child)))))))

(defn p-last
  "checks that the last element of the container has a certain characteristic
   ((p-last 1) (-> (nav/parse-string \"(defn [] 1)\")))
   => true
 
   ((p-last 'z) (-> (nav/parse-string \"[x y z]\")))
   => true
 
   ((p-last 'x) (-> (nav/parse-string \"[y z]\")))
   => false"
  {:added "3.0"}
  ([template]
   (let [template (if (symbol? template) {:is template} template)
         m-fn (compile-matcher template)]
     (Matcher. (fn [nav]
                 (if-let [child (-> nav nav/down nav/right-most)]
                   (m-fn child)))))))

(defn p-nth
  "checks that the last element of the container has a certain characteristic
   ((p-nth [0 'defn]) (-> (nav/parse-string \"(defn [] 1)\")))
   => true
 
   ((p-nth [2 'z]) (-> (nav/parse-string \"[x y z]\")))
   => true
 
   ((p-nth [2 'x]) (-> (nav/parse-string \"[y z]\")))
   => false"
  {:added "3.0"}
  ([[num template]]
   (let [template (if (symbol? template) {:is template} template)
         m-fn (compile-matcher template)]
     (Matcher. (fn [nav]
                 (if-let [child (nav/down nav)]
                   (let [child (if (zero? num)
                                 child
                                 (-> (iterate nav/next child)
                                     (nth num)))]
                     (m-fn child))))))))

(defn- p-nth-move
  [num template directon]
  (let [template (if (symbol? template) {:is template} template)
        m-fn (compile-matcher template)]
    (Matcher. (fn [nav]
                (let [dir (if (zero? num)
                            nav
                            (-> (iterate directon nav) (nth num)))]
                  (m-fn dir))))))

(defn p-nth-left
  "checks that the last element of the container has a certain characteristic
   ((p-nth-left [0 'defn]) (-> (nav/parse-string \"(defn [] 1)\") nav/down))
   => true
 
   ((p-nth-left [1 ^:& vector?]) (-> (nav/parse-string \"(defn [] 1)\") nav/down nav/right-most))
   => true"
  {:added "3.0"}
  ([[num template]]
   (p-nth-move num template nav/left)))

(defn p-nth-right
  "checks that the last element of the container has a certain characteristic
   ((p-nth-right [0 'defn]) (-> (nav/parse-string \"(defn [] 1)\") nav/down))
   => true
 
   ((p-nth-right [1 vector?]) (-> (nav/parse-string \"(defn [] 1)\") nav/down))
   => true"
  {:added "3.0"}
  ([[num template]]
   (p-nth-move num template nav/right)))

(defn p-nth-ancestor
  "search for match n-levels up
 
   ((p-nth-ancestor [2 {:contains 3}])
    (-> (nav/parse-string \"(* (- (+ 1 2) 3) 4)\")
        nav/down nav/right nav/down nav/right nav/down))
   => true"
  {:added "3.0"}
  ([[num template]]
   (p-nth-move num template nav/up)))

(defn tree-search
  "helper function for p-contains"
  {:added "3.0"}
  ([nav m-fn dir1 dir2]
   (binding [zip/*handler* (assoc zip/*handler*
                                  :step
                                  zip/+nil-handler+)]
     (if nav
       (cond (nil? nav) nil
             (m-fn nav) true

             :else
             (or (tree-search (dir1 nav) m-fn dir1 dir2)
                 (tree-search (dir2 nav) m-fn dir1 dir2)))))))

(defn p-contains
  "checks that any element (deeply nested also) of the container matches
   ((p-contains '=) (nav/parse-string \"(if (= x y))\"))
   => true
 
   ((p-contains 'x) (nav/parse-string \"(if (= x y))\"))
   => true"
  {:added "3.0"}
  ([template]
   (let [template (if (symbol? template) {:is template} template)
         m-fn (compile-matcher template)]
     (Matcher. (fn [nav]
                 (-> nav (nav/down) (tree-search m-fn nav/right nav/down)))))))

(defn tree-depth-search
  "helper function for p-nth-contains"
  {:added "3.0"}
  ([nav m-fn level dir1 dir2]
   (if nav
     (cond
       (< level 0) nil
       (and (= level 0) (m-fn nav)) true
       :else
       (or (tree-depth-search (dir1 nav) m-fn (dec level) dir1 dir2)
           (tree-depth-search (dir2 nav) m-fn level dir1 dir2))))))

(defn p-nth-contains
  "search for match n-levels down
 
   ((p-nth-contains [2 {:contains 1}])
    (nav/parse-string \"(* (- (+ 1 2) 3) 4)\"))
   => true"
  {:added "3.0"}
  ([[num template]]
   (let [template (if (symbol? template) {:is template} template)
         m-fn (compile-matcher template)]
     (Matcher. (fn [nav]
                 (let [[dir num] (if (zero? num)
                                   [nav num]
                                   [(nav/down nav) (dec num)])]
                   (tree-depth-search dir m-fn num nav/down nav/right)))))))

(defn p-ancestor
  "checks that any parent container matches
   ((p-ancestor {:form 'if}) (-> (nav/parse-string \"(if (= x y))\") nav/down nav/next nav/next))
   => true
   ((p-ancestor 'if) (-> (nav/parse-string \"(if (= x y))\") nav/down nav/next nav/next))
   => true"
  {:added "3.0"}
  ([template]
   (let [template (if (symbol? template) {:form template} template)
         m-fn (compile-matcher template)]
     (Matcher. (fn [nav]
                 (-> nav (nav/up) (tree-search m-fn nav/up (fn [_]))))))))

(defn p-sibling
  "checks that any element on the same level has a certain characteristic
   ((p-sibling '=) (-> (nav/parse-string \"(if (= x y))\") nav/down nav/next nav/next))
   => false
 
   ((p-sibling 'x) (-> (nav/parse-string \"(if (= x y))\") nav/down nav/next nav/next))
   => true"
  {:added "3.0"}
  ([template]
   (let [template (if (symbol? template) {:is template} template)
         m-fn (compile-matcher template)]
     (Matcher. (fn [nav]
                 (or (-> nav nav/right (tree-search m-fn nav/right (fn [_])))
                     (-> nav nav/left  (tree-search m-fn nav/left (fn [_])))
                     false))))))

(defn p-left
  "checks that the element on the left has a certain characteristic
   ((p-left '=) (-> (nav/parse-string \"(if (= x y))\") nav/down nav/next nav/next nav/next))
   => true
 
   ((p-left 'if) (-> (nav/parse-string \"(if (= x y))\") nav/down nav/next))
   => true"
  {:added "3.0"}
  ([template]
   (let [template (if (symbol? template) {:is template} template)
         m-fn (compile-matcher template)]
     (Matcher. (fn [nav]
                 (if-let [left (-> nav nav/left)]
                   (m-fn left)))))))

(defn p-right
  "checks that the element on the right has a certain characteristic
   ((p-right 'x) (-> (nav/parse-string \"(if (= x y))\") nav/down nav/next nav/next))
   => true
 
   ((p-right {:form '=}) (-> (nav/parse-string \"(if (= x y))\") nav/down))
   => true"
  {:added "3.0"}
  ([template]
   (let [template (if (symbol? template) {:is template} template)
         m-fn (compile-matcher template)]
     (Matcher. (fn [nav]
                 (if-let [right (-> nav nav/right)]
                   (m-fn right)))))))

(defn p-left-of
  "checks that any element on the left has a certain characteristic
   ((p-left-of '=) (-> (nav/parse-string \"(= x y)\") nav/down nav/next))
   => true
 
   ((p-left-of '=) (-> (nav/parse-string \"(= x y)\") nav/down nav/next nav/next))
   => true"
  {:added "3.0"}
  ([template]
   (let [template (if (symbol? template) {:is template} template)
         m-fn (compile-matcher template)]
     (Matcher. (fn [nav]
                 (or (-> nav nav/left (tree-search m-fn nav/left (fn [_])))
                     false))))))

(defn p-right-of
  "checks that any element on the right has a certain characteristic
   ((p-right-of 'x) (-> (nav/parse-string \"(= x y)\") nav/down))
   => true
 
   ((p-right-of 'y) (-> (nav/parse-string \"(= x y)\") nav/down))
   => true
 
   ((p-right-of 'z) (-> (nav/parse-string \"(= x y)\") nav/down))
   => false"
  {:added "3.0"}
  ([template]
   (let [template (if (symbol? template) {:is template} template)
         m-fn (compile-matcher template)]
     (Matcher. (fn [nav]
                 (or (-> nav nav/right (tree-search m-fn nav/right (fn [_])))
                     false))))))

(defn p-left-most
  "checks that any element on the right has a certain characteristic
   ((p-left-most true) (-> (nav/parse-string \"(= x y)\") nav/down))
   => true
 
   ((p-left-most true) (-> (nav/parse-string \"(= x y)\") nav/down nav/next))
   => false"
  {:added "3.0"}
  ([bool]
   (Matcher. (fn [nav] (= (-> nav nav/left nil?) bool)))))

(defn p-right-most
  "checks that any element on the right has a certain characteristic
   ((p-right-most true) (-> (nav/parse-string \"(= x y)\") nav/down nav/next))
   => false
 
   ((p-right-most true) (-> (nav/parse-string \"(= x y)\") nav/down nav/next nav/next))
   => true"
  {:added "3.0"}
  ([bool]
   (Matcher. (fn [nav] (= (-> nav nav/right nil?) bool)))))
