(ns js.lib.react-query
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:macro-only true
   :bundle  {:default [["@tanstack/react-query" :as [* ReactQuery]]]}})

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
