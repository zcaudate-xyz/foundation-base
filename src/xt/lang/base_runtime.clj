(ns xt.lang.base-runtime
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.lib.function :as f]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]
   :export [MODULE]})

(defn.xt xt-exists?
  "checks that the xt map exists"
  {:added "4.0"}
  []
  (return (x:global-has? XT)))

(defn.xt xt-create
  "creates an empty xt structure"
  {:added "4.0"}
  []
  (return {"::" "xt"
           :config   {}
           :spaces   {}
           :hash     {:lookup   (k/lu-create)
                      :counter  (:- "0x011c9dc5")}}))

(defn.xt
  xt
  "gets the current xt or creates a new one"
  {:added "4.0"}
  []
  (if (x:global-has? XT)
    (return (!:G XT))
    (do (x:global-set XT (-/xt-create))
        (return (!:G XT)))))

(defn.xt
  xt-current
  "gets the current xt"
  {:added "4.0"}
  []
  (if (x:global-has? XT)
    (return (!:G XT))
    (return nil)))

(defn.xt xt-purge
  "empties the current xt"
  {:added "4.0"}
  []
  (if (x:global-has? XT)
    (do (var out (!:G XT))
        (x:global-del XT)
        (return out))
    (return nil)))

(defn.xt xt-purge-config
  "clears all `:config` entries"
  {:added "4.0"}
  []
  (var g (-/xt))
  (var prev (k/get-key g "config"))
  (k/set-key g "config" {})
  (return [true prev]))

(defn.xt xt-purge-spaces
  "clears all `:spaces` entries"
  {:added "4.0"}
  []
  (var g (-/xt))
  (var prev (k/get-key g "spaces"))
  (k/set-key g "spaces" {})
  (return [true prev]))

