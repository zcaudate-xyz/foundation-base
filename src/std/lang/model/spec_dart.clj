(ns std.lang.model.spec-dart
  (:require [std.lang.base.book :as book]
            [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-data :as data]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.script :as script]
            [std.lang.model.spec-xtalk]
            [std.lang.model.spec-xtalk.fn-dart :as fn-dart]
            [std.lib.collection :as collection]))

(defn dart-map-key
  [key grammar mopts]
  (cond
    (or (keyword? key)
        (string? key)
        (symbol? key)
        (number? key)
        (boolean? key)
        (nil? key))
    (data/default-map-key key grammar mopts)

    :else
    (str "(" (common/*emit-fn* key grammar mopts) ")")))

(def +features+
  (-> (grammar/build :exclude [:pointer
                               :block
                               :data-set])
      (grammar/build:override
       {:var        {:symbol '#{var} :raw "var"}
        :defn       {:symbol '#{defn}}
        :new        {:symbol '#{new} :raw "new" :emit :call}
        :with-global {:value true :raw "globalThis"}})
      (grammar/build:override fn-dart/+dart+)))

(def +template+
  (-> (emit/default-grammar)
      (collection/merge-nested
       {:banned #{:set :regex}
        :highlight '#{return break continue}
        :default {:common    {:statement ""}
                  :function  {:prefix ""
                              :raw ""
                              :args {:sep ", "}}
                  :invoke    {:reversed true :hint ""}
                  :block     {:start " {" :end "}"}}
        :token   {:symbol {:replace {\- "_"}}
                  :nil {:as "null"}}
        :data    {:vector {:start "[" :end "]" :space ""}
                  :map    {:space "" :key-fn dart-map-key}}})))

(def +grammar+
  (grammar/grammar :dart
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta
   {:module-current   (fn [])
    :module-import    (fn [name _ _]
                        (list :- "import" (str "'" name "';")))}))

(def +book+
  (book/book {:lang :dart
              :parent :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
