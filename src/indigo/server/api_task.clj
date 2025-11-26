(ns indigo.server.api-task
  (:require [code.heal :as heal]
            [std.json :as json]
            [std.block :as block]
            [std.lib :as h]
            [std.html :as html]
            [std.lib.walk :as walk]
            [std.string :as str]
            [indigo.server.api-prompt :as prompt]))

(defn from-html
  [body]
  (let [full (html/tree
              (str "<div>" body "</div>"))
        full (if (= 2 (count full))
               (second full)
               full)
        full (walk/postwalk
              (fn [x]
                (if (map? x)
                  (let [v (or (:class x)
                              (:classname x))
                        v (if (string? v)
                            [v]
                            (vec (keep (fn [v]
                                         (not-empty (str/trim v)))
                                       v)))]
                    (cond-> x
                      :then (dissoc :classname :class)
                      (seq v) (assoc :class v)))
                  x))
              full)]
    (block/string (block/layout full))))

(defn to-html
  [body]
  (std.html/html
   (try 
     (read-string body)
     (catch Throwable t
       ""))))

(defn to-heal
  [body]
  (heal/heal body))

(defn to-plpgsql-dsl
  [body]
  (prompt/with-prompt-fn prompt/to-plpgsql-prompt body))

(defn to-jsxc-dsl
  [body]
  (prompt/with-prompt-fn prompt/to-jsxc-prompt body))

(defn to-js-dsl
  [body]
  (prompt/with-prompt-fn prompt/to-js-prompt body))

(defn to-python-dsl
  [body]
  (prompt/with-prompt-fn prompt/to-python-prompt body))
