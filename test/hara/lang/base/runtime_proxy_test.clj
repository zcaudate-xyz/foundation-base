(ns hara.lang.base.runtime-proxy-test
  (:require [hara.lang :as l]
            [hara.lang.base.runtime :as rt]
            [hara.lang.base.runtime-proxy :as p]
            [hara.lang.base.util :as ut])
  (:use code.test))

^{:refer hara.lang.base.runtime-proxy/proxy-get-rt :added "4.0"}
(fact "gets the redirected runtime"

  (p/proxy-get-rt
   'hara.lang
   :js)
  => rt/rt-default?)

^{:refer hara.lang.base.runtime-proxy/proxy-raw-eval :added "4.0"}
(fact "evaluates the raw string"

  (p/proxy-raw-eval
   {:redirect 'hara.lang
    :lang :js}
   "1 + 1")
  => "1 + 1")

^{:refer hara.lang.base.runtime-proxy/proxy-init-ptr :added "4.0"}
(fact "initialises ptr"
  (p/proxy-init-ptr {:redirect 'hara.lang :lang :js} {}) => nil)

^{:refer hara.lang.base.runtime-proxy/proxy-tags-ptr :added "4.0"}
(fact "gets the ptr tags"

  (p/proxy-tags-ptr
   {:redirect 'hara.lang
    :lang :js}
   ((l/ptr :js)))
  => [:default nil nil])

^{:refer hara.lang.base.runtime-proxy/proxy-deref-ptr :added "4.0"}
(fact "dereefs the pointer"
  (p/proxy-deref-ptr {:redirect 'hara.lang :lang :js} {}) => {:library nil})

^{:refer hara.lang.base.runtime-proxy/proxy-display-ptr :added "4.0"}
(fact "displays the pointer"
  (p/proxy-display-ptr {:redirect 'hara.lang :lang :js} (ut/lang-pointer :js {}))
  => "<free>")

^{:refer hara.lang.base.runtime-proxy/proxy-invoke-ptr :added "4.0"}
(fact "invokes the pointer"
  (p/proxy-invoke-ptr {:redirect 'hara.lang :lang :js} (ut/lang-pointer :js {}) [])
  => string?)

^{:refer hara.lang.base.runtime-proxy/proxy-transform-in-ptr :added "4.0"}
(fact "transforms the pointer on in"
  (p/proxy-transform-in-ptr {:redirect 'hara.lang :lang :js} (ut/lang-pointer :js {}) [])
  => [])

^{:refer hara.lang.base.runtime-proxy/proxy-transform-out-ptr :added "4.0"}
(fact "transforms the pointer on out"
  (p/proxy-transform-out-ptr {:redirect 'hara.lang :lang :js} (ut/lang-pointer :js {}) :ret)
  => :ret)

^{:refer hara.lang.base.runtime-proxy/proxy-started? :added "4.0"}
(fact "checks if proxied has started"
  (p/proxy-started? {:redirect 'hara.lang :lang :js}) => true)

^{:refer hara.lang.base.runtime-proxy/proxy-stopped? :added "4.0"}
(fact "checks if proxied has stopped"
  (p/proxy-stopped? {:redirect 'hara.lang :lang :js}) => true)

^{:refer hara.lang.base.runtime-proxy/proxy-remote? :added "4.0"}
(fact "checks if proxied is remote"
  (p/proxy-remote? {:redirect 'hara.lang :lang :js}) => false)

^{:refer hara.lang.base.runtime-proxy/proxy-info :added "4.0"}
(fact "gets the proxied info"
  (p/proxy-info {:redirect 'hara.lang :lang :js} :brief) => {})

^{:refer hara.lang.base.runtime-proxy/proxy-health :added "4.0"}
(fact "checks the proxied health"
  (p/proxy-health {:redirect 'hara.lang :lang :js}) => true)
