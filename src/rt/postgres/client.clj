(ns rt.postgres.client
  (:require [std.protocol.component :as protocol.component]
            [std.protocol.context :as protocol.context]
            [lib.postgres.connection :as conn]
            [lib.postgres :as base]
            [rt.postgres.client-impl :as client-impl]
            [std.lang.base.runtime :as default]
            [std.lang.interface.type-shared :as shared]
            [std.lang.base.impl :as impl]
            [std.lib :as h :refer [defimpl]]))

(def +pg-defaults+
  {:host   (or (System/getenv "DEFAULT_RT_POSTGRES_HOST")
              "127.0.0.1")
   :port   (h/parse-long
           (or (System/getenv "DEFAULT_RT_POSTGRES_PORT")
               "5432"))
   :user   (or (System/getenv "DEFAULT_RT_POSTGRES_USER")
              "postgres")
   :pass   (or (System/getenv "DEFAULT_RT_POSTGRES_PASS")
              "postgres")
   :dbname (or (System/getenv "DEFAULT_RT_POSTGRES_DBNAME")
               "postgres")
   :vendor :postgresql})

(defn- rt-pg-string [pg]
  (str "rt.postgres" (into {} (-> pg
                                  (update :notifications (comp keys deref))
                                  (update :instance (comp h/hash-id deref))
                                  (->> (h/filter-vals identity))))))

(defimpl RuntimePostgres [instance notifications]
  :string rt-pg-string
  :protocols [std.protocol.component/IComponent
              :method {-kill  base/stop-pg
                       -start base/start-pg
                       -stop  base/stop-pg}
              protocol.context/IContext
              :prefix "default/default-"
              :method {-raw-eval    client-impl/raw-eval-pg
                       -invoke-ptr  client-impl/invoke-ptr-pg
                       -init-ptr    client-impl/init-ptr-pg}
              
              protocol.context/IContextLifeCycle
              :prefix "default/default-"])

(defn rt-postgres:create
  "creates a postgres runtime"
  {:added "4.0"}
  ([m]
   (map->RuntimePostgres (assoc (merge +pg-defaults+
                                       m)
                                :lang :postgres
                                :tag  :postgres
                                :instance       (atom nil)
                                :notifications  (atom {})
                                :lifecycle {:rt/setup    {:export {:suppress true}
                                                          :code   {:label true}}
                                            :rt/teardown {:code   {:suppress true}}}))))

(defn rt-postgres
  "creates and startn a postgres runtime"
  {:added "4.0"}
  ([]
   (rt-postgres {}))
  ([m]
   (-> (rt-postgres:create m)
       (h/start))))

(defn rt-add-notify
  "adds a notification channel"
  {:added "4.0"}
  [{:keys [notifications] :as pg}
   id
   {:keys [channel on-close on-notify]
    :as m}]
  (or (get @notifications id)
      (get (swap! notifications assoc id (conn/notify-create pg m))
           id)))

(defn rt-remove-notify
  "removes a notification channel"
  {:added "4.0"}
  [{:keys [notifications]} id]
  (let [[conn] (get @notifications id)
        _ (conn/conn-close conn)
        _ (swap! notifications dissoc id)]
    conn))

(defn rt-list-notify
  "lists all notification channels"
  {:added "4.0"}
  ([{:keys [notifications]}]
   (keys @notifications)))

(def +init+
  [(default/install-type!
    :postgres :jdbc.client
    {:type :hara/rt.postgres.client
     :config {:layout :module}
     :instance {:create rt-postgres:create}})
   
   (default/install-type!
    :postgres :jdbc
    {:type :hara/rt.postgres
     :config {:layout :module
              :lifecycle {:rt/setup    {:export {:suppress true}
                                        :code   {:label true}}
                          :rt/teardown {:code   {:suppress true}}}}
     :instance
     {:create (fn [m]
                (-> {:rt/client {:type :hara/rt.postgres
                                 :constructor rt-postgres:create}}
                    (merge m)
                    (shared/rt-shared:create)))}})])
