(ns code.test.base.listener-test
  (:use code.test)
  (:require [code.test.base.listener :refer :all]
            [std.lib :as h]
            [std.string :as str]
            [code.test.base.runtime :as rt]
            [code.test.base.print :as print]))

^{:refer code.test.base.listener/summarise-verify :added "3.0"}
(fact "extract the comparison into a valid format "
  ^:hidden
  
  (summarise-verify {:status :success :data true :meta
                     {:path "path" :refer "refer" :ns "ns" :line 1 :desc "desc" :parent-form "parent"}
                     :checker {:form "check"} :actual {:form "actual"}})
  => {:status :success
      :path "path"
      :name "refer"
      :ns "ns"
      :line 1
      :desc "desc"
      :form "actual"
      :check "check"
      :checker {:form "check"}
      :actual {:form "actual"}
      :parent "parent"})

^{:refer code.test.base.listener/summarise-evaluate :added "3.0"}
(fact "extract the form into a valid format"
  ^:hidden
  
  (summarise-evaluate {:status :success :data "data"
                       :meta {:path "path" :refer "refer" :ns "ns" :line 1 :desc "desc"} :form "form" :original "original"})
  => {:status :success
      :path "path"
      :name "refer"
      :ns "ns"
      :line 1
      :desc "desc"
      :form "form"
      :original "original"
      :data "data"})

^{:refer code.test.base.listener/form-printer :added "3.0"}
(fact "prints out result for each form"
  ^:hidden
  
  (str/includes? (h/with-out-str
                   (form-printer {:result {:status :exception :data (ex-info "error" {})}}))
                 "THROW")
  => true)

^{:refer code.test.base.listener/check-printer :added "3.0"}
(fact "prints out result per check"
  ^:hidden
  
  (binding [print/*options* #{:print-success}]
    (str/includes? (h/with-out-str
                     (check-printer {:result {:status :success :data true :meta {:path "path" :refer "refer" :ns "ns" :line 1 :desc "desc"} :checker {:form "check"} :actual {:form "actual"}}}))
                   "SUCCESS"))
  => true)

^{:refer code.test.base.listener/form-error-accumulator :added "3.0"}
(fact "accumulator for thrown errors"
  ^:hidden
  
  (let [errors (atom {})]
    (binding [rt/*errors* errors]
      (form-error-accumulator {:result {:status :exception :data (ex-info "error" {})}})
      (first (:exception @errors))))
  => (contains {:status :exception}))

^{:refer code.test.base.listener/check-error-accumulator :added "3.0"}
(fact "accumulator for errors on checks"
  ^:hidden
  
  (let [errors (atom {})]
    (binding [rt/*errors* errors]
      (check-error-accumulator {:result {:status :failure :data false}})
      (first (:failed @errors))))
  => (contains {:status :failure}))

^{:refer code.test.base.listener/fact-printer :added "3.0"}
(fact "prints out results after every fact"
  ^:hidden
  
  (binding [print/*options* #{:print-facts}]
    (str/includes?
     (h/with-out-str
       (fact-printer {:meta {:path "path" :refer "refer" :ns "ns" :line 1 :desc "desc"} :results []}))
                   "Fact"))
  => true)

^{:refer code.test.base.listener/fact-accumulator :added "3.0"}
(fact "accumulator for fact results"
  ^:hidden
  
  (let [acc (atom nil)]
    (binding [rt/*accumulator* acc]
      (fact-accumulator {:id :id :meta :meta :results :results})
      @acc))
  => {:id :id :meta :meta :results :results})

^{:refer code.test.base.listener/bulk-printer :added "3.0"}
(fact "prints out the end summary"
  ^:hidden
  
  (str/includes? (h/with-out-str
                   (bulk-printer {:results {:files 1 :facts 1 :checks 1 :passed 1 :failed 0 :throw 0 :timeout 0}}))
                 "Summary")
  => true)

^{:refer code.test.base.listener/install-listeners :added "3.0"}
(fact "installs all listeners"
  ^:hidden
  
  (install-listeners)
  => true)
