(ns std.lang.base.preprocess-input
  (:require [clojure.string]
            [std.lang.base.preprocess :as preprocess]
            [std.lang.base.util :as ut]
            [std.lib.collection :as collection]
            [std.lib.context.pointer :as ptr]
            [std.lib.foundation :as f]
            [std.lib.walk :as walk]))

(defn to-input-form
  "processes a form"
  {:added "4.0"}
  [[tag input & more :as x]]
  (cond (collection/form? tag)
        (cond (= 'clojure.core/deref (first tag))
              (let [tok (second tag)]
                (cond (= tok '! )
                      (list '!:template input)

                      (and (symbol? tok)
                           (clojure.string/starts-with? (str tok) "."))
                      (apply list '!:lang {:lang (keyword (subs (str tok) 1))}
                             input more)

                      (and (collection/form? tok)
                           (= 'var (first tok)))
                      (list '!:eval
                            (apply list
                                   (list 'var (or (f/var-sym (resolve (second tok)))
                                                  (f/error "Var not found" {:input (second tok)})))
                                   (if input
                                     (cons input more)
                                     more)))

                      :else
                      (apply list '!:decorate (apply vec (rest tag))
                             input more)))

              (= 'clojure.core/unquote (first tag))
              (f/error "Not supported" {:input x}))

        (= 'clojure.core/deref tag)
        (if (and (collection/form? input)
                 (= 'var (first input)))
          (list '!:deref (list 'var (or (f/var-sym (resolve (second input)))
                                        (f/error "Var not found" {:input (second input)}))))
          (list '!:eval input))))

(defn to-input
  "converts a form to input (extracting deref forms)"
  {:added "4.0"}
  [raw]
  (let [check-fn (fn [child]
                   (and (collection/form? child)
                        (= (first child) '(clojure.core/unquote !))))]
    (walk/prewalk (fn [x]
                    (or (cond (ptr/pointer? x)
                              (ut/sym-full x)

                              (and preprocess/*macro-splice*
                                   (or (vector? x)
                                       (collection/form? x))
                                   (some check-fn x))
                              (-> (into (empty x)
                                        (reduce (fn [acc child]
                                                  (if (check-fn child)
                                                    (apply conj acc (eval (second child)))
                                                    (conj acc child)))
                                                (empty x)
                                                x))
                                  (with-meta (meta x)))

                              (collection/form? x)
                              (to-input-form x))
                        x))
                  raw)))
