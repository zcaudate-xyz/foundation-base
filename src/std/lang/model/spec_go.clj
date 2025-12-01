(ns std.lang.model.spec-go
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.emit-fn :as fn]
            [std.lang.base.emit-data :as data]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.string :as str]
            [std.lib :as h]))

(defn go-typesystem
  "handle generic types"
  {:added "4.0"}
  [arr grammar mopts]
  (let [[_ type & args] arr]
     (cond (= 'slice type)
           (str "[]" (emit/emit-main (first args) grammar mopts))

           (= 'map type)
           (str "map[" (emit/emit-main (first args) grammar mopts) "]"
                (emit/emit-main (second args) grammar mopts))

           :else
           (str (emit/emit-main type grammar mopts)
                "[" (str/join ", " (map #(emit/emit-main % grammar mopts) args)) "]"))))

(defn go-vector
  "emit vector or slice"
  {:added "4.0"}
  ([arr grammar mopts]
   (let [sym (first arr)]
     (cond (= :> sym)
           (go-typesystem arr grammar mopts)

           :else
           (str "[]interface{}{"
                (str/join ", " (map #(emit/emit-main % grammar mopts) arr))
                "}")))))

(defn tf-go-arrow
  "macro for channel op"
  {:added "4.0"}
  [[_ & args]]
  (if (= 1 (count args))
    (list :% (list :- "<-") (first args))
    (list :% (first args) (list :- " <- ") (second args))))

(defn go-defstruct
  "defstruct implementation"
  {:added "4.0"}
  [[_ sym fields] grammar mopts]
  (str "type " (emit/emit-main sym grammar mopts) " struct {\n"
       (str/join "\n"
                 (map (fn [field]
                        (if (vector? field)
                          (let [[n t & tags] field
                                tag-str (if (seq tags)
                                          (str " `" (str/join " " tags) "`")
                                          "")]
                            (str "  " (emit/emit-main n grammar mopts) " "
                                 (emit/emit-main t grammar mopts)
                                 tag-str))
                          (str "  " (emit/emit-main field grammar mopts))))
                      fields))
       "\n}"))

(defn go-definterface
  "definterface implementation"
  {:added "4.0"}
  [[_ sym methods] grammar mopts]
  (str "type " (emit/emit-main sym grammar mopts) " interface {\n"
       (str/join "\n"
                 (map (fn [method]
                         (str "  " (emit/emit-main method grammar mopts)))
                      methods))
       "\n}"))

(def +features+
  (-> (grammar/build)
      (grammar/build:override
       {:var        {:symbol '#{var} :raw "var"}
        :new        {:symbol '#{new} :raw "new" :emit :call}})
      (grammar/build:extend
       {:go-chan    {:op :go-chan    :symbol '#{chan} :raw "chan " :emit :pre}
        :go-arrow   {:op :go-arrow   :symbol '#{<-}   :macro #'tf-go-arrow :emit :macro :type :macro}
        :go         {:op :go         :symbol '#{go}   :raw "go " :emit :pre}
        :defer      {:op :defer      :symbol '#{defer} :raw "defer " :emit :pre}
        :make       {:op :make       :symbol '#{make} :raw "make" :emit :invoke}

        :defstruct  {:op :defstruct :symbol '#{defstruct}
                     :type :def :section :code
                     :emit #'go-defstruct}
        :definterface {:op :definterface :symbol '#{definterface}
                       :type :def :section :code
                       :emit #'go-definterface}})))

(def +template+
  (-> (emit/default-grammar)
      (h/merge-nested
       {:banned #{:set :regex}
        :highlight '#{return break continue fallthrough}
        :default {:common    {:statement ""}
                  :function  {:prefix "func"
                              :raw ""
                              :args {:sep ", "}}
                  :typehint  {:enabled true :assign "" :space " " :after true}
                  :invoke    {:reversed true :hint ""}
                  :block     {:start " {" :end "}"}}
        :token   {:symbol {:replace {\- "_"}}}
        :data    {:vector {:custom #'go-vector}
                  :map    {:start "map[string]interface{}{" :end "}" :space ""}}})))

(def +grammar+
  (grammar/grammar :go
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta
   {:module-current   (fn [])
    :module-import    (fn [name _ opts]
                        (list :- "import" (str "\"" name "\"")))}))

(def +book+
  (book/book {:lang :go
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
