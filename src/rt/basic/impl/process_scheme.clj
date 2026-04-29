(ns rt.basic.impl.process-scheme
  (:require [clojure.string :as str]
            [rt.basic.type-basic :as basic]
            [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [std.lang.base.runtime :as rt]
            [std.lang.model.spec-scheme :as spec]))

(def +scheme-init+
  (common/put-program-options
   :scheme {:default {:oneshot :racket
                      :basic   :racket}
            :env     {:racket {:exec  "racket"
                               :flags {:oneshot ["-e"]
                                       :basic   ["-e"]}}}}))

(defn- scheme-bootstrap
  [body-form]
  (str/join
   "\n"
   ["(begin"
    "  (require json racket/string racket/tcp)"
    "  (define XT_NAMESPACE (make-base-namespace))"
    "  (parameterize ([current-namespace XT_NAMESPACE])"
    "    (namespace-require 'racket/string))"
    "  (define (xt-json-normalize x)"
    "    (cond"
    "      [(null? x) 'null]"
    "      [(vector? x)"
    "       (list->vector (map xt-json-normalize (vector->list x)))]"
    "      [(hash? x)"
    "       (for/hasheq ([(k v) (in-hash x)])"
    "         (values (cond"
    "                   [(keyword? k) (keyword->string k)]"
    "                   [(symbol? k) (symbol->string k)]"
    "                   [else k])"
    "                 (xt-json-normalize v)))]"
    "      [(list? x)"
    "       (map xt-json-normalize x)]"
    "      [else x]))"
    "  (define (return-encode out id key)"
    "    (with-handlers ([exn:fail?"
    "                     (lambda (_)"
    "                       (jsexpr->string"
    "                        (hasheq 'type \"raw\""
    "                                'value (format \"~a\" out))))])"
    "      (jsexpr->string"
    "       (hasheq 'id id"
    "               'key key"
    "               'type \"data\""
    "               'value (xt-json-normalize out)))))"
    "  (define (xt-read-all s)"
    "    (call-with-input-string"
    "     s"
    "     (lambda (in)"
    "       (let loop ([forms '()])"
    "         (let ([form (read in)])"
    "           (if (eof-object? form)"
    "             (reverse forms)"
    "             (loop (cons form forms))))))))"
    "  (define (xt-eval-source s)"
    "    (parameterize ([current-namespace XT_NAMESPACE])"
    "      (for/fold ([result 'null]) ([form (in-list (xt-read-all s))])"
    "        (eval form))))"
    "  (define (return-wrap thunk)"
    "    (with-handlers ([exn:fail?"
    "                     (lambda (e)"
    "                       (jsexpr->string"
    "                        (hasheq 'type \"error\""
    "                                'value (exn-message e))))])"
    "      (return-encode (thunk) 'null 'null)))"
    "  (define (return-eval s)"
    "    (return-wrap (lambda () (xt-eval-source s))))"
    "  (define (client-basic host port opts)"
    "    (define-values (in out) (tcp-connect host port))"
    "    (let loop ()"
    "      (define line (read-line in 'any))"
    "      (unless (eof-object? line)"
    "        (unless (string=? line \"<PING>\")"
    "          (displayln (return-eval (string->jsexpr line)) out)"
    "          (flush-output out))"
    "        (loop))))"
    (str "  " body-form)
    ")"]))

(def ^{:arglists '([body])}
  default-oneshot-wrap
  (fn [body]
    (scheme-bootstrap
     (str "(display (return-eval " (pr-str body) "))"))))

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (fn [port & [{:keys [host]}]]
    (scheme-bootstrap
     (format "(client-basic %s %s '())"
             (pr-str (or host "127.0.0.1"))
             port))))

(def +scheme-oneshot-config+
  (common/set-context-options
   [:scheme :oneshot :default]
   {:main {:in #'default-oneshot-wrap}
    :json :full}))

(def +scheme-oneshot+
  [(rt/install-type!
    :scheme :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

(def +scheme-basic-config+
  (common/set-context-options
   [:scheme :basic :default]
   {:bootstrap #'default-basic-client
    :main      {}
    :json      :full
    :encode    :json
    :timeout   2000}))

(def +scheme-basic+
  [(rt/install-type!
    :scheme :basic
    {:type :hara/rt.basic
     :instance {:create #'basic/rt-basic:create}
     :config {:layout :full}})])
