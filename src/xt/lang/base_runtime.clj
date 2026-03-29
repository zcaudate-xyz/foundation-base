(ns xt.lang.base-runtime
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]
            [std.lib.env :as env]
            [std.lib.foundation]
            [std.lib.function :as f]
            [std.lib.template :as template]))

(l/script :xtalk
  {:require [[xt.lang.common-base :as common-base]
             [xt.lang.common-data :as common-data]
             [xt.lang.common-string :as common-string]]})

(defspec.xt XTWatchFn
  [:fn [:xt/any :xt/str] :xt/any])

(defspec.xt XTWatchMap
  [:xt/dict :xt/str XTWatchFn])

(defspec.xt XTItem
  [:xt/record
   ["value" :xt/any]
   ["watch" XTWatchMap]])

(defspec.xt XTSpace
  [:xt/dict :xt/str XTItem])

(defspec.xt XTConfigMap
  [:xt/dict :xt/str :xt/any])

(defspec.xt XTSpacesMap
  [:xt/dict :xt/str XTSpace])

(defspec.xt XTLookup
  :xt/any)

(defspec.xt XTHash
  [:xt/record
   ["lookup" XTLookup]
   ["counter" :xt/num]])

(defspec.xt XTState
  [:xt/record
   ["::" :xt/str]
   ["config" XTConfigMap]
   ["spaces" XTSpacesMap]
   ["hash" XTHash]])

(defspec.xt xt-exists?
  [:fn [] :xt/bool])

(defspec.xt xt-create
  [:fn [] XTState])

(defspec.xt xt
  [:fn [] XTState])

(defspec.xt xt-current
  [:fn [] [:xt/maybe XTState]])

(defspec.xt xt-purge
  [:fn [] [:xt/maybe XTState]])

(defspec.xt xt-purge-config
  [:fn [] [:xt/tuple :xt/bool XTConfigMap]])

(defspec.xt xt-purge-spaces
  [:fn [] [:xt/tuple :xt/bool XTSpacesMap]])

(defspec.xt xt-lookup-id
  [:fn [:xt/any] [:xt/maybe :xt/num]])

(defspec.xt xt-config-list
  [:fn [] [:xt/array :xt/str]])

(defspec.xt xt-config-set
  [:fn [:xt/str :xt/any]
   [:xt/tuple :xt/bool [:xt/maybe :xt/any]]])

(defspec.xt xt-config-del
  [:fn [:xt/str]
   [:xt/tuple :xt/bool [:xt/maybe :xt/any]]])

(defspec.xt xt-config
  [:fn [:xt/str] [:xt/maybe :xt/any]])

(defspec.xt xt-space-list
  [:fn [] [:xt/array :xt/str]])

(defspec.xt xt-space-del
  [:fn [:xt/str]
   [:xt/tuple :xt/bool [:xt/maybe XTSpace]]])

(defspec.xt xt-space
  [:fn [:xt/str] XTSpace])

(defspec.xt xt-space-clear
  [:fn [:xt/str]
   [:xt/tuple :xt/bool [:xt/maybe XTSpace]]])

(defspec.xt xt-item-del
  [:fn [:xt/str :xt/str]
   [:xt/tuple :xt/bool [:xt/maybe XTItem]]])

(defspec.xt xt-item-trigger
  [:fn [:xt/str :xt/str]
   [:xt/maybe [:xt/array :xt/str]]])

(defspec.xt xt-item-set
  [:fn [:xt/str :xt/str :xt/any]
   [:xt/tuple :xt/bool XTItem]])

(defspec.xt xt-item
  [:fn [:xt/str :xt/str]
   [:xt/maybe :xt/any]])

(defspec.xt xt-item-get
  [:fn [:xt/str :xt/str [:fn [] :xt/any]]
   :xt/any])

(defspec.xt xt-var-entry
  [:fn [:xt/str]
   [:xt/maybe XTItem]])

(defspec.xt xt-var
  [:fn [:xt/str]
   [:xt/maybe :xt/any]])

(defspec.xt xt-var-set
  [:fn [:xt/str :xt/any]
   [:xt/tuple :xt/bool [:xt/maybe XTItem]]])

(defspec.xt xt-var-trigger
  [:fn [:xt/str]
   [:xt/maybe [:xt/array :xt/str]]])

(defspec.xt xt-add-watch
  [:fn [:xt/str :xt/str XTWatchFn]
   :xt/bool])

(defspec.xt xt-remove-watch
  [:fn [:xt/str :xt/str]
   :xt/bool])

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
           :hash     {:lookup   (x:lu-create)
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
  (var prev (x:get-key g "config"))
  (x:set-key g "config" {})
  (return [true prev]))

(defn.xt xt-purge-spaces
  "clears all `:spaces` entries"
  {:added "4.0"}
  []
  (var g (-/xt))
  (var prev (x:get-key g "spaces"))
  (x:set-key g "spaces" {})
  (return [true prev]))

