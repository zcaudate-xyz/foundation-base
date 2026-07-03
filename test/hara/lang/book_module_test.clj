(ns hara.lang.book-module-test
  (:require [xt.lang.common-promise]
            [xt.lang.common-data]
            [xt.lang.common-lib]
            [js.blessed]
            [js.blessed.frame]
            [hara.lang :as l]
            [hara.lang.book-module :refer :all]
            [hara.lang.impl :as impl])
  (:use code.test))

(def +library+ (impl/clone-default-library))

^{:refer hara.lang.book-module/book-module? :added "4.0"}
(fact "checks of object is a book module"
  (book-module? (book-module {:lang :lua :id 'L.core})) => true)

^{:refer hara.lang.book-module/book-module :added "4.0"}
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

(def +polyfill-book+
  {:modules
   {'demo.poly (book-module {:lang :js :id 'demo.poly})
    'demo.core (book-module {:lang :js :id 'demo.core})}})

^{:refer hara.lang.book-module/polyfill-default-alias :added "4.1"}
(fact "returns the default alias for a derived polyfill module"
  (polyfill-default-alias 'common-net)  => 'polyfill-net
  (polyfill-default-alias 'polyfill-net) => 'polyfill-net
  (polyfill-default-alias 'net)          => 'polyfill-net)

^{:refer hara.lang.book-module/module-derived-view :added "4.1"}
(fact "returns a compilation view with derived polyfill links"

  (-> (module-derived-view
       +polyfill-book+
       (book-module {:lang :js
                     :id 'demo.core
                     :code '{identity {:deps #{}
                                       :polyfill-modules #{demo.poly}}}}))
      (select-keys [:link :internal :alias]))
  => '{:link {polyfill-demo.poly demo.poly}
        :internal {demo.poly polyfill-demo.poly}
        :alias {polyfill-demo.poly demo.poly}}

  (-> (module-derived-view
       +polyfill-book+
       (book-module {:lang :js
                     :id 'demo.core
                     :code '{identity {:deps #{}
                                       :polyfill-modules #{demo.core}}}}))
      (select-keys [:link :internal :alias]))
  => '{:link {} :internal nil :alias {}}

  (-> (module-derived-view
       +polyfill-book+
       (book-module {:lang :js
                     :id 'demo.core
                     :internal {'demo.poly 'polyfill-demo.poly}
                     :code '{identity {:deps #{}
                                       :polyfill-modules #{demo.poly}}}}))
      (select-keys [:link :internal :alias]))
  => '{:link {} :internal {demo.poly polyfill-demo.poly} :alias {}}

  (module-derived-view
   +polyfill-book+
   (book-module {:lang :js
                 :id 'demo.core
                 :code '{identity {:deps #{}
                                   :polyfill-modules #{missing.module}}}}))
  => (throws-info '{:module demo.core
                    :polyfill missing.module})

  (module-derived-view
   +polyfill-book+
   (book-module {:lang :js
                 :id 'demo.core
                 :link '{polyfill-demo.poly other.module}
                 :code '{identity {:deps #{}
                                   :polyfill-modules #{demo.poly}}}}))
  => (throws-info '{:module demo.core
                    :alias polyfill-demo.poly
                    :current other.module
                    :polyfill demo.poly})

  (module-derived-view
   +polyfill-book+
   (book-module {:lang :js
                 :id 'demo.core
                 :alias '{polyfill-demo.poly other.module}
                 :code '{identity {:deps #{}
                                   :polyfill-modules #{demo.poly}}}}))
  => (throws-info '{:module demo.core
                    :alias polyfill-demo.poly
                    :current other.module
                    :polyfill demo.poly}))

^{:refer hara.lang.book-module/resolve-module-view :added "4.1"}
(fact "resolves a module id or module map to the derived compilation view"

  (resolve-module-view nil nil) => nil

  (-> (resolve-module-view
       +polyfill-book+
       'demo.core)
      :id)
  => 'demo.core

  (-> (resolve-module-view
       nil
       (book-module {:lang :js :id 'demo.core}))
      :id)
  => 'demo.core

  (-> (resolve-module-view
       +polyfill-book+
       (book-module {:lang :js
                     :id 'demo.core
                     :code '{identity {:deps #{}
                                       :polyfill-modules #{demo.poly}}}}))
      :link)
  => '{polyfill-demo.poly demo.poly})

^{:refer hara.lang.book-module/module-deps-code :added "4.0"}
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
     (l/get-book +library+ :js)
     (l/get-module
      +library+
      :js
      'js.blessed.ui-core)))
  => '#{js.blessed.ui-style xt.lang.common-data xt.lang.common-lib js.react}

  (-> (book-module {:id 'demo.promise
                    :lang :python
                    :code '{promise-wrap {:deps #{}
                                          :polyfill-modules #{xt.lang.common-promise}}}})
      (module-deps-code))
  => '#{xt.lang.common-promise}

  (module-deps-code
   {:grammar {:reserved '{x:promise {:op :x-promise
                                      :emit :hard-link
                                      :raw xt.lang.common-promise/promise}}}}
    (book-module {:id 'demo.promise
                  :lang :python
                  :code '{promise-wrap {:deps #{}
                                        :xtalk-ops #{:x-promise}}}}))
  => '#{xt.lang.common-promise}

  (module-deps-code
   {:grammar {:reserved '{x:promise {:op :x-promise
                                     :emit :macro}}}}
    (book-module {:id 'demo.promise
                  :lang :js
                  :code '{promise-wrap {:deps #{}
                                        :xtalk-ops #{:x-promise}}}}))
  => '#{}

  (impl/with:library [+library+]
    (module-deps-code
     (l/get-book +library+ :js)
     (book-module {:id 'demo.promise
                   :lang :js
                   :code '{promise-wrap {:deps #{}
                                         :xtalk-ops #{:x-promise
                                                      :x-promise-then
                                                      :x-promise-catch}}}})))
  => '#{})

^{:refer hara.lang.book-module/module-deps-all :added "4.1"}
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

^{:refer hara.lang.book-module/module-deps-native :added "4.0"}
(fact "gets the native link dependencies"

  (impl/with:library [+library+]
    (module-deps-native
     (hara.lang/get-book +library+ :js)
     (hara.lang/get-module
      +library+
      :js
      'js.react)))
  => '{"react" #{React}})

^{:refer hara.lang.book-module/module-deps-fragment :added "4.0"}
(fact "gets all fragments that have beeen used in js.react"

  (impl/with:library [+library+]
    (module-deps-fragment
     (hara.lang/get-book +library+ :js)
     (hara.lang/get-module
      +library+
      :js
      'js.react)))
  => '#{js.react/ref
         js.react/curr:set
         js.react/lazy
         xt.lang.spec-base/for:array
         xt.lang.spec-base/x:json-encode
         js.react/watch
         xt.lang.spec-promise/x:promise
         xt.lang.spec-base/x:len
         js.react/Component
         xt.lang.spec-base/x:is-function?
         js.react/init
         xt.lang.spec-base/x:not-nil?
         js.react/createDOMRoot
         js.react/local
         xt.lang.spec-base/x:nil?
         js.react/run
         js.react/curr
         xt.lang.common-math/min
         xt.lang.common-math/max
         xt.lang.spec-base/x:now-ms
         xt.lang.common-math/floor
         xt.lang.spec-base/x:get-key
         xt.lang.spec-promise/x:with-delay
         xt.lang.common-trace/LOG!
         js.react/const})

^{:refer hara.lang.book-module/module-entries :added "4.0"}
(fact "creates an export entry for a module"

  (impl/with:library [+library+]
    (module-entries
     (hara.lang/get-module
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
