(ns js.core
  (:require [lang.core :as l]
            [std.lib.foundation :as f]
            [std.lib.template :as template]
            [js.core.impl]
            [js.core.fetch :as fetch])
  (:refer-clojure :exclude [abs concat eval filter find future identity keys map max min name pop read reduce replace reverse some sort]))

(l/script :js)

(f/intern-all js.core.impl)
(f/intern-in fetch/fetch
             fetch/fetch-api
             fetch/toBlob
             fetch/toJson
             fetch/toText
             fetch/toPrint)

(defmacro.js ^{:public true}
  assignNew
  "assigns a new object from the supplied maps"
  [& args]
  (apply list 'Object.assign {} args))

(defmacro.js ^{:public true}
  future
  "creates a promise from a body"
  [& body]
  (if (seq body)
    (template/$
     (new Promise (fn [resolve reject]
                   (try
                     (resolve ('((fn [] ~@body))))
                     (catch e (reject e))))))
    '(new Promise (fn [resolve] (return (resolve))))))

(defmacro.js ^{:public true}
  future-delayed
  "creates a delayed promise"
  [[ms] & body]
  (template/$
   (new Promise (fn [resolve reject]
                 (setTimeout
                  (fn []
                    (try
                      (resolve ('((fn [] ~@body))))
                      (catch e (reject e))))
                  ~ms)))))

(defmacro.js ^{:public true}
  timeout
  "creates a promise that resolves after a delay"
  [ms]
  (template/$
   (new Promise (fn [resolve]
                 (setTimeout resolve ~ms)))))

(defmacro.js ^{:public true}
  isWeb
  "checks that the current platform exposes a browser window"
  []
  '(and (not= "undefined" (typeof window))
        (not= nil window.navigator)))

(defmacro.js ^{:public true}
  randomColor
  "creates a random hexadecimal color"
  []
  '(+ "#" (. (Math.floor (* (Math.random) 16777215))
              (toString 16)
              (padStart 6 0)
              (toUpperCase))))

(defmacro.js ^{:public true}
  randomId
  "creates a random base-36 identifier"
  [n]
  (template/$
   (. (Math.random)
      (toString 36)
      (substr 2 (or ~n 4)))))

(defmacro.js ^{:public true}
  arrayify
  "wraps a non-array value in an array"
  [x]
  (list :? (list 'x:is-array? x)
        x
        (list '== nil x)
        []
        :else [x]))

(defmacro.js ^{:public true}
  identity
  "returns its argument"
  [x]
  x)
