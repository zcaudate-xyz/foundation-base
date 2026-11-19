(ns dupasupa.config)

(def DATABASE
  {:application "dupasupa"
   :scaffold 'dupasupa.db
   :rpc-namespace "dupasupa.db.rpc"
   :rpc-schema "dupasupa_rpc"
   :seed-namespace 'dupasupa.seed
   :group-namespaces ['dupasupa.groups]})
