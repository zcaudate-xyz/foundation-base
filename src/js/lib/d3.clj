(ns js.lib.d3
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [count filter force format map max
                            merge min namespace partition range
                            reduce reverse shuffle some sort]))

(l/script :js {})

(defn d3-macro-fn
  "rewrite function
 
   (d3/d3-macro-fn \"d3.bisector\" {:a 1 :b 2})
   => '(. (d3.bisector) (a 1) (b 2))"
  {:added "4.0"}
  ([s m]
   (let [init (clojure.core/list (clojure.core/symbol s))]
     (if (empty? m)
       init
       (apply list '. init (clojure.core/map
                            (fn [[k v & more]]
                              (clojure.core/apply
                               clojure.core/list
                               (clojure.core/symbol (clojure.core/name k))
                               v
                               more))
                            (seq m)))))))

(defn d3-macro
  "creates a macro form"
  {:added "4.0"}
  ([[s props]]
   (with-meta
     (h/$ (defmacro.js ~s
            ([& [m]]
             (d3-macro-fn ~(clojure.core/str "d3." s)
                          m))))
     {:arglists (clojure.core/list 'quote (clojure.core/list [{:keys (vec props)}]))})))

(defn d3-tmpl
  "creates fragment or macro
 
   (d3/d3-tmpl 'minindex)
   => '(def$.js minindex d3.minindex)
 
   (d3/d3-tmpl '[bin #{value domain  thresholds}])
   => '(defmacro.js bin ([& [m]] (d3-macro-fn \"d3.bin\" m)))"
  {:added "4.0"}
  ([s]
   (if (vector? s)
     (d3-macro s)
     (h/$ (def$.js ~s ~(clojure.core/symbol (clojure.core/str "d3." s)))))))

(h/template-entries [d3-tmpl {:category "stats"}]
  [min
   minIndex
   max
   maxIndex
   extent
   sum
   mean
   median
   cumsum
   quantile
   quantileSorted
   variance
   deviation
   fcumsum
   fsum
   Adder])

(h/template-entries [d3-tmpl {:category "search"} ] 
  [least
    leastIndex
    greatest
    greatestIndex
    bisectCenter
    bisectLeft
    bisect
    bisectRight
   [bisector #{center left right}]
    quickselect
    ascending
   descending])

(h/template-entries [d3-tmpl {:category "transform"}]
  [group
   groups
   index
   indexes
   rollup
   rollups
   groupSort
   count
   cross
   merge
   pairs
   permute
   shuffle
   shuffler
   ticks
   tickIncrement
   tickStep
   nice
   range
   transpose
   zip])

(h/template-entries [d3-tmpl {:category "iterable"}]
  [every
   some
   filter
   map
   reduce
   reverse
   sort])

(h/template-entries [d3-tmpl {:category "set"}]
  [difference
   disjoint
   intersection
   superset
   subset
   union])

(h/template-entries [d3-tmpl {:category "histogram"}]
  [[bin #{value domain  thresholds}]
    thresholdFreedmanDiaconis
    thresholdScott
    thresholdSturges])

(h/template-entries [d3-tmpl {:category "interning"}]
  [InternMap
   InternSet])

(h/template-entries [d3-tmpl {:category "axis"}]
  [axisTop
   axisRight
   axisBottom
   axisLeft
   [axis #{scale ticks
           tickArguments
           tickValues
           tickFormat
           tickSize
           tickSizeInner
           tickSizeOuter
           tickPadding}]])

(h/template-entries [d3-tmpl {:category "brush"}]
  [brushX
   brushY
   brushSelection
   [brush #{move clear extent filter
            touchable keyModifiers handleSize on}]])

(h/template-entries [d3-tmpl {:category "chord"}]
  [[chord #{padAngle
            sortGroups
            sortSubgroups
            sortChords}]
   chordDirected
   chordTranspose
   [ribbon #{source target radius sourceRadius targetRadius startAgle endAngle padAngle context}]
   [ribbonArrow #{headRadius}]])

(h/template-entries [d3-tmpl {:category "color"}]
  [[color #{opacity rgb copy brighter darker displayable formatHex formatRgb toString}]
   rgb
   hsl
   lab
   gray
   hcl
   lch
   cubehelix])

(h/template-entries [d3-tmpl {:category "scale-chromatic"
                              :tag "categorical"}]
  [schemeCategory10
   schemeAccent
   schemeDark2
   schemePaired
   schemePastel1
   schemePastel2
   schemeSet1
   schemeSet2
   schemeSet3
   schemeTableau10])


(h/template-entries [d3-tmpl {:category "scale-chromatic"
                              :tag "diverging"}]
  [interpolateBrBG
   interpolatePiYG
   interpolatePRGn
   interpolatePuOr
   interpolateRdBu
   interpolateRdGy
   interpolateRdYlBu
   interpolateRdYlGn
   interpolateSpectral
   schemeBrBG
   schemePiYG
   schemePRGn
   schemePuOr
   schemeRdBu
   schemeRdGy
   schemeRdYlBu
   schemeRdYlGn
   schemeSpectral])

(h/template-entries [d3-tmpl {:category "scale-chromatic"
                              :tag "single-hue"}]
  [interpolateBlues
   interpolateGreens
   interpolateGreys
   interpolateOranges
   interpolatePurples
   interpolateReds
   schemeBlues
   schemeGreens
   schemeGreys
   schemeOranges
   schemePurples
   schemeReds])

(h/template-entries [d3-tmpl {:category "scale-chromatic"
                              :tag "multi-hue"}]
  [interpolateBuGn
   interpolateBuPu
   interpolateCividis
   interpolateCool
   interpolateCubehelixDefault
   interpolateGnBu
   interpolateInferno
   interpolateMagma
   interpolateOrRd
   interpolatePlasma
   interpolatePuBu
   interpolatePuBuGn
   interpolatePuRd
   interpolateRdPu
   interpolateTurbo
   interpolateViridis
   interpolateWarm
   interpolateYlGn
   interpolateYlGnBu
   interpolateYlOrBr
   interpolateYlOrRd
   schemeBuGn
   schemeBuPu
   schemeGnBu
   schemeOrRd
   schemePuBu
   schemePuBuGn
   schemePuRd
   schemeRdPu
   schemeYlGn
   schemeYlGnBu
   schemeYlOrBr
   schemeYlOrRd])

(h/template-entries [d3-tmpl {:category "scale-chromatic"
                              :tag "cyclical"}]
  [interpolateRainbow
   interpolateSinebow])

(h/template-entries [d3-tmpl {:category "contour"}]
  [[contours #{contour size smooth thresholds}]
   contourDensity
   [density #{x y weight size cellSize thresholds bandwidth}]])

(h/template-entries [d3-tmpl {:category "delaunay"}]
  [[Delaunay #{from points halfedges hull triangles inedges find
               neighbors render renderHull renderTriangle renderPoints
               hullPolygon trianglePolygons trianglePolygon update voronoi}]
   [voronoi #{delaunay circumcenters vectors xmin ymin xmax ymax
              contains neighbors render renderBounds renderCell cellPolygons cellPolygon update}]])

(h/template-entries [d3-tmpl {:category "dispatch"}]
  [[dispatch #{on copy call apply}]])

(h/template-entries [d3-tmpl {:category "drag"}]
  [[drag #{container filter touchable subject clickDistance on}]
   dragDisable
   dragEnable])

(h/template-entries [d3-tmpl {:category "csv"}]
  [csvParse
   csvParseRows
   csvFormat
   csvFormatBody
   csvFormatRows
   csvFormatRow
   csvFormatValue
   tsvParse
   tsvParseRows
   tsvFormat
   tsvFormatBody
   tsvFormatRows
   tsvFormatRow
   tsvFormatValue
   autoType])

(h/template-entries [d3-tmpl {:category "ease"}]
  [easeLinear
   [easePolyIn #{exponent}]
   [easePolyOut #{exponent}]
   [easePoly #{exponent}]
   [easePolyInOut #{exponent}]
   easeQuadIn
    easeQuadOut
    easeQuad
    easeQuadInOut
    easeCubicIn
    easeCubicOut
    easeCubic
    easeCubicInOut
    easeSinIn
    easeSinOut
    easeSin
    easeSinInOut
    easeExpIn
    easeExpOut
    easeExp
    easeExpInOut
    easeCircleIn
    easeCircleOut
    easeCircle
    easeCircleInOut
   [easeElasticIn #{amplitude period}]
   [easeElastic #{amplitude period}]
   [easeElasticOut #{amplitude period}]
   [easeElasticInOut #{amplitude period}]
   [easeBackIn #{overshoot}]
   [easeBackOut #{overshoot}]
   [easeBack #{overshoot}]
   [easeBackInOut #{overshoot}]
   easeBounceIn
   easeBounce
   easeBounceOut
   easeBounceInOut])

(h/template-entries [d3-tmpl {:category "fetch"}]
  [blob
   buffer
   csv
   dsv
   html
   image
   json
   svg
   text
   tsv
   xml])

(h/template-entries [d3-tmpl {:category "force"}]
  [[forceSimulation #{restart stop tick nodes alpha alphaMin
                      alphaDecay alphaTarget velocityDecay
                      force find randomSource on}]
   [force #{initialize}]
   [forceCenter #{x y strength}]
   [forceCollide #{radius strength iterations}]
   [forceLink #{links id distance strength iterations}]
   [forceManyBody #{strength theta distanceMin distanceMax}]
   [forceX #{strength x}]
   [forceY #{strength y}]
   [forceRadial #{strength radius x y}]])

(h/template-entries [d3-tmpl {:category "format"}]
  [format
   formatPrefix
   formatSpecifier
   precisionFixed
   precisionPrefix
   precisionRound
   formatLocale
   formatDefaultLocale])

(h/template-entries [d3-tmpl {:category "geo"}]
  [[geoPath #{area bounds centroid measure projection context pointRadius}]
   [projection #{invert stream preclip postclip clipAngle clipExtent
                 scale translate center angle reflectX reflectY
                 rotate precision fitExtent fitSize fitWidth fitHeight}]
   geoPath
   geoAzimuthalEqualArea
   geoAzimuthalEqualAreaRaw
   geoAzimuthalEquidistant
   geoAzimuthalEquidistantRaw
   geoGnomonic
   geoGnomonicRaw
   geoOrthographic
   geoOrthographicRaw
   geoStereographic
   geoStereographicRaw
   geoEqualEarth
   geoEqualEarthRaw
   geoAlbersUsa
   geoAlbers
   geoConicConformal
   geoConicConformalRaw
   geoConicEqualArea
   geoConicEqualAreaRaw
   geoConicEquidistant
   geoConicEquidistantRaw
   geoEquirectangular
   geoEquirectangularRaw
   geoMercator
   geoMercatorRaw
   geoTransverseMercator
   geoTransverseMercatorRaw
   geoNaturalEarth1
   geoNaturalEarth1Raw])

(h/template-entries [d3-tmpl {:category "geo"}]
  [geoProjection
   geoProjectionMutator
   geoArea
   geoBounds
   geoCentroid
   geoDistance
   geoLength
   geoInterpolate
   geoContains
   [geoRotation #{invert}]])

(h/template-entries [d3-tmpl {:category "geo"}]
  [[geoCircle #{center radius precision}]
   [geoGraticule #{lines outline extent extentMajor extentMinor step stepMajor stepMinor precision}]
   geoGraticule10])

(h/template-entries [d3-tmpl {:category "geo"}]
  [[geoStream #{point lineStart lineEnd polygonStart polygonEnd sphere}]
   geoTransform
   geoIdentity
   geoClipAntimeridian
   geoClipCircle
   geoClipRectangle])


(h/template-entries [d3-tmpl {:category "hierarchy"}]
  [[hierarchy #{ancestors descendants leaves find path links sum count
                sort each eachAfter eachBefore copy}]
   [stratify #{id parentId}]
   [cluster #{size nodeSize seperation}]
   [tree #{size nodeSize seperation}]
   [treemap #{tile size round padding paddingInner paddingOuter paddingTop
              paddingRight paddingBottom paddingLeft}]
   treemapBinary
   treemapDice
   treemapSlice
   treemapSliceDice
   treemapSquarify
   treemapResquarify
   [partition #{size round padding}]
   [pack #{radius size padding}]
   packSiblings
   packEnclose])

(h/template-entries [d3-tmpl {:category "interpolate"}]
  [interpolate
   interpolateNumber
   interpolateRound
   interpolateString
   interpolateDate
   interpolateArray
   interpolateNumberArray
   interpolateObject
   interpolateTransformCss
   interpolateTransformSvg
   interpolateZoom
   interpolateDiscrete
   quantize
   interpolateRgb
   interpolateRgbBasis
   interpolateRgbBasisClosed
   interpolateHsl
   interpolateHslLong
   interpolateLab
   interpolateHcl
   interpolateHclLong
   interpolateCubehelix
   interpolateCubehelixLong
   interpolateHue
   interpolateBasis
   interpolateBasisClosed
   piecewise])

(h/template-entries [d3-tmpl {:category "path"}]
  [[path #{moveTo closePath lineTo quadraticCurveTo bezierCurveTo arcTo arc rect toString}]])

(h/template-entries [d3-tmpl {:category "polygon"}]
  [polygonArea
   polygonCentroid
   polygonHull
   polygonContains
   polygonLength])

(h/template-entries [d3-tmpl {:category "quadtree"}]
  [[quadtree #{x y extent cover add addAll remove removeAll copy root data size find visit visitAfter}]])

(h/template-entries [d3-tmpl {:category "random"}]
  [randomUniform
   randomInt
   randomNormal
   randomLogNormal
   randomBates
   randomIrwinHall
   randomExponential
   randomPareto
   randomBernoulli
   randomGeometric
   randomBinomial
   randomGamma
   randomBeta
   randomWeibull
   randomCauchy
   randomLogistic
   randomPoisson
   randomLcg])

(h/template-entries [d3-tmpl {:category "axis"
                              :tag "continuous"}]
  [tickFormat
   [scaleLinear #{invert domain range rangeRound clamp unknown interpolate ticks tickFormat nice copy}]
   [scalePow #{invert exponent domain range rangeRound clamp interpolate ticks tickFormat nice copy}]
   [scaleSqrt #{invert domain range rangeRound clamp unknown interpolate ticks tickFormat nice copy}]
   [scaleLog #{invert base domain range rangeRound clampinterpolate ticks tickFormat nice copy}]
   [scaleSymlog #{invert base domain range rangeRound clampinterpolate ticks tickFormat nice copy}]
   [scaleIdentity #{invert domain range rangeRound clamp unknown interpolate ticks tickFormat nice copy}]
   [scaleRadial #{invert domain range rangeRound clamp unknown interpolate ticks tickFormat nice copy}]
   [scaleTime #{invert domain range rangeRound clamp unknown interpolate ticks tickFormat nice copy}]
   [scaleUtc #{invert domain range rangeRound clamp unknown interpolate ticks tickFormat nice copy}]])

(h/template-entries [d3-tmpl {:category "axis"
                              :tag "sequential"}]
  [[scaleSequential #{domain clamp interpolator range rangeRound copy}]
   [scaleSequentialLog #{domain clamp interpolator range rangeRound copy}]
   [scaleSequentialPow #{domain clamp interpolator range rangeRound copy}]
   [scaleSequentialSqrt #{domain clamp interpolator range rangeRound copy}]
   [scaleSequentialSymlog #{domain clamp interpolator range rangeRound copy}]
   [scaleSequentialQuantile #{domain clamp interpolator range rangeRound copy}]])

(h/template-entries [d3-tmpl {:category "axis"
                              :tag "diverging"}]
  [[scaleDiverging #{domain clamp interpolator range rangeRound copy unknown}]
   [scaleDivergingLog #{domain clamp interpolator range rangeRound copy unknown}]
   [scaleDivergingPow #{domain clamp interpolator range rangeRound copy unknown}]
   [scaleDivergingSqrt #{domain clamp interpolator range rangeRound copy unknown}]
   [scaleDivergingSymlog #{domain clamp interpolator range rangeRound copy unknown}]])

(h/template-entries [d3-tmpl {:category "axis"
                              :tag "quantize"}]
  [[scaleQuantize  #{invertExtent domain range ticks tickFormat nice thresholds copy}]
   [scaleQuantile  #{invertExtent domain range quantiles copy}]
   [scaleThreshold #{invertExtent domain range copy}]])

(h/template-entries [d3-tmpl {:category "axis"
                              :tag "ordinal"}]
  [[scaleOrdinal #{domain range unknown copy}]
   scaleImplicit
   [scaleBand #{domain range rangeRound round paddingInner paddingOuter
                padding align bandwidth step copy}]
   [scalePoint #{domain range rangeRound round padding align bandwidth step copy}]])

(h/template-entries [d3-tmpl {:category "selection"}]
  [selection
   select
   selectAll
   matcher
   selector
   selectorAll
   window
   style
   create
   creator
   pointer
   pointers
   local
   namespace
   namespaces])

(h/template-entries [d3-tmpl {:category "shape"}]
  [[arc #{centroid innerRadius outerRadius cornerRadius startAngle endAngle padAngle padRadius context}]
   [pie #{value sort sortValues startAngle endAngle padAngle}]
   [line #{x y defined curve context}]
   [lineRadial #{angle radius defined curve context}]
   [area #{x x0 x1 y y0 y1 defined curve context lineX0 lineY0 lineX1 lineY1}]
   [areaRadial #{angle startAngle endAngle radius innerRadius outerRadius
                 defined curve context lineStartAngle lineInnerRadius lineEndAngle lineOuterRadius}]])

(h/template-entries [d3-tmpl {:category "shape"
                              :tag "curve"}]
  [curveBasis
   curveBasisClosed
   curveBasisOpen
   curveBundle
   curveBumpX
   curveBumpY
   curveCardinal
   curveCardinalClosed
   curveCardinalOpen
   curveCatmullRom
   curveCatmullRomClosed
   curveCatmullRomOpen
   curveLinear
   curveLinearClosed
   curveMonotoneX
   curveMonotoneY
   curveNatural
   curveStep
   curveStepAfter
   curveStepBefore])

(h/template-entries [d3-tmpl {:category "shape"
                              :tag "link"}]
  [[linkVertical #{source target x y content}]
   [linkHorizontal #{source target x y context}]
   [linkRadial #{angle radius}]])

(h/template-entries [d3-tmpl {:category "shape"
                              :tag "link"}]
  [;;[symbol #{type size context}]
   symbols
   symbolCircle
   symbolCross
   symbolDiamond
   symbolSquare
   symbolStar
   symbolTriangle
   symbolWye])

(h/template-entries [d3-tmpl {:category "shape"
                              :tag "stack"}]
  [[stack #{keys value order offset}]
   stackOrderAppearance
   stackOrderAscending
   stackOrderDescending
   stackOrderInsideOut
   stackOrderNone
   stackOrderReverse
   stackOffsetExpand
   stackOffsetDiverging
   stackOffsetNone
   stackOffsetSilhouette
   stackOffsetWiggle])

(h/template-entries [d3-tmpl {:category "time"
                              :tag "format"}]
  [timeFormat
   timeParse
   utcFormat
   utcParse
   isoFormat
   isoParse
   timeFormatLocale
   timeFormatDefaultLocale])

(h/template-entries [d3-tmpl {:category "time"
                              :tag "interval"}]
  [[timeInterval #{floor round ceil offset range filter every count}]
   timeMillisecond
   timeMilliseconds
   timeSecond
   timeSeconds
   timeMinute
   timeMinutes
   timeHour
   timeHours
   timeDay
   timeDays
   timeWeek
   timeWeeks
   timeSunday
   timeSundays
   timeMonday
   timeMondays
   timeTuesday
   timeTuesdays
   timeWednesday
   timeWednesdays
   timeThursday
   timeThursdays
   timeFriday
   timeFridays
   timeSaturday
   timeSaturdays
   timeMonth
   timeMonths
   timeYear
   timeYears])

(h/template-entries [d3-tmpl {:category "time"
                              :tag "timer"}]
  [now
   timer
   timerFlush
   timeout
   interval])

(h/template-entries [d3-tmpl {:category "transition"}]
  [interrupt
   [transition #{select selectAll filter merge transition selection}]
   [active #{attr attrTween style styleTween text textTween remove tween
             delay duration ease easeVarying end on each call empty nodes node size}]])

(h/template-entries [d3-tmpl {:category "transition"}]
  [[zoom #{transform translateBy translateTo scaleBy scaleTo constrain
           filter touchable wheelDelta extent scaleExtent translateExtent
           clickDistance tapDistance duration interpolate on}]
   [zoomTransform #{scale translate apply applyX applyY invert invertX invertY rescaleX rescaleY toString}]
   zoomIdentity])
