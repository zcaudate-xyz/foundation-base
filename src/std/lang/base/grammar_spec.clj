(ns std.lang.base.grammar-spec
  (:require [std.lib :as h :refer [defimpl]]
            [std.string :as str]))

(def ^:dynamic *symbol* nil)

(defn get-comment
  "gets the comment access prefix for a language
 
   (get-comment helper/+default+ {})
   => \"//\""
  {:added "4.0"}
  [grammar _] 
  (-> grammar :default :comment :prefix))

(defn format-fargs
  "formats function inputs"
  {:added "3.0"}
  ([[doc? attr? & body]]
   (let [[doc attr body] (h/fn:init-args doc? attr? body)
         body (cond (vector? (first body))
                    body

                    (vector? (ffirst body))
                    (first body)

                    :else (h/error "Invalid body" {:body body}))]
     [doc attr body])))

(defn format-defn-mixins
  "applies all mixins in order"
  {:added "4.0"}
  [m mixins]
  (reduce (fn [m entry]
            (cond (symbol? entry)
                  ((resolve entry) m)

                  (h/form? entry)
                  (eval entry)

                  (map? entry)
                  (merge m (eval entry))
                  
                  :else m))
          m
          mixins))

(defn format-defn
  "standardize defn forms"
  {:added "3.0"}
  ([[op sym & args]]
   (let [[doc attr body] (format-fargs args)
         attr (merge attr {:doc doc})
         {mixins :!
          :as sym-meta} (meta sym)
         mixed (if (not (nil? mixins))
                 (binding [*symbol* sym]
                   (format-defn-mixins (merge attr sym-meta)
                                       (if (vector?  mixins)
                                         mixins
                                         [mixins]))))]
     [attr
      (concat (list op (with-meta sym
                         (dissoc (merge sym-meta mixed) :!)))
              body)])))

