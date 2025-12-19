(ns lib.postgres.connection-test
  (:require [lib.postgres.connection :refer :all]
            [std.lib :as h]
            [code.test :refer :all]))

(fact "test default vendor selection"
  (with-redefs [lib.postgres.connection/load-impl
                (fn [vendor]
                  {:ns (case vendor
                         :impossibl 'lib.postgres.impl.impossibl
                         :postgresql 'lib.postgres.impl.postgresql)})
                ns-resolve
                (fn [ns sym]
                  (constantly {:vendor (if (= ns 'lib.postgres.impl.impossibl)
                                         :impossibl
                                         :postgresql)}))
                lib.postgres.connection/get-env
                (fn [k]
                  (if (= k "DEFAULT_RT_POSTGRES_IMPL")
                    "lib.postgres.impl.postgresql"
                    nil))]

    (let [res (conn-create {})]
      res => {:vendor :postgresql}))

  (with-redefs [lib.postgres.connection/load-impl
                (fn [vendor]
                  {:ns (case vendor
                         :impossibl 'lib.postgres.impl.impossibl
                         :postgresql 'lib.postgres.impl.postgresql)})
                ns-resolve
                (fn [ns sym]
                  (constantly {:vendor (if (= ns 'lib.postgres.impl.impossibl)
                                         :impossibl
                                         :postgresql)}))
                lib.postgres.connection/get-env
                (fn [k]
                  (if (= k "DEFAULT_RT_POSTGRES_IMPL")
                    "lib.postgres.impl.impossibl"
                    nil))]

    (let [res (conn-create {})]
      res => {:vendor :impossibl}))

  (with-redefs [lib.postgres.connection/load-impl
                (fn [vendor]
                  {:ns (case vendor
                         :impossibl 'lib.postgres.impl.impossibl
                         :postgresql 'lib.postgres.impl.postgresql)})
                ns-resolve
                (fn [ns sym]
                  (constantly {:vendor (if (= ns 'lib.postgres.impl.impossibl)
                                         :impossibl
                                         :postgresql)}))
                lib.postgres.connection/get-env
                (fn [k] nil)]

    (let [res (conn-create {})]
      res => {:vendor :impossibl})))
