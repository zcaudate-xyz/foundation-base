(ns rt.basic.impl-annex.process-circom
  (:require [rt.basic.type-common :as common]
             [rt.basic.type-twostep :as twostep]
             [std.fs :as fs]
             [std.lang.base.book :as book]
             [std.lang.base.impl :as impl]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.runtime :as rt]
            [std.lang.base.util :as ut]
            [std.lib.os :as os]))

(defn sh-exec-circom
  "executes the circom compile and run process"
  {:added "4.0"}
  [input-args input-body {:keys [extension
                                 root]
                          :as opts}]
  (let [tmp-exec (java.io.File/createTempFile "tmp" "")
        tmp-file (str tmp-exec "." extension)
        _        (spit tmp-file input-body)
        parent   (str (fs/parent tmp-file))
        basename (fs/file-name tmp-exec)
        cpp-dir  (str parent "/" basename "_cpp")

        ;; Compile: circom <file> --c --output <parent>
        compile-args (vec (concat input-args [(str tmp-file) "--c" "--output" parent]))
        _ (os/sh {:args compile-args :root parent})

        ;; Build: make -C <cpp-dir>
        _ (os/sh {:args ["make" "-C" cpp-dir] :root parent})

        ;; Run: ./<cpp-dir>/<basename>
        exec-path (str cpp-dir "/" basename)]

    (str (os/sh {:args [exec-path] :root parent}))))

(def +program-init+
  (common/put-program-options
   :circom {:default  {:twostep     :circom
                       :interactive false
                       :ws-client   false}
            :env      {:circom   {:exec "circom"
                                  :extension   "circom"
                                  :exec-fn sh-exec-circom
                                  :stderr true
                                  :flags  {:twostep []
                                           :interactive false
                                           :json false
                                           :ws-client false}}}}))

(def +circom-twostep+
  [(rt/install-type!
    :circom :twostep
    {:type :hara/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])
