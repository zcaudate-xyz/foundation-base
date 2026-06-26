(ns hara.runtime.basic.impl.process-elisp
  (:require [clojure.string :as str]
            [hara.runtime.basic.type-basic :as basic]
            [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-oneshot :as oneshot]
            [hara.runtime.basic.type-verify :as type-verify]
            [hara.lang.runtime :as rt]
            [hara.model.spec-elisp :as spec]))

(defn elisp-root
  []
  (or (System/getenv "PWD")
      (System/getProperty "user.dir")))

(def +elisp-init+
  (common/put-program-options
   :elisp (let [root (elisp-root)]
            {:default {:oneshot :emacs
                       :verify  :emacs
                       :basic   :emacs}
             :env     {:emacs {:exec  "emacs"
                               :root  root
                               :env   {"PWD" root}
                               :extension "el"
                               :flags {:oneshot ["--quick" "--batch" "--eval"]
                                       :verify  ["--quick" "--batch" "--eval" "(progn (require 'bytecomp) (batch-byte-compile-file \"__FILE__\"))"]
                                       :basic   ["--quick" "--batch" "--eval"]}}}})))

(defn- elisp-bootstrap
  [body-form]
  (str/join
   "\n"
      ["(progn"
       "  (require 'json)"
       "  (require 'seq)"
       "  (require 'subr-x)"
       "  (require 'calc)"
       "  (require 'url)"
       "  (require 'url-http)"
       (str "  (defconst __xt_root__ " (pr-str (elisp-root)) ")")
       "  (setq default-directory (file-name-as-directory __xt_root__))"
       "  (defvar __xt_globals__ (make-hash-table :test 'equal))"
     "  (defun __xt_xor__ (a b)"
     "    (if a"
     "      b"
     "      (not b)))"
     "  (defun __xt_break_throw__ ()"
     "    (throw :__xt_break__ nil))"
     "  (defun xt-json-normalize (x)"
     "    (cond"
     "      ((null x) :null)"
     "      ((vectorp x)"
     "       (vconcat (mapcar (lambda (v)"
     "                         (if (null v)"
     "                           :false"
     "                           (xt-json-normalize v)))"
     "                       x)))"
    "      ((hash-table-p x)"
    "       (let ((out (make-hash-table :test 'equal)))"
    "         (maphash (lambda (k v)"
    "                    (puthash k (xt-json-normalize v) out))"
    "                  x)"
    "         out))"
    "      ((listp x)"
     "       (mapcar (lambda (v)"
     "                 (cond"
     "                   ((null v) :false)"
     "                   ((consp v)"
     "                    (cons (car v) (xt-json-normalize (cdr v)))"
     "                    )"
     "                   (t"
     "                    (xt-json-normalize v))))"
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
     "        (setq result (eval form t)))))"
     "  (defun xt-promise-create ()"
     "    (vector \"__xt_promise__\" \"pending\" nil nil nil nil))"
     "  (defun xt-proto-create (m)"
     "    m)"
     "  (defun xt-proto-get (obj)"
     "    (gethash \"_xt_proto\" obj))"
     "  (defun xt-proto-set (obj proto)"
     "    (puthash \"_xt_proto\" proto obj)"
     "    obj)"
     "  (defun xt-proto-method (obj key)"
     "    (let ((direct (gethash key obj)))"
     "      (if (null direct)"
     "          (let ((proto (gethash \"_xt_proto\" obj)))"
     "            (if (null proto)"
     "                nil"
     "              (gethash key proto)))"
     "        direct)))"
     "  (defun xt-promise-native-p (value)"
     "    (and (vectorp value)"
     "         (= 6 (length value))"
     "         (equal \"__xt_promise__\" (aref value 0))))"
     "  (defun xt-promise-resolve! (promise value)"
     "    (when (equal (aref promise 1) \"pending\")"
     "      (aset promise 1 \"resolved\")"
     "      (aset promise 2 value)"
     "      (dolist (cb (reverse (or (aref promise 3) nil)))"
     "        (funcall cb value))"
     "      (dolist (cb (reverse (or (aref promise 5) nil)))"
     "        (funcall cb))"
     "      (aset promise 3 nil)"
     "      (aset promise 4 nil)"
     "      (aset promise 5 nil))"
     "    promise)"
     "  (defun xt-promise-reject! (promise err)"
     "    (when (equal (aref promise 1) \"pending\")"
     "      (aset promise 1 \"rejected\")"
     "      (aset promise 2 err)"
     "      (dolist (cb (reverse (or (aref promise 4) nil)))"
     "        (funcall cb err))"
     "      (dolist (cb (reverse (or (aref promise 5) nil)))"
     "        (funcall cb))"
     "      (aset promise 3 nil)"
     "      (aset promise 4 nil)"
     "      (aset promise 5 nil))"
     "    promise)"
     "  (defun xt-promise-bind-result (promise result)"
     "    (if (xt-promise-native-p result)"
     "        (progn"
     "          (xt-promise-then"
     "           result"
     "           (lambda (value)"
     "             (xt-promise-resolve! promise value)))"
     "          (xt-promise-catch"
     "           result"
     "           (lambda (err)"
     "             (xt-promise-reject! promise err)))"
     "          promise)"
     "      (xt-promise-resolve! promise result)))"
     "  (defun xt-promise (thunk)"
     "    (let ((promise (xt-promise-create)))"
     "      (condition-case err"
     "          (xt-promise-bind-result promise (funcall thunk))"
     "        (error"
     "         (xt-promise-reject! promise err)))"
     "      promise))"
     "  (defun xt-promise-new (thunk)"
     "    (let ((promise (xt-promise-create)))"
     "      (condition-case err"
     "          (funcall thunk"
     "                   (lambda (value) (xt-promise-resolve! promise value))"
     "                   (lambda (err) (xt-promise-reject! promise err)))"
     "        (error"
     "         (xt-promise-reject! promise err)))"
     "      promise))"
     "  (defun xt-promise-all (promises)"
     "    (let ((next (xt-promise-create))"
     "          (remaining (length promises))"
     "          (index 0)"
     "          (results (make-vector (length promises) nil))"
     "          (done nil))"
     "      (if (equal remaining 0)"
     "          (xt-promise-resolve! next results)"
     "        (while (< index remaining)"
     "          (let* ((current-index index)"
     "                 (current (elt promises index))"
     "                 (promise (if (xt-promise-native-p current)"
     "                              current"
     "                            (xt-promise (lambda () current)))))"
     "            (xt-promise-then"
     "             promise"
     "             (lambda (value)"
     "               (unless done"
     "                 (aset results current-index value)"
     "                 (setq remaining (- remaining 1))"
     "                 (when (equal remaining 0)"
     "                   (setq done t)"
     "                   (xt-promise-resolve! next results)))))"
     "            (xt-promise-catch"
     "             promise"
     "             (lambda (err)"
     "               (unless done"
     "                 (setq done t)"
     "                 (xt-promise-reject! next err)))))"
     "          (setq index (+ index 1))))"
     "      next))"
     "  (defun xt-promise-then (promise thunk)"
     "    (let ((next (xt-promise-create)))"
     "      (let ((on-success (lambda (value)"
     "                          (condition-case err"
     "                              (xt-promise-bind-result next (funcall thunk value))"
     "                            (error"
     "                             (xt-promise-reject! next err)))))"
     "            (on-error (lambda (err)"
     "                        (xt-promise-reject! next err))))"
     "        (cond"
     "          ((equal (aref promise 1) \"resolved\")"
     "           (funcall on-success (aref promise 2)))"
     "          ((equal (aref promise 1) \"rejected\")"
     "           (funcall on-error (aref promise 2)))"
     "          (t"
     "           (aset promise 3 (cons on-success (aref promise 3)))"
     "           (aset promise 4 (cons on-error (aref promise 4)))))"
     "        next)))"
     "  (defun xt-promise-catch (promise thunk)"
     "    (let ((next (xt-promise-create)))"
     "      (let ((on-success (lambda (value)"
     "                          (xt-promise-resolve! next value)))"
     "            (on-error (lambda (err)"
     "                        (condition-case caught"
     "                            (xt-promise-bind-result next (funcall thunk err))"
     "                          (error"
     "                           (xt-promise-reject! next caught))))))"
     "        (cond"
     "          ((equal (aref promise 1) \"resolved\")"
     "           (funcall on-success (aref promise 2)))"
     "          ((equal (aref promise 1) \"rejected\")"
     "           (funcall on-error (aref promise 2)))"
     "          (t"
     "           (aset promise 3 (cons on-success (aref promise 3)))"
     "           (aset promise 4 (cons on-error (aref promise 4)))))"
     "        next)))"
     "  (defun xt-promise-finally (promise thunk)"
     "    (let ((next (xt-promise-create)))"
     "      (xt-promise-then"
     "       promise"
     "       (lambda (value)"
     "         (condition-case err"
     "             (progn"
     "               (funcall thunk)"
     "               (xt-promise-resolve! next value))"
     "           (error"
     "            (xt-promise-reject! next err)))))"
     "      (xt-promise-catch"
     "       promise"
     "       (lambda (err)"
     "         (condition-case cleanup-err"
     "             (progn"
     "               (funcall thunk)"
     "               (xt-promise-reject! next err))"
     "           (error"
     "            (xt-promise-reject! next cleanup-err)))))"
     "      next))"
     "  (defun xt-with-delay (ms thunk)"
     "    (let ((promise (xt-promise-create)))"
     "      (run-at-time"
     "       (/ (float ms) 1000.0)"
     "       nil"
     "       (lambda ()"
     "         (condition-case err"
     "             (xt-promise-bind-result promise (funcall thunk))"
     "           (error"
     "            (xt-promise-reject! promise err)))))"
     "      promise))"
    "  (defun return-encode (out id key)"
    "    (let ((payload `((type . \"data\")"
    "                     (value . ,(xt-json-normalize out)))))"
    "      (when (not (null id))"
    "        (setq payload (cons `(id . ,id) payload)))"
    "      (when (not (null key))"
    "        (setq payload (cons `(key . ,key) payload)))"
    "      (condition-case nil"
    "          (json-serialize payload)"
    "        (error"
    "         (json-serialize"
    "          `((type . \"raw\")"
    "            (value . ,(format \"%S\" out))))))))"
    "  (defun return-wrap (f)"
    "    (condition-case err"
    "        (return-encode (funcall f) nil nil)"
    "      (error"
    "       (json-serialize"
    "        `((type . \"error\")"
    "          (value . ,(error-message-string err)))))))"
    "  (defun return-eval (s)"
    "    (return-wrap (lambda () (xt-eval-source s))))"
    "  (defun xt-client-filter (proc chunk)"
    "    (let ((buffer (concat (or (process-get proc 'xt-buffer) \"\") chunk)))"
    "      (while (string-match \"\\n\" buffer)"
    "        (let ((line (substring-no-properties buffer 0 (match-beginning 0))))"
    "          (setq buffer (substring-no-properties buffer (match-end 0)))"
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
     "  (defvar xt-proto-create nil)"
     "  (defvar xt-proto-get nil)"
     "  (defvar xt-proto-set nil)"
     "  (defvar xt-proto-method nil)"
     "  (setq xt-proto-create #'xt-proto-create)"
     "  (setq xt-proto-get #'xt-proto-get)"
     "  (setq xt-proto-set #'xt-proto-set)"
     "  (setq xt-proto-method #'xt-proto-method)"
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

(def +elisp-verify-config+
  (common/set-context-options
   [:elisp :verify :default]
   {:main    {}
    :emit    {}
    :json    false
    :exec-fn #'type-verify/verify-exec-file}))

(def +elisp-oneshot+
  [(rt/install-type!
    :elisp :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

(def +elisp-verify+
  [(rt/install-type!
    :elisp :verify
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
