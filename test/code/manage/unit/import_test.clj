(ns code.manage.unit.import-test
  (:require [code.framework.common :as common]
            [code.framework.docstring :as docstring]
            [code.manage.unit.import :as cmi
             :refer [analyse
                     analyse-fact-tests
                     analyse-test-code
                     arrow-block?
                     examples-config
                     find-example-end
                     gather-fact
                     gather-fact-body
                     gather-selected-fact-body
                     next-significant-index
                     significant-block?
                     strip-check
                     trim-right-void]]
            [std.block :as block]
            [std.block.navigate :as nav]
            [std.fs :as fs]
            [std.lib.result :as res])
  (:use code.test))

(def +fact-with-examples+
  (str "^{:refer example.core/hello-world :added \"0.1\" :examples [0 2 3 :no-check 4]}\n"
       "(fact \"Sample test program\"\n"
       "  ^{:hello 0}        ;; meta is preserved\n"
       "  (inc 1) => 2       ;; make sure comments are preserved\n"
       "\n"
       "  (inc 2) => 5\n"
       "\n"
       "  ^{:hello 2}\n"
       "  (single-form-example)\n"
       "\n"
       "  ^{:hello 3}\n"
       "  (no-check-example)\n"
       "  => :no-result\n"
       "\n"
       "  (inc 4)\n"
       "  => 5)"))

(def +fact-ns-with-examples+
  (str "(ns example.core-test\n"
       "  (:use code.test))\n"
       "\n"
       +fact-with-examples+))

