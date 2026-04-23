(ns rt.basic.impl-annex.process-perl
  (:require [clojure.string]
            [rt.basic.type-basic :as basic]
            [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as rt]
            [std.lang.model-annex.spec-perl :as spec]
            [xt.lang.common-lib :as lib]))

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

(defn perl-body-wrap
  "wraps forms for Perl basic eval - flat do block, CORE::eval returns last value"
  {:added "4.0"}
  [forms]
  (apply list 'do forms))

(defn default-basic-body-transform
  "transform basic forms for Perl - uses flat statements, no function wrapper"
  {:added "4.0"}
  [input mopts]
  (rt/return-transform
   input mopts
   {:wrap-fn perl-body-wrap
    :format-fn identity}))

(def ^{:arglists '([body])}
  default-oneshot-wrap
  (let [bootstrap  (impl/emit-entry-deps
                    lib/return-eval
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
  '[(:- "use IO::Socket::INET;")
    (:- "use JSON::PP;")
    (:- "$| = 1;")
    (defn client-basic [host port opts]
      (:- "my $conn = IO::Socket::INET->new(PeerHost => $host, PeerPort => $port, Proto => 'tcp') or die \"Cannot connect: $!\";")
      (:- "while (my $line = <$conn>) {")
      (:- "  chomp $line;")
      (:- "  my $input = decode_json($line);")
      (:- "  my $out = return_eval($input);")
      (:- "  print $conn $out . \"\\n\";")
      (:- "}"))])

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (let [return-eval-src (impl/emit-entry-deps
                         lib/return-eval
                          {:lang :perl
                           :layout :flat})]
    (fn [port & [{:keys [host]}]]
      (str return-eval-src
            "\n\n"
           (impl/emit-as
            :perl (concat +client-basic+
                          [(list 'client-basic
                                 (or host "127.0.0.1")
                                 port
                                 {})]))))))

(def +default-basic-config+
  {:bootstrap #'default-basic-client
    :main   {}
   :emit   {:body  {:transform #'default-basic-body-transform}
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
