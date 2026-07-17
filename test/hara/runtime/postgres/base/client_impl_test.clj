(ns hara.runtime.postgres.base.client-impl-test
  (:require [lib.jdbc :as jdbc]
            [lib.postgres :as base]
            [lib.postgres.connection :as conn]
            [hara.runtime.postgres.base.client :as client]
            [hara.runtime.postgres.base.client-impl :as client-impl]
            [postgres.core.addon :as addon]
            [postgres.core.builtin :as builtin]
            [postgres.sample.scratch-v1 :as scratch]
            [hara.common.util :as ut]
            [std.lib.component :as component])
  (:use code.test))

^{:refer hara.runtime.postgres.base.client-impl/raw-eval-pg-return :added "4.0"}
(fact "returns a regularised result"

  (client-impl/raw-eval-pg-return [{:pg_jit_available true}])
  => true

  (client-impl/raw-eval-pg-return [{:result "{\"a\": 1}"}])
  => {:a 1})

^{:refer hara.runtime.postgres.base.client-impl/raw-eval-pg :added "4.0"
  :setup [(def -pg- (client/rt-postgres {:dbname "test-scratch"
                                         :temp :create}))]
  :teardown (component/stop -pg-)}
(fact "executes a raw value"

  (binding [conn/*execute* jdbc/fetch]
    (client-impl/raw-eval-pg -pg- "select 1;"))
  => [{:?column? 1}])

^{:refer hara.runtime.postgres.base.client-impl/init-ptr-pg :added "4.0"
  :setup [(def -pg- (client/rt-postgres {:dbname "test-scratch"
                                         :mode :dev}))]
  :teardown (component/stop -pg-)}
(fact "initiates a pointer in the runtime"

  (do (client-impl/init-ptr-pg -pg- scratch/addf))
  => anything)

^{:refer hara.runtime.postgres.base.client-impl/prepend-select-check-form :added "4.0"}
(fact "checks if form needs a `SELECT` prepended"

  (client-impl/prepend-select-check builtin/acos
                             [])
  => true

  (client-impl/prepend-select-check addon/b:insert
                             [])
  => nil

  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             [1])
  => true

  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             ["hello"])
  => true

  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             ['(:int 0.5)])
  => true

  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             ['(++ 0.5 :int)])
  => true

  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             ['(if a 1 2)])
  => nil

  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             ['(do:block 1 2 3)])
  => nil

  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             ['(do:assert 1 2 3)])
  => nil

  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             ['(do:run 1 2 3)])
  => nil

  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             [[:anything]])
  => nil)

^{:refer hara.runtime.postgres.base.client-impl/prepend-select-check :added "4.0"}
(fact "checks if values needs a `SELECT` prepended"

  (client-impl/prepend-select-check builtin/cot [40])
  => true)

^{:refer hara.runtime.postgres.base.client-impl/invoke-ptr-pg-single :added "4.0"
  :setup [(def -pg- (client/rt-postgres {:dbname "test-scratch"}))]
  :teardown (component/stop -pg-)}
(fact "invokes single "

  (client-impl/invoke-ptr-pg-single -pg-
                                    (ut/lang-pointer :postgres)
                                    '[(+ 1 2)])
  => 3)

^{:refer hara.runtime.postgres.base.client-impl/invoke-ptr-pg-transform-let-fn :added "4.0"}
(fact "transforms the let form"

  (client-impl/invoke-ptr-pg-transform-let-fn
   'form)
  => '[:do :$$ \\ :begin \\ (\| (do [:loop \\ (\| (do form)) \\ :end-loop])) \\ :end \; \\ :$$ :language "plpgsql" \;])

^{:refer hara.runtime.postgres.base.client-impl/invoke-ptr-pg-transform-try-fn :added "4.0"}
(fact "transforms the try form"

  (client-impl/invoke-ptr-pg-transform-try-fn
   'form)
  => '[:do :$$ \\ :begin \\ (\| form) \\ :end \; \\ :$$ :language "plpgsql" \;])

^{:refer hara.runtime.postgres.base.client-impl/invoke-ptr-pg-transform-prep :added "4.0"}
(fact "transforms a form"

  (client-impl/invoke-ptr-pg-transform-prep
   '(try
      (return (+ a b c))
      (catch others
          (return 1)))
   false)
  => '[(try (do [:perform (set_config "temp.out" (:text (+ a b c)) false)]) (catch others (do [:perform (set_config "temp.out" (:text 1) false)]))) true])

^{:refer hara.runtime.postgres.base.client-impl/invoke-ptr-pg-transform :added "4.0"}
(fact "transforms a let and try form"

  (client-impl/invoke-ptr-pg-transform
   :try
   '(try
      (return (+ a b c))
      (catch others
          (return 1))))
  => '[[[:select (set-config "temp.out" nil false)] false]
      [[:do
        :$$
        \\
        :begin
        \\
        (\|
         (try
          (do [:perform (set_config "temp.out" (:text (+ a b c)) false)])
          (catch
           others
           (do [:perform (set_config "temp.out" (:text 1) false)]))))
        \\
        :end
        \;
        \\
        :$$
        :language
        "plpgsql"
        \;]
       false]
      [[:select (current-setting "temp.out" false)] true]]

  (client-impl/invoke-ptr-pg-transform
   :let
   '(let [(:integer a) 1
          (:integer b) 2]
      (return (+ a b))))
  => '[[[:select (set-config "temp.out" nil false)] false]
      [[:do
        :$$
        \\
        :begin
        \\
        (\|
         (do
           [:loop
            \\
            (\|
             (do
               (let
                   [(:integer a) 1 (:integer b) 2]
                 (do
                   [:perform (set_config "temp.out" (:text (+ a b)) false)]
                   [:exit]))))
            \\
            :end-loop]))
        \\
        :end
        \;
        \\
        :$$
        :language
        "plpgsql"
        \;]
       false]
      [[:select (current-setting "temp.out" false)] true]])

^{:refer hara.runtime.postgres.base.client-impl/invoke-ptr-pg-block :added "4.0"
  :setup [(def -pg- (client/rt-postgres {:dbname "test-scratch"}))]
  :teardown (component/stop -pg-)}
(fact "invokes a block"

  (client-impl/invoke-ptr-pg-block -pg-
                                   (ut/lang-pointer :postgres)
                                   '[(let [(:integer a) 1
                                           (:integer b) 2]
                                       (return (+ a b)))])
  => 3

  (client-impl/invoke-ptr-pg-block -pg-
                                   (ut/lang-pointer :postgres
                                                    {:form '(+ 1 2 3)})
                                   [])
  => 6)

^{:refer hara.runtime.postgres.base.client-impl/invoke-ptr-pg :added "4.0"
  :setup [(def -pg- (client/rt-postgres {:dbname "test-scratch"}))]
  :teardown (component/stop -pg-)}
(fact "invokes a pointer in runtime"

  (client-impl/invoke-ptr-pg -pg- builtin/cot [40])
  => -0.8950829176379128

  (client-impl/invoke-ptr-pg -pg-
                             (ut/lang-pointer :postgres)
                             '[(let [(:integer a) 1
                                      (:integer b) 2]
                                  (return (+ a b)))])
  => 3

  (client-impl/invoke-ptr-pg -pg-
                             (ut/lang-pointer :postgres
                                              {:form '(+ 1 2 3)})
                             [])
  => 6)


^{:refer hara.runtime.postgres.base.client-impl/invoke-ptr-pg-form :added "4.1"}
(fact "wraps one form in a free pointer form and invokes it without arguments"
  (with-redefs [client-impl/invoke-ptr-pg-single vector]
    (client-impl/invoke-ptr-pg-form :pg
                                    {:id :query}
                                    '(+ 1 2)))
  => '[:pg {:id :query :form (+ 1 2)} []])
