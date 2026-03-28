(ns xt.cell.binding.trigger
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.cell.service.db-stream :as db-stream]
             [xt.lang.base-lib :as k]]
   :export  [MODULE]})

(defn.xt normalize-deps
  "normalizes dependency paths"
  {:added "4.0"}
  [prepared]
  (var model-id (k/get-key prepared "model_id"))
  (var deps (or (k/get-key prepared "deps") []))
  (return
   (k/arr-map deps
              (fn [path]
                (return (:? (k/arr? path)
                            path
                            [model-id path]))))))

(defn.xt compile-trigger
  "compiles trigger metadata"
  {:added "4.0"}
  [prepared]
  (return (k/get-key prepared "trigger")))

(defn.xt compile-stream-options
  "compiles stream metadata into option context"
  {:added "4.0"}
  [prepared]
  (var stream (k/get-key prepared "stream"))
  (when (k/nil? stream)
    (return nil))
  (var stream-spec (:? (and (k/nil? (k/get-key stream "target"))
                            (k/obj? (k/get-key stream "db")))
                        (k/obj-assign (k/obj-clone stream)
                                      {"target" (k/get-in stream ["db" "target"])})
                        stream))
  (var view-context {"model-id" (k/get-key prepared "model_id")
                     "view-id" (k/get-key prepared "view_id")})
  (return {"context"
           {"stream"
            {"key" (db-stream/subscription-key
                    (k/get-key stream-spec "db")
                    stream-spec
                    view-context)
             "spec" stream-spec}}}))

(defn.xt compile-view-hooks
  "compiles deps, trigger, and stream option metadata"
  {:added "4.0"}
  [prepared]
  (return {"deps" (-/normalize-deps prepared)
           "trigger" (-/compile-trigger prepared)
           "options" (-/compile-stream-options prepared)}))

(def.xt MODULE (!:module))
