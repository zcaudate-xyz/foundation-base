(ns code.manage.unit.import-test
  (:require [code.framework.common :as common]
            [code.framework.docstring :as docstring]
            [code.manage.unit.import :refer [analyse-test-code examples-config gather-fact]]
            [std.block.navigate :as nav])
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

^{:refer code.manage.unit.import/examples-config :added "4.1"}
(fact "parses `:examples` metadata into selection options"
  (examples-config {:examples [0 2 3 :no-check 4]})
  => {0 {:check true}
      2 {:check true}
      3 {:check false}
      4 {:check true}})

^{:refer code.manage.unit.import/gather-fact :added "4.1"}
(fact "imports only the intro by default"
  (-> +fact-with-examples+
      nav/parse-string
      nav/down nav/right nav/down nav/right
      gather-fact)
  => (contains {:added "0.1"
                :examples [0 2 3 :no-check 4]
                :intro "Sample test program"
                :test []}))

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

^{:refer code.manage.unit.import/analyse-test-code :added "4.1"}
(fact "does not copy `:examples` metadata into imported source metadata"
  (-> (analyse-test-code +fact-ns-with-examples+)
      (get-in '[example.core hello-world :meta]))
  => {:added "0.1"})
