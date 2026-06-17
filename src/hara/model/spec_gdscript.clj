(ns hara.model.spec-gdscript
  (:require [clojure.string :as string]
            [std.fs :as fs]
            [hara.lang.book :as book]
            [hara.lang.book-module :as module]
            [hara.common.emit :as emit]
            [hara.common.emit-common :as common]
            [hara.common.emit-data :as data]
            [hara.common.emit-helper :as helper]
            [hara.common.grammar :as grammar]
            [hara.common.preprocess-base :as preprocess-base]
            [hara.lang.script :as script]
            [hara.common.util :as ut]
            [hara.model.spec-gdscript.rewrite :as rewrite]
            [hara.model.spec-xtalk]
            [hara.model.spec-xtalk.fn-gdscript :as fn]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.template :as template])
  (:refer-clojure :exclude [await]))

;;
;; GDScript is indentation-based and Python-like.  This spec is intentionally
;; minimal: it targets Godot 4.x GDScript and covers expressions, variables,
;; functions, control flow and basic data literals.
;;

(defn- gdscript-token-boolean
  [v]
  (if v "true" "false"))

(defn- gdscript-symbol-global
  [fsym grammar mopts]
  (list '. 'Globals [(helper/emit-symbol-full fsym
                                              (namespace fsym)
                                              grammar)]))

