(ns js.cell-v2
  (:require [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.cell-v2.control :as control]
             [js.cell-v2.db :as db]
             [js.cell-v2.event :as event]
             [js.cell-v2.protocol :as protocol]
             [js.cell-v2.route :as route]
             [js.cell-v2.store :as store]
             [js.cell-v2.remote :as remote]]})

(defn.js make-system
  "creates a new js.cell-v2 system"
  {:added "4.0"}
  [opts]
  (:= opts (or opts {}))
  (var events (or (. opts ["events"])
                  (event/make-bus)))
  (var system {"::" "cell-v2"
               :events events
               :routes (or (. opts ["routes"])
                           (route/make-registry))
               :dbs (or (. opts ["dbs"])
                        (db/make-registry))
               :stores (or (. opts ["stores"])
                           (store/make-registry))
               :remotes (or (. opts ["remotes"])
                            (remote/make-registry))
               :defaultDb (. opts ["defaultDb"])
               :defaultStore (. opts ["defaultStore"])
               :state (or (. opts ["state"])
                          {})})
  (k/set-key events "owner" system)
  (return system))

(defn.js add-event-listener
  "adds an event listener to the system"
  {:added "4.0"}
  [system listener-id pred f]
  (return (event/add-listener (. system ["events"])
                              listener-id
                              pred
                              f)))

(defn.js remove-event-listener
  "removes an event listener from the system"
  {:added "4.0"}
  [system listener-id]
  (return (event/remove-listener (. system ["events"])
                                 listener-id)))

(defn.js list-event-listeners
  "lists event listeners on the system"
  {:added "4.0"}
  [system]
  (return (event/list-listeners (. system ["events"]))))

(defn.js emit
  "emits an event on the system bus"
  {:added "4.0"}
  [system input]
  (return (event/emit (. system ["events"])
                      input)))

(defn.js add-signal-listener
  "adds a signal listener to the system"
  {:added "4.0"}
  [system listener-id pred f]
  (return (-/add-event-listener system listener-id pred f)))

(defn.js emit-signal
  "emits a signal on the system bus"
  {:added "4.0"}
  [system signal body meta status]
  (return (-/emit system
                  (event/signal-event signal
                                      (or status "ok")
                                      body
                                      meta))))

(defn.js register-route
  "registers a route on the system"
  {:added "4.0"}
  [system route-id handler opts]
  (return (route/register-route (. system ["routes"])
                                route-id
                                handler
                                opts)))

(defn.js dispatch-route
  "dispatches a route via the system"
  {:added "4.0"}
  [system route-id args ctx]
  (return (route/dispatch-route (. system ["routes"])
                                route-id
                                args
                                ctx)))

(defn.js get-route
  "gets a registered route entry from the system"
  {:added "4.0"}
  [system route-id]
  (return (route/get-route (. system ["routes"])
                           route-id)))

(defn.js list-routes
  "lists route ids registered on the system"
  {:added "4.0"}
  [system]
  (return (route/list-routes (. system ["routes"]))))

(defn.js install-control-routes
  "registers the standard control routes on the system"
  {:added "4.0"}
  [system]
  (return (control/register-control-routes system)))

(defn.js register-store
  "registers a store adaptor on the system"
  {:added "4.0"}
  [system key adaptor]
  (return (store/register-store (. system ["stores"])
                                key
                                adaptor)))

(defn.js register-db
  "registers an xt.db-backed database on the system"
  {:added "4.0"}
  [system key entry]
  (return (db/register-db (. system ["dbs"])
                          key
                          entry)))

(defn.js create-db
  "creates and registers an xt.db-backed database on the system"
  {:added "4.0"}
  [system key m schema lookup opts]
  (return (db/create-db (. system ["dbs"])
                        key
                        m
                        schema
                        lookup
                        opts)))

(defn.js list-dbs
  "lists database keys on the system"
  {:added "4.0"}
  [system]
  (return (db/list-dbs (. system ["dbs"]))))

(defn.js store-read
  "calls store read on the system"
  {:added "4.0"}
  [system key input]
  (return (store/store-read (. system ["stores"])
                            key
                            input)))

(defn.js store-write
  "calls store write on the system"
  {:added "4.0"}
  [system key input]
  (return (store/store-write (. system ["stores"])
                             key
                             input)))

(defn.js store-sync
  "calls store sync on the system"
  {:added "4.0"}
  [system key input]
  (return (store/store-sync (. system ["stores"])
                            key
                            input)))

(defn.js store-clear
  "calls store clear on the system"
  {:added "4.0"}
  [system key input]
  (return (store/store-clear (. system ["stores"])
                             key
                             input)))

(defn.js store-remove
  "calls store remove on the system"
  {:added "4.0"}
  [system key input]
  (return (store/store-remove (. system ["stores"])
                              key
                              input)))

(defn.js store-query
  "calls store query on the system"
  {:added "4.0"}
  [system key input]
  (return (store/store-query (. system ["stores"])
                             key
                             input)))

(defn.js db-sync
  "syncs rows into the system db"
  {:added "4.0"}
  [system key input]
  (return (db/db-sync (. system ["dbs"])
                      key
                      input)))

(defn.js db-remove
  "removes rows from the system db"
  {:added "4.0"}
  [system key input]
  (return (db/db-remove (. system ["dbs"])
                        key
                        input)))

(defn.js db-query
  "queries the system db"
  {:added "4.0"}
  [system key tree]
  (return (db/db-query (. system ["dbs"])
                       key
                       tree)))

(defn.js db-delete
  "deletes rows from the system db"
  {:added "4.0"}
  [system key table-name ids]
  (return (db/db-delete (. system ["dbs"])
                        key
                        table-name
                        ids)))

