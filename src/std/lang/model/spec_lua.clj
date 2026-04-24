(ns std.lang.model.spec-lua
  (:require [clojure.string]
            [std.fs :as fs]
            [std.lang.base.book :as book]
            [std.lang.base.book-module :as module]
            [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as common]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.grammar-spec :as spec]
            [std.lang.base.impl :as impl]
            [std.lang.base.script :as script]
            [std.lang.base.util :as ut]
            [std.lang.model.spec-xtalk]
            [std.lang.model.spec-xtalk.fn-lua :as fn]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.template :as template]))

;;
;; LANG
;;

(defn tf-local
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

(defn tf-c-ffi
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

(defn tf-for-object
  "for object transform"
  {:added "4.0"}
  [[_ [[k v] m] & body]]
  (apply list 'for [[k v] :in (list 'pairs m)]
         body))

(defn tf-for-array
  "for array transform"
  {:added "4.0"}
  [[_ [e arr] & body]]
  (if (vector? e)
    (apply list 'for [e :in (list 'ipairs arr)]
           body)
    (apply list 'for [['_ e] :in (list 'ipairs arr)]
           body)))

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
  (apply list 'for [i := (list 'quote [start end (or step 1)])]
           body))

(defn tf-for-return
  "for return transform"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error final]}]]
  (if (and (seq? statement)
           (= 'x:return-run (first statement)))
    (let [[_ runner] statement]
      (template/$ (do (var ~res nil)
                      (var ~err nil)
                      (~runner
                       (fn [value]
                         (:= ~res value)
                         (:= ~err nil))
                       (fn [value]
                         (:= ~res nil)
                         (:= ~err value)))
                      (if (not ~err)
                        ~(if final (list 'return success) success)
                        ~(if final (list 'return error) error)))))
    (template/$ (do (var '[~res ~err] ~statement)
                    (if (not ~err)
                      ~(if final (list 'return success) success)
                      ~(if final (list 'return error) error))))))

(defn tf-for-try
  "for try transform"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error]}]]
  (template/$ (do (var '[ok out] (pcall (fn []
                                   (return ~statement))))
           (if ok
             (do* (var ~res := out)
                  ~@(if success [success]))
             (do* (var ~err := out)
                  ~@(if error [error]))))))

(defn tf-for-async
  "for async transform"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error finally]}]]
  (template/$ (x:thread-spawn
        (fn []
          (for:try [[~res ~err] ~statement]
                   {:success ~success
                     :error ~error})
          ~@(if finally [finally])))))

(defn tf-yield
  "yield transform"
  {:added "4.0"}
  [[_ e]]
  (list 'coroutine.yield e))

(defn tf-defgen
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
        :neq        {:raw "~="}
        :for-object {:macro #'tf-for-object :emit :macro}
        :for-array  {:macro #'tf-for-array  :emit :macro}
        :for-iter   {:macro #'tf-for-iter   :emit :macro}
        :for-index  {:macro #'tf-for-index  :emit :macro}
        :for-return {:macro #'tf-for-return :emit :macro}
        :for-try    {:macro #'tf-for-try    :emit :macro}
        :for-async  {:macro #'tf-for-async  :emit :macro}
        :defgen     {:macro #'tf-defgen     :emit :macro}
        :yield      {:macro #'tf-yield      :emit :macro}
        :prototype-get       {:emit :alias :raw 'getmetatable}
        :prototype-set       {:emit :alias :raw 'setmetatable}
        :prototype-create    {:macro #'lua-tf-prototype-create  :emit :macro}
        :prototype-tostring  {:emit :unit  :default "__tostring"}})
      (grammar/build:override fn/+lua+)
      (grammar/build:extend
       {:cat    {:op :cat    :symbol '#{cat}       :raw ".."   :emit :infix}
        :len    {:op :len    :symbol '#{len}       :raw "#"    :emit  :pre}
        :local  {:op :local  :symbol '#{local var} :macro  #'tf-local :emit :macro}
        :c-ffi  {:op :c-ffi  :symbol '#{%.c}       :macro  #'tf-c-ffi :emit :macro}
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

(comment
  (lib/get-book (impl/default-library) :lua)

  (!.lua
   (let [#{a} hello
         b 2]))
  
  (!.lua
   (defgen hello []
     (yield n)))
  (!.lua (x:offset))
  (!.lua (x:random))

  
  
  (./create-tests))
