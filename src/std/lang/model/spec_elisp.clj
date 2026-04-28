(ns std.lang.model.spec-elisp
  (:require [clojure.string :as str]
            [std.lang.base.book :as book]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.script :as script]
            [std.lang.model.spec-xtalk.fn-elisp :as fn]
            [std.lib.collection :as collection]))

(def +features+
  (-> (grammar/build-min [:coroutine
                          :xtalk])
      (merge (grammar/build-xtalk))
      (grammar/build:override fn/+elisp+)))

(def +reserved+
  (grammar/to-reserved +features+))

(declare elisp-expand)

(defn elisp-expand
  [form]
  (cond (collection/form? form)
        (let [form     (apply list (map elisp-expand form))
              op       (first form)
              reserved (and (symbol? op)
                            (str/starts-with? (name op) "x:")
                            (get +reserved+ op))]
          (cond (and reserved
                     (= :macro (:emit reserved))
                     (:macro reserved))
                (recur ((:macro reserved) form))

                (and reserved
                     (= :hard-link (:emit reserved))
                     (:raw reserved))
                (recur (cons (:raw reserved) (rest form)))

                :else
                form))

        (vector? form)
        (mapv elisp-expand form)

        (map? form)
        (into (empty form)
              (map (fn [[k v]]
                     [(elisp-expand k) (elisp-expand v)]))
              form)

        (set? form)
        (set (map elisp-expand form))

        :else
        form))

(declare elisp-transform)

(defn elisp-transform-bindings
  [bindings]
  (->> (partition 2 bindings)
       (mapv (fn [[sym value]]
               (list sym (elisp-transform value))))))

(defn elisp-transform-fn
  [[_ args & body]]
  (list* 'lambda
         (apply list args)
         (map elisp-transform body)))

(defn elisp-transform-defn
  [[_ sym args & body]]
  (list* 'defun
         sym
         (apply list args)
         (map elisp-transform body)))

(defn elisp-transform-let
  [[_ bindings & body]]
  (list* 'let
         (elisp-transform-bindings bindings)
         (map elisp-transform body)))

(defn elisp-transform
  [form]
  (cond (collection/form? form)
        (let [[op & args] form]
          (case op
            return (if (= 1 (count args))
                     (elisp-transform (first args))
                     (list* 'progn (map elisp-transform args)))
            do     (list* 'progn (map elisp-transform args))
            fn     (elisp-transform-fn form)
            fn:>   (elisp-transform-fn form)
            defn   (elisp-transform-defn form)
            defgen (elisp-transform-defn form)
            let    (elisp-transform-let form)
            not=   (if (= 2 (count args))
                     (list 'not (list 'equal
                                      (elisp-transform (first args))
                                      (elisp-transform (second args))))
                     (apply list
                            (map elisp-transform
                                 (cons op args))))
            ==     (list 'equal
                         (elisp-transform (first args))
                         (elisp-transform (second args)))
            nil?   (list 'null (elisp-transform (first args)))
            (apply list
                   (map elisp-transform
                        (cons op args)))))

        (vector? form)
        (mapv elisp-transform form)

        (map? form)
        (into (empty form)
              (map (fn [[k v]]
                     [(elisp-transform k) (elisp-transform v)]))
              form)

        (set? form)
        (set (map elisp-transform form))

        :else
        form))

(declare emit-elisp-form)

(defn emit-elisp-coll
  [start end coll]
  (str start
       (str/join " " (map emit-elisp-form coll))
       end))

(defn emit-elisp-map
  [m]
  (let [table '__xt_tbl]
    (emit-elisp-form
     (list 'let
           (list (list table (list 'make-hash-table :test (list 'quote 'equal))))
           (cons 'progn
                 (concat
                  (map (fn [[k v]]
                         (list 'puthash
                               (if (keyword? k) (name k) k)
                               v
                               table))
                       m)
                  [table]))))))

(defn emit-elisp-form
  [form]
  (cond (nil? form)      "nil"
        (true? form)     "t"
        (false? form)    "nil"
        (string? form)   (pr-str form)
        (keyword? form)  (str form)
        (number? form)   (str form)
        (symbol? form)   (str form)
        (map? form)      (emit-elisp-map form)
        (vector? form)   (emit-elisp-coll "[" "]" form)
        (set? form)      (emit-elisp-coll "(list " ")" form)
        (collection/form? form)
        (emit-elisp-coll "(" ")" form)
        :else
        (pr-str form)))

(defn emit-elisp
  "emits code into emacs lisp schema"
  {:added "4.1"}
  [form mopts]
  (-> (if (and (vector? form)
               (every? collection/form? form))
        (if (= 1 (count form))
          (first form)
          (cons 'progn form))
        form)
      (elisp-expand)
      (elisp-transform)
      (emit-elisp-form)))

(def +grammar+
  (grammar/grammar :el
    +reserved+
    {:emit #'emit-elisp}))

(def +meta+ (book/book-meta {}))

(def +book+
  (book/book {:lang :elisp
              :parent :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
