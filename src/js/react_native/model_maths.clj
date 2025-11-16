(ns js.react-native.matrix-math
  (:require [std.lang :as l]
            [std.lib :as h]
            ))

(l/script :js
  {:bundle {:default  [["react-native/Libraries/Utilities/MatrixMath" :as MatrixMath]]}})


(h/template-entries [common/js-tmpl {:type :fragment
                                     :base "MatrixMath"}]
  [createIdentityMatrix
   createCopy
   createOrthographic
   createFrustum
   createPerspective
   createTranslate2d
   reuseTranslate2dCommand
   reuseTranslate3dCommand
   createScale
   reuseScaleCommand
   reuseScale3dCommand
   reusePerspectiveCommand
   reuseScaleXCommand
   reuseScaleYCommand
   reuseScaleZCommand
   reuseRotateXCommand
   reuseRotateYCommand
   reuseRotateZCommand
   createRotateZ
   reuseSkewXCommand
   reuseSkewYCommand
   multiplyInto
   determinant
   inverse
   transpose
   multiplyVectorByMatrix
   v3Length
   v3Normalize
   v3Dot
   v3Combine
   v3Cross
   quaternionToDegreesXYZ
   roundTo3Places
   decomposeMatrix])
