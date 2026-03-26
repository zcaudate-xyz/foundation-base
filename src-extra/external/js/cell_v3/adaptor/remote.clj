(ns js.cell-v3.adaptor.remote
  (:require [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]]})

(defn.js make-registry
  "creates a remote registry"
  {:added "4.0"}
  []
  (return {"::" "cell-v3.remote-registry"
           :entries {}}))

(defn.js register-remote
  "registers a remote adaptor"
  {:added "4.0"}
  [registry key adaptor]
  (var prev (. registry ["entries"] [key]))
  (:= (. registry ["entries"] [key])
      (j/assign {:key key}
                (or adaptor {})))
  (return prev))

(defn.js get-remote
  "gets a remote adaptor"
  {:added "4.0"}
  [registry key]
  (return (. registry ["entries"] [key])))

(defn.js list-remotes
  "lists remote keys"
  {:added "4.0"}
  [registry]
  (return (k/obj-keys (. registry ["entries"]))))

(defn.js remote-call
  "calls the remote adaptor"
  {:added "4.0"}
  [registry key input]
  (var entry (-/get-remote registry key))
  (when (k/nil? entry)
    (k/err (k/cat "ERR - Remote not found - " key)))
  (var f (k/get-key entry "call"))
  (when (k/nil? f)
    (k/err (k/cat "ERR - Remote operation not found - " key " call")))
  (return (f input entry registry)))

(defn.js normalize-result
  "normalizes a remote call result into an envelope"
  {:added "4.0"}
  [input]
  (if (and (k/obj? input)
           (k/not-nil? (k/get-key input "status")))
    (return (j/assign {:events []
                       :meta {}
                       :store {}}
                      input))
    (return {:status "ok"
             :body input
             :events []
             :meta {}
             :store {}})))