(def +selected-docstring+
  "\n  ^{:hello 0}        ;; meta is preserved
  (inc 1) => 2       ;; make sure comments are preserved

  ^{:hello 2}
  (single-form-example)

  ^{:hello 3}
  (no-check-example)

  (inc 4)
  => 5")

(def +full-docstring+
  "\n  ^{:hello 0}        ;; meta is preserved
  (inc 1) => 2       ;; make sure comments are preserved

  (inc 2) => 5

  ^{:hello 2}
  (single-form-example)

  ^{:hello 3}
  (no-check-example)
  => :no-result

  (inc 4)
  => 5")

(def +sample-source+
  "(ns example.core)\n\n(defn hello-world []\n  :hello)")

(def +sample-test+
  (str "(ns example.core-test\n"
       "  (:use code.test))\n\n"
       "^{:refer example.core/hello-world :added \"0.1\"}\n"
       "(fact \"hello\"\n"
       "  (hello-world)\n"
       "  => :hello)"))

^{:refer code.manage.unit.import/examples-config :added "4.1"}
(fact "parses `:examples` metadata into selection options"
  (examples-config {:examples [0 2 3 :no-check 4]})
  => {0 {:check true}
      2 {:check true}
      3 {:check false}
      4 {:check true}})

^{:refer code.manage.unit.import/significant-block? :added "4.1"}
(fact "recognises code blocks and excludes whitespace/comments"
  (let [blocks (->> (nav/parse-root "  (+ 1 1)\n  => 2\n  ;; comment\n  (- 3 1)")
                    nav/down
                    (iterate nav/right*)
                    (map nav/block)
                    (take-while some?)
                    vec)]
    (significant-block? (nth blocks 2))  => true
    (significant-block? (nth blocks 6))  => true
    (significant-block? (nth blocks 0))  => false
    (significant-block? (nth blocks 12)) => false))

^{:refer code.manage.unit.import/arrow-block? :added "4.1"}
(fact "identifies the => arrow symbol"
  (let [blocks (->> (nav/parse-root "  (+ 1 1)\n  => 2")
                    nav/down
                    (iterate nav/right*)
                    (map nav/block)
                    (take-while some?)
                    vec)]
    (arrow-block? (nth blocks 2)) => false
    (arrow-block? (nth blocks 6)) => true
    (arrow-block? (nth blocks 8)) => false))

^{:refer code.manage.unit.import/next-significant-index :added "4.1"}
(fact "finds the next non-void/non-comment block at or after an index"
  (let [blocks (->> (nav/parse-root "  (+ 1 1)\n  => 2\n  ;; comment\n  (- 3 1)")
                    nav/down
                    (iterate nav/right*)
                    (map nav/block)
                    (take-while some?)
                    vec)]
    (next-significant-index blocks 0)  => 2
    (next-significant-index blocks 3)  => 6
    (next-significant-index blocks 9)  => 16
    (next-significant-index blocks 17) => nil))

^{:refer code.manage.unit.import/trim-right-void :added "4.1"}
(fact "removes trailing whitespace blocks from a vector"
  (let [blocks (->> (nav/parse-root "  (+ 1 1)\n  => 2")
                    nav/down
                    (iterate nav/right*)
                    (map nav/block)
                    (take-while some?)
                    vec)]
    (map block/string (trim-right-void (subvec blocks 0 8)))
    => [" " " " "(+ 1 1)" "\n" " " " " "=>"]))

^{:refer code.manage.unit.import/strip-check :added "4.1"}
(fact "removes the => arrow and everything after it from an example"
  (let [blocks (->> (nav/parse-root "  (+ 1 1)\n  => 2\n  ;; comment\n  (- 3 1)")
                    nav/down
                    (iterate nav/right*)
                    (map nav/block)
                    (take-while some?)
                    vec)]
    (map block/string (strip-check (subvec blocks 2 7)))
    => ["(+ 1 1)"]
    (map block/string (strip-check blocks))
    => [" " " " "(+ 1 1)"]))

^{:refer code.manage.unit.import/find-example-end :added "4.1"}
(fact "finds the end index of an example expression"
  (let [blocks (->> (nav/parse-root "  (+ 1 1)\n  => 2\n  ;; comment\n  (- 3 1)")
                    nav/down
                    (iterate nav/right*)
                    (map nav/block)
                    (take-while some?)
                    vec)]
    (find-example-end blocks 2)  => 16
    (find-example-end blocks 9)  => 16
    (find-example-end blocks 16) => 17)
  (let [blocks (->> (nav/parse-root "  (+ 1 1)\n  (+ 2 2)")
                    nav/down
                    (iterate nav/right*)
                    (map nav/block)
                    (take-while some?)
                    vec)]
    (find-example-end blocks 2) => 6))

^{:refer code.manage.unit.import/gather-selected-fact-body :added "4.1"}
(fact "builds a filtered fact body using `:examples` metadata"
  (let [n (-> +fact-with-examples+ nav/parse-string nav/down nav/right nav/down nav/right)]
    (-> (gather-selected-fact-body (nav/right* n) (common/gather-meta n))
        docstring/->docstring))
  => +selected-docstring+)

^{:refer code.manage.unit.import/gather-fact-body :added "4.1"}
(fact "falls back to the full fact body when `*test-full*` is enabled"
  (binding [common/*test-full* true]
    (-> +fact-with-examples+
        nav/parse-string
        nav/down nav/right nav/down nav/right
        gather-fact
        (update-in [:test] docstring/->docstring)
        :test))
  => +full-docstring+)

^{:refer code.manage.unit.import/gather-fact :added "4.1"}
(fact "filters imported fact examples using `:examples` metadata when enabled"
  (binding [common/*test-examples* true]
    (-> +fact-with-examples+
        nav/parse-string
        nav/down nav/right nav/down nav/right
        gather-fact
        (update-in [:test] docstring/->docstring)))
  => (contains {:added "0.1"
                :examples [0 2 3 :no-check 4]
                :test +selected-docstring+}))

^{:refer code.manage.unit.import/analyse-fact-tests :added "4.1"}
(fact "collects fact test data for each referred var"
  (let [result (binding [common/*test-examples* true]
                 (analyse-fact-tests (-> +fact-ns-with-examples+ nav/parse-root nav/down)))]
    (-> result (get-in '[example.core hello-world :ns]))           => 'example.core
    (-> result (get-in '[example.core hello-world :var]))          => 'hello-world
    (-> result (get-in '[example.core hello-world :intro]))        => "Sample test program"
    (-> result (get-in '[example.core hello-world :meta]))         => {:added "0.1"}
    (-> result (get-in '[example.core hello-world :test :path]))   => nil
    (-> result (get-in '[example.core hello-world :test :form]))   => 'fact
    (-> result (get-in '[example.core hello-world :test :code]) docstring/->docstring)
    => +selected-docstring+))

^{:refer code.manage.unit.import/analyse-test-code :added "4.1"}
(fact "does not copy `:examples` metadata into imported source metadata"
  (-> (analyse-test-code +fact-ns-with-examples+)
      (get-in '[example.core hello-world :meta]))
  => {:added "0.1"})

^{:refer code.manage.unit.import/analyse :added "4.1"}
(fact "analyses a test namespace through the file lookup"
  (let [result (with-redefs [slurp (constantly +fact-ns-with-examples+)]
                 (analyse 'example.core-test
                          {:examples true}
                          {'example.core-test "test/example/core_test.clj"}
                          {:root "."}))]
    (-> result (get-in '[example.core hello-world :ns]))         => 'example.core
    (-> result (get-in '[example.core hello-world :var]))        => 'hello-world
    (-> result (get-in '[example.core hello-world :meta]))       => {:added "0.1"}
    (-> result (get-in '[example.core hello-world :test :path])) => "test/example/core_test.clj"
    (-> result (get-in '[example.core hello-world :test :code]) docstring/->docstring)
    => +selected-docstring+))

^{:refer code.manage.unit.import/import :added "4.1"}
(fact "returns an error when the source file is missing"
  (let [result (with-redefs [slurp (constantly +sample-test+)]
                 (cmi/import 'example.core
                         {}
                         {'example.core-test "test/example/core_test.clj"}
                         {:root "."}))]
    (res/result? result) => true
    (:data result)       => :no-source-file))

^{:refer code.manage.unit.import/import :added "4.1"}
(fact "returns an error when the test file is missing"
  (let [result (with-redefs [slurp (constantly +sample-source+)]
                 (cmi/import 'example.core
                         {}
                         {'example.core "src/example/core.clj"}
                         {:root "."}))]
    (res/result? result) => true
    (:data result)       => :no-test-file))

^{:refer code.manage.unit.import/import :added "4.1"}
(fact "transforms the source file with imported docstrings"
  (let [lookup {'example.core       "src/example/core.clj"
                'example.core-test  "test/example/core_test.clj"}
        slurp-fn (fn [path]
                   (condp = path
                     "src/example/core.clj"       +sample-source+
                     "test/example/core_test.clj" +sample-test+
                     (slurp path)))]
    (with-redefs [slurp slurp-fn
                  fs/exists? (constantly true)]
      (cmi/import 'example.core {:bulk true} lookup {:root "."})))
  => (contains {:changed '[hello-world]
                :verified true
                :path "src/example/core.clj"}))