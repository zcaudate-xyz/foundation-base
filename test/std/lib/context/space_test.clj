(ns std.lib.context.space-test
  (:use code.test)
  (:require [std.lib.context.space :refer :all]
            [std.lib.context.registry :as reg]))

(fact:global
 {:component
  {|sp| {:create (space-create {:namespace 'test})
         :setup  (fn [sp]
                   (doto sp (space-context-set :null :default {})))}}})

^{:refer std.lib.context.space/space-context-set :added "3.0"
  :setup  [(def |sp| (space-create {:namespace 'test}))]}
(fact "sets the context in the space"
  ^:hidden

  (space-context-set |sp| :null :default {})
  => (contains-in
      [:changed {:null {:context :null, :scratch std.lib.context.registry.RuntimeNull,
                        :key :default, :resource :hara/context.rt.null, :config {}}}]))

^{:refer std.lib.context.space/space-context-unset :added "3.0"
  :use [|sp|]}
(fact "unsets the context in the space"
  ^:hidden

  (space-context-unset |sp| :null)
  => (contains {:context :null, :key :default,
                :resource :hara/context.rt.null, :config {}}))

^{:refer std.lib.context.space/space-context-get :added "3.0"
  :use [|sp|]}
(fact "gets the context in the space"
  ^:hidden

  (space-context-get |sp| :null)
  => (contains
      {:context :null, :key :default,
       :resource :hara/context.rt.null, :config {} :variant :default}))

^{:refer std.lib.context.space/space-rt-start :added "3.0"
  :use [|sp|]}
(fact "starts the context runtime"
  ^:hidden

  (space-rt-start |sp| :null)
  => reg/rt-null?)

^{:refer std.lib.context.space/space-rt-stop :added "3.0"
  :use [|sp|]}
(fact "stops the context runtime"
  ^:hidden

  (space-rt-stop |sp| :null)
  => nil

  (-> (doto |sp|
        (space-rt-start :null))
      (space-rt-stop :null))
  => reg/rt-null?)

^{:refer std.lib.context.space/space-stop :added "3.0"}
(fact "shutdown all runtimes in the space")

^{:refer std.lib.context.space/space? :added "3.0"}
(fact "checks that an object is of type space"
  ^:hidden

  (space? (space-create {:namespace 'test}))
  => true)

^{:refer std.lib.context.space/space-create :added "3.0"}
(fact "creates a space"
  ^:hidden

  (space-create {:namespace 'test}))

^{:refer std.lib.context.space/space :added "3.0"}
(fact "gets the space in the current namespace"

  (space))

^{:refer std.lib.context.space/space-resolve :added "3.0"}
(fact "resolves a space given various inputs"

  (space-resolve *ns*))

^{:refer std.lib.context.space/protocol-tmpl :added "3.0"}
(fact "constructs a template function"
  ^:hidden

  ((protocol-tmpl {:prefix   "space-"
                  :protocol "protocol.context"
                  :instance 'sp
                  :resolve  `space-resolve
                  :default  `*namespace*})
   '{:name -rt-get :arglists ([sp ctx])})
  => '(clojure.core/defn space-rt-get
        ([ctx] (space-rt-get std.lib.context.space/*namespace* ctx))
        ([sp ctx] (clojure.core/-> (std.lib.context.space/space-resolve sp)
                                   (protocol.context/-rt-get ctx)))))

^{:refer std.lib.context.space/space:rt-current :added "4.0"}
(fact "gets the current rt in the space")


^{:refer std.lib.context.space/rt-proxy? :added "4.0"}
(fact "gets the current rt in the space"
  ^:hidden
  
  (rt-proxy?
   (map->RuntimeProxy {:namespace 'test
                       :ctx   :null}))
  true)


(comment
  space-context-set
  (space:rt-get rt:scratch :lang/lua)
  (reg/registry-list)
  (space-rt (space 'std.lang.codegen.form-test)
            :lang/start)
  
  (rt:current)
  
  (res/res:spec-get :hara/context.space)
  (res/res-key :namespace :hara/context.space :default {:namespace (the-ns 'std.lib.context.pointer)})
  
  (res/res-stop :hara/context.space)
  
  
  (h/hash-code (res/res :hara/context.space))
  (h/hash-code (res/res :hara/context.space {:namespace 'std.lib.context.pointer}))
  (h/hash-code (res/res :hara/context.space {:namespace (the-ns 'std.lib.context.pointer)}))
  

  (space-create )
  (into {} (space))
  (into {} (space 'hello)))
