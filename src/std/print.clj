(ns std.print
  (:require [std.concurrent.print :as print]
            [std.print.base.report :as report]
            [std.lib :as h])
  (:refer-clojure :exclude [print println with-out-str prn]))

(h/intern-in print/print
             print/println
             print/prn

             report/print-header
             report/print-row
             report/print-title
             report/print-subtitle
             report/print-column
             report/print-summary
             report/print-tree-graph)
