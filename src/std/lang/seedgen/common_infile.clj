(ns std.lang.seedgen.common-infile
  (:require [code.framework :as base]
            [code.project :as project]
            [std.lang.seedgen.common-util :as common]
            [std.lib.result :as res]
            [std.task :as task]))

(defn seedgen-root
  "returns an explicit error result when the test file is missing
   (project/in-context
    (seed-infile/seedgen-root 'xt.sample.train-001-test {}))
   => :js
 
   (project/in-context
    (seed-infile/seedgen-root 'xt.sample.missing-test {}))
   => (contains {:status :error
                 :data :no-test-file})"
  {:added "4.1"}
  ([ns params lookup project]
   (let [test-ns   (project/test-ns ns)
         test-file (lookup test-ns)
         params    (task/single-function-print params)]
     (cond (nil? test-file)
           (res/result {:status :error
                        :data :no-test-file})

           :else
           (first (common/seedgen-root-langs test-file true))))))

(comment
  (code.project/in-context
   (std.lang.seedgen.common-infile/seedgen-root 'xt.sample.train-001-test
                                              {}))
  => :js)

(defn seedgen-list
  "returns an empty list when a test file only declares the seedgen root
   (project/in-context
    (seed-infile/seedgen-list 'xt.sample.train-001-test {}))
   => []
 
   (let [tmp (java.io.File/createTempFile \"seedgen-infile\" \".clj\")
         path (.getAbsolutePath tmp)
         lookup {'sample.multi-test path}]
     (try
       (spit path (str \"(ns sample.multi-test\\n\"
                       \"  (:use code.test)\\n\"
                       \"  (:require [std.lang :as l]))\\n\\n\"
                       \"^{:seedgen/root {:all true}}\\n\"
                       \"(l/script- :js {:runtime :basic})\\n\\n\"
                       \"(l/script- :lua {:runtime :basic})\\n\\n\"
                       \"(l/script- :python {:runtime :basic})\\n\"))
       (seed-infile/seedgen-list 'sample.multi-test {} lookup nil)
       (finally
         (.delete tmp))))
   => [:lua :python]"
  {:added "4.1"}
  ([ns params lookup project]
   (let [test-ns   (project/test-ns ns)
         test-file (lookup test-ns)
         params    (task/single-function-print params)]
     (cond (nil? test-file)
           (res/result {:status :error
                        :data :no-test-file})

           :else
           (common/seedgen-root-langs test-file false)))))

(comment
  (code.project/in-context
   (std.lang.seedgen.common-infile/seedgen-list '<sample>
                                              {}))
  => [:lua :python])

(defn seedgen-incomplete
  {:added "4.1"}
  ([ns params lookup project]
   (let [test-ns   (project/test-ns ns)
         test-file (lookup test-ns)
         params    (task/single-function-print params)]
     (cond (nil? test-file)
           (res/result {:status :error
                        :data :no-test-file})

           :else
           (let [analysis (base/analyse-file [:test test-file])]
             (cond (res/result? analysis)
                   analysis

                    :else
                    (let [fact-forms (common/seedgen-fact-forms test-file)
                          root-lang  (first (common/seedgen-root-langs test-file true))]
                      (->> analysis
                           vals
                          (mapcat seq)
                           (keep (fn [[_ {:keys [ns var test]}]]
                                   (let [refer (symbol (str ns) (str var))
                                         form  (get fact-forms refer (:sexp test))
                                         langs (common/seedgen-coverage-langs form)
                                         suppress (common/seedgen-suppressed-langs form)]
                                     (when (and (not (contains? suppress root-lang))
                                                (not (some #{root-lang} langs)))
                                       [refer
                                        {:status :incomplete
                                         :line (:line test)}]))))
                           (into (sorted-map))))))))))

(comment
  (code.project/in-context
   (std.lang.seedgen.common-infile/seedgen-incomplete <sample>
                                                    {}))
  => '{xt.lang.common-spec/example.B {:status ...}})
