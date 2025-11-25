(ns code.test.base.print
  (:require [std.print.ansi :as ansi]
            [std.string :as str]
            [std.fs :as fs]
            [code.test.checker.common :as checker]
            [code.test.base.runtime :as rt]
            [code.test.diff :as diff]
            [std.lib.walk :as walk]
            [std.print :as print]
            [std.pretty :as pretty]))

(defonce ^:dynamic *options* #{:print-thrown :print-failure :print-bulk})

(defn- rel
  [path]
  (cond (and rt/*root* path)
        (fs/relativize rt/*root* path)

        :else path))

(defn pad [s n]
  (let [len (count s)]
    (if (< len n)
      (str (apply str (repeat (- n len) " ")) s)
      s)))

(defn print-success
  "outputs the description for a successful test"
  {:added "3.0"}
  ([{:keys [path name ns line desc form check] :as summary}]
   (let [line (if line (str "L:" line " @ ") "")]
     (print/println
      "\n"
      (str (ansi/style "Success" #{:green :bold})
           (ansi/style (format "  %s%s" line (or (rel path) "<current>")) #{:bold})
           (if name (str "\n   " (ansi/white "Refer") "  " (ansi/style name #{:bold})) "")
           (if desc (str "\n    " (ansi/white "Info") "  \"" desc "" \") "")
           (str "\n    " (ansi/white "Form") "  " (str/indent-rest (pretty/pprint-str form) 10))
           (str "\n   " (ansi/white "Check") "  " check))))))

(defn format-diff-map [diff indent]
  (let [missing (:+ diff)
        extra   (:- diff)
        changed (:> diff)]
    (str/join "\n"
              (concat
               (for [[k v] missing]
                 (str (apply str (repeat indent " ")) (ansi/green "+ ") (pr-str k) " " (pr-str v)))
               (for [[k v] extra]
                 (str (apply str (repeat indent " ")) (ansi/red "- ") (pr-str k) " " (pr-str v)))
               (for [[k v] changed]
                 (str (apply str (repeat indent " ")) (ansi/yellow "> ") (pr-str k) " " (pr-str v)))))))

(defn format-diff-seq [diff indent]
  (if (vector? diff)
    (str/join "\n"
              (for [[op & args] diff]
                (str (apply str (repeat indent " "))
                     (case op
                       :+ (str (ansi/green "+") " at " (first args) ": " (pr-str (second args)))
                       :- (str (ansi/red "-") " at " (first args) ": " (second args) " items")
                       (pr-str [op args])))))
    (str/indent (pretty/pprint-str diff) indent)))

(defn format-diff [diff]
  (cond (and (map? diff) (or (:+ diff) (:- diff) (:> diff)))
        (format-diff-map diff 4)

        (vector? diff)
        (format-diff-seq diff 4)

        :else
        (str/indent (pretty/pprint-str diff) 4)))

(defn print-failure
  "outputs the description for a failed test"
  {:added "3.0"}
  ([{:keys [path name ns line desc form check compare actual replace original parent checker] :as summary}]
   (let [line (if line (str "L:" line " @ ") "")
         bform  (walk/postwalk-replace replace form)
         bcheck (walk/postwalk-replace replace check)
         pattern? (or (not= bform form) (not= bcheck check))
         expect (or (:expect checker) check)
         diff   (if (not compare) (diff/diff expect actual))]
     (print/println
      (str (ansi/style "Failure" #{:red :bold})
           (ansi/style (format "  %s%s" line (or (rel path) "<current>")) #{:bold})
           (if name (str "\n   " (ansi/white "Refer") "  " (ansi/style name #{:bold})) "")
           (if desc (str "\n    " (ansi/white "Info") "  \"" desc "" \") "")
           (str "\n    " (ansi/white "Form") "  " (str/indent-rest (pretty/pprint-str bform) 10))
           (if compare
             (str "\n   " (ansi/white "Compare") "  \n"
                  (str/indent (pretty/pprint-str compare)
                              4))
             (str
              (str "\n   " (ansi/white "Check") "  " (str/indent-rest (pretty/pprint-str bcheck) 10))
              (str "\n  " (ansi/white "Result") "  " (if (coll? actual)
                                                       (str/indent-rest (pretty/pprint-str actual) 10)
                                                       actual))))
           (if pattern? (str "\n " (ansi/white "Pattern") "  " (ansi/blue (str form " : " check))))
           (if original (str "\n  " (ansi/white "Linked") "  " (ansi/blue (format "L:%d,%d"
                                                                                  (:line original)
                                                                                  (:column original)))))
           (if parent (str "\n  " (ansi/white (pad "Parent" 7)) "  " (ansi/blue (str parent))))
           "\n")))))

(defn print-thrown
  "outputs the description for a form that throws an exception"
  {:added "3.0"}
  ([{:keys [path name ns line desc form replace original actual parent data] :as summary}]
   (let [line (if line (str "L:" line " @ ") "")
         bform (walk/postwalk-replace replace form)
         pattern? (not= bform form)
         data (or data actual)
         data (if (and (map? data) (:status data))
                (:data data)
                data)]
     (print/println
      (str (ansi/style " Thrown" #{:yellow :bold})
           (ansi/style (format "  %s%s" line (or (rel path) "<current>")) #{:bold})
           (if name (str "\n   " (ansi/white "Refer") "  " (ansi/style name #{:bold})) "")
           (if desc (str "\n    " (ansi/white "Info") "  \"" desc "\"") "")
           (str "\n    " (ansi/white "Form") "  " (str/indent-rest (pretty/pprint-str bform) 10))
           (str "\n   " (ansi/white "Error") "  " (if (instance? Throwable data)
                                                    (or (.getMessage ^Throwable data) data)
                                                    data))
           (if (not= bform form) (str " :: " form))
           (if pattern? (str "\n " (ansi/white "Pattern") "  " (ansi/blue (str form))))
           (if original (str "\n  " (ansi/white "Linked") "  " (ansi/blue (format "L:%d,%d"
                                                                                  (:line original)
                                                                                  (:column original)))))
           (if parent (str "\n  " (ansi/white "Parent") "  " (ansi/blue (str parent))))
           "\n")))))

(defn print-timedout
  "outputs the description for a form that has timed out"
  {:added "4.0"}
  ([{:keys [path name ns line desc form replace original actual parent data] :as summary}]
   (let [line (if line (str "L:" line " @ ") "")
         bform (walk/postwalk-replace replace form)
         pattern? (not= bform form)
         data (or data actual)
         data (if (and (map? data) (:status data))
                (:data data)
                data)]
     (print/println
      (str (ansi/style "Timed Out" #{:red :bold})
           (ansi/style (format "  %s%s" line (or (rel path) "<current>")) #{:bold})
           (if name (str "\n   " (ansi/white "Refer") "  " (ansi/style name #{:bold})) "")
           (if desc (str "\n    " (ansi/white "Info") "  \"" desc "\"") "")
           (str "\n    " (ansi/white "Form") "  " (str/indent-rest (pretty/pprint-str bform) 10))
           (str "\n    " (ansi/white "Data") "  " (if (instance? Throwable data)
                                                    (or (.getMessage ^Throwable data) data)
                                                    data))
           (if (not= bform form) (str " :: " form))
           (if pattern? (str "\n " (ansi/white "Pattern") "  " (ansi/blue (str form))))
           (if original (str "\n  " (ansi/white "Linked") "  " (ansi/blue (format "L:%d,%d"
                                                                                  (:line original)
                                                                                  (:column original)))))
           (if parent (str "\n  " (ansi/white "Parent") "  " (ansi/blue (str parent))))
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
         errors (->> ops (filter #(-> % :type (= :exception))))
         thrown (count errors)]
     (if (or (*options* :print-facts-success)
             (not (and (= num total)
                       (pos? thrown))))
       (print/println
        (str (ansi/style "   Fact" #{:blue :bold})
             (ansi/style (str "  [" (or path "<current>") line "]") #{:bold})
             (if name (str "\n   " (ansi/white (pad "Refer" 7)) "  " (ansi/style name #{:highlight :bold})) "")
             (if desc (str "\n   " (ansi/white (pad "Info" 7)) "  \"" desc "" \") "")
             (str "\n  " (ansi/white (pad "Passed" 7)) "  "
                  (str (ansi/style num (if (= num total) #{:blue} #{:green}))
                       " of "
                       (ansi/blue total)))
             (if (pos? thrown)
               (str "\n  " (ansi/white (pad "Thrown" 7)) "  " (ansi/yellow thrown))
               ""))
        "\n")))))

(defn print-summary
  "outputs the description for an entire test run"
  {:added "3.0"}
  ([{:keys [files thrown facts checks passed failed timedout] :as result}]
   (print/println
    (str (ansi/style (str "Summary (" files ")") #{:blue :bold})
         (str "\n  " (ansi/white " Files") "  " (ansi/blue files))
         (str "\n  " (ansi/white " Facts") "  " (ansi/blue facts))
         (str "\n  " (ansi/white "Checks") "  " (ansi/blue checks))
         (str "\n  " (ansi/white "Passed") "  " ((if (= passed checks)
                                                   ansi/blue
                                                   ansi/yellow) passed))
         (str "\n  " (ansi/white "Thrown") "  " ((if (pos? thrown)
                                                   ansi/yellow
                                                   ansi/blue) thrown))
         (if (and timedout (pos? timedout))
           (str "\n  " (ansi/white "Timeout") " " (ansi/red timedout))
           ""))
    "\n") (if (pos? failed)
            (print/println
             (ansi/style (str "Failed  (" failed ")") #{:red :bold})
             "\n")

            (print/println
             (ansi/style (str "Success (" passed ")") #{:cyan :bold})
             "\n")) (print/println "")))

