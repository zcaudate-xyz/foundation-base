(ns code.framework
  (:require [std.lib.walk :as walk]
            [code.framework.cache :as cache]
            [code.framework.common :as common]
            [code.framework.docstring :as docstring]
            [code.framework.test.clojure]
            [code.framework.test.fact]
            [code.framework.text :as text]
            [code.edit :as nav]
            [code.query :as query]
            [std.fs :as fs]
            [code.project :as project]
            [std.text.diff :as text.diff]
            [std.string :as str]
            [std.print.ansi :as ansi]
            [std.lib.result :as res]
            [std.lib :as h :refer [definvoke]]))

(def ^:dynamic *toplevel-forms*
  '#{defn
     defn.origin
     defn.c
     defn.js
     defn.gl
     defn.lua
     defn.R
     defn.py
     defn.pg
     deftype.pg
     defpolicy.pg
     defn.sh
     defn.sol
     defn.xt
     defgen.js
     defgen.lua
     defgen.py
     defgen.xt
     defmulti
     defmacro
     defmacro.!
     defmacro.c
     defmacro.js
     defmacro.gl
     defmacro.lua
     defmacro.R
     defmacro.pg
     defmacro.py
     defmacro.sh
     defmacro.sol
     defmacro.xt
     defvar.xt
     defvar.js
     defcache
     definvoke
     defmemoize
     deftask})

