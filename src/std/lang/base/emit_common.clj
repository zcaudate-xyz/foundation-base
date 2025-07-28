(ns std.lang.base.emit-common
  (:require [std.string :as str]
            [std.lib :as h]
            [std.lang.base.util :as ut]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.emit-helper :as helper]))

;; controls whether to fail with a stacktrace
(def ^:dynamic *explode* nil)

(def ^:dynamic *trace* nil)

(def ^:dynamic *indent* 0)

(def ^:dynamic *compressed* false)

(def ^:dynamic *multiline* false)

(def ^:dynamic *max-len* 60)

(def ^:dynamic *emit-internal* false)

(def ^:dynamic *emit-fn* helper/default-emit-fn)

(defmacro with:explode
  "form to control `explode` option"
  {:added "4.0"}
  ([& body]
   `(binding [*explode* true]
      ~@body)))

(defmacro with-trace
  "form to control `trace` option"
  {:added "4.0"}
  ([& body]
   `(binding [*trace* true]
      ~@body)))

(defmacro with-compressed
  "formats without newlines and indents"
  {:added "3.0"}
  ([& body]
   `(binding [*compressed* true]
      ~@body)))

(defmacro with-indent
  "adds indentation levels"
  {:added "3.0"}
  ([[& increment] & body]
   `(binding [*indent* (+ *indent* (or ~@increment 2))]
      ~@body)))

(defn newline-indent
  "returns a newline with indent"
  {:added "3.0"}
  []
  (if-not *compressed*
    (str "\n"
         (apply str (repeat *indent* " ")))
    " "))

(defn emit-reserved-value
  "emits a reserved value"
  {:added "4.0"}
  [form {:keys [reserved] :as grammar} mopts]
  (if-let [{:keys [emit value free raw] :as props} (get reserved form)]
    (or (and value
             (or free
                 raw
                 (h/error "No raw value found" props)))
        
        (h/error "Cannot use reserved token in body" props))))

(defn emit-free-raw
  "emits free value"
  {:added "4.0"}
  ([sep args grammar mopts]
   (let [str-array (binding [*indent* 0]
                     (mapv (fn [e]
                             [e (or (and (string? e) e)
                                    (and (keyword? e)
                                         (or (emit-reserved-value e grammar mopts)
                                             (h/strn e)))
                                    (*emit-fn* e grammar mopts))])
                           args))]
     (loop [[prev out]   (first str-array)
            [[curr s] :as input] (rest str-array)]
       (cond (empty? input)
             out
             
             (or (char? curr)
                 (char? prev))
             (recur [curr (str out s)]
                    (rest input))
             
             :else
             (recur [curr (str out (or sep " ") s)]
                    (rest input)))))))

(defn emit-free
  "emits string with multiline support"
  {:added "4.0"}
  ([{:keys [sep]} [tag & more :as form] grammar mopts]
   (let [{:keys [indent prefix full-body]} (meta form)
         prev    *indent*
         output  (emit-free-raw sep more grammar mopts)]
     (if (str/multi-line? output)
       (let [indent-fn (if full-body
                         str/indent
                         str/indent-rest)]
         (indent-fn output
                    (+ (or prev 0)
                       (or indent 0))))
       output))))

;;
;; COMMENT
;;

(defn emit-comment
  "emits a comment"
  {:added "4.0"}
  ([_ [tag & more :as form] grammar mopts]
   (let [{:keys [indent prefix full-body]} (meta form)
         prefix  (or prefix (-> grammar :default :comment :prefix))
         prev    *indent*
         output  (emit-free-raw " " more grammar mopts)]
     (if (str/multi-line? output)
       (let [indent-fn (if full-body
                         str/indent
                         str/indent-rest)]
         (str prefix " "
              (indent-fn output
                         0
                         {:custom (str (str/spaces (+ (or prev 0)
                                                      (or indent 0)))
                                       prefix
                                       " ")})))
       (str prefix " " output)))))

(defn emit-indent
  "emits an indented form"
  {:added "4.0"}
  ([_ [tag & more :as form] grammar mopts]
   (let [{:keys [indent prefix full-body]} (meta form)]
     (emit-comment nil
                   (with-meta form
                     (merge {:prefix " "}
                            (meta form)))
                   grammar mopts))))

