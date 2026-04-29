(ns rt.basic.impl.process-scheme
  (:require [clojure.string :as str]
            [rt.basic.type-basic :as basic]
            [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [std.lang.base.runtime :as rt]
            [std.lang.model.spec-scheme :as spec]))

(defn scheme-root
  []
  (or (System/getenv "PWD")
      (System/getProperty "user.dir")))

(def +scheme-init+
  (common/put-program-options
   :scheme (let [root (scheme-root)]
             {:default {:oneshot :racket
                        :basic   :racket}
              :env     {:racket {:exec  "racket"
                                 :root  root
                                 :env   {"PWD" root}
                                 :flags {:oneshot ["-e"]
                                         :basic   ["-e"]}}}})))

(defn- scheme-bootstrap
  [body-form]
  (str/join
   "\n"
      ["(begin"
      "  (require json racket/file racket/format racket/path racket/port racket/string racket/system racket/tcp racket/vector)"
      (str "  (define __xt_root__ " (pr-str (scheme-root)) ")")
      "  (current-directory __xt_root__)"
      "  (define XT_NAMESPACE (make-base-namespace))"
      "  (parameterize ([current-namespace XT_NAMESPACE])"
      "    (namespace-require 'json)"
      "    (namespace-require 'racket/file)"
      "    (namespace-require 'racket/format)"
      "    (namespace-require 'racket/path)"
      "    (namespace-require 'racket/port)"
      "    (namespace-require 'racket/string)"
      "    (namespace-require 'racket/system)"
      "    (namespace-require 'racket/vector)"
      "    (namespace-require 'racket/tcp)"
     "    (eval '(define __xt_globals__ (make-hash)))"
     "    (eval '(define-syntax-rule (while test body ...)"
     "             (let loop ()"
     "               (when test"
     "                 body ..."
     "                 (loop))))))"
     "    (eval '(define (xt-type-native x)"
     "             (cond"
     "               [(null? x) \"nil\"]"
     "               [(string? x) \"string\"]"
     "               [(number? x) \"number\"]"
     "               [(boolean? x) \"boolean\"]"
     "               [(procedure? x) \"function\"]"
     "               [(vector? x) \"array\"]"
     "               [(hash? x) \"object\"]"
     "               [(list? x) \"list\"]"
     "               [else \"unknown\"])))"
     "    (eval '(define (xt-json-normalize x)"
     "             (cond"
     "               [(null? x) 'null]"
     "               [(vector? x)"
     "                (map xt-json-normalize (vector->list x))]"
     "               [(hash? x)"
     "                (for/hasheq ([(k v) (in-hash x)])"
     "                  (values (cond"
     "                            [(keyword? k) (string->symbol (keyword->string k))]"
     "                            [(string? k) (string->symbol k)]"
     "                            [(symbol? k) k]"
     "                            [else k])"
     "                          (xt-json-normalize v)))]"
     "               [(list? x)"
     "                (map xt-json-normalize x)]"
     "               [else x])))"
     "    (eval '(define (xt-return-encode out id key)"
     "             (let ([ts (xt-type-native out)])"
     "               (with-handlers ([exn:fail?"
     "                                (lambda (_)"
     "                                  (jsexpr->string"
     "                                   (hasheq 'id (xt-json-normalize id)"
     "                                           'key (xt-json-normalize key)"
     "                                           'type \"raw\""
     "                                           'return ts"
     "                                           'value (format \"~a\" out))))])"
     "                 (jsexpr->string"
     "                  (hasheq 'id (xt-json-normalize id)"
     "                          'key (xt-json-normalize key)"
     "                          'type \"data\""
     "                          'return ts"
     "                          'value (xt-json-normalize out)))))))"
     "  (define (xt-json-normalize x)"
     "    (cond"
     "      [(null? x) 'null]"
     "      [(vector? x)"
      "       (map xt-json-normalize (vector->list x))]"
    "      [(hash? x)"
     "       (for/hasheq ([(k v) (in-hash x)])"
     "         (values (cond"
     "                   [(keyword? k) (string->symbol (keyword->string k))]"
     "                   [(string? k) (string->symbol k)]"
     "                   [(symbol? k) k]"
     "                   [else k])"
     "                 (xt-json-normalize v)))]"
     "      [(list? x)"
     "       (map xt-json-normalize x)]"
     "      [else x]))"
     "  (define (xt-type-native x)"
     "    (cond"
     "      [(null? x) \"nil\"]"
     "      [(string? x) \"string\"]"
     "      [(number? x) \"number\"]"
     "      [(boolean? x) \"boolean\"]"
     "      [(procedure? x) \"function\"]"
     "      [(vector? x) \"array\"]"
     "      [(hash? x) \"object\"]"
     "      [(list? x) \"list\"]"
     "      [else \"unknown\"]))"
      "  (define (xt-return-encode out id key)"
      "    (let ([ts (xt-type-native out)])"
      "      (with-handlers ([exn:fail?"
      "                       (lambda (_)"
      "                         (jsexpr->string"
     "                          (hasheq 'id (xt-json-normalize id)"
     "                                  'key (xt-json-normalize key)"
     "                                  'type \"raw\""
     "                                  'return ts"
     "                                  'value (format \"~a\" out))))])"
      "        (jsexpr->string"
      "         (hasheq 'id (xt-json-normalize id)"
      "                 'key (xt-json-normalize key)"
      "                 'type \"data\""
      "                 'return ts"
      "                 'value (xt-json-normalize out))))))"
      "  (define (xt-promise-native? value)"
      "    (and (vector? value)"
      "         (= 3 (vector-length value))"
      "         (equal? \"__xt_promise__\" (vector-ref value 0))))"
      "  (define (xt-promise-resolved value)"
      "    (vector \"__xt_promise__\" \"resolved\" value))"
      "  (define (xt-promise-rejected err)"
      "    (vector \"__xt_promise__\" \"rejected\" err))"
      "  (define (xt-promise-wrap value)"
      "    (if (xt-promise-native? value)"
      "      value"
      "      (xt-promise-resolved value)))"
      "  (define (xt-promise thunk)"
      "    (with-handlers ([(lambda (e) #t)"
      "                     (lambda (e)"
      "                       (xt-promise-rejected e))])"
      "      (xt-promise-wrap (thunk))))"
      "  (define (xt-promise-all promises)"
      "    (xt-promise-resolved"
      "     (list->vector"
      "      (map (lambda (promise)"
      "             (let ([out (xt-promise-wrap promise)])"
      "               (if (xt-promise-native? out)"
      "                 (vector-ref out 2)"
      "                 out)))"
      "           (if (vector? promises)"
      "             (vector->list promises)"
      "             promises)))))"
      "  (define (xt-promise-then promise thunk)"
      "    (let ([out (xt-promise-wrap promise)])"
      "      (if (and (xt-promise-native? out)"
      "               (equal? \"rejected\" (vector-ref out 1)))"
      "        out"
      "        (with-handlers ([(lambda (e) #t)"
      "                         (lambda (e)"
      "                           (xt-promise-rejected e))])"
      "          (xt-promise-wrap"
      "           (thunk (if (xt-promise-native? out)"
      "                    (vector-ref out 2)"
      "                    out)))))))"
      "  (define (xt-promise-catch promise thunk)"
      "    (let ([out (xt-promise-wrap promise)])"
      "      (if (and (xt-promise-native? out)"
      "               (equal? \"rejected\" (vector-ref out 1)))"
      "        (with-handlers ([(lambda (e) #t)"
      "                         (lambda (e)"
      "                           (xt-promise-rejected e))])"
      "          (xt-promise-wrap (thunk (vector-ref out 2))))"
      "        out)))"
      "  (define (xt-promise-finally promise thunk)"
      "    (let ([out (xt-promise-wrap promise)])"
      "      (with-handlers ([(lambda (e) #t)"
      "                       (lambda (e)"
      "                         (xt-promise-rejected e))])"
      "        (thunk)"
      "        out)))"
      "  (define (xt-with-delay ms thunk)"
      "    (sleep (/ ms 1000.0))"
      "    (xt-promise thunk))"
      "  (namespace-set-variable-value! 'xt-type-native xt-type-native #t XT_NAMESPACE)"
      "  (namespace-set-variable-value! 'xt-json-normalize xt-json-normalize #t XT_NAMESPACE)"
      "  (namespace-set-variable-value! 'xt-return-encode xt-return-encode #t XT_NAMESPACE)"
      "  (namespace-set-variable-value! 'xt-promise xt-promise #t XT_NAMESPACE)"
      "  (namespace-set-variable-value! 'xt-promise-all xt-promise-all #t XT_NAMESPACE)"
      "  (namespace-set-variable-value! 'xt-promise-then xt-promise-then #t XT_NAMESPACE)"
      "  (namespace-set-variable-value! 'xt-promise-catch xt-promise-catch #t XT_NAMESPACE)"
      "  (namespace-set-variable-value! 'xt-promise-finally xt-promise-finally #t XT_NAMESPACE)"
      "  (namespace-set-variable-value! 'xt-promise-native? xt-promise-native? #t XT_NAMESPACE)"
      "  (namespace-set-variable-value! 'xt-with-delay xt-with-delay #t XT_NAMESPACE)"
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
     "      (xt-return-encode (thunk) 'null 'null)))"
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
