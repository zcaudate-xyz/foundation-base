(ns rt.basic.impl.process-dart
  (:require [clojure.string :as str]
            [rt.basic.type-common :as common]
            [rt.basic.type-twostep :as twostep]
            [std.fs :as fs]
            [std.lang.base.runtime :as rt]
            [std.lang.model.spec-dart]
            [std.lib.foundation :as f]
            [std.lib.os :as os]))

(defn- char-count
  [s ch]
  (count (filter #(= ch %) s)))

(defn normalize-dart-source
  "Adds statement terminators so emitted Dart source can compile as a standalone file."
  {:added "4.1"}
  [source]
  (->> (reduce (fn [[lines depth] line]
                 (let [trimmed   (str/trim line)
                       delta     (+ (- (char-count trimmed \()
                                         (char-count trimmed \)))
                                    (- (char-count trimmed \[)
                                       (char-count trimmed \])))
                       next-depth (max 0 (+ depth delta))
                       complete? (and (zero? depth)
                                      (zero? next-depth))
                       line'     (cond (or (empty? trimmed)
                                           (str/starts-with? trimmed "//")
                                           (str/ends-with? trimmed "{")
                                           (= trimmed "}")
                                           (str/ends-with? trimmed ";")
                                           (and (pos? depth)
                                                (pos? next-depth)))
                                       line

                                       (or complete?
                                           (and (pos? depth)
                                                (zero? next-depth)))
                                       (str line ";")

                                       :else
                                       line)]
                   [(conj lines line') next-depth]))
               [[] 0]
               (str/split-lines source))
       first
       (str/join "\n")))

(defn sh-exec-dart
  "Executes the dart compile and run process."
  {:added "4.1"}
  [input-args input-body {:keys [trim
                                 stderr
                                 raw
                                 root
                                 extension
                                 output-flag]
                          :as opts
                          :or {trim str/trim-newline}}]
  (let [tmp-exec (java.io.File/createTempFile "tmp" "")
        tmp-file (str tmp-exec
                      "."
                      (or extension
                          (f/error "Requires File Extension"
                                   opts)))
        root-dir     (str (or root (fs/parent tmp-file)))
        compile-args (vec (concat input-args
                                  [(str tmp-file)
                                   output-flag
                                   (str tmp-exec)]))
        run-args     [(str "./" (fs/file-name tmp-exec))]
        run!         (fn [args]
                       (let [proc (os/sh {:wait false
                                          :output false
                                          :args args
                                          :root root-dir})]
                         (os/sh-wait proc)
                         (os/sh-output proc)))
        raw-output   (fn [{:keys [exit out err]}]
                       (let [out-lines (->> (str/split-lines (trim out))
                                            (remove empty?)
                                            seq)
                             err-lines (->> (str/split-lines (trim err))
                                            (remove empty?)
                                            seq)]
                         [exit (or out-lines err-lines [])]))
        stderr-output (fn [{:keys [out err]}]
                        (trim (or (not-empty err)
                                  out
                                  "")))]
    (try
      (spit tmp-file (normalize-dart-source input-body))
      (let [compile-ret (run! compile-args)]
        (cond raw
              (if (zero? (:exit compile-ret))
                (raw-output (run! run-args))
                (raw-output compile-ret))

              (not (zero? (:exit compile-ret)))
              (if stderr
                (stderr-output compile-ret)
                (f/error "Twostep compile failed"
                         {:args compile-args
                          :root root-dir
                          :file tmp-file
                          :exec (str tmp-exec)
                          :result compile-ret}))

              :else
              (let [run-ret (run! run-args)]
                (if (zero? (:exit run-ret))
                  (trim (:out run-ret))
                  (if stderr
                    (stderr-output run-ret)
                    (f/error "Twostep execution failed"
                             {:args run-args
                              :root root-dir
                              :file tmp-file
                              :exec (str tmp-exec)
                              :result run-ret}))))))
      (catch Throwable t
        (if stderr
          (trim (.getMessage t))
          (throw t)))
      (finally
        (doseq [path [tmp-file (str tmp-exec)]]
          (try (fs/delete path)
               (catch Throwable _)))))))

(defn transform-form
  "Transforms forms into a standalone Dart `main` function."
  {:added "4.1"}
  [forms opts]
  (let [forms (if (symbol? (first forms))
                [forms]
                forms)
         body  (concat '[do]
                       (butlast forms)
                       [(list 'print (list 'json.encode (last forms)))])]
    `(:- "import 'dart:convert' as json;\n\n"
         "void main() {\n "
         ~body
         "\n}")))

(defn- dart-exec
  "Resolves a user-local Dart SDK binary before falling back to PATH."
  {:added "4.1"}
  []
  (let [home (System/getenv "HOME")
        candidates (remove nil?
                           [(some-> home (str "/.local/bin/dart"))
                            (some-> home (str "/.local/lib/dart-sdk/bin/dart"))])]
    (or (some (fn [path]
                (when (.exists (java.io.File. path))
                  path))
              candidates)
        "dart")))

(def +program-init+
  (common/put-program-options
   :dart {:default {:twostep :dart}
           :env     {:dart {:exec (dart-exec)
                             :extension "dart"
                             :exec-fn sh-exec-dart
                             :stderr true
                            :flags {:twostep ["compile" "exe"]
                                    :interactive false
                                    :json false
                                    :ws-client false}
                            :output-flag "-o"}}}))

(def +dart-twostep-config+
  (common/set-context-options
   [:dart :twostep :default]
   {:emit {:body {:transform #'transform-form}}
    :json :string}))

(def +dart-twostep+
  [(rt/install-type!
    :dart :twostep
    {:type :hara/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])
