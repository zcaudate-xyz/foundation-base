(ns std.contract.sketch-test
  (:require [malli.core :as mc]
            [malli.util :as mu]
            [std.contract.sketch :as s])
  (:use code.test)
  (:refer-clojure :exclude [remove]))

^{:refer std.contract.sketch/optional-string :added "3.0"}
(fact "string for optional"
  ^:hidden
  
  (s/optional-string
   (s/as:optional :int))
  => ":int@?")

^{:refer std.contract.sketch/maybe-string :added "3.0"}
(fact "string for maybe"
  ^:hidden
  
  (s/maybe-string
   (s/as:maybe :int))
  => "?@:int")

^{:refer std.contract.sketch/as:optional :added "3.0"}
(fact "creates an optional"
  ^:hidden
  
  (str (s/as:optional :int))
  => ":int@?"

  (into {} (s/as:optional :int))
  => {:v :int})

^{:refer std.contract.sketch/optional? :added "3.0"}
(fact "checks if optional type"
  ^:hidden
  
  (s/optional? (s/as:optional :int))
  => true)

^{:refer std.contract.sketch/as:maybe :added "3.0"}
(fact "creates a maybe"
  ^:hidden
  
  (str (s/as:maybe :int))
  => "?@:int"
  
  (into {} (s/as:maybe :int))
  => {:v :int}
  
  (mc/ast (s/lax {:size [:or zero? pos-int?]}))
  => {:type :map,
      :keys {:size {:order 0,
                    :value {:type :maybe,
                            :child {:type :or, :children [{:type 'zero?} {:type 'pos-int?}]}},
                    :properties {:optional true}}}}

  (mc/ast [:or zero? pos-int?])
  => {:type :or,
      :children [{:type 'zero?}
                 {:type 'pos-int?}]}
  
  (mc/form (s/to-schema (s/from-schema (s/lax {:size [:or zero? pos-int?]}))))
  => [:map [:size {:optional true} [:maybe [:or 'zero? 'pos-int?]]]]
  
  (mc/ast (s/lax {:size "hello"}))
  => {:type :map,
      :keys {:size {:order 0, :value {:type :maybe,
                                      :child {:type :=, :value "hello"}},
                    :properties {:optional true}}}})

^{:refer std.contract.sketch/maybe? :added "3.0"}
(fact "checks if maybe type"
  ^:hidden
  
  (s/maybe? (s/as:maybe :string))
  => true)

^{:refer std.contract.sketch/func-string :added "3.0"}
(fact "string for func"
  ^:hidden
  
  (s/func-string
   (s/func [x]))
  => "#spec (fn [x])")

^{:refer std.contract.sketch/func-invoke :added "3.0"}
(fact "invokes the func"
  ^:hidden
  
  ((s/func [x])
   (fn [a b c]))
  => false

  ((s/func [x])
   (fn [a]))
  => true)

^{:refer std.contract.sketch/fn-sym :added "3.0"}
(fact "gets function symbol"
  ^:hidden
  
  (s/fn-sym s/fn-sym)
  => symbol?)

^{:refer std.contract.sketch/func-form :added "3.0"}
(fact "constructs a func"
  ^:hidden
  
  (str (s/func-form '[x y]))
  => "#spec (fn [x y])")

^{:refer std.contract.sketch/func :added "3.0"}
(fact "macro for constructing a func"
  ^:hidden
  
  (str (s/func [x y]))
  => "#spec (fn [x y])")

^{:refer std.contract.sketch/func? :added "3.0"}
(fact "checks if instance is a func"
  ^:hidden
  
  (s/func? (s/func [x y]))
  => true)


^{:refer std.contract.sketch/from-schema-map.properties :added "3.0"}
(fact "sketch from malli's map syntax"
  ^:hidden

  (mc/ast
   [:and
   [:map
    [:x int?]
    [:y int?]]
    [:fn (fn [{:keys [x y]}] (> x y))]])
  => (contains-in
      {:type :and,
       :children [{:type :map,
                   :keys {:x {:order 0, :value {:type 'int?}},
                          :y {:order 1, :value {:type 'int?}}}}
                  {:type :fn
                   :value fn?}]})
  
  (with-out-str
    (pr
     (mc/from-ast
      {:type :string,
       :properties {:min 1, :max 10}})))
  => "[:string {:min 1, :max 10}]"
  
  (mc/ast [:maybe :int])
  => {:type :maybe, :child {:type :int}}
  
  (mc/ast [:string {:min 1, :max 10}])
  => {:type :string,
      :properties {:min 1, :max 10}}
  
  (mc/ast [:tuple {:title "location"} :double :double])
  => {:type :tuple, :children [{:type :double}
                               {:type :double}],
      :properties {:title "location"}}

  (mc/ast [:=> [:cat :int] :int])
  => {:type :=>,
      :input {:type :cat, :children [{:type :int}]},
      :output {:type :int}}

  (mc/ast [:-> :int :int])
  => {:type :->, :children [{:type :int}
                            {:type :int}]}

  (mc/ast [:= 1])
  => {:type :=, :value 1}

  (mc/ast [:enum 1 2])
  => {:type :enum, :values [1 2]}

  (mc/ast [:qualified-keyword {:namespace :aaa}])
  => {:type :qualified-keyword, :properties {:namespace :aaa}}

  (mc/ast [:and
           {:title "Age"
            :description "It's an age"
            :json-schema/example 20}
           :int [:> 18]])
  => {:type :and,
      :children [{:type :int}
                 {:type :>, :value 18}],
      :properties {:title "Age",
                   :description "It's an age",
                   :json-schema/example 20}}

  (mc/ast
   [:map
    [:x :boolean]
    [:y {:optional true} :int]
    [:z :string]])
  => {:type :map,
      :keys {:x {:order 0, :value {:type :boolean}},
             :y {:order 1, :value {:type :int}, :properties {:optional true}},
             :z {:order 2, :value {:type :string}}}})

^{:refer std.contract.sketch/from-schema-map :added "3.0"}
(fact "sketch from malli's map syntax"
  ^:hidden

  (mc/form
   (s/to-schema
    (s/from-schema-map
     {:type :map,
      :keys {:size {:order 0,
                    :value {:type :maybe, :child {:type :=, :value "hello"}},
                    :properties {:optional true}}}})))
  => [:map [:size {:optional true} [:maybe [:= "hello"]]]]
  
  (s/from-schema-map
   (mc/ast [:map
            [:street :string]
            [:country [:enum "FI" "UA"]]]))
  => {:street [:string], :country #{"FI" "UA"}}

  
  (s/from-schema-map
   {:type :map,
    :keys {:street {:order 0, :value {:type :string}},
           :country {:order 1, :value {:type :enum, :values ["FI" "UA"]}}}})
  => {:street [:string], :country #{"FI" "UA"}})

^{:refer std.contract.sketch/from-schema :added "3.0"}
(fact "sketch from schema"
  ^:hidden
  
  (s/from-schema
   [:map
    [:street :string]
    [:country [:enum "FI" "UA"]]])
  => {:street [:string],
      :country #{"FI" "UA"}})

^{:refer std.contract.sketch/to-schema-extend :added "3.0"}
(fact "extending schema conversion"
  ^:hidden
  
  (mc/form (s/to-schema-extend 1))
  => [:= 1])

^{:refer std.contract.sketch/to-schema :added "3.0"}
(fact "converts object to schema"
  ^:hidden

  (mc/form (s/to-schema {:a 1
                          :b 2}))
  => [:map
      [:a [:= 1]]
      [:b [:= 2]]]

  (mc/form (s/to-schema {:a (s/lax {:b [:string]})}))
  => [:map
      [:a [:map
           [:b {:optional true}
            [:maybe :string]]]]])

^{:refer std.contract.sketch/lax :added "3.0"}
(fact "relaxes a map (optional keys and maybe vals)"
  ^:hidden
  
  (mc/form
   (s/lax {:a 1
           :b 2}))
  => [:map
      [:a {:optional true}
       [:maybe [:= 1]]]
      [:b {:optional true}
       [:maybe [:= 2]]]]

  (mc/form
   (s/lax {:a (s/lax {:c 3})
           :b 2}))
  => [:map [:a {:optional true}
            [:maybe [:map [:c {:optional true}
                           [:maybe [:= 3]]]]]]
      [:b {:optional true} [:maybe [:= 2]]]])

^{:refer std.contract.sketch/norm :added "3.0"}
(fact "gets rid of optional keys"
  ^:hidden
  
  (mc/form
   (s/norm
    (s/to-schema
     (s/lax {:a 1
             :b 2}))))
  => [:map [:a [:maybe [:= 1]]] [:b [:maybe [:= 2]]]]


  (mc/form
   (s/norm
    (s/lax {:a 1
            :b 2})))
  => [:map [:a [:maybe [:= 1]]] [:b [:maybe [:= 2]]]]

  (mc/form
   (mu/required-keys [:map
                      [:a {:optional true}
                       [:= "hello"]]]
                     [:a]))
  => [:map [:a [:= "hello"]]])

^{:refer std.contract.sketch/closed :added "3.0"}
(fact "closes the map"
  ^:hidden

  (mc/form
   (s/closed {:a 1}))
  => [:map {:closed true} [:a [:= 1]]])

^{:refer std.contract.sketch/opened :added "3.0"}
(fact "opens the map"
  ^:hidden

  (mc/form
   (s/opened (s/closed {:a 1})))
  => [:map [:a [:= 1]]])

^{:refer std.contract.sketch/tighten :added "3.0"}
(fact "tightens a map (no optionals or maybes)"
  ^:hidden
  
  (mc/form
   (s/tighten
    (s/lax {:a 1
            :b 2})))
  => [:map [:a [:= 1]] [:b [:= 2]]])

^{:refer std.contract.sketch/remove :added "3.0"}
(fact "removes a key from map"
  ^:hidden
  
  (mc/form
   (s/remove
    (s/lax {:a 1
            :b 2})
    [:a]))
  => [:map [:b {:optional true} [:maybe [:= 2]]]])


(comment
  (mc/ast [:maybe [:int]])

  (mc/schema
   [:maybe
    (mc/schema
     [:= "hello"])])
  
  (mc/schema
   [:maybe
    (mc/schema
     [:map [:a [:= "hello"]]])])
  
  (mc/ast [:map
           [:x {:optional true} [:int]]])
  (mu/assoc (mc/schema
             [:map [:a [:= "hello"]]])
            :b
            [:= "world"])
  
  (lax {:a "hello"})

  (norm (lax {:a [:string]}))
  (from-schema (lax {:a [:string]}))
  (from-schema (norm {:a "hello"}))
  
  (to-schema {:a "hello"}))
