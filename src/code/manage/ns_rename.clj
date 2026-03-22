(ns code.manage.ns-rename
  (:require [code.framework :as base]
            [code.project :as project]
            [std.fs :as fs]
            [std.print :as print]
            [std.print.ansi :as ansi]
            [std.text.diff :as text.diff]))

(defn- subs-trim
  ([s end]
   (subs-trim s 0 end))
  ([s start end]
   (subs s start (- (count s) end))))

(defn- remove-suffix
  [^String s]
  (if (< 0 (.indexOf s "."))
    (subs s 0 (.lastIndexOf s "."))
    s))

(defn- dot-pattern
  [^String s]
  (.replaceAll s "\\." "\\\\."))

(defn- replace-fn
  [^String old ^String new]
  (fn [^String original]
    (.replaceAll original
                 (dot-pattern (str old))
                 (str new))))

(defn move-list
  "compiles a list of file movements"
  {:added "3.0"}
  ([[old new] {:keys [write print] :as params} _ {:keys [source-paths test-paths root]}]
   (let [old (fs/ns->file old)
         new (fs/ns->file new)
         list-fn (fn [path] (->> (fs/select (fs/path root path)
                                            {:include [old]})
                                 (map str)
                                 (map (juxt identity #(.replace ^String % old new)))))
         mlist (mapcat list-fn (concat source-paths test-paths))
         simulate (not write)]
     (if (:function print)
       (print/print-title (format "MOVES (%s)" (count mlist))))
     (mapv (fn [i [old new]]
             (if (:function print)
               (print/println
                (ansi/blue (format "%4s" (inc i)))
                (ansi/bold (str (fs/relativize root new)))
                '<=
                (str (fs/relativize root old))))
             (fs/move old new {:simulate simulate}))
           (range (count mlist))
           mlist)
     mlist)))

(defn change-list
  "compiles a list of code changes"
  {:added "3.0"}
  ([[old new] {:keys [write print] :as params} lookup project]
   (let [transform (replace-fn old new)
         changes (->> (sort (keys lookup))
                      (keep (fn [id]
                              (let [change (base/transform-code id {:transform transform
                                                                    :print {:function false}
                                                                    :write write
                                                                    :full true}
                                                                lookup project)]
                                (when (seq (:deltas change))
                                  [id change])))))]
     (when (:function print)
       (print/print-title (format "CHANGES (%s" (count changes)))
       (print/print "\n")
       (doseq [[id change] changes]
         (print/print (format (ansi/style (str (:path change)) #{:blue :bold}))
                      "\n")
         (print/print (text.diff/->string (:deltas change))
                      "\n")))
     changes)))

(defn ns-rename
  "top-level ns rename function"
  {:added "3.0"}
  ([[old new] {:keys [write print] :as params} lookup project]
   (let [changes (change-list [old new] params lookup project)
         moves   (move-list [old new] params lookup project)]
     {:changes changes
      :moves moves})))

(comment

  (project/in-context (refactor '[code.manage.refactor
                                  code.manage.refactoring]
                                {:print {:function true}}))

  (project/in-context (refactor '[jvm.artifact.bitstream
                                  jvm.artifact.bytes]
                                {:print {:function true}})))
