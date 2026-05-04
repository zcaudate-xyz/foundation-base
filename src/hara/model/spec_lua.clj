(ns hara.model.spec-lua
  (:require [clojure.string]
            [std.fs :as fs]
            [hara.lang.book :as book]
            [hara.lang.book-module :as module]
            [hara.common.emit :as emit]
            [hara.common.emit-common :as common]
            [hara.common.grammar :as grammar]
            [hara.common.grammar-spec :as spec]
            [hara.lang.impl :as impl]
            [hara.lang.script :as script]
            [hara.common.util :as ut]
            [hara.model.spec-lua.rewrite :as rewrite]
            [hara.model.spec-xtalk]
            [hara.model.spec-xtalk.fn-lua :as fn]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.template :as template]))

(defn lua-tf-incby
  "lowers `:+=` into plain assignment"
  {:added "4.1"}
  [[_ target value]]
  (list := target (list '+ target value)))

(defn lua-tf-decby
  "lowers `:-=` into plain assignment"
  {:added "4.1"}
  [[_ target value]]
  (list := target (list '- target value)))

(defn lua-tf-mulby
  "lowers `:*=` into plain assignment"
  {:added "4.1"}
  [[_ target value]]
  (list := target (list '* target value)))

(defn lua-tf-local
  "a more flexible `var` replacement"
  {:added "4.0"}
  [[_ decl & args]]
  (if (empty? args)
    (list 'var* :local decl)
    (let [bound (last args)]
      (cond (and (collection/form? bound)
                 (= 'fn (first bound)))
            (apply list 'defn (with-meta decl {:inner true})
                   (rest bound))
            
            (vector? decl)
            (apply list 'var* :local (list 'quote decl)
                   [:= (list 'unpack bound)])

            (set? decl)
            (cons 'do
                  (map (fn [sym]
                         (let [sym-index (ut/sym-default-str sym)]
                           (list 'var* :local sym := (list '. bound [sym-index]))))
                       decl))
            
            :else (list 'var* :local decl := bound)))))

(defn lua-tf-c-ffi
  "transforms a c ffi block"
  {:added "4.0"}
  [[_ & forms]]
  `(\\ "[["
    ^{:indent 2}
    (\\ \\ (~'!:lang {:lang :c} (~'do ~@forms)))
    \\ "]]"))

(defn lua-map-key
  "custom lua map key"
  {:added "3.0"}
  ([key grammar mopts]
   (cond (not (or (collection/form? key)
                  (symbol? key)
                  (number? key)))
         (let [key-str (cond (string? key)
                             key
                             
                             :else
                             (ut/sym-default-str key))
               key-str (cond (and (not (#{"function"
                                          "return"
                                          "end"
                                          "for"
                                          "in"
                                          "var"} key-str))
                                  (re-find #"^[A-Za-z][\w\d]*$" key-str))
                             key-str
                             
                             :else
                             (str "['" key-str "']"))]
           key-str)

         :else
         (str  "[" (common/*emit-fn* key grammar mopts) "]"))))

(defn lua-tf-for-object
  "for object transform"
  {:added "4.0"}
  [[_ [[k v] m] & body]]
  (apply list 'for [[k v] :in (list 'pairs m)]
         body))

(defn lua-tf-for-array
  "for array transform"
  {:added "4.0"}
  [[_ [e arr] & body]]
  (if (vector? e)
    (apply list 'for [e :in (list 'ipairs arr)]
           body)
    (apply list 'for [['_ e] :in (list 'ipairs arr)]
           body)))

(defn lua-tf-for-iter
  "for iter transform"
  {:added "4.0"}
  [[_ [e it] & body]]
  (apply list 'for [e :in it]
         body))

(defn lua-tf-for-index
  "for index transform"
  {:added "4.0"}
  [[_ [i [start end step :as range]] & body]]
  (apply list 'for [i := (list 'quote [start end (or step 1)])]
         body))

(defn lua-tf-for-return
  "for return transform"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error]}]]
  (template/$
   (do (var '[~res ~err] ~statement)
       (if (not ~err)
         ~success
         ~error))))

(defn lua-tf-for-async
  "for async transform"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error finally]}]]
  (template/$ (x:thread-spawn
               (fn []
                 (for:try [[~res ~err] ~statement]
                          {:success ~success
                           :error ~error})
                 ~@(if finally [finally])))))

(defn lua-tf-yield
  "yield transform"
  {:added "4.0"}
  [[_ e]]
  (list 'coroutine.yield e))

(defn lua-tf-throw
  "preserves the raw thrown payload through pcall"
  {:added "4.1"}
  [[_ value]]
  (list 'error value 0))

(defn lua-tf-defgen
  "defgen transform"
  {:added "4.0"}
  [[_ sym args & body]]
  (list 'defn sym args
        (list 'return (list 'coroutine.wrap
                            (apply list 'fn [] body)))))

(defn lua-tf-prototype-create
  [[_ m]]
  (template/$
   (do (var mt ~m)
       (:= (. mt __index) mt)
       (return mt))))

(defn lua-tf-prototype-method
  [[_ obj key]]
  (let [direct   (list 'x:get-key obj key nil)
        value    (list ':? (list 'not= nil direct)
                       direct
                       (list 'x:get-key (list 'proto:get obj) key nil))]
    value))

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
                               :coroutine
                               :prototype
                               :macro
                               :macro-arrow
                               :macro-let
                               :macro-xor])
      (merge (grammar/build-xtalk))
        (grammar/build:override
        {:var        {:symbol '#{var*}}
         :not        {:raw "not "}
         :and        {:raw "and"}
         :or         {:raw "or"}
         :throw      {:macro #'lua-tf-throw :emit :macro}
         :neq        {:raw "~="}
         :for-object {:macro #'lua-tf-for-object :emit :macro}
         :for-array  {:macro #'lua-tf-for-array  :emit :macro}
         :for-iter   {:macro #'lua-tf-for-iter   :emit :macro}
        :for-index  {:macro #'lua-tf-for-index  :emit :macro}
        :defgen     {:macro #'lua-tf-defgen     :emit :macro}
         :yield      {:macro #'lua-tf-yield      :emit :macro}
         :prototype-get       {:emit :alias :raw 'getmetatable}
         :prototype-set       {:emit :alias :raw 'setmetatable}
         :prototype-create    {:macro #'lua-tf-prototype-create  :emit :macro
                               :op-spec {:allow-blocks true}}
         :prototype-method    {:macro #'lua-tf-prototype-method  :emit :macro}
         :prototype-tostring  {:emit :unit  :default "__tostring"}})
      (grammar/build:override fn/+lua+)
      (grammar/build:extend
       {:cat    {:op :cat    :symbol '#{cat}       :raw ".."   :emit :infix}
        :len    {:op :len    :symbol '#{len}       :raw "#"    :emit  :pre}
        :incby  {:op :incby  :symbol '#{:+=}       :macro  #'lua-tf-incby :emit :macro}
        :decby  {:op :decby  :symbol '#{:-=}       :macro  #'lua-tf-decby :emit :macro}
        :mulby  {:op :mulby  :symbol '#{:*=}       :macro  #'lua-tf-mulby :emit :macro}
        :local  {:op :local  :symbol '#{local var} :macro  #'lua-tf-local :emit :macro}
        :c-ffi  {:op :c-ffi  :symbol '#{%.c}       :macro  #'lua-tf-c-ffi :emit :macro}
        :repeat {:op :repeat
                 :symbol '#{repeat} :type :block
                 :block {:raw "repeat"
                         :main    #{:body}
                         :control [[:until {:required true
                                            :input #{:parameter}}]]}}})))

(def +template+
  (->> {:banned #{:keyword :set :regex}
        :allow   {:assign  #{:symbol :quote}}
        :highlight '#{return break local tab until}
        :default {:comment   {:prefix "--"}
                  :common    {:apply ":" :statement ""
                              :namespace-full "___"
                              :namespace-sep  "_"}
                  :index     {:offset 1  :end-inclusive true}
                  :return    {:multi true}
                  :block     {:parameter {:start " " :end " "}
                              :body      {:start "" :end ""}}
                  :function  {:raw "function"
                              :body      {:start "" :end "end"}}
                  :infix     {:if  {:check "and" :then "or"}}
                  :global    {:reference nil}}
        :token  {:nil       {:as "nil"}
                 :string    {:quote :single}}
        :data   {:map-entry {:start ""  :end ""  :space "" :assign "=" :keyword :symbol
                             :key-fn #'lua-map-key}
                 :vector    {:start "{" :end "}" :space ""}}
        :rewrite {:staging [#'rewrite/lua-rewrite-stage]}
        :block  {:for       {:body    {:start "do" :end "end"}}
                 :while     {:body    {:start "do" :end "end"}}
                 :branch    {:wrap    {:start "" :end "end"}
                             :control {:default {:parameter  {:start " " :end " then"}
                                                 :body {:append true}}
                                       :if      {:raw "if"}
                                       :elseif  {:raw "elseif"}
                                       :else    {:raw "else"}}}
                 :repeat    {:body  {:start "" :end ""}}}
        :function {:defn      {:raw "local function"}}
        :define   {:def       {:raw "local"}
                   :defglobal {:raw ""}
                   :declare   {:raw "local"}}}
       (collection/merge-nested (emit/default-grammar))))

(defn lua-module-link
  "gets the absolute lua based module
   
   (lua-module-link 'kmi.common {:root-ns 'kmi.hello})
   => \"./common\"
  
   (lua-module-link 'kmi.exchange
                    {:root-ns 'kmi :target \"src\"})
   => \"./kmi/exchange\""
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

(defn lua-module-export
  "outputs the js module export form"
  {:added "4.0"}
  ([module mopts]
   (let [table  (->> (module/module-entries module
                                            #{:defn
                                              :def})
                     (cons 'tab))]
     (list 'return table))))


(def +meta+
  (book/book-meta
   {:module-current f/NIL
    :module-link    #'lua-module-link
    :module-export  #'lua-module-export
    :module-import  (fn [name {:keys [as]} opts]  
                      (template/$ (var* :local ~as := (require ~(str name)))))
    :has-ptr        (fn [ptr] (list 'not= (ut/sym-full ptr) nil))
    :teardown-ptr   (fn [ptr] (list := (ut/sym-full ptr) nil))}))

(def +grammar+
  (grammar/grammar :lua
    (grammar/to-reserved +features+)
    +template+))

(defn variant-meta
  "merges variant metadata onto base lua metadata"
  {:added "4.1"}
  [m]
  (book/book-meta
   (collection/merge-nested +meta+ m)))

(defn variant-grammar
  "merges variant feature overrides onto base lua grammar"
  {:added "4.1"}
  [m]
  (grammar/grammar :lua
    (grammar/to-reserved
     (collection/merge-nested +features+ m))
    +template+))

(def +book+
  (book/book {:lang :lua
              :parent :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