(defn gdscript-dot
  "rewrites JS-style array length lookups to GDScript size()"
  [[_ obj & props]]
  (let [grammar preprocess-base/*macro-grammar*
        mopts   preprocess-base/*macro-opts*
        props (if (and (= 1 (count props))
                       (vector? (first props))
                       (= "length" (ffirst props)))
                ['(size)]
                props)
        target (let [emitted (common/*emit-fn* obj grammar mopts)]
                 (if (collection/form? obj)
                   (str "(" emitted ")")
                   emitted))]
    (list ':- (str target
                   (apply str (map #(common/emit-index-entry % grammar mopts)
                                   props))))))

(defn gdscript-var
  "var -> fn.inner shorthand, mirroring Python behaviour"
  ([[_ sym & args]]
   (let [bound (last args)]
     (cond (and (collection/form? bound)
                (= 'fn (first bound)))
           (apply list 'fn.inner (with-meta sym {:inner true})
                  (rest bound))

           (vector? sym)
           (list 'var* (list 'quote sym) := bound)

           (set? sym)
           (cons 'do
                 (map (fn [e]
                        (list 'var* e := (list '. bound (list 'get (ut/sym-default-str e)))))
                      sym))

           :else
           (list 'var* sym := bound)))))

(defn gdscript-fn
  "basic transform for GDScript lambdas/closures"
  ([[_ & args]]
   (cond (symbol? (first args))
         (apply list 'fn.inner (with-meta (first args)
                                 {:inner true})
                (rest args))

         :else
         (let [[args body] args
               args (if (empty? args)
                      [(list ':* '__args)]
                      args)]
           (apply list :- :lambda
                  (concat (if (not-empty args)
                            [(list 'quote args) ":"]
                            [":"])
                          [body]))))))

(defn tf-for-object
  "for object loop"
  [[_ [[k v] m] & body]]
  (let [[binding method] (cond (= k '_) [v '(values)]
                               (= v '_) [k '(keys)]
                               :else [[k v] '(items)])]
    (apply list 'for [binding :in (list '. m method)]
           (or (not-empty body)
               ['(pass)]))))

(defn tf-for-array
  "for array loop"
  [[_ [e arr] & body]]
  (if (vector? e)
    (let [[i v] e]
      (apply list 'for [i :in (list 'range (list '. arr ['size]))]
             (list 'var v (list '. arr [i]))
             (or (not-empty body)
                 ['(pass)])))
    (apply list 'for [e :in arr]
           (or (not-empty body)
               ['(pass)]))))

(defn tf-for-iter
  "for iter loop"
  [[_ [e it] & body]]
  (apply list 'for [e :in it]
         (or (not-empty body)
             ['(pass)])))

(defn tf-for-index
  "for index transform"
  [[_ [i range] & body]]
  (apply list 'for [i :in (apply list 'range (filter identity range))]
         (or (not-empty body)
             ['(pass)])))

(def +features+
  (-> (grammar/build :include [:builtin
                               :builtin-global
                               :builtin-module
                               :builtin-helper
                               :free-control
                               :free-literal
                               :math
                               :compare
                               :logic
                               :return
                               :throw
                               :data-table
                               :data-shortcuts
                               :vars
                               :fn
                               :control-base
                               :control-general
                               :top-base
                               :top-global
                               :top-declare
                               :for
                               :prototype
                               :macro
                               :macro-arrow
                               :macro-let
                               :macro-xor])
       (merge (grammar/build-xtalk))
       (grammar/build:override
        {:pow         {:raw "**"}
         :and         {:raw "and"}
         :or          {:raw "or"}
         :not         {:raw "not" :emit :prefix}
         :throw       {:raw "push_error" :emit :prefix}
         :index       {:macro #'gdscript-dot :emit :macro}
         :fn          {:macro  #'gdscript-fn   :emit :macro}
         :var         {:symbol #{'var*} :raw "var"}
         :var.inner   {:raw "var"}
         :for-object  {:macro #'tf-for-object :emit :macro}
         :for-array   {:macro #'tf-for-array  :emit :macro}
         :for-iter    {:macro #'tf-for-iter   :emit :macro}
         :for-index   {:macro #'tf-for-index  :emit :macro}})
       (grammar/build:override fn/+gdscript+)
       (grammar/build:extend
        {:var-let   {:op :var-let :symbol #{'var}  :macro #'gdscript-var :emit :macro}
         :pass      {:op :pass    :symbol #{'pass} :raw "pass" :emit :return :type :special}
         :del       {:op :del     :symbol #{'del}  :raw "free"  :emit :prefix}
         :with      {:op :with    :symbol #{'with} :type :block
                     :block  {:main #{:parameter :body}}}})))

(def +template+
  (->> {:banned #{:keyword :set :regex}
        :allow   {:assign  #{:symbol :quote}}
        :highlight '#{return break pass await yield}
        :default {:comment   {:prefix "#"}
                  :common    {:statement ""
                              :assign "="
                              :namespace "."
                              :namespace-full "____"
                              :access "."
                              :apply "."
                              :line-spacing 1}
                  :block     {:parameter {:start " " :end ""}
                              :body      {:start ":" :end "" :append true}}
                  :invoke    {:reversed true
                              :hint ":"}
                  :function  {:raw "func"
                              :args      {:start "(" :end "):" :space ""}
                              :body      {:start "" :end "" :append true}}
                  :infix     {:if  {:check "and" :then "or"}}
                  :global    {:reference 'Globals}}
        :token   {:nil       {:as "null"}
                  :boolean   {:as #'gdscript-token-boolean}
                  :string    {}
                  :symbol    {:global #'gdscript-symbol-global
                              :namespace {:alias true :link false}}}
        :block    {:try      {:control {:catch  {:raw  "catch"
                                                 :args {:start "" :end ":" :space ""}}}}
                   :branch   {:control {:elseif {:raw "elif"}}}}
        :data     {:map-entry {:key-fn data/default-map-key}
                   :set       {:start "{" :end "}" :space ""}
                   :vector    {:start "[" :end "]" :space ""}
                   :tuple     {:start "(" :end ")" :space ""}
                   :free      {:start ""  :end "" :space ""}}
        :rewrite  {:staging [#'rewrite/gdscript-rewrite-stage]}
        :function {:defn        {:raw "func"
                                 :symbol {:layout :flat}
                                 :args  {:start "(" :end "):" :space ""}}
                   :fn.inner    {:raw "func"
                                 :symbol {:layout :flat}
                                 :args   {:start "(" :end "):" :space ""}}}
        :define   {:defglobal  {:raw ""}
                   :def        {:raw "var"}}}
       (collection/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :gd
    (grammar/to-reserved +features+)
    +template+))

(defn gdscript-module-link
  "computes a relative module path for GDScript preload"
  ([ns graph]
   (let [{:keys [target root-ns]} graph
         root-path (->> (string/split (name root-ns) #"\.")
                        (butlast)
                        (string/join "/"))
         ns-path   (string/replace (name ns) #"\." "/")]
     (if (string/starts-with? ns-path (str root-path))
       (str "./" (fs/relativize root-path ns-path))
       (str "./" ns-path)))))

(defn gdscript-module-export
  "outputs the gdscript module export form"
  ([module mopts]
   (let [entries (->> (module/module-entries module #{:defn :def})
                      (cons 'tab))]
     (list 'return entries))))

(def +meta+
  (book/book-meta
   {:module-current f/NIL
    :module-link    #'gdscript-module-link
    :module-export  #'gdscript-module-export
    :module-import  (fn [name {:keys [as]} opts]
                      (template/$ (var* :local ~as := (preload ~(str name)))))
    :has-ptr        (fn [ptr] (list 'not= (ut/sym-full ptr) nil))
    :teardown-ptr   (fn [ptr] (list := (ut/sym-full ptr) nil))}))

(def +book+
  (book/book {:lang :gdscript
              :parent :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
