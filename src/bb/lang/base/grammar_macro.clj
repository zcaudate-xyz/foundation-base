(ns bb.lang.base.grammar-macro
  (:require [bb.lib :as h :refer [defimpl]]
            [bb.lang.base.grammar-spec :as spec]))

;;
;; macros
;;

(defn tf-macroexpand
  "macroexpands the current form"
  {:added "3.0"}
  ([form]
   (macroexpand-1 form)))

(defn tf-when
  "transforms `when` to branch

   (tf-when '(when true :A :B :C))
   => '(br* (if true :A :B :C))"
  {:added "3.0"}
  ([[_ check & body]]
   `(~'br* (~'if ~check ~@body))))

(defn tf-if
  "transforms `if` to branch

   (tf-if '(if true :A :B))
   => '(br* (if true :A) (else :B))"
  {:added "3.0"}
  ([[_ check then & [else]]]
   `(~'br*
     (~'if ~check ~then)
     ~@(if else
         `[(~'else ~else)]))))

(defn tf-cond
  "transforms `cond` to branch"
  {:added "3.0"}
  ([[_ check then & more]]
   (let [args (partition 2 more)
         [ei el] (if (= :else (first (last args)))
                   [(butlast args) (last args)]
                   [args nil])]
     `(~'br*
       (~'if ~check ~then)
       ~@(map (fn [[check then]]
                `(~'elseif ~check ~then))
              ei)
       ~@(if el
           `[(~'else ~(second el))])))))

(defn tf-let-bind
  "converts to a statement"
  {:added "4.0"}
  ([[_ bindings & body]]
   `(~'do* ~@(map
              (fn [[sym val]]
                (if (= '_ sym)
                  val
                  `(~'var ~sym := ~val)))
              (partition 2 bindings))
     ~@body)))

(defn tf-case
  "transforms the case statement to switch representation"
  {:added "4.0"}
  ([[_ val & options]]
   (let [cs (partition-all 2 options)
         [cs df] (if (= 1 (count (last cs)))
                   [(butlast cs) [(last cs)]]
                   [cs []])]
     `(~'switch [~val]
       ~@(map (fn [[val body]]
                `(~'case [~val] ~body))
              cs)
       ~@(map (fn [[body]]
                `(~'default ~body))
              df)))))

(defn tf-lambda-arrow
  "generalized lambda transformation"
  {:added "4.0"}
  ([[_ & more]]
   (let [[args & exprs] (if (= 1 (count more))
                          [[] (first more)]
                          more)
         final   (last exprs)
         body    (butlast exprs)]
     `(~'fn ~args ~@body (~'return ~final)))))

(defn tf-tcond
  "transforms the ternary cond

   (tf-tcond '(:?> :a a :b b :else c))
   => '(:? :a [a (:? :b [b c])])"
  {:added "4.0"}
  ([[_ & more]]
   {:op :tcond     :symbol '#{:?>}    :macro tf-tcond         :type :macro}
   (let [pairs (map vec (partition 2 more))
         _ (if (not= :else (first (last pairs)))
             (h/error "ternary cond has to end with :else" {:form (last pairs)}))]
     (reduce (fn [acc [q alt]]
               (list :? q [alt acc]))
             (last (last pairs))
             (reverse (butlast pairs))))))

(defn tf-xor
  "transforms to xor using ternary if

   (tf-xor '(xor a b))
   => '(:? a b (not b))"
  {:added "4.0"}
  ([[_ a b]]
   (list :? a b (list 'not b))))

(defn tf-doto
  "basic transformation for `doto` syntax"
  {:added "4.0"}
  ([[_ sym & forms]]
   (let [sforms (map (fn [form]
                       (apply list (first form) sym (rest form)))
                     forms)]
     (apply list 'do sforms))))

(defn tf-do-arrow
  "do:> transformation

   (tf-do-arrow '(do:>
                  1 2 (return 3)))
   => '((quote ((fn [] 1 2 (return 3)))))"
  {:added "4.0"}
  ([[_ & body]]
   `('((~'fn [] ~@body)))))

(defn tf-forange
  "creates the forange form

   (tf-forange '(forange [i 10] (print i)))
   => '(for [(var i 0) (< i 10) [(:= i (+ i 1))]] (print i))

   (tf-forange '(forange [i [10 3 -2]] (print i)))
   => '(for [(var i 10) (< i 3) [(:= i (- i 2))]] (print i))"
  {:added "4.0"}
  ([[_ [type? sym size params] & body]]
   (let [[types sym size params] (if (keyword? type?)
                                   [[type?] sym size params]
                                   [nil type? sym size])
         [start end step] (if (vector? size)
                            size
                            [0 size])
         step (or step 1)
         [sign step] (if (and (number? step)
                              (neg? step))
                       ['- (- step)]
                       ['+ step])]
     `(~'for [(~'var ~@types ~sym ~start) (~'< ~sym ~end) [(:= ~sym (~sign ~sym ~step))
                                                           ~@params]]
             ~@body))))

(def +op-macro+
  [{:op :tfirst    :symbol #{'->}     :emit :macro  :macro #'tf-macroexpand   :type :template}
   {:op :tlast     :symbol #{'->>}    :emit :macro  :macro #'tf-macroexpand   :type :template}
   {:op :doto      :symbol #{'doto}   :emit :macro  :macro  #'tf-doto     :type :block}
   {:op :if        :symbol #{'if}     :emit :macro  :macro  #'tf-if       :type :block}
   {:op :cond      :symbol #{'cond}   :emit :macro  :macro  #'tf-cond     :type :block}
   {:op :when      :symbol #{'when}   :emit :macro  :macro  #'tf-when     :type :block}])

(def +op-macro-arrow+
  [{:op :dofn      :symbol #{'do:>}   :emit :macro :macro #'tf-do-arrow       :type :template}
   {:op :fn-arrow  :symbol '#{fn:>}   :emit :macro :macro #'tf-lambda-arrow   :type :template}])

(def +op-macro-let+
  [{:op :let-bind  :symbol #{'let}    :macro #'tf-let-bind       :type :macro}])

(def +op-macro-xor+
  [{:op :txor      :symbol #{'xor}    :macro #'tf-xor            :type :macro}])

(def +op-macro-case+
  [{:op :switch     :symbol #{'switch}     :emit :inner
    :type :block    :block  {:main #{:parameter  :body}
                             :control [[:case     {:required true
                                                   :main #{:parameter :body}}]
                                       [:default  {:main #{:body}}]]}}
   {:op :case       :symbol #{'case}   :emit :macro  :macro  #'tf-case     :type :block}])

(def +op-macro-forange+
  [{:op :forange :symbol '#{forange} :style/indent 1
    :emit :macro  :macro  #'tf-forange  :type :block}])
