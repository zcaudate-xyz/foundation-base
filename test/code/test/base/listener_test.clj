(ns code.test.base.listener-test
  (:require [clojure.string]
            [code.test.base.context :as ctx]
            [code.test.base.listener :refer :all]
            [code.test.base.print :as print]
            [code.test.base.runtime :as rt]
            [std.lib.env :as env])
  (:use code.test))

^{:refer code.test.base.listener/result-function :added "4.1"}
(fact "prefers explicit refer metadata and falls back to function"
  [(result-function {:meta {:refer 'demo/ref
                            :function 'demo/fn}})
   (result-function {:meta {:function 'demo/fn}})]
  => '[demo/ref demo/fn])

^{:refer code.test.base.listener/result-name :added "4.1"}
(fact "builds a display name from refer, function, desc or id"
  [(result-name {:meta {:refer 'demo/ref}})
   (result-name {:meta {:function 'demo/fn}})
   (result-name {:meta {:desc "A demo"}})
   (result-name {:meta {:id :demo}})]
  => ["demo/ref" "demo/fn" "A demo" ":demo"])

^{:refer code.test.base.listener/summarise-verify :added "3.0"}
(fact "extract the comparison into a valid format "
  ^:hidden
  
  (summarise-verify {:status :success :data true :meta
                     {:path "path" :refer "refer" :function 'foo :ns "ns" :line 1 :desc "desc" :parent-form "parent"}
                     :checker {:form "check"} :actual {:form "actual"}})
  => {:status :success
      :path "path"
      :name "refer"
      :function 'foo
      :ns "ns"
      :line 1
      :desc "desc"
      :form "actual"
      :check "check"
      :checker {:form "check"}
      :actual {:form "actual"}
      :data nil
      :parent "parent"})

^{:refer code.test.base.listener/summarise-evaluate :added "3.0"}
(fact "extract the form into a valid format"
  ^:hidden
  
  (summarise-evaluate {:status :success :data "data"
                       :meta {:path "path" :refer "refer" :function 'foo :ns "ns" :line 1 :desc "desc"} :form "form" :original "original"})
  => {:status :success
      :path "path"
      :name "refer"
      :function 'foo
      :ns "ns"
      :line 1
      :desc "desc"
      :form "form"
      :original "original"
      :data "data"})

^{:refer code.test.base.listener/form-printer :added "3.0"}
(fact "prints out result for each form"
  ^:hidden

  (binding [ctx/*print* #{:print-throw :no-beep}]
    (clojure.string/includes? (env/with-out-str
                     (form-printer {:result {:status :exception :data (ex-info "error" {})}}))
                   "THROW"))
  => true)

^{:refer code.test.base.listener/check-printer :added "3.0"}
(fact "prints out result per check"
  ^:hidden
  
  (binding [ctx/*print* #{:print-success}]
    (clojure.string/includes? (env/with-out-str
                     (check-printer {:result {:status :success :data true :meta {:path "path" :refer "refer" :ns "ns" :line 1 :desc "desc"} :checker {:form "check"} :actual {:form "actual"}}}))
                   "SUCCESS"))
  => true)

^{:refer code.test.base.listener/form-error-accumulator :added "3.0"}
(fact "accumulator for thrown errors"
  ^:hidden
  
  (let [errors (atom {})]
    (binding [ctx/*errors* errors]
      (form-error-accumulator {:result {:status :exception :data (ex-info "error" {})}})
      (first (:exception @errors))))
  => (contains {:status :exception}))

^{:refer code.test.base.listener/check-error-accumulator :added "3.0"}
(fact "accumulator for errors on checks"
  ^:hidden
  
  (let [errors (atom {})]
    (binding [ctx/*errors* errors]
      (check-error-accumulator {:result {:status :failure :data false}})
      (first (:failed @errors))))
  => (contains {:status :failure}))

^{:refer code.test.base.listener/fact-printer :added "3.0"}
(fact "prints out results after every fact"
  ^:hidden
  
  (binding [ctx/*print* #{:print-facts}]
    (clojure.string/includes?
     (env/with-out-str
       (fact-printer {:meta {:path "path" :refer "refer" :ns "ns" :line 1 :desc "desc"} :results []}))
                   "Fact"))
  => true)

^{:refer code.test.base.listener/fact-accumulator :added "3.0"}
(fact "accumulator for fact results"
  ^:hidden
  
  (let [acc (atom nil)]
    (binding [ctx/*accumulator* acc]
      (fact-accumulator {:id :id :meta :meta :results :results})
      @acc))
  => {:id :id :meta :meta :results :results})

^{:refer code.test.base.listener/bulk-printer :added "3.0"}
(fact "prints out the end summary"
  ^:hidden
  
  (clojure.string/includes? (env/with-out-str
                   (bulk-printer {:results {:files 1 :facts 1 :checks 1 :passed 1 :failed 0 :throw 0 :timeout 0}}))
                 "Summary")
  => true)

^{:refer code.test.base.listener/install-listeners :added "3.0"}
(fact "installs all listeners"
  ^:hidden
  
  (install-listeners)
  => true)
