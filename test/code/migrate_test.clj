(ns code.migrate-test
  (:require [clojure.string :as str]
            [code.migrate :refer :all]
            [code.migrate.catalog :as catalog]
            [code.migrate.test :as migrate-test])
  (:use code.test))

(def +cases+
  (:cases
   (catalog/read-edn
    "resources/code/migrate/conformance/bootstrap-pairs.edn")))

(def +source-unit+
  {:unit/kind :source
   :source/string (:case/input (first +cases+))})

(def +test-unit+
  {:unit/kind :test
   :source/string (:case/input (second +cases+))})

^{:refer code.migrate/migrate-pair :added "4.1"}
(fact "migrates source and test as one required pair"
  (let [pair (migrate-pair +source-unit+ +test-unit+)]
    [(set (keys pair))
     (mapv :diagnostics (vals pair))
     (str/includes? (get-in pair [:test :output]) "(Test/run")
     (str/includes? (get-in pair [:test :output])
                    "^{:refer example.core/add-one")
     (str/includes? (get-in pair [:test :output])
                    "[code.test.base.process :as process]")
     (not (str/includes? (get-in pair [:test :output])
                         "[code.test.work :as code.test]"))
     (str/includes? (get-in pair [:test :output])
                    "[:pass :status :error]")
     (str/includes? (get-in pair [:test :output])
                    "(defn migration-test-check")
     (str/includes? (get-in pair [:test :output])
                    "(println :failure")
     (str/includes? (get-in pair [:test :output]) ":actual")])
  => [#{:source :test} [[] []] true true true true true true true true])

^{:refer code.migrate.test/emit-test-run :added "4.1"}
(fact "keeps native tests and setup grouped by referred function"
  (let [source "(ns std.lib.zip-test (:require [std.lib.zip :refer :all]) (:use code.test))\n\n^{:refer std.lib.zip/left-element :added \"3.0\" :class [:zip/element]}\n(fact \"left\"\n  (identity :setup)\n  (+ 1 1) => 2\n  (+ 2 2) => 4)\n\n^{:refer std.lib.zip/right-element :added \"3.0\"}\n(fact \"right\")\n"
        migration-catalog (load-catalog)
        target (catalog/target-by-id migration-catalog
                                     :migration/std-lib-zip)
        output (:output (migrate-test/emit-test-run source
                                                    migration-catalog
                                                    target))]
    [(dec (count (str/split output #"\^\{:refer")))
     (dec (count (str/split output #"\(Test/run")))
     (str/includes? output
                    "^{:refer std.lib.zip/left-element :added \"3.0\" :class [:zip/element]}\n(do\n (identity :setup)\n (Test/run")
     (str/includes? output
                    "^{:refer std.lib.zip/right-element :added \"3.0\"}\n(Test/run [] migration-test-check)")
     (mapv #(str/includes? output %)
           [":name \"left #1\"" ":name \"left #2\""])])
  => [2 2 true true [true true]])

^{:refer code.migrate.test/rewrite-block-test-compat :added "4.1"}
(fact "preserves historical block string expectations portably"
  (mapv migrate-test/rewrite-block-test-compat
        '((construct/token 3/4)
          (->> (block-children x) (apply str))
          (->> (replace-children x ys) str)))
  => '[(construct/token-from-string "3/4" (/ 3.0 4.0))
       (apply str
              (map std.block.base/block-representation (block-children x)))
       (std.block.base/block-representation (replace-children x ys))]

  (migrate-test/rewrite-block-test-compat
   (keyword ":a")
   {:target/test-namespace 'std.block.type-test})
  => :std.block.type-test/a

  (mapv #(migrate-test/rewrite-block-test-compat
          % {:target/id :migration/std-block-navigate})
        '((str (navigator [1 2]))
          (-> (parse-string "x") left str)
          (-> (parse-string "x") left (str))))
  => '[(display-navigator (navigator [1 2]))
       (-> (parse-string "x") left display-navigator)
       (-> (parse-string "x") left display-navigator)]

  (migrate-test/rewrite-block-test-compat
   '(quote
     (clojure.core/defn -tag-
       ([zip] (-tag- zip :right))
       ([zip step]
        (clojure.core/if-let
         [elem (std.lib.zip/get zip)]
         (std.block.base/block-tag elem)))))
   {:target/id :migration/std-block-navigate})
  => '(quote
       (std.foundation/defn -tag-
         ([zip] (-tag- zip (std.foundation/keyword "right")))
         ([zip step]
          (std.foundation/if-let
           [elem (zip/get zip)]
           (std.block.base/block-tag elem))))))

^{:refer code.migrate/migrate-pair
  :id rejects-invalid-pair
  :added "4.1"}
(fact "rejects reversed or incomplete unit pairs"
  (migrate-pair +test-unit+ +source-unit+)
  => (throws clojure.lang.ExceptionInfo))
