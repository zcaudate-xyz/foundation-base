(ns tahto.core.runtime-proxy-test
  (:require [tahto.core :as l]
            [tahto.core.runtime :as rt]
            [tahto.core.runtime-proxy :as p]
            [tahto.base.util :as ut])
  (:use code.test))

^{:refer tahto.core.runtime-proxy/proxy-get-rt :added "4.0"}
(fact "gets the redirected runtime"

  (p/proxy-get-rt
   'tahto.core
   :js)
  => rt/rt-default?)

^{:refer tahto.core.runtime-proxy/proxy-raw-eval :added "4.0"}
(fact "evaluates the raw string"

  (p/proxy-raw-eval
   {:redirect 'tahto.core
    :lang :js}
   "1 + 1")
  => "1 + 1")

^{:refer tahto.core.runtime-proxy/proxy-init-ptr :added "4.0"}
(fact "initialises ptr"
  (p/proxy-init-ptr {:redirect 'tahto.core :lang :js} {}) => nil)

^{:refer tahto.core.runtime-proxy/proxy-tags-ptr :added "4.0"}
(fact "gets the ptr tags"

  (p/proxy-tags-ptr
   {:redirect 'tahto.core
    :lang :js}
   ((l/ptr :js)))
  => [:default nil nil])

^{:refer tahto.core.runtime-proxy/proxy-deref-ptr :added "4.0"}
(fact "dereefs the pointer"
  (p/proxy-deref-ptr {:redirect 'tahto.core :lang :js} {}) => {:library nil})

^{:refer tahto.core.runtime-proxy/proxy-display-ptr :added "4.0"}
(fact "displays the pointer"
  (p/proxy-display-ptr {:redirect 'tahto.core :lang :js} (ut/lang-pointer :js {}))
  => "<free>")

^{:refer tahto.core.runtime-proxy/proxy-invoke-ptr :added "4.0"}
(fact "invokes the pointer"
  (p/proxy-invoke-ptr {:redirect 'tahto.core :lang :js} (ut/lang-pointer :js {}) [])
  => string?)

^{:refer tahto.core.runtime-proxy/proxy-transform-in-ptr :added "4.0"}
(fact "transforms the pointer on in"
  (p/proxy-transform-in-ptr {:redirect 'tahto.core :lang :js} (ut/lang-pointer :js {}) [])
  => [])

^{:refer tahto.core.runtime-proxy/proxy-transform-out-ptr :added "4.0"}
(fact "transforms the pointer on out"
  (p/proxy-transform-out-ptr {:redirect 'tahto.core :lang :js} (ut/lang-pointer :js {}) :ret)
  => :ret)

^{:refer tahto.core.runtime-proxy/proxy-started? :added "4.0"}
(fact "checks if proxied has started"
  (p/proxy-started? {:redirect 'tahto.core :lang :js}) => true)

^{:refer tahto.core.runtime-proxy/proxy-stopped? :added "4.0"}
(fact "checks if proxied has stopped"
  (p/proxy-stopped? {:redirect 'tahto.core :lang :js}) => true)

^{:refer tahto.core.runtime-proxy/proxy-remote? :added "4.0"}
(fact "checks if proxied is remote"
  (p/proxy-remote? {:redirect 'tahto.core :lang :js}) => false)

^{:refer tahto.core.runtime-proxy/proxy-info :added "4.0"}
(fact "gets the proxied info"
  (p/proxy-info {:redirect 'tahto.core :lang :js} :brief) => {})

^{:refer tahto.core.runtime-proxy/proxy-health :added "4.0"}
(fact "checks the proxied health"
  (p/proxy-health {:redirect 'tahto.core :lang :js}) => true)
