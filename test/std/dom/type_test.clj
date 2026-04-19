(ns std.dom.type-test
  (:require [std.dom.type :refer :all])
  (:use code.test))

^{:refer std.dom.type/metaclass :added "3.0"}
(fact "returns info associated with given metaclass"
  
  (metaclass-add :test {:metatype :value :data "hello"})
  
  (metaclass :test)
  => {:id :test :metatype :value :data "hello"})

^{:refer std.dom.type/metaclass-remove :added "3.0"}
(fact "removes metaclass information"

  (metaclass-remove :test)
  => {:id :test :metatype :value :data "hello"})

^{:refer std.dom.type/metaclass-add :added "3.0"}
(fact "adds metaclass information"

  (metaclass-add :test {:metatype :value})
  => {:id :test :metatype :value})

^{:refer std.dom.type/metaprops :added "3.0"}
(fact "returns metaprops info associated with the node"

  (metaprops-add :test {:tag :test/node})
  
  (metaprops :test/node)
  => {:tag :test/node,
      :metaclass :test,
      :metatype :value})

^{:refer std.dom.type/metaprops-add :added "3.0"}
(fact "adds metaprops information"

  (metaprops-add :test {:tag :test/node})
  => {:tag :test/node
      :metaclass :test
      :metatype :value})

^{:refer std.dom.type/metaprops-remove :added "3.0"}
(fact "removes metaprops information"

  (metaprops-remove :test/node)
  => {:tag :test/node
      :metaclass :test
      :metatype :value})

^{:refer std.dom.type/metaprops-install :added "3.0"}
(fact "adds metaclass information"

  (metaprops-install {:test/a {:tag :test/a
                               :metaclass :dom/value
                               :metatype :dom/value}})^:hidden
  
  (metaprops-remove :test/a)
  => (contains {:tag :test/a}))
