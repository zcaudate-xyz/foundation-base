(ns js.lib.react-query-test
  (:require [js.lib.react-query :refer :all])
  (:use code.test))

^{:refer js.lib.react-query/useApiQueriesSingle :added "4.0" :unchecked true}
(fact "is defined"

  (var? #'useApiQueriesSingle)
  => true)

^{:refer js.lib.react-query/useApiQueriesBase :added "4.0" :unchecked true}
(fact "is defined"

  (var? #'useApiQueriesBase)
  => true)

^{:refer js.lib.react-query/useApiQueriesWire :added "4.0" :unchecked true}
(fact "is defined"

  (var? #'useApiQueriesWire)
  => true)

^{:refer js.lib.react-query/useApiQueries :added "4.0" :unchecked true}
(fact "is defined"

  (var? #'useApiQueries)
  => true)

^{:refer js.lib.react-query/useApi :added "4.0" :unchecked true}
(fact "is defined"

  (var? #'useApi)
  => true)
