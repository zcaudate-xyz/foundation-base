(ns kmi.lang.interface-spec
  (:require [hara.lang :as l]))

;;
;; JS
;;

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-iter :as it]]})

(defn.js proto-create
  "creates a prototype map from a spec map"
  {:added "4.1"}
  [spec-map]
  (var out {})
  (xt/for:object [[k f] spec-map]
    (if (xt/x:is-function? f)
      (xt/x:set-key out k
                    (fn [a b c d]
                      (return (f this a b c d))))
      (xt/x:set-key out k f)))
  (return out))

(defn.js runtime-attach
  "attaches runtime dispatch using native JS prototype linkage"
  {:added "4.1"}
  [obj protocol]
  (when protocol
    (Object.setPrototypeOf obj protocol))
  (return obj))

(defn.js runtime-protocol
  "gets runtime dispatch from a managed JS object"
  {:added "4.1"}
  [obj]
  (return (Object.getPrototypeOf obj)))


;;
;; LUA
;;

(l/script :lua
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-iter :as it]]})

(defn.lua proto-create
  "creates a prototype map from a spec map"
  {:added "4.1"}
  [spec-map]
  (xt/x:set-key spec-map "__index" spec-map)
  (return spec-map))


(defn.lua runtime-attach
  "attaches runtime dispatch using native Lua metatables"
  {:added "4.1"}
  [obj protocol]
  (when protocol
    (when (xt/x:nil? (xt/x:get-key protocol "__index"))
      (:= protocol (-/proto-create protocol)))
    (setmetatable obj protocol))
  (return obj))

(defn.lua runtime-protocol
  "gets runtime dispatch from a managed Lua object"
  {:added "4.1"}
  [obj]
  (return (getmetatable obj)))


;;
;; PYTHON
;;

(l/script :python
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-iter :as it]]})

(defn.py proto-create
  "creates a prototype map from a spec map"
  {:added "4.1"}
  [spec-map]
  (xt/x:set-key spec-map "__index" spec-map)
  (return spec-map))

(defn.py runtime-attach
  "attaches runtime dispatch for Python-managed objects"
  {:added "4.1"}
  [obj protocol]
  (xt/x:set-key obj "_rt_protocol" protocol)
  (return obj))

(defn.py runtime-protocol
  "gets runtime dispatch from a managed Python object"
  {:added "4.1"}
  [obj]
  (return (xt/x:get-key obj "_rt_protocol")))


;;
;; DART
;;

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-iter :as it]]})

(defn.dt proto-create
  "creates a protocol map for Dart-managed objects"
  {:added "4.1"}
  [spec-map]
  (return spec-map))

(defn.dt runtime-attach
  "attaches runtime dispatch for Dart-managed objects"
  {:added "4.1"}
  [obj protocol]
  (when protocol
    (xt/x:set-key obj "_rt_protocol" protocol))
  (return obj))

(defn.dt runtime-protocol
  "gets runtime dispatch from a managed Dart object"
  {:added "4.1"}
  [obj]
  (return (xt/x:get-key obj "_rt_protocol")))


;;
;; TOP LEVEL
;;

(l/script :xtalk)

(defabstract.xt proto-create
  [spec-map])

(defabstract.xt runtime-attach
  [obj protocol])

(defabstract.xt runtime-protocol
  [obj])
