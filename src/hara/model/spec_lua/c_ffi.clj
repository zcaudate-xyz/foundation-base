(ns hara.model.spec-lua.c-ffi
  (:require [std.lib.collection :as collection]))

(defn c-ffi-type
  "coerces a C type annotation to a string"
  {:added "4.1"}
  [t]
  (cond (keyword? t) (name t)
        (symbol? t) (name t)
        (vector? t) (clojure.string/join " " (map c-ffi-type t))
        :else (str t)))

(defn c-ffi-sym
  "sanitizes a symbol for use in C"
  {:added "4.1"}
  [s]
  (-> (str s)
      (clojure.string/replace "-" "_")
      (clojure.string/replace "." "_")
      (clojure.string/replace "/" "____")))

(defn c-ffi-form?
  "identifies C FFI blocks that should not be rewritten by the Lua
   lambda hoisting stage.  Both the `%.c` grammar macro and the
   `@.c` reader shorthand (which becomes `!:lang {:lang :c}` at the
   input stage) are recognised."
  {:added "4.1"}
  [form]
  (and (collection/form? form)
       (or (= '%.c (first form))
           (and (= 'clojure.core/deref (first form))
                (= '.c (second form)))
           (and (= '!:lang (first form))
                (= :c (:lang (second form)))))))

(defn c-ffi-fn-form?
  "checks if a form is a C FFI function declaration"
  {:added "4.1"}
  [form]
  (and (collection/form? form)
       (or (= 'fn (first form))
           (= 'fn:> (first form)))))

(defn c-ffi-forms
  "collects fn/fn:> forms, descending into nested do blocks"
  {:added "4.1"}
  [forms]
  (mapcat (fn [form]
            (cond (c-ffi-fn-form? form)
                  [form]

                  (and (collection/form? form)
                       (= 'do (first form)))
                  (c-ffi-forms (rest form))

                  :else
                  []))
          forms))

(defn c-ffi-args
  "emits C argument list from a fn args vector"
  {:added "4.1"}
  [args]
  (let [args (vec args)
        typed? (and (pos? (count args))
                    (not (symbol? (first args))))]
    (if typed?
      (map (fn [[type name]]
             (str (c-ffi-type type) " " (c-ffi-sym name)))
           (partition 2 args))
      (map (fn [name]
             (let [type (or (:tag (meta name))
                            (:- (meta name))
                            "void*")]
               (str (c-ffi-type type) " " (c-ffi-sym name))))
           args))))

(defn c-ffi-decl
  "emits a single C forward declaration from a fn/fn:> form"
  {:added "4.1"}
  [form]
  (let [[_ name args & _] form
        ret-type (or (:tag (meta name))
                     (:- (meta name))
                     "void")]
    (str (c-ffi-type ret-type)
         " "
         (c-ffi-sym name)
         "("
         (clojure.string/join ", " (c-ffi-args args))
         ");")))

(defn c-ffi-body->string
  "emits C forward declarations from a collection of fn/fn:> forms"
  {:added "4.1"}
  [forms]
  (clojure.string/join "\n" (map c-ffi-decl (c-ffi-forms forms))))
