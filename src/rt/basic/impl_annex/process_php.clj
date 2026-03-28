(ns rt.basic.impl-annex.process-php
  (:require [clojure.string]
            [rt.basic.type-basic :as basic]
            [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as rt]
            [std.lang.model-annex.spec-php :as spec]
            [xt.lang.base-repl :as k]))

(def +php-init+
  (common/put-program-options
   :php  {:default  {:oneshot    :php
                     :basic      :php}
          :env      {:php    {:exec   "php"
                              :flags  {:oneshot   ["-r"]
                                       :basic     ["-r"]}}}}))

;;
;; ONESHOT
;;

(def ^{:arglists '([body])}
  default-oneshot-wrap
  (let [bootstrap  (impl/emit-entry-deps
                    k/return-eval
                    {:lang :php
                     :layout :flat})]
    (fn [body]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :php [(list 'echo (list 'return-eval body))])))))

(def +php-oneshot-config+
  (common/set-context-options
   [:php :oneshot :default]
   {:main  {:in    #'default-oneshot-wrap}
    :emit  {:body  {:transform #'rt/return-transform}}
    :json :full}))

(def +php-oneshot+
  [(rt/install-type!
    :php :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

;;
;; BASIC
;;

(def +client-basic+
  '[(defn client-basic
      [host port opts]
      (let [conn (fsockopen host port)]
         (while (not (feof conn))
            (let [line (fgets conn)
                  input (json_decode line)
                  out   (return-eval input)]
               (if input
                   (fwrite conn (. (json_encode out) "\n")))))))])

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (let [bootstrap (->> [(impl/emit-entry-deps
                         k/return-eval
                         {:lang :php
                          :layout :flat})
                        (impl/emit-as
                         :php +client-basic+)]
                       (clojure.string/join "\n\n"))]
    (fn [port & [{:keys [host]}]]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :php [(list 'client-basic
                       (or host "127.0.0.1")
                       port
                       {})])))))

(def +default-basic-config+
  {:bootstrap #'default-basic-client
    :main   {}
   :emit   {:body  {:transform #'rt/return-transform}
            :lang/format :global}
   :json   :full
   :encode :json ;; default
   :timeout 2000})

(def +php-basic-config+
  (common/set-context-options
   [:php :basic :default]
   +default-basic-config+))

(def +php-basic+
  [(rt/install-type!
    :php :basic
    {:type :hara/rt.basic
     :instance {:create #'basic/rt-basic:create}
     :config {:layout :full}})])
