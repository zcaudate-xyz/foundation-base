(ns lib.lucene.impl-test
  (:use code.test)
  (:require [lib.lucene.impl :refer :all]
            [lib.lucene.impl.index :as index]
            [lib.lucene.impl.analyzer :as analyzer]))

^{:refer lib.lucene.impl/create-directories :added "3.0"}
(fact "create multiple lucene directories"
  (with-redefs [index/directory (constantly :dir)]
    (create-directories {:template {:a {}} :root "test"}))
  => {:a :dir})

^{:refer lib.lucene.impl/create-analyzers :added "3.0"}
(fact "creates multiple analyzers"
  (with-redefs [analyzer/analyzer (constantly :analyzer)]
    (create-analyzers {:template {:a {}} :root "test"}))
  => {:a :analyzer})

^{:refer lib.lucene.impl/start-lucene :added "3.0"}
(fact "starts the lucene engine"
  (with-redefs [create-directories (constantly {:a :dir})
                create-analyzers (constantly {:a :analyzer})]
    (let [res (start-lucene {:instance (atom {})})]
      @(:instance res)))
  => {:directories {:a :dir}
      :analyzers {:a :analyzer}})

^{:refer lib.lucene.impl/stop-lucene :added "3.0"}
(fact "stops the lucene engine"
  (with-redefs [index/close (constantly nil)]
    (let [res (stop-lucene {:instance (atom {:directories {:a :dir}})})]
      @(:instance res)))
  => nil)

^{:refer lib.lucene.impl/get-index :added "3.0"}
(fact "gets a particular index"
  (get-index {:instance (atom {:directories {:a :dir}
                               :analyzers {:a :analyzer}})}
             :a)
  => [:dir :analyzer])

^{:refer lib.lucene.impl/index-add-lucene :added "3.0"}
(fact "adds an entry to the index"
  (with-redefs [index/add-entry (constantly :added)]
    (index-add-lucene {:instance (atom {:directories {:a :dir} :analyzers {:a :analyzer}})}
                      :a {} {}))
  => :added)

^{:refer lib.lucene.impl/index-update-lucene :added "3.0"}
(fact "updates an entry in the index"
  (with-redefs [index/update-entry (constantly :updated)]
    (index-update-lucene {:instance (atom {:directories {:a :dir} :analyzers {:a :analyzer}})}
                         :a {} {} {}))
  => :updated)

^{:refer lib.lucene.impl/index-remove-lucene :added "3.0"}
(fact "removes an entry from the index"
  (with-redefs [index/remove-entry (constantly :removed)]
    (index-remove-lucene {:instance (atom {:directories {:a :dir} :analyzers {:a :analyzer}})}
                         :a {} {}))
  => :removed)

^{:refer lib.lucene.impl/search-lucene :added "3.0"}
(fact "searches lucene"
  (with-redefs [index/search (constantly :found)]
    (search-lucene {:instance (atom {:directories {:a :dir} :analyzers {:a :analyzer}})}
                   :a {} {}))
  => :found)
