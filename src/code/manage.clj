(ns code.manage
  (:require [code.framework :as base]
            [code.manage.fn-format :as fn-format]
            [code.manage.ns-rename :as ns-rename]
            [code.manage.ns-format :as ns-format]
            [code.manage.var :as var]
            [code.manage.unit.template :as template]
            [code.manage.unit :as unit]
            [code.manage.unit.require :as unit.require]
            [code.project :as project]
            [std.block :as block]
            [std.task :as task]
            [std.lib :as h :refer [definvoke]]
            [std.lib.result :as res])
  (:refer-clojure :exclude [import])
  (:gen-class))

(h/intern-in ns-rename/ns-rename)

(defmethod task/task-defaults :code
  ([_]
   template/code-default))

(defmethod task/task-defaults :code.transform
  ([_]
   template/code-transform))

(defmethod task/task-defaults :code.locate
  ([_]
   template/code-locate))

(definvoke analyse
  "analyse either a source or test file
 
   (analyse 'code.manage)
   ;;#code{:test {code.manage [analyse .... vars]}}
   => code.framework.common.Entry
 
   (analyse '#{code.manage} {:return :summary})
   ;; {:errors 0, :warnings 0, :items 1, :results 1, :total 16}
   => map?"
  {:added "3.0"}
  [:task {:template :code
          :params  {:title "ANALYSE NAMESPACE"
                    :parallel true
                    :print {:result false :summary false}
                    :sorted true}
          :main    {:fn #'base/analyse}
          :item    {:display #(->> (vals %) (mapcat keys) sort vec)}
          :result  {:ignore  nil
                    :keys    {:count #(->> (vals %) (mapcat keys) count)
                              :functions #(->> (vals %) (mapcat keys) sort vec)}
                    :columns (template/code-default-columns :functions #{:bold})}}])

(comment (code.manage/analyse ['code.format] {:print {:item true :result true :summary true}}))

(definvoke extract
  "returns the list of vars in a namespace
 
   (vars 'code.manage)
 
   (vars 'code.manage {:sorted false})
 
   (vars '#{code.manage} {:return #{:errors :summary}})
   => (contains-in {:errors any
                    :summary {:errors 0
                              :warnings 0
                              :items 1
                              :results 1
                              :total number?}})"
  {:added "3.0"}
  [:task {:template :code
          :params {:title "PROCESS"
                   :parallel true
                   :sorted true
                   :process identity
                   :print {:result false :summary false}}
          :main {:fn #'base/extract}
          :item {:display identity}
          :result {:columns (template/code-default-columns :data #{:bold})}}])

(definvoke vars
  "returns the list of vars in a namespace
 
   (vars 'code.manage)
 
   (vars 'code.manage {:sorted false})
 
   (vars '#{code.manage} {:return #{:errors :summary}})
   => (contains-in {:errors any
                    :summary {:errors 0
                              :warnings 0
                              :items 1
                              :results 1
                              :total number?}})"
  {:added "3.0"}
  [:task {:template :code
          :params {:title "NAMESPACE VARS"
                   :parallel true
                   :sorted true
                   :print {:result false :summary false}}
          :main {:fn #'base/vars}
          :item {:display identity}
          :result {:columns (template/code-default-columns :data #{:bold})}}])

(comment (code.manage/vars ['code.format] {:print {:item true :result true :summary true}}))

(definvoke docstrings
  "returns docstrings
 
   (docstrings '#{code.manage.unit} {:return :results})
   ;;{:errors 0, :warnings 0, :items 1, :results 1, :total 14}
   => map?"
  {:added "3.0"}
  [:task {:template :code
          :params {:title "VAR DOCSTRINGS"
                   :parallel true
                   :print {:result false :summary false}}
          :main   {:fn #'base/docstrings}
          :item   {:display (comp (template/empty-status :info :none) vec keys)}
          :result {:keys  {:count (comp count keys)
                           :functions (comp vec keys)}
                   :columns (template/code-default-columns :functions #{:bold})}}])

(comment (code.manage/docstrings ['code.format] {:print {:item true :result true :summary true}}))

(definvoke transform-code
  "helper function for any arbitrary transformation of text
 
   (transform-code {:transform #(str % \"\\n\\n\\n\\nhello world\")})
   ;; {:deletes 0, :inserts 5, :changed [arrange], :updated false}
   => map?
 
   (transform-code '#{code.manage.unit}
                   {:print {:summary true :result true :item true :function true}
                    :transform #(str % \"\\n\\n\\n\\nhello world\")
                    :full true})"
  {:added "3.0"}
  [:task {:template :code.transform
          :params {:title "TRANSFORM CODE"
                   :parallel true}
          :main   {:fn #'base/transform-code}
          :result template/base-transform-result}])

(comment
  (into {} transform-code)
  (transform-code ['code.framework]
                  {:transform #(str % "\n\n\n\nhello world")
                   :print {:summary true :result true :item true :function true}}))

(definvoke heal-code
  "helper function for any arbitrary transformation of text
 
   (transform-code {:transform #(str % \"\\n\\n\\n\\nhello world\")})
   ;; {:deletes 0, :inserts 5, :changed [arrange], :updated false}
   => map?
 
   (transform-code '#{code.manage.unit}
                   {:print {:summary true :result true :item true :function true}
                    :transform #(str % \"\\n\\n\\n\\nhello world\")
                    :full true})"
  {:added "3.0"}
  [:task {:template :code.transform
          :params {:title "HEAL CODE"
                   :transform block/heal
                   :no-analysis true
                   :print {:function true :result true :summary true}
                   :parallel true}
          :main   {:fn #'base/transform-code}
          :result template/base-transform-result}])


(definvoke import
  "import docstrings from tests
 
   (import {:write false})
 
   (import {:full true
            :write false
            :print {:function false}})
 
   (import '[code.manage.unit]
           {:print {:summary true :result true :item true}
            :write false})"
  {:added "3.0"}
  [:task {:template :code.transform
          :main   {:fn #'unit/import}
          :params {:title "IMPORT DOCSTRINGS"
                   :parallel true
                   :write true}
          :item   {:list template/source-namespaces}
          :result (template/code-transform-result :changed)}])

(comment (code.manage/import ['code.framework] {:print {:item true :result true :summary true}}))

(definvoke purge
  "removes docstrings from source code
 
   (purge {:write false})
 
   (purge {:full true :write false})
 
   (purge '[platform.unit] {:return :summary :write false})
   ;;{:items 38, :results 32, :deletes 1272, :total 169}
   => map?"
  {:added "3.0"}
  [:task {:template :code.transform
          :main   {:fn #'unit/purge}
          :params {:title "PURGE DOCSTRINGS"
                   :parallel true}
          :item {:list template/source-namespaces}
          :result (assoc (template/code-transform-result :changed)
                         :columns (template/code-transform-columns #{:bold :red}))}])

(comment (code.manage/purge ['code.manage] {:print {:item true :result true :summary true}}))

(definvoke missing
  "checks for functions with missing tests
 
   (missing)
 
   (missing '[platform] {:print {:result false :summary false}
                         :return :all})"
  {:added "3.0"}
  [:task {:template :code
          :params {:title "MISSING TESTS"}
          :main {:fn #'unit/missing}
          :item {:list template/source-namespaces}
          :result {:columns (template/code-default-columns :data #{:green})}}])

(comment (code.manage/missing ['code.manage] {:print {:item true :result true :summary true}}))

(definvoke todos
  "checks for tests with `TODO` as docstring
 
   (todos)
 
   (todos '[platform] {:print {:result false :summary false}
                       :return :all})"
  {:added "3.0"}
  [:task {:template :code
          :params {:title "TODO TESTS"}
          :main {:fn #'unit/todos}
          :item {:list template/test-namespaces}
          :result {:columns (template/code-info-columns #{:green})}}])

(comment (code.manage/todos ['code.manage] {:print {:item true :result true :summary true}}))

(definvoke incomplete
  "both functions missing tests or tests with todos
 
   (incomplete)
 
   (incomplete '[code.manage] {:print {:item true}})"
  {:added "3.0"}
  [:task {:template :code
          :params {:title "INCOMPLETE TESTS"
                   :parallel true
                   :print {:item false}}
          :main   {:fn #'unit/incomplete}
          :item   {:list template/source-namespaces}
          :result {:columns (template/code-default-columns #{:bold :green})}}])

(comment (code.manage/incomplete ['code.manage] {:print {:item true :result true :summary true}}))

(definvoke orphaned
  "tests without corresponding source code
 
   (orphaned)
 
   (orphaned '[code.manage] {:print {:item true}})"
  {:added "3.0"}
  [:task {:template :code
          :params {:title "ORPHANED TESTS"
                   :parallel true}
          :main {:fn #'unit/orphaned}
          :item {:list template/test-namespaces}
          :result {:columns (template/code-info-columns #{:bold :blue})}}])

(comment (code.manage/orphaned ['code.manage] {:print {:item true :result true :summary true}}))

(definvoke scaffold
  "creates a scaffold for a new or existing set of tests
 
   (scaffold {:write false})
 
   (scaffold '[code.manage] {:print {:item true}
                             :write false})"
  {:added "3.0"}
  [:task {:template :code.transform
          :params {:title "CREATE TEST SCAFFOLD"
                   :parallel true
                   :write true}
          :main {:fn #'unit/scaffold}
          :item {:list template/source-namespaces
                 :display (template/empty-result :new :info :no-new)}
          :result (template/code-transform-result :new)}])

(comment (code.manage/scaffold ['code.framework] {:print {:function true :item true :result true :summary true}}))

(definvoke create-tests
  "creates and arranges the tests"
  {:added "3.0"}
  [:task {:template :code.transform
          :params {:title "CREATE TESTS"
                   :parallel true
                   :write true}
          :main {:fn #'unit/create-tests}
          :item {:list template/source-namespaces
                 :display (template/empty-result :new :info :no-new)}
          :result (template/code-transform-result :new)}])

(defn- compare-status [arr]
  (cond (empty? arr)
        (res/result {:status :info
                     :data :ok})

        :else
        (res/result {:status :warn
                     :data :not-in-order})))

(defn- compare-columns
  [orig compare]
  [{:key    :key
    :align  :left}
   {:key    :count
    :length 8
    :align  :center
    :color  #{:bold}}
   {:key    :test
    :align  :left
    :length 60
    :color  orig}
   {:key    :source
    :align  :left
    :length 60
    :color  compare}])

(definvoke in-order?
  "checks if tests are in order
 
   (in-order?)
 
   (in-order? '[code.manage] {:print {:item true}})"
  {:added "3.0"}
  [:task {:template :code
          :params {:title "TESTS IN ORDER"
                   :parallel true}
          :main {:fn #'unit/in-order?}
          :item {:list template/source-namespaces
                 :display compare-status}
          :result {:keys {:count first
                          :source second
                          :test #(nth % 2)}
                   :columns (compare-columns #{:bold :cyan} #{:cyan})}}])

(comment (code.manage/in-order? ['code.framework] {:print {:function true :item true :result true :summary true}}))

(definvoke arrange
  "arranges the test corresponding to function order
 
   (arrange {:print {:function false}
             :write false})
 
   (arrange '[code.manage] {:print {:item true}
                            :write false})"
  {:added "3.0"}
  [:task {:template :code.transform
          :params {:title "ARRANGE TESTS"
                   :parallel true
                   :write true}
          :main {:fn #'unit/arrange}
          :item {:list template/test-namespaces}
          :result (template/code-transform-result :changed)}])

(comment (code.manage/arrange ['code.framework] {:print {:function true :item true :result true :summary true}}))

(definvoke locate-code
  "locates code base upon query"
  {:added "3.0"}
  [:task {:template :code.locate
          :params {:title "LOCATE CODE"
                   :parallel true}
          :main {:fn #'base/locate-code}}])

(comment
  (code.manage/locate-code '[code.framework]
                           {:query ['comment]
                            :print {:function true :item true :result true :summary true}})
  (code.manage/locate-code '[hara]
                           {:query [(list '#{defn defmacro} '_ '^:%?- string? '^:%?- map? 'vector? '& '_)]
                            :print {:function true :item true :result true :summary true}}))

(definvoke locate-test
  "locates test based upon query"
  {:added "4.0"}
  [:task {:template :code.locate
          :params {:title "LOCATE TEST"
                   :parallel true}
          :item {:list template/test-namespaces}
          :main {:fn #'base/locate-code}}])


(definvoke grep
  "finds a string or regular expression in files
 
   (grep '[code.manage] {:query \"hello\"})"
  {:added "3.0"}
  [:task {:template :code.locate
          :params {:title "GREP"
                   :parallel true
                   :highlight true}
          :main {:fn #'base/grep-search}}])

(comment (code.manage/grep '[code.framework]
                           {:query "comment"
                            :print {:function true :result true}}))

(definvoke grep-replace
  "grep and replaces in files
 
   (grep-replace '[code.manage] {:query \"hello\"
                                 :replace \"HELLO\"})"
  {:added "3.0"}
  [:task {:template :code.transform
          :params {:title "GREP REPLACE"
                   :parallel true
                   :print {:function true}}
          :main {:fn #'base/grep-replace}
          :result (template/code-transform-result :changed)}])

(comment (code.manage/grep-replace '[code.framework]
                                   {:query "comment"
                                    :replace "comment 111111"
                                    :print {:function true :result true :write false}}))

(definvoke unclean
  "finds source code that has top-level comments
 
   (unclean 'code.manage)
 
   (unclean '[hara])"
  {:added "3.0"}
  [:task {:template :code.locate
          :params {:title "SOURCE CODE WITH COMMENTS"
                   :parallel true
                   :query '[comment]}
          :main {:fn #'base/locate-code}
          :result {:columns (template/code-default-columns :source #{:red :bold})}}])

(comment
  (code.manage/unclean '[code.framework] {:print {:function true :item true :result true :summary true}}))

(definvoke unchecked
  "returns tests without `=>` checks
 
   (unchecked)
 
   (unchecked '[code.manage])"
  {:added "3.0"}
  [:task {:template :code
          :params {:title "FACT HAS NO `=>` FORMS"
                   :parallel true}
          :main {:fn #'unit/unchecked}
          :item {:list template/source-namespaces}
          :result {:columns (template/code-info-columns #{:magenta :bold})}}])

(definvoke commented
  "returns tests that are in comment blocks
 
   (commented)
 
   (commented '[code.manage])"
  {:added "3.0"}
  [:task {:template :code
          :params {:title "REFERENCED VAR IN COMMENT FORM"
                   :parallel true}
          :main {:fn #'unit/commented}
          :item {:list template/source-namespaces}
          :result {:columns (template/code-info-columns #{:white :bold})}}])

(definvoke pedantic
  "returns tests that may be improved
 
   (pedantic)
 
   (pedantic '[code.manage])"
  {:added "3.0"}
  [:task {:template :code
          :params {:title "FUNCTIONS THAT COULD BE IMPROVED UPON"
                   :parallel true}
          :main {:fn #'unit/pedantic}
          :item {:list template/source-namespaces}
          :result {:columns (template/code-default-columns
                             :data #{:warn :bold}
                             (fn [items]
                               (->> items
                                    (map (fn [sym]
                                           (let [{:keys [row tag]
                                                  :or {row 0}} (meta sym)]
                                             (if (= tag :N)
                                               [row sym]
                                               [row sym (symbol (name tag))]))))
                                    (sort-by first)
                                    (vec))))}}])

(definvoke refactor-code
  "refactors code based on given `:edits`
 
   (refactor-code '[code.manage]
                  {:edits []})"
  {:added "3.0"}
  [:task {:template :code.transform
          :params {:title "REPLACE VAR USAGES"
                   :parallel true
                   :print {:function true}}
          :main   {:fn #'base/refactor-code}
          :result template/base-transform-result}])

(definvoke refactor-test
  "refactors code tests based on given `:edits`"
  {:added "4.0"}
  [:task {:template :code.transform
          :params {:title "REPLACE VAR USAGES"
                   :parallel true
                   :print {:function true}}
          :main   {:fn #'base/refactor-code}
          :item {:list template/test-namespaces}
          :result template/base-transform-result}])

(defn refactor-swap
  "refactors by providing a list of symbols to swap"
  {:added "4.0"}
  [ns params narrow update-fn]
  (refactor-code
   ns
   (std.lib/merge-nested
    {:print {:function true}
     :edits [(fn [nav]
               (code.query/modify
                nav
                [narrow]
                (fn [nav]
                  (-> nav
                      (std.block.navigate/swap update-fn)))))]}
    params)))

(comment
  (refactor-code '[hara]
                 {:edits [fn-format/fn:list-forms]
                  #_#_:write true})
  
  (refactor-code '[hara]
                 {:edits [fn-format/fn:defmethod-forms]
                  :write true}))

(definvoke ns-format
  "formats ns forms"
  {:added "3.0"}
  [:task {:template :code.transform
          :params {:title "FORMAT NS FORMS"
                   :parallel true
                   :print {:function true}}
          :main   {:fn #'ns-format/ns-format}
          :result template/base-transform-result}])

(definvoke find-usages
  "find usages of a var
 
   (find-usages '[code.manage]
                {:var 'code.framework/analyse})"
  {:added "3.0"}
  [:task {:template :code.locate
          :params {:title "ALL VAR USAGES"
                   :parallel true
                   :highlight? true}
          :main {:fn #'var/find-usages}}])

(comment (find-usages '[code.manage] {:print {:item true} :var 'code.framework/analyse}))

(definvoke require-file
  "requires the file and returns public vars

   (require-file 'code.manage)
   => (contains '[analyse extract ...])"
  {:added "3.0"}
  [:task {:template :code
          :params {:title "REQUIRE FILE"
                   :parallel true}
          :main {:fn #'unit.require/require-file}
          :item {:display identity}
          :result {:columns (template/code-default-columns :data #{:bold})}}])

(def +tasks+
  {:analyse       analyse
   :extract       extract
   :vars          vars
   :docstrings    docstrings
   :transform-code transform-code
   :import        import
   :purge         purge
   :missing       missing
   :todos         todos
   :incomplete    incomplete
   :orphaned      orphaned
   :scaffold      scaffold
   :create-tests  create-tests
   :in-order      in-order?
   :arrange       arrange
   :locate-code   locate-code
   :locate-test   locate-test
   :grep          grep
   :grep-replace  grep-replace
   :unclean       unclean
   :unchecked     unchecked
   :commented     commented
   :pedantic      pedantic
   :refactor-code refactor-code
   :refactor-test refactor-test
   :ns-format     ns-format
   :find-usages   find-usages
   :require-file  require-file
   :heal-code     heal-code})

(defn -main
  "main entry point for code.manage

   (code.manage/-main \"import\" \"[xyz.zcaudate]\" \"{:tag :all}\")"
  {:added "3.0"}
  [& [cmd & args]]
  (let [print-fn (fn []
                   (do (h/p "Available Tasks:")
                       (doseq [cmd  (map name (sort (keys +tasks+)))]
                         (h/p (str "  - " cmd)))))]
    (if (not cmd)
      (print-fn)
      
      (let [opts (task/process-ns-args args)
            func (ns-resolve (find-ns 'code.manage) (symbol cmd))
            args (mapv (fn [x] (try (read-string x) (catch Throwable _ x))) args)]
        (if func
          (func (or (:ns opts) :all) (merge {:print {:function true
                                                     :summary true
                                                     :result true
                                                     :item true}}
                                            (dissoc opts :ns)))
          (print-fn))
        (if-not (get opts :no-exit)
          (System/exit 0))))))




(comment

  (definvoke replace-usages
    "replace usages of a var
 
   (replace-usages '[code.manage]
                   {:var 'code.framework/analyse
                   :new 'analyse-changed})"
    {:added "3.0"}
    [:task {:template :code.transform
            :params {:title "REPLACE VAR USAGES"
                     :print {:item false
                             :function true}}
            :main   {:fn #'var/replace-usages}
            :result (template/code-transform-result :changed)}])
  
  (definvoke replace-refers
    "TODO"
    {:added "3.0"}
    [:task {:template :code.transform
            :params {:title "REPLACE REFERS"
                     :print {:item false
                             :function true}}
            :main   {:fn #'var/replace-refers}
            :result (template/code-transform-result :changed)}])
  
  (definvoke replace-errors
    "TODO"
    {:added "3.0"}
    [:task {:template :code.transform
            :params {:title "REPLACE ERRORS"
                     :print {:item false
                             :function true}}
            :main   {:fn #'var/replace-errors}
            :result (template/code-transform-result :changed)}])
  
  (definvoke list-ns-unused
    "TODO"
    {:added "3.0"}
    [:task {:template :code
            :params {:title "LIST UNUSED NS ENTRIES"
                     :print {:item true}}
            :item {:list template/source-namespaces}
            :main   {:fn #'var/list-ns-unused}}])
  
  (definvoke remove-ns-unused
    "TODO"
    {:added "3.0"}
    [:task {:template :code.transform
            :params {:title "REMOVE UNUSED NS ENTRIES"
                     :print {:function true}}
            :main   {:fn #'var/remove-ns-unused}
            :result template/base-transform-result}])

  (definvoke rename-ns-abbrevs
    "TODO"
    {:added "3.0"}
    [:task {:template :code.transform
            :params {:title "RENAME NS ABBREVS"
                     :print {:function true}}
            :main {:fn #'var/rename-ns-abbrevs}
            :result template/base-transform-result}])

  (definvoke refactor-ns-forms
    "refactors and reorganises ns forms
 
   (refactor-ns-forms '[code.manage])"
    {:added "3.0"}
    [:task {:template :code.transform
            :params {:title "TRANSFORMING NS FORMS"
                     :print {:function true}}
            :main {:fn #'ns-form/refactor}
            :result template/base-transform-result}])

  (comment
    (code.manage/refactor-ns-forms '[code.manage] {:write true}))

  (definvoke lint
    [:task {:template :code.transform
            :params {:title "LINTING CODE"
                     :print {:function true}}
            :main   {:fn #'lint/lint}
            :result template/base-transform-result}])

  (comment
    (code.manage/lint 'code.manage {:write true}))

  (definvoke line-limit
    [:task {:template :code
            :params {:title "LINES EXCEEDING LIMIT"
                     :print {:function true}}
            :main   {:fn #'lint/line-limit}}])

  (comment
    (time (analyse 'code.framework-test))
    (time (def a (analyse 'code.framework-test)))
    (time (def a (analyse 'code.manage)))
    (code.framework.cache/purge)
    (./reset '[code.manage])
    (./reset '[code.manage])
    (./incomplete '[code.manage])

    (vars 'code.manage)
    (vars '[thing] {:print {:item true}})
    (./import)
    (code.manage/line-limit ['hara] {:length 110})))


