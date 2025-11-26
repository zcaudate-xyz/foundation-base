(ns indigo.templates
  (:require [clojure.string :as str]
            [indigo.event :as event]
            [std.lib :as h]))

(defonce ^:dynamic *templates* (atom {}))

(defmacro deftemplate [name docstring template]
  `(swap! *templates* assoc ~(keyword name)
          {:name ~(keyword name)
           :doc ~docstring
           :template ~template}))

(defn list-templates []
  (vals @*templates*))

(defn apply-template [template-name values]
  (let [template (:template (get @*templates* template-name))]
    (reduce-kv
     (fn [s k v]
       (str/replace s (str "{{" (name k) "}}") v))
     template
     values)))

(defn apply-template-tool
  [event-bus {:keys [template-name values]}]
  (let [rendered (apply-template (keyword template-name) (read-string values))]
    (event/publish event-bus :templates {:type "template-rendered"
                                         :template-name template-name
                                         :rendered rendered})))
