(ns rt.basic.impl.process-elisp
  (:require [clojure.string :as str]
            [rt.basic.type-basic :as basic]
            [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [std.lang.base.runtime :as rt]
            [std.lang.model.spec-elisp :as spec]))

(def +elisp-init+
  (common/put-program-options
   :elisp {:default {:oneshot :emacs
                     :basic   :emacs}
           :env     {:emacs {:exec  "emacs"
                             :flags {:oneshot ["--quick" "--batch" "--eval"]
                                     :basic   ["--quick" "--batch" "--eval"]}}}}))

(defn- elisp-bootstrap
  [body-form]
  (str/join
   "\n"
   ["(progn"
    "  (require 'json)"
    "  (defun xt-json-normalize (x)"
    "    (cond"
    "      ((null x) :null)"
    "      ((vectorp x)"
    "       (vconcat (mapcar #'xt-json-normalize x)))"
    "      ((hash-table-p x)"
    "       (let ((out (make-hash-table :test 'equal)))"
    "         (maphash (lambda (k v)"
    "                    (puthash k (xt-json-normalize v) out))"
    "                  x)"
    "         out))"
    "      ((listp x)"
    "       (mapcar (lambda (v)"
    "                 (if (consp v)"
    "                   (cons (car v) (xt-json-normalize (cdr v)))"
    "                   (xt-json-normalize v)))"
    "               x))"
    "      (t x)))"
    "  (defun xt-read-all (s)"
    "    (let ((idx 0)"
    "          (forms nil)"
    "          item)"
    "      (condition-case nil"
    "          (while t"
    "            (setq item (read-from-string s idx))"
    "            (setq idx (cdr item))"
    "            (push (car item) forms))"
    "        (end-of-file nil))"
    "      (nreverse forms)))"
    "  (defun xt-eval-source (s)"
    "    (let ((result :null))"
    "      (dolist (form (xt-read-all s) result)"
    "        (setq result (eval form)))))"
    "  (defun return-encode (out id key)"
    "    (condition-case nil"
    "        (json-serialize"
    "         `((id . ,id)"
    "           (key . ,key)"
    "           (type . \"data\")"
    "           (value . ,(xt-json-normalize out))))"
    "      (error"
    "       (json-serialize"
    "        `((type . \"raw\")"
    "          (value . ,(format \"%S\" out)))))))"
    "  (defun return-wrap (f)"
    "    (condition-case err"
    "        (return-encode (funcall f) :null :null)"
    "      (error"
    "       (json-serialize"
    "        `((type . \"error\")"
    "          (value . ,(error-message-string err)))))))"
    "  (defun return-eval (s)"
    "    (return-wrap (lambda () (xt-eval-source s))))"
    "  (defun xt-client-filter (proc chunk)"
    "    (let ((buffer (concat (or (process-get proc 'xt-buffer) \"\") chunk)))"
    "      (while (string-match \"\\n\" buffer)"
    "        (let ((line (substring buffer 0 (match-beginning 0))))"
    "          (setq buffer (substring buffer (match-end 0)))"
    "          (unless (string= line \"<PING>\")"
    "            (process-send-string"
    "             proc"
    "             (concat"
    "              (return-eval"
    "               (json-parse-string line :null-object :null :false-object :false))"
    "              \"\\n\")))))"
    "      (process-put proc 'xt-buffer buffer)))"
    "  (defun client-basic (host port opts)"
    "    (let ((proc (open-network-stream \"xt-elisp-basic\" nil host port)))"
    "      (set-process-query-on-exit-flag proc nil)"
    "      (set-process-coding-system proc 'utf-8-unix 'utf-8-unix)"
    "      (set-process-filter proc #'xt-client-filter)"
    "      (while (process-live-p proc)"
    "        (accept-process-output proc 1))"
    "      proc))"
    (str "  " body-form)
    ")"]))

(def ^{:arglists '([body])}
  default-oneshot-wrap
  (fn [body]
    (elisp-bootstrap
     (str "(princ (return-eval " (pr-str body) "))"))))

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (fn [port & [{:keys [host]}]]
    (elisp-bootstrap
     (format "(client-basic %s %s nil)"
             (pr-str (or host "127.0.0.1"))
             port))))

(def +elisp-oneshot-config+
  (common/set-context-options
   [:elisp :oneshot :default]
   {:main {:in #'default-oneshot-wrap}
    :json :full}))

(def +elisp-oneshot+
  [(rt/install-type!
    :elisp :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

(def +elisp-basic-config+
  (common/set-context-options
   [:elisp :basic :default]
   {:bootstrap #'default-basic-client
    :main      {}
    :json      :full
    :encode    :json
    :timeout   2000}))

(def +elisp-basic+
  [(rt/install-type!
    :elisp :basic
    {:type :hara/rt.basic
     :instance {:create #'basic/rt-basic:create}
     :config {:layout :full}})])
