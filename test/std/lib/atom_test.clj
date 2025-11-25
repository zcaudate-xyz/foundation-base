(ns std.lib.atom-test
  (:use code.test)
  (:require [std.lib.atom :refer :all]))

^{:refer std.lib.atom/update-diff :added "4.0"}
(fact "updates a diff in a sub nesting"
  ^:hidden
  
  (update-diff {:a {:b {:c 1}}}
               [:a]
               atom-put-fn [:b] {:d 4} )
  => [[{:c 1} {:c 1, :d 4}]
      {:a {:b {:c 1, :d 4}}}])

^{:refer std.lib.atom/swap-return! :added "3.0"
  :style/indent 1}
(fact "returns output and new state of atom"
  ^:hidden
  
  (swap-return! (atom {})
                (fn [m]
                  [true (assoc m :a 1)])
                true)
  => [true {:a 1}])

^{:refer std.lib.atom/atom:keys :added "3.0"}
(fact "lists the nested keys of an atom"
  ^:hidden
  
  (atom:keys (atom {:a {:b 1 :c 2}})
             [:a])
  => [:b :c])

^{:refer std.lib.atom/atom:get :added "3.0"}
(fact "gets all the nested keys within an atom"
  ^:hidden
  
  (atom:get (atom {:a {:b 1 :c 2}})
            [:a :c])
  => 2)

^{:refer std.lib.atom/atom:mget :added "3.0"}
(fact "gets all the nested keys within an atom"
  ^:hidden
  
  (atom:mget (atom {:a {:b 1 :c 2}})
             [[:a :b]
              [:a :c]])
  => [1 2])

^{:refer std.lib.atom/atom-put-fn :added "4.0"}
(fact "constructs the output and next state for a put operation"
  ^:hidden
  
  (atom-put-fn {:a {:b 1 :c 2}}
               []
               {:a {:d 3}})
  => [[{:a {:b 1, :c 2}}
       {:a {:b 1, :c 2, :d 3}}]
      {:a {:b 1, :c 2, :d 3}}])

^{:refer std.lib.atom/atom:put :added "3.0"}
(fact "puts an entry into the atom"
  ^:hidden
  
  (atom:put (atom {:a {:b 1 :c 2}})
            []
            {:a {:d 3}})
  => [{:a {:b 1, :c 2}}
      {:a {:b 1, :c 2, :d 3}}])

^{:refer std.lib.atom/atom-reduce-fn :added "3.0"}
(fact "helper function for mutations on atom"

  (atom-reduce-fn {}
                  (fn [v old] (+ v (or old 0)))
                  (fn [path v old new] [path v old new])
                  [[[:a] 1]])
  => '[[[[:a] 1 nil 1]] {:a 1}])

^{:refer std.lib.atom/atom-set-fn :added "4.0"}
(fact "constructs the output and next state for a set operation"
  ^:hidden
  
  (atom-set-fn {:a {:b 1 :c 2}}
               [[[:a :b] 3]
                [[:a :d] 5]])
  => '[([[:a :b] 1 3]
        [[:a :d] nil 5])
       {:a {:b 3, :c 2, :d 5}}])

^{:refer std.lib.atom/atom:set :added "3.0"}
(fact "sets the entries given a set of inputs"
  ^:hidden
  
  (atom:set (atom {:a {:b 1 :c 2}})
            [:a :b] 3
            [:a :d] 5)
  => '([[:a :b] 1 3]
       [[:a :d] nil 5])
  
  (-> (atom:set (atom {:a {:b 1 :c 2}})
                [:a :b] 3
                [:a :d] 5)
       (atom:set-changed))
  => [:changed {:a {:b 3, :d 5}}])

^{:refer std.lib.atom/atom-set-keys-fn :added "4.0"}
(fact "constructs the output and next state for a set-keys operation"
  ^:hidden
  
  (atom-set-fn {:a {:d 1 :b 1 :c {:a 1}}}
               [[[:a]
                 {:b 4 :c {:b 2}}]])
  => '[([[:a]
         {:d 1, :b 1, :c {:a 1}}
         {:b 4, :c {:b 2}}])
       {:a {:b 4, :c {:b 2}}}])

^{:refer std.lib.atom/atom:set-keys :added "3.0"}
(fact "sets the entries given a set of inputs"
  ^:hidden
  
  (-> (atom:set-keys (atom {:a {:d 1 :b 1 :c {:a 1}}})
                     [:a]
                    {:b 4 :c {:b 2}})
      (atom:set-changed))
  => [:changed {:a {:b 4, :c {:b 2}}}])

