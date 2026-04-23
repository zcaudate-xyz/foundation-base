(ns std.lang.base.book-module-test
  (:require [xt.lang.common-data]
            [xt.lang.common-lib]
            [js.blessed]
            [js.blessed.frame]
            [std.lang :as l]
            [std.lang.base.book-module :refer :all]
            [std.lang.base.impl :as impl])
  (:use code.test))

(def +library+ (impl/clone-default-library))

^{:refer std.lang.base.book-module/book-module? :added "4.0"}
(fact "checks of object is a book module"
  (book-module? (book-module {:lang :lua :id 'L.core})) => true)

^{:refer std.lang.base.book-module/book-module :added "4.0"}
(fact "creates a book module"

  (book-module {:id 'L.core
                :lang :lua
                :link '{- L.core}
                :declare {}
                :alias '{cr coroutine
                         t  table}
                :native   {}
                :code   '{identity {}}})
  => book-module?)

^{:refer std.lang.base.book-module/module-deps-code :added "4.0"}
(fact "gets the code link dependencies"

  (-> (book-module {:id 'L.nginx
                    :lang :lua
                    :link '{-  L.nginx
                            u  L.core}
                    :declare {}
                    :alias '{cr coroutine
                             t  table}
                    :native   {}
                    :code   '{identity {}}})
      (module-deps-code))
  => '#{}


  (impl/with:library [+library+]
    (module-deps-code
     (l/get-module
      +library+
      :js
      'js.blessed.ui-core)))
  => '#{js.blessed.ui-style xt.lang.common-data xt.lang.common-lib js.react})

^{:refer std.lang.base.book-module/module-deps-native :added "4.0"}
(fact "gets the native link dependencies"

  (impl/with:library [+library+]
    (module-deps-native
     (std.lang/get-module
      +library+
      :js
      'js.react)))
  => '{"react" #{React}})

^{:refer std.lang.base.book-module/module-deps-fragment :added "4.0"}
(fact "gets all fragments that have beeen used in js.react"

  (impl/with:library [+library+]
    (module-deps-fragment
     (std.lang/get-module
      +library+
      :js
      'js.react)))
  => '#{js.core/floor
         js.core/map
         js.react/ref
         js.react/curr:set
         xt.lang.spec-base/x:json-encode
         xt.lang.spec-base/x:now-ms
         xt.lang.spec-base/for:array
         js.react/lazy
         js.react/watch
         js.core/delayed
         xt.lang.spec-base/x:nil?
         js.react/Component
         js.react/init
         js.react/createDOMRoot
         xt.lang.spec-base/x:get-key
         js.core/min
         js.react/local
         js.core/identity
         js.core/repeating
         js.core/round
         xt.lang.spec-base/x:len
         js.react/run
         js.react/curr
         js.core/randomId
         js.core/future
         js.core/indexOf
         xt.lang.spec-base/x:not-nil?
         xt.lang.spec-base/x:is-function?
         xt.lang.common-trace/LOG!
         js.core/clearTimeout
         js.core/clearInterval
         js.core/future-delayed
        js.core/max
        js.react/const})


^{:refer std.lang.base.book-module/module-entries :added "4.0"}
(fact "creates an export entry for a module"

  (impl/with:library [+library+]
    (module-entries
     (std.lang/get-module
      +library+
      :js
      'js.react)
     #{:defn}))
  => '([(:% \" getDOMRoot \") js.react/getDOMRoot]
       [(:% \" renderDOMRoot \") js.react/renderDOMRoot]
       [(:% \" useStateFor \") js.react/useStateFor]
       [(:% \" id \") js.react/id]
       [(:% \" useStep \") js.react/useStep]
       [(:% \" makeLazy \") js.react/makeLazy]
       [(:% \" useLazy \") js.react/useLazy]
       [(:% \" useRefresh \") js.react/useRefresh]
       [(:% \" useGetCount \") js.react/useGetCount]
       [(:% \" useFollowRef \") js.react/useFollowRef]
       [(:% \" useIsMounted \") js.react/useIsMounted]
       [(:% \" useIsMountedWrap \") js.react/useIsMountedWrap]
       [(:% \" useMountedCallback \") js.react/useMountedCallback]
       [(:% \" useFollowDelayed \") js.react/useFollowDelayed]
       [(:% \" useStablized \") js.react/useStablized]
       [(:% \" runIntervalStop \") js.react/runIntervalStop]
       [(:% \" runIntervalStart \") js.react/runIntervalStart]
       [(:% \" useInterval \") js.react/useInterval]
       [(:% \" runTimeoutStop \") js.react/runTimeoutStop]
       [(:% \" runTimeoutStart \") js.react/runTimeoutStart]
       [(:% \" useTimeout \") js.react/useTimeout]
       [(:% \" useCountdown \") js.react/useCountdown]
       [(:% \" useNow \") js.react/useNow]
       [(:% \" useSubmit \") js.react/useSubmit]
       [(:% \" useSubmitResult \") js.react/useSubmitResult]
       [(:% \" convertIndex \") js.react/convertIndex]
       [(:% \" convertModular \") js.react/convertModular]
       [(:% \" convertIndices \") js.react/convertIndices]
       [(:% \" convertPosition \") js.react/convertPosition]
       [(:% \" useChanging \") js.react/useChanging]
       [(:% \" useTree \") js.react/useTree]))


^{:refer std.lang.base.book-module/module-deps-all :added "4.1"}
(fact "gets all module dependencies including explicit links"
  (module-deps-all
   (book-module {:id 'L.nginx
                 :lang :lua
                 :link '{- L.nginx
                         u L.core
                         json L.json}
                 :code '{identity {:deps #{L.core/add
                                          L.json/parse}}}}))
  => '#{L.core L.json})
