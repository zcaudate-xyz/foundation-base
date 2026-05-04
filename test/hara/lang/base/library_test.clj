(ns hara.lang.base.library-test
  (:require [hara.lang.base.book :as b]
            [hara.lang.base.book-entry :as entry]
            [hara.lang.base.book-meta :as meta]
            [hara.lang.base.book-module :as module]
            [hara.common.emit-common :as common]
            [hara.common.emit-helper :as helper]
            [hara.common.grammar :as grammar]
            [hara.lang.base.library :as lib]
            [hara.lang.base.library-snapshot :as snap]
            [hara.lang.base.library-snapshot-prep-test :as prep]
            [hara.common.util :as ut]
            [hara.model.spec-lua :as lua]
            [std.lib.atom :as atom]
            [std.lib.deps :as deps]
            [std.lib.env :as env])
  (:use code.test))

(def +library+ (lib/library {:snapshot prep/+snap+}))

^{:refer hara.lang.base.library/wait-snapshot :added "4.0"}
(fact "gets the current waiting snapshot"

  (lib/wait-snapshot +library+)
  => snap/snapshot?

  (meta (lib/wait-snapshot +library+))
  => {:parent nil})

^{:refer hara.lang.base.library/wait-apply :added "4.0"}
(fact "get the library state when task queue is empty"

  (snap/snapshot? (lib/wait-apply +library+ identity))

  (lib/wait-apply +library+
                   snap/get-book :lua)
  => b/book?

  (lib/wait-apply +library+
                   deps/deps-ordered [:lua.redis])
  => '(:x :lua :lua.redis))

^{:refer hara.lang.base.library/wait-mutate! :added "4.0"}
(fact "mutates library once task queue is empty"

  (-> +library+
      (doto (lib/wait-mutate! snap/delete-module :x 'x.core))
      (lib/wait-apply snap/get-book :x)
      (b/list-entries))
  => ()

  (do (lib/add-module! +library+ prep/+x-module+)
      (assert (= (keys (get-in (lib/get-snapshot +library+)
                               [:x :book :modules]))
                 '(x.core)))))

^{:refer hara.lang.base.library/get-snapshot :added "4.0"}
(fact "gets the current snapshot for the library"

  (lib/get-snapshot +library+)
  => snap/snapshot?)

^{:refer hara.lang.base.library/get-book :added "4.0"}
(fact "gets a book from library"

  (lib/get-book +library+ :x)
  => b/book?)

^{:refer hara.lang.base.library/get-book-raw :added "4.0"}
(fact "gets the raw book, without merge"

  (b/list-entries (lib/get-book-raw +library+ :lua.redis))
  => empty?

  (b/list-entries (lib/get-book +library+ :lua.redis))
  => coll?)

^{:refer hara.lang.base.library/get-module :added "4.0"}
(fact "gets a module from library"

  (lib/get-module +library+ :x 'x.core)
  => module/book-module?)

^{:refer hara.lang.base.library/get-entry :added "4.0"}
(fact "gets an entry from library"

  (lib/get-entry +library+ '{:lang :lua
                              :module L.core
                              :section :fragment
                              :id sub})
  => entry/book-entry?)

^{:refer hara.lang.base.library/add-book! :added "4.0"}
(fact "adds a book to the library"

  (lib/add-book! +library+
                 (b/book (b/book {:lang :js
                                  :parent  :x
                                  :meta    (meta/book-meta {})
                                  :grammar (grammar/grammar :js
                                             (grammar/to-reserved (grammar/build))
                                             helper/+default+)})))

  (lib/wait-apply +library+ deps/deps-ordered [:js])
  => '(:x :js)

  (lib/delete-book! +library+ :js)
  => (any nil? map?))

^{:refer hara.lang.base.library/delete-book! :added "4.0"}
(fact "deletes a book"
  (lib/delete-book! +library+ :js) => nil)

^{:refer hara.lang.base.library/reset-all! :added "4.0"}
(fact "resets the library"

  (lib/reset-all! +library+
                  (lib/reset-all! +library+))
  => snap/snapshot?)

^{:refer hara.lang.base.library/list-modules :added "4.0"}
(fact "lists all modules"

  (lib/list-modules +library+ :lua)
  => (contains ['L.core 'x.core]
               :in-any-order :gaps-ok))

^{:refer hara.lang.base.library/list-entries :added "4.0"}
(fact "lists entries"

  (lib/list-entries +library+ :lua)
  => '(L.core/identity-fn)

  (lib/list-entries +library+ :lua 'L.core)
  => '{:code (identity-fn), :fragment (add sub)})

^{:refer hara.lang.base.library/add-module! :added "4.0"}
(fact "adds a module to the library"

  (lib/add-module! +library+ (module/book-module '{:lang :lua.redis
                                                   :id L.redis.hello
                                                   :link {r L.redis
                                                          u L.core}}))
  => coll?

  (lib/delete-module! +library+ :lua.redis 'L.redis.hello )
  => coll?)

^{:refer hara.lang.base.library/delete-module! :added "4.0"}
(fact "deletes a module from the library"
  (lib/delete-module! +library+ :lua.redis 'L.redis.hello) => coll?)

^{:refer hara.lang.base.library/delete-modules! :added "4.0"}
(fact  "deletes a bunch of modules from the library"
  (lib/delete-modules! +library+ :lua.redis ['L.redis.hello]) => coll?)

^{:refer hara.lang.base.library/library-string :added "4.0"}
(fact "returns the library string"

  (lib/library-string +library+)
  => string?)

^{:refer hara.lang.base.library/library? :added "4.0"}
(fact "checks if object is a library"

  (lib/library? +library+)
  => true)

^{:refer hara.lang.base.library/library:create :added "4.0"}
(fact "creates a new library"

  (lib/library:create {})
  => lib/library?)

^{:refer hara.lang.base.library/library :added "4.0"}
(fact "creates and start a new library"
  (lib/library {}) => lib/library?)

^{:refer hara.lang.base.library/add-entry! :added "4.0"}
(fact "adds the entry with the bulk dispatcher"

  (comment
    (lib/delete-entry! +library+ {:lang :lua
                                        :section :code
                                        :module 'L.core
                                        :id 'add-fn})
    (-> (lib/add-entry! +library+
                        (b/book-entry {:lang :lua
                                       :section :code
                                       :namespace (env/ns-sym)
                                       :module 'L.core
                                       :id 'add-fn
                                       :form-input '(defn add-fn [x y] (return (+ x y)))
                                       :deps #{}}))
        first
        deref)))

^{:refer hara.lang.base.library/add-entry-single! :added "4.0"
  :setup [(lib/delete-entry! +library+ {:lang :lua
                                        :section :code
                                        :module 'L.core
                                        :id 'add-fn})]}
(fact "adds an entry synchronously"

  (lib/add-entry-single!
   +library+
   (b/book-entry {:lang :lua
                  :section :code
                  :namespace (env/ns-sym)
                  :module 'L.core
                  :id 'add-fn
                  :form-input '(defn add-fn [x y] (return (+ x y)))
                  :deps #{}}))
  => coll?)

^{:refer hara.lang.base.library/delete-entry! :added "4.0"}
(fact "deletes an entry from the library"
  (lib/delete-entry! +library+ {:lang :lua :module 'L.core :id 'add-fn}) => coll?)

^{:refer hara.lang.base.library/install-module! :added "4.0"
  :setup [(lib/delete-module! +library+  :lua 'L.util)]}
(fact "installs a module to library"

  (lib/install-module! +library+
                       :lua 'L.util
                       {})
  => coll?)

^{:refer hara.lang.base.library/install-book! :added "4.0"
  :setup [(lib/delete-book! +library+ :lua.redis)]}
(fact "installs a book to library"

  (lib/install-book! +library+ prep/+book-lua-redis-empty+)
  => coll?

  (:parent (lib/get-book-raw +library+ :lua.redis))
  => :lua

  (:parent prep/+book-lua-redis-empty+)
  => :lua)

^{:refer hara.lang.base.library/purge-book! :added "4.0"}
(fact "clears all modules from book"
  (lib/purge-book! +library+ :lua) => coll?)

(comment
  (comment
  ^{:refer hara.lang.base.library/create-dispatch-handler :added "4.0"}
  (fact "the actual dispatch handler")

  ^{:refer hara.lang.base.library/create-dispatch :added "4.0"}
  (fact "creates the dispatch for adding entries in bulk"

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

  (atom/swap-return! (:instance +lib+)
    (fn [snapshot]
      [nil (snap/add-book snapshot
                          hara.common.emit-prep-lua-test/+book-min+)]))

  ((:dispatch +lib+) (entry/create-fragment
                      '(def$ G G)
                      {:lang :lua
                       :namespace 'L.core
                       :module 'L.core}))

  (atom/swap-return! (:instance +lib+)
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
