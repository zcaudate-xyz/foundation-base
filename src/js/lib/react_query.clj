(ns js.lib.react-query
  (:require [std.lang :as l]
            [std.lib.foundation :as f]))

(l/script :js
  {:import [["@tanstack/react-query" :as [* ReactQuery]]]
   :require [[js.react :as r] [xt.lang.common-lib :as k] [xt.lang.common-data :as xtd] [xt.lang.spec-base :as xt] [xt.lang.common-sort-by :as xtsb]]})

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactQuery"
                                   :tag "js"}]
  [CancelledError
   HydrationBoundary
   InfiniteQueryObserver
   IsRestoringProvider
   Mutation
   MutationCache
   MutationObserver
   QueriesObserver
   Query
   QueryCache
   QueryClient
   QueryClientContext
   QueryClientProvider
   QueryErrorResetBoundary
   QueryObserver
   dataTagErrorSymbol
   dataTagSymbol
   defaultScheduler
   defaultShouldDehydrateMutation
   defaultShouldDehydrateQuery
   dehydrate
   experimental_streamedQuery
   focusManager
   hashKey
   hydrate
   infiniteQueryOptions
   isCancelledError
   isServer
   keepPreviousData
   matchMutation
   matchQuery
   mutationOptions
   noop
   notifyManager
   onlineManager
   partialMatchKey
   queryOptions
   replaceEqualDeep
   shouldThrowError
   skipToken
   timeoutManager
   unsetMarker
   useInfiniteQuery
   useIsFetching
   useIsMutating
   useIsRestoring
   useMutation
   useMutationState
   usePrefetchInfiniteQuery
   usePrefetchQuery
   useQueries
   useQuery
   useQueryClient
   useQueryErrorResetBoundary
   useSuspenseInfiniteQuery
   useSuspenseQueries
   useSuspenseQuery])

;;
;;
;;
;; Queries

(defn.js useApiQueriesSingle
  "A hook for managing a single API query, including its input, output, and transformation."
  {:added "0.1"}
  [[key q]]
  (var [input setInput]   (r/useState))
  (var raw (-/useQuery {:queryKey [key input]
                         :queryFn
                         (fn [#{queryKey}]
                           (var [_ input] queryKey)
                           (return (q.fn input)))
                         :enabled (or q.enabled (k/not-nil? input))}))
  (var #{[(:= transform k/identity)
          (:= path ["data"])]} q)
  (var #{data} raw)
  (var output q.default)
  (try
    (:= output (or (transform (xtd/get-in data path))
                   q.default))
    (catch e))
  
  (return
   (xtd/obj-assign raw #{[input setInput
                          output
                          :queryRaw q.fn]})))

(defn.js useApiQueriesBase
  "A base hook for managing multiple API queries, providing a structured way to define and access query states."
  {:added "0.1"}
  [api]
  (return
   (-> (. api
          queries)
       (xtd/obj-pairs)
       (xtsb/sort-by [xtd/first])
       (xtd/arr-juxt
         xtd/first
         -/useApiQueriesSingle))))

(defn.js useApiQueriesWire
  "A hook for wiring up API queries with dependencies, ensuring proper execution order and data flow."
  {:added "0.1"}
  [api queries]
  (xt/for:array [qpair (-> (. api queries)
                           (xtd/obj-pairs)
                           (xtsb/sort-by [xtd/first]))]
    (var [qkey q] qpair)
    (when q.deps
      (var params {})
      (xt/for:array [dpair (-> q.deps
                               (xtd/obj-pairs)
                               (xtsb/sort-by [xtd/first]))]
        (var [dkey d] dpair)
        (var #{output
               dataUpdatedAt} (xtd/get-in queries [dkey]))
        (var #{[(:= transform k/identity)
                (:= check k/T)]} d)
        (var flag)
        (try
          (var input (transform output))
          (:= (. params [d.key]) {:value input
                                  :enabled (check input)
                                  :updated dataUpdatedAt})
          (catch e
              (:= (. params [d.key]) {:value nil
                                      :enabled false
                                      :updated dataUpdatedAt}))))
      (var params-str (xt/x:json-encode params))
      (r/useEffect
       (fn []
          (when (xtd/obj-empty?
                 (xtd/obj-filter params (fn [#{enabled}]
                                          (return (== enabled false)))))
            (var #{setInput
                   input
                   refetch} (xtd/get-in queries [qkey]))
            (var ninput (xtd/obj-map params (fn [x] (return (xt/x:get-key x "value")))))
            (cond (not (xtd/eq-nested ninput input))
                  (setInput ninput)
                 
                 q.refetch
                 (do (refetch)))))
       [params-str])))
  (return queries))

(defn.js useApiQueries
  "A comprehensive hook for managing multiple API queries within a component, providing a unified interface for data fetching."
  {:added "0.1"}
  [api]
  (var queries (-/useApiQueriesBase api))
  (return (-/useApiQueriesWire api queries)))

(defn.js  
  useApi
  "A central hook for interacting with the API, providing access to both queries and mutations."
  {:added "0.1"}
  [api]
  (var client (-/useQueryClient))
  (var queries   (-/useApiQueries api))
  (var mutations (-> (. api mutations)
                     (xtd/obj-pairs)
                     (xtsb/sort-by [xtd/first])
                     (xtd/arr-juxt
                       xtd/first
                       (fn [[key mut]]
                         (return
                          (-/useMutation {:onSuccess
                                           (fn []
                                             (xt/for:array [key (or mut.refresh [])]
                                               (. client
                                                  (invalidateQueries {:queryKey [key]}))))
                                           :mutationFn mut.fn}))))))
  (return #{client
            queries
            mutations}))

   
