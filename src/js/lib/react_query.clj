(ns js.lib.react-query
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.react :as r]
             [xt.lang.base-lib :as k]]
   :import [["@tanstack/react-query" :as [* ReactQuery]]]})

(h/template-entries [l/tmpl-entry {:type :fragment
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
    (:= output (or (transform (k/get-in data path))
                   q.default))
    (catch e))
  
  (return
   (k/obj-assign raw #{[input setInput
                        output
                        :queryRaw q.fn]})))

(defn.js useApiQueriesBase
  "A base hook for managing multiple API queries, providing a structured way to define and access query states."
  {:added "0.1"}
  [api]
  (return
   (-> (. api
          queries)
       (k/obj-pairs)
       (k/sort-by [k/first])
       (k/arr-juxt
        k/first
        -/useApiQueriesSingle))))

(defn.js useApiQueriesWire
  "A hook for wiring up API queries with dependencies, ensuring proper execution order and data flow."
  {:added "0.1"}
  [api queries]
  (k/for:array [qpair (-> (. api queries)
                             (k/obj-pairs)
                             (k/sort-by [k/first]))]
    (var [qkey q] qpair)
    (when q.deps
      (var params {})
      (k/for:array [dpair (-> q.deps
                                 (k/obj-pairs)
                                 (k/sort-by [k/first]))]
        (var [dkey d] dpair)
        (var #{output
               dataUpdatedAt} (k/get-in queries [dkey]))
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
      (var params-str (k/json-encode params))
      (r/useEffect
       (fn []
         (when (k/obj-empty?
                (k/obj-filter params (fn [#{enabled}]
                                       (return (== enabled false)))))
           (var #{setInput
                  input
                  refetch} (k/get-in queries [qkey]))
           (var ninput (k/obj-map params (k/key-fn "value")))
           (cond (not (k/eq-nested ninput input))
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
                     (k/obj-pairs)
                     (k/sort-by [k/first])
                     (k/arr-juxt
                      k/first
                      (fn [[key mut]]
                        (return
                         (-/useMutation {:onSuccess
                                          (fn []
                                            (k/for:array [key (or mut.refresh [])]
                                              (. client
                                                 (invalidateQueries {:queryKey [key]}))))
                                          :mutationFn mut.fn}))))))
  (return #{client
            queries
            mutations}))

   