;;
;; MACRO
;;

(defn emit-macro
  "emits form"
  {:added "4.0"}
  ([_ [tag & more :as form] grammar mopts]
   (let [{:keys [macro] :as m} (get-in grammar [:reserved tag])]
     (if m
       (binding [preprocess/*macro-opts* mopts
                 preprocess/*macro-grammar* grammar]
         (*emit-fn* (macro form) grammar mopts))
       (h/error "Not found: " {:input form})))))

;;
;; HELPERS
;;

(defn emit-array
  "returns an array of emitted strings"
  {:added "4.0"}
  ([array grammar mopts]
   (emit-array array grammar mopts *emit-fn*))
  ([array grammar mopts emit-fn]
   (map #(emit-fn % grammar mopts) array)))

(defn emit-wrappable?
  "checks if form if wrappable"
  {:added "4.0"}
  ([form grammar]
   (if (h/form? form)
     (let [infix? #{:infix :infix* :infix- :infix-if
                    :assign :bi
                    :prefix :postfix}
           {:keys [macro emit wrappable]} (get-in grammar [:reserved (first form)])]
       (boolean (or wrappable
                    (infix? emit)
                    ('#{fn fn.inner} (first form))
                    (and (= :macro emit)
                         (emit-wrappable? (macro form) grammar)))))
     false)))

(defn emit-squash
  "emits a squashed representation"
  {:added "4.0"}
  [_ [tag & more] grammar mopts]
  (str/join (emit-array more grammar mopts)))

(defn emit-wrapping
  "emits a potentially wrapped form"
  {:added "4.0"}
  ([form grammar mopts]
   (let [result (*emit-fn* form grammar mopts)
         {:keys [start end]} (get-in grammar [:default :common])
         wrap? (emit-wrappable? form grammar)]
     (if wrap?
       (str start result end)
       result))))

(defn wrapped-str
  "wrapped string using `:start` and `:end` keys of grammar"
  {:added "3.0"}
  ([inner path grammar]
   (let [{:keys [start end]} (helper/get-options grammar path)]
     (str start inner end))))

;;
;; OP
;;

(defn emit-unit
  "emits a unit"
  {:added "4.0"}
  ([{:keys [raw default transform] :as props} [_ value] grammar mopts]
   (*emit-fn* (cond-> (or value default)
                transform (transform raw))
              grammar mopts)))

(defn emit-internal
  "emits string within the form"
  {:added "4.0"}
  ([[_ value] grammar mopts]
   (*emit-fn* value grammar mopts)))

(defn emit-internal-str
  "emits internal string"
  {:added "4.0"}
  ([[_ value] grammar mopts]
   (binding [*emit-internal* true]
     (cond (vector? value)
           (str/join "\n"
                     (mapv #(*emit-fn* % grammar mopts)
                           value))

           :else
           (*emit-fn* value grammar mopts)))))

(defn emit-pre
  "emits string before the arg"
  {:added "3.0"}
  ([raw args grammar mopts]
   (assert (= 1 (count args)) (str "Single argument for " raw))
   (str raw (emit-wrapping (first args) grammar mopts))))

(defn emit-post
  "emits string after the arg"
  {:added "3.0"}
  ([raw args grammar mopts]
   (assert (= 1 (count args)) (str "Single argument " raw))
   (str (emit-wrapping (first args) grammar mopts) raw)))


(defn emit-prefix
  "emits operator before the arg"
  {:added "4.0"}
  ([raw args grammar mopts]
   (assert (= 1 (count args)) (str "Single argument for " raw))
   (let [{:keys [space]} (get-in grammar [:default :common])]
     (str raw space (emit-wrapping (first args) grammar mopts)))))

(defn emit-postfix
  "emits operator before the arg"
  {:added "4.0"}
  ([raw args grammar mopts]
   (assert (= 1 (count args)) (str "Single argument " raw))
   (let [{:keys [space]} (get-in grammar [:default :common])]
     (str (emit-wrapping (first args) grammar mopts) space raw))))

(defn emit-infix
  "emits infix ops"
  {:added "3.0"}
  ([raw args grammar mopts]
   (assert (pos? (count args)) (str "One or more arguments " raw))
   (let [{:keys [start end space]} (get-in grammar [:default :common])]
     (str/join (str space raw space)
               (emit-array args grammar mopts emit-wrapping)))))

(defn emit-infix-default
  "emits infix with a default value"
  {:added "3.0"}
  ([raw args default grammar mopts]
   (assert (pos? (count args)) (str "One or more arguments " raw))
   (let [args (case (count args)
                1 (cons default args)
                args)]
     (emit-infix raw args grammar mopts))))

(defn emit-infix-pre
  "emits infix with a default value"
  {:added "3.0"}
  ([raw args grammar mopts]
   (assert (pos? (count args)) (str "One or more arguments " raw))
   (case (count args)
     1 (emit-pre raw args grammar mopts)
     (emit-infix raw args grammar mopts))))

(defn emit-infix-if-single
  "checks for infix in single
 
   (emit-infix-if '(:? true x y)
                  helper/+default+
                  {})
   => \"true ? x : y\""
  {:added "4.0"}
  ([[_ check then else] grammar mopts]
   (let [{:keys [start end space]} (get-in grammar [:default :common])
         inif (get-in grammar [:default :infix :if])]
     (h/-> (emit-array [check then else] grammar mopts emit-wrapping)
           (interleave [(str space (or (:check inif) "?") space)
                        (str space (or (:then inif) ":") space) ""])
           (str/join)))))

(defn emit-infix-if
  "emits an infix if string"
  {:added "3.0"}
  ([[_ & forms] grammar mopts]
   (let [[check & args] forms]
     (cond (>= 2 (count args))
           (emit-infix-if-single (apply list :? check args) grammar mopts)

           :else
           (let [pairs (map vec (partition 2 forms))
                 _ (if (not= :else (first (last pairs)))
                     (h/error "ternary cond has to end with :else" {:form (last pairs)}))]
             (emit-infix-if-single
              (reduce (fn [acc [q alt]]
                        (list :? q alt acc))
                      (last (last pairs))
                      (reverse (butlast pairs)))
              grammar mopts))))))

(defn emit-between
  "emits the raw symbol between two elems"
  {:added "3.0"}
  ([raw args grammar mopts]
   (str/join raw (emit-array args grammar mopts emit-wrapping))))

(defn emit-bi
  "emits infix with two args"
  {:added "3.0"}
  ([raw args grammar mopts]
   (assert (= 2 (count args)) (str "Two arguments for " raw))
   (emit-infix raw args grammar mopts)))

(defn emit-assign
  "emits a setter expression"
  {:added "3.0"}
  ([raw args grammar mopts]
   (assert (= 2 (count args)) (str "Two arguments for assign"))
   (let [{:keys [space assign]} (get-in grammar [:default :common])]
     (str/join (str space (or raw assign) space)
               (emit-array args grammar mopts emit-wrapping)))))

(defn emit-return-do
  "creates a return statement on `do` block"
  {:added "4.0"}
  ([arg grammar mopts]
   (let [{:keys [space]} (get-in grammar [:default :common])
         pre-form (butlast arg)
         ret-form (list 'return (last arg))]
     (*emit-fn* (apply list
                       (concat pre-form
                               [ret-form]))
                grammar mopts))))

(defn emit-return-base
  "return base type"
  {:added "4.0"}
  ([raw args grammar mopts]
   (let [{:keys [space sep]} (get-in grammar [:default :common])
         {:keys [multi]} (get-in grammar [:default :return])
         _ (if (not multi)
             (assert (>= 2 (count args)) (str "One or Zero arguments for " raw)))
         out (->> (emit-array args grammar mopts)
                  (str/join (str sep space)))]
     (cond (and (h/form? (first args))
                (:return/none (meta (first args))))
           out

           :else
           (str raw (if (not-empty args)
                      space)
                out)))))

(defn emit-return
  "creates a return type statement"
  {:added "3.0"}
  ([raw args grammar mopts]
   (let [val (first args)]
     (cond (not (h/form? val))
           (emit-return-base raw args grammar mopts)
           
           ('#{return} (first val))
           (emit-return raw (apply list (rest val)) grammar mopts)
           
           ('#{do do*} (first val))
           (emit-return-do val grammar mopts)

           :else
           (emit-return-base raw args grammar mopts)))))

;;
;; GLOBAL
;;

(defn emit-with-global
  "customisable emit function for global vars"
  {:added "4.0"}
  [_ [_ token] grammar mopts]
  (if-let [transform (get-in grammar [:token :symbol :global])]
    (*emit-fn* (transform token grammar mopts) grammar mopts)
    (apply str (helper/emit-symbol-full token (namespace token) grammar))))

;;
;; TOKEN
;;

(defn emit-symbol-classify
  "classify symbol given options
 
   (emit-symbol-classify 't/hello {:module {:alias '{t table}}})
   => '[:alias table]
 
   (emit-symbol-classify 't.n/hello {:module {:alias '{t table}}})
   => '[:unknown t.n]"
  {:added "3.0"}
  ([sym {:keys [lang module entry book snapshot] :as mopts}]
   (cond (not (namespace sym))
         [:unknown nil]

         :else
         (let [[sym-ns sym-id] (ut/sym-pair sym)]
           (or (if-let [ns (get (:alias module) sym-ns)]
                 [:alias ns])
               
               (let [link-ns (or (if (get (:internal module)
                                          sym-ns)
                                   sym-ns)
                                 
                                 (if (or (= (:id module) sym-ns)
                                         (= (:module entry) sym-ns))
                                   sym-ns)

                                 (get (:link module) sym-ns))
                     sym-ns  (or link-ns sym-ns) 
                     ext-module (if book
                                  (get-in book [:modules sym-ns])
                                  (get-in snapshot [lang :book :modules sym-ns]))]
                 (cond (and (not link-ns)
                            (not ext-module)) nil

                       (= :defglobal (get-in ext-module [:code sym-id :op-key]))
                       [:global sym-ns]

                       (or (= (:id module) sym-ns)
                           (= (:module entry) sym-ns))
                       [:self sym-ns]
                       
                       :else
                       [:link sym-ns]))
               
               (do [:unknown sym-ns]))))))

(defn emit-symbol-standard
  "emits a standard symbol
 
   (emit-symbol-standard 'print! helper/+default+ {:layout :full})
   => \"printf\"
 
   (emit-symbol-standard 'print!
                         {:token {:symbol    {:replace {}}
                                  :string    {:quote :single}}}
                         {:layout :full})
   => \"print!\""
  {:added "3.0"}
  ([sym grammar {:keys [lang layout module] :as mopts}]
   (let [[type ns] (emit-symbol-classify sym mopts)
         {:keys [link-fn] :as opts}  (get-in grammar [:token :symbol])
         dopts (get-in grammar [:default :common])
         module-symbol  (fn [sym]
                          (str (or (get (:internal module)
                                        ns)
                                   (namespace sym))
                               (:namespace dopts)
                               (name sym)))
         link-symbol (fn [sym]
                       (cond (= layout :flat)   (name sym)
                             (= layout :module) (if (= ns (:id module))
                                                  (name sym)
                                                  (module-symbol sym))
                             ;; #_#_
                             :else (module-symbol sym)

                             #_#_
                             :else (h/error "Not Implemented." {:input sym
                                                               :layout layout
                                                                :classify [type ns]})))
         sym  (cond (= type :alias)   (str ns (:namespace dopts) (name sym))

                    (= type :global)  (emit-with-global nil
                                                        [nil (symbol (str ns) (name sym))]
                                                        grammar mopts)
                    
                    (= type :unknown) (if ns
                                        (h/error "Unknown namespace."
                                                 {:lang lang
                                                  :namespace ns
                                                  :input sym
                                                  :layout layout
                                                  :classify [type ns]})
                                        (name sym))
                    
                    link-fn           (*emit-fn* (link-fn sym mopts)
                                                 grammar mopts)

                    (#{:full
                       :host}  layout)  (helper/emit-symbol-full sym ns grammar)
                    
                    (= type :self)     (name sym)
                    
                    (= type :link)     (link-symbol sym))]
     (if (not (and (not= type :unknown)
                   link-fn))
       (apply str (replace (:replace opts) sym))
       sym))))

(defn emit-symbol
  "emits symbol allowing for custom functions"
  {:added "4.0"}
  [sym grammar mopts]
  (let [{:keys [emit-fn]}  (get-in grammar [:token :symbol])]
    (if emit-fn
      (emit-fn sym grammar mopts)
      (emit-symbol-standard sym grammar mopts))))

(defn emit-token
  "customisable emit function for tokens"
  {:added "3.0"}
  ([key token grammar mopts]
   (let [{:keys [emit as custom]} (get-in grammar [:token key])
         custom-fn (case key
                     :string (cond *emit-internal*
                                   identity
                                   
                                   (= :single (get-in grammar [:token :string :quote]))
                                   helper/pr-single
                                   
                                   :else
                                   custom)
                     :symbol (or (if custom
                                   #(custom % grammar mopts))
                                 #(emit-symbol % grammar mopts))
                     custom)
         
         output (cond  custom-fn (custom-fn token)
                       as        (if (or (fn? as)
                                         (var? as))
                                   (as token) as)
                       emit      (emit token grammar mopts)
                       :else     (pr-str token))]
     output)))

(defn emit-with-decorate
  "customisable emit function for global vars"
  {:added "4.0"}
  [_ [_ opts form] grammar mopts]
  (*emit-fn* form grammar mopts))

(defn emit-with-uuid
  "injects uuid for testing"
  {:added "4.0"}
  [_ [_ & [seed0 seed1 :as seeds]] grammar mopts]
  (let [code-fn (fn [x] (cond (or (number? x) (string? x))
                              (h/hash-code x)

                              :else (h/hash-code (h/strn x))))
        uuid  (str (if (not-empty seeds)
                     (java.util.UUID. (code-fn (or seed0 (rand)))
                                      (code-fn (or seed1 (rand))))
                     (java.util.UUID/randomUUID)))]
    (str uuid)))

(defn emit-with-rand
  "injects uuid for testing"
  {:added "4.0"}
  [_ [_ & [type]] grammar mopts]
  (let [token (case type
                :int (rand-int Integer/MAX_VALUE)
                (rand))]
    (*emit-fn* token grammar mopts)))

;;
;; INVOKE
;;

(defn invoke-kw-parse
  "seperates standard and keyword arguments"
  {:added "3.0"}
  ([args]
   (let [idx (h/index-at keyword? args)]
     (if (neg? idx)
       [args []]
       [(take idx args) (partition 2 (drop idx args))]))))

(defn emit-invoke-kw-pair
  "emits a kw argument pair"
  {:added "3.0"}
  ([[k v] grammar mopts]
   (let [{:keys [assign]} (helper/get-options grammar [:default :invoke])]
     (str (str/snake-case (h/strn k)) assign (*emit-fn* v grammar mopts)))))

(defn emit-invoke-args
  "produces the string for invoke call"
  {:added "3.0"}
  ([args grammar mopts]
   (let [[args kwargs] (invoke-kw-parse args)
         cargs (concat (if (not-empty args)
                         (emit-array args grammar mopts))
                       (if (not-empty kwargs)
                         (map #(emit-invoke-kw-pair % grammar mopts) kwargs)))]
     cargs)))

(defn emit-invoke-layout
  "layout for invoke blocks
 
   (emit-invoke-layout [\"ab\\nc\"
                        \"de\\nf\"]
                       helper/+default+ {})
   => \"(ab\\nc,de\\nf)\""
  {:added "4.0"}
  ([str-array grammar mopts]
   (let [{:keys [sep space]} (helper/get-options grammar [:default :invoke])]
     (cond (or (some str/multi-line? str-array)
               (and (not *multiline*)
                    (< (apply + (map count str-array)) *max-len*)))
           (-> (str/join (str sep space) str-array)
               (wrapped-str [:default :invoke] grammar))
           
           :else
           (h/-> (str/join (str sep "\n" (str/spaces 2)
                                )
                           str-array)
                 (str "\n" (str/spaces 2) % "\n")
                 (wrapped-str [:default :invoke] grammar)
                 (str/indent-rest *indent*))))))

(defn emit-invoke-raw
  "invoke call for reserved ops"
  {:added "3.0"}
  ([raw args grammar mopts]
   (let [{:keys [sep space]} (helper/get-options grammar [:default :invoke])
         isep (str sep space)
         cargs (emit-invoke-args args grammar mopts)]
     (str raw  (emit-invoke-layout cargs grammar mopts)))))

(defn emit-invoke-static
  "generates a static call, alternat"
  {:added "3.0"}
  ([[sym & args] grammar mopts]
   (let [sns (namespace sym)
         sfn (name sym)
         {:keys [static]} (helper/get-options grammar [:default :invoke])]
     (emit-invoke-raw (str sns static sfn) args grammar mopts))))

(defn emit-invoke-typecast
  "generates typecast expression"
  {:added "3.0"}
  ([form grammar mopts]
   (assert (<= 2 (count form)) (str "At least two arguments for cast"))
   (let [{:keys [start end]} (helper/get-options grammar [:default :invoke])
         sym   (last form)
         casts (map name (butlast form))]
     (str start start (str/join " " casts) end (*emit-fn* sym grammar mopts) end))))

(defn emit-invoke
  "general invoke call, incorporating keywords"
  {:added "3.0"}
  ([_ [sym & args :as form] grammar mopts]
   (let [{:keys [custom keyword-fn]} (get-in grammar [:default :invoke])]
     (cond custom
           (custom form grammar mopts)

           (keyword? sym)
           (cond keyword-fn
                 (keyword-fn form grammar mopts)
                 
                 (namespace sym)
                 (emit-invoke-static form grammar mopts)
                 
                 :else
                 (emit-invoke-typecast form grammar mopts))
           
           :else
           (emit-invoke-raw (emit-wrapping sym grammar mopts) args grammar mopts)))))

;;
;; New Constructor Call
;;

(defn emit-new
  "invokes a constructor"
  {:added "3.0"}
  ([raw [sym & more :as args] grammar mopts]
   (assert (<= 1 (count args)) (str "One or more arguments for " raw))
   (let [sym (if (keyword? sym)
               (name sym)
               sym)]
     (str raw " " (emit-invoke raw (cons sym more) grammar mopts)))))

(defn emit-class-static-invoke
  "creates"
  {:added "4.0"}
  ([raw [sym field & args] grammar mopts]
   (let [{:keys [static]} (helper/get-options grammar [:default :invoke])]
     (cond (keyword? field)
           (str sym (or raw static) (name field))
           
           :else
           (emit-invoke-raw (str sym (or raw static) field) args grammar mopts)))))

;;
;; INDEX
;;

(defn emit-index-entry
  "classifies the index entry"
  {:added "3.0"}
  ([v grammar mopts]
   (cond (symbol? v)
         (do (assert (nil? (namespace v)) "No namespace")
             (str "." (emit-symbol v grammar mopts)))

         (h/form? v)
         (let [sym  (first v)
               sym  (if (string? sym)
                      (symbol sym)
                      sym)
               _     (assert (symbol? sym))
               {:keys [apply]}   (helper/get-options grammar [:default :invoke])
               braces (meta sym)]
           (emit-invoke-raw (str apply
                                 (emit-symbol sym grammar mopts)
                                 (if (not-empty braces)
                                   (*emit-fn* braces grammar mopts)
                                   ""))
                            (rest v) grammar mopts))

         (vector? v)
         (do (assert (= 1 (count v)) "Only one index")
             (let [{:keys [start end]}   (helper/get-options grammar [:default :index])]
               (str start (*emit-fn* (first v) grammar mopts) end)))


         (set? v)
         (str "." (*emit-fn* v grammar mopts))
         
         :else (h/error "Not valid" {:input v}))))

(defn emit-index
  "creates an indexed expression"
  {:added "3.0"}
  ([raw args grammar mopts]
   (assert (<= 2 (count args)) (str "Two or more arguments for raw" raw))
   (let [[sym & more] args
         index (mapv #(emit-index-entry % grammar mopts) more)
         sym  (emit-wrapping sym grammar mopts)]
     (apply str sym index))))

;; Ops
;;

(defn emit-op
  "helper for the emit op"
  {:added "3.0"}
  ([key [sym & args :as form] {:keys [reserved] :as grammar} mopts & [expansion]]
   (let [{:keys [op emit transform raw default type] :as props} (get reserved sym)]
     (case emit
       
       ;; WHITESPACE
       :discard       ""
       :free          (emit-free props form grammar mopts)
       :squash        (emit-squash key form grammar mopts)
       :comment       (emit-comment key form grammar mopts)
       :indent        (emit-indent key form grammar mopts)
       
       ;; FORMS
       :token         raw
       :alias         (*emit-fn* (cons raw args) grammar mopts)
       :unit          (emit-unit props form grammar mopts)
       :internal      (emit-internal form grammar mopts)
       :internal-str  (emit-internal-str form grammar mopts)
       :pre           (emit-pre raw args grammar mopts)
       :post          (emit-post raw args grammar mopts)
       :prefix        (emit-prefix raw args grammar mopts)
       :postfix       (emit-postfix raw args grammar mopts)
       :infix         (emit-infix raw args grammar mopts)
       :infix-        (emit-infix-pre raw args grammar mopts)
       :infix*        (emit-infix-default raw args default grammar mopts)
       :infix-if      (emit-infix-if form grammar mopts)
       :bi            (emit-bi raw args grammar mopts)
       :between       (emit-between raw args grammar mopts)
       :assign        (emit-assign raw args grammar mopts)
       :invoke        (emit-invoke-raw raw args grammar mopts)
       :new           (emit-new raw args grammar mopts)
       :static-invoke (emit-class-static-invoke raw args grammar mopts)
       :index         (emit-index raw args grammar mopts)
       :return        (emit-return raw args grammar mopts)
       :macro         (emit-macro key form grammar mopts)
       :template      (emit-macro key form grammar mopts)
       
       ;; LANGUAGE SPECIFIC GLOBAL
       :with-global   (emit-with-global key form grammar mopts)
       
       ;; LANGUAGE SPECIFIC DECORATIONS
       :with-decorate (emit-with-decorate key form grammar mopts)

       ;; LANGUAGE HELPERS
       :with-uuid     (emit-with-uuid key form grammar mopts)
       :with-rand     (emit-with-rand key form grammar mopts)
       
       ;; RESTRICTED SYMBOLS
       :throw         (h/error "Not allowed as op" {:symbol sym :form form})

       ;; HANDLE ERROR
       (if-let [f (get expansion emit)]
         (f key props form grammar mopts)
         (h/error "No matching clause"
                  {:emit emit
                   :key key
                   :symbol sym
                   :form form
                   :props props}))))))


(defn form-key
  "returns the key associated with the form"
  {:added "3.0"}
  ([form {:keys [banned reserved] :as grammar}]
   (let [key (helper/form-key-base form)
         check-fn  (fn [val input]
                     (if-let [m (get reserved val)]
                       [(:op m) (or (:type m) :statement)]
                       input))
         [key type check?]  (cond (vector? key) key
                                 
                                 :else
                                 (check-fn (first form)
                                           [:invoke :invoke]))
         _ (if (get banned key)
             (h/error "Disallowed in form" {:form form :key key :type type}))]
     [key type check?])))

;;
;;
;;

(def +emit-lookup+
  {:token    #'emit-token
   :invoke   #'emit-invoke
   :macro    #'emit-macro})

(defn emit-common-loop
  "emits the raw string"
  {:added "4.0"}
  ([form grammar mopts]
   (emit-common-loop form
                     grammar
                     mopts
                     +emit-lookup+
                     emit-op))
  ([form grammar mopts fn-lookup fn-default]
   (let [output-fn (fn [[key type]]
                     (let [emit-fn (or (get fn-lookup type)
                                       fn-default)]
                       (emit-fn key form grammar mopts)))
         [key type check? :as input] (form-key form grammar)]
     (cond check?
           (or (emit-reserved-value form grammar mopts)
               (output-fn input))

           :else
           (output-fn input)))))

(defn emit-common
  "emits a string based on grammar"
  {:added "4.0"}
  [form grammar mopts]
  (binding [*emit-fn* emit-common-loop]
    (emit-common-loop form grammar mopts)))


(comment
  
  (./import)
  (./create-tests))
