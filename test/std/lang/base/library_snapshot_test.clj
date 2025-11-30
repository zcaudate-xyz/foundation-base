(ns std.lang.base.library-snapshot-test
  (:use code.test)
  (:require [std.lang.base.library-snapshot :as snap]
            [std.lang.base.library-snapshot-prep-test :as prep]
            [std.lang.base.impl-entry :as entry]
            [std.lang.model.spec-lua :as lua]
            [std.lang.base.book-module :as m]
            [std.lang.base.book-entry :as e]
            [std.lang.base.book :as b]
            [std.lang.base.book-meta :as meta]
            [std.lib :as h]))

^{:refer std.lang.base.library-snapshot/get-deps :added "4.0"}
(fact "gets a dependency chain"
  ^:hidden
  
  (snap/get-deps prep/+snap+ :lua)
  => #{:x}

  (snap/get-deps prep/+snap+ :x)
  => #{}
  
  (h/deps:ordered prep/+snap+ [:redis])
  => '(:x :lua :redis)

  (h/deps:ordered prep/+snap+ [:lua])
  => '(:x :lua))

^{:refer std.lang.base.library-snapshot/snapshot-string :added "4.0"}
(fact "gets the snapshot string"
  ^:hidden
  
  (snap/snapshot-string prep/+snap+)
  => "#lib.snapshot [:lua :redis :x]")

^{:refer std.lang.base.library-snapshot/snapshot? :added "4.0"}
(fact "checks if object is a snapshot"
  ^:hidden
  
  (snap/snapshot? prep/+snap+)
  => true)

^{:refer std.lang.base.library-snapshot/snapshot :added "4.0"}
(fact "creates a snapshot"
  ^:hidden
  
  (snap/snapshot {})
  => snap/snapshot?)

^{:refer std.lang.base.library-snapshot/snapshot-reset :added "4.0"}
(fact "resets a snapshot to it's blanked modules"
  ^:hidden
  
  (-> (snap/snapshot-reset prep/+snap+)
      (get-in [:lua :book :modules]))
  => {}

  (-> (snap/snapshot-reset prep/+snap+ [:lua])
      keys)
  => '(:lua))

^{:refer std.lang.base.library-snapshot/snapshot-merge :added "4.0"}
(fact "a rough merge of only the modules from the child to the parent"
  ^:hidden
  
  (snap/snapshot-merge nil prep/+snap+)
  => map?
  
  (snap/snapshot-merge prep/+snap+ nil)
  => map?
  
  (snap/snapshot-merge prep/+snap+ prep/+snap+)
  => map?)

^{:refer std.lang.base.library-snapshot/get-book-raw :added "4.0"}
(fact "gets the raw book"
  ^:hidden
  
  (-> (snap/get-book-raw prep/+snap+ :lua)
      :modules
      keys)
  => '(L.core)
  
  (-> (snap/get-book-raw prep/+snap+ :redis)
      :modules
      keys)
  => nil)

^{:refer std.lang.base.library-snapshot/get-book :added "4.0"}
(fact "gets the merged book for a given language"
  ^:hidden
  
  (-> (snap/get-book prep/+snap+ :redis)
      :modules
      keys
      set)
  => '#{L.core x.core})

^{:refer std.lang.base.library-snapshot/add-book :added "4.0"}
(fact "adds a book to a snapshot"
  ^:hidden
  
  (-> (snap/add-book (snap/snapshot {})
                     prep/+book-x+)
      (keys))
  => '(:x))

^{:refer std.lang.base.library-snapshot/set-module :added "4.0"}
(fact "sets a module in the snapshot"
  ^:hidden
  
  (-> (snap/set-module prep/+snap+
                       (m/book-module '{:lang :redis
                                        :id L.redis
                                        :link {- L.redis
                                               u L.core}}))
      second
      (get-in [:redis :book])
      (b/book-string))
  => "#book [:redis] {L.redis {:code 0, :fragment 0}}")

^{:refer std.lang.base.library-snapshot/delete-module :added "4.0"}
(fact "deletes a module in the snapshot"
  ^:hidden
  
  (-> (snap/delete-module prep/+snap+
                          :lua 'L.core)
      second
      (get-in [:lua :book])
      (b/book-string))
  => "#book [:lua] {}")

^{:refer std.lang.base.library-snapshot/delete-modules :added "4.0"}
(fact  "deletes a bunch of modules in the snapshot"
  (-> (snap/delete-modules prep/+snap+ :lua ['L.core])
      second
      (get-in [:lua :book])
      (b/book-string))
  => "#book [:lua] {}")

^{:refer std.lang.base.library-snapshot/list-modules :added "4.0"}
(fact "list modules for a snapshot"
  ^:hidden
  
  (set (snap/list-modules prep/+snap+ :lua))
  => '#{L.core x.core})

^{:refer std.lang.base.library-snapshot/list-entries :added "4.0"}
(fact "lists entries for a snapshot"
  ^:hidden
  
  (set (snap/list-entries prep/+snap+ :lua))
  => '#{x.core/identity-fn L.core/identity-fn}

  (snap/list-entries prep/+snap+ :lua 'L.core)
  => '{:code (identity-fn), :fragment (add sub)}

  (snap/list-entries prep/+snap+ :x 'x.core :code)
  => '(identity-fn))

^{:refer std.lang.base.library-snapshot/set-entry :added "4.0"}
(fact "sets an entry in the snapshot"
  ^:hidden
  
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
      (get-in [:lua :book :modules 'L.core :code 'sub-fn :form]))
  => '(defn sub-fn [a b] (return (fn [] (return ((L.core/identity-fn (fn [x y] (return (- x y)))) a b))))))

^{:refer std.lang.base.library-snapshot/set-entries :added "4.0"
  :setup [(def +snap-mixed+
            (-> prep/+snap+
                (snap/set-entries [(entry/create-fragment
                                    '(def$ G G)
                                    {:lang :lua
                                     :namespace (h/ns-sym)
                                     :module 'L.core})
                                   (entry/create-code-base
                                    '(defn sub-g
                                       [a]
                                       (return (- a -/G)))
                                    {:lang :lua
                                     :namespace (h/ns-sym)
                                     :module 'L.core}
                                    {})])
                second
                (snap/set-module (m/book-module '{:lang :redis
                                                  :id L.redis
                                                  :link {- L.redis
                                                         u L.core}}))
                second))]}
(fact "sets an entry in the snapshot"
  ^:hidden
  
  (-> (snap/set-entries +snap-mixed+
                        [(entry/create-code-base
                          '(defn redis-g
                             []
                             (return u/G))
                          {:lang :redis
                           :namespace (h/ns-sym)
                           :module 'L.redis}
                          {})])
      second
      (get-in [:redis :book :modules 'L.redis :code 'redis-g :form]))
  => '(defn redis-g [] (return G))

  ;;
  ;; Will fail if the order of insert is wrong
  ;;
  (snap/set-entries prep/+snap+
                    [(entry/create-code-base
                      '(defn sub-g
                         [a]
                         (return (- a -/G)))
                      {:lang :lua
                       :namespace (h/ns-sym)
                       :module 'L.core}
                      {})])
  => (throws))

^{:refer std.lang.base.library-snapshot/delete-entry :added "4.0"}
(fact "deletes an entry from the snapshot"
  ^:hidden
  
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

^{:refer std.lang.base.library-snapshot/delete-entries :added "4.0"}
(fact "delete entries from the snapshot"
  ^:hidden
  
  (-> prep/+snap+
      (snap/delete-entries [{:lang :lua
                             :section :code
                             :module 'L.core
                             :id 'identity-fn}])
      second
      (get-in [:lua :book])
      (b/list-entries))
  => ())

^{:refer std.lang.base.library-snapshot/install-check-merged :added "4.0"}
(fact "checks that the book is not merged (used to check mutate)"
  (snap/install-check-merged {:merged [1]}) => nil)

^{:refer std.lang.base.library-snapshot/install-module-update :added "4.0"}
(fact "updates the book module"
  (snap/install-module-update (b/book {:lang :lua :meta {:a 1} :grammar {:a 1} :modules {}}) {:id :lua :code {}})
  => vector?)

^{:refer std.lang.base.library-snapshot/install-module :added "4.0"}
(fact "adds an new module or update fields if exists"
  ^:hidden
  
  (snap/install-module prep/+snap+
                       :lua 'L.util
                       {})
  => vector?
  
  (snap/install-module prep/+snap+
                       :lua 'L.core
                       '{:import [["hello" :as hello]
                                  ["world" :as world]]})
  => vector?)

^{:refer std.lang.base.library-snapshot/install-book-update :added "4.0"}
(fact "updates the book grammar, meta and parent"
  (snap/install-book-update prep/+snap+ {:lang :lua :grammar {:a 1} :meta {:a 1} :parent :x})
  => vector?)

^{:refer std.lang.base.library-snapshot/install-book :added "4.0"}
(fact "adds a new book or updates grammar if exists"
  ^:hidden
  
  (snap/install-book prep/+snap+
                     (b/book {:lang :hello
                              :meta (meta/book-meta {})
                              :grammar (assoc (:grammar prep/+book-x+)
                                              :tag :hello)}))
  => vector?

  (snap/install-book prep/+snap+
                     (b/book {:lang :redis
                              :meta    (meta/book-meta {})
                              :grammar (assoc lua/+grammar+
                                              :tag :redis)}))
  => vector?)

(comment
  (./import)
  (./create-tests))
