(ns std.lib.deps-test
  (:use code.test)
  (:require [std.lib.deps :refer :all]
            [std.protocol.deps :as protocol.deps]))

(defrecord MapContext []
  protocol.deps/IDeps
  (-get-entry [m id] (get m id))
  (-get-deps  [m id] (get m id))
  (-list-entries  [m] (keys m))
  protocol.deps/IDepsCompile
  (-step-construct   [_ acc id] (conj acc id))
  (-init-construct   [_] #{})
  protocol.deps/IDepsTeardown
  (-step-deconstruct [_ acc id] (disj acc id))
  protocol.deps/IDepsMutate
  (-add-entry        [m id entry deps] (assoc m id (set deps)))
  (-remove-entry     [m id] (dissoc m id))
  (-refresh-entry    [m id] (assoc m id #{})))

(defn context [m]
  (map->MapContext m))

(fact:global
 {:component
  {|ctx| {:create (context {:a #{:b}
                            :b #{:c}
                            :c #{}})}}})

^{:refer std.lib.deps/deps-map :added "3.0"
  :use [|ctx|]}
(fact "creates a map of deps"
  ^:hidden
  (deps-map |ctx| [:a :b])
  => {:a #{:b}, :b #{:c}})

^{:refer std.lib.deps/deps-resolve :added "3.0"
  :use [|ctx|]}
(fact "resolves all dependencies"
  ^:hidden
  (deps-resolve |ctx| [:a])
  => {:all #{:c :b :a},
      :graph {:a #{:b}, :b #{:c}, :c #{}}}

  (deps-resolve |ctx| [:c])
  => {:all #{:c}, :graph {:c #{}}})

^{:refer std.lib.deps/deps-ordered :added "3.0"
  :use [|ctx|]}
(fact "orders dependencies "

  (deps-ordered |ctx|)
  => '(:c :b :a))

^{:refer std.lib.deps/construct :added "3.0"
  :use [|ctx|]}
(fact "builds an object from context"

  (construct |ctx|)
  => #{:c :b :a})

^{:refer std.lib.deps/deconstruct :added "3.0"
  :use [|ctx|]}
(fact "deconstructs an object from context"

  (deconstruct |ctx| #{:c :b :a} [:a])
  => #{})

^{:refer std.lib.deps/dependents-direct :added "3.0"
  :use [|ctx|]}
(fact "returns list of direct dependents"

  (dependents-direct |ctx| :c)
  => #{:b})

^{:refer std.lib.deps/dependents-topological :added "3.0"
  :use [|ctx|]}
(fact "constructs a topological graph of dependents"

  (dependents-topological |ctx| [:c] [:a :b :c])
  => {:c #{:b}})

^{:refer std.lib.deps/dependents-all :added "3.0"
  :use [|ctx|]}
(fact "returns graph of all dependents"
  ^:hidden
  (dependents-all |ctx| :c)
  => {:c #{:b}, :b #{:a}, :a #{}}

  (dependents-all |ctx| :b)
  => {:b #{:a}, :a #{}})

^{:refer std.lib.deps/dependents-ordered :added "3.0"
  :use [|ctx|]}
(fact "returns ordered depenedents"

  (dependents-ordered |ctx| :c)
  => [:a :b :c] ^:hidden

  (dependents-ordered |ctx| :b)
  => [:a :b])

^{:refer std.lib.deps/dependents-refresh :added "3.0"
  :use [|ctx|]}
(fact "refresh all dependents"
  (first (dependents-refresh |ctx| :c))
  => (contains {:b #{} :c #{}}))

^{:refer std.lib.deps/unload-entry :added "3.0"
  :use [|ctx|]}
(fact "unloads itself as well as all dependents for a given id"
  ^:hidden
  (-> (unload-entry |ctx| :b)
      (update 0 #(into {} %)))
  => [{:c #{}}
      [:a :b]])

^{:refer std.lib.deps/reload-entry :added "3.0"
  :use [|ctx|]}
(fact "unloads and reloads itself and all dependents"
  ^:hidden
  (-> (reload-entry |ctx| :c)
      (update 0 #(into {} %)))
  => [{:c #{}, :b #{}, :a #{}}
      [:a :b :c]])
