(ns code.manage.unit.isolate-test
  (:require [clojure.string :as str]
            [code.manage.unit.isolate :refer :all]
            [std.block :as block]
            [std.fs :as fs])
  (:use code.test))

(def +sample-test+
  (str "(ns example.core-test\n"
       "  (:use code.test))\n\n"
       "(def helper 1)\n\n"
       "(fact:global {:setup [(identity helper)]})\n\n"
       "^{:refer example.core/first :added \"0.1\"}\n"
       "(fact \"first\"\n"
       "  (+ helper 1)\n"
       "  => 2)\n\n"
       "^{:refer example.core/second :added \"0.1\"}\n"
       "(fact \"second\"\n"
       "  (+ helper 2)\n"
       "  => 4\n\n"
       "  (+ helper 3)\n"
       "  => 5)\n"))

(defn fact-count
  [s]
  (->> (top-level-entries s)
       (filter (comp fact-block? :block))
       count))

^{:refer code.manage.unit.isolate/unwrap-fact-block :added "4.1"}
(fact "returns the metadata prefix and inner fact block"
  (let [entry (->> (top-level-entries +sample-test+)
                   (filter (comp fact-block? :block))
                   second)]
    [(:prefix (unwrap-fact-block (:block entry)))
     (block/string (:block (unwrap-fact-block (:block entry))))])
  => ["^{:refer example.core/second :added \"0.1\"}\n"
      "(fact \"second\"\n  (+ helper 2)\n  => 4\n\n  (+ helper 3)\n  => 5)"])

^{:refer code.manage.unit.isolate/unwrap-meta-block :added "4.1"}
(fact "strips metadata wrappers to return the inner expression"
  (let [entry (->> (top-level-entries +sample-test+)
                   (filter (comp fact-block? :block))
                   first)]
    [(block/string (unwrap-meta-block (:block entry)))
     (block/string (unwrap-meta-block (block/parse-first "(def x 1)")))])
  => ["(fact \"first\"\n  (+ helper 1)\n  => 2)"
      "(def x 1)"])

^{:refer code.manage.unit.isolate/ns-block? :added "4.1"}
(fact "recognises ns forms and rejects others"
  (let [entries (top-level-entries +sample-test+)]
    [(ns-block? (:block (first entries)))
     (ns-block? (:block (second entries)))
     (ns-block? (block/parse-first "(fact \"x\" 1)"))])
  => [true false false])

