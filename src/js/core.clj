(ns js.core
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]
            [std.lang.base.util :as ut]
            [std.lang.base.impl-deps :as impl-deps]
            [std.lang.base.pointer :as pointer]
            [xt.module :as module]
            [xt.lang.base-repl])
  (:refer-clojure :exclude [abs identity reduce map reverse sort eval find min read replace
                            future concat pop name some keys filter max]))

(l/script :js
  {:macro-only true
   :require-impl [js.core.impl
                  js.core.fetch]
   :bundle {:node     {:util     [["util"   :as nodeUtil]]
                       :fetch    [["node-fetch" :as nodeFetch]]}
            :uuid     [["uuid" :as [* UUID]]]}})

(h/intern-all js.core.impl)
(h/intern-in js.core.fetch/fetch
             js.core.fetch/fetch-api
             js.core.fetch/toBlob
             js.core.fetch/toJson
             js.core.fetch/toText
             js.core.fetch/toPrint)

;;
;; node
;;

(def$.js inspect nodeUtil.inspect)

;;
;; uuid
;;

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "UUID"
                                   :tag "js"}]
  [[uuidNIL NIL]
   [uuidParse parse]
   [uuidStringify stringify]
   [uuidV1 v1]
   [uuidV3 v3]
   [uuidV4 v4]
   [uuidV5 v5]
   [uuid   v4]
   [uuidValidate validate]
   [uuidVersion version]])