(defn.xt xt-lookup-id
  "gets the runtime id for pointer-like objects"
  {:added "4.0"}
  [obj]
  (when (or (k/fn? obj)
            (k/obj? obj)
            (k/arr? obj))
    (var #{hash} (-/xt))
    (var #{lookup counter} hash)
    (var hash-id (k/lu-get lookup obj))
    (when (k/nil? hash-id)
      (k/set-key hash "counter" (+ 1 counter))
      (k/lu-set lookup obj counter)
      (return counter))
    (return hash-id)))

;;
;; CONFIG
;; 


(defn.xt xt-config-list
  "lists all config entries in the xt"
  {:added "4.0"}
  []
  (var g (-/xt))
  (var #{config} g)
  (return (k/obj-keys config)))

(defn.xt xt-config-set
  "sets the config for a module"
  {:added "4.0"}
  [module m]
  (var g (-/xt))
  (var #{config} g)
  (var prev (k/get-key config module))
  (k/set-key config module m)
  (return [true prev]))

(defn.xt xt-config-del
  "deletes a single xt config entry"
  {:added "4.0"}
  [module]
  (var g (-/xt))
  (var #{config} g)
  (var prev (k/get-key config module))
  (k/del-key config module)
  (return [true prev]))

(defn.xt xt-config
  "gets a config entry"
  {:added "4.0"}
  [module]
  (var g (-/xt))
  (var #{config} g)
  (return (k/get-key config module)))

;;
;; SPACE
;; 

(defn.xt xt-space-list
  "lists all spaces in the xt"
  {:added "4.0"}
  []
  (var g (-/xt))
  (var #{spaces} g)
  (return (k/obj-keys spaces)))

(defn.xt xt-space-del
  "deletes a space"
  {:added "4.0"}
  [module]
  (var g (-/xt))
  (var #{spaces} g)
  (var prev  (k/get-key spaces module))
  (k/del-key g module)
  (return [true prev]))

(defn.xt xt-space
  "gets a space"
  {:added "4.0"}
  [module]
  (var g (-/xt))
  (var #{spaces} g)
  (var curr  (k/get-key spaces module))
  (when (k/nil? curr)
    (:= curr {})
    (k/set-key spaces module curr))
  (return curr))

(defn.xt xt-space-clear
  "clears all items in the space"
  {:added "4.0"}
  [module]
  (var g (-/xt))
  (var #{spaces} g)
  (var prev  (k/get-key spaces module))
  (k/set-key spaces module {})
  (return [true prev]))

;;
;; ITEM
;; 

(defn.xt xt-item-del
  "deletes a single item in the space"
  {:added "4.0"}
  [module key]
  (var space (-/xt-space module))
  (var prev (k/get-key space key))
  (k/del-key space key)
  (return [true prev]))

(defn.xt xt-item-trigger
  "triggers as item"
  {:added "4.0"}
  [module key]
  (var space (-/xt-space module))
  (var prev  (k/get-key space key))
  (when (k/not-nil? prev)
    (var #{value watch} prev)
    (k/for:object [[watch-key watch-fn] watch]
      (watch-fn value (k/cat module "/" key)))
    (return (k/obj-keys watch))))

(defn.xt xt-item-set
  "sets a single item in the space"
  {:added "4.0"}
  [module key value]
  (var space (-/xt-space module))
  (var prev  (k/get-key space key))
  (when (k/nil? prev)
    (:= prev {:watch {}})
    (k/set-key space key prev))
  (k/set-key prev "value" value)
  (var #{watch} prev)
  (k/for:object
   [[watch-key watch-fn] watch]
   (watch-fn value (k/cat module "/" key)))
  (return [true prev]))

(defn.xt xt-item
  "gets an xt item by module and key"
  {:added "4.0"}
  [module key]
  (var space (-/xt-space module))
  (var curr  (k/get-key space key))
  (when curr
    (return (k/get-key curr "value")))
  (return nil))

(defn.xt xt-item-get
  "gets an xt item or sets a default if not exist"
  {:added "4.0"}
  [module key init-fn]
  (var space (-/xt-space module))
  (var curr (k/get-key space key))
  (cond curr
        (return (k/get-key curr "value"))
        
        :else
        (do (var value (init-fn))
            (k/set-key space key {:value value
                                  :watch {}})
            (return value))))

(defn.xt xt-var-entry
  "gets the var entry"
  {:added "4.0"}
  [sym]
  (var [module key] (k/sym-pair sym))
  (var space (-/xt-space module))
  (return (k/get-key space key)))

(defn.xt xt-var
  "gets an xt item
 
   (!.lua
    (rt/xt-var \"-/hello\"))"
  {:added "4.0"}
  [sym]
  (var [module key] (k/sym-pair sym))
  (return (-/xt-item module key)))

(defn.xt xt-var-set
  "sets the var"
  {:added "4.0"}
  [sym value]
  (var [module key] (k/sym-pair sym))
  (if (k/nil? value)
    (return (-/xt-item-del module key))
    (return (-/xt-item-set module key value))))

(defn.xt xt-var-trigger
  "triggers the var"
  {:added "4.0"}
  [sym]
  (var [module key] (k/sym-pair sym))
  (return (-/xt-item-trigger module key)))

(defn.xt xt-add-watch
  "adds a watch"
  {:added "4.0"}
  [sym watch-key watch-fn]
  (var entry (-/xt-var-entry sym))
  (when entry
    (var #{watch} entry)
    (k/set-key watch watch-key watch-fn)
    (return true))
  (return false))

(defn.xt xt-remove-watch
  "removes a watch"
  {:added "4.0"}
  [sym watch-key]
  (var entry (-/xt-var-entry sym))
  (when entry
    (var #{watch} entry)
    (k/del-key watch watch-key)
    (return true))
  (return false))

(def.xt MODULE (!:module))

(defn defvar-fn
  "helper function for defvar macros"
  {:added "4.0"}
  [&form tag sym-id doc? attrs? more]
  (let [sym-ns  (or (get (meta sym-id) :ns)
                    (str (h/ns-sym)))
        sym-id  (if (vector? sym-id)
                  (first sym-id)
                  sym-id)
        sym-key (h/strn sym-id)
        [doc attr more] (f/fn:init-args doc? attrs? more)
        more (if (vector? (first more))
               more
               (first more))
        def-sym (clojure.core/symbol (str "defn." tag))]
    (h/$ [(~def-sym ~(with-meta sym-id (merge (meta &form)
                                              (meta sym-id)))
            []
            (return (xt.lang.base-runtime/xt-item-get
                     ~sym-ns
                     ~sym-key
                     (fn ~@more))))
          (~def-sym ~(with-meta (clojure.core/symbol (str sym-id "-reset"))
                       (merge (meta &form)
                              (meta sym-id)))
            [val]
            (return (xt.lang.base-runtime/xt-var-set
                     ~(str sym-ns "/" sym-key)
                     val)))])))

(defmacro defvar.xt
  "shortcut for a xt getter and a reset var"
  {:added "4.0"}
  [sym-id & [doc? attrs? & more]]
  (defvar-fn &form "xt" sym-id doc? attrs? more))

(defmacro defvar.js
  "shortcut for a js getter and a reset var"
  {:added "4.0"}
  [sym-id & [doc? attrs? & more]]
  (defvar-fn &form "js" sym-id doc? attrs? more))

(defmacro defvar.lua
  "shortcut for a lua getter and a reset var"
  {:added "4.0"}
  [sym-id & [doc? attrs? & more]]
  (defvar-fn &form "lua" sym-id doc? attrs? more))

(defmacro defvar.py
  "TODO"
  {:added "4.0"}
  [sym-id & [doc? attrs? & more]]
  (defvar-fn &form "python" sym-id doc? attrs? more))
