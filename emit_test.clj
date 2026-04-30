(ns emit-test
  (:require [std.lang :as l]
            [std.lib :as h]
            [rt.basic :as rb]))

;; Load all the required modules
(require '[xt.event.base-view])
(require '[rt.basic])

;; Initialize the Ruby runtime
(def rt (rb/rt :ruby))
(rb/rt:start rt)

;; Get the emit for create-view
(println "=== Ruby code for add-listener ===")
(println (l/emit-as :ruby
  '[(var v (view/create-view
            (fn:> [x] {:value x})
            {}
            [3]
            {:value 0}
            nil
            nil))
    (view/add-listener v "a1" (fn:>) nil nil)
    (view/add-listener v "b2" (fn:>) nil nil)
    [(view/get-output v)
     (view/list-listeners v)
     (. (view/remove-listener v "b2") ["meta"])
     (view/list-listeners v)]]
  {:lang :ruby
   :namespace 'xtbench.ruby.event.base-view-test}))

(rb/rt:stop rt)
(System/exit 0)