(defn.xt xt-lookup-id
  "gets the runtime id for pointer-like objects"
  {:added "4.0"}
  [obj]
  (when (or (common-base/fn? obj)
            (common-base/obj? obj)
            (common-base/arr? obj))
    (var #{hash} (-/xt))
    (var #{lookup counter} hash)
    (var hash-id (x:lu-get lookup obj))
    (when (x:nil? hash-id)
      (x:set-key hash "counter" (+ 1 counter))
      (x:lu-set lookup obj counter)
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
  (return (common-data/obj-keys config)))

(defn.xt xt-config-set
  "sets the config for a module"
  {:added "4.0"}
  [module m]
  (var g (-/xt))
  (var #{config} g)
  (var prev (x:get-key config module))
  (x:set-key config module m)
  (return [true prev]))

(defn.xt xt-config-del
  "deletes a single xt config entry"
  {:added "4.0"}
  [module]
  (var g (-/xt))
  (var #{config} g)
  (var prev (x:get-key config module))
  (x:del-key config module)
  (return [true prev]))

(defn.xt xt-config
  "gets a config entry"
  {:added "4.0"}
  [module]
  (var g (-/xt))
  (var #{config} g)
  (return (x:get-key config module)))

;;
;; SPACE
;; 

(defn.xt xt-space-list
  "lists all spaces in the xt"
  {:added "4.0"}
  []
  (var g (-/xt))
  (var #{spaces} g)
  (return (common-data/obj-keys spaces)))

(defn.xt xt-space-del
  "deletes a space"
  {:added "4.0"}
  [module]
  (var g (-/xt))
  (var #{spaces} g)
  (var prev  (x:get-key spaces module))
  (x:del-key g module)
  (return [true prev]))

(defn.xt xt-space
  "gets a space"
  {:added "4.0"}
  [module]
  (var g (-/xt))
  (var #{spaces} g)
  (var curr  (x:get-key spaces module))
  (when (x:nil? curr)
    (:= curr {})
    (x:set-key spaces module curr))
  (return curr))

(defn.xt xt-space-clear
  "clears all items in the space"
  {:added "4.0"}
  [module]
  (var g (-/xt))
  (var #{spaces} g)
  (var prev  (x:get-key spaces module))
  (x:set-key spaces module {})
  (return [true prev]))

;;
;; ITEM
;; 

(defn.xt xt-item-del
  "deletes a single item in the space"
  {:added "4.0"}
  [module key]
  (var space (-/xt-space module))
  (var prev (x:get-key space key))
  (x:del-key space key)
  (return [true prev]))

(defn.xt xt-item-trigger
  "triggers as item"
  {:added "4.0"}
  [module key]
  (var space (-/xt-space module))
  (var prev  (x:get-key space key))
  (when (x:not-nil? prev)
    (var #{value watch} prev)
    (for:object [[watch-key watch-fn] watch]
      (watch-fn value (x:cat module "/" key)))
    (return (common-data/obj-keys watch))))

(defn.xt xt-item-set
  "sets a single item in the space"
  {:added "4.0"}
  [module key value]
  (var space (-/xt-space module))
  (var prev  (x:get-key space key))
  (when (x:nil? prev)
    (:= prev {:watch {}})
    (x:set-key space key prev))
  (x:set-key prev "value" value)
  (var #{watch} prev)
  (for:object
   [[watch-key watch-fn] watch]
   (watch-fn value (x:cat module "/" key)))
  (return [true prev]))

(defn.xt xt-item
  "gets an xt item by module and key"
  {:added "4.0"}
  [module key]
  (var space (-/xt-space module))
  (var curr  (x:get-key space key))
  (when curr
    (return (x:get-key curr "value")))
  (return nil))

(defn.xt xt-item-get
  "gets an xt item or sets a default if not exist"
  {:added "4.0"}
  [module key init-fn]
  (var space (-/xt-space module))
  (var curr (x:get-key space key))
  (cond curr
        (return (x:get-key curr "value"))
        
        :else
        (do (var value (init-fn))
            (x:set-key space key {:value value
                                  :watch {}})
            (return value))))

(defn.xt xt-var-entry
  "gets the var entry"
  {:added "4.0"}
  [sym]
  (var [module key] (common-string/sym-pair sym))
  (var space (-/xt-space module))
  (return (x:get-key space key)))

(defn.xt xt-var
  "gets an xt item
 
   (!.lua
    (rt/xt-var \"-/hello\"))"
  {:added "4.0"}
  [sym]
  (var [module key] (common-string/sym-pair sym))
  (return (-/xt-item module key)))

(defn.xt xt-var-set
  "sets the var"
  {:added "4.0"}
  [sym value]
  (var [module key] (common-string/sym-pair sym))
  (if (x:nil? value)
    (return (-/xt-item-del module key))
    (return (-/xt-item-set module key value))))

(defn.xt xt-var-trigger
  "triggers the var"
  {:added "4.0"}
  [sym]
  (var [module key] (common-string/sym-pair sym))
  (return (-/xt-item-trigger module key)))

(defn.xt xt-add-watch
  "adds a watch"
  {:added "4.0"}
  [sym watch-key watch-fn]
  (var entry (-/xt-var-entry sym))
  (when entry
    (var #{watch} entry)
    (x:set-key watch watch-key watch-fn)
    (return true))
  (return false))

(defn.xt xt-remove-watch
  "removes a watch"
  {:added "4.0"}
  [sym watch-key]
  (var entry (-/xt-var-entry sym))
  (when entry
    (var #{watch} entry)
    (x:del-key watch watch-key)
    (return true))
  (return false))

(defn defvar-fn
  "helper function for defvar macros"
  {:added "4.0"}
  [&form tag sym-id doc? attrs? more]
  (let [sym-ns  (or (get (meta sym-id) :ns)
                    (str (env/ns-sym)))
        sym-id  (if (vector? sym-id)
                  (first sym-id)
                  sym-id)
        sym-key (std.lib.foundation/strn sym-id)
        [doc attr more] (f/fn:init-args doc? attrs? more)
        more (if (vector? (first more))
               more
               (first more))
        def-sym (clojure.core/symbol (str "defn." tag))]
    (template/$ [(~def-sym ~(with-meta sym-id (merge (meta &form)
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
