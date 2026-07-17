(ns dart.ui.wind
  "Dart entrypoint for the generated portable-to-WDynamic descriptor adapter."
  (:require [hara.lang :as l]))

(l/script :dart
  {:require [[xt.ui.wind :as wind-ui]]
   :import [["package:fluttersdk_wind/fluttersdk_wind.dart" :as wind]]})

(defn.dt prepare-view
  "prepares the bundled WDynamic json/actions; the Flutter host owns Widget lifecycle"
  [runtime view]
  (return (wind-ui/prepare runtime view)))
