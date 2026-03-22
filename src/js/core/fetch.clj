^{:no-test true}
(ns js.core.fetch
  (:require [std.lang :as l]
            [std.lib.foundation :as f]
            [std.lib.template :as template]))

(l/script :js js.core)

;;
;; fetch
;;

(def +fetch-opts+
  {:method #{"GET" "POST" "PUT" "DELETE"}
   :mode   #{"cors" "no-cors" "same-origin"}
   :cache  #{"default", "no-cache",
             "reload", "force-cache",
             "only-if-cached"}
   :credentials #{"include", "same-origin", "omit"}
   :headers coll?
   :body f/T
   :redirect #{"manual" "follow" "error"}
   :referrerPolicy #{"no-referrer",
                     "no-referrer-when-downgrade"
                     "origin"
                     "origin-when-cross-origin"
                     "same-origin"
                     "strict-origin"
                     "strict-origin-when-cross-origin"
                     "unsafe-url"}
   :as #{"blob" "formData" "json" "arrayBuffer" "text"}})

(defn check-opts
  "checks for for errors"
  {:added "4.0"}
  [opts]
  (doseq [[k v] opts]
    (let [chk (+fetch-opts+ k)]
      (if-not chk
        (f/error (str "Key not found: " k)))
      (if-not (chk v)
        (f/error (str "Val is invalid: " v) {:options chk})))))

(defn fetch-fn
  "creates the fetch form with checks"
  {:added "4.0"}
  ([url opts]
   (let [_ (check-opts opts)
         {:keys [as]} opts
         form  (template/$ (fetch ~url ~(dissoc opts :as)))
         form  (if as
                 (template/$ (. ~form (then (fn [res]
                                       (return (. res (~(symbol as))))))))
                 form)]
     form)))

(defmacro.js fetch
  "fetches a http request"
  {:added "4.0"}
  [url & [{:keys [method
                  mode
                  cache
                  credentials
                  headers
                  redirect
                  referrerPolicy
                  body]
           :as opts}]]
  (fetch-fn url (or opts {})))

(defmacro.js toText
  "returns the next promise as text"
  {:added "4.0"}
  [p]
  (list '. p '(then (fn:> [res] (res.text)))))

(defmacro.js toJson
  "returns the next promise as json"
  {:added "4.0"}
  [p]
  (list '. p '(then (fn:> [res] (res.json)))))

(defmacro.js toBlob
  "returns the next promise as blob"
  {:added "4.0"}
  [p]
  (list '. p '(then (fn:> [res] (res.blob)))))

(defmacro.js toPrint
  "prints out the promise"
  {:added "4.0"}
  [p]
  (list '. p '(then (fn [res]
                      (console.warn res)
                      (return res)))))

(defmacro.js toAssign
  "assigns the result to an object"
  {:added "4.0"}
  [p state]
  (template/$ (. ~p (then (fn [res]
                     (return (j/assign ~state res)))))))

(defmacro.js fetch-api
  "api calls for fetch"
  {:added "4.0"}
  ([url & [opts]]
   (let [fetch-form (fetch-fn url (dissoc (or opts {}) :as))]
     (template/$ (do:> (return (new Promise
                            (fn [resolve reject]
                              (-> ~fetch-form
                                  (. (then (fn [res]
                                             (if (== 200 res.status)
                                               (return (res.json))
                                               (reject res)))))
                                  (. (then (fn [json]
                                             (if (== "ok" json.status)
                                               (return (resolve json.data))
                                               (reject json.data)))))
                                  (. (catch (fn [err]
                                              (reject {:tag "net/connection_error"
                                                       :status "error"
                                                       :data (Object.assign {} err)})))))))))))))

