(ns rt.chromedriver.spec
  (:require [std.json :as json]
            [std.lib :as h]
            [std.string :as str]
            [std.fs :as fs])
  (:refer-clojure :exclude [get-method]))

(def +path+ "assets/rt.chromedriver")

(defn spec-download
  "downloads the chrome devtools spec"
  {:added "4.0"}
  []
  (fs/create-directory (str "resources/" +path+))
  (h/sys:wget-bulk "https://raw.githubusercontent.com/ChromeDevTools/devtools-protocol/master/json/"
                   +path+
                   ["js_protocol.json"
                    "browser_protocol.json"]))

(def +spec-js+
  (delay (json/read
          (h/sys:resource-content
           "assets/rt.chromedriver/js_protocol.json"))))

(def +spec-browser+
  (delay (json/read
          (h/sys:resource-content
           "assets/rt.chromedriver/browser_protocol.json"))))

(def +spec-all+
  (delay (h/map-juxt [#(get % "domain")
                      #(get % "commands")]
                     (concat (get @+spec-js+ "domains")
                             (get @+spec-browser+ "domains")))))

(defn list-domains
  "lists all domains"
  {:added "4.0"}
  []
  (sort (keys @+spec-all+)))

(defn get-domain-raw
  "gets the raw domain"
  {:added "4.0"}
  [domain]
  (h/map-juxt [#(get % "name")
               identity]
              (get @+spec-all+ domain)))

(def ^{:arglists '([domain])}
  get-domain
  (memoize get-domain-raw))

(defn list-methods
  "lists all spec methods"
  {:added "4.0"}
  [domain]
  (sort (keys (get-domain domain))))

(defn get-method
  "gets the method"
  {:added "4.0"}
  [domain method]
  (get (get-domain domain) method))

;;
;; TEMPLATES
;;

(defn tmpl-connection
  "creates a connection form given template"
  {:added "4.0"}
  [[sym [domain method]]]
  (let [{:strs [parameters]} (get-method domain method)
        permanent (remove #(get % "optional") parameters)
        optional  (filter #(get % "optional") parameters)
        name-fn   (fn [{:strs [name]}] name)
        sym-fn    (fn [{:strs [name]}] (symbol (str/spear-case name)))
        p-args    (mapv sym-fn permanent)
        p-map     (h/map-juxt [name-fn
                               sym-fn]
                              permanent)
        o-args    (vec (sort (map sym-fn optional)))]
    (list 'defn sym (vec (concat '[conn] p-args ['& [{:keys o-args :as 'm} 'timeout 'opts]]))
          (list '
           rt.chromedriver.connection/send
           'conn
           (str domain "." method)
           (list 'merge p-map 'm)
           'timeout
           'opts))))

(defn tmpl-browser
  "constructs the browser template"
  {:added "4.0"}
  [[sym src]]
  (let [src-var (resolve src)
        {:keys [arglists]} (meta src-var)
        sym-arglists (list (vec (cons 'browser (rest (first arglists)))))]
    (list 'def (with-meta sym {:arglists (list 'quote sym-arglists)})
          (list 'rt.chromedriver.impl/wrap-browser-state src))))
