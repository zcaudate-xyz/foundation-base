(ns std.lib.context.pointer
  (:require [std.lib.apply :as apply]
            [std.lib.atom :as at]
            [std.lib.collection :as c]
            [std.lib.context.registry :as reg]
            [std.lib.context.space :as space]
            [std.lib.env :as env]
            [std.lib.foundation :as h]
            [std.lib.impl :as impl :refer [defimpl]]
            [std.lib.resource :as res]
            [std.protocol.apply :as protocol.apply]
            [std.protocol.component :as protocol.component]
            [std.protocol.context :as protocol.context]))

(defonce ^:dynamic *runtime* nil)

(defn pointer-deref
  "derefs a pointer"
  {:added "3.0"}
  ([{:keys [context] :as ptr}]
   (let [rt (space/space:rt-current context)]
     (protocol.context/-deref-ptr rt ptr))))

(defn pointer-default
  "function to get the pointer's context"
  {:added "4.0"}
  [ptr]
  (or *runtime*
      (:context/rt ptr)
      (if (:context/fn ptr)
        ((:context/fn ptr) ptr))
      (space/space:rt-current (:context ptr))))

(defn- pointer-string
  ([{:keys [context] :as ptr}]
   (let [rt   (h/suppress (pointer-default ptr))
         tags (and rt (protocol.context/-tags-ptr rt ptr))
         path (cond-> [(or (:context rt)
                           context)]
                tags (concat tags)
                :then vec)]
     (str "!" path (if rt
                     (str "\n" (protocol.context/-display-ptr rt ptr))
                     " <no-context>")))))

(defimpl Pointer [context]
  :invoke apply/invoke-as
  :string pointer-string
  :protocols [std.protocol.context/IPointer
              :body   {-ptr-context context
                       -ptr-keys (keys ptr)
                       -ptr-val  (get ptr key)}
              protocol.apply/IApplicable
              :body   {-apply-in         (protocol.context/-invoke-ptr rt app args)
                       -transform-in     (protocol.context/-transform-in-ptr rt app args)
                       -transform-out    (protocol.context/-transform-in-ptr rt app return)}
              :method {-apply-default    pointer-default}]
  :interfaces  [clojure.lang.IDeref
                :method {deref {[ptr] pointer-deref}}])

(defn pointer?
  "checks that object is a pointer"
  {:added "3.0"}
  ([obj]
   (instance? Pointer obj)))

(defn pointer
  "creates a pointer"
  {:added "3.0"}
  ([{:keys [context] :as m}]
   (or context (h/error "Context required"))
   (map->Pointer m)))

(def +init+
  (h/template-entries [(space/protocol-tmpl {:prefix   "rt-"
                                             :protocol "protocol.context"
                                             :instance 'rt-or-ctx
                                             :resolve  `space/space:rt-current})]
    (vals (:sigs protocol.context/IContext))
    (vals (:sigs protocol.context/IContextLifeCycle))))
