(ns std.lang.base.emit-preprocess)

(def ^:dynamic *macro-form* nil)

(def ^:dynamic *macro-grammar* nil)

(def ^:dynamic *macro-opts* nil)

(def ^:dynamic *macro-splice* nil)

(def ^:dynamic *macro-skip-deps* nil)

(defn macro-form
  "gets the current macro form"
  {:added "4.0"}
  []
  *macro-form*)

(defn macro-opts
  "gets current macro-opts"
  {:added "4.0"}
  []
  *macro-opts*)

(defn macro-grammar
  "gets the current grammar"
  {:added "4.0"}
  []
  *macro-grammar*)

(defmacro ^{:style/indent 1}
  with:macro-opts
  "bind macro opts"
  {:added "4.0"}
  [[mopts] & body]
  `(binding [*macro-opts* ~mopts]
     ~@body))

(defn to-input-form
  {:added "4.0"}
  [& args]
  (apply (requiring-resolve 'std.lang.base.preprocess-input/to-input-form)
         args))

(defn to-input
  {:added "4.0"}
  [& args]
  (apply (requiring-resolve 'std.lang.base.preprocess-input/to-input)
         args))

(defn get-fragment
  {:added "4.0"}
  [& args]
  (apply (requiring-resolve 'std.lang.base.preprocess-resolve/get-fragment)
         args))

(defn value-template-args
  {:added "4.1"}
  [& args]
  (apply (requiring-resolve 'std.lang.base.preprocess-value/value-template-args)
         args))

(defn value-standalone
  {:added "4.1"}
  [& args]
  (apply (requiring-resolve 'std.lang.base.preprocess-value/value-standalone)
         args))

(defn process-namespaced-resolve
  {:added "4.0"}
  [& args]
  (apply (requiring-resolve 'std.lang.base.preprocess-resolve/process-namespaced-resolve)
         args))

(defn process-namespaced-symbol
  {:added "4.0"}
  [& args]
  (apply (requiring-resolve 'std.lang.base.preprocess-resolve/process-namespaced-symbol)
         args))

(defn process-inline-assignment
  {:added "4.0"}
  [& args]
  (apply (requiring-resolve 'std.lang.base.preprocess-assign/process-inline-assignment)
         args))

(defn protect-reserved-head
  {:added "4.1"}
  [& args]
  (apply (requiring-resolve 'std.lang.base.preprocess-assign/protect-reserved-head)
         args))

(defn to-staging-form
  {:added "4.0"}
  [& args]
  (apply (requiring-resolve 'std.lang.base.preprocess-staging/to-staging-form)
         args))

(defn process-standard-symbol
  {:added "4.0"}
  [& args]
  (apply (requiring-resolve 'std.lang.base.preprocess-resolve/process-standard-symbol)
         args))

(defn to-staging
  {:added "4.0"}
  [& args]
  (apply (requiring-resolve 'std.lang.base.preprocess-staging/to-staging)
         args))

(defn to-resolve
  {:added "4.0"}
  [& args]
  (apply (requiring-resolve 'std.lang.base.preprocess-staging/to-resolve)
         args))

(defn find-natives
  {:added "4.0"}
  [& args]
  (apply (requiring-resolve 'std.lang.base.preprocess-resolve/find-natives)
         args))
