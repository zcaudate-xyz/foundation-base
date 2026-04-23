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

^{:refer code.manage.unit.isolate/failure-line :added "4.1"}
(fact "normalises failure line formats"
  (failure-line {:line 12})
  => 12

  (failure-line {:line {:row 8}})
  => 8)

^{:refer code.manage.unit.isolate/isolate-target-ns :added "4.1"}
(fact "builds the suffixed target namespace"
  (isolate-target-ns 'example.core-test "-fix")
  => 'example.core-fix-test)

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
