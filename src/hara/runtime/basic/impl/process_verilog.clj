(ns hara.runtime.basic.impl.process-verilog
  (:require [clojure.string]
            [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-twostep :as twostep]
            [hara.runtime.basic.type-verify :as type-verify]
            [hara.lang.runtime :as rt]
            [std.fs :as fs]
            [std.lib.foundation :as f]
            [std.lib.os :as os]))

;;
;; PROGRAM OPTIONS
;;

(def +program-init+
  (common/put-program-options
   :verilog {:default {:twostep :iverilog
                       :verify  :iverilog}
             :env {:iverilog {:exec "iverilog"
                              :extension "v"
                              :output-flag "-o"
                              :stderr true
                              :flags {:twostep []
                                      :verify  ["-g2012" "-o" "/dev/null"]}}
                   :vvp     {:exec "vvp"}}}))

;;
;; BODY TRANSFORM
;;

(defn- module-def?
  "Checks if a form is a top-level module definition."
  [form]
  (and (seq? form)
       (= 'defn (first form))))

(defn- top-level-block?
  "Checks if a form is an initial or always block."
  [form]
  (and (seq? form)
       (#{'initial 'always} (first form))))

(defn transform-form
  "Normalises Verilog forms for execution.

   - A single symbol (usually a module pointer) is emitted as-is.
   - A single module definition is emitted as-is.
   - A single non-module statement is wrapped in a temporary testbench module.
   - A sequence of forms keeps module definitions at the top level and wraps
     the remaining statements in a testbench module.

   If the wrapped statements already contain an initial/always block they are
   placed directly inside the module body; otherwise they are wrapped in an
   initial block and $finish is appended so the simulator terminates."
  {:added "4.1"}
  [forms opts]
  (cond
    (symbol? forms)
    [forms]

    (module-def? forms)
    [forms]

    (and (seq? forms) (symbol? (first forms)))
    [(list 'defn '__hara_tb__ []
           (list 'initial
                 (apply list 'do
                        (concat [forms] [(list '$finish)]))))]

    :else
    (let [modules (filter module-def? forms)
          stmts   (remove module-def? forms)]
      (if (seq stmts)
        (let [procedural? (some top-level-block? stmts)
              body (if procedural?
                     (apply list 'do stmts)
                     (list 'initial
                           (apply list 'do
                                  (concat stmts [(list '$finish)]))))]
          (concat modules
                  [(list 'defn '__hara_tb__ [] body)]))
        forms))))

;;
;; EXECUTION
;;

(defn sh-exec-verilog
  "Executes a Verilog program using the Icarus Verilog two-step toolchain:
   `iverilog -o <out> <file.v>` followed by `vvp <out>`."
  {:added "4.1"}
  [input-args input-body process]
  (let [process   (if (map? process) process {})
        {:keys [trim stderr raw root shell output-flag]
         :or {trim clojure.string/trim-newline
              output-flag "-o"}} process
        tmp-exec  (java.io.File/createTempFile "hara_verilog_" "")
        tmp-file  (str tmp-exec ".v")
        root-dir  (str (or root (fs/parent tmp-file)))
        run!      (fn [args]
                    (let [proc (os/sh (merge shell
                                             {:wait false
                                              :output false
                                              :args args
                                              :root root-dir}))]
                      (os/sh-wait proc)
                      (os/sh-output proc)))
        stderr-output (fn [{:keys [out err]}]
                        (trim (or (not-empty err)
                                  out
                                  "")))
        raw-output (fn [{:keys [exit out err]}]
                     (let [out-lines (->> (clojure.string/split-lines (trim out))
                                          (remove empty?)
                                          seq)
                           err-lines (->> (clojure.string/split-lines (trim err))
                                          (remove empty?)
                                          seq)]
                       [exit (or out-lines err-lines [])]))]
    (try
      (spit tmp-file input-body)
      (let [compile-args (vec (concat input-args
                                      [output-flag (str tmp-exec) tmp-file]))
            compile-ret  (run! compile-args)]
        (cond raw
              (if (zero? (:exit compile-ret))
                (raw-output (run! ["vvp" (str tmp-exec)]))
                (raw-output compile-ret))

              (not (zero? (:exit compile-ret)))
              (if stderr
                (stderr-output compile-ret)
                (f/error "Verilog compile failed"
                         {:args compile-args
                          :root root-dir
                          :file tmp-file
                          :result compile-ret}))

              :else
              (let [run-ret (run! ["vvp" (str tmp-exec)])]
                (if (zero? (:exit run-ret))
                  (trim (:out run-ret))
                  (if stderr
                    (stderr-output run-ret)
                    (f/error "Verilog simulation failed"
                             {:args ["vvp" (str tmp-exec)]
                              :root root-dir
                              :file tmp-file
                              :result run-ret}))))))
      (catch Throwable t
        (if stderr
          (trim (.getMessage t))
          (throw t)))
      (finally
        (doseq [path [tmp-file (str tmp-exec)]]
          (try (fs/delete path)
               (catch Throwable _)))))))

;;
;; RUNTIME INSTALLATION
;;

(def +verilog-twostep-config+
  (common/set-context-options
   [:verilog :twostep :default]
   {:exec-fn #'sh-exec-verilog
    :emit  {:body {:transform #'transform-form}}}))

(def +verilog-verify-config+
  (common/set-context-options
   [:verilog :verify :default]
   {:main    {}
    :emit    {}
    :json    false
    :exec-fn #'type-verify/verify-exec-file}))

(def +verilog-twostep+
  [(rt/install-type!
    :verilog :twostep
    {:type :hara/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])

(def +verilog-verify+
  [(rt/install-type!
    :verilog :verify
    {:type :hara/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])
