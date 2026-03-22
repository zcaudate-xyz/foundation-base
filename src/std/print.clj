(ns std.print
  (:require [std.concurrent.print :as print]
            [std.lib.foundation]
            [std.print.base.report :as report])
  (:refer-clojure :exclude [print println with-out-str prn]))

(std.lib.foundation/intern-in print/print
             print/println
             print/prn

             report/print-header
             report/print-row
             report/print-title
             report/print-subtitle
             report/print-column
             report/print-summary
             report/print-tree-graph)
