(ns js.cell.binding.trigger
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[js.cell.service.db-stream :as db-stream]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]]
   :export  [MODULE]})

(defn.xt normalize-deps
  "normalizes dependency paths"
  {:added "4.0"}
  [prepared]
  (var model-id (xt/x:get-key prepared "model_id"))
  (var deps (or (xt/x:get-key prepared "deps") []))
  (return
   (xt/x:arr-map deps
                 (fn [path]
                   (return (:? (xt/x:is-array? path)
                               path
                               [model-id path]))))))

(defn.xt compile-trigger
  "compiles trigger metadata"
  {:added "4.0"}
  [prepared]
  (return (xt/x:get-key prepared "trigger")))

(defn.xt compile-stream-options
  "compiles stream metadata into option context"
  {:added "4.0"}
  [prepared]
  (var stream (xt/x:get-key prepared "stream"))
  (when (xt/x:nil? stream)
    (return nil))
  (var stream-spec (:? (and (xt/x:nil? (xt/x:get-key stream "target"))
                            (xt/x:is-object? (xt/x:get-key stream "db")))
                        (xt/x:obj-assign (xtd/obj-clone stream)
                                         {"target" (xtd/get-in stream ["db" "target"])})
                        stream))
  (var view-context {"model-id" (xt/x:get-key prepared "model_id")
                     "view-id" (xt/x:get-key prepared "view_id")})
  (return {"context"
           {"stream"
            {"key" (db-stream/subscription-key
                    (xt/x:get-key stream-spec "db")
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
