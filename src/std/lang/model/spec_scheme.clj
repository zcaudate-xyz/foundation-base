(ns std.lang.model.spec-scheme
  (:require [clojure.string :as str]
            [std.lang.base.book :as book]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.script :as script]
            [std.lang.model.spec-xtalk :as xtalk]
            [std.lang.model.spec-xtalk.fn-scheme :as fn]
            [std.lib.collection :as collection]))

(def +features+
  (-> (grammar/build-min [:coroutine
                          :xtalk])
      (merge (grammar/build-xtalk))
      (grammar/build:override fn/+scheme+)))

(def +reserved+
  (grammar/to-reserved +features+))

(declare scheme-expand)

(defn scheme-expand
  [form]
  (cond (collection/form? form)
        (let [form     (apply list (map scheme-expand form))
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
        (mapv scheme-expand form)

        (map? form)
        (into (empty form)
              (map (fn [[k v]]
                     [(scheme-expand k) (scheme-expand v)]))
              form)

        (set? form)
        (set (map scheme-expand form))

        :else
        form))

(declare scheme-transform)

(defn scheme-transform-bindings
  [bindings]
  (->> (partition 2 bindings)
       (mapv (fn [[sym value]]
               (list sym (scheme-transform value))))))

(defn scheme-transform-fn
  [[_ args & body]]
  (list* 'lambda
         (apply list args)
         (map scheme-transform body)))

(defn scheme-transform-defn
  [[_ sym args & body]]
  (list* 'define
         (list* sym args)
         (map scheme-transform body)))

(defn scheme-transform-let
  [[_ bindings & body]]
  (list* 'let
         (scheme-transform-bindings bindings)
         (map scheme-transform body)))

(defn scheme-transform
  [form]
  (cond (collection/form? form)
        (let [[op & args] form]
          (case op
            return (if (= 1 (count args))
                     (scheme-transform (first args))
                     (list* 'begin (map scheme-transform args)))
            do     (list* 'begin (map scheme-transform args))
            fn     (scheme-transform-fn form)
            fn:>   (scheme-transform-fn form)
            defn   (scheme-transform-defn form)
            defgen (scheme-transform-defn form)
            let    (scheme-transform-let form)
            not=   (list 'not (list 'equal?
                                    (scheme-transform (first args))
                                    (scheme-transform (second args))))
            ==     (list 'equal?
                         (scheme-transform (first args))
                         (scheme-transform (second args)))
            nil?   (list 'null? (scheme-transform (first args)))
            (apply list
                   (map scheme-transform
                        (cons op args)))))

        (vector? form)
        (mapv scheme-transform form)

        (map? form)
        (into (empty form)
              (map (fn [[k v]]
                     [(scheme-transform k) (scheme-transform v)]))
              form)

        (set? form)
        (set (map scheme-transform form))

        :else
        form))

(declare emit-scheme-form)

(defn emit-scheme-coll
  [start end coll]
  (str start
       (str/join " " (map emit-scheme-form coll))
       end))

(defn emit-scheme-map
  [m]
  (str "(hash"
       (if (seq m)
         (str " "
              (str/join " "
                        (mapcat (fn [[k v]]
                                  [(emit-scheme-form k)
                                   (emit-scheme-form v)])
                                m)))
         "")
       ")"))

(defn emit-scheme-form
  [form]
  (cond (nil? form)      "'()"
        (true? form)     "#t"
        (false? form)    "#f"
        (string? form)   (pr-str form)
        (keyword? form)  (pr-str (name form))
        (number? form)   (str form)
        (symbol? form)   (str form)
        (map? form)      (emit-scheme-map form)
        (vector? form)   (emit-scheme-coll "#(" ")" form)
        (set? form)      (emit-scheme-coll "(set" ")" form)
        (collection/form? form)
        (emit-scheme-coll "(" ")" form)
        :else
        (pr-str form)))

(defn emit-scheme
  "emits code into scheme schema"
  {:added "4.0"}
  [form mopts]
  (-> (if (and (vector? form)
               (every? collection/form? form))
        (if (= 1 (count form))
          (first form)
          (cons 'begin form))
        form)
      (scheme-expand)
      (scheme-transform)
      (emit-scheme-form)))

(def +grammar+
  (grammar/grammar :scm
    +reserved+
    {:emit #'emit-scheme}))

(def +meta+ (book/book-meta {}))

(def +book+
  (book/book {:lang :scheme
              :parent :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
