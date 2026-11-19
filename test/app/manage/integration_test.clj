(ns app.manage.integration-test
  (:use code.test)
  (:require [app.manage.integration :as integration]
            [app.manage.integration.postgres :as postgres]))

^{:refer app.manage.integration/start! :id resource-order}
(fact "resources share context and unwind once in reverse order"
  (let
    [events (atom [])
     f (integration/fixture
        [{:id :database
          :start (fn [_] (swap! events conj :db-start) :db)
          :stop (fn [_ value] (swap! events conj [:db-stop value]))}
         {:id :client
          :start (fn [context] (swap! events conj [:client-start (:database context)]) :client)
          :stop (fn [_ value] (swap! events conj [:client-stop value]))}])]
    (integration/start! f)
    (integration/context f) => {:database :db :client :client}
    (integration/stop! f)
    (integration/stop! f)
    @events => [:db-start [:client-start :db] [:client-stop :client] [:db-stop :db]]))

^{:refer app.manage.integration/start! :id partial-failure}
(fact "partial starts and cleanup failures still release every prior resource"
  (let
    [events (atom [])
     f (integration/fixture
        [{:id :database :start (fn [_] :db) :stop (fn [_ _] (swap! events conj :db-stop))}
         {:id :client
          :start (fn [_] (throw (ex-info "start failed" {})))
          :stop (fn [_ _] (swap! events conj :client-stop) (throw (ex-info "close failed" {})))}])]
    (try (integration/start! f) (catch Exception e [(.getMessage e) (count (.getSuppressed e))]))
    =>
    ["start failed" 1]
    @events => [:client-stop :db-stop]
    (integration/context f) => {}))

^{:refer app.manage.integration.postgres/database :id local-scope}
(fact "database descriptions do no IO and reject remote or overridable destinations"
  (let
    [db (postgres/database {:label "example"
                            :admin-url "postgresql://postgres:postgres@127.0.0.1:55122/postgres"})]
    (re-matches #"app_test_example_[a-f0-9]{32}" (:name db)) => string?
    @(:created? db) => false
    (get (postgres/config db) :vendor) => :postgresql)
  (postgres/database {:admin-url "postgresql://u:p@remote.example/postgres"}) => (throws)
  (postgres/database {:admin-url "postgresql://u:p@localhost/application"}) => (throws)
  (postgres/database {:admin-url "postgresql://u:p@localhost/postgres?host=remote"}) => (throws))

^{:refer app.manage.integration/check :id declaration}
(fact "integration checks declare explicit DSL namespaces and target prerequisites"
  (:targets (integration/check {:targets [:db :client] :namespaces ['example.integration-test]}))
  =>
  [:db :client]
  (integration/check {:namespaces []}) => (throws))

^{:refer app.manage.integration.postgres/execute! :id bootstrap-ownership}
(fact "bootstrap and destructive cleanup reject unowned database destinations before connecting"
  (let
    [db (postgres/database {:label "example"
                            :admin-url "postgresql://postgres:postgres@127.0.0.1:55122/postgres"})]
    (postgres/execute! db "SELECT 1") => (throws)
    (postgres/stop! (assoc db :name "application")) => (throws)
    (postgres/stop! (assoc-in db [:admin :host] "remote.example")) => (throws)))

^{:refer app.manage.integration/run! :id strict-results}
(fact "application checks reject empty, skipped or unexecuted facts as well as assertion failures"
  (mapv (fn [result]
          (with-redefs [code.test/run (fn [_] result)]
            (:status (integration/run! ['example.integration-test]))))
        [{} {:passed 1 :facts 1 :queued 2} {:passed 1 :facts 1 :queued 1 :skipped 1}
         {:passed 1 :facts 1 :queued 1 :failed 1} {:passed 1 :facts 1 :queued 1}])
  =>
  [:error :error :error :error :ok])
