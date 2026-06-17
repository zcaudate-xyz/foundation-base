(ns hara.runtime.basic.type-verify
  (:require [clojure.string]
            [std.fs :as fs]
            [std.lib.foundation :as f]
            [std.lib.os :as os]))

(defn- verify-temp-file
  "creates a temp file with the requested extension"
  ([extension]
   (verify-temp-file extension nil))
  ([extension dir]
   (let [ext (if (clojure.string/starts-with? extension ".")
               extension
               (str "." extension))
         file (if dir
                (java.io.File/createTempFile "hara_verify_" ext (java.io.File. dir))
                (java.io.File/createTempFile "hara_verify_" ext))]
     (str file))))

(defn- substitute-file
  "replaces __FILE__ placeholders in args with the temp file path"
  [args tmp-file]
  (let [replaced (mapv #(clojure.string/replace % "__FILE__" tmp-file) args)]
    (if (= replaced args)
      (conj replaced tmp-file)
      replaced)))

(defn- verify-error-result
  "formats a verify failure"
  [{:keys [exit err out]} stderr]
  (let [msg (or (not-empty (clojure.string/trim-newline (or err "")))
                (not-empty (clojure.string/trim-newline (or out "")))
                "verify failed")]
    (if stderr
      msg
      (f/error "Verify failed"
               {:exit exit
                :err err
                :out out}))))

(defn verify-exec-oneshot
  "runs a pipe-based checker (or inline checker) and returns the original
   source body when the checker exits 0."
  {:added "4.0"}
  [input-args input-body {:keys [pipe trim shell root stderr]
                          :or {trim clojure.string/trim-newline}}]
  (let [args (if pipe
               input-args
               (conj input-args input-body))
        proc (os/sh (merge shell
                           {:wait false
                            :args args
                            :root root}))
        _    (cond-> proc
               pipe  (doto (os/sh-write input-body) (os/sh-close))
               :then (os/sh-wait))
        {:keys [exit] :as ret} (os/sh-output proc)]
    (if (zero? exit)
      input-body
      (verify-error-result ret stderr))))

(defn verify-exec-file
  "writes the source body to a temp file and runs a file-based checker.
   returns the original source body when the checker exits 0."
  {:added "4.0"}
  [input-args input-body {:keys [extension trim shell root stderr]
                          :or {trim clojure.string/trim-newline}}]
  (let [tmp-file (verify-temp-file (or extension
                                       (f/error "Verify requires file extension"))
                                   root)
        args     (substitute-file input-args tmp-file)]
    (try
      (spit tmp-file input-body)
      (let [proc (os/sh (merge shell
                               {:wait false
                                :args args
                                :root root}))
            _    (os/sh-wait proc)
            {:keys [exit] :as ret} (os/sh-output proc)]
        (if (zero? exit)
          input-body
          (verify-error-result ret stderr)))
      (finally
        (try (fs/delete tmp-file)
             (catch Throwable _))
        ;; emacs writes the compiled file next to the source
        (try (fs/delete (str tmp-file "c"))
             (catch Throwable _))))))

(defn verify-exec-twostep
  "alias for verify-exec-file, used for compile-only verification of
   compiled languages that are registered under the twostep runtime type."
  {:added "4.0"}
  [input-args input-body opts]
  (verify-exec-file input-args input-body opts))
