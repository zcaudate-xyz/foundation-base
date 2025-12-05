(ns code.refactor.tamagui-test
  (:require [code.refactor.tamagui :as sut]
            [code.test :refer :all]
            [code.edit :as edit]
            [code.query :as query]
            [std.lib.zip :as zip]
            [std.block.base :as base]))

(fact "refactor-string with wildcard match"
  (-> (edit/parse-root
       "(l/script :js
          {:require [[js.tamagui :as tm]]}
          (defn.js MyComp []
            (return
             [:% tm/YStack {:p \"$4\" :bg \"red\" :ai \"center\"}
              [:% tm/Text {:color \"white\"} \"Hello\"]
              [:% tm/Button {:size \"$4\"} \"Click Me\"]])))")
      (query/modify '[_]
                    (fn [zloc]
                      (sut/transform-zipper zloc)))
      (sut/replace-require)
      (edit/root-string))
  =>
  "(l/script :js
          {:require [[js.lib.figma :as fg]]}
          (defn.js MyComp []
            (return
             [:div {:className \"flex flex-col items-center bg-red p-4\"} [:span {:className \"text-white\"} \"Hello\"] [:% fg/Button {:size \"$4\"} \"Click Me\"]])))")

(fact "refactor-string with vector className"
  (-> (edit/parse-root
       "(l/script :js
          {:require [[js.tamagui :as tm]]}
          (defn.js MyComp []
            (return
             [:% tm/YStack {:className [\"existing\" \"classes\"] :p \"$4\"}
              \"Content\"])))")
      (query/modify '[_]
                    (fn [zloc]
                      (sut/transform-zipper zloc)))
      (sut/replace-require)
      (edit/root-string))
  =>
  "(l/script :js
          {:require [[js.lib.figma :as fg]]}
          (defn.js MyComp []
            (return
             [:div {:className [\"existing\" \"classes\" \"flex flex-col p-4\"]} \"Content\"])))")

(fact "token conversion"
  (-> (edit/parse-root
       "[:% tm/Stack {:bg \"$red10\" :color \"$blue5\" :br \"$4\"}]")
      (query/modify '[_]
                    (fn [zloc]
                      (sut/transform-zipper zloc)))
      (edit/root-string))
  =>
  "[:div {:className \"text-blue-400 rounded-xl bg-red-800\"}]")

(fact "new components"
  (-> (edit/parse-root
       "[:% tm/Group
         [:% tm/VisuallyHidden \"Hidden\"]
         [:% tm/Spacer]
         [:% tm/Circle {:size \"$4\"}]]")
      (query/modify '[_]
                    (fn [zloc]
                      (sut/transform-zipper zloc)))
      (edit/root-string))
  =>
  "[:div {} [:span {:className \"sr-only\"} \"Hidden\"] [:div {:className \"flex-1\"}] [:div {:className \"rounded-full flex items-center justify-center\" :size \"$4\"}]]")


^{:refer code.refactor.tamagui/convert-color :added "4.1"}
(fact "TODO")

^{:refer code.refactor.tamagui/convert-value :added "4.1"}
(fact "TODO")

^{:refer code.refactor.tamagui/convert-flex :added "4.1"}
(fact "TODO")

^{:refer code.refactor.tamagui/convert-justify :added "4.1"}
(fact "TODO")

^{:refer code.refactor.tamagui/convert-align :added "4.1"}
(fact "TODO")

^{:refer code.refactor.tamagui/process-props :added "4.1"}
(fact "TODO")

^{:refer code.refactor.tamagui/refactor-element :added "4.1"}
(fact "TODO")

^{:refer code.refactor.tamagui/transform-zipper :added "4.1"}
(fact "TODO")

^{:refer code.refactor.tamagui/replace-require :added "4.1"}
(fact "TODO")

^{:refer code.refactor.tamagui/refactor-string :added "4.1"}
(fact "TODO")

^{:refer code.refactor.tamagui/refactor-file :added "4.1"}
(fact "TODO")