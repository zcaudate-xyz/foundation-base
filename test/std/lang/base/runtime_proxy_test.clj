(ns std.lang.base.runtime-proxy-test
  (:use code.test)
  (:require [std.lang.base.runtime-proxy :as p]
            [std.lang.base.runtime :as rt]
            [std.lang :as l]
            [std.lang.base.util :as ut]))

^{:refer std.lang.base.runtime-proxy/proxy-get-rt :added "4.0"}
(fact "gets the redirected runtime"
  ^:hidden
  
  (p/proxy-get-rt
   'std.lang
   :js)
  => rt/rt-default?)

^{:refer std.lang.base.runtime-proxy/proxy-raw-eval :added "4.0"}
(fact "evaluates the raw string"
  ^:hidden
  
  (p/proxy-raw-eval
   {:redirect 'std.lang
    :lang :js}
   "1 + 1")
  => "1 + 1")

^{:refer std.lang.base.runtime-proxy/proxy-init-ptr :added "4.0"}
(fact "initialises ptr"
  (p/proxy-init-ptr {:redirect 'std.lang :lang :js} {}) => nil)

^{:refer std.lang.base.runtime-proxy/proxy-tags-ptr :added "4.0"}
(fact "gets the ptr tags"
  ^:hidden
  
  (p/proxy-tags-ptr
   {:redirect 'std.lang
    :lang :js}
   ((l/ptr :js)))
  => [:default nil nil])

^{:refer std.lang.base.runtime-proxy/proxy-deref-ptr :added "4.0"}
(fact "dereefs the pointer"
  (p/proxy-deref-ptr {:redirect 'std.lang :lang :js} {}) => {:library nil})

^{:refer std.lang.base.runtime-proxy/proxy-display-ptr :added "4.0"}
(fact "displays the pointer"
  (p/proxy-display-ptr {:redirect 'std.lang :lang :js} (ut/lang-pointer :js {}))
  => "<free>")

^{:refer std.lang.base.runtime-proxy/proxy-invoke-ptr :added "4.0"}
(fact "invokes the pointer"
  (p/proxy-invoke-ptr {:redirect 'std.lang :lang :js} (ut/lang-pointer :js {}) [])
  => string?)

^{:refer std.lang.base.runtime-proxy/proxy-transform-in-ptr :added "4.0"}
(fact "transforms the pointer on in"
  (p/proxy-transform-in-ptr {:redirect 'std.lang :lang :js} (ut/lang-pointer :js {}) [])
  => [])

^{:refer std.lang.base.runtime-proxy/proxy-transform-out-ptr :added "4.0"}
(fact "transforms the pointer on out"
  (p/proxy-transform-out-ptr {:redirect 'std.lang :lang :js} (ut/lang-pointer :js {}) :ret)
  => :ret)

^{:refer std.lang.base.runtime-proxy/proxy-started? :added "4.0"}
(fact "checks if proxied has started"
  (p/proxy-started? {:redirect 'std.lang :lang :js}) => true)

^{:refer std.lang.base.runtime-proxy/proxy-stopped? :added "4.0"}
(fact "checks if proxied has stopped"
  (p/proxy-stopped? {:redirect 'std.lang :lang :js}) => true)

^{:refer std.lang.base.runtime-proxy/proxy-remote? :added "4.0"}
(fact "checks if proxied is remote"
  (p/proxy-remote? {:redirect 'std.lang :lang :js}) => false)

^{:refer std.lang.base.runtime-proxy/proxy-info :added "4.0"}
(fact "gets the proxied info"
  (p/proxy-info {:redirect 'std.lang :lang :js} :brief) => {})

^{:refer std.lang.base.runtime-proxy/proxy-health :added "4.0"}
(fact "checks the proxied health"
  (p/proxy-health {:redirect 'std.lang :lang :js}) => true)
