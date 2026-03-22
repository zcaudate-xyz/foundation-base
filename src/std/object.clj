(ns std.object
  (:require [std.lib.foundation :as f]
            [std.object.element :as element]
            [std.object.element.class :as class]
            [std.object.element.common :as common]
            [std.object.framework :as framework]
            [std.object.framework.access :as access]
            [std.object.framework.read :as read]
            [std.object.framework.struct :as struct]
            [std.object.framework.write :as write]
            [std.object.query :as query])
  (:refer-clojure :exclude [get get-in set keys instance?]))

(f/intern-in class/class-convert

             common/element?
             common/element
             common/context-class

             element/to-element
             element/class-info
             element/class-hierarchy
             element/constructor?
             element/method?
             element/field?
             element/static?
             element/instance?
             element/public?
             element/private?
             element/plain?

             query/query-class
             query/query-instance
             query/query-hierarchy
             query/delegate

             access/get
             access/get-in
             access/set
             access/keys
             access/meta-clear

             read/meta-read
             read/meta-read-exact
             read/meta-read-exact?
             read/to-data
             read/to-map
             read/read-ex
             read/read-getters-form
             read/read-getters
             read/read-all-getters
             read/read-fields

             struct/struct-fields
             struct/struct-getters
             struct/struct-accessor

             write/meta-write
             write/meta-write-exact
             write/meta-write-exact?
             write/from-data
             write/write-ex
             write/write-setters-form
             write/write-setters
             write/write-all-setters
             write/write-fields

             framework/vector-like
             framework/map-like
             framework/string-like
             framework/unextend)
