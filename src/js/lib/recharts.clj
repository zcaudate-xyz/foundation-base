(ns js.lib.recharts
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:macro-only true
   :bundle  {:default [["recharts" :as [* Recharts]]]}})

(h/template-entries [l/tmpl-entry {:type :fragment
                                :base "Lucide"
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
