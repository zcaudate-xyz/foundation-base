(ns hara.model.spec-haxe
    (:require [hara.lang.book :as book]
              [hara.common.emit :as emit]
              [hara.common.emit-common :as common]
              [hara.common.emit-data :as data]
              [hara.common.grammar :as grammar]
              [hara.lang.script :as script]
              [hara.model.spec-xtalk]
              [std.lib.collection :as collection]
              [std.lib.template :as template]))

;;;
;;; CORE FUNCTION TRANSFORMS
;;;

(defn haxe-tf-x-del
      "Removes a field from a Dynamic object."
      [[_ obj key]]
      (template/$ (Reflect.deleteField ~obj ~(name key))))

(defn haxe-tf-x-del-key
      "Removes a key from a Map."
      [[_ obj key]]
      (template/$ (. ~obj (remove ~key))))

(defn haxe-tf-x-cat
      "String concatenation."
      [[_ & args]]
      (apply list '+ args))

(defn haxe-tf-x-len
      "Array/Map length."
      [[_ arr]]
      (list '. arr 'length))

(defn haxe-tf-x-get-key
      "Map/object key access."
      [[_ obj key default]]
      (let [val (template/$ (. ~obj (get ~key)))]
           (if default
               (list 'or val default)
               val)))

(defn haxe-tf-x-has-key?
      "Map key existence."
      [[_ obj key check]]
      (if check
          (list '== check (template/$ (. ~obj (get ~(name key)))))
          (template/$ (. ~obj (exists ~key)))))

(defn haxe-tf-x-err
      "Throws an exception."
      [[_ msg]]
      (list 'throw (list 'new 'Exception msg)))

(defn haxe-tf-x-ex-native?
      [[_ err]]
      (list 'Std.isOfType err 'Exception))

(defn haxe-tf-x-ex-new
      [[_ message & [data]]]
      (if data
          (list 'new 'Exception message data)
          (list 'new 'Exception message)))

(defn haxe-tf-x-ex-message
      [[_ err]]
      (template/$ (. ~err message)))

(defn haxe-tf-x-ex-data
      [[_ err]]
      (template/$ (. ~err data)))

(defn haxe-tf-x-eval
      "No runtime eval in Haxe. Throws."
      [[_ s]]
      (list 'throw (list 'new 'Exception (list '+ "eval not supported: " s))))

(defn haxe-tf-x-apply
      [[_ f args]]
      (list f (list :* args)))

(defn haxe-tf-x-random
      [_]
      '(Math.random))

(defn haxe-tf-x-print
      ([[_ & args]]
       (apply list 'trace args)))

(defn haxe-tf-x-type-native
      [[_ obj]]
      (template/$
       (cond (Std.isOfType ~obj Array)
             (return "array")
             (Std.isOfType ~obj haxe.ds.StringMap)
             (return "object")
             (Std.isOfType ~obj haxe.ds.IntMap)
             (return "object")
             (Reflect.isFunction ~obj)
             (return "function")
             (== bool (type ~obj))
             (return "boolean")
             (Std.isOfType ~obj Int)
             (return "number")
             (Std.isOfType ~obj Float)
             (return "number")
             (Std.isOfType ~obj String)
             (return "string")
             (== null ~obj)
             (return "nil")
             :else
             (return (str (type ~obj))))))

(defn haxe-tf-x-to-string
      [[_ e]]
      (list 'Std.string e))

(defn haxe-tf-x-to-number
      [[_ e]]
      (list 'Std.parseFloat e))

(defn haxe-tf-x-is-string?
      [[_ e]]
      (list 'Std.isOfType e 'String))

(defn haxe-tf-x-is-number?
      [[_ e]]
      (list 'or (list 'Std.isOfType e 'Int) (list 'Std.isOfType e 'Float)))

(defn haxe-tf-x-is-integer?
      [[_ e]]
      (list 'Std.isOfType e 'Int))

(defn haxe-tf-x-is-boolean?
      [[_ e]]
      (list '== 'Bool (list 'type e)))

(defn haxe-tf-x-is-function?
      [[_ e]]
      (list 'Reflect.isFunction e))

(defn haxe-tf-x-is-object?
      [[_ e]]
      (list 'or (list 'Std.isOfType e 'haxe.ds.StringMap) (list 'Std.isOfType e 'haxe.ds.IntMap)))

(defn haxe-tf-x-is-array?
      [[_ e]]
      (list 'Std.isOfType e 'Array))

(defn haxe-tf-x-json-encode
      [[_ obj]]
      (template/$ (haxe.Json.stringify ~obj)))

(defn haxe-tf-x-json-decode
      [[_ s]]
      (template/$ (haxe.Json.parse ~s)))

(defn haxe-tf-x-return-encode
      [[_ out id key]]
      (template/$
       (do (var type-fn
                (function [obj]
                          (cond (Std.isOfType obj Array) (return "array")
                                (Std.isOfType obj haxe.ds.StringMap) (return "object")
                                (Std.isOfType obj haxe.ds.IntMap) (return "object")
                                (Reflect.isFunction obj) (return "function")
                                (== bool (type obj)) (return "boolean")
                                (or (Std.isOfType obj Int) (Std.isOfType obj Float)) (return "number")
                                (Std.isOfType obj String) (return "string")
                                (== null obj) (return "nil")
                                :else (return (str (type obj))))))
           (var json-filter
                (function [obj]
                          (cond (Std.isOfType obj Array)
                                (do (var out [])
                                    (for:array [v obj]
                                               (when (not (Reflect.isFunction v))
                                                     (. out (push (json-filter v)))))
                                    (return out))
                                (Std.isOfType obj haxe.ds.StringMap)
                                (do (var out (new haxe.ds.StringMap))
                                    (for:object [[k v] obj]
                                                (when (not (Reflect.isFunction v))
                                                      (. out (set k (json-filter v)))))
                                    (return out))
                                (Reflect.isFunction obj)
                                (return null)
                                :else
                                (return obj))))
           (var ts (type-fn ~out))
           (try
            (return (haxe.Json.stringify {:id ~id :key ~key :type "data" :return ts :value (json-filter ~out)}))
            (catch Exception
                   (return (haxe.Json.stringify {:id ~id :key ~key :type "raw" :return ts :value (str ~out)})))))))

(defn haxe-tf-x-return-wrap
      [[_ f encode-fn]]
      (template/$
       (do (try (:= out (~f))
                (catch Exception (:= e)
                       (return (haxe.Json.stringify {:type "error" :value (str e)}))))
           (return (~encode-fn out null null)))))

(defn haxe-tf-x-return-eval
      [[_ s wrap-fn]]
      (template/$
       (do (function thunk []
                     (var g (globals))
                     (eval ~s g g)
                     (return (. g (get "OUT"))))
           (return (~wrap-fn thunk)))))

(def +haxe-core+
     {:x-del            {:macro #'haxe-tf-x-del            :emit :macro}
      :x-del-key        {:macro #'haxe-tf-x-del-key        :emit :macro}
      :x-cat            {:macro #'haxe-tf-x-cat            :emit :macro}
      :x-len            {:macro #'haxe-tf-x-len            :emit :macro}
      :x-get-key        {:macro #'haxe-tf-x-get-key        :emit :macro}
      :x-has-key?       {:macro #'haxe-tf-x-has-key?       :emit :macro}
      :x-err            {:macro #'haxe-tf-x-err            :emit :macro}
      :x-ex-native?     {:macro #'haxe-tf-x-ex-native?     :emit :macro}
      :x-ex-new         {:macro #'haxe-tf-x-ex-new         :emit :macro}
      :x-ex-message     {:macro #'haxe-tf-x-ex-message     :emit :macro}
      :x-ex-data        {:macro #'haxe-tf-x-ex-data        :emit :macro}
      :x-eval           {:macro #'haxe-tf-x-eval           :emit :macro}
      :x-apply          {:macro #'haxe-tf-x-apply          :emit :macro}
      :x-random         {:macro #'haxe-tf-x-random         :emit :macro}
      :x-print          {:macro #'haxe-tf-x-print          :emit :macro}
      :x-type-native    {:macro #'haxe-tf-x-type-native    :emit :macro}})

(def +haxe-type+
     {:x-to-string      {:macro #'haxe-tf-x-to-string      :emit :macro}
      :x-to-number      {:macro #'haxe-tf-x-to-number      :emit :macro}
      :x-is-string?     {:macro #'haxe-tf-x-is-string?     :emit :macro}
      :x-is-number?     {:macro #'haxe-tf-x-is-number?     :emit :macro}
      :x-is-integer?    {:macro #'haxe-tf-x-is-integer?    :emit :macro}
      :x-is-boolean?    {:macro #'haxe-tf-x-is-boolean?    :emit :macro}
      :x-is-function?   {:macro #'haxe-tf-x-is-function?   :emit :macro}
      :x-is-object?     {:macro #'haxe-tf-x-is-object?     :emit :macro}
      :x-is-array?      {:macro #'haxe-tf-x-is-array?      :emit :macro}})

(def +haxe-js+
     {:x-json-encode    {:macro #'haxe-tf-x-json-encode    :emit :macro}
      :x-json-decode    {:macro #'haxe-tf-x-json-decode    :emit :macro}})

(def +haxe-return+
     {:x-return-encode  {:macro #'haxe-tf-x-return-encode  :emit :macro
                         :op-spec {:allow-blocks true}}
      :x-return-wrap    {:macro #'haxe-tf-x-return-wrap    :emit :macro
                         :op-spec {:allow-blocks true}}
      :x-return-eval    {:macro #'haxe-tf-x-return-eval    :emit :macro
                         :op-spec {:allow-blocks true}}})

(def +haxe+
     (merge +haxe-core+
            +haxe-type+
            +haxe-js+
            +haxe-return+))

;;;
;;; GRAMMAR
;;;

(defn haxe-fn
      "Normalizes function forms for Haxe."
      [[_ & args]]
      (if (symbol? (first args))
          (apply list 'fn.inner (rest args))
          (apply list 'fn.inner args)))

(defn haxe-var
      "Emits Haxe `var` declarations."
      [[_ decl & args]]
      (if (empty? args)
          (list 'var* decl)
          (let [bound (last args)]
               (cond
                (vector? decl)
                (let [tmp (gensym "value_")]
                     (apply list 'do*
                            (cons (list 'var* tmp := bound)
                                  (map-indexed (fn [i sym]
                                                   (list 'var* sym := (list '. tmp [i])))
                                               decl))))

                (set? decl)
                (apply list 'do*
                       (map (fn [sym]
                                (list 'var* sym := (list '. bound [(name sym)])))
                            (sort-by name decl)))

                :else
                (list 'var* decl := bound)))))

(defn haxe-for-object
      "for object transform"
      [[_ [[k v] m] & body]]
      (cond (= k '_)
            (apply list 'for [(list 'var* v) :in (list '. m 'values)]
                   body)

            (= v '_)
            (apply list 'for [(list 'var* k) :in (list '. m 'keys)]
                   body)

            :else
            (apply list 'for [(list 'var* k) ':=> (list 'var* v) :in m]
                   body)))

(defn haxe-for-array
      "for array transform"
      [[_ [e arr] & body]]
      (if (vector? e)
          (let [[i v] e]
               (template/$ (do (var* ~arr-sym := ~arr)
                               (for [(var* ~i := 0) (< ~i (. ~arr-sym length)) (:++ ~i)]
                                    (var* ~v := (. ~arr-sym [~i]))
                                    ~@body))))
          (apply list 'for [e :in arr]
                 body)))

(defn haxe-symbol
      "Emits Haxe symbols, handling reserved words and hyphen conversion."
      [sym grammar mopts]
      (let [reserved #{"break" "case" "cast" "catch" "class" "continue" "default"
                       "do" "dynamic" "else" "enum" "extends" "extern" "false"
                       "for" "function" "if" "implements" "import" "in" "inline"
                       "interface" "macro" "new" "null" "override" "package" "private"
                       "public" "return" "static" "switch" "throw" "true" "try"
                       "typedef" "untyped" "using" "var" "while"}
            sym-name (when (symbol? sym) (name sym))
            local-name (when sym-name
                             (cond-> (clojure.string/replace sym-name "-" "_")
                                     (contains? reserved sym-name) (str "_")))]
           (cond (keyword? sym)
                 (str "\"" (name sym) "\"")

                 (and (symbol? sym)
                      (nil? (namespace sym)))
                 local-name

                 :else
                 (common/emit-symbol sym grammar mopts))))

(defn haxe-map-key
      "Map keys in Haxe must be strings for StringMap."
      [key grammar mopts]
      (cond
       (keyword? key)
       (common/*emit-fn* (name key) grammar mopts)

       (or (string? key)
           (symbol? key)
           (number? key)
           (boolean? key)
           (nil? key))
       (data/default-map-key key grammar mopts)

       :else
       (emit/emit-main key grammar mopts)))

(def +features+
     (-> (grammar/build :exclude [:pointer
                                  :block
                                  :data-set])
         (grammar/build:override
          {:fn          {:macro #'haxe-fn :emit :macro}
           :var         {:symbol '#{var var*} :raw "var" :assign "="}
           :pow         {:emit :alias :raw 'Math.pow :value true}
           :defn        {:symbol '#{defn}}
           :new         {:symbol '#{new} :raw "new" :emit :new}
           :for-object  {:macro #'haxe-for-object :emit :macro}
           :for-array   {:macro #'haxe-for-array  :emit :macro}
           :with-global {:value true :raw "__globals__"}})
         (grammar/build:override +haxe+)
         (grammar/build:extend
          {:haxe-or      {:op :haxe-or      :symbol #{'haxe:or}      :emit :infix :raw "??"}})))

(def +template+
     (-> (emit/default-grammar)
         (collection/merge-nested
          {:banned #{:set :regex}
           :highlight '#{return break continue}
           :default {:common    {:statement ";"}
                     :function  {:prefix "function"
                                 :raw ""
                                 :args {:sep ", "}}
                     :invoke    {:reversed true :hint ""}
                     :block     {:start " {" :end "}"}}
           :function {:defgen {:body {:start " {" :end "}"}}}
           :define  {:def {:raw "var"}}
           :token   {:symbol {:replace {"-" "_"}
                              :global #'haxe-symbol}
                     :nil {:as "null"}}
           :data    {:vector {:start "[" :end "]" :space ""}
                     :map    {:start "[" :end "]" :space ""
                              :sep " => "}
                     :map-entry {:start "" :end ""
                                 :assign " => "
                                 :key-fn #'haxe-map-key}}})))

(def +grammar+
     (grammar/grammar :hx
                      (grammar/to-reserved +features+)
                      +template+))

(def +meta+
     (book/book-meta
      {:module-current   (fn [])
       :module-import    (fn [name {:keys [as]} _]
                             (list :-
                                   (str "import " name ";"
                                        (when as
                                              (str " // as " as)))))}))

(def +book+
     (book/book {:lang :haxe
                 :parent :xtalk
                 :meta +meta+
                 :grammar +grammar+}))

(def +init+
     (script/install +book+))