(defmacro.js assignNew
  "assigns new object"
  {:added "4.0"}
  [& args]
  (apply list 'Object.assign {} args))

(defmacro.js future
  "creates a future"
  {:added "4.0"}
  ([& body]
   (cond (not-empty body)
         (h/$ (new Promise (fn [resolve reject]
                             (try
                               (resolve
                                ('((fn [] ~@body))))
                               (catch e (reject e))))))

         :else
         '(new Promise (fn [resolve] (return (resolve)))))))

(defmacro.js ^{:style/indent 1}
  future-delayed
  "creates a future delayed call"
  {:added "4.0"}
  ([[ms] & body]
   (h/$ (new Promise (fn [resolve reject]
                       (setTimeout
                        (fn []
                          (try
                            (resolve
                             ('((fn [] ~@body))))
                            (catch e (reject e))))
                        ~ms))))))

(defmacro.js timeout
  [ms]
  (h/$ (new Promise (fn [resolve]
                      (setTimeout resolve ~ms)))))

(defmacro.js ^{:standalone true}
  isWeb
  "checks that platform is web"
  {:added "4.0"}
  []
  '(and (not= "undefined" (typeof window))
        (not= nil window.navigator)))

(defmacro.js ^{:standalone true}
  randomColor
  "creates a random color"
  {:added "4.0"}
  []
  '(+ "#" (. (Math.floor (* (Math.random)
                            16777215))
             (toString 16)
             (padStart 6 0)
             (toUpperCase))))

(defmacro.js ^{:standalone true}
  randomId
  "creates a random id"
  {:added "4.0"}
  [n]
  (h/$ (. (Math.random)
          (toString 36)
          (substr 2 (or ~n 4)))))

(defmacro.js  ^{:standalone true}
  asyncFn
  "creates an async function"
  {:added "4.0"}
  []
  '(fn [handler-fn context #{success error}]
     (return (. (new Promise
                     (fn [resolve reject]
                       (resolve (handler-fn context))))
                (then success)
                (catch error)))))

(defmacro.js import-missing
  "generates all depenent imports missing from current namespace"
  {:added "4.0"}
  []
  (->> (clojure.core/keys (module/current-natives :js))
       (clojure.core/apply dissoc (module/linked-natives :js))
       (clojure.core/map (fn [[k m]]
                           (impl-deps/module-import-form (l/get-book (l/default-library)
                                                                     :js)
                                                         k
                                                         m
                                                         {})))
       (clojure.core/apply list 'do)))

(defmacro.js import-set-global
  "sets all dependent imports to global"
  {:added "4.0"}
  [& [exclude]]
  (let [form-fn (fn [sym]
                  
                  (let [sym-str (.replaceAll (clojure.core/name sym)
                                             "-"
                                             "_")]
                    (list 'when
                          (list 'not 
                                (list '. 'globalThis
                                      (symbol sym-str)))
                          (list 'js.core/defineProperty 'globalThis
                                sym-str
                                {:value sym
                                 :writeable true}))))
        output (->> (module/linked-natives :js)
                    (clojure.core/mapcat
                     (fn [[pkg {:keys [as]}]]
                       (cond (set? as)
                             (mapv form-fn as)
                             
                             :else
                             (let [sym (if (vector? as)
                                         (clojure.core/last as)
                                         as)]
                               (if (and sym
                                        (not ((or exclude #{})
                                              sym)))
                                 [(form-fn sym)])))))
                    (clojure.core/keep clojure.core/identity)
                    (clojure.core/apply list 'do))]
    output))

(defmacro.js  ^{:standalone true}
  arrayify
  "makes an array
 
   (j/arrayify 1)
   => [1]"
  {:added "4.0"}
  [x]
  (list :? (list 'x:is-array? x)
        x

        (list '== nil x)
        []
        
        :else [x]))

(defmacro.js ^{:standalone true}
  identity
  "identity function"
  {:added "4.0"}
  ([x] x))

(defmacro.js async
  "performs an async call"
  {:added "4.0"}
  [& body]
  (clojure.core/concat
   ['. (clojure.core/first body)]
   (clojure.core/map
    (fn [statement]
      (cond (vector? statement)
            (cond (= :init  (clojure.core/first statement))
                  '[(new Promise (fn [resolve] (resolve)))]

                  (= :catch (clojure.core/first statement))
                  (clojure.core/apply clojure.core/list 'catch (clojure.core/rest statement))

                  (= :then  (clojure.core/first statement))
                  (clojure.core/apply clojure.core/list 'then (clojure.core/rest statement))
                  
                  (and (vector? (clojure.core/first statement))
                       (< 1 (clojure.core/count statement)))
                  (list 'then
                        (clojure.core/concat (apply list 'fn  (clojure.core/butlast statement))
                                             [(list 'return (clojure.core/last statement))]))

                  (clojure.core/pos? (clojure.core/count statement))
                  (list 'then
                        (clojure.core/concat (apply list 'fn [] (clojure.core/butlast statement))
                                             [(list 'return (clojure.core/last statement))]))
                  
                  :else (h/error "Not valid" {:input statement}))
            
            (or (h/form? statement)
                (symbol? statement))
            (list 'then (list 'fn [] (list 'return statement)))
            
            :else (h/error "Not valid" {:input statement})))
    (clojure.core/rest body))))

(defmacro.js wrap:print
  "wraps a form in a print statement"
  {:added "4.0"}
  [f]
  (h/$ (fn [...args]
         (console.log "INPUT" args)
         (var out (~f ...args))
         (console.log "OUTPUT" out)
         (return out))))

(defmacro.js ^{:standalone true}
  settle
  "notify on future"
  {:added "4.0"}
  [f val]
  (h/$ (do:>
        (var out ~val)
        (if (and (not= nil out)
                 (== "Promise" (. out ["constructor"] ["name"])))
          (return (. out (then ~f)))
          (return (~f out))))))

(defmacro.js ^{:standalone true}
  notify
  "notify on future"
  {:added "4.0"}
  [val & [f]]
  (h/$ (do:>
        (var out ~val)
        (if (and (not= nil out)
                 (== "Promise" (. out ["constructor"] ["name"])))
          (return (. out
                     (then  (xt.lang.base-repl/>notify ~f))
                     (catch (xt.lang.base-repl/>notify ~f))))
          (return (xt.lang.base-repl/notify out))))))

(defmacro.js ^{:standalone true}
  notify-api
  "notify on api return"
  {:added "4.0"}
  [val]
  (h/$ (do:>
        (var out ~val)
        (if (== "Promise" (. out ["constructor"] ["name"]))
          (return (. out
                     (then (fn [res]
                             (if (== (. res ["status"])
                                       "ok")
                               (xt.lang.base-repl/notify (. res ["data"]))
                               (xt.lang.base-repl/notify res))))))
          (return (xt.lang.base-repl/notify out)))))) 

(defmacro.js STACKTRACE!
  "Adds a trace entry with stack infomation"
  {:added "4.0"}
  [msg & [tag]]
  (h/$ (try
         (throw (new Error ~msg))
         (catch e (xt.lang.base-lib/TRACE! (. e ["stack"]) ~tag)))))

(defmacro.js
  LOG!
  "like `xt.lang.base-lib/LOG!` but also for promises"
  {:added "4.0"}
  [body]
  (let [{:keys [line]} (meta (l/macro-form))
        {:keys [namespace id]} (:entry (l/macro-opts))
        label (clojure.core/str (str (or namespace
                                         (h/ns-sym))
                                     "/" id))]
    (h/$ (do:>
          (var out ~body)
          (if (== "Promise" (. out ["constructor"] ["name"]))
            (return (. out
                       (then (fn [res]
                               (console.log ~label ~line "\n\n" res)
                               (return res)))
                       (catch (fn [err]
                                (console.log ~label ~line "\n\n" err)
                                (return err)))))
            (do (console.log ~label ~line "\n\n" out)
                (return out)))))))

(defmacro <!
  "shortcut for notify/wait-on"
  {:added "4.0"}
  [body & [f]]
  (list 'xt.lang.base-notify/wait-on [:js 5000]
        (with-meta (list 'js.core/notify body f)
          (meta &form))))

(defmacro <api
  "shortcut for notify/wait-on for api calls"
  {:added "4.0"}
  [body]
  (list 'xt.lang.base-notify/wait-on :js
        (list 'js.core/notify-api body)))

(defmacro.js module:async
  "wraps an esm import to split out components"
  {:added "4.0"}
  [module]
  (h/$ (new Proxy
            ~module
            {:get (fn [esm key]
                    (return (. esm
                               (then (fn [esm]
                                       (return {"__esMODULE" true
                                                :default (. esm default [key])}))))))})))

(defmacro.js module:demos
  [ns]
  (apply vector :<> 
         (->> (ns-interns ns)
              (clojure.core/filter
               (fn [[k v]] (str/ends-with? (str k) "Demo")))
              (clojure.core/sort-by
               (fn [[_ v]] (:line (meta v))))
              (clojure.core/map (fn [[k _]]
                                  [:% (symbol (str ns)
                                              (str k))])))))

(comment

  
  (defmacro.js ^{:standalone true}
    chain
    [task next-fn]
    (list :? (list '== "Promise" (list '. task ["constructor"] ["name"]))
          (list '. task (list 'then next-fn))
          (list next-fn task))))

(comment
  (!.js (super))
  (./create-tests)
  (!.js
 (:- "hello" "world")))



