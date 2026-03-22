(ns js.react.ext-cell-test
  (:require [std.fs :as fs]
            [std.lang :as l])
  (:use code.test))

^{:refer js.react.ext-cell/initCellBase :added "4.0" :unchecked true}
(fact "initialises cell listeners")

^{:refer js.react.ext-cell/listenCell :added "4.0" :unchecked true}
(fact "listen to parts of the cell")

^{:refer js.react.ext-cell/listenCellOutput :added "4.0" :unchecked true}
(fact "listens to the cell output")

^{:refer js.react.ext-cell/listenCellThrottled :added "4.0" :unchecked true}
(fact "listens to the throttled output")

^{:refer js.react.ext-cell/listenRawEvents :added "4.0" :unchecked true}
(fact "listens to the raw cell events")
