(ns hara.runtime.basic.impl-annex.process-r
  (:require [clojure.string]
            [hara.runtime.basic.type-basic :as basic]
            [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-oneshot :as oneshot]
            [hara.runtime.basic.type-verify :as type-verify]
            [hara.lang.impl :as impl]
            [hara.lang.runtime :as rt]
            [hara.model.annex.spec-r :as spec]
            [xt.lang.common-lib :as lib]))

(def +program-init+
  (common/put-program-options
   :r   {:default  {:oneshot      :rlang
                    :verify       :rscript
                    :basic        :rlang
                    :interactive  :rlang}
         :env      {:rlang     {:exec    "R"
                                :output  {}
                                :flags   {:oneshot ["--vanilla" "-s" "-e"]
                                          :basic ["--vanilla" "-s" "-e"]
                                          :interactive ["--vanilla" "-i"]
                                          :json ["rjsonlite" :instal]}}
                    :rscript   {:exec    "Rscript"
                                :extension "R"
                                :flags   {:verify ["--vanilla" "-e"
                                                   "tryCatch(parse(\"__FILE__\"), error=function(e) quit(status=1))"]}}}}))

(def ^{:arglists '([body])}
  default-oneshot-wrap
  (let [bootstrap (->> ["suppressPackageStartupMessages(library(jsonlite))"
                        (impl/emit-entry-deps
                         lib/return-eval
                         {:lang :r
                          :layout :flat})]
                       (clojure.string/join "\n\n"))]
    (fn [body]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :r [(list 'cat (list 'return-eval body))])))))

(defn default-oneshot-trim
  "trim for oneshot
 
   (default-oneshot-trim \"{\\\"type\\\":\\\"data\\\",\\\"return\\\":\\\"number\\\",\\\"value\\\":1}\")
   => \"{\\\"type\\\":\\\"data\\\",\\\"return\\\":\\\"number\\\",\\\"value\\\":1}\""
  {:added "4.0"}
  [s]
  (->> (clojure.string/split-lines (str s))
       (map clojure.string/trim)
       (filter not-empty)
       last))

(defn- flatten-body-forms
  "flattens the top-level script container (a vector or `do` block)
   into a sequence of body forms without touching nested data literals.
 
   A vector arriving here is a single data-literal form (the runtime already
   normalised multi-form vectors to `do`). Single-element vectors are
   unwrapped so that bulk-wrapped oneshot args become the single body form."
  [input]
  (cond (vector? input)
        (if (= 1 (count input))
          [(first input)]
          [input])

        (and (seq? input)
             ('#{do do*} (first input)))
        (vec (rest input))

        :else
        [input]))

(defn- mark-local-defn
  "rewrites top-level function definitions in an immediate eval block to
   simple `(def ^{:inner true} sym (fn ...))` assignments so they are not
   qualified with the module namespace."
  [form]
  (if (and (seq? form)
           ('#{defn defn-} (first form)))
    (let [[op sym args & body] form
          sym (with-meta sym {:inner true})]
      (list 'def sym (apply list 'fn args body)))
    form))

(defn default-body-transform
  "standard R body transform
 
   Ensures multi-form scripts are treated as a sequence rather than a
   single vector, so function definitions can appear in top-level blocks.
   The last form is wrapped in an explicit `return` for consistent
   cross-runtime behaviour."
  {:added "4.0"}
  [input mopts]
  (let [forms (->> (rt/normalize-body-forms input mopts)
                   (map mark-local-defn))
        forms (if (empty? forms)
                [nil]
                forms)]
    (list (apply list 'fn [] (rt/return-format forms)))))

(def +r-oneshot-config+
  (common/set-context-options
   [:r :oneshot :default]
   {:main  {:in  #'default-oneshot-wrap
            :out #'default-oneshot-trim}
    :emit  {:body  {:transform #'default-body-transform}}
    :json :full}))

(def +r-verify-config+
  (common/set-context-options
   [:r :verify :default]
   {:main    {}
    :emit    {}
    :json    false
    :exec-fn #'type-verify/verify-exec-file}))

(def +r-oneshot+
  [(rt/install-type!
    :r :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

(def +r-verify+
  [(rt/install-type!
    :r :verify
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

;;
;;
;;

(def +client-basic+
  '[(defn client-basic
      [host port opts]
      (while true
        (while true
          (var conn  := (socketConnection :host host
                                          :port port
                                          :blocking true))
          (tryCatch
           (block
            (while true
              (var line := (readLines conn :n 1))
              (cond (== line "<PING>") (next)
                    
                    :else
                    (writeLines (return-eval (fromJSON line)) conn :sep "\n"))))
           :error   (fn [err])))))])


(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (let [root      (or (System/getenv "PWD")
                      (System/getProperty "user.dir"))
        bootstrap (->> [(str "setwd(" (pr-str root) ")")
                         "suppressPackageStartupMessages(library(jsonlite))"
                         (impl/emit-entry-deps
                          lib/return-eval
                          {:lang :r
                           :layout :flat})
                         (impl/emit-as
                         :r +client-basic+)]
                       (clojure.string/join "\n\n"))]
    (fn [port & [{:keys [host]}]]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :r [(list 'client-basic
                      (or host "127.0.0.1")
                      port
                      {})])))))

(def +r-basic-config+
  (common/set-context-options
   [:r :basic :default]
   {:bootstrap #'default-basic-client
     :main  {}
     :container {:image "foundation-base/rt-basic-r:latest"}
     :container-backup true
     :emit  {:body  {:transform #'default-body-transform}}
     :json :full
     :encode :json
     :timeout 2000}))

(def +r-basic+
  [(rt/install-type!
    :r :basic
    {:type :hara/rt.basic
     :instance {:create #'basic/rt-basic:create}
     :config {:layout :full}})])
