(ns std.lang.base.pointer
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as common]
            [std.lang.base.impl :as impl]
            [std.lang.base.impl-deps :as deps]
            [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.book :as book]
            [std.lang.base.book-entry :as e]
            [std.lang.base.util :as ut]
            [std.string :as str]
            [std.json :as json]
            [std.lib :as h]))

(defn- default-rt-wrap [f]
  (fn [rt input]
    (f rt input)))

;; controls whether to copy a form
(def ^:dynamic *clip* nil)

;; controls whether to copy a form
(def ^:dynamic *rt-wrap* #'default-rt-wrap)

;; controls whether to print a form
(def ^:dynamic *print* #{})

;; controls whether to shortcut at the input string
(def ^:dynamic *input* #{})

;; controls returning the raw output
(def ^:dynamic *output* #{:full})

(defmacro with:clip
  "form to control `clip` option"
  {:added "4.0"}
  ([& body]
   `(binding [*clip* true]
      ~@body)))

(defmacro ^{:style/indent 0}
  with:print
  "form to control `print` option"
  {:added "4.0"}
  ([& body]
   (let [[params & body] (if (and (vector? (first body))
                                  (not-empty (first body)))
                           body
                         (cons #{:input} body))]
     `(binding [*print* ~(set params)]
        ~@body))))

(defmacro ^{:style/indent 0}
  with:print-all
  "toggles print for all intermediate steps"
  {:added "4.0"}
  ([& body]
   `(binding [*print* #{:input-form
                        :raw-input
                        :raw-output}]
      ~@body)))

(defmacro with:rt-wrap
  "wraps an additional function to the invoke function"
  {:added "4.0"}
  ([[f] & body]
   `(binding [*rt-wrap* ~f]
      ~@body)))

(defmacro with:rt
  "forcibly applies a runtime"
  {:added "4.0"}
  ([[rt] & body]
   `(binding [std.lib.context.pointer/*runtime* ~rt]
      ~@body)))

(defmacro ^{:style/indent 0}
  with:input
  "form to control `input` option"
  {:added "4.0"}
  ([& body]
   (let [[params & body] (if (and (vector? (first body))
                                  (not-empty (first body)))
                           body
                           (cons #{:default} body))]
     `(binding [*input* ~(set params)]
        ~@body))))

(defmacro with:raw
  "form to control `raw` option"
  {:added "4.0"}
  ([& body]
   `(binding [*output* #{:raw}]
      ~@body)))

(defn get-entry
  "gets the library entry given pointer"
  {:added "4.0"}
  ([{:keys [lang id module section library runtime] :as ptr}]
   (let [library (or library (impl/runtime-library))]
     (lib/get-entry library ptr))))

;;
;;
;;

(defn ptr-tag
  "creates a tag for the pointer"
  {:added "4.0"}
  ([ptr tag]
   (vec (cons tag [(ut/sym-full ptr) (:section ptr)]))))

(defn ptr-deref
  "gets the entry or the free pointer data"
  {:added "4.0"}
  ([ptr]
   (let [{:keys [lang module id]} ptr]
     (if (and lang module id)
       (get-entry ptr)
       (into {} ptr)))))

(defn ptr-display
  "emits the display string for pointer"
  {:added "4.0"}
  ([{:keys [lang form id module section library] :as ptr} meta]
   (let [meta  (assoc meta
                      :lang lang
                      :module module
                      :library (or library (impl/runtime-library)))]
     (cond form
           (impl/emit-str form meta)

           (not id)
           (cond->  "<free"
             module (str ":" module)
             :then (str ">"))
           
           :else
           (let [entry (get-entry ptr)]
             (cond (nil? entry)
                   (str (ptr-tag ptr :not-found))
                   
                   (= :fragment section)
                   (let [{:keys [form standalone template]} entry]
                     (cond (h/form? standalone)
                           (str/trim (with-out-str (clojure.pprint/pprint (second standalone))))
                           
                           (not template)
                           (impl/emit-str form meta)
                           
                           :else
                           (let [args (second form)]
                             (str/trim (with-out-str (clojure.pprint/pprint
                                                      (or (h/suppress (list 'fn:> args (apply template args)))
                                                          form)))))))
                   :else
                   (impl/emit-entry entry meta)))))))

(defn ptr-invoke-meta
  "prepares the meta for a pointer"
  {:added "4.0"}
  [{:keys [library] :as ptr} {:keys [module
                                        emit]
                                 :as meta}]
  (let [lang     (or (:lang meta)
                     (:lang ptr))
        library  (or library (impl/runtime-library))
        snapshot (cond-> (lib/get-snapshot library)
                   module (-> (snap/set-module module) second)) 
        book     (snap/get-book snapshot lang)
        module   (or module
                     (get-in book [:modules (:module ptr)]))
        
        module   (if-let [internal (-> emit :runtime :module/internal)]
                   (assoc module
                          :link (h/transpose internal)
                          :internal internal)
                   module)]
    (assoc meta
           :lang lang
           :snapshot snapshot
           :module module)))

(defn rt-macro-opts
  "creates the default macro-opts for a runtime"
  {:added "4.0"}
  [lang-or-runtime]
  (let [{:keys [lang module] :as rt} (if (keyword? lang-or-runtime)
                                       (ut/lang-rt lang-or-runtime)
                                       lang-or-runtime)]
    (ptr-invoke-meta (ut/lang-pointer lang
                                      {:module module})
                     (select-keys rt [:lang :layout :emit]))))

(defn ptr-invoke-string
  "emits the invoke string"
  {:added "4.0"}
  [ptr args meta]
  (let [meta (ptr-invoke-meta ptr meta)]
    (binding [impl/*print-form* (:input-form *print*)]
      (cond (:id ptr)
            (let [entry (-> (snap/get-book (:snapshot meta)
                                           :lang)
                            
                            (book/get-base-entry (:module ptr)
                                                 (:id ptr)
                                                 (:section ptr)))]
              (if (= :defrun (:op-key entry))
                (impl/emit-str (apply list 'do (drop 2 (:form @ptr))) meta)
               (impl/emit-str (apply list ptr args) meta)))
            
            :else
            (impl/emit-str (with-meta (vec args)
                             {:bulk true})
                           meta)))))

(defn ptr-invoke-script
  "emits a script with dependencies"
  {:added "4.0"}
  [ptr args meta]
  (let [meta (ptr-invoke-meta ptr (merge {:layout :full}
                                         meta))]
    (binding [impl/*print-form* (:input-form *print*)]
      (cond (:form ptr)
            (impl/emit-script (:form ptr) meta)

            (:id ptr)
            (if (= :defrun (:op-key @ptr))
              (impl/emit-script (apply list 'do (drop 2 (:form @ptr))) meta)
              (impl/emit-script (apply list ptr args) meta))
            
            :else
            (impl/emit-script (with-meta (vec args)
                                {:bulk true})
                              meta)))))

(defn ptr-intern
  "interns the symbol into the workspace environment"
  {:added "4.0"}
  [ns name {:keys [lang id module section form template standalone] :as m}]
  (let [ks [:lang :id :module :section]
        ptr (ut/lang-pointer lang (select-keys m ks))]
    (intern ns name ptr)))

(defn ptr-output-json
  "extracetd function from ptr-output"
  {:added "4.0"}
  [out]
  (let [{:strs [type value return]} out
        type (h/unseqify type)
        out (case type
              "data"    value
              "raw"     (h/wrapped value identity return)
              "error"   (throw (if (map? value)
                                 (ex-info "" value)
                                 (ex-info "" {:err (vec (filter not-empty
                                                                (str/split-lines (str value))))})))
              out)
        _    (when (:output *print*)
               (h/pl out))]
    out))

(defn ptr-output
  "output types for embedded return values"
  {:added "4.0"}
  [out json]
  (cond (:raw *output*) out

        (or (var? json)
            (fn? json)) (json out)
        
        :else
        (let [out (if (= json :string)
                    (str out)
                    out)]
          (if (and (string? out)
                   json)
            
            (let [out (try (json/read out)
                           (catch Throwable t
                             (h/wrapped out)))]
              (if (:json *output*)
                out
                (if (and (= json :full)
                         (not (h/wrapped? out)))
                  (ptr-output-json out)
                  out)))
            
            out))))

(defn ptr-invoke
  "invokes the pointer"
  {:added "4.0"}
  [rt raw-eval body main json]
  (let [in-fn   (fn [body]
                  (cond-> body (:in main)  ((:in main))))
        out-fn  (fn [body]
                  (cond-> body (:out main) ((:out main))))
        _    (when *clip*
               (h/clip body))
        _    (when (:input *print*)
               (h/pl body))]
    (cond (not-empty *input*)
          (cond-> body
            (:raw *input*) in-fn)
          
          :else
          (let [input (in-fn body)
                _    (when (:raw-input *print*)
                       (h/local :println
                                "\n"
                                (h/pl-add-lines input)
                                "\n"))
                raw-eval (if *rt-wrap*
                           (*rt-wrap* raw-eval)
                           raw-eval)
                raw   (try (out-fn (raw-eval rt input))
                           (catch Throwable t
                             (when common/*explode*
                               (h/prn :OUTPUT-ERROR t))
                             (throw t)))
                _    (when (:raw-output *print*)
                       (try (let [data (json/read raw)
                                  {:strs [type value]}  data]
                              (cond (= type "error")
                                    (doseq [[k v] value]
                                      (h/pl (str k ":\n" v)))))
                            (catch Throwable t
                              (h/pl raw))))
                output (ptr-output raw json)]
            output))))

