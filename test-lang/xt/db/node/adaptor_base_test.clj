(ns xt.db.node.adaptor-base-test
  (:use code.test)
  (:require [hara.lang :as l]
            [postgres.gen.bind-macro :as gen]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.substrate :as substrate]
             [xt.db.text.base-flatten :as f]
             [xt.db.text.sql-util :as ut]
             [xt.db.node.adaptor-base :as adaptor]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.adaptor-base/install-schema :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.adaptor-base/install-sqlite :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.adaptor-base/call-db-handler :added "4.1"}
(fact "TODO")


;; 1. create a node
;; 2. setup an sqlite db as caching
;; 3. setup a postgres db as primary


(comment


  
  (!.js
    (substrate/node-create))

  

  )
