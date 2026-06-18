(ns hara.model.annex.spec-julia
  (:require [clojure.string]
             [std.fs :as fs]
             [hara.lang.book :as book]
             [hara.lang.book-module :as module]
             [hara.common.emit :as emit]
             [hara.common.emit-common :as common]
             [hara.common.emit-helper :as helper]
             [hara.common.grammar :as grammar]
             [hara.lang.script :as script]
             [hara.common.util :as ut]
             [hara.model.spec-xtalk]
             [hara.model.annex.spec-julia.rewrite :as rewrite]
             [hara.model.annex.spec-xtalk.fn-julia :as fn]
             [std.lib.collection :as collection]
             [std.lib.foundation :as f]
             [std.lib.template :as template])
  (:refer-clojure :exclude [for import]))

;;
;; LANG
;;

(defn tf-local
  "a more flexible `var` replacement"
  {:added "4.0"}
  [[op decl & args]]
  (if (empty? args)
    (list 'var* :local decl)
    (let [bound (last args)]
      (cond (and (collection/form? bound)
                 (= 'fn (first bound)))
            (apply list 'defn (with-meta decl {:inner true})
                   (rest bound))

            (vector? decl)
            (let [tmp (gensym "value__")]
              (apply list 'do*
                     (cons (list 'var* :local tmp := bound)
                           (map-indexed (fn [i sym]
                                          (list 'var* :local sym := (list 'getindex tmp (inc i))))
                                        decl))))

            (set? decl)
            (apply list 'do*
                   (map (fn [sym]
                          (list 'var* :local sym := (list 'getindex (list 'Dict bound) (ut/sym-default-str sym))))
                        (sort-by ut/sym-default-str decl)))

            :else
            (if (= op 'local)
              (list 'var* :local decl := bound)
              (list ':= decl bound))))))

(defn tf-ternary
  "expands ternary (:?) to block-style if-else so Julia can return non-boolean values"
  {:added "4.1"}
  [[_ test then else]]
  (list 'if test then else))

(defn julia-map-key
  "custom julia map key"
  {:added "3.0"}
  ([key grammar mopts]
   (cond (keyword? key)
         (str "\"" (name key) "\"")

         :else
         (common/*emit-fn* key grammar mopts))))

(defn julia-symbol-global
  [key _grammar _mopts]
  (list 'get 'XT_GLOBALS (ut/sym-default-str key) nil))

(defn tf-for-array
  "for array transform"
  {:added "4.0"}
  [[_ [e arr] & body]]
  (let [idx (gensym "idx__")
        arr-sym (gensym "arr__")]
    (if (vector? e)
      (let [[i v] e]
        (template/$
         (do (local ~arr-sym ~arr)
             (for [~idx :in (to 1 1 (length ~arr-sym))]
               (local ~i ~idx)
               (local ~v (getindex ~arr-sym ~idx))
               ~@body))))
      (template/$
       (do (local ~arr-sym ~arr)
           (for [~idx :in (to 1 1 (length ~arr-sym))]
             (local ~e (getindex ~arr-sym ~idx))
             ~@body))))))

(defn tf-for-object
  "for object transform"
  {:added "4.0"}
  [[_ [[k v] m] & body]]
  (cond (= k '_)
        (apply list 'for [v :in (list 'values m)]
               body)

        (= v '_)
        (apply list 'for [k :in (list 'keys m)]
               body)

        :else
        (let [pair (gensym "pair__")]
          (template/$
           (for [~pair :in (collect ~m)]
             (local ~k (getindex ~pair 1))
             (local ~v (getindex ~pair 2))
             ~@body)))))

(defn tf-for-iter
  "for iter transform"
  {:added "4.1"}
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
  (let [start (common/*emit-fn* start grammar mopts)
        step  (common/*emit-fn* step grammar mopts)
        end   (common/*emit-fn* end grammar mopts)]
    (if (= step "1")
      (str start ":" end)
      (str start ":" step ":" end))))

(defn emit-throw
  "emits throw with parentheses for complex expressions"
  {:added "4.1"}
  [[_ expr] grammar mopts]
  (str "throw(" (common/*emit-fn* expr grammar mopts) ")"))

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
                               :control-try-catch
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
        :with-global {:value true :raw "XT_GLOBALS"}
        :inif   {:macro #'tf-ternary :emit :macro}
        :for-array  {:macro #'tf-for-array   :emit :macro}
        :for-object {:macro #'tf-for-object  :emit :macro}
        :for-iter   {:macro #'tf-for-iter    :emit :macro}
        :for-index  {:macro #'tf-for-index   :emit :macro}
        :throw {:op :throw :symbol #{'throw} :emit #'emit-throw}})
      (grammar/build:override fn/+julia+)
      (grammar/build:extend
       {:cat   {:op :cat   :symbol '#{cat}       :raw "*"      :emit :infix}
        :len   {:op :len   :symbol '#{len}       :raw "length" :emit :pre}
        :local {:op :local :symbol '#{local var} :macro #'tf-local :emit :macro}
        :pair  {:op :pair  :symbol '#{=>}        :raw "=>"     :emit :infix}
        :dict  {:op :dict  :symbol '#{dict}      :macro #'tf-dict :emit :macro}
        :push! {:op :push! :symbol '#{push!}     :raw "push!"  :emit :invoke}
        :xor   {:op :xor   :symbol '#{xor}       :raw "⊻"      :emit :infix}
        :bxor  {:op :bxor  :symbol '#{b:xor}     :raw "⊻"      :emit :infix}
        :bsl   {:op :bsl   :symbol '#{b:<<}      :raw "<<"     :emit :bi}
        :bsr   {:op :bsr   :symbol '#{b:>>}      :raw ">>"     :emit :bi}
        :splat {:op :splat :symbol '#{...}       :raw "..."    :emit :post}
        :%     {:op :%     :symbol #{:%}         :emit :squash}
        :to    {:op :to    :symbol #{'to}        :emit #'emit-to}
        :prototype-create {:op :prototype-create :symbol #{'proto:create} :macro #'fn/julia-tf-x-prototype-create :emit :macro}
        :prototype-set    {:op :prototype-set    :symbol #{'proto:set}    :macro #'fn/julia-tf-x-prototype-set    :emit :macro}
        :prototype-get    {:op :prototype-get    :symbol #{'proto:get}    :macro #'fn/julia-tf-x-prototype-get    :emit :macro}
        :prototype-method {:op :prototype-method :symbol #{'proto:method} :macro #'fn/julia-tf-x-prototype-method :emit :macro}
        :prototype-tostring {:op :prototype-tostring :symbol #{'proto:tostring} :macro #'fn/julia-tf-x-prototype-tostring :emit :macro}})))

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
                  :string    {:quote :double}
                  :symbol    {:global #'julia-symbol-global
                              :replace (assoc helper/+sym-replace+ \! "!")}}
         :data   {:map-entry {:start ""  :end ""  :space "" :assign " => " :keyword :string
                              :key-fn #'julia-map-key}
                  :map       {:start "Dict{Any,Any}(" :end ")"}
                  :vector    {:start "Any[" :end "]" :space ", "}}
         :rewrite {:staging [#'rewrite/julia-rewrite-stage]}
         :block  {:for       {:body    {:start "" :end "end"}
                              :parameter {:start " " :end "" :space " "}}
                  :try      {:wrap    {:start "" :end "end"}
                             :body    {:start "" :end "" :append true}
                            :control {:catch   {:raw "catch"
                                                :parameter {:start " " :end ""}
                                                :body {:start "" :end "" :append true}}
                                      :finally {:raw "finally"
                                                :body {:start "" :end "" :append true}}}}
                 :while     {:body    {:start "" :end "end"}}
                 :branch    {:wrap    {:start "" :end "end"}
                             :control {:default {:parameter  {:start " " :end ""}
                                                 :body {:append true :start "" :end ""}}
                                       :if      {:raw "if"}
                                       :elseif  {:raw "elseif"}
                                       :else    {:raw "else"}}}}
        :function {:defn      {:raw "function"}}
        :define   {:def       {:raw ""}
                   :defglobal {:raw ""}
                   :declare   {:raw ""}}}
       (collection/merge-nested (emit/default-grammar))))

(defn julia-module-link
  "gets the absolute julia based module"
  {:added "4.0"}
  ([ns graph]
   (let [{:keys [target root-ns]} graph
         root-path (->> (clojure.string/split (name root-ns) #"\.")
                        (butlast)
                        (clojure.string/join "/"))

         ns-path   (clojure.string/replace (name ns) #"\." "/")]
     (if (clojure.string/starts-with? ns-path (str root-path))
       (f/->> (fs/relativize root-path ns-path)
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
   {:module-current f/NIL
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
