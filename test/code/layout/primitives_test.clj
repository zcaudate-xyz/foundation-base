(ns code.layout.primitives-test
  (:use code.test)
  (:require [code.layout.primitives :as p]))

^{:refer code.layout.primitives/fn-multiline-let :added "4.0"}
(fact "multiline check for let"
  ^:hidden

  (p/fn-multiline-let '([a 1] (+ a 1)))
  => true)

^{:refer code.layout.primitives/get-layout-spec :added "4.0"}
(fact "get the layout specification"
  ^:hidden
  
  (p/get-layout-spec :list 'let)
  => (contains-in
      {:block 1,
       :args {:arg/bindings
              {:at 1, :inline true, :layout
               {:type :vector, :columns 2, :align-col true}},
              :arg/default {}},
       :fn  {:fn/multiline fn?},
       :type :list,
       :op 'let}))

^{:refer code.layout.primitives/arg-label-sort :added "4.0"}
(fact "orders the order for label functions"
  ^:hidden
  
  (p/arg-label-sort
   {:arg/bindings
    {:at 1,
     :inline true,
     :layout {:type :vector, :columns 2, :align-col true}},
    :arg/default {}})
  => [[:arg/bindings {:at 1, :inline true, :layout {:type :vector, :columns 2, :align-col true}}]
      [:arg/default {}]])

^{:refer code.layout.primitives/arg-label-idx :added "4.0"}
(fact "labels the type of arg"
  ^:hidden
  
  (p/arg-label-idx 1 [:arg/bindings {:at 1}])
  => :arg/bindings

  (p/arg-label-idx 2 [:arg/bindings {:at 1}])
  => nil

  (p/arg-label-idx 2 [:arg/key {:at even?}])
  => :arg/key)

^{:refer code.layout.primitives/arg-label-fn :added "4.0"}
(fact "creates a labelling function"
  ^:hidden
  
  ((p/arg-label-fn {:arg/bindings
                    {:at 1,
                     :inline true,
                     :layout {:type :vector, :columns 2, :align-col true}},
                    :arg/default {}})
   1 [])
  => :arg/bindings

  ((p/arg-label-fn {:arg/bindings
                    {:at 1,
                     :inline true,
                     :layout {:type :vector, :columns 2, :align-col true}},
                    :arg/default {}})
   2 'a)
  => :arg/default)

^{:refer code.layout.primitives/stack-entry :added "4.0"}
(fact "creates a stack entry from type and op"
  ^:hidden

  (p/stack-entry :list 'let)
  => (contains-in
      {:block 1,
       :args {:arg/bindings
              {:at 1, :inline true, :layout
               {:type :vector, :columns 2, :align-col true}},
              :arg/default {}},
       :fn  {:fn/multiline fn?
             :fn/label fn?},
       :type :list,
       :op 'let}))

^{:refer code.layout.primitives/stack-entry-root :added "4.0"}
(fact "creates the root stack entry"
  ^:hidden

  (p/stack-entry-root {})
  => {:type :root, :op :type/root, :indent 0, :position 0})