^{:refer std.lib.atom/atom:set-changed :added "3.0"}
(fact "figure out what has changed in set"
  ^:hidden
  (->> (atom:set (atom {:a {:b 1 :c 2}})
                 [:a] {:b 3 :d 4})
       (atom:set-changed))
  => [:changed {:a {:b 3, :d 4}}])

^{:refer std.lib.atom/atom:put-changed :added "3.0"}
(fact "figure out what has changed in put operation"
  ^:hidden

  (->> (atom:put (atom {:a {:b 1 :c 2}})
                 []
                 {:a {:d 3}})
       (atom:put-changed))
  => [:changed {:a {:d 3}}])

^{:refer std.lib.atom/atom-swap-fn :added "4.0"}
(fact "constructs the output and next state for a swap operation"
  ^:hidden
  
  (atom-swap-fn {:a {:b 1 :c 2}}
                [[[:a :b] inc]
                 [[:a :c] dec]])
  => '[([[:a :b] 1 2]
        [[:a :c] 2 1])
       {:a {:b 2, :c 1}}])

^{:refer std.lib.atom/atom:swap :added "3.0"}
(fact "swaps entries atomically given function"
  ^:hidden
  
  (atom:swap (atom {:a {:b 1 :c 2}})
    [:a :b] inc
    [:a :c] dec)
  => '([[:a :b] 1 2] [[:a :c] 2 1]))

^{:refer std.lib.atom/atom-delete-fn :added "4.0"}
(fact "constructs the output and next state for a delete operation"
  ^:hidden

  (atom-delete-fn {:a {:b 1 :c 2}}
                  [[:a :b]
                   [:a :c]])
  => '[([[:a :b] 1] [[:a :c] 2])
       {:a {}}])

^{:refer std.lib.atom/atom:delete :added "3.0"}
(fact "deletes individual enties from path"
  ^:hidden
  
  (atom:delete (atom {:a {:b 1 :c 2}})
               [:a :b]
               [:a :c])
  => '([[:a :b] 1] [[:a :c] 2]))

^{:refer std.lib.atom/atom:clear :added "3.0"}
(fact "clears the previous entry"
  ^:hidden
  
  (atom:clear (atom {:a {:b 1 :c 2}})
              [:a :b])
  => 1)

^{:refer std.lib.atom/atom-batch-fn :added "4.0"}
(fact "constructs the output and next state for a batch operation"
  ^:hidden

  (atom-batch-fn {:a {:b 1 :c 2}}
                 [[:set  [:a :b] 3]
                  [:swap [:a :c] + 10]
                  [:put  [:a :d] {:x 8 :y 9}]
                  [:delete [:a :d :x]]])
  => '[([[:a :b] 1 3]
        [[:a :c] 2 12]
        [[:a :d] nil {:x 8, :y 9}]
        [[:a :d :x] 8 nil])
       {:a {:b 3, :c 12, :d {:y 9}}}])

^{:refer std.lib.atom/atom:batch :added "3.0"}
(fact "performs a batched operation given keys"
  ^:hidden
  
  (def -atm- (atom {:a {:b 1 :c 2}}))

  (atom:batch -atm-
              [[:set  [:a :b] 3]
               [:swap [:a :c] + 10]
               [:put  [:a :d] {:x 8 :y 9}]
               [:delete [:a :d :x]]])
  => '([[:a :b] 1 3]
       [[:a :c] 2 12]
       [[:a :d] nil {:x 8, :y 9}]
       [[:a :d :x] 8 nil])

  @-atm-
  => {:a {:b 3
          :c 12
          :d {:y 9}}})

^{:refer std.lib.atom/atom:cursor :added "3.0"
  :let [a  (atom {:a {:b 1}})
        ca (atom:cursor a [:a :b])]}
(fact "adds a cursor to the atom to swap on any change"
  ^:hidden
  
  (do (swap! ca + 10)
      (swap! a update-in [:a :b] + 100)
      [(deref a) (deref ca)])
  => [{:a {:b 111}} 111])

^{:refer std.lib.atom/atom:derived :added "3.0"
  :let  [a (atom 1)
         b (atom 10)
         c (atom:derived [a b] +)]}
(fact "constructs an atom derived from other atoms"
  ^:hidden
  
  (do (swap! a + 1)
      (swap! b + 10)
      [@a @b @c])
  => [2 20 22])

(comment
  (./import)
  (./create-tests)
  )
