(ns hara.runtime.basic.impl.process-gdscript
  (:require [clojure.string]
            [xt.lang.common-promise]
            [hara.runtime.basic.type-basic :as basic]
            [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-oneshot :as oneshot]
            [hara.lang.impl :as impl]
            [hara.lang.runtime :as rt]
            [hara.model.spec-gdscript :as spec]
            [std.lib.os :as os]
            [xt.lang.common-lib :as lib]))

;;
;; PROGRAM
;;

(def +program-init+
  (common/put-program-options
   :gdscript {:default {:oneshot   :godot
                        :basic     :godot
                        :interactive :godot}
              :env {:godot {:exec  "godot"
                            :flags {:oneshot   ["--headless" "--script"]
                                    :basic     ["--headless" "--script"]
                                    :interactive ["--script"]}}}}))

;;
;; ONESHOT
;;

(defn default-body-wrap
  "wraps body forms in a local helper so the last value is returned"
  {:added "4.1"}
  [forms]
  (let [forms (rt/return-format forms '#{:- := var var* local def defn break throw})]
    (list 'do
          (apply list 'defn (with-meta 'OUT-FN
                              {:inner true})
                 []
                 forms)
          (list 'return (list 'OUT-FN)))))

(defn default-body-transform
  "transform code for return"
  {:added "4.1"}
  [input mopts]
  (-> (rt/normalize-body-forms input mopts)
      (default-body-wrap)))

(def ^{:arglists '([body])}
  default-oneshot-wrap
  (fn [body]
    (str "extends SceneTree\n\n"
         "func _init():\n"
         "  var __result__ = " body "\n"
         "  print(JSON.stringify(__result__))\n"
         "  quit()\n")))

(def +gdscript-oneshot-config+
  (common/set-context-options
   [:gdscript :oneshot :default]
   {:main  {:in    #'default-oneshot-wrap}
    :emit  {:body  {:transform #'default-body-transform}}
    :json :full}))

(def +gdscript-oneshot+
  [(rt/install-type!
    :gdscript :oneshot
    {:type :hara/rt.oneshot
     :instance {:create #'oneshot/rt-oneshot:create}})])

;;
;; BASIC
;;

(def +gdscript-basic+
  [(rt/install-type!
    :gdscript :basic
    {:type :hara/rt.basic
     :instance {:create #'basic/rt-basic:create}
     :config {:layout :full}})])
