(ns code.test.base.print
  (:require [std.print.ansi :as ansi]
            [std.string :as str]
            [std.fs :as fs]
            [code.test.checker.common :as checker]
            [code.test.base.runtime :as rt]
            [code.test.base.context :as context]
            [code.test.checker.diff :as diff]
            [std.lib.walk :as walk]
            [std.lib.result :as res]
            [std.lib.time :as t]
            [std.print :as print]
            [std.pretty :as pretty]))

(defn- rel
  [path]
  (cond (and context/*root* path)
        (fs/relativize context/*root* path)

        :else path))

(defn- pad-left
  [n s]
  (let [len (count s)]
    (if (> n len)
      (str (apply str (repeat (- n len) " ")) s)
      s)))

(defn format-diff-map
  "formats a map diff"
  {:added "4.0"}
  [diff indent]
  (let [missing (:+ diff)
        extra   (:- diff)
        changed (:> diff)]
    (str/join "\n"
              (concat
               (for [[k v] missing]
                 (str (apply str (repeat indent " ")) (ansi/green "+ ") (pr-str k) " " (pr-str v)))
               (for [[k v] extra]
                 (str (apply str (repeat indent " ")) (ansi/red "- ") (pr-str k) " " (pr-str v)))
               (for [[k {:keys [expect actual]}] changed]
                 (str (apply str (repeat indent " ")) (ansi/yellow "> ") (pr-str k)
                      "\n" (apply str (repeat (+ indent 4) " ")) (ansi/yellow "exp: ") (pr-str expect)
                      "\n" (apply str (repeat (+ indent 4) " ")) (ansi/yellow "got: ") (pr-str actual)))))))

(defn format-diff-seq
  "formats a seq diff"
  {:added "4.0"}
  [diff indent]
  (if (vector? diff)
    (str/join "\n"
              (for [[op & args] diff]
                (str (apply str (repeat indent " "))
                     (case op
                       :+ (str (ansi/green "+") " at " (first args) ": " (pr-str (second args)))
                       :- (str (ansi/red "-") " at " (first args) ": " (second args) " items")
                       (pr-str [op args])))))
    (str/indent (pretty/pprint-str diff) indent)))

(defn format-diff
  "formats a diff"
  {:added "4.0"}
  [diff]
  (cond (and (map? diff) (or (:+ diff) (:- diff) (:> diff)))
        (format-diff-map diff 4)

        (vector? diff)
        (format-diff-seq diff 4)

        :else
        (str/indent (pretty/pprint-str diff) 4)))

(defn print-preliminary
  "prints preliminary info"
  {:added "4.1"}
  ([title color
    {:keys [path name ns line desc form original check] :as summary}]
   (let [line (if line (str "L:" line " @ ") "")]
     (str (ansi/style (pad-left 8 title) #{color :bold})
          (ansi/style (format "  %s%s" line (or (rel path) "<current>")) #{:bold color})
          (if desc (str "\n" (ansi/style (pad-left 8 "Desc:")  #{color}) "  "  (ansi/style (str "\"" desc "\"") #{})) "")
          (str "\n"  (ansi/style (pad-left 8 "Form:") #{color}) "  " (str/indent-rest (pretty/pprint-str (or original form)) 12))))))

(defn print-success
  "outputs the description for a successful test"
  {:added "3.0"}
  ([{:keys [name check] :as summary}]
   (print/println
    (str  "\n"
          (print-preliminary "SUCCESS" :green summary)
          (str "\n"  (ansi/style (pad-left 8  "Check:") #{:green :bold}) "  " check)
          (if name (str "\n" (ansi/style (pad-left 8 "###") #{:green :bold}) "  " (ansi/style name #{:green :bold})) "")
          "\n"))))

(defn print-throw
  "prints throw info"
  {:added "4.1"}
  ([{:keys [name data] :as summary}]
   (print/println
    (str (print-preliminary "THROW" :yellow summary)
         (str "\n" (ansi/style (pad-left 8 "ERROR") #{:yellow :bold})
              "  " (str/indent-rest
                    (str/join-lines
                     (take 20 (str/split-lines
                               (pr-str data))))
                    10))
         (if name (str "\n" (ansi/style (pad-left 8 "###") #{:yellow :bold}) "  " (ansi/style name #{:yellow  :bold})) "")
         "\n"))))

(defn print-timeout
  "prints timeout info"
  {:added "4.0"}
  ([{:keys [name data actual check parent] :as summary}]
   (print/println
    (str (print-preliminary "TIMEOUT" :magenta  summary)
         (if parent (str "\n"  (ansi/style  (pad-left 8 "Parent") #{:magenta}) "  " (str/indent-rest (pretty/pprint-str parent) 12)))
         (if check  (str "\n"  (ansi/style (pad-left 8  "Check:") #{:magenta}) "  " check))
         (str "\n" (ansi/style (pad-left 8 "AFTER") #{:bold :magenta}) "  " (ansi/style (str (t/format-ms (if actual
                                                                                                              (:data actual)
                                                                                                              data)))
                                                                                          #{:bold :magenta}))
         (if name (str "\n" (ansi/style (pad-left 8 "###") #{:magenta :bold}) "  " (ansi/style name #{:magenta :bold})) "")
         "\n"))))

(defn print-failed
  "prints failed info"
  {:added "4.1"}
  ([{:keys [name actual check parent checker] :as summary}]
   (let [result (:data actual)
         diff   (try (diff/diff checker result) (catch Throwable _ nil))]
     (print/println
      (str (print-preliminary "FAILED" :red summary)
           (if parent  (str "\n" (ansi/style  (pad-left 8 "Parent")
                                              #{:red}) "  " (str/indent-rest (pretty/pprint-str parent) 12)))
           (if diff
             (str "\n"  (ansi/style (pad-left 8  "Actual:") #{:red}) "  " (str/indent-rest (pretty/pprint-str result) 12))
             (str "\n"  (ansi/style (pad-left 8  "Check:") #{:red}) "  " check))
           (if diff
             (str "\n" (ansi/style (pad-left 8 "Diff:") #{:red :bold}) "  " (str/indent-rest (format-diff diff) 10))
             (str "\n"  (ansi/style (pad-left 8 "OUTPUT") #{:red :bold}) "  " (str/indent-rest
                                                                               (str/join-lines
                                                                                (take 20 (str/split-lines
                                                                                          (pr-str result))))
                                                                               10)))
           (if name (str "\n" (ansi/style (pad-left 8 "###") #{:red :bold}) "  " (ansi/style name #{:red :bold})) "")
           "\n")))))

(defn print-fact
  "outputs the description for a fact form that contains many statements"
  {:added "3.0"}
  ([{:keys [path name ns line desc refer] :as meta}  results]
   (let [name   (if name (str name " @ ") "")
         line   (if line (str ":" line) "")
         all    (->> results (filter #(-> % :from (= :verify))))
         passed (->> all (filter checker/succeeded?))
         num    (count passed)
         total  (count all)
         ops    (->> results (filter #(-> % :from (= :evaluate))))
         errors  (->> ops (filter #(-> % :status (= :exception))))
         timeout (->> ops (filter #(-> % :status (= :timeout))) count)
         throw  (count errors)]
     (if (or (context/*print* :print-facts-success)
             (not (and (= num total)
                       (pos? throw))))
       (print/println
        (str (ansi/style (pad-left 8 "Fact") #{:blue :bold})
             (ansi/style (str "  [" (or path "<current>") line "]") #{:bold})
             (if name (str "\n" (ansi/white (pad-left 8 "###")) "  " (ansi/style name #{:highlight :bold})) "")
             (if desc (str "\n" (ansi/white (pad-left 8 "Info:")) "  \"" desc "" \") "")
             (str          "\n" (ansi/white (pad-left 8 "Passed:")) "  "
                  (str (ansi/style num (if (= num total) #{:blue} #{:green}))
                       " of "
                       (ansi/blue total)))
             (if (pos? throw)
               (str "\n"  (ansi/white (pad-left 8 "Throw")) "  " (ansi/yellow throw))
               "")
             (if (pos? timeout)
               (str "\n"  (ansi/white (pad-left 8 "Timeout")) "  " (ansi/magenta timeout))
               ""))
        "\n")))))

(defn print-summary
  "outputs the description for an entire test run"
  {:added "3.0"}
  ([{:keys [files throw facts checks passed failed timeout queued] :as result}]
   (print/println
    (str (ansi/style (str "Summary (" files ")") #{:blue :bold})
         (str "\n" (ansi/white (pad-left 8 "Files:"))   "  " (ansi/blue files))
         (str "\n" (ansi/white (pad-left 8 "Facts:"))   "  "
              (if (and queued (> queued facts))
                (str (ansi/blue facts) " of " (ansi/blue queued))
                (ansi/blue facts)))
         (if (pos? throw)
           (str "\n" (ansi/white (pad-left 8 "Thrown:"))  "  " (ansi/yellow throw)))
         (str "\n" (ansi/white (pad-left 8 "Timeout:")) "  " (ansi/magenta timeout))
         (if (pos? failed)
           (str "\n" (ansi/white (pad-left 8 "Failed:"))  "  " (ansi/red failed)))
         (str "\n" (ansi/style (pad-left 8 "Success:")
                               #{:bold}) "  " (ansi/style
                                               (str passed " of " checks)
                                               #{:bold
                                                 (if (= passed checks)
                                                   :green
                                                   :red)})))
    "\n")
   (print/println "")))
