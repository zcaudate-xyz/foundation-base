(ns js.lib.recharts
  (:require [std.lang :as l]
            [std.lib.foundation :as f]))

(l/script :js
  {:import [["recharts" :as [* Recharts]]]})

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "Recharts"
                                   :tag "js"}]
  [Area
   AreaChart
   Bar
   BarChart
   Brush
   CartesianAxis
   CartesianGrid
   Cell
   ComposedChart
   Cross
   Curve
   Customized
   DefaultLegendContent
   DefaultTooltipContent
   DefaultZIndexes
   Dot
   ErrorBar
   Funnel
   FunnelChart
   Global
   Label
   LabelList
   Layer
   Legend
   Line
   LineChart
   Pie
   PieChart
   PolarAngleAxis
   PolarGrid
   PolarRadiusAxis
   Polygon
   Radar
   RadarChart
   RadialBar
   RadialBarChart
   Rectangle
   ReferenceArea
   ReferenceDot
   ReferenceLine
   ResponsiveContainer
   Sankey
   Scatter
   ScatterChart
   Sector
   SunburstChart
   Surface
   Symbols
   Text
   Tooltip
   Trapezoid
   Treemap
   XAxis
   YAxis
   ZAxis
   ZIndexLayer
   getNiceTickValues
   useActiveTooltipDataPoints
   useActiveTooltipLabel
   useChartHeight
   useChartWidth
   useMargin
   useOffset
   usePlotArea
   useXAxisDomain
   useYAxisDomain])