^{:refer code.manage.unit.isolate/fact-block? :added "4.1"}
(fact "recognises fact forms and rejects others"
  (let [entries (top-level-entries +sample-test+)]
    [(fact-block? (:block (nth entries 3)))
     (fact-block? (:block (nth entries 2)))
     (fact-block? (:block (first entries)))])
  => ['fact nil nil])

^{:refer code.manage.unit.isolate/top-level-entries :added "4.1"}
(fact "returns all top level blocks with line info"
  [(count (top-level-entries +sample-test+))
   (mapv #(block/tag (:block %)) (top-level-entries +sample-test+))
   (-> (top-level-entries +sample-test+) first :line :row)]
  => [5 [:list :list :list :meta :meta] 1])

^{:refer code.manage.unit.isolate/rewrite-ns-form :added "4.1"}
(fact "rewrites the namespace symbol"
  (rewrite-ns-form (:block (first (top-level-entries +sample-test+)))
                   'example.core-fix-test)
  => "(ns example.core-fix-test (:use code.test))")

^{:refer code.manage.unit.isolate/latest-run-file :added "4.1"}
(fact "chooses the latest saved run file"
  (let [root  (str (fs/create-tmpdir "isolate-runs"))
        dir   (str root "/.hara/runs")]
    (try
      (fs/create-directory dir)
      (spit (str dir "/run-1.edn") "{}")
      (Thread/sleep 20)
      (spit (str dir "/run-2.edn") "{}")
      (latest-run-file root)
        (finally
          (fs/delete root))))
  => #".*run-2\.edn$")

^{:refer code.manage.unit.isolate/sanitize-run-string :added "4.1"}
(fact "quotes unreadable printed objects"
  (sanitize-run-string "{:fn #function[a/b] :obj #object[x 0x1 \"x@1\"] :w #<Wrap@1: \"v\">}")
  => "{:fn \"#function[a/b]\" :obj \"#object[x 0x1 \\\"x@1\\\"]\" :w \"#<Wrap@1: \\\"v\\\">\"}")

^{:refer code.manage.unit.isolate/parse-run-string :added "4.1"}
(fact "parses run reports and tags error payloads"
  (parse-run-string (sanitize-run-string "{:failed [{:actual #error {:cause \"x\"}}]}"))
  => {:failed [{:actual {:cause "x" :tag :error}}]})

^{:refer code.manage.unit.isolate/read-run-file :added "4.1"}
(fact "reads legacy run reports with unreadable printed values"
  (let [root  (str (fs/create-tmpdir "isolate-read"))
        dir   (str root "/.hara/runs")
        path  (str dir "/run-1.edn")]
    (try
      (fs/create-directory dir)
      (spit path
            (str "{:failed [{:ns example.core-test\n"
                 "           :line {:row 8}\n"
                 "           :checker {:fn #function[demo/fn]}\n"
                 "           :actual {:status :exception\n"
                 "                    :data #error {:cause \"bad\"}}\n"
                 "           :data [#object[java.lang.Object 0x1 \"java.lang.Object@1\"]\n"
                 "                  #<Wrapped@1: \"oops\">]}]}"))
      (let [entry (-> (read-run-file path) :failed first)]
        [(get-in entry [:checker :fn])
         (get-in entry [:actual :data :tag])
         (get-in entry [:data 0])
         (get-in entry [:data 1])])
      (finally
        (fs/delete root))))
  => ["#function[demo/fn]"
      :error
      "#object[java.lang.Object 0x1 \"java.lang.Object@1\"]"
      "#<Wrapped@1: \"oops\">"])

^{:refer code.manage.unit.isolate/failure-line :added "4.1"}
(fact "normalises failure line formats"
  (failure-line {:line 12})
  => 12

  (failure-line {:line {:row 8}})
  => 8)

^{:refer code.manage.unit.isolate/significant-entry? :added "4.1"}
(fact "treats expressions and symbols as significant"
  [(significant-entry? {:block (block/parse-first "(+ 1 1)")})
   (significant-entry? {:block (block/parse-first "=>")})]
  => [true true])

^{:refer code.manage.unit.isolate/arrow-entry? :added "4.1"}
(fact "identifies the => symbol"
  [(arrow-entry? {:block (block/parse-first "=>")})
   (arrow-entry? {:block (block/parse-first "+")})]
  => [true false])

^{:refer code.manage.unit.isolate/next-significant-index :added "4.1"}
(fact "finds the next non-whitespace entry"
  (let [entries (:body (fact-body-entries
                        {:block (block/parse-first "(fact \"x\"\n  (+ 1 1)\n  => 2)")
                         :line {:row 1 :col 1 :end-row 3 :end-col 8}}))]
    [(next-significant-index entries 0)
     (next-significant-index entries 4)])
  => [3 7])

^{:refer code.manage.unit.isolate/trim-right-void :added "4.1"}
(fact "drops trailing whitespace entries"
  (let [entries (body-entries (block/parse-first "(fact \"x\"\n  (+ 1 1)\n  => 2\n  )") 0)]
    [(count entries)
     (count (trim-right-void entries))
     (-> (trim-right-void entries) last :block block/tag)])
  => [16 13 :long])

^{:refer code.manage.unit.isolate/find-example-end :added "4.1"}
(fact "finds the end index of a fact example"
  (let [entries (:body (fact-body-entries
                        {:block (block/parse-first "(fact \"x\"\n  (+ 1 1)\n  => 2)")
                         :line {:row 1 :col 1 :end-row 3 :end-col 8}}))]
    (find-example-end entries 3))
  => 10)

^{:refer code.manage.unit.isolate/entry-matches-line? :added "4.1"}
(fact "checks whether an entry spans a target line"
  [(entry-matches-line? {:line {:row 10 :end-row 12}} #{11})
   (entry-matches-line? {:line {:row 10 :end-row 12}} #{9 13})]
  => [true nil])

^{:refer code.manage.unit.isolate/body-entries :added "4.1"}
(fact "returns child entries with adjusted line info"
  (let [entries (body-entries (block/parse-first "(fact \"x\"\n  (+ 1 1))") 5)]
    [(count entries)
     (-> entries first :line :row)
     (block/string (:block (nth entries 6)))])
  => [7 6 "(+ 1 1)"])

^{:refer code.manage.unit.isolate/fact-body-entries :added "4.1"}
(fact "splits a fact into operator, intro and body entries"
  (let [{:keys [op intro body]} (fact-body-entries
                                  {:block (block/parse-first "(fact \"x\"\n  (+ 1 1)\n  => 2)")
                                   :line {:row 1 :col 1 :end-row 3 :end-col 8}})]
    [(block/string (:block op))
     (block/string (:block intro))
     (count body)
     (block/string (:block (nth body 3)))])
  => ["fact" "\"x\"" 10 "(+ 1 1)"])

^{:refer code.manage.unit.isolate/example-ranges :added "4.1"}
(fact "returns index ranges for each fact example"
  (let [body (:body (fact-body-entries
                      (second (filter (comp fact-block? :block)
                                      (top-level-entries +sample-test+)))))]
    (example-ranges body))
  => [[3 14] [14 21]])

^{:refer code.manage.unit.isolate/selected-fact-body :added "4.1"}
(fact "builds a body string for selected examples"
  (let [body (:body (fact-body-entries
                      (second (filter (comp fact-block? :block)
                                      (top-level-entries +sample-test+)))))]
    (selected-fact-body body #{15} true))
  => "\n\n  (+ helper 2)\n  => 4")

^{:refer code.manage.unit.isolate/isolate-fact-string :added "4.1"}
(fact "rebuilds a fact with only selected examples"
  (let [entry (second (filter (comp fact-block? :block)
                              (top-level-entries +sample-test+)))]
    (isolate-fact-string entry #{18}))
  => "^{:refer example.core/second :added \"0.1\"}\n(fact \"second\"\n\n  (+ helper 3)\n  => 5)")

^{:refer code.manage.unit.isolate/failing-entries :added "4.1"}
(fact "selects failed/throw/timeout entries for the test namespace"
  (failing-entries {:failed [{:ns 'example.core-test :line 1}]
                    :throw [{:ns 'example.core-test :line 2}]
                    :timeout [{:ns 'example.core-test :line 3}]
                    :passed [{:ns 'example.core-test :line 4}]}
                   'example.core)
  => [{:ns 'example.core-test :line 1}
      {:ns 'example.core-test :line 2}
      {:ns 'example.core-test :line 3}])

^{:refer code.manage.unit.isolate/failure-function :added "4.1"}
(fact "prefers :function, then :name, then line"
  [(failure-function {:function 'example.core/foo})
   (failure-function {:name "foo"})
   (failure-function {:line 12})]
  => ['example.core/foo 'foo (symbol "12line-")])

^{:refer code.manage.unit.isolate/failure-functions :added "4.1"}
(fact "returns unique failure identifiers in order"
  (failure-functions [{:function 'a}
                      {:function 'a}
                      {:line 5}
                      {:name "b"}
                      {:line 5}])
  => ['a (symbol "5line-") 'b])

^{:refer code.manage.unit.isolate/isolate-target-ns :added "4.1"}
(fact "builds the suffixed target namespace"
  (isolate-target-ns 'example.core-test "-fix")
  => 'example.core-fix-test)

^{:refer code.manage.unit.isolate/isolate-string :added "4.1"}
(fact "keeps support forms and only the selected failing checks"
  (let [out (isolate-string +sample-test+
                            'example.core-fix-test
                            #{18})]
    [(-> out top-level-entries first :block block/value second)
     (str/includes? out "(def helper 1)")
     (str/includes? out "fact:global")
     (fact-count out)
     (str/includes? out "\"first\"")
     (str/includes? out "\"second\"")
     (str/includes? out "(+ helper 2)")
     (str/includes? out "(+ helper 3)")])
  => ['example.core-fix-test true true 1 false true false true])

^{:refer code.manage.unit.isolate/isolate :added "4.1"}
(fact "writes an isolated namespace from a saved run report"
  (let [root       (str (fs/create-tmpdir "isolate-task"))
        test-dir   (str root "/test/example")
        run-dir    (str root "/.hara/runs")
        test-path  (str test-dir "/core_test.clj")
        run-path   (str run-dir "/run-1.edn")
        lookup     {'example.core-test test-path}
        project    {:root root
                    :name 'example/demo
                    :version "0.1.0"
                    :source-paths ["src"]
                    :test-paths ["test"]}]
    (try
      (fs/create-directory test-dir)
      (fs/create-directory run-dir)
      (spit test-path +sample-test+)
      (spit run-path "{:failed [{:ns example.core-test :line 18 :function example.core/second}]}")
      (let [result      (isolate 'example.core-test
                                 {:run run-path
                                  :suffix "-fix"
                                  :write true
                                  :print {:function false}}
                                 lookup
                                 project)
            target-path (str root "/test/example/core_fix_test.clj")
            out         (slurp target-path)]
        [(:updated result)
         (-> out top-level-entries first :block block/value second)
         (fact-count out)
         (str/includes? out "\"second\"")
         (str/includes? out "\"first\"")
         (str/includes? out "(+ helper 2)")
         (str/includes? out "(+ helper 3)")
         (:functions result)])
       (finally
         (fs/delete root))))
  => [true 'example.core-fix-test 1 true false false true '[example.core/second]])
