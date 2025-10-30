;; src/code/heal/core.clj
(ns code.heal.core
  (:require [clojure.java.shell :as shell]
            [clojure.string :as str]
            [clojure.tools.reader :as reader]
            [clojure.tools.reader.reader-types :as reader-types]))

(defn- git-log
  "Executes `git log` command and returns the output."
  [repo-path & args]
  (let [{:keys [exit out err]} (apply shell/sh "git" "log" args :dir repo-path)]
    (if (zero? exit)
      out
      (throw (ex-info (str "Git log failed: " err) {:repo-path repo-path :error err})))))

(defn- git-show
  "Executes `git show` command for a specific commit and file."
  [repo-path commit-hash file-path]
  (let [{:keys [exit out err]} (shell/sh "git" "show" (str commit-hash ":" file-path) :dir repo-path)]
    (if (zero? exit)
      out
      (throw (ex-info (str "Git show failed: " err) {:repo-path repo-path :commit commit-hash :file file-path :error err})))))

(defn- parse-clojure-forms
  "Parses a string of Clojure code into a sequence of forms,
   returning a map with :forms and :error (if any)."
  [code-str]
  (try
    (loop [rdr (reader-types/string-reader code-str)
           forms []]
      (let [form (reader/read {:read-cond :allow :features #{:clj} :eof ::reader/eof} rdr)]
        (if (= form ::reader/eof)
          {:forms forms :error nil}
          (recur rdr (conj forms form))))) ; Use conj to add to the end
    (catch Exception e
      {:forms [] :error (.getMessage e)}))) ; Return empty vector on error

(defn- extract-file-changes
  "Extracts changes for a specific file from a git diff, hunk by hunk.
   Returns a sequence of maps, each representing a hunk with :old-code and :new-code."
  [diff-str file-path]
  (let [lines (str/split-lines diff-str)
        file-diff-lines (drop-while #(not (str/starts-with? % "diff --git")) lines)
        target-file-diff-lines (->> file-diff-lines
                                    (partition-by #(str/starts-with? % "diff --git"))
                                    (filter #(some (fn [line] (str/includes? line (str "a/" file-path))) %))
                                    first)
        hunks (->> target-file-diff-lines
                   (partition-by #(str/starts-with? % "@@"))
                   (filter #(str/starts-with? (first %) "@@")))]
    (map (fn [hunk-lines]
           (let [hunk-header (first hunk-lines)
                 content-lines (rest hunk-lines)
                 old-lines (atom [])
                 new-lines (atom [])]
             (doseq [line content-lines]
               (cond
                 (str/starts-with? line "+") (swap! new-lines conj (subs line 1))
                 (str/starts-with? line "-") (swap! old-lines conj (subs line 1))
                 :else (do (swap! old-lines conj (subs line 1))
                           (swap! new-lines conj (subs line 1))))) ; Context lines go to both
             {:old-code (str/join "\n" @old-lines)
              :new-code (str/join "\n" @new-lines)}))
         hunks)))

(defn analyze-commit-for-healing
  "Analyzes a single commit for potential healing patterns in a given file.
   Looks for 'fix' in the commit message and extracts code changes."
  [repo-path commit-hash file-path]
  (let [commit-info (git-log repo-path "-n" "1" commit-hash)
        commit-message (first (str/split-lines commit-info))
        diff-output (git-show repo-path commit-hash file-path)
        hunk-changes (extract-file-changes diff-output file-path)]
    (when (str/includes? (str/lower-case commit-message) "fix")
      (map (fn [hunk] ; Iterate over hunks
             (let [added-parsed (parse-clojure-forms (:new-code hunk))
                   removed-parsed (parse-clojure-forms (:old-code hunk))]
               {:commit-hash commit-hash
                :message commit-message
                :file-path file-path
                :old-code (:old-code hunk)
                :new-code (:new-code hunk)
                :added-forms (:forms added-parsed)
                :removed-forms (:forms removed-parsed)
                :added-error (:error added-parsed)
                :removed-error (:error removed-parsed)}))
           hunk-changes))))

(defn- canonicalize-form
  "Converts a Clojure form into a canonical representation for fuzzy matching.
   Removes metadata, sorts map keys, and normalizes whitespace."
  [form]
  (cond
    (map? form)
    (->> form
         (sort-by key)
         (into {}) ; Preserve map type
         (canonicalize-form)) ; Recurse for nested maps

    (coll? form)
    (->> form
         (map canonicalize-form)
         (into (empty form))) ; Preserve collection type

    (string? form)
    (str/trim (str/replace form #"\s+" " ")) ; Normalize whitespace in strings

    :else
    form))

(defn suggest-parenthesis-healing
  "Analyzes a commit analysis result and suggests parenthesis healing.
   (Very basic heuristic for now)."
  [analysis-result]
  (when (and (:removed-error analysis-result)
             (nil? (:added-error analysis-result)))
    ;; This is a commit where a parsing error was fixed.
    ;; Now, try to identify the specific change.
    ;; This is a placeholder for more advanced diffing.
    (let [{:keys [removed-forms added-forms]} analysis-result]
      (when (and (= 1 (count removed-forms))
                 (= 1 (count added-forms)))
        (let [removed-form (first removed-forms)
              added-form (first added-forms)
              canonical-removed (canonicalize-form removed-form)
              canonical-added (canonicalize-form added-form)]
          ;; If the forms are structurally similar (fuzzy match)
          ;; but one had an error and the other doesn't, it's a candidate.
          (when (= canonical-removed canonical-added) ; Simple fuzzy match for now
            {:type :parenthesis-fix
             :description (str "Potential parenthesis fix: changed from "
                               (pr-str removed-form) " to " (pr-str added-form))
             :original-form removed-form
             :fixed-form added-form}))))))
