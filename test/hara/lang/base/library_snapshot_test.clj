(ns hara.lang.base.library-snapshot-test
  (:require [hara.lang.base.book :as b]
            [hara.lang.base.book-entry :as e]
            [hara.lang.base.book-meta :as meta]
            [hara.lang.base.book-module :as m]
            [hara.lang.base.impl-entry :as entry]
            [hara.lang.base.library-snapshot :as snap]
            [hara.lang.base.library-snapshot-prep-test :as prep]
            [hara.lang.model.spec-lua :as lua]
            [std.lib.deps :as deps]
            [std.lib.env :as env])
  (:use code.test))

^{:refer hara.lang.base.library-snapshot/get-deps :added "4.0"}
(fact "gets a dependency chain"

  (snap/get-deps prep/+snap+ :lua)
  => #{:x}

  (snap/get-deps prep/+snap+ :x)
  => #{}

  (deps/deps-ordered prep/+snap+ [:lua.redis])
  => '(:x :lua :lua.redis)

  (deps/deps-ordered prep/+snap+ [:lua])
  => '(:x :lua))

^{:refer hara.lang.base.library-snapshot/snapshot-string :added "4.0"}
(fact "gets the snapshot string"

  (snap/snapshot-string prep/+snap+)
  => "#lib.snapshot [:lua :lua.redis :x]")

^{:refer hara.lang.base.library-snapshot/snapshot? :added "4.0"}
(fact "checks if object is a snapshot"

  (snap/snapshot? prep/+snap+)
  => true)

^{:refer hara.lang.base.library-snapshot/snapshot :added "4.0"}
(fact "creates a snapshot"

  (snap/snapshot {})
  => snap/snapshot?)

^{:refer hara.lang.base.library-snapshot/snapshot-reset :added "4.0"}
(fact "resets a snapshot to it's blanked modules"

  (-> (snap/snapshot-reset prep/+snap+)
      (get-in [:lua :book :modules]))
  => {}

  (-> (snap/snapshot-reset prep/+snap+ [:lua])
      keys)
  => '(:lua))

^{:refer hara.lang.base.library-snapshot/snapshot-merge :added "4.0"}
(fact "a rough merge of only the modules from the child to the parent"

  (snap/snapshot-merge nil prep/+snap+)
  => map?

  (snap/snapshot-merge prep/+snap+ nil)
  => map?

  (snap/snapshot-merge prep/+snap+ prep/+snap+)
  => map?)

^{:refer hara.lang.base.library-snapshot/get-book-raw :added "4.0"}
(fact "gets the raw book"

  (-> (snap/get-book-raw prep/+snap+ :lua)
      :modules
      keys)
  => '(L.core)

  (-> (snap/get-book-raw prep/+snap+ :lua.redis)
      :modules
      keys)
  => nil)

^{:refer hara.lang.base.library-snapshot/get-book :added "4.0"}
(fact "gets the merged book for a given language"

  (-> (snap/get-book prep/+snap+ :lua.redis)
      :modules
      keys
      set)
  => '#{L.core x.core})

^{:refer hara.lang.base.library-snapshot/add-book :added "4.0"}
(fact "adds a book to a snapshot"

  (-> (snap/add-book (snap/snapshot {})
                     prep/+book-x+)
      (keys))
  => '(:x))

^{:refer hara.lang.base.library-snapshot/set-module :added "4.0"}
(fact "sets a module in the snapshot"

  (-> (snap/set-module prep/+snap+
                       (m/book-module '{:lang :lua.redis
                                        :id L.redis
                                        :link {- L.redis
                                               u L.core}}))
      second
      (get-in [:lua.redis :book])
      (b/book-string))
  => "#book [:lua.redis] {L.redis {:code 0, :fragment 0}}")

^{:refer hara.lang.base.library-snapshot/delete-module :added "4.0"}
(fact "deletes a module in the snapshot"

  (-> (snap/delete-module prep/+snap+
                          :lua 'L.core)
      second
      (get-in [:lua :book])
      (b/book-string))
  => "#book [:lua] {}")

^{:refer hara.lang.base.library-snapshot/delete-modules :added "4.0"}
(fact  "deletes a bunch of modules in the snapshot"
  (-> (snap/delete-modules prep/+snap+ :lua ['L.core])
      second
      (get-in [:lua :book])
      (b/book-string))
  => "#book [:lua] {}")

^{:refer hara.lang.base.library-snapshot/list-modules :added "4.0"}
(fact "list modules for a snapshot"

  (set (snap/list-modules prep/+snap+ :lua))
  => '#{L.core x.core})

^{:refer hara.lang.base.library-snapshot/list-entries :added "4.0"}
(fact "lists entries for a snapshot"

  (set (snap/list-entries prep/+snap+ :lua))
  => '#{x.core/identity-fn L.core/identity-fn}

  (snap/list-entries prep/+snap+ :lua 'L.core)
  => '{:code (identity-fn), :fragment (add sub)}

  (snap/list-entries prep/+snap+ :x 'x.core :code)
  => '(identity-fn))

^{:refer hara.lang.base.library-snapshot/set-entry :added "4.0"}
(fact "stores raw entries and materializes them through the merged book view"

  (-> (snap/set-entry prep/+snap+
                      (entry/create-code-base
                       '(defn sub-fn
                          [a b]
                          (return (fn:> ((-/identity-fn -/sub) a b))))
                       {:lang :lua
                        :namespace 'L.core
                         :module 'L.core}
                        {}))
      second
      (snap/get-book :lua)
      (b/get-code-entry-view 'L.core/sub-fn)
      :form)
  => '(defn sub-fn [a b] (return (fn [] (return ((L.core/identity-fn (fn [x y] (return (- x y)))) a b))))))

^{:refer hara.lang.base.library-snapshot/set-entries :added "4.0"
  :setup [(def +snap-mixed+
            (-> prep/+snap+
                (snap/set-entries [(entry/create-fragment
                                    '(def$ G G)
                                    {:lang :lua
                                     :namespace (env/ns-sym)
                                     :module 'L.core})
                                   (entry/create-code-base
                                    '(defn sub-g
                                       [a]
                                       (return (- a -/G)))
                                    {:lang :lua
                                     :namespace (env/ns-sym)
                                     :module 'L.core}
                                    {})])
                second
                (snap/set-module (m/book-module '{:lang :lua.redis
                                                  :id L.redis
                                                  :link {- L.redis
                                                         u L.core}}))
                second))]}
(fact "materializes entries for the current language and preserves raw insertion order"

  (-> (snap/set-entries +snap-mixed+
                        [(entry/create-code-base
                          '(defn redis-g
                             []
                             (return u/G))
                          {:lang :lua.redis
                           :namespace (env/ns-sym)
                           :module 'L.redis}
                          {})])
      second
      (snap/get-book :lua.redis)
      (b/get-code-entry-view 'L.redis/redis-g)
      :form)
  => '(defn redis-g [] (return G))

  (let [[diff snapshot]
        (snap/set-entries prep/+snap+
                          [(entry/create-code-base
                            '(defn sub-g
                               [a]
                               (return (- a -/G)))
                            {:lang :lua
                             :namespace (env/ns-sym)
                             :module 'L.core}
                            {})])]
    [(seq diff)
     (snap/snapshot? snapshot)])
  => [true true])

^{:refer hara.lang.base.library-snapshot/delete-entry :added "4.0"}
(fact "deletes an entry from the snapshot"

  (-> prep/+snap+
      (snap/delete-entry {:lang :lua
                          :section :code
                          :module 'L.core
                          :id 'identity-fn})
      second
      (snap/delete-entry {:lang :lua
                          :section :code
                          :module 'L.core
                          :id 'identity-fn})
      first)
  => '([[:modules L.core :code identity-fn] nil]))

^{:refer hara.lang.base.library-snapshot/delete-entries :added "4.0"}
(fact "delete entries from the snapshot"

  (-> prep/+snap+
      (snap/delete-entries [{:lang :lua
                             :section :code
                             :module 'L.core
                             :id 'identity-fn}])
      second
      (get-in [:lua :book])
      (b/list-entries))
  => ())

^{:refer hara.lang.base.library-snapshot/install-check-merged :added "4.0"}
(fact "checks that the book is not merged (used to check mutate)"
  (snap/install-check-merged {:merged [1]}) => nil)

^{:refer hara.lang.base.library-snapshot/install-module-update :added "4.0"}
(fact "updates the book module"
  (snap/install-module-update (b/book {:lang :lua :meta {:a 1} :grammar {:a 1} :modules {}}) {:id :lua :code {}})
  => vector?)

^{:refer hara.lang.base.library-snapshot/install-module :added "4.0"}
(fact "adds an new module or update fields if exists"

  (snap/install-module prep/+snap+
                       :lua 'L.util
                       {})
  => vector?

  (snap/install-module prep/+snap+
                       :lua 'L.core
                       '{:import [["hello" :as hello]
                                  ["world" :as world]]})
  => vector?)

^{:refer hara.lang.base.library-snapshot/install-module-specialized :added "4.1"}
(fact "adds a specialized module clone to the snapshot"
  (let [[snapshot status module] (snap/install-module-specialized
                                  prep/+snap+
                                  :lua
                                  'L.core
                                  'L.core-specialized
                                  {:bindings {'L.core 'L.util}})]
    [status
     (get-in snapshot [:lua :book :modules 'L.core-specialized :id])
     (get-in module [:link '-])])
  => [:new 'L.core-specialized 'L.core-specialized])

^{:refer hara.lang.base.library-snapshot/install-book-update :added "4.0"}
(fact "updates the book grammar, meta and parent"
  (snap/install-book-update prep/+snap+ {:lang :lua :grammar {:a 1} :meta {:a 1} :parent :x})
  => vector?)

^{:refer hara.lang.base.library-snapshot/install-book :added "4.0"}
(fact "adds a new book or updates grammar if exists"

  (snap/install-book prep/+snap+
                     (b/book {:lang :hello
                              :meta (meta/book-meta {})
                              :grammar (assoc (:grammar prep/+book-x+)
                                              :tag :hello)}))
  => vector?

  (snap/install-book prep/+snap+
                     (b/book {:lang :lua.redis
                              :meta    (meta/book-meta {})
                              :grammar (assoc lua/+grammar+
                                               :tag :lua.redis)}))
  => vector?)

(comment
  (./import)
  (./create-tests))
