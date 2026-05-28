(ns lib.supabase.rpc
  (:require [lib.supabase.common :as common]
            [lib.supabase.route :as route]
            [std.string.case :as case]))

(defn fn-meta [f]
  (let [f (if (instance? clojure.lang.IDeref f)
            @f
            f)]
    {:id (cond (map? f) (or (:id f) (get f "id"))
               :else f)
     :schema (or (:static/schema f)
                 (get f "static/schema")
                 (:schema f)
                 (get f "schema"))}))

(defn api-rpc
  "Calls a PostgREST RPC function."
  {:added "4.1.4"}
  [{:keys [fn args] :as opts}]
  (let [{:keys [id schema]} (fn-meta fn)
        headers (when schema
                  {"Content-Profile" schema})
        route-path (route/rest-route :rpc (case/snake-case (name id)))]
    (common/api-call (merge (route/route-request :rest/rpc
                                                  opts
                                                  (case/snake-case (name id)))
                            {:headers headers
                             :route route-path})
                     args)))

(defn rpc
  "Calls a PostgREST RPC function."
  {:added "4.1.4"}
  ([client fn-name]
   (rpc client fn-name {} nil))
  ([client fn-name params]
   (rpc client fn-name params nil))
  ([client fn-name params opts]
   (api-rpc (merge opts
                   {:client client
                    :fn fn-name
                    :args params}))))
