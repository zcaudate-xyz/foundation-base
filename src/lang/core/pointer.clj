(ns lang.core.pointer
  (:require [clojure.string]
            [std.json :as json]
            [lang.base.book :as book]
            [lang.base.book-entry :as e]
            [lang.base.book-module :as module]
            [lang.base.emit :as emit]
            [lang.base.emit-common :as common]
            [lang.core.impl :as impl]
            [lang.core.impl-deps :as deps]
            [lang.core.library :as lib]
            [lang.core.library-snapshot :as snap]
            [lang.base.util :as ut]
            [std.lib.collection :as collection]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [std.lib.os :as os]))

(defn- default-rt-wrap [f]
  (fn [rt input]
    (f rt input)))

;; controls whether to copy a form
(def ^:dynamic *clip* nil)

;; controls whether to copy a form
(def ^:dynamic *rt-wrap* #'default-rt-wrap)

;; controls whether to print a form
(def ^:dynamic *print* #{})

;; controls whether free-form scripts include global initializers
(def ^:dynamic *global-init* false)

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

(defmacro with:global-init
  "includes global initializers in free-form scripts"
  {:added "4.1"}
  ([& body]
   `(binding [*global-init* true]
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

(defn free-form
  "normalizes free-pointer body forms into a canonical pointer form"
  {:added "4.1"}
  [body]
  (let [forms (if (vector? body) body (vec body))]
    (case (count forms)
      0 '(do)
      1 (first forms)
      (apply list 'do forms))))

(defn free-form-body
  "returns the body forms for a canonical free-pointer form"
  {:added "4.1"}
  [form]
  (cond (nil? form)
        []

        (and (collection/form? form)
             (= 'do (first form)))
        (rest form)

        :else
        [form]))

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
   (let [source-lang lang
         library     (or library (impl/runtime-library))
         display-ns  (or (:namespace meta) (env/ns-sym))
         runtime     (or (:runtime meta)
                         (when-not (:lang meta)
                           (f/suppress (ut/lang-rt-default ptr display-ns))))
         target-lang (or (:lang meta) (:lang runtime) source-lang)
         meta        (assoc meta
                            :lang target-lang
                            :module module
                            :library library
                            :namespace display-ns)]
     (letfn [(abstract-error? [t]
               (when t
                 (or (= :abstract (:emit (ex-data t)))
                     (abstract-error? (.getCause ^Throwable t)))))
             (source-form [entry]
               (or (:form entry) (:form-input entry)))
             (display-source [entry]
               (when-let [form (source-form entry)]
                 (env/pp-str form)))
             (emit-with-fallback [emit-fn entry]
               (try
                 (emit-fn)
                 (catch Throwable t
                   (if (abstract-error? t)
                     (or (display-source entry)
                         (throw t))
                     (throw t)))))]
       (cond form
             (emit-with-fallback #(impl/emit-str form meta)
                                 {:form form})

             (not id)
             (cond->  "<free"
               module (str ":" module)
               :then (str ">"))

             :else
             (let [entry (cond-> (get-entry ptr)
                           (not= :fragment section)
                           (or (book/get-code-entry-view (lib/get-book library source-lang)
                                                        (ut/sym-full ptr))))]
               (cond (nil? entry)
                     (str (ptr-tag ptr :not-found))

                     (= :fragment section)
                     (let [{:keys [form standalone template]} entry]
                       (emit-with-fallback
                        #(cond (collection/form? standalone)
                               (clojure.string/trim (with-out-str (clojure.pprint/pprint (second standalone))))

                               (symbol? form)
                               (str form)

                               (not template)
                               (impl/emit-str form meta)

                              :else
                              (let [args (second form)]
                                (clojure.string/trim (with-out-str (clojure.pprint/pprint
                                                         (or (f/suppress (list 'fn:> args (apply template args)))
                                                             form))))))
                        entry))
                     :else
                     (emit-with-fallback #(impl/emit-entry entry meta)
                                         entry))))))))

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
        module   (module/resolve-module-view
                  book
                  (or module
                      (:module ptr)))
        
        module   (if-let [internal (-> emit :runtime :module/internal)]
                   (-> module
                       (update :link merge (collection/transpose internal))
                       (update :internal merge internal))
                    module)]
    (assoc meta
            :lang lang
            :book book
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
  (let [meta (ptr-invoke-meta ptr meta)
        global-init? (get ptr :global-init *global-init*)]
    (binding [impl/*print-form* (:input-form *print*)]
      (cond (:form ptr)
            (let [meta (if global-init?
                         (assoc (merge {:layout :full} meta)
                                :global-init true)
                         meta)]
              (if global-init?
                (impl/emit-script (:form ptr) meta)
                (impl/emit-str (:form ptr) meta)))

            (:id ptr)
            (let [entry @ptr]
              (if (= :defrun (:op-key entry))
                (impl/emit-str (apply list 'do
                                      (drop 2 (or (:form-input entry)
                                                  (:form entry))))
                               meta)
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
                                         meta))
        meta (cond-> meta
               (:form ptr) (assoc :global-init
                                  (get ptr :global-init *global-init*)))]
    (binding [impl/*print-form* (:input-form *print*)]
      (cond (:form ptr)
            (impl/emit-script (:form ptr) meta)

            (:id ptr)
            (let [entry @ptr]
              (if (= :defrun (:op-key entry))
                (impl/emit-script (apply list 'do
                                         (drop 2 (or (:form-input entry)
                                                     (:form entry))))
                                  meta)
                (impl/emit-script (apply list ptr args) meta)))
            
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
  ([out]
   (ptr-output-json out nil))
  ([out {:keys [preserve-errors preserve-payload]}]
   (let [{:strs [type value return]} out
         type (collection/unseqify type)
         out (case type
               "data"  (if preserve-payload
                         out
                         value)
               "raw"   (if preserve-payload
                         out
                         (f/wrapped value identity return))
               "error" (if preserve-errors
                         out
                         (throw (if (map? value)
                                  (ex-info "" value)
                                  (ex-info "" {:err (vec (filter not-empty
                                                                 (clojure.string/split-lines (str value))))}))))
               out)
         _    (when (:output *print*)
                (env/pl out))]
     out)))

(defn ptr-output
  "output types for embedded return values"
  {:added "4.0"}
  ([out json]
   (ptr-output out json nil))
  ([out json {:keys [preserve-errors preserve-payload] :as opts}]
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
                              (f/wrapped out)))]
               (if (:json *output*)
                 out
                 (if (and (= json :full)
                          (not (f/wrapped? out)))
                   (ptr-output-json out opts)
                   out)))
             
             out)))))

(defn ptr-invoke
  "invokes the pointer"
  {:added "4.0"}
  [rt raw-eval body main json]
  (let [in-fn   (fn [body]
                  (cond-> body (:in main)  ((:in main))))
        out-fn  (fn [body]
                  (cond-> body (:out main) ((:out main))))
        _    (when *clip*
               (os/clip body))
        _    (when (:input *print*)
               (env/pl body))]
    (cond (not-empty *input*)
          (cond-> body
            (:raw *input*) in-fn)
          
          :else
          (let [input (in-fn body)
                _    (when (:raw-input *print*)
                       (env/local :println
                                "\n"
                                (env/pl-add-lines input)
                                "\n"))
                raw-eval (if *rt-wrap*
                           (*rt-wrap* raw-eval)
                           raw-eval)
                raw   (try (out-fn (raw-eval rt input))
                           (catch Throwable t
                             (when common/*explode*
                               (env/prn :OUTPUT-ERROR t))
                             (throw t)))
                _    (when (:raw-output *print*)
                       (try (let [data (json/read raw)
                                  {:strs [type value]}  data]
                              (cond (= type "error")
                                    (doseq [[k v] value]
                                      (env/pl (str k ":\n" v)))))
                            (catch Throwable t
                              (env/pl raw))))
                output (ptr-output raw
                                   json
                                   {:preserve-payload (or (:output/preserve-payload rt)
                                                          (get-in rt [:process :output/preserve-payload]))
                                    :preserve-errors  (or (:output/preserve-errors rt)
                                                          (get-in rt [:process :output/preserve-errors]))})]
            output))))
