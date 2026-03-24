(ns rt.basic.impl.process-perl
  (:require [clojure.string]
            [rt.basic.type-basic :as basic]
            [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as rt]
            [std.lang.model.spec-perl :as spec]
            [xt.lang.base-repl :as k]))

(def +perl-init+
  (common/put-program-options
   :perl  {:default  {:oneshot    :perl
                      :basic      :perl}
           :env      {:perl    {:exec   "perl"
                                :flags  {:oneshot   ["-e"]
                                         :basic     ["-e"]}}}}))

;;
;; ONESHOT
;;

(defn default-body-transform
  "transform oneshot forms for `return-eval`"
  {:added "4.0"}
  [input {:keys [bulk]}]
  (if bulk
    (apply list 'do input)
    input))

(def ^{:arglists '([body])}
  default-oneshot-wrap
  (let [bootstrap  (impl/emit-entry-deps
                    k/return-eval
                    {:lang :perl
                     :layout :flat})]
    (fn [body]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :perl [(list 'print (list 'return-eval body))])))))

(def +perl-oneshot-config+
  (common/set-context-options
   [:perl :oneshot :default]
   {:main  {:in    #'default-oneshot-wrap}
    :emit  {:body  {:transform #'default-body-transform}}
    :json :full}))

(def +perl-oneshot+
  [(rt/install-type!
    :perl :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

;;
;; BASIC
;;

(def +client-basic+
  (let [io-socket (symbol "IO::Socket::INET->new")]
    [(list 'defn 'client-basic
           ['host 'port 'opts]
           '(:- "use IO::Socket::INET;")
           '(:- "use JSON::PP;")
           (list 'let ['conn (list io-socket
                                   {:PeerHost 'host
                                    :PeerPort 'port
                                    :Proto "tcp"})]
                 '(while (my $line = (<$conn>))
                    (let [input (decode_json $line)
                          out   (return-eval input)]
                      (print $conn (encode_json out) "\n")))))]))

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (let [bootstrap (->> [(impl/emit-entry-deps
                         k/return-eval
                         {:lang :perl
                          :layout :flat})
                        (impl/emit-as
                         :perl +client-basic+)]
                       (clojure.string/join "\n\n"))]
    (fn [port & [{:keys [host]}]]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :perl [(list 'client-basic
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

(def +perl-basic-config+
  (common/set-context-options
   [:perl :basic :default]
   +default-basic-config+))

(def +perl-basic+
  [(rt/install-type!
    :perl :basic
    {:type :hara/rt.basic
     :instance {:create #'basic/rt-basic:create}
     :config {:layout :full}})])
