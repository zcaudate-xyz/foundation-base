(ns rt.basic.impl.process-r
  (:require [std.lang.base.runtime :as default]
            [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [rt.basic.type-basic :as basic]
            [std.lang.model.spec-r :as spec]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as rt]
            [std.json :as json]
            [std.lib :as h]
            [std.string :as str]
            [xt.lang.base-repl :as k]))

(def +program-init+
  (common/put-program-options
   :r   {:default  {:oneshot      :rlang
                    :basic        :rlang
                    :interactive  :rlang}
         :env      {:rlang     {:exec    "r"
                                :output  {}
                                :flags   {:oneshot ["-s" "-e"]
                                          :basic ["-s" "-e"]
                                          :interactive ["-i"]
                                          :json ["rjsonlite" :instal]}}}}))

(def ^{:arglists '([body])}
  default-oneshot-wrap
  (let [bootstrap (->> ["library(jsonlite)"
                        (impl/emit-entry-deps
                         k/return-eval
                         {:lang :r
                          :layout :flat})]
                       (str/join "\n\n"))]
    (fn [body]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :r [(list 'toString (list 'return-eval body))])))))

(defn default-oneshot-trim
  "trim for oneshot
 
   (default-oneshot-trim \"[1] \\\"1\\\"\")
   => \"1\""
  {:added "4.0"}
  [s]
  (json/read (subs s 4)))

(def +r-oneshot-config+
  (common/set-context-options
   [:r :oneshot :default]
   {:main  {:in  #'default-oneshot-wrap
            :out #'default-oneshot-trim}
    :emit  {:body  {:transform #'default/return-transform}}
    :json :full}))

(def +r-oneshot+
  [(rt/install-type!
    :r :oneshot
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
          (var conn  := (socketConnection :port port :blocking true))
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
  (let [bootstrap (->> ["library(jsonlite)"
                        (impl/emit-entry-deps
                         k/return-eval
                         {:lang :r
                          :layout :flat})
                        (impl/emit-as
                         :r +client-basic+)]
                       (str/join "\n\n"))]
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
    :emit  {:body  {:transform #'default/return-transform}}
    :json :full
    :encode :json
    :timeout 2000}))

(def +r-basic+
  [(rt/install-type!
    :r :basic
    {:type :hara/rt.basic
     :instance {:create #'basic/rt-basic:create}
     :config {:layout :full}})])
