(ns rt.basic.impl.process-c
  (:require [std.lang.model.spec-c :as spec]
            [std.lang.base.impl :as impl]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.runtime :as rt]
            [std.lang.base.pointer :as ptr]
            [std.lib :as h]
            [std.string :as str]
            [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [rt.basic.type-twostep :as twostep]))

(def +program-init+
  (common/put-program-options
   :c  {:default  {:oneshot     :tcc
                   :twostep     :gcc
                   :interactive false
                   :ws-client   false}
        :env      {:tcc    {:exec "tcc"
                            :pipe true
                            :stderr true
                            :flags  {:oneshot ["-run" "-"]
                                     :interactive false
                                     :json false
                                     :ws-client false}}
                   :gcc    {:exec "gcc"
                            :extension   "c"
                            :stderr true
                            :flags  {:twostep []
                                     :interactive false
                                     :json false
                                     :ws-client false}
                            :output-flag "-o"}}}))

(def FORMAT
  {[:char]   "%c"
   [:short]  "%d"
   [:int]    "%d"
   [:long]   "%d"
   [:long :long] "%d"
   [:double] "%f"
   [:float] "%f"
   [:long :double] "%f"
   [:char :*] "\"%s\""})

(defn get-format-string
  "gets the format string given entry"
  {:added "4.0"}
  [entry]
  (let [return-type (:- (meta (second (:form entry))))]
    (or (get FORMAT return-type)
        "\"%s\"")))

(defn transform-form-format
  "formats the form"
  {:added "4.0"}
  [form opts]
  (let [ptr (-> opts :emit :input :pointer)
        {:keys [book]} opts
        sym (and (h/form? form)
                 (first form))]
    (cond (and (symbol? sym)
               (namespace sym))
          (let [s (get-format-string (book/get-entry book sym))]
            (list 'printf s form))
          
          
          (:id ptr)
          (let [s (get-format-string (ptr/get-entry ptr))
                args (-> opts :emit :input :args)]
            (list 'printf s (apply list form args)))

          :else
          form)))

(defn transform-form
  "transforms the form for tcc output"
  {:added "4.0"}
  [forms opts]
  (let [forms (if (symbol? (first forms))
                [forms]
                forms)
        body (concat
              '[do]
              (butlast forms)
              [(transform-form-format
                (last forms)
                opts)])]
    `(:- "#include <stdio.h>\nint main(){\n "
         ~body
         "\nreturn 0;\n}")))

(def +c-oneshot-config+
  (common/set-context-options
   [:c :oneshot :default]
   {:emit  {:body  {:transform #'transform-form}}
    :json :string}))

(def +c-oneshot+
  [(rt/install-type!
    :c :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

(def +c-twostep-config+
  (common/set-context-options
   [:c :twostep :default]
   {:emit  {:body  {:transform #'transform-form}}
    #_#_
    :json :string}))

(def +c-twostep+
  [(rt/install-type!
    :c :twostep
    {:type :hara/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])
  
(comment
  (./create-tests))
