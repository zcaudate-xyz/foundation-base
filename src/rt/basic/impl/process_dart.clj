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

(declare dart-exec)

(def +dart-shell-env+
  {"DART_TOOL_DISABLE_ANALYTICS" "true"})

(defn normalize-dart-source
  "Adds statement terminators so emitted Dart source can compile as a standalone file.
   Tracks both paren/bracket depth (for multi-line expressions) and brace depth
   (for block vs. assignment contexts) to correctly place semicolons."
  {:added "4.1"}
  [source]
  (->> (reduce (fn [[lines paren-depth brace-stack] line]
                 (let [trimmed        (str/trim line)
                       p-delta        (+ (- (char-count trimmed \()
                                            (char-count trimmed \)))
                                         (- (char-count trimmed \[)
                                            (char-count trimmed \])))
                       next-paren     (max 0 (+ paren-depth p-delta))
                       closing-brace? (str/starts-with? trimmed "}")
                       opening-brace? (str/ends-with? trimmed "{")
                       ;; distinguish block bodies (fn/if/for/etc) from map/object literals
                       block-brace?   (and opening-brace?
                                           (or (re-find #"(?:\)|async|sync\*?)\s*\{$" trimmed)
                                               (re-find #"^(?:else|try|finally|do)\s*\{$" trimmed)))
                       assign-brace?  (and opening-brace?
                                           (not block-brace?))
                       closed-stack   (if (and closing-brace?
                                               (seq brace-stack))
                                        (rest brace-stack)
                                        brace-stack)
                       next-brace-stack (if opening-brace?
                                          (cons (if assign-brace?
                                                  :assignment
                                                  :block)
                                                closed-stack)
                                          closed-stack)
                        in-brace?      (seq brace-stack)
                        in-assign?     (= (first brace-stack) :assignment)
                        complete?      (and (zero? paren-depth)
                                            (zero? next-paren)
                                            (not in-brace?))
                       line'          (cond
                                        ;; blank lines and comments unchanged
                                        (or (empty? trimmed)
                                            (str/starts-with? trimmed "//"))
                                        line

                                        ;; closing brace of an assignment block (map/object)
                                        ;; the var declaration needs its semicolon here
                                         (and closing-brace?
                                              in-assign?
                                              (not opening-brace?)
                                              (not= (first closed-stack) :assignment))
                                         (if (str/ends-with? trimmed ";")
                                           line
                                           (str line ";"))

                                        ;; closing brace of a function/control block
                                        closing-brace?
                                        line

                                        ;; line that opens a new block
                                        opening-brace?
                                        line

                                        ;; already terminated
                                        (str/ends-with? trimmed ";")
                                        line

                                        ;; end of multi-line paren/bracket expression:
                                        ;; add ; only in statement context (top-level or inside
                                        ;; a function block, NOT inside an assignment/map brace)
                                        (and (pos? paren-depth)
                                             (zero? next-paren)
                                             (not in-assign?))
                                        (str line ";")

                                        ;; inside a brace block (map entries, function body)
                                        in-brace?
                                        line

                                        ;; inside a multi-line paren/bracket expression
                                        (and (pos? paren-depth) (pos? next-paren))
                                        line

                                        ;; complete top-level statement
                                        complete?
                                        (str line ";")

                                        :else line)]
                   [(conj lines line') next-paren next-brace-stack]))
               [[] 0 '()]
               (str/split-lines source))
        first
        (str/join "\n")))

(defn ensure-dart-imports
  "Hoists required imports to the top of the standalone Dart file."
  {:added "4.1"}
  [source]
  (let [convert-needed? (or (str/includes? source "jsonEncode(")
                            (str/includes? source "jsonDecode("))
        math-needed?    (str/includes? source "math.")
        lines           (str/split-lines source)
        [import-lines body-lines]
        (reduce (fn [[imports body] line]
                  (if (re-matches #"\s*import\s+'[^']+'(?:\s+as\s+\w+)?;\s*" line)
                    [(conj imports (str/trim line)) body]
                    [imports (conj body line)]))
                 [[] []]
                 lines)
        imports         (cond-> (vec (distinct import-lines))
                          (and convert-needed?
                               (not-any? #(= "import 'dart:convert';" %) import-lines))
                          (conj "import 'dart:convert';")
                          (and math-needed?
                               (not-any? #(= "import 'dart:math' as math;" %) import-lines))
                          (conj "import 'dart:math' as math;"))]
    (str/join "\n"
              (concat imports
                      (when (and (seq imports)
                                 (seq body-lines))
                        [""])
                      body-lines))))

(defn dart-package-imports
  "Finds package imports referenced by the generated Dart source."
  {:added "4.1"}
  [source]
  (->> (re-seq #"package:([^/]+)/" source)
       (map second)
       distinct
       sort
       vec))

(defn dart-package-root
  "Returns the cached Dart package root used for twostep scripts."
  {:added "4.1"}
  [root]
  (str (java.io.File. (or root (System/getProperty "user.dir"))
                      "target/dart-twostep")))

(defn dart-pubspec
  "Creates a minimal pubspec for generated twostep scripts."
  {:added "4.1"}
  [packages]
  (str "name: foundation_base_dart_twostep\n"
       "publish_to: 'none'\n"
       "environment:\n"
       "  sdk: '>=3.0.0 <4.0.0'\n"
       (when (seq packages)
         (str "dependencies:\n"
              (apply str
                     (map (fn [package]
                            (str "  " package ": any\n"))
                          packages))))))

(defn ensure-dart-package-context
  "Creates a cached package root and resolves dependencies for package imports."
  {:added "4.1"}
  [root source]
  (let [packages (dart-package-imports source)]
    (when (seq packages)
      (let [package-root   (dart-package-root root)
            package-dir    (java.io.File. package-root)
            _              (.mkdirs package-dir)
            pubspec-path   (str package-root "/pubspec.yaml")
            pubspec-body   (dart-pubspec packages)
            current-body   (when (.exists (java.io.File. pubspec-path))
                             (slurp pubspec-path))
            changed?       (not= current-body pubspec-body)
            package-config (java.io.File. (str package-root "/.dart_tool/package_config.json"))]
        (when changed?
          (spit pubspec-path pubspec-body))
        (when (or changed?
                  (not (.exists package-config)))
          (let [proc (os/sh {:wait false
                             :output false
                             :env +dart-shell-env+
                             :args [(dart-exec) "pub" "get"]
                             :root package-root})]
            (os/sh-wait proc)
            (let [{:keys [exit err out] :as ret} (os/sh-output proc)]
              (when-not (zero? exit)
                (f/error "Unable to prepare Dart package context"
                         {:root package-root
                          :packages packages
                          :result ret
                          :stderr err
                          :stdout out})))))
        package-root))))

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
  (let [package-root  (ensure-dart-package-context root input-body)
        raw-output    (fn [{:keys [exit out err]}]
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
    (if package-root
      (let [script-dir (doto (java.io.File. (str package-root "/bin"))
                         (.mkdirs))
            tmp-file-obj (java.io.File/createTempFile
                          "tmp"
                          (str "."
                               (or extension
                                   (f/error "Requires File Extension"
                                            opts)))
                          script-dir)
            tmp-file     (str tmp-file-obj)
            file-name    (.getName tmp-file-obj)
            run-args     [(dart-exec) "run" "--verbosity" "error" (str "bin/" file-name)]
            run!         (fn [args]
                           (let [proc (os/sh {:wait false
                                              :output false
                                              :env +dart-shell-env+
                                              :args args
                                              :root package-root})]
                             (os/sh-wait proc)
                             (os/sh-output proc)))]
        (try
          (spit tmp-file (-> input-body
                             normalize-dart-source
                             ensure-dart-imports))
          (let [run-ret (run! run-args)]
            (if raw
              (raw-output run-ret)
              (if (zero? (:exit run-ret))
                (trim (:out run-ret))
                (if stderr
                  (stderr-output run-ret)
                  (f/error "Twostep execution failed"
                           {:args run-args
                            :root package-root
                            :file tmp-file
                            :result run-ret})))))
          (catch Throwable t
            (if stderr
              (trim (.getMessage t))
              (throw t)))
          (finally
            (try (fs/delete tmp-file)
                 (catch Throwable _)))))
      (let [temp-root    (some-> root str)
            temp-dir     (some-> temp-root java.io.File.)
            _            (when temp-dir
                           (.mkdirs temp-dir))
            tmp-exec (if temp-dir
                       (java.io.File/createTempFile "tmp" "" temp-dir)
                       (java.io.File/createTempFile "tmp" ""))
            tmp-file (str tmp-exec
                          "."
                          (or extension
                              (f/error "Requires File Extension"
                                       opts)))
            root-dir     (str (or temp-root
                                  root
                                  (fs/parent tmp-file)))
            compile-args (vec (concat input-args
                                      [(str tmp-file)
                                       output-flag
                                       (str tmp-exec)]))
            run-args     [(str "./" (fs/file-name tmp-exec))]
            run!         (fn [args]
                           (let [proc (os/sh {:wait false
                                              :output false
                                              :env +dart-shell-env+
                                              :args args
                                              :root root-dir})]
                             (os/sh-wait proc)
                             (os/sh-output proc)))]
        (try
          (spit tmp-file (-> input-body
                             normalize-dart-source
                             ensure-dart-imports))
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
                   (catch Throwable _)))))))))

(defn transform-form
  "Transforms forms into a standalone Dart `main` function."
  {:added "4.1"}
  [forms opts]
  (let [forms (if (symbol? (first forms))
                [forms]
                forms)
        statement-op? '#{:- := var return break throw
                         do do* if for while try
                         for:index for:array for:object for:iter
                         xt/for:index xt/for:array xt/for:object xt/for:iter}
        await-form (fn [form]
                     (if (and (seq? form)
                              (statement-op? (first form)))
                       form
                       (list 'await form)))
        out-json (list ':?
                       (list '== 'out nil)
                       (list 'jsonEncode 'out)
                       (list ':?
                             (list '== (list '. (list '. 'out 'runtimeType) (list 'toString))
                                   "String")
                             'out
                             (list 'jsonEncode 'out)))
        body  (concat '[do]
                      (map await-form (butlast forms))
                      [(list 'var 'out (list 'await (last forms)))
                       (list 'print out-json)])]
    `(:- "Future<void> main() async {\n "
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
