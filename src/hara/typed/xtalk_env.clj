(ns hara.typed.xtalk-env
  (:require [hara.typed.xtalk-common :as types]
            [hara.typed.xtalk-compat :as compat]
            [hara.typed.xtalk-ops :as ops]
            [hara.typed.xtalk-parse :as parse]))

(defn maybe-register-function!
  [resolved-sym]
  (let [ns-sym (some-> resolved-sym namespace symbol)]
    (when ns-sym
      (when-not (types/get-function resolved-sym)
        (try
          (-> ns-sym
              parse/analyze-namespace
              parse/register-types!)
          (catch clojure.lang.ExceptionInfo ex
            nil)))
      (types/get-function resolved-sym))))

(defn lookup-symbol-type
  [sym {:keys [env] :as ctx}]
  (or (get env sym)
      (some-> sym
              (compat/resolve-local-symbol ctx)
              ops/builtin-type)
      (let [resolved (compat/resolve-local-symbol sym ctx)]
        (some-> (or (types/get-function resolved)
                    (maybe-register-function! resolved))
                types/fn-type))
      types/+unknown-type+))

(defn binding-decl
  [target ctx]
  (cond
    (symbol? target)
    {:symbol target
     :type (some-> target meta :- (types/normalize-return-meta ctx))}

    (and (seq? target)
         (= 2 (count target))
         (symbol? (second target)))
    {:symbol (second target)
     :type (types/normalize-type (first target) ctx)}

     :else
     nil))

(defn dynamic-assignment-target?
  [target]
  (and (seq? target)
       (symbol? (first target))
       (contains? #{'. 'x:get-key 'x:get-path 'x:get-idx}
                  (first target))))

(defn map-binding-updates
  [target expr-type ctx]
  (let [resolved (compat/resolve-type expr-type ctx)
        key-syms (:keys target)
        str-syms (:strs target)
        sym-syms (:syms target)]
    (when (or (seq key-syms)
              (seq str-syms)
              (seq sym-syms))
      (into {}
            (concat
             (map (fn [sym]
                    [sym (compat/field-access-type resolved (types/field-key sym) ctx)])
                  key-syms)
             (map (fn [sym]
                    [sym (compat/field-access-type resolved (name sym) ctx)])
                  str-syms)
             (map (fn [sym]
                    [sym (compat/field-access-type resolved sym ctx)])
                  sym-syms))))))

(defn binding-updates
  [target expr-type ctx]
  (cond
    (symbol? target)
    {target expr-type}

    (and (vector? target)
         (every? symbol? target))
    (let [resolved (compat/resolve-type expr-type ctx)
          item-type (cond
                      (= :array (:kind resolved))
                      (:item resolved)

                      (= :tuple (:kind resolved))
                      nil

                      :else
                      types/+unknown-type+)]
      (if (= :tuple (:kind resolved))
        (into {}
              (map-indexed (fn [idx sym]
                             [sym (or (nth (:types resolved) idx nil)
                                      types/+unknown-type+)]))
              target)
        (into {}
              (map (fn [sym]
                     [sym item-type]))
              target)))

    (and (set? target)
         (every? symbol? target))
    (let [resolved (compat/resolve-type expr-type ctx)]
      (into {}
            (map (fn [sym]
                   [sym (compat/field-access-type resolved (types/field-key sym) ctx)]))
            target))

    (map? target)
    (map-binding-updates target expr-type ctx)

    :else
    nil))
