(ns xt.lang.event-route
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.event-common :as event-common]]
   :export  [MODULE]})

(defn.xt interim-from-url
  "creates interim from url"
  {:added "4.0"}
  [url]
  (var arr (k/split (k/cat "/" url) "?"))
  (var body (k/first arr))
  (var search nil)
  (when (< 1 (k/len arr))
    (:= search (k/second arr)))
  (var path (-> (k/split body "/")
                (k/arr-filter k/not-empty?)))
  (var params {})
  (when search
    (k/for:array [pair (k/split search "&")]
      (var [key val] (k/split pair "="))
      (k/set-key params key val)))
  (cond (k/is-empty? params)
        (return {:path path :params {}})

        :else
        (return {:path path :params {(k/json-encode path) params}})))

(defn.xt interim-to-url
  "creates url from interim"
  {:added "4.0"}
  [interim]
  (var #{path params} interim)
  (var param-arr [])
  (k/for:object [[key val] (or (k/get-key params (k/json-encode path))
                               {})]
    (when val
      (x:arr-push param-arr (k/cat key "=" val))))
  (return
   (k/cat (k/arr-join path "/")
          (:? (k/not-empty? param-arr)
              (k/cat "?" (k/arr-join param-arr "&"))
              ""))))

(defn.xt path-to-tree
  "turns a path to tree"
  {:added "4.0"}
  [path terminate]
  (var out {})
  (var arr [])
  (k/for:array [[i v] path]
    (k/set-key out (k/json-encode arr) v)
    (x:arr-push arr v))
  (when terminate
    (k/set-key out (k/json-encode arr) nil))
  (return out))

(defn.xt interim-to-tree
  "converts interim to tree"
  {:added "4.0"}
  [interim terminate]
  (var #{path params} interim)
  (var tree (-/path-to-tree path terminate))
  (k/set-key tree "params" params)
  (return tree))

(defn.xt path-from-tree
  "gets the path from tree"
  {:added "4.0"}
  [tree]
  (var path [])
  (var v    (k/get-key tree (k/json-encode path)))
  (while (k/not-empty? v)
    (x:arr-push path v)
    (:= v (k/get-key tree (k/json-encode path))))
  (return path))

(defn.xt path-params-from-tree
  "gets path params from tree"
  {:added "4.0"}
  [tree path]
  (return
   (or (-> tree
           (k/get-key "params")
           (k/get-key (k/json-encode path)))
       {})))

(defn.xt interim-from-tree
  "converts interim from tree"
  {:added "4.0"}
  [tree]
  (var #{params} tree)
  (var path (-/path-from-tree tree))
  (return {:path path
           :params params}))

(defn.xt changed-params-raw
  "checks for changed params"
  {:added "4.0"}
  [pparams nparams]
  (:= pparams (or pparams {}))
  (:= nparams (or nparams {}))
  (var diff-fn
       (fn [m other]
         (var out {})
         (k/for:object [[k v] m]
           (when (not= v (k/get-key other k))
             (k/set-key out k true)))
         (return out)))
  (return (k/obj-assign
           (diff-fn pparams nparams)
           (diff-fn nparams pparams))))

(defn.xt changed-params
  "gets diff between params"
  {:added "4.0"}
  [ptree ntree path]
  (var pparams (-/path-params-from-tree ptree (or path [])))
  (var nparams (-/path-params-from-tree ntree (or path [])))
  (return (-/changed-params-raw pparams nparams)))

(defn.xt changed-path-raw
  "checks that path has changed"
  {:added "4.0"}
  [ppath npath]
  (var all {})
  (var arr [])
  (var changed false)
  
  (k/for:array [[i v] npath]
    (var pv (k/get-idx ppath i))
    (when (not= pv v)
      (:= changed true))
    (when changed
      (k/set-key all (k/json-encode arr) true))
    (x:arr-push arr v))
  (return all))

(defn.xt changed-path
  "gets changed routes"
  {:added "4.0"}
  [ptree ntree]
  (var ppath (-/path-from-tree ptree))
  (var npath (-/path-from-tree ntree))
  (return (-/changed-path-raw ppath npath)))

(defn.xt get-url
  "gets the url for the route"
  {:added "4.0"}
  [route]
  (var #{tree} route)
  (return (-/interim-to-url (-/interim-from-tree tree))))

(defn.xt get-segment
  "gets the value for a segment segment"
  {:added "4.0"}
  [route path]
  (var #{tree} route)
  (var pkey (k/json-encode path))
  (return (k/get-key tree pkey)))

(defn.xt get-param
  "gets the param value"
  {:added "4.0"}
  [route param path]
  (var #{tree} route)
  (:= path (or path (-/path-from-tree tree)))
  (return (k/get-key (-/path-params-from-tree tree path)
                     param)))

(defn.xt get-all-params
  "gets all params in the route"
  {:added "4.0"}
  [route path]
  (var #{tree} route)
  (:= path (or path (-/path-from-tree tree)))
  (return (-/path-params-from-tree tree path)))


;;
;;
;;

(defn.xt make-route
  "makes a route"
  {:added "4.0"}
  [initial]
  (var input   (:? (k/fn? initial) (initial) initial))
  (var interim (-/interim-from-url input))
  (var tree    (-/interim-to-tree interim false))
  (return
   (event-common/blank-container
    "event.route"
    {:tree      tree
     :history   []})))

(defn.xt add-url-listener
  "adds a url listener"
  {:added "4.0"}
  [route listener-id callback meta]
  (return
   (event-common/add-listener
    route listener-id "route.url"
    callback
    meta
    (fn:> true))))

(defn.xt add-path-listener
  "adds a path listener"
  {:added "4.0"}
  [route path listener-id callback meta]
  (var pkey (k/json-encode path))
  (return
   (event-common/add-listener
    route listener-id "route.path"
    callback
    (k/obj-assign
     {:route/path path}
     meta)
    (fn [event]
      (return (k/get-key (. event ["path"])
                         pkey))))))

(defn.xt add-param-listener
  "adds a param listener"
  {:added "4.0"}
  [route param listener-id callback meta]
  (return
   (event-common/add-listener
    route listener-id "route.param"
    callback
    (k/obj-assign
     {:route/param param}
     meta)
    (fn [event]
      (return (k/get-key (. event ["params"])
                         param))))))

(defn.xt add-full-listener
  "adds a full listener"
  {:added "4.0"}
  [route path param listener-id callback meta]
  (var pkey (k/json-encode path))
  (return
   (event-common/add-listener
    route listener-id "route.full"
    callback
    (k/obj-assign
     {:route/path  path
      :route/param param}
     meta)
    (fn [event]
      (return (and (k/get-key (. event ["path"])
                              pkey)
                   (k/get-key (. event ["params"])
                              param)))))))

(def.xt ^{:arglists '([route listener-id])}
  remove-listener
  event-common/remove-listener)

(def.xt ^{:arglists '([route])}
  list-listeners
  event-common/list-listeners)

(defn.xt set-url
  "sets the url for a route"
  {:added "4.0"}
  [route url terminate]
  (var #{tree listeners} route)
  (var ninterim (-/interim-from-url url))
  (var ninterim-params (k/get-key ninterim "params"))
  (var all-params (k/get-key tree "params"))
  
  ^CHANGES
  (var ppath   (-/path-from-tree tree))
  (var npath   (k/get-key ninterim "path"))
  (var pkey    (k/json-encode npath))
  
  (var pparams (k/get-key all-params pkey))
  (var nparams (k/get-key ninterim-params pkey))

  (var dpath   (-/changed-path-raw ppath npath))
  (var dparams (-/changed-params-raw pparams nparams))
  
  ^MERGE
  (k/obj-assign tree (-/path-to-tree npath terminate))
  (cond (k/is-empty? nparams)
        (k/del-key all-params pkey)
        
        :else
        (k/set-key all-params pkey nparams))

  (var #{history} route)
  (k/arr-pushl history url 50)
  (return
   (event-common/trigger-listeners
    route
    {:type "route.url"
     :params dparams
     :path   dpath})))

(defn.xt set-path
  "sets the path and param"
  {:added "4.0"}
  [route path params]
  (var #{tree} route)
  (var all-params (k/get-key tree "params"))

  ^CHANGES
  (var ppath    (-/path-from-tree tree))
  (var npath    (or path ppath))
  (var pkey    (k/json-encode npath))
  
  (var pparams  (k/get-key all-params pkey))
  (var nparams  (or params pparams))

  (var dpath   (-/changed-path-raw ppath npath))
  (var dparams (-/changed-params-raw pparams nparams))

  ^MERGE
  (k/obj-assign tree (-/path-to-tree npath true))
  (cond (k/is-empty? nparams)
        (k/del-key all-params pkey)
        
        :else
        (k/set-key all-params pkey nparams))

  (var #{history} route)
  (k/arr-pushl history (-/get-url route) 50)
  (return
   (event-common/trigger-listeners
    route
    {:type "route.path"
     :params dparams
     :path   dpath})))

(defn.xt set-segment
  "sets the current segment"
  {:added "4.0"}
  [route path value]
  (var #{tree} route)
  (var pkey   (k/json-encode path))
  (var pvalue (k/get-key tree pkey))
  (k/set-key tree pkey value)
  
  (var #{history} route)
  (k/arr-pushl history (-/get-url route) 50)
  (return
   (event-common/trigger-listeners
    route
    {:type "route.path"
     :params {}
     :path   {pkey true}})))

(defn.xt set-param
  "sets a param in a route"
  {:added "4.0"}
  [route param value path]
  (var #{tree} route)
  (:= path  (or path (-/path-from-tree tree)))
  (var pkey (k/json-encode path))
  (var all-params (k/get-key tree "params"))
  (var pparams (or (k/get-key all-params pkey) {}))
  (var pvalue  (k/get-key pparams param))
  (cond (not= pvalue value)
        (do (cond (k/nil? value)
                  (k/del-key pparams param)

                  :else
                  (k/set-key pparams param value))

            (cond (k/is-empty? pparams)
                  (k/del-key all-params pkey)
                  
                  :else
                  (k/set-key all-params pkey pparams))

            (var #{history} route)
            (k/arr-pushl history (-/get-url route) 50)
            (return
             (event-common/trigger-listeners
              route
              {:type "route.params"
               :params {param true}
               :path   {}})))

        :else
        (return [])))

(defn.xt reset-route
  "resets the route, clearing all params"
  {:added "4.0"}
  [route url]
  (k/set-key route "history" [])
  (k/set-key route "tree"
             (-/interim-to-tree (-/interim-from-url (or url "")) true))
  (-/set-url route (or url "") true))

(def.xt MODULE (!:module))
