(ns std.lib.link-test
  (:use code.test)
  (:require [std.lib.link :refer :all])
  (:import (std.lib.link Link)))

(fact:global
 {:component
  {|lnk| {:create (do (->Link {:ns 'std.lib.foundation
                               :name 'ipending?}
                              #'|lnk|
                              identity
                              *registry*))}}})

^{:refer std.lib.link/resource-path :added "3.0"}
(fact "converts a namespace to a resource path"

  (resource-path 'code.test)
  => "code/test.clj")

^{:refer std.lib.link/ns-metadata-raw :added "3.0"}
(fact "returns the metadata associated with a given namespace"
  ^:hidden

  (ns-metadata-raw 'code.test.compile)
  => map?)

^{:refer std.lib.link/ns-metadata :added "3.0"}
(fact "provides source metadata support for links"

  (ns-metadata 'std.lib)
  => map?)

^{:refer std.lib.link/map->Link :added "3.0" :adopt true}
(fact "defines a Link type"
  (resolve 'map->Link) => var?)

^{:refer std.lib.link/link:create :added "3.0"}
(fact "creates a link"
  ^:hidden

  (ns-unmap *ns* '-ipending0?-)
  (declare -ipending0?-)

  (link:create {:ns 'std.lib.foundation :name 'ipending?}
               #'-ipending0?-)
  ;; #link{:source org.registry.ipending?, :bound false, :status :resolved, :synced false, :registered false}
  => link?)

^{:refer std.lib.link/link? :added "3.0"
  :use [|lnk|]}
(fact "checks if object is a link"
  ^:hidden

  (link? |lnk|)
  => true)

^{:refer std.lib.link/register-link :added "3.0"
  :use [|lnk|]}
(fact "adds link to global registry"
  ^:hidden

  (register-link |lnk|)
  => #'std.lib.link-test/|lnk|

  (registered-link? |lnk|)
  => true)

^{:refer std.lib.link/deregister-link :added "3.0"
  :use [|lnk|]}
(fact "removes a link from global registry"
  ^:hidden

  (deregister-link |lnk|)
  => #'std.lib.link-test/|lnk|

  (registered-link? |lnk|)
  => false)

^{:refer std.lib.link/registered-link? :added "3.0"
  :use [|lnk|]}
(fact "checks if a link is registered"
  ^:hidden

  (registered-link? |lnk|)
  => any?)

^{:refer std.lib.link/registered-links :added "3.0"
  :use [|lnk|]}
(fact "returns all registered links"
  ^:hidden

  (register-link |lnk|)
  => #'std.lib.link-test/|lnk|

  (registered-links)
  => (contains [(exactly #'|lnk|)]))

^{:refer std.lib.link/link:unresolved :added "3.0"}
(fact "returns all unresolved links"

  (link:unresolved)
  => (any nil? seq?))

^{:refer std.lib.link/link:resolve-all :added "3.0"}
(comment "resolves all unresolved links in a background thread"

  (link:resolve-all))

^{:refer std.lib.link/link:bound? :added "3.0"
  :use [|lnk|]}
(fact "checks if the var of the link has been bound, should be true"
  ^:hidden

  (link:bound? |lnk|)
  => true

  (link:bound? (->Link {:ns 'std.lib :name 'ipending?}
                       nil
                       nil
                       nil))
  => false)

^{:refer std.lib.link/link:status :added "3.0"
  :use [|lnk|]}
(fact "lists the current status of the link"
  ^:hidden

  (link:status (->Link {:ns 'std.lib :name 'iobj?}
                       nil
                       nil
                       nil))
  => :resolved ^:hidden

  (link:status (->Link {:ns 'std.lib :name 'WRONG}
                       nil
                       nil
                       nil))
  => :source-var-not-found

  (link:status (->Link {:ns 'error :name 'WRONG}
                       nil
                       nil
                       nil))
  => :unresolved

  (link:status (->Link {:ns 'std.lib.link-test :name '|lnk|}
                       nil
                       nil
                       nil))
  => :linked)

^{:refer std.lib.link/find-source-var :added "3.0"}
(fact "finds the source var in the link"
  ^:hidden

  (find-source-var (->Link {:ns 'std.lib :name 'iobj?}
                           nil
                           nil
                           nil))
  => #'std.lib/iobj?)

^{:refer std.lib.link/link-synced? :added "3.0"}
(fact "checks if the source and alias have the same value"
  ^:hidden

  (def -iobj?- std.lib.foundation/iobj?)

  (link-synced? (->Link {:ns 'std.lib :name 'iobj?}
                        #'-iobj?-
                        nil
                        nil))
  => true)

^{:refer std.lib.link/link-selfied? :added "3.0"}
(fact "checks if the source and alias have the same value"
  ^:hidden

  (declare -selfied-)
  (def -selfied- (link:create {:name '-selfied-}
                              #'-selfied-))

  (link-selfied? -selfied-)
  => true)

^{:refer std.lib.link/link:info :added "3.0"}
(fact "displays the link"

  (link:info -selfied-)
  => {:source 'std.lib.link-test/-selfied-
      :bound true,
      :status :linked,
      :synced false,
      :registered false})

^{:refer std.lib.link/transform-metadata :added "3.0"}
(fact "helper function for adding metadata to vars"
  (transform-metadata #'transform-metadata {} {})
  => var?)

^{:refer std.lib.link/bind-metadata :added "3.0"}
(fact "retrievess the metadata of a function from source code"
  ^:hidden

  (declare -metadata-)
  (def -metadata- (link:create {:ns 'std.lib.foundation :name 'iobj?}
                               #'-metadata-))

  (bind-metadata -metadata-)

  (-> (meta #'-metadata-)
      :arglists)
  => '([x]))

^{:refer std.lib.link/bind-source :added "3.0"}
(fact "retrieves the source var"
  ^:hidden

  (declare -source-)
  (def ^Link -source- (link:create {} #'-source-))

  (bind-source (.alias -source-) #'std.lib.foundation/iobj? (.transform -source-))
  => std.lib.foundation/iobj?)

^{:refer std.lib.link/bind-resolve :added "3.0"}
(fact "binds a link or a series of links"
  ^:hidden

  (deflink -ipending0?- std.lib.foundation/iobj?)

  (deflink -ipending1?- -ipending0?-)

  (deflink -ipending2?- -ipending1?-)

  (binding [*bind-root* true]
    (bind-resolve -ipending2?-))
  (fn? -ipending2?-) => true
  (fn? -ipending1?-) => true
  (fn? -ipending0?-) => true)

^{:refer std.lib.link/bind-preempt :added "3.0"}
(fact "retrieves the source var if available"
  ^:hidden

  (deflink -ipending0?- std.lib.foundation/iobj?)

  (binding [*bind-root* true]
    (bind-preempt -ipending0?-))
  => std.lib.foundation/iobj?)

^{:refer std.lib.link/bind-verify :added "3.0"}
(fact "retrieves the source var if available"
  ^:hidden

  (deflink -ipending0?- std.lib.foundation/iobj?)

  (binding [*bind-root* true]
    (bind-verify -ipending0?-))
  => (exactly #'std.lib.link-test/-ipending0?-))

^{:refer std.lib.link/link:bind :added "3.0"}
(fact "automatically loads the var if possible"
  ^:hidden

  (deflink -ipending0?- std.lib.foundation/iobj?)

  (binding [*bind-root* true]
    (link:bind -ipending0?- :auto))
  => std.lib.foundation/iobj?

  (deflink -to-element0- std.object.element/to-element)

  (binding [*bind-root* true]
    (link:bind -to-element0- :resolve))
  => (throws)) ;; because std.object.element is not loaded?

^{:refer std.lib.link/link-invoke :added "3.0"}
(fact "invokes a link"
  ^:hidden

  (deflink -ipending0?- std.lib.foundation/iobj?)

  (deflink -ipending1?- -ipending0?-)

  (deflink -ipending2?- -ipending1?-)

  (-ipending2?- (promise)) => true)

^{:refer std.lib.link/intern-link :added "3.0"}
(fact "creates a registers a link"
  ^:hidden

  (intern-link '-ipending0?- {:ns 'std.lib.foundation :name 'iobj?})
  ;;#link{:source org.registry.iobj?, :bound true, :status :resolved, :synced false, :registered true}
  => link?)

^{:refer std.lib.link/link-form :added "3.0"}
(fact "creates the form in `link` macro"
  (link-form 'ns 'sym :resolve)
  => seq?)

^{:refer std.lib.link/deflink :added "3.0"}
(fact "creates a named link"
  (deflink -hello- std.lib.foundation/iobj?)
  => link?)

^{:refer std.lib.link/link :added "3.0"}
(fact "creates invokable aliases for early binding"
  (link {:ns 'std.lib.foundation :resolve :auto} iobj?)
  => vector?)
