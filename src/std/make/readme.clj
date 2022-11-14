(ns std.make.readme
  (:require [std.lib.os :as os]
            [std.lib.impl :refer [defimpl]]
            [std.lib.env :as env]
            [std.lib.foundation :as h]
            [std.lib.future :as f]
            [std.string :as str]
            [std.make.common :as common]))

(defn has-orgfile?
  "checks that an orgfile exists"
  {:added "4.0"}
  [{:keys [instance] :as mcfg}]
  (let [{:keys [root orgfile build]} @instance]
    (string? orgfile)))

(defn tangle-params
  "gets :tangle params"
  {:added "4.0"}
  ([{:keys [instance] :as mcfg}]
   (let [{:keys [root orgfile build]} @instance
         org-path (str root "/" orgfile)]
     (h/->> (slurp org-path)
            (str/split-lines)
            (map str/trim)
            (drop-while empty?)
            (take-while not-empty)
            (keep (fn [line]
                    (re-find #"\#\+([^\:]*):(.*)$" line)))
            (map (juxt (comp keyword str/lower-case second)
                       (comp str/trim second rest)))
            (into {})))))

(defn tangle-parse
  "parses an org file"
  {:added "4.0"}
  ([{:keys [instance] :as mcfg}]
   (let [{:keys [root orgfile build]} @instance
         org-path (str root "/" orgfile)]
     (if-not (.exists (java.io.File. org-path))
       [root []]
       (let [out-file (fn [header]
                        (let [tokens (str/split header #"\s+")
                              i (.indexOf ^java.util.List tokens ":tangle")]
                          (if (pos? i)
                            (nth tokens (inc i)))))
             lines (str/split-lines (slurp org-path))
             [acc _] (reduce (fn [[acc curr] line]
                               (cond (str/starts-with? line "#+BEGIN_SRC")
                                     [acc [{:header line :out (out-file line)}]]
                                     
                                     (str/starts-with? line "#+END_SRC")
                                     [(conj acc curr) nil]

                                     (nil? curr)
                                     [acc nil]

                                     :else
                                     [acc (conj curr line)]))
                             [[] nil]
                             lines)]
         [root acc])))))

(defn tangle
  "tangles a file"
  {:added "4.0"}
  ([mcfg]
   (let [[root blocks]   (tangle-parse mcfg)
         blocks  (filter (comp :out first) blocks)
         dir-fn  (fn [^String s] (subs s 0 (.lastIndexOf s "/")))
         dirs    (set (map (comp dir-fn :out first) blocks))
         _       @(f/on:all (mapv (fn [d] (os/sh "mkdir" "-p" d {:async true
                                                                 :root root}))
                                  dirs))]
     (mapv (fn [[{:keys [out]} & lines]]
             (doto (str root "/" out) (spit (str/join "\n" lines))))
           blocks))))

(defn make-readme-raw
  "simple filter to strip out `* Build` block"
  {:added "4.0"}
  ([s]
   (let [lines    (str/split-lines s)
         [acc curr] (reduce (fn [[acc curr] line]
                              (if (str/starts-with? line "* ")
                                [(conj acc curr) [line]]
                                [acc (conj curr line)]))
                            [[] [(first lines)]]
                            (rest lines))
         all      (conj acc curr)
         no-build (remove (fn [[header]]
                            (str/starts-with? header "* Build"))
                          all)]
     (->> (mapcat identity no-build)
          (str/join "\n")))))

(defn make-readme
  "makes a README.md from Main.org"
  {:added "4.0"}
  ([{:keys [instance] :as mcfg}]
   (let [{:keys [root orgfile build]} @instance
         org-path (str root "/" orgfile)]
     (if-not (.exists (java.io.File. org-path))
       nil
       (let [content  (->> (slurp org-path)
                           (make-readme-raw))
             out     (try  (-> (os/sh "pandoc" "-f" "org" "-t" "gfm" "-o" "README.md"
                                      {:wait false
                                       :root (str root "/" build)})
                               (os/sh-write content)
                               (os/sh-close)
                               (os/sh-wait)
                               (os/sh-output))
                           (catch Throwable t
                             (env/prn "Pandoc not found")))]
         out)))))