(defn tf-for-index
  "default for-index transform
 
   (tf-for-index '(for:index
                   [i [0 2 3]]))
   => '(for [(var i := 0) (< i 2) (:= i (+ i 3))])"
  {:added "4.0"}
  [[_ [i [start stop step]] & body]]
  (let [step (or step 1)
        sign (if (and (number? step)
                      (pos? step))
               '<
               '>)]
    `(~'for [(var ~i := ~start) (~sign ~i ~stop) (:= ~i (~'+ ~i ~step))]
      ~@body)))

;;
;; SPEC;
;;

(def +op-builtin+
  [{:op :with-lang     :symbol #{'!:lang}      :emit :with-lang}
   {:op :with-eval     :symbol #{'!:eval}      :emit :with-eval}
   {:op :with-deref    :symbol #{'!:deref}     :emit :with-deref}
   {:op :with-decorate :symbol #{'!:decorate}  :emit :with-decorate}
   {:op :free          :symbol #{:-}           :emit :free      :sep " " :type :free}
   {:op :squash        :symbol #{:%}           :emit :squash}
   {:op :comment       :symbol #{:#}           :emit :comment   :value #'get-comment :type :free}
   {:op :index         :symbol #{'.}           :emit :index     :value true   :raw "."  :free " "}
   {:op :quote         :symbol #{'quote}       :emit :quote}
   {:op :internal      :symbol #{'%}           :emit :internal}
   {:op :internal-str  :symbol #{'-%%-}        :emit :internal-str}])

(def +op-builtin-global+
  [{:op :with-global   :symbol #{'!:G}         :emit :with-global}])

(def +op-builtin-module+
  [{:op :with-module   :symbol #{'!:module}    :emit :with-module}])

(def +op-builtin-helper+
  [{:op :with-uuid     :symbol #{'!:uuid}      :emit :with-uuid}
   {:op :with-rand     :symbol #{'!:rand}      :emit :with-rand}])

(def +op-free-control+
  [{:op :c-slash     :symbol #{\\}           :emit :free       :raw "\n"  :value true   :sep " "  :type :free}
   {:op :c-star      :symbol #{\*}           :emit :free       :raw "\n"  :value true   :sep " "  :type :free}
   {:op :c-minus     :symbol #{\- 'comment}  :emit :discard    :raw " "   :value true   :free " " :sep " " :type :free}
   {:op :c-vert      :symbol #{\|}           :emit :indent     :raw "|"   :value true}])

(def +op-free-literal+                       
  [{:op :c-null      :symbol #{\0}           :emit :throw   :raw ""    :value true   :free  ""}
   {:op :c-hat       :symbol #{\^}           :emit :throw   :raw "^"   :value true   :free  "^"}
   {:op :c-comma     :symbol #{\,}           :emit :throw   :raw ","   :value true   :free  ","}
   {:op :c-colon     :symbol #{\:}           :emit :throw   :raw ":"   :value true   :free  ":" }
   {:op :c-semicolon :symbol #{\;}           :emit :throw   :raw ";"   :value true   :free  ";" }
   {:op :c-lparens   :symbol #{\(}           :emit :throw   :raw "("   :value true   :free  "(" }
   {:op :c-rparens   :symbol #{\)}           :emit :throw   :raw ")"   :value true   :free  ")" }
   {:op :c-lbracket  :symbol #{\[}           :emit :throw   :raw "["   :value true   :free  "["}
   {:op :c-rbracket  :symbol #{\]}           :emit :throw   :raw "]"   :value true   :free  "]"}
   {:op :c-lcurly    :symbol #{\{}           :emit :throw   :raw "["   :value true   :free  "{"}
   {:op :c-rcurly    :symbol #{\}}           :emit :throw   :raw "}"   :value true   :free  "}"}
   {:op :c-dquote    :symbol #{\"}           :emit :throw   :raw "\""  :value true   :free  "\""}
   {:op :c-squote    :symbol #{\'}           :emit :throw   :raw "'"   :value true   :free  "'"}
   {:op :c-at        :symbol #{\@}           :emit :throw   :raw "@"   :value true   :free  "@"}
   {:op :c-tilda     :symbol #{\~}           :emit :throw   :raw "~"   :value true   :free  "~"}
   {:op :c-backtick  :symbol #{\`}           :emit :throw   :raw "`"   :value true   :free  "`"}])

(def +op-math+
  [{:op :add         :symbol #{'+}           :emit :infix   :raw "+"}
   {:op :mul         :symbol #{'*}           :emit :infix   :raw "*"}
   {:op :sub         :symbol #{'-}           :emit :infix-  :raw "-"}
   {:op :div         :symbol #{'/}           :emit :infix*  :raw "/"   :default 1}
   {:op :pow         :symbol #{'pow}         :emit :bi      :raw "^"}
   {:op :mod         :symbol #{'mod}         :emit :bi      :raw "%"}])

(def +op-compare+
  [{:op :eq          :symbol #{'==}          :emit :bi      :raw "=="}
   {:op :neq         :symbol #{'not=}        :emit :bi      :raw "!="}
   {:op :lt          :symbol #{'<}           :emit :infix   :raw "<"}
   {:op :lte         :symbol #{'<=}          :emit :bi      :raw "<="}
   {:op :gt          :symbol #{'>}           :emit :bi      :raw ">"}
   {:op :gte         :symbol #{'>=}          :emit :bi      :raw ">="}])

(def +op-logic+
  [{:op :inif        :symbol #{:?}           :emit :infix-if}
   {:op :not         :symbol #{'not}         :emit :pre     :raw "!"}
   {:op :or          :symbol #{'or}          :emit :infix   :raw "||"}
   {:op :and         :symbol #{'and}         :emit :infix   :raw "&&"}])

(def +op-counter+
  [{:op :incby       :symbol #{:+=}          :emit :assign   :raw "+="}
   {:op :decby       :symbol #{:-=}          :emit :assign   :raw "-="}
   {:op :mulby       :symbol #{:*=}          :emit :assign   :raw "*="}
   {:op :incto       :symbol #{:++}          :emit :pre      :raw "++"}
   {:op :decto       :symbol #{:--}          :emit :pre      :raw "--"}])

(def +op-return+
  [{:op :ret         :symbol #{'return}      :emit :return   :raw "return"}
   {:op :break       :symbol #{'break}       :emit :return   :raw "break"}])

(def +op-throw+
  [{:op :throw       :symbol #{'throw}       :emit :return   :raw "throw"}])

(def +op-await+
  [{:op :await       :symbol #{'await}       :emit :return   :raw "await"}])

(def +op-async+
  [{:op :async       :symbol #{'async}       :emit :return   :raw "async"}])

(def +op-data-table+
  [{:op :table       :symbol #{'tab}         :emit :table}])

(def +op-data-shortcuts+
  [{:op :spread      :symbol #{:..}          :emit :pre  :raw "..."}])

(def +op-data-range+
  [{:op :range       :symbol #{:to}          :emit :between  :value true :raw ":"}])

(def +op-vars+
  [{:op :seteq       :symbol #{:=}           :emit :assign      :raw "="}
   {:op :var         :symbol #{'var}         :emit :def-assign  :raw ""      :assign "="}])

(def +op-bit+        
  [{:op :bor         :symbol #{'b:|}      :emit :infix    :raw "|"}
   {:op :band        :symbol #{'b:&}      :emit :infix    :raw "&"}
   {:op :bxor        :symbol #{'b:xor}    :emit :infix    :raw "^"}
   {:op :bsr         :symbol #{'b:>>}     :emit :bi       :raw ">>"}
   {:op :bsl         :symbol #{'b:<<}     :emit :bi       :raw "<<"}])

(def +op-pointer+
  [{:op :ptr-deref   :symbol '#{:&}  :raw "&" :emit :pre}
   {:op :ptr-ref     :symbol '#{:*}  :raw "*" :emit :pre}])

;;
;; LAMBDA
;;

(def +op-fn+
  [{:op   :fn        :symbol #{'fn}
    :type :fn        :block  {:raw "function"
                              :main #{:body}}}])

(def +op-block+
  [{:op   :block     :symbol #{'block}           
    :type :block     :block  {:raw ""
                              :main #{:body}}}])

;;
;; BLOCKS
;;


(def +op-control-base+
  [{:op :do*         :symbol #{'do*}        :emit :do*  :type :block}
   {:op :do          :symbol #{'do}         :emit :do   :type :block}])

(def +op-control-general+
  [{:op :for         :symbol #{'for}       
    :type :block     :block  {:main  #{:parameter :body}}}
   {:op :while       :symbol #{'while}                             
    :type :block     :block  {:main #{:parameter :body}}} 
   {:op :branch      :symbol #{'br*}                            
    :type :block     :block  {:main  #{}
                              :raw ""
	                      :control [[:if      {:required true
	                                           :main #{:parameter :body}}]
                                        [:elseif  {:main #{:parameter :body}}]
                                        [:else    {:main #{:body}}]]}}])

(def +op-control-try-catch+
  [{:op :try         :symbol #{'try}     
    :type :block     :block  {:main #{:body}
                              :control [[:catch   {:required true
                                                   :main #{:parameter :body}}]
                                        [:finally {:main #{:body}}]]}}])

;;
;; TOP LEVEL
;;

(def +op-top-base+
  [{:op :defn        :symbol #{'defn 'defn- 'defabstract} :spec :defn    
    :type :def       :section :code             :fn true
    :arglists '([sym doc? & [attr? & body]])      
    :format #'format-defn}
   
   {:op :def         :symbol #{'def}        :spec :def     
    :type :def       :section :code
    :arglists '([sym body])}
   
   {:op :defrun      :symbol #{'defrun}     :spec :defrun   
    :type :def       :section :code
    :arglists '([sym & body])}])

(def +op-top-global+
  [{:op :defglobal   :symbol #{'defglobal 'deftemp}  :spec :def  
    :type :def       :section :code         :raw ""
    :arglists '([sym body])}])

;;
;; OBJECT ORIENTATED
;;

(def +op-class+
  [{:op :new         :symbol #{'new}        :emit :new  :value true :raw "new"}
   {:op :defclass    :symbol #{'defclass}   :spec :defclass  
    :type :def       :section :code         :abstract true}
   {:op :static-invoke      :symbol #{'$}   :emit :static-invoke}
   {:op :this        :symbol #{'this}       :emit :throw     :raw "this"  :value true}
   {:op :super       :symbol #{'super}      :emit :invoke    :raw "super" :value true}
   {:op :fn.inner    :symbol #{'fn.inner}   :type :fn :block  {:raw "" :main #{:body}}}
   {:op :var.inner   :symbol #{'var.inner}  :assign "=" :raw "" :emit :def-assign}])

(def +op-for+
  [{:op :for-index   :symbol #{'for:index}     :emit :macro :macro #'tf-for-index :style/indent 1}
   {:op :for-object  :symbol #{'for:object}    :emit :abstract :style/indent 1}
   {:op :for-array   :symbol #{'for:array}     :emit :abstract :style/indent 1}
   {:op :for-iter    :symbol #{'for:iter}      :emit :abstract :style/indent 1}
   {:op :for-return  :symbol #{'for:return}    :emit :abstract :style/indent 1}
   {:op :for-async   :symbol #{'for:async}     :emit :abstract :style/indent 1}
   {:op :for-try     :symbol #{'for:try}       :emit :abstract :style/indent 1}])

(def +op-coroutine+
  [{:op :defgen    :symbol #{'defgen}   :spec :defgen  
    :type :def     :section :code       :abstract true :fn true
    :arglists '([sym doc? & [attr? & body]])
    :format #'format-defn}
   {:op :yield     :symbol #{'yield}     :emit :return   :raw "yield"}])
