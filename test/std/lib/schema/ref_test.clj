(ns std.lib.schema.ref-test
  (:use code.test)
  (:require [std.lib.schema.ref :refer :all]))

^{:refer std.lib.schema.ref/with:ref-fn :added "4.0"}
(fact "passes a function for use in `reverse-ref-attr` method to add additional params to schema"
  (resolve 'with:ref-fn) => var?)

^{:refer std.lib.schema.ref/keyword-reverse :added "3.0"}
(fact "reverses the keyword by either adding or removing '_' in the value"
  ^:hidden
  
  (keyword-reverse :a/b) => :a/_b

  (keyword-reverse :a/_b) => :a/b

  (keyword-reverse :b) => (throws Exception)

  (keyword-reverse :_b) => (throws Exception))

^{:refer std.lib.schema.ref/keyword-reversed? :added "3.0"}
(fact "checks whether the keyword is reversed (begins with '_')"
  ^:hidden
  
  (keyword-reversed? :a) => false

  (keyword-reversed? :a/b) => false

  (keyword-reversed? :a/_b) => true
  
  (keyword-reversed? :_a) => false)

^{:refer std.lib.schema.ref/is-reversible? :added "3.0"}
(fact "determines whether a ref attribute is reversible or not"
  ^:hidden
  
  (is-reversible? {:ident   :account/email    ;; okay
                   :type    :ref
                   :ref     {:ns  :email}})
  => true

  (is-reversible? {:ident   :email            ;; does not have keyword-ns
                   :type    :ref
                   :ref     {:ns  :email}})
  => false
  
  (is-reversible? {:ident   :account/email}) ;; is missing data
  => false

  (is-reversible? {:ident   :account/_email   ;; is already reversible
                   :type    :ref
                   :ref     {:ns  :email}})
  => false

  (is-reversible? {:ident   :account/email    ;; is not a ref
                   :type    :long
                   :ref     {:ns  :email}})
  => false

  (is-reversible? {:ident   :account/email    ;; is tagged 'norev'
                   :type    :ref
                   :ref     {:ns  :email
                             :norev true}})
  => false)

^{:refer std.lib.schema.ref/determine-rval :added "3.0"}
(fact "outputs the :rval value of a :ref schema reference"
  ^:hidden
  
  (determine-rval [[:account :email false]
                   [{:ident  :account/email}]])
  => :accounts

  (determine-rval [[:account :email true]
                   [{:ident  :account/email}]])
  => :email-accounts

  (determine-rval [[:account :image true]
                   [{:ident  :account/big-image}]])
  => :big-image-accounts

  (determine-rval [[:node  :node  true]
                   [{:ident  :node/children
                     :ref    {:ns    :node}}]])
  => :children-of

  (determine-rval [[:node  :node  false]
                   [{:ident  :node/children
                     :ref    {:ns    :node
                              :rval  :parents}}]])
  => :parents
  
  (determine-rval [[:node  :node  false]
                   [{:ident  :node/children
                     :ref    {:ns    :node}}]])
  => :children-of)

^{:refer std.lib.schema.ref/forward-ref-attr :added "3.0"}
(fact "creates the :ref schema attribute for the forward reference case"
  ^:hidden
  
  (forward-ref-attr [{:ident  :node/children
                      :ref    {:ns    :node
                               :rval  :parents}}])
  => [{:ident    :node/children
       :ref      {:ns     :node
                  :type   :forward
                  :val    :children
                  :key    :node/children
                  :rval   :parents
                  :rkey   :node/_children
                  :rident :node/parents}}]

  (forward-ref-attr [{:ident  :node/children
                      :ref    {:ns    :node}}])
  => (throws Exception))

^{:refer std.lib.schema.ref/reverse-ref-attr :added "3.0"}
(fact "creates the reverse :ref schema attribute for backward reference"
  ^:hidden
  
  (reverse-ref-attr [{:ident    :node/children
                      :ref      {:ns     :node
                                 :type   :forward
                                 :val    :children
                                 :key    :node/children
                                 :rval   :parents
                                 :rkey   :node/_children
                                 :rident :node/parents}}])
  => [{:ident :node/parents
       :cardinality :many
       :type :ref
       :ref  {:ns      :node
              :type    :reverse
              :val     :parents
              :key     :node/_children
              :rval    :children
              :rkey    :node/children
              :rident  :node/children}}]

  (reverse-ref-attr [{:ident    :node/children
                      :ref      {:ns     :node}}])
  => (throws Exception))

^{:refer std.lib.schema.ref/forward-ref-attr-fn :added "3.0"}
(fact "helper for `forward-ref-attr`"
  (resolve 'forward-ref-attr-fn) => var?)

^{:refer std.lib.schema.ref/attr-ns-pair :added "3.0"}
(fact "constructs a :ns and :ident root pair for comparison"
  ^:hidden
  
  (attr-ns-pair [{:ident  :a/b
                  :ref    {:ns :c}}])
  => [:a :c])

^{:refer std.lib.schema.ref/mark-multiple :added "3.0"}
(fact "marks multiple ns/ident groups"
  ^:hidden
  
  (mark-multiple [[[:a :b] [1 2]]
                  [[:c :d] [1]]])
  => [[[:c :d false] 1]
      [[:a :b true] 1] [[:a :b true] 2]])

^{:refer std.lib.schema.ref/ref-attrs :added "3.0"}
(fact "creates forward and reverse attributes for a flattened schema"
  ^:hidden
  
  (ref-attrs {:account/email [{:ident   :account/email
                               :type    :ref
                               :ref     {:ns  :email}}]})
  => {:email/accounts [{:ident :email/accounts
                        :cardinality :many
                        :type :ref
                        :ref {:type   :reverse
                              :key    :account/_email
                              :ns     :account
                              :val    :accounts
                              :rval   :email
                              :rident :account/email
                              :rkey   :account/email}}]
      :account/email  [{:ident :account/email
                        :type :ref
                        :ref {:type   :forward
                              :key    :account/email
                              :ns     :email
                              :val    :email
                              :rval   :accounts
                              :rident :email/accounts
                              :rkey   :account/_email}}]})

(comment
  (./import)
  (./scaffold))
