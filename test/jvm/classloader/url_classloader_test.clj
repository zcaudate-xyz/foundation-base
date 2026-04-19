(ns jvm.classloader.url-classloader-test
  (:require [jvm.classloader.common :as common]
            [jvm.classloader.url-classloader :refer :all]
            [jvm.protocol :as protocol.classloader])
  (:use code.test))

^{:refer jvm.classloader.url-classloader/ucp-remove-url :added "4.0"}
(fact "removes the url"
  (let [entry  (common/to-url "/tmp/")
        loader (java.net.URLClassLoader. (into-array java.net.URL [entry]))
        ucp    (loader-access-ucp loader)]
    [(boolean (first (ucp-remove-url ucp entry)))
     (protocol.classloader/-has-url? ucp "/tmp/")])
  => [true false])
