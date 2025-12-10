(ns indigo.server.api-translate
  (:require [std.block :as block]
            [std.html :as html]
            [std.lib :as h]
            [std.string :as str]
            [indigo.server.api-prompt :as prompt]))

(defn to-layout [source]
  (->> (read-string (str "[" source "]"))
       (block/layout)
       (block/children)
       (map str)
       (str/join "\n\n")))

(defn to-heal [source]
  (block/heal source))

(defn from-html [source]
  (pr-str (html/tree source)))

(defn to-html [dsl-str]
  (let [form (read-string dsl-str)]
    (html/html form)))

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
