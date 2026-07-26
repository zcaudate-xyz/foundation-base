(ns tahto.core.library-test
  (:require [tahto.base.book :as b]
            [tahto.base.book-entry :as entry]
            [tahto.base.book-meta :as meta]
            [tahto.base.book-module :as module]
tahto/lang/library_test.clj:6:            [tahto.common.emit-common :as common]
tahto/lang/library_test.clj:7:            [tahto.common.emit-helper :as helper]
tahto/lang/library_test.clj:8:            [tahto.common.grammar :as grammar]
            [tahto.core.library :as lib]
            [tahto.core.library-snapshot :as snap]
            [tahto.core.library-snapshot-prep-test :as prep]
tahto/lang/library_test.clj:12:            [tahto.common.emit-prep-lua-test :as lua-prep]
tahto/lang/library_test.clj:13:            [tahto.common.util :as ut]
tahto/lang/library_test.clj:14:            [tahto.model.spec-lua :as lua]
            [std.lib.atom :as atom]
            [std.lib.deps :as deps]
            [std.lib.env :as env])
  (:use code.test))

(def +library+ (lib/library {:snapshot prep/+snap+}))

(def +contract-book+
  (b/book {:lang :lua
           :meta (:meta lua-prep/+book-empty+)
           :grammar (:grammar lua-prep/+book-empty+)
           :modules {'L.contract (module/book-module {:lang :lua
                                                      :id 'L.contract
                                                      :code {'foo (b/book-entry {:lang :lua
                                                                                 :module 'L.contract
                                                                                 :section :code
                                                                                 :id 'foo
                                                                                 :form-input '(defabstract foo [x])
                                                                                 :namespace 'L.contract})}})
                     'L.impl (module/book-module {:lang :lua
                                                 :id 'L.impl
                                                 :implements ['L.contract]
                                                 :code {'foo (b/book-entry {:lang :lua
                                                                            :module 'L.impl
                                                                            :section :code
                                                                            :id 'foo
                                                                            :form-input '(defn foo [x] x)
                                                                            :namespace 'L.impl})}})}}))

(def +contract-snapshot+
  (snap/snapshot {:lua {:id :lua
                        :book +contract-book+}}))

^{:refer tahto.core.library/wait-snapshot :added "4.0"}
(fact "gets the current waiting snapshot"

  (lib/wait-snapshot +library+)
  => snap/snapshot?

  (meta (lib/wait-snapshot +library+))
  => {:parent nil})

^{:refer tahto.core.library/wait-apply :added "4.0"}
(fact "get the library state when task queue is empty"

  (snap/snapshot? (lib/wait-apply +library+ identity))

  (lib/wait-apply +library+
                   snap/get-book :lua)
  => b/book?

  (lib/wait-apply +library+
                   deps/deps-ordered [:lua.redis])
  => '(:x :lua :lua.redis))

^{:refer tahto.core.library/wait-mutate! :added "4.0"}
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

^{:refer tahto.core.library/get-snapshot :added "4.0"}
(fact "gets the current snapshot for the library"

  (lib/get-snapshot +library+)
  => snap/snapshot?)

^{:refer tahto.core.library/snapshot-find-module :added "4.1"}
(fact "finds a module anywhere in the merged library snapshot"

  (-> (lib/snapshot-find-module prep/+snap+ 'L.core)
      first)
  => :lua

  (-> (lib/snapshot-find-module prep/+snap+ 'L.core)
      second)
  => module/book-module?

  (-> (lib/snapshot-find-module prep/+snap+ 'x.core)
      first)
  => :x

  (lib/snapshot-find-module prep/+snap+ 'missing.module)
  => nil)

^{:refer tahto.core.library/entry-arity :added "4.1"}
(fact "returns the arity of an entry from its input form"

  (lib/entry-arity {:form-input '(defn add-fn [a b] (+ a b))})
  => 2

  (lib/entry-arity {:form-input '(defn no-args [] x)})
  => 0

  (lib/entry-arity {:form-input '(do something)})
  => nil)

^{:refer tahto.core.library/entry-abstract? :added "4.1"}
(fact "checks if an entry was declared with `defabstract`"

  (lib/entry-abstract? {:form-input '(defabstract foo [x])})
  => true

  (lib/entry-abstract? {:form-input '(defn foo [x] x)})
  => false

  (lib/entry-abstract? {:form-input "not a seq"})
  => false)

^{:refer tahto.core.library/validate-module-implements :added "4.1"}
(fact "checks that a module satisfies all declared abstract contracts"

  (lib/validate-module-implements +contract-snapshot+ :lua 'L.impl)
  => true

  (lib/validate-module-implements (snap/snapshot
                                   {:lua {:id :lua
                                          :book (assoc-in +contract-book+
                                                          [:modules 'L.impl :code]
                                                          {})}})
                                  :lua
                                  'L.impl)
  => (throws-info {:missing '[foo]})

  (lib/validate-module-implements (snap/snapshot
                                   {:lua {:id :lua
                                          :book (assoc-in +contract-book+
                                                          [:modules 'L.impl :code 'foo :form-input]
                                                          '(defn foo [x y] x))}})
                                  :lua
                                  'L.impl)
  => (throws-info {:mismatched '[{:id foo
                                  :expected 1
                                  :actual 2}]}))

^{:refer tahto.core.library/get-book :added "4.0"}
(fact "gets a book from library"

  (lib/get-book +library+ :x)
  => b/book?)

^{:refer tahto.core.library/get-book-raw :added "4.0"}
(fact "gets the raw book, without merge"

  (b/list-entries (lib/get-book-raw +library+ :lua.redis))
  => empty?

  (b/list-entries (lib/get-book +library+ :lua.redis))
  => coll?)

^{:refer tahto.core.library/get-module :added "4.0"}
(fact "gets a module from library"

  (lib/get-module +library+ :x 'x.core)
  => module/book-module?)

^{:refer tahto.core.library/get-entry :added "4.0"}
(fact "gets an entry from library"

  (lib/get-entry +library+ '{:lang :lua
                              :module L.core
                              :section :fragment
                              :id sub})
  => entry/book-entry?)

^{:refer tahto.core.library/add-book! :added "4.0"}
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

^{:refer tahto.core.library/delete-book! :added "4.0"}
(fact "deletes a book"
  (lib/delete-book! +library+ :js) => nil)

^{:refer tahto.core.library/reset-all! :added "4.0"}
(fact "resets the library"

  (lib/reset-all! +library+
                  (lib/reset-all! +library+))
  => snap/snapshot?)

^{:refer tahto.core.library/list-modules :added "4.0"}
(fact "lists all modules"

  (lib/list-modules +library+ :lua)
  => (contains ['L.core 'x.core]
               :in-any-order :gaps-ok))

^{:refer tahto.core.library/list-entries :added "4.0"}
(fact "lists entries"

  (lib/list-entries +library+ :lua)
  => '(L.core/identity-fn)

  (lib/list-entries +library+ :lua 'L.core)
  => '{:code (identity-fn), :fragment (add sub)})

^{:refer tahto.core.library/add-module! :added "4.0"}
(fact "adds a module to the library"

  (lib/add-module! +library+ (module/book-module '{:lang :lua.redis
                                                   :id L.redis.hello
                                                   :link {r L.redis
                                                          u L.core}}))
  => coll?

  (lib/delete-module! +library+ :lua.redis 'L.redis.hello )
  => coll?)

^{:refer tahto.core.library/delete-module! :added "4.0"}
(fact "deletes a module from the library"
  (lib/delete-module! +library+ :lua.redis 'L.redis.hello) => coll?)

^{:refer tahto.core.library/delete-modules! :added "4.0"}
(fact  "deletes a bunch of modules from the library"
  (lib/delete-modules! +library+ :lua.redis ['L.redis.hello]) => coll?)

^{:refer tahto.core.library/library-string :added "4.0"}
(fact "returns the library string"

  (lib/library-string +library+)
  => string?)

^{:refer tahto.core.library/library? :added "4.0"}
(fact "checks if object is a library"

  (lib/library? +library+)
  => true)

^{:refer tahto.core.library/library:create :added "4.0"}
(fact "creates a new library"

  (lib/library:create {})
  => lib/library?)

^{:refer tahto.core.library/library :added "4.0"}
(fact "creates and start a new library"
  (lib/library {}) => lib/library?)

^{:refer tahto.core.library/add-entry! :added "4.0"}
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

^{:refer tahto.core.library/add-entry-single! :added "4.0"
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

^{:refer tahto.core.library/delete-entry! :added "4.0"}
(fact "deletes an entry from the library"
  (lib/delete-entry! +library+ {:lang :lua :module 'L.core :id 'add-fn}) => coll?)

^{:refer tahto.core.library/install-module! :added "4.0"
  :setup [(lib/delete-module! +library+  :lua 'L.util)]}
(fact "installs a module to library"

  (lib/install-module! +library+
                       :lua 'L.util
                       {})
  => coll?)

^{:refer tahto.core.library/install-module-specialized! :added "4.1"}
(fact "installs a specialized module clone into the library"
  :setup [(def +specialized-lib+
            (lib/library {:snapshot prep/+snap+}))
          (lib/install-module-specialized! +specialized-lib+
                                           :lua
                                           'L.core
                                           'L.core.specialized
                                           {})]

  (lib/get-module +specialized-lib+ :lua 'L.core.specialized)
  => module/book-module?

  (-> (lib/get-module +specialized-lib+ :lua 'L.core.specialized)
      :id)
  => 'L.core.specialized)

^{:refer tahto.core.library/install-book! :added "4.0"
  :setup [(lib/delete-book! +library+ :lua.redis)]}
(fact "installs a book to library"

  (lib/install-book! +library+ prep/+book-lua-redis-empty+)
  => coll?

  (:parent (lib/get-book-raw +library+ :lua.redis))
  => :lua

  (:parent prep/+book-lua-redis-empty+)
  => :lua)

^{:refer tahto.core.library/purge-book! :added "4.0"}
(fact "clears all modules from book"
  (lib/purge-book! +library+ :lua) => coll?)

(comment
  (comment
  ^{:refer tahto.core.library/create-dispatch-handler :added "4.0"}
  (fact "the actual dispatch handler")

  ^{:refer tahto.core.library/create-dispatch :added "4.0"}
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
tahto/lang/library_test.clj:385:                          tahto.common.emit-prep-lua-test/+book-min+)]))

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