(defn.js db-clear
  "clears the system db"
  {:added "4.0"}
  [system key]
  (return (db/db-clear (. system ["dbs"])
                       key)))

(defn.js register-remote
  "registers a remote adaptor on the system"
  {:added "4.0"}
  [system key adaptor]
  (return (remote/register-remote (. system ["remotes"])
                                  key
                                  adaptor)))

(defn.js remote-call
  "calls a remote adaptor on the system"
  {:added "4.0"}
  [system key input]
  (var out (remote/remote-call (. system ["remotes"])
                               key
                               input))
  (var envelope (remote/normalize-result out))
  (var status (k/get-key envelope "status"))
  (var body   (k/get-key envelope "body"))
  (var meta   (k/obj-assign {:remote key}
                            (or (k/get-key envelope "meta")
                                {})))
  (-/emit-signal system event/EV_REMOTE body meta status)
  (k/for:array [entry (or (k/get-key envelope "events") [])]
    (var signal (or (k/get-key entry "signal")
                    (k/get-key entry "topic")))
    (when signal
      (-/emit-signal system
                     signal
                     (k/get-key entry "body")
                     (k/get-key entry "meta")
                     (or (k/get-key entry "status")
                          status))))
  (var store-effects (or (k/get-key envelope "store")
                         {}))
  (var store-keys (store/list-stores (. system ["stores"])))
  (var store-key (or (k/get-key store-effects "key")
                     (. system ["defaultStore"])))
  (when (and (not store-key)
             (== 1 (k/len store-keys)))
    (:= store-key (k/first store-keys)))
  (when store-key
    (var store-sync-input (k/get-key store-effects "sync"))
    (when store-sync-input
      (var store-sync-out (-/store-sync system store-key store-sync-input))
      (-/emit-signal system
                     event/EV_DB_SYNC
                     (or store-sync-out store-sync-input)
                     {:remote key
                      :store store-key}
                     "ok"))
    (var store-remove-input (k/get-key store-effects "remove"))
    (when store-remove-input
      (var store-remove-out (-/store-remove system store-key store-remove-input))
      (-/emit-signal system
                     event/EV_DB_REMOVE
                     (or store-remove-out store-remove-input)
                     {:remote key
                      :store store-key}
                     "ok")))
  (var db-effects (or (k/get-key envelope "db")
                      {}))
  (var db-keys (db/list-dbs (. system ["dbs"])))
  (var db-key (or (k/get-key db-effects "key")
                  (. system ["defaultDb"])))
  (when (and (not db-key)
             (== 1 (k/len db-keys)))
    (:= db-key (k/first db-keys)))
  (when db-key
    (var db-sync-input (k/get-key db-effects "sync"))
    (when db-sync-input
      (var db-sync-out (-/db-sync system db-key db-sync-input))
      (-/emit-signal system
                     event/EV_DB_SYNC
                     (or (k/second db-sync-out)
                         db-sync-input)
                     {:remote key
                      :db db-key}
                     "ok"))
    (var db-remove-input (k/get-key db-effects "remove"))
    (when db-remove-input
      (var db-remove-out (-/db-remove system db-key db-remove-input))
      (-/emit-signal system
                     event/EV_DB_REMOVE
                     (or (k/second db-remove-out)
                         db-remove-input)
                     {:remote key
                      :db db-key}
                     "ok")))
  (return envelope))

(defn.js call-action
  "calls a protocol action on the system"
  {:added "4.0"}
  [system action body ctx]
  (cond (. action (startsWith "remote/"))
        (return (-/remote-call system
                               (. action (substring 7))
                               (or (. body ["input"])
                                   body)))

        (. action (startsWith "store/"))
        (do (var op (. action (substring 6)))
            (var key (or (. body ["store"])
                         (. system ["defaultStore"])))
            (cond (== op "read")
                  (return (-/store-read system key (. body ["input"])))

                  (== op "write")
                  (return (-/store-write system key (. body ["input"])))

                  (== op "sync")
                  (return (-/store-sync system key (. body ["input"])))

                  (== op "clear")
                  (return (-/store-clear system key (. body ["input"])))

                  (== op "remove")
                  (return (-/store-remove system key (. body ["input"])))

                  (== op "query")
                  (return (-/store-query system key (. body ["input"])))

                  :else
                  (k/err (k/cat "ERR - Store action not found - " op))))

        (. action (startsWith "db/"))
        (do (var op (. action (substring 3)))
            (var key (or (. body ["db"])
                         (. body ["store"])
                         (. body ["key"])
                         (. system ["defaultDb"])))
            (cond (== op "sync")
                  (return (-/db-sync system key (. body ["input"])))

                  (== op "remove")
                  (return (-/db-remove system key (. body ["input"])))

                  (== op "query")
                  (return (-/db-query system key (. body ["input"])))

                  (== op "delete")
                  (return (-/db-delete system
                                       key
                                       (. body ["table"])
                                       (. body ["ids"])))

                  (== op "clear")
                  (return (-/db-clear system key))

                  :else
                  (k/err (k/cat "ERR - DB action not found - " op))))

        :else
        (return (-/dispatch-route system
                                  action
                                  (or (. body ["input"])
                                      body)
                                  ctx))))

(defn.js protocol-call
  "constructs a call frame"
  {:added "4.0"}
  [id action body meta]
  (return (protocol/call id action body meta)))

(defn.js protocol-emit
  "constructs an emit frame"
  {:added "4.0"}
  [id signal status body meta ref]
  (return (protocol/emit id signal status body meta ref)))