(defn import-selector
  "creates an import selector
 
   (import-selector '-hello-)
   ;;[(#{<options>} | -hello- string? map? & _)]
   => vector?"
  {:added "3.0"}
  ([] (import-selector '_))
  ([var]
   [(list *toplevel-forms* '| var '^:%?- string? '^:%?- map? '& '_)]))

(defn toplevel-selector
  "creates a selector for querying toplevel forms
 
   (toplevel-selector '-hello-)
   ;;[(#{<def forms>} | -hello- & _)]
   => vector?"
  {:added "3.0"}
  ([] (toplevel-selector '_))
  ([var]
   [(list *toplevel-forms* '| var '& '_)]))

(defn analyse-source-function
  "helper function for `analyse-source-code`"
  {:added "3.0"}
  ([nsp nav]
   (let [id   (nav/value nav)
         line (-> nav nav/up nav/line-info)
         nav (if (= :string (nav/tag (nav/right nav)))
               (nav/delete-right nav)
               nav)
         nav (if (= :map (nav/tag (nav/right nav)))
               (nav/delete-right nav)
               nav)
         code (-> nav nav/up nav/string)]
     [id {:ns nsp
          :var id
          :source {:code code
                   :line line
                   :path common/*path*}}])))

(defn analyse-source-code
  "analyses a source file for namespace and function definitions
 
   (-> (analyse-source-code (slurp \"test-data/code.manage/src/example/core.clj\"))
       (get-in '[example.core -foo-]))
   => '{:ns example.core,
        :var -foo-,
        :source {:code \"(defn -foo-\\n  [x]\\n  (println x \\\"Hello, World!\\\"))\",
                 :line {:row 3, :col 1, :end-row 6, :end-col 31},
                 :path nil}}"
  {:added "3.0"}
  ([s]
   (let [nav  (-> (nav/parse-root s)
                  (nav/down))
         nsp  (->  (query/$ nav [(ns | _ & _)] {:walk :top})
                   first)
         fns  (->> (query/$* nav (toplevel-selector) {:return :zipper :walk :top})
                   (map (partial analyse-source-function nsp))
                   (into {}))]
     (common/entry {nsp fns}))))

(defn find-test-frameworks
  "find test frameworks given a namespace form
   (find-test-frameworks '(ns ...
                            (:use code.test)))
   => #{:fact}
 
   (find-test-frameworks '(ns ...
                            (:use clojure.test)))
   => #{:clojure}"
  {:added "3.0"}
  ([ns-form]
   (let [folio (atom #{})]
     (walk/postwalk (fn [form]
                      (if-let [k (common/test-frameworks form)]
                        (swap! folio conj k)))
                    ns-form)
     @folio)))

(defn analyse-test-code
  "analyses a test file for docstring forms
 
   (-> (analyse-test-code (slurp \"test-data/code.manage/test/example/core_test.clj\"))
       (get-in '[example.core -foo-])
       (update-in [:test :code] docstring/->docstring))
   => (contains '{:ns example.core
                  :var -foo-
                  :test {:code \"1\\n  => 1\"
                         :line {:row 6 :col 1 :end-row 7 :end-col 16}
                         :path nil}
                 :meta {:added \"3.0\"}
                  :intro \"\"})"
  {:added "3.0"}
  ([s]
   (let [nav        (-> (nav/parse-root s)
                        (nav/down))
         nsloc      (query/$ nav [(ns | _ & _)] {:walk :top
                                                 :return :zipper
                                                 :first true})

         nsp        (nav/value nsloc)
         ns-form    (-> nsloc nav/up nav/value)
         frameworks (find-test-frameworks ns-form)]
     (->> frameworks
          (map (fn [framework] (common/analyse-test framework nav)))
          (apply h/merge-nested)
          (common/entry)))))

(definvoke analyse-file
  "helper function for analyse, taking a file as input
 
   (analyse-file [:source \"src/code/framework.clj\"])"
  {:added "3.0"}
  [:recent {:compare (comp fs/last-modified second)}]
  ([[type path]]
   (binding [common/*path* path]
     (case type
       :source (analyse-source-code (slurp path))
       :test   (analyse-test-code (slurp path))))))

(defn analyse
  "seed analyse function for the `code.manage/analyse` task
 
   (->> (project/in-context (analyse 'code.framework-test))
        (common/display-entry))
   => (contains-in {:test {'code.framework
                           (contains '[analyse
                                       analyse-file
                                       analyse-source-code])}})
 
   (->> (project/in-context (analyse 'code.framework))
        (common/display-entry))
   => (contains-in {:source {'code.framework
                             (contains '[analyse
                                         analyse-file
                                         analyse-source-code])}})"
  {:added "3.0"}
  ([ns {:keys [output sorted] :as params} lookup project]
   (let [path   (lookup ns)
         result  (cache/fetch ns path)
         nresult (or result
                     (and path (analyse-file [(project/file-type path) path])))
         _    (if (nil? result)
                (cache/update ns path nresult))
         nresult (cond (and (nil? nresult)
                            (nil? path))
                       (cond (= type :test)
                             (res/result {:status :error
                                          :data :no-test-file})

                             (= type :source)
                             (res/result {:status :error
                                          :data :no-test-file}))

                       (= output :map)
                       nresult

                       (= output :vector)
                       (let [nresult (mapcat vals (vals nresult))]
                         (if (true? sorted)
                           (sort-by :var nresult)
                           (sort-by #(or (-> % :source :line :row)
                                         (-> % :test :line :row)) nresult)))

                       :else
                       nresult)]
     nresult)))

(defn extract
  "returns all vars in a given namespace
   (project/in-context (vars {:sorted true}))
   => (contains '[analyse
                  analyse-file
                  analyse-source-code
                  analyse-source-function])"
  {:added "3.0"}
  ([ns {:keys [process] :as params} lookup project]
   (let [path     (lookup ns)
         code     (slurp path)
         results  (process code)]
     (if (seq results)
       results))))

(defn var-function
  "constructs a var, with or without namespace
 
   ((var-function true) {:ns 'hello :var 'world})
   => 'hello/world
 
   ((var-function false) {:ns 'hello :var 'world})
   => 'world"
  {:added "3.0"}
  ([full]
   (fn [{:keys [ns var] :as m}]
     (with-meta (if (true? full)
                  (symbol (str ns) (str var))
                  var)
       m))))

(defn vars
  "returns all vars in a given namespace
   (project/in-context (vars {:sorted true}))
   => (contains '[analyse
                  analyse-file
                  analyse-source-code
                  analyse-source-function])"
  {:added "3.0"}
  ([ns {:keys [full sorted] :as params} lookup project]
   (let [analysis (analyse ns {:output :vector :sorted sorted} lookup project)]
     (cond (res/result? analysis)
           analysis

           :else
           (mapv (var-function full) analysis)))))

(definvoke read-ns-form
  "memoised version of fs/read-ns"
  {:added "4.0"}
  [:recent {:compare fs/last-modified}]
  ([path]
   (first (fs/read-code path))))

(defn no-test
  "checks that a namespace does not require test"
  {:added "4.0"}
  ([ns params lookup project]
   (let [source-file (lookup ns)
         ns-form     (read-ns-form source-file)]
     (:no-test (meta ns-form)))))

(defn docstrings
  "returns all docstrings in a given namespace with given keys
 
   (->> (project/in-context (docstrings))
        (map first)
        sort)
   => (project/in-context (vars {:sorted true}))"
  {:added "3.0"}
  ([ns {:keys [full sorted]} lookup project]
   (let [ns       (project/test-ns ns)
         analysis (analyse ns {:output :vector :sorted sorted} lookup project)]
     (cond (res/result? analysis) analysis

           :else
           (h/map-juxt [(var-function full)
                        (comp docstring/->docstring :code :test)]
                       analysis)))))

(defn transform-code
  "transforms the code and performs a diff to see what has changed
 
   ;; options include :skip, :full and :write
   (project/in-context (transform-code 'code.framework {:transform identity}))
   => (contains {:changed []
                 :updated false
                 :path any})"
  {:added "3.0"}
  ([ns {:keys [write print skip transform verify full no-analysis] :as params} lookup {:keys [root] :as project}]
   (cond skip
         (let [path  (lookup ns)
               text  (if (fs/exists? path) "" (slurp path))]
           (spit path (transform text))
           {:updated true :path path})

         :else
         (let [path     (lookup ns)
               params   (assoc params :output :map)
               [original analysis] (if (fs/exists? path)
                                     [(slurp path) (if no-analysis
                                                     {}
                                                     (analyse ns params lookup project))]
                                     ["" {}])
               revised  (transform original)
               verified   (or (first (keep
                                      (fn [[k f]]
                                        (try
                                          (f revised)
                                          nil
                                          (catch Throwable t
                                            (h/prn t)
                                            [k false])))
                                      verify))
                              true)
               deltas   (if no-analysis
                          (text.diff/diff original revised)
                          (text/deltas ns analysis original revised))
               path     (str (fs/relativize root path))
               updated  (when (and write (seq deltas))
                          (spit (fs/path root path) revised)
                          true)
               _        (when (and (:function print) (seq deltas))
                          (h/local :print (str "\n" (ansi/style path #{:bold :blue :underline}) "\n\n"))
                          (h/local :print (text.diff/->string deltas) "\n"))]
           (cond-> (if no-analysis
                     (assoc (text.diff/summary deltas)
                            :changed (text.diff/get-lines deltas))
                     (text/summarise-deltas deltas))
             full  (assoc :deltas deltas)
             :then (assoc :updated (boolean updated)
                          :verified verified
                          :path (str path)))))))

(defn locate-code
  "finds code base upon a query"
  {:added "3.0"}
  ([ns {:keys [print query] :as params} lookup {:keys [root] :as project}]
   (let [path     (lookup ns)
         code     (slurp path)
         analysis (analyse ns params lookup project)
         line-lu  (common/line-lookup ns analysis)
         results  (->> (query/$* (nav/parse-root code)
                                 query
                                 {:return :zipper})
                       (mapv nav/line-info))]
     (if (seq results)
       (let [lines (str/split-lines code)
             path  (str (fs/relativize root path))]
         (when (:function print)
           (h/local :print (str "\n" (ansi/style path #{:bold :blue :underline}) "\n\n"))
           (h/local :print (text/->string results lines line-lu params) "\n"))
         results)))))

;; GREP

(defn compile-regex
  "compiles a regex string from an input string
 
   (compile-regex \"(:require\")
   => \"\\\\(:require\""
  {:added "3.0"}
  ([obj]
   (cond (h/regexp? obj) obj

         (string? obj)
         (h/-> (str obj)
               (.replaceAll "\\(" "\\\\(")
               (.replaceAll "\\(" "\\\\(")
               (.replaceAll "\\{" "\\\\{")
               (.replaceAll "\\}" "\\\\}")
               (.replaceAll "\\[" "\\\\[")
               (.replaceAll "\\]" "\\\\]")
               (.replaceAll "\\+" "\\\\+")
               (.replaceAll "\\*" "\\\\*")
               (.replaceAll "\\." "\\\\.")
               (.replaceAll "\\^" "\\\\^")
               (.replaceAll "\\$" "\\\\$")
               (.replaceAll "\\?" "\\\\?"))

         :else (compile-regex (str obj)))))

(defn search-regex
  "constructs a search regex (for line numbers)
 
   (search-regex \"hello\")
   => #\"^(.*?)(hello)\""
  {:added "3.0"}
  ([obj]
   (re-pattern (str "^(.*?)" "(" (compile-regex obj) ")"))))

(defn grep-search
  "finds code based on string query
 
   (project/in-context (grep-search {:query '[docstrings]
                                     :print {:function true}}))"
  {:added "3.0"}
  ([ns {:keys [print query] :as params} lookup {:keys [root] :as project}]
   (let [query    (search-regex query)
         path     (lookup ns)
         code     (slurp path)
         analysis (analyse ns params lookup project)
         line-lu  (common/line-lookup ns analysis)
         lines    (str/split-lines code)
         results  (keep (fn [[i line]]
                          (if-let [[_ start match] (re-find query line)]
                            (let [col (inc (count start))]
                              {:row i :end-row i
                               :col col
                               :end-col (+ col (count match))})))
                        (map vector (iterate inc 1) lines))]
     (if (seq results)
       (let [path  (str (fs/relativize root path))]
         (when (:function print)
           (h/local :print (str "\n" (ansi/style path #{:bold :blue :underline}) "\n\n"))
           (h/local :print (text/->string results lines line-lu params) "\n"))
         results)))))

(defn grep-replace
  "replaces code based on string query
 
   (project/in-context (grep-replace {:query \"docstrings\"
                                      :replace \"[DOCSTRINGS]\"
                                     :print {:function true}}))"
  {:added "3.0"}
  ([ns {:keys [print query replace] :as params} lookup {:keys [root] :as project}]
   (let [query    (compile-regex query)
         transform-fn (fn [original]
                        (->> (str/split-lines original)
                             (map (fn [line] (str/replace line query replace)))
                             (str/join "\n")))
         params (assoc params :transform transform-fn)]
     (transform-code ns params lookup project))))

;; REFACTOR

(defn- all-string [nav]
  (def ^:dynamic *nav* nav)
  (->> (iterate nav/right* nav)
       (take 10)
       (map #(doto % (-> nav/string prn)))
       (map nav/string)
       (str/joinl)
       (println))
  (h/error "oeoeu"))

(defn refactor-code
  "takes in a series of edits and performs them on the code
 
   (project/in-context (refactor-code {:edits []}))
   => {:changed [], :updated false, :path \"test/code/framework_test.clj\"}"
  {:added "3.0"}
  ([ns {:keys [edits] :as params} lookup project]
   (let [;;funcs (map +transform+ edits)
         transform (fn [original]
                     (reduce (fn [text f]
                               (nav/root-string (f (nav/parse-root text))))
                             original
                             edits))
         params  (assoc params :transform transform)]
     (transform-code ns params lookup project))))

(comment
  (./incomplete 'code.framework.docstring)

  (project/in-context (locate-code 'code.format.ns
                                   {:query code.format.ns/+load-has-shorthand+
                                    :print {:function true}}))
  
  (project/in-context (locate-parent 'code.format.ns
                                     {:query code.format.ns/+load-has-shorthand+
                                      :print {:function true}}))

  (project/in-context (locate-parent 'code.format.ns
                                     {:query code.format.ns/+ns-has-use-form+
                                      :print {:function true}}))

  (project/in-context (locate-parent 'code.format.ns
                                     {:query code.format.ns/+require-has-refer+
                                      :print {:function

                                              true}})))

(comment

  (./reset '[hara.block code.manage])
  (def a* (analyse-source-code (slurp "src/hara/code/framework.clj")))

  (def a* (analyse-test-code (slurp "test/hara/code/framework_test.clj")))

  (-> (nav/parse-root (slurp "test/hara/code/base_test.clj"))
      (query/select [{:is 'docstrings}])
      (first))

  (-> (nav/parse-root (slurp "test/hara/code/base_test.clj"))
      (query/select [{:is 'docstrings}])
      (first)
      (nav/up)
      (nav/up)
      (nav/up)
      (nav/up)
      (nav/up)
      (nav/left)
      (nav/left)
      (nav/left)
      (nav/left)
      (nav/left)
      (nav/find-next-token :code)
      (nav/find-next-token :code)
      (nav/next)
      ;(nav/right-expression)
      (nav/height))

  13

  (-> (nav/parse-root (slurp "test-data/code.manage/src/example/core.clj"))
      (query/select [{:is 'foo}])))
