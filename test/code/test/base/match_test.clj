(ns code.test.base.match-test
  (:use code.test)
  (:require [code.test.base.match :refer :all]))

^{:refer code.test.base.match/match-base :added "3.0"}
(fact "determines whether a term matches with a filter"
  ^:hidden
  
  (match-base {:unit #{:web}}
              {:unit #{:web}}
              false)
  => [true false false]
  (match-base {:refer 'user/foo
               :namespace 'user}
              {:refers '[user/other]
               :namespaces '[foo bar]}
              true)
  => [true false false])

^{:refer code.test.base.match/match-include :added "3.0"}
(fact "determines whether inclusion is a match"
  ^:hidden
  
  (match-include {:unit #{:web}}
                 {:unit #{:web}})
  => true

  (match-include {:refer 'user/foo
                  :namespace 'user}
                 {})
  => true)

^{:refer code.test.base.match/match-exclude :added "3.0"}
(fact "determines whether exclusion is a match"
  ^:hidden
  
  (match-exclude {:unit #{:web}}
                 {:unit #{:web}})
  => true
  (match-exclude {:refer 'user/foo
                  :namespace 'user}
                 {})
  => false)

^{:refer code.test.base.match/match-options :added "3.0"}
(fact "determines whether a set of options can match"
  ^:hidden
  
  (match-options {:unit #{:web}
                  :refer 'user/foo}
                 {:include [{:tags #{:web}}]
                  :exclude []})
  => true

  (match-options {:unit #{:web}
                  :refer 'user/foo}
                 {:include [{:tags #{:web}}]
                  :exclude [{:refers '[user/foo]}]})
  => false

  (match-options {:unit #{:web}
                  :ns 'user
                  :refer 'user/foo}
                 {:include [{:namespaces [#"us"]}]})
  => true)
