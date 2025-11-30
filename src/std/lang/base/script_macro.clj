(ns std.lang.base.script-macro
  (:require [std.lang.base.util :as ut]
            [std.lang.base.library :as lib]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.emit :as emit]
            [std.lang.base.grammar-spec :as grammar]
            [std.lang.base.impl :as impl]
            [std.lang.base.impl-entry :as entry]
            [std.lang.base.script-annex :as annex]
            [std.lang.base.script-control :as control]
            [std.lang.base.script-lint :as lint]
            [std.lang.base.pointer :as ptr]
	    [std.lib :as h]))

(def +form-allow+ [:line :column :file :name :ns])

(defn body-arglists
  "makes arglists from body"
  {:added "4.0"}
  [body]
  (let [args-fn (fn [body]
                  (vec (remove (fn [x]
                                 (or (keyword? x)
                                     (number? x)
                                     (and (vector? x)
                                          (not (some symbol? x)))
                                     (string? x)))
                               (first body))))]
    (if (vector? (first body))
      (list (args-fn body))
      (map args-fn body))))

(defn intern-in
  "interns a macro"
  {:added "4.0"}
  ([prefix tag val & [namespace]]
   (intern (or namespace (h/ns-sym))
           (with-meta (symbol (str prefix (name tag)))
             {:macro true
              :arglists '([& body])})
           val)))

(defn intern-prep
  "outputs the module and form meta"
  {:added "4.0"}
  [lang form]
  (let [fmeta  (meta form)
        module (or (:module fmeta)
                   (let [rt (ut/lang-rt lang)]
                     (:module rt)))
        _      (assert (boolean module) "Module required for insertion.")]
    [module fmeta]))

(defn intern-def$-fn
  "interns a fragment macro"
  {:added "4.0"}
  ([lang [op sym body :as form] smeta]
   (let [[module fmeta] (intern-prep lang form)
         entry  (entry/create-fragment form
                                       (merge smeta
                                              fmeta
                                              {:lang lang
                                               :module module}))
         lib    (impl/runtime-library)
         _      (lib/add-entry-single! lib entry)]
     (ptr/ptr-intern (:namespace entry)
                     (with-meta sym (merge (select-keys fmeta +form-allow+)
                                           smeta))
                     entry))))

(defn intern-def$
  "intern a def fragment macro"
  {:added "4.0"}
  ([lang tag]
   (intern-in
    "def$." tag
    (fn [&form &env sym body]
      `(intern-def$-fn ~lang
                       (quote ~&form)
                       (merge ~(meta sym)
                              {:time (h/time-ns)}))))))

(defn intern-defmacro-fn
  "function to intern a macro"
  {:added "4.0"}
  ([lang [op sym & body :as form] smeta]
   (let [[doc attr body] (grammar/format-fargs body)
         [module fmeta]  (intern-prep lang form)
         entry  (entry/create-macro (apply list 'defmacro sym body)
                                    (merge smeta
                                           fmeta
                                           {:lang lang
                                            :module module}))
         lib    (impl/runtime-library)
         _      (lib/add-entry-single! lib entry)]
     (ptr/ptr-intern (:namespace entry)
                     (with-meta sym (merge attr
                                           {:doc doc
                                            :arglists (body-arglists body)}
                                           (select-keys fmeta +form-allow+)
                                           smeta))
                     entry))))

(defn intern-defmacro
  "the intern macro function"
  {:added "4.0"}
  ([lang tag]
   (intern-in
    "defmacro." tag
    (fn [&form &env sym & body]
      `(intern-defmacro-fn ~lang
                           (quote ~&form)
                           (merge ~(meta sym)
                                  {:time (h/time-ns)}))))))

(defn call-thunk
  "calls the thunk given meta to control pointer output
   
   (macro/call-thunk {:debug true}
                     (fn [] ptr/*print*))
   => #{:input}"
  {:added "4.0"}
  [{:keys [input raw debug clip] :as meta}
   thunk]
  (binding [ptr/*print* (if (= (:tag meta) *)
                          #{:input-form :raw-input :raw-output}
                          (cond-> ptr/*print* debug (conj :input)))
            ptr/*output* (cond-> ptr/*output*
                           raw (conj :raw))
            ptr/*input* (if (= (:tag meta) -)
                          #{:input}
                          (cond-> ptr/*input*
                            input (conj :input)))
            ptr/*clip* (boolean clip)]
    (thunk)))

(defn intern-!-fn
  "interns a free pointer macro"
  {:added "4.0"}
  [lang args {:keys [input raw debug clip] :as meta}]
  (let [{:keys [module] :as rt} (ut/lang-rt lang)]
    (call-thunk meta
                (fn []
                  (h/p:rt-invoke-ptr
                   rt
                   (ut/lang-pointer lang {:module module}) args)))))

(defn intern-!
  "interns a macro for free evalutation"
  {:added "4.0"}
  ([lang tag]
   (intern-in
    "!." tag
    (fn [&form _ & body]
      `(intern-!-fn ~lang
                    (quote ~(vec body))
                    ~(meta &form))))))

(defn intern-free-fn
  "interns a free pointer in the namespace
   
   (macro/intern-free-fn :lua '(defptr.lua hello 1)
                         {})
   => #'std.lang.base.script-macro-test/hello"
  {:added "4.0"}
  [lang [_ sym body] smeta]
  (let [rt (ut/lang-rt lang)
        module (or (:module smeta) 
                   (:module rt))
        ptr (ut/lang-pointer lang {:module module
                                   :form body})]
    (intern (h/ns-sym) sym ptr)))

(defn intern-free
  "creates a defptr macro
 
   (macro/intern-free :lua \"hello\")
   => #'std.lang.base.script-macro-test/defptr.hello"
  {:added "4.0"}
  ([lang tag]
   (intern-in
    "defptr." tag
    (fn [&form _ & body]
      `(intern-free-fn ~lang (quote ~&form)
                       ~(meta &form))))))

(defn intern-top-level-fn
  "interns a top level function"
  {:added "4.0"}
  ([lang [op reserved] [_ sym & body :as form-raw] smeta]
   (let [{:keys [format section priority]} reserved
         [module fmeta] (intern-prep lang form-raw)
         form          (apply list op sym body)
         
         [tmeta entry] (binding [preprocess/*macro-splice* (:static/template smeta)]
                         (entry/create-code-raw form
                                                reserved
                                                (merge smeta
                                                       fmeta
                                                       {:lang lang
                                                        :module module})))
         lib    (impl/runtime-library)
         sym    (cond (vector? sym)
                      (first (filter symbol? sym))
                      
                      :else sym)
         var    (ptr/ptr-intern (:namespace entry)
                                (with-meta sym (merge tmeta
                                                      (if (:fn reserved)
                                                        {:arglists (body-arglists
                                                                    (drop 2 (:form-input entry)))})
                                                      (select-keys fmeta +form-allow+)
                                                      smeta))
                                entry)
         rt     (ut/lang-rt-default @var)
         _      (lib/add-entry-single! lib entry)
         _      (comment "TODO: USE BULK ENTRY ADD"
                  (if (= :single (:lang/add-entry rt))
                    (lib/add-entry-single! lib entry)
                    (lib/add-entry! lib entry)))
         init   (h/p:rt-init-ptr rt entry)
         module (lib/get-module lib
                                (:lang entry)
                                (:module entry))
         
         ;;
         ;;     LINT HACK FOR JS
         ;;
         #_#_
         _      (when (and (#{:js :xtalk :lua} (:lang entry))
                           (not (:static/no-lint smeta)))
                  (lint/lint-entry (lib/get-entry lib entry)
                                   module))]
     (if init
       init
       var))))

(defn intern-top-level
  "interns a top level macro
 
   (impl/with:library [+library+]
     (macro/intern-top-level :lua \"hello\" 'def))
   => #'std.lang.base.script-macro-test/def.hello
 
   (impl/with:library [+library+]
     ^{:module L.core}
     (def.hello abc 1))
   => #'std.lang.base.script-macro-test/abc
   
   (impl/with:library [+library+]
     (ptr/ptr-deref abc))
 
   (impl/with:library [+library+]
     (ptr/ptr-display abc {}))
   => \"def abc = 1;\""
  {:added "4.0"}
  ([lang tag op]
   (let [lib  (impl/runtime-library)
         {:keys [grammar]} (lib/get-book lib lang)]
     (intern-top-level lang tag op grammar)))
  ([lang tag op grammar]
   (let [reserved (or (get-in grammar [:reserved op])
                      (h/error "Not found" {:op op
                                            :tag tag
                                            :lang lang}))]
     (intern-in
      (str op ".") tag
      (fn [&form &env sym & body]
        `(intern-top-level-fn ~lang
                              (quote [~op ~reserved])
                              (quote ~&form)
                              (merge (quote ~(meta sym))
                                     {:time (h/time-ns)})))))))

(defn intern-macros
  "interns the top-level macros in the grammar"
  {:added "4.0"}
  ([lang grammar]
   (let [{:keys [macros reserved tag]} grammar
         fvar   (intern-def$ lang tag)
         mvar   (intern-defmacro lang tag)
         evar   (intern-! lang tag)
         pvar   (intern-free lang tag)
         tvars  (mapv (fn [op] (intern-top-level lang tag op grammar))
                      macros)]
     [[fvar mvar evar pvar] tvars])))

(defn intern-highlights
  "interns the highlight macros in the grammar"
  {:added "4.0"}
  ([lang grammar]
   (let [{:keys [highlight reserved]} grammar]
     (vec (keep (fn [sym]
                  (let [op (get reserved sym)]
                    (intern (h/ns-sym)
                            (with-meta sym (merge {:macro true}
                                                  (select-keys op [:arglists :style/indent])))
                            (fn [_ _ & args]))))
                highlight)))))

(defn intern-grammar
  "interns a bunch of macros in the namespace
 
   (:macros (:grammar (lib/get-book +library+ :lua)))
   => '#{defrun defn defglobal defgen defn- deftemp defclass defabstract def}
   
   (impl/with:library [+library+]
     (macro/intern-grammar :lua (:grammar (lib/get-book +library+ :lua))))
   => map?"
  {:added "4.0"}
  [lang grammar]
  (let [lib  (impl/runtime-library)
        {:keys [grammar]} (lib/get-book lib lang)
        m  {:macros     (intern-macros lang grammar)
            :highlights (intern-highlights lang grammar)}]
    (lib/wait-mutate!
     lib
     (fn [snapshot]
       [m (update-in snapshot [lang :book] merge m)]))))


(defn intern-defmacro-rt-fn
  "defines both a library entry as well as a runtime macro"
  {:added "4.0"}
  [lang form smeta]
  (let [{:static/keys [wrap]
         :rt/keys [default]
         :or {default :oneshot}} smeta
        v (intern-defmacro-fn lang
                              form
                              smeta)
        
        vptr (intern *ns* (with-meta (symbol (str  (.sym ^clojure.lang.Var v)
                                                   ":ptr"))
                            (meta v))
                     @v)
        vmacro (fn [_ _ & args]
                 (let [form (list `control/script-rt-oneshot
                                  default
                                  (h/var-sym vptr)
                                  (mapv (fn [x]
                                          (list 'quote x))
                                        args))]
                   (cond->> form
                     wrap (list wrap))))
        _  (alter-meta! v merge {:macro true})
        _  (alter-var-root v (fn [_] vmacro))]
    [v vptr]))

(defmacro defmacro.!
  "macro for runtime lang macros"
  {:added "4.0"}
  [sym args & body]
  (let [{:static/keys [lang]} (meta sym)]
    `(intern-defmacro-rt-fn ~lang
                            (quote ~&form)
                            (merge ~(meta sym)
                                   {:time (h/time-ns)}))))

(comment
  (h/p:space-context-unset (ut/lang-context :bash))
  (std.lang/with-trace
    (std.lang/with:input
        (rt.shell/man:ptr :man))))
