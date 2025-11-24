(ns rt.postgres.client-test
  (:use code.test)
  (:require [rt.postgres.client :as client]
            [std.lib :as h]
            [std.lang :as l]
            [lib.postgres.connection :as conn]
            [lib.postgres :as base]
            [rt.postgres.script.scratch :as scratch]))

^{:refer rt.postgres.client/setup-module :adopt true :added "4.0"}
(fact "creates a postgres runtime"
  ;; Only checking function existance/stub as we mock everything
  )

^{:refer rt.postgres.client/rt-postgres:create :added "4.0"}
(fact "creates a postgres runtime"
  (client/rt-postgres:create {})
  => map?)

^{:refer rt.postgres.client/rt-postgres :added "4.0"}
(fact "creates and startn a postgres runtime"
  (with-redefs [base/start-pg (fn [pg] (assoc pg :started true))]
    (client/rt-postgres {}))
  => (contains {:started true}))

^{:refer rt.postgres.client/rt-add-notify :added "4.0"}
(fact "adds a notification channel"
  (let [pg (client/rt-postgres:create {})]
    (with-redefs [conn/notify-create (fn [_ _] [:conn])]
      (client/rt-add-notify pg "id" {:channel "ch"})
      @(:notifications pg) => {"id" [:conn]})))

^{:refer rt.postgres.client/rt-remove-notify :added "4.0"}
(fact "removes a notification channel"
  (let [pg (client/rt-postgres:create {})]
    (with-redefs [conn/notify-create (fn [_ _] [:conn])
                  conn/conn-close (fn [_] nil)]
      (client/rt-add-notify pg "id" {:channel "ch"})
      (client/rt-remove-notify pg "id")
      @(:notifications pg) => {})))

^{:refer rt.postgres.client/rt-list-notify :added "4.0"}
(fact "lists all notification channels"
  (let [pg (client/rt-postgres:create {})]
    (with-redefs [conn/notify-create (fn [_ _] [:conn])]
      (client/rt-add-notify pg "id" {:channel "ch"})
      (client/rt-list-notify pg) => (contains ["id"]))))
