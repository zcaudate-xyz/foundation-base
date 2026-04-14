(ns rt.basic.impl-annex.process-lean
  (:require [clojure.string]
            [rt.basic.type-common :as common]
            [rt.basic.type-twostep :as twostep]
            [std.fs :as fs]
            [std.lang.base.runtime :as rt]
            [std.lang.model-annex.spec-lean :as spec]
            [std.lib.foundation :as f]
            [std.lib.os :as os]))

(defn- raw-output
  [trim {:keys [exit out err]}]
  (let [out-lines (->> (clojure.string/split-lines (trim out))
                       (remove empty?)
                       seq)
        err-lines (->> (clojure.string/split-lines (trim err))
                       (remove empty?)
                       seq)]
    [exit (or out-lines err-lines [])]))

(defn- stderr-output
  [trim {:keys [out err]}]
  (trim (or (not-empty err)
            out
            "")))

(defn sh-exec-lean
  "Executes a Lean source file and returns its `#eval` output."
  {:added "4.1"}
  [input-args input-body {:keys [trim
                                 stderr
                                 raw
                                 root
                                 extension]
                          :as opts
                          :or {trim clojure.string/trim-newline}}]
  (let [tmp-exec  (java.io.File/createTempFile "tmp" "")
        tmp-file  (str tmp-exec
                       "."
                       (or extension
                           (f/error "Requires File Extension"
                                    opts)))
        root-dir  (str (or root (fs/parent tmp-file)))
        run-args  (vec (concat input-args [(str tmp-file)]))
        run!      (fn [args]
                    (let [proc (os/sh {:wait false
                                       :output false
                                       :args args
                                       :root root-dir})]
                      (os/sh-wait proc)
                      (os/sh-output proc)))]
    (try
      (spit tmp-file input-body)
      (let [run-ret (run! run-args)]
        (cond raw
              (raw-output trim run-ret)

              (zero? (:exit run-ret))
              (trim (:out run-ret))

              stderr
              (stderr-output trim run-ret)

              :else
              (f/error "Lean execution failed"
                       {:args run-args
                        :root root-dir
                        :file tmp-file
                        :result run-ret})))
      (catch Throwable t
        (if stderr
          (trim (.getMessage t))
          (throw t)))
      (finally
        (try (fs/delete tmp-file)
             (catch Throwable _))))))

(defn sh-exec-lean-docker
  "Executes a Lean source file inside a Docker container."
  {:added "4.1"}
  [input-args input-body {:keys [trim
                                 stderr
                                 raw
                                 root
                                 extension
                                 container]
                          :as opts
                          :or {trim clojure.string/trim-newline}}]
  (let [{:keys [image flags environment exec workdir]
         :or {workdir "/work"}} (or container
                                   (f/error "Container config required"
                                            {:opts opts}))
        tmp-exec      (java.io.File/createTempFile "tmp" "")
        tmp-file      (str tmp-exec
                           "."
                           (or extension
                               (f/error "Requires File Extension"
                                        opts)))
        root-dir      (str (or root (fs/parent tmp-file)))
        file-name     (str (fs/file-name tmp-file))
        container-exec (or exec input-args)
        run-args      (vec (concat container-exec [file-name]))
        docker-run!   (fn [args]
                        (let [env-args    (mapcat (fn [[k v]]
                                                    ["-e" (str k "=" v)])
                                                  environment)
                              docker-args (vec (concat ["docker" "run" "--rm"
                                                        "-v" (str root-dir ":" workdir)
                                                        "-w" workdir]
                                                       flags
                                                       env-args
                                                       [image]
                                                       args))
                              proc        (os/sh {:wait false
                                                  :output false
                                                  :args docker-args})]
                          (os/sh-wait proc)
                          (os/sh-output proc)))]
    (try
      (spit tmp-file input-body)
      (let [run-ret (docker-run! run-args)]
        (cond raw
              (raw-output trim run-ret)

              (zero? (:exit run-ret))
              (trim (:out run-ret))

              stderr
              (stderr-output trim run-ret)

              :else
              (f/error "Lean docker execution failed"
                       {:args run-args
                        :root root-dir
                        :file tmp-file
                        :image image
                        :result run-ret})))
      (catch Throwable t
        (if stderr
          (trim (.getMessage t))
          (throw t)))
      (finally
        (try (fs/delete tmp-file)
             (catch Throwable _))))))

(defn sh-exec-lean-portable
  "Uses local Lean when available and otherwise falls back to Docker."
  {:added "4.1"}
  [input-args input-body {:keys [container
                                 container-backup
                                 force-container]
                          :as opts}]
  (let [use-container? (or force-container
                           (and container
                                (if (contains? opts :container-backup)
                                  container-backup
                                  true)
                                (not (twostep/local-exec-available? input-args))))]
    (if use-container?
      (sh-exec-lean-docker input-args input-body opts)
      (sh-exec-lean input-args input-body opts))))

(defn transform-form
  "Transforms forms into a standalone Lean script ending in `#eval`."
  {:added "4.1"}
  [forms opts]
  (let [forms (if (symbol? (first forms))
                [forms]
                forms)]
    (apply list
           :lines
           (concat (butlast forms)
                   [(list :%
                          (list :raw-str "#eval ")
                          (last forms))]))))

(def +program-init+
  (common/put-program-options
   :lean {:default {:twostep :lean}
          :env     {:lean {:exec "lean"
                           :extension "lean"
                           :stderr true
                           :flags {:twostep []
                                   :interactive false
                                   :json false
                                   :ws-client false}}}}))

(def +lean-twostep-config+
  (common/set-context-options
   [:lean :twostep :default]
   {:container {:image "foundation-base/rt-twostep-lean:latest"}
    :container-backup true
    :exec-fn #'sh-exec-lean-portable
    :main {:in identity}
    :emit {:body {:transform #'transform-form}}
    :json :string}))

(def +lean-twostep+
  [(rt/install-type!
    :lean :twostep
    {:type :hara/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])
