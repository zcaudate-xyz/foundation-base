(ns std.lang.model.spec-julia
  (:require [std.lang.base.emit-common :as common]
            [std.lang.base.emit :as emit]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.impl :as impl]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.book-module :as module]
            [std.lang.base.script :as script]
            [std.lang.model.spec-xtalk]
            [std.lang.model.spec-xtalk.fn-julia :as fn]
            [std.string :as str]
            [std.lib :as h]
            [std.fs :as fs]))

;;
;; LANG
;;

(defn tf-local
  "a more flexible `var` replacement"
  {:added "4.0"}
  [[op decl & args]]
  (if (empty? args)
    (if (= op 'local)
      (list 'var* :local decl)
      (list 'var* :local decl)) ;; default declaration
    (let [bound (last args)]
      (cond (and (h/form? bound)
                 (= 'fn (first bound)))
            (apply list 'defn (with-meta decl {:inner true})
                   (rest bound))

            :else
            (if (= op 'local)
              (list 'var* :local decl := bound)
              (list ':= decl bound))))))

(defn julia-map-key
  "custom julia map key"
  {:added "3.0"}
  ([key grammar mopts]
   (cond (keyword? key)
         (str "\"" (name key) "\"")

         :else
         (common/*emit-fn* key grammar mopts))))

(defn tf-for-iter
  "for iter transform"
  {:added "4.0"}
  [[_ [e it] & body]]
  (apply list 'for [e :in it]
         body))

(defn tf-for-index
  "for index transform"
  {:added "4.0"}
  [[_ [i [start end step :as range]] & body]]
  (apply list 'for [i :in (list 'to start (or step 1) end)]
           body))

(defn tf-dict
  "dict transform"
  {:added "4.0"}
  [[_ & args]]
  (let [pairs (partition 2 args)
        args  (map (fn [[k v]]
                     (list '=> (if (keyword? k) (name k) k) v))
                   pairs)]
    (apply list 'Dict args)))

(defn emit-to
  "emits the range"
  {:added "4.0"}
  [[_ start step end] grammar mopts]
  (if (= step 1)
    (str (common/emit-wrapping start grammar mopts) ":" (common/emit-wrapping end grammar mopts))
    (str (common/emit-wrapping start grammar mopts) ":" (common/emit-wrapping step grammar mopts) ":" (common/emit-wrapping end grammar mopts))))

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
                               :macro
                               :macro-arrow
                               :macro-let
                               :macro-xor])
      (merge (grammar/build-xtalk))
      (grammar/build:override
       {:var    {:symbol '#{var*}}
        :not    {:raw "!"}
        :and    {:raw "&&"}
        :or     {:raw "||"}
        :neq    {:raw "!="}
        :mod    {:raw "%"}
        :pow    {:raw "^"}
        :for-iter   {:macro #'tf-for-iter   :emit :macro}
        :for-index  {:macro #'tf-for-index  :emit :macro}})
      (grammar/build:override fn/+julia+)
      (grammar/build:extend
       {:cat    {:op :cat    :symbol '#{cat}       :raw "*"   :emit :infix}
        :len    {:op :len    :symbol '#{len}       :raw "length"    :emit  :pre}
        :local  {:op :local  :symbol '#{local var} :macro  #'tf-local :type :macro}
        :pair   {:op :pair   :symbol '#{=>}        :raw "=>"        :emit :infix}
        :dict   {:op :dict   :symbol '#{dict}      :macro #'tf-dict :emit :macro}
        :push!  {:op :push!  :symbol '#{push!}     :raw "push!" :emit :invoke}
        :%      {:op :%      :symbol #{:%}         :emit :squash}
        :to     {:op :to     :symbol #{'to}        :emit #'emit-to}})))

(def +template+
  (->> {:banned #{:set :regex}
        :allow   {:assign  #{:symbol :quote}}
        :highlight '#{return break local end for if elseif else function module using import}
        :default {:comment   {:prefix "#"}
                  :common    {:apply "(" :statement ""
                              :namespace-full "."
                              :namespace-sep  "."}
                  :index     {:offset 1  :end-inclusive true}
                  :return    {:multi true}
                  :block     {:parameter {:start "(" :end ")" :space ", "}
                              :body      {:start "" :end "end"}}
                  :function  {:raw "function"
                              :body      {:start "" :end "end"}}
                  :infix     {:if  {:check "&&" :then "||"}}
                  :global    {:reference nil}}
        :token  {:nil       {:as "nothing"}
                 :string    {:quote :double}}
        :data   {:map-entry {:start ""  :end ""  :space "" :assign " => " :keyword :string
                             :key-fn #'julia-map-key}
                 :map       {:start "Dict(" :end ")" :space ", "}
                 :vector    {:start "[" :end "]" :space ", "}}
        :block  {:for       {:body    {:start "" :end "end"}
                             :parameter {:start " " :end "" :space " "}}
                 :while     {:body    {:start "" :end "end"}}
                 :branch    {:wrap    {:start "" :end "end"}
                             :control {:default {:parameter  {:start " " :end ""}
                                                 :body {:append true :start "" :end " end"}}
                                       :if      {:raw "if"}
                                       :elseif  {:raw "elseif"}
                                       :else    {:raw "else"}}}}
        :function {:defn      {:raw "function"}}
        :define   {:def       {:raw ""}
                   :defglobal {:raw ""}
                   :declare   {:raw ""}}}
       (h/merge-nested (emit/default-grammar))))

(defn julia-module-link
  "gets the absolute julia based module"
  {:added "4.0"}
  ([ns graph]
   (let [{:keys [target root-ns]} graph
         root-path (->> (str/split (name root-ns) #"\.")
                        (butlast)
                        (str/join "/"))

         ns-path   (str/replace (name ns) #"\." "/")]
     (if (str/starts-with? ns-path (str root-path))
       (h/->> (fs/relativize root-path ns-path)
              (str)
              (str "./"))
       (str "./" ns-path)))))

(defn julia-module-export
  "outputs the julia module export form"
  {:added "4.0"}
  ([module mopts]
   (let [exports (module/module-entries module #{:defn :def})]
     (if (seq exports)
       (list 'export (apply list exports))
       nil))))

(def +meta+
  (book/book-meta
   {:module-current h/NIL
    :module-link    #'julia-module-link
    :module-export  #'julia-module-export
    :module-import  (fn [name {:keys [as]} opts]
                      (if as
                        (list 'import name :as as)
                        (list 'using name)))
    :has-ptr        (fn [ptr] (list '!= (ut/sym-full ptr) nil))
    :teardown-ptr   (fn [ptr] (list := (ut/sym-full ptr) nil))}))

(def +grammar+
  (grammar/grammar :julia
    (grammar/to-reserved +features+)
    +template+))

(def +book+
  (book/book {:lang :julia
              :parent :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
