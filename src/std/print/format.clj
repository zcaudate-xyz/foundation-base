(ns std.print.format
  (:require [std.lib.foundation :as f]
            [std.print.format.chart :as chart]
            [std.print.format.common :as common]
            [std.print.format.report :as report]
            [std.print.format.time :as time]))

(f/intern-in chart/bar-graph
             chart/tree-graph
             chart/sparkline
             chart/table
             chart/table:parse

             common/pad
             common/pad:left
             common/pad:right
             common/pad:center
             common/justify
             common/border
             common/indent

             report/report:bold
             report/report:column
             report/report:header
             report/report:row
             report/report:title

             time/t
             time/t:ms
             time/t:ns
             time/t:time)
