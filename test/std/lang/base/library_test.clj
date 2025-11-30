(ns std.lang.base.library-test
  (:use code.test)
  (:require [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot-prep-test :as prep]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.book :as b]
            [std.lang.base.book-meta :as meta]
            [std.lang.base.book-module :as module]
            [std.lang.base.book-entry :as entry]
            [std.lang.base.grammar :as grammar]
            [std.lang.model.spec-lua :as lua]
            [std.lang.base.emit-common :as common]
	    [std.lang.base.emit-helper :as helper]
            [std.lang.base.util :as ut]
            [std.lib :as h]))

(def +library+ (lib/library {:snapshot prep/+snap+}))

^{:refer std.lang.base.library/wait-snapshot :added "4.0"}
(fact "gets the current waiting snapshot"

  (lib/wait-snapshot +library+)
  => snap/snapshot?

  (meta (lib/wait-snapshot +library+))
  => {:parent nil})

^{:refer std.lang.base.library/wait-apply :added "4.0"}
(fact "get the library state when task queue is empty"
  ^:hidden
  
  (snap/snapshot? (lib/wait-apply +library+ identity))
  
  (lib/wait-apply +library+
                   snap/get-book :lua)
  => b/book?

  (lib/wait-apply +library+
                   h/deps:ordered [:redis])
  => '(:x :lua :redis))

^{:refer std.lang.base.library/wait-mutate! :added "4.0"}
(fact "mutates library once task queue is empty"
  ^:hidden
  
  (-> +library+
      (doto (lib/wait-mutate! snap/delete-module :x 'x.core))
      (lib/wait-apply snap/get-book :x)
      (b/list-entries))
  => ()

  (do (lib/add-module! +library+ prep/+x-module+)
      (assert (= (keys (get-in (lib/get-snapshot +library+)
                               [:x :book :modules]))
                 '(x.core)))))

^{:refer std.lang.base.library/get-snapshot :added "4.0"}
(fact "gets the current snapshot for the library"
  ^:hidden
  
  (lib/get-snapshot +library+)
  => snap/snapshot?)

^{:refer std.lang.base.library/get-book :added "4.0"}
(fact "gets a book from library"
  ^:hidden
  
  (lib/get-book +library+ :x)
  => b/book?)

^{:refer std.lang.base.library/get-book-raw :added "4.0"}
(fact "gets the raw book, without merge"

  (b/list-entries (lib/get-book-raw +library+ :redis))
  => empty?
  
  (b/list-entries (lib/get-book +library+ :redis))
  => coll?)

^{:refer std.lang.base.library/get-module :added "4.0"}
(fact "gets a module from library"
  ^:hidden

  (lib/get-module +library+ :x 'x.core)
  => module/book-module?)

^{:refer std.lang.base.library/get-entry :added "4.0"}
(fact "gets an entry from library"
  ^:hidden
  
  (lib/get-entry +library+ '{:lang :lua
                              :module L.core
                              :section :fragment
                              :id sub})
  => entry/book-entry?)

^{:refer std.lang.base.library/add-book! :added "4.0"}
(fact "adds a book to the library"
  ^:hidden
  
  (lib/add-book! +library+
                 (b/book (b/book {:lang :js
                                  :parent  :x
                                  :meta    (meta/book-meta {})
                                  :grammar (grammar/grammar :js
                                             (grammar/to-reserved (grammar/build))
                                             helper/+default+)})))

  (lib/wait-apply +library+ h/deps:ordered [:js])
  => '(:x :js)

  (lib/delete-book! +library+ :js)
  => (any nil? map?))

^{:refer std.lang.base.library/delete-book! :added "4.0"}
(fact "deletes a book"
  (lib/delete-book! +library+ :js) => nil)

^{:refer std.lang.base.library/reset-all! :added "4.0"}
(fact "resets the library"
  ^:hidden
  
  (lib/reset-all! +library+
                  (lib/reset-all! +library+))
  => snap/snapshot?)

^{:refer std.lang.base.library/list-modules :added "4.0"}
(fact "lists all modules"
  ^:hidden
  
  (lib/list-modules +library+ :lua)
  => (contains ['L.core 'x.core]
               :in-any-order :gaps-ok))

^{:refer std.lang.base.library/list-entries :added "4.0"}
(fact "lists entries"
  ^:hidden
  
  (lib/list-entries +library+ :lua)
  => '(L.core/identity-fn)

  (lib/list-entries +library+ :lua 'L.core)
  => '{:code (identity-fn), :fragment (add sub)})

^{:refer std.lang.base.library/add-module! :added "4.0"}
(fact "adds a module to the library"

  (lib/add-module! +library+ (module/book-module '{:lang :redis
                                                   :id L.redis.hello
                                                   :link {r L.redis
                                                          u L.core}}))
  => coll?
  
  (lib/delete-module! +library+ :redis 'L.redis.hello )
  => coll?)

^{:refer std.lang.base.library/delete-module! :added "4.0"}
(fact "deletes a module from the library"
  (lib/delete-module! +library+ :redis 'L.redis.hello) => coll?)

^{:refer std.lang.base.library/delete-modules! :added "4.0"}
(fact  "deletes a bunch of modules from the library"
  (lib/delete-modules! +library+ :redis ['L.redis.hello]) => coll?)

^{:refer std.lang.base.library/library-string :added "4.0"}
(fact "returns the library string"
  ^:hidden
  
  (lib/library-string +library+)
  => string?)

^{:refer std.lang.base.library/library? :added "4.0"}
(fact "checks if object is a library"
  ^:hidden
  
  (lib/library? +library+)
  => true)

^{:refer std.lang.base.library/library:create :added "4.0"}
(fact "creates a new library"
  ^:hidden
  
  (lib/library:create {})
  => lib/library?)

^{:refer std.lang.base.library/library :added "4.0"}
(fact "creates and start a new library"
  (lib/library {}) => lib/library?)

^{:refer std.lang.base.library/add-entry! :added "4.0"}
(fact "adds the entry with the bulk dispatcher"
  ^:hidden

  (comment
    (lib/delete-entry! +library+ {:lang :lua
                                        :section :code
                                        :module 'L.core
                                        :id 'add-fn})
    (-> (lib/add-entry! +library+
                        (b/book-entry {:lang :lua
                                       :section :code
                                       :namespace (h/ns-sym)
                                       :module 'L.core
                                       :id 'add-fn
                                       :form-input '(defn add-fn [x y] (return (+ x y)))
                                       :deps #{}}))
        first
        deref)))

^{:refer std.lang.base.library/add-entry-single! :added "4.0"
  :setup [(lib/delete-entry! +library+ {:lang :lua
                                        :section :code
                                        :module 'L.core
                                        :id 'add-fn})]}
(fact "adds an entry synchronously"
  ^:hidden
  
  (lib/add-entry-single!
   +library+
   (b/book-entry {:lang :lua
                  :section :code
                  :namespace (h/ns-sym)
                  :module 'L.core
                  :id 'add-fn
                  :form-input '(defn add-fn [x y] (return (+ x y)))
                  :deps #{}}))
  => coll?)

^{:refer std.lang.base.library/delete-entry! :added "4.0"}
(fact "deletes an entry from the library"
  (lib/delete-entry! +library+ {:lang :lua :module 'L.core :id 'add-fn}) => coll?)

^{:refer std.lang.base.library/install-module! :added "4.0"
  :setup [(lib/delete-module! +library+  :lua 'L.util)]}
(fact "installs a module to library"
  ^:hidden
  
  (lib/install-module! +library+
                       :lua 'L.util
                       {})
  => coll?)

^{:refer std.lang.base.library/install-book! :added "4.0"
  :setup [(lib/delete-book! +library+ :redis)]}
(fact "installs a book to library"
  ^:hidden
  
  (lib/install-book! +library+ prep/+book-redis-empty+)
  => coll?

  (:parent (lib/get-book-raw +library+ :redis))
  => :lua
  
  (:parent prep/+book-redis-empty+)
  => :lua)

^{:refer std.lang.base.library/purge-book! :added "4.0"}
(fact "clears all modules from book"
  (lib/purge-book! +library+ :lua) => coll?)

(comment
  (comment
  ^{:refer std.lang.base.library/create-dispatch-handler :added "4.0"}
  (fact "the actual dispatch handler")

  ^{:refer std.lang.base.library/create-dispatch :added "4.0"}
  (fact "creates the dispatch for adding entries in bulk"
    ^:hidden

    (comment
      
      (lib/create-dispatch (atom (snap/snapshot {}))
                           (atom {}))
      => map?)))
  

  (./import
   )
  (./create-tests)
  (snap/get-book (get-snapshot +lib+)
                 :lua
                 )
  (def +lib+
    (library {}))
  
  (h/swap-return! (:instance +lib+)
    (fn [snapshot]
      [nil (snap/add-book snapshot
                          std.lang.base.emit-prep-lua-test/+book-min+)]))
  
  ((:dispatch +lib+) (entry/create-fragment
                      '(def$ G G)
                      {:lang :lua
                       :namespace 'L.core
                       :module 'L.core}))

  (h/swap-return! (:instance +lib+)
    (fn [snapshot]
      (snap/set-entries snapshot [(entry/create-fragment
                                    '(def$ G G)
                                    {:lang :lua
                                     :namespace 'L.core
                                     :module 'L.core})])))
  
  
  
  (do (dotimes [i 100]
        (set-entry +lib+ (entry/create-fragment
                          '(def$ G G)
                          {:lang :lua
                           :namespace 'L.core
                           :module 'L.core})))
      (-> (set-entry +lib+ (entry/create-fragment
                            '(def$ G G)
                            {:lang :lua
                             :namespace 'L.core
                             :module 'L.core}))
          first
          ))
  
  
  
  )
