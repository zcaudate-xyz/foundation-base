(ns lang.core.runtime-proxy-test
  (:require [lang.core :as l]
            [lang.core.runtime :as rt]
            [lang.core.runtime-proxy :as p]
            [lang.base.util :as ut])
  (:use code.test))

^{:refer lang.core.runtime-proxy/proxy-get-rt :added "4.0"}
(fact "gets the redirected runtime"

  (p/proxy-get-rt
   'lang.core
   :js)
  => rt/rt-default?)

^{:refer lang.core.runtime-proxy/proxy-raw-eval :added "4.0"}
(fact "evaluates the raw string"

  (p/proxy-raw-eval
   {:redirect 'lang.core
    :lang :js}
   "1 + 1")
  => "1 + 1")

^{:refer lang.core.runtime-proxy/proxy-init-ptr :added "4.0"}
(fact "initialises ptr"
  (p/proxy-init-ptr {:redirect 'lang.core :lang :js} {}) => nil)

^{:refer lang.core.runtime-proxy/proxy-tags-ptr :added "4.0"}
(fact "gets the ptr tags"

  (p/proxy-tags-ptr
   {:redirect 'lang.core
    :lang :js}
   ((l/ptr :js)))
  => [:default nil nil])

^{:refer lang.core.runtime-proxy/proxy-deref-ptr :added "4.0"}
(fact "dereefs the pointer"
  (p/proxy-deref-ptr {:redirect 'lang.core :lang :js} {}) => {:library nil})

^{:refer lang.core.runtime-proxy/proxy-display-ptr :added "4.0"}
(fact "displays the pointer"
  (p/proxy-display-ptr {:redirect 'lang.core :lang :js} (ut/lang-pointer :js {}))
  => "<free>")

^{:refer lang.core.runtime-proxy/proxy-invoke-ptr :added "4.0"}
(fact "invokes the pointer"
  (p/proxy-invoke-ptr {:redirect 'lang.core :lang :js} (ut/lang-pointer :js {}) [])
  => string?)

^{:refer lang.core.runtime-proxy/proxy-transform-in-ptr :added "4.0"}
(fact "transforms the pointer on in"
  (p/proxy-transform-in-ptr {:redirect 'lang.core :lang :js} (ut/lang-pointer :js {}) [])
  => [])

^{:refer lang.core.runtime-proxy/proxy-transform-out-ptr :added "4.0"}
(fact "transforms the pointer on out"
  (p/proxy-transform-out-ptr {:redirect 'lang.core :lang :js} (ut/lang-pointer :js {}) :ret)
  => :ret)

^{:refer lang.core.runtime-proxy/proxy-started? :added "4.0"}
(fact "checks if proxied has started"
  (p/proxy-started? {:redirect 'lang.core :lang :js}) => true)

^{:refer lang.core.runtime-proxy/proxy-stopped? :added "4.0"}
(fact "checks if proxied has stopped"
  (p/proxy-stopped? {:redirect 'lang.core :lang :js}) => true)

^{:refer lang.core.runtime-proxy/proxy-remote? :added "4.0"}
(fact "checks if proxied is remote"
  (p/proxy-remote? {:redirect 'lang.core :lang :js}) => false)

^{:refer lang.core.runtime-proxy/proxy-info :added "4.0"}
(fact "gets the proxied info"
  (p/proxy-info {:redirect 'lang.core :lang :js} :brief) => {})

^{:refer lang.core.runtime-proxy/proxy-health :added "4.0"}
(fact "checks the proxied health"
  (p/proxy-health {:redirect 'lang.core :lang :js}) => true)
