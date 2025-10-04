(ns js.react-native.animate-test
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]
            [xt.lang.base-notify :as notify]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.core :as j]
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [js.react-native.animate :as a]
             [xt.lang.event-animate :as event-animate]
             [xt.lang.base-lib :as k]]
   :export [MODULE]})

^{:refer js.react-native.animate/val :added "4.0"}
(fact "shortcut for Animated.Value"
  ^:hidden
  
  ((:template @a/val) 0)
  => '(React.useCallback (new ReactNative.Animated.Value 0) [])

  (defn.js ValDemo
    []
    (let [ind       (a/val 1)
          textRef   (a/useListenSingle
                     ind
                     (fn [v]
                       (return {:text (+ "ind: " (j/toFixed v 2))})))]
      (return
       (n/EnclosedCode 
        {:label "js.react-native.animate/val"} 
        [:% n/Row
         [:% n/Button
          {:title "-1"
           :onPress (fn []
                      (a/setValue ind
                                  (- ind._value 1)))}]
         [:% n/Padding {:style {:width 10}}]
         [:% n/Button
          {:title "+1"
           :onPress (fn []
                      (a/setValue ind
                                  (+ ind._value 1)))}]
         [:% n/Fill]
         [:% n/TextInput
          {:ref textRef
           :editable false
           :style {:padding 5
                   :width 100
                   :textAlign "right"}}]])))))

^{:refer js.react-native.animate/isAnimatedValue :added "4.0"}
(comment "checks that value is animated"
  ^:hidden
  
  (!.js
   (a/isAnimatedValue (new a/Value 1)))
  => true

  (!.js
   (a/isAnimatedValue 1))
  => false

  (!.js
   (a/isAnimatedValue nil))
  => false)

^{:refer js.react-native.animate/createTransition :added "4.0"}
(fact "creates a transition from params"
  ^:hidden
  
  (defn.js CreateTransitionDemo
    []
    (let [ind         (a/val 0)
          offFn       (r/const
                       (a/createTransition
                        ind
                        {:default {:type "timing"
                                   :duration 200
                                   :easing a/linear}}
                        [30 0]
                        k/identity))
          onFn        (r/const
                       (a/createTransition
                        ind
                        {:default {:type "timing"
                                   :duration 200
                                   :easing a/linear}}
                        [0 30]
                        k/identity))
          textRef (a/useListenSingle
                   ind
                   (fn [v]
                     (return {:text (j/toFixed v 3)})))]
      (return
       (n/EnclosedCode 
{:label "js.react-native.animate/createTransition"} 
[:% n/Row
         [:% n/Button
          {:title "PUSH"
           :onPress (fn []
                      (if (< ind._value 15)
                        (onFn)
                        (offFn)))}]
         [:% n/Fill]
         [:% n/TextInput {:ref textRef
                          :editable false
                          :style {:padding 5
                                  :textAlign "right"}}]])))))

^{:refer js.react-native.animate/setPropsAll :added "4.0"}
(fact "sets props for all the elements")

^{:refer js.react-native.animate/derive :added "4.0"}
(fact "derives a value from one or more Animated.Value"
  ^:hidden
  
  (defn.js DeriveDemo
    []
    (let [ind       (a/val 1)
          ind2      (a/derive (fn:> [v] (* 2 v))
                              [ind])
          textRef   (a/useListenArray
                     [ind ind2]
                     (fn [v v2]
                       (return {:text
                                (+ (+ " ind: "  (j/toFixed v 2))
                                   (+ ", ind2: " (j/toFixed v2 2)))})))]
      (return
       (n/EnclosedCode 
{:label "js.react-native.animate/val"} 
[:% n/Row
         [:% n/Button
          {:title "-1"
           :onPress (fn [] (a/setValue ind
                                       (- ind._value 1)))}]
         [:% n/Padding {:style {:width 10}}]
         [:% n/Button
          {:title "+1"
           :onPress (fn [] (a/setValue ind
                                       (+ ind._value 1)))}]
         [:% n/Fill]
         [:% n/TextInput {:ref textRef
                          :editable false
                          :style {:padding 5
                                  :width 200
                                  :textAlign "right"}}]])))))

^{:refer js.react-native.animate/listenSingle :added "4.0"}
(fact "listens to indicator and sets function"
  ^:hidden
  
  (defn.js ListenSingleDemo
    []
    (let [[active setActive]  (r/local 1)
          ind    (a/useBinaryIndicator
                  active
                  {:default {:type "timing"
                             :duration 200
                             :easing a/linear}})
          textRef (r/ref)
          _       (r/init []
                    (a/listenSingle
                     textRef
                     ind
                     (fn [ind]
                       (return
                        {:text (j/toFixed ind 2)}))))]
      (return
       (n/EnclosedCode 
{:label "js.react-native.animate/listenSingle"} 
[:% n/Row
         [:% n/Button
          {:title "PUSH"
           :onPress (fn [] (setActive (not active)))}]
         [:% n/Fill]
         [:% n/TextInput {:ref textRef
                          :editable false
                          :style {:padding 5
                                  :width 100
                                  :textAlign "right"}}]])))))

^{:refer js.react-native.animate/useListenSingle :added "4.0"}
(fact "listens to a single indicator to set ref"
  ^:hidden
  
  (defn.js UseListenSingleDemo
    []
    (let [[active setActive]  (r/local 1)
          ind    (a/useBinaryIndicator
                  active
                  {:default {:type "timing"
                             :duration 200
                             :easing a/linear}})
          textRef (a/useListenSingle
                     ind
                     (fn [ind]
                       (return
                        {:text (j/toFixed ind 2)})))]
      (return
       (n/EnclosedCode 
{:label "js.react-native.animate/useListenSingle"} 
[:% n/Row
         [:% n/Button
          {:title "PUSH"
           :onPress (fn [] (setActive (not active)))}]
         [:% n/Fill]
         [:% n/TextInput {:ref textRef
                          :editable false
                          :style {:padding 5
                                  :width 100
                                  :textAlign "right"}}]])))))

^{:refer js.react-native.animate/listenArray :added "4.0"}
(fact "listen to ref for array"
  ^:hidden
  
  (defn.js ListenArrayDemo
    []
    (let [[active setActive]  (r/local 1)
          ind    (a/useBinaryIndicator
                  active
                  {:default {:type "timing"
                             :duration 200
                             :easing a/linear}})
          ind2    (a/derive (fn [v] (return (- (* 2 v))))
                            [ind])
          textRef (r/ref)
          _       (r/init []
                    (a/listenArray
                     textRef
                     [ind ind2]
                     (fn [v v2]
                       (return
                        {:text
                         (+ "[" (j/toFixed v 2)
                            ", " (j/toFixed v2 2)
                            "]")}))))]
      (return
       (n/EnclosedCode 
{:label "js.react-native.animate/listenArray"} 
[:% n/Row
         [:% n/Button
          {:title "PUSH"
           :onPress (fn [] (setActive (not active)))}]
         [:% n/Fill]
         [:% n/TextInput {:ref textRef
                          :editable false
                          :style {:padding 5
                                  :width 100
                                  :textAlign "right"}
                          }]])))))

^{:refer js.react-native.animate/useListenArray :added "4.0"}
(fact "creates a ref as well as animated value listeners"
  ^:hidden
  
  (defn.js UseListenArrayDemo
    []
    (let [[active setActive]  (r/local 1)
          ind    (a/useBinaryIndicator
                  active
                  {:default {:type "timing"
                             :duration 200
                             :easing a/linear}})
          [index setIndex]  (r/local 1)
          ind2    (a/useIndexIndicator
                  index
                  {:default {:type "timing"
                             :duration 200
                             :easing a/linear}})
          textRef (a/useListenArray
                   [ind ind2]
                   (fn [v v2]
                     (return
                      {:text
                       (+ "[" (j/toFixed v 2)
                          ", " (j/toFixed v2 2)
                          "]")})))]
      (return
       (n/EnclosedCode 
{:label "js.react-native.animate/useListenArray"} 
[:% n/Row
         [:% n/Text
          {:style {:width 300}}
          [:% n/Button
           {:title "P"
            :color "red"
           :onPress (fn [] (setActive (not active)))}]
         [:% n/Text "  "]
          [:% n/Button
           {:title "1"
            :color (:? (== index 1) "black")
            :onPress (fn []
                       (setIndex 1))}]
          [:% n/Text "  "]
          [:% n/Button
           {:title "2"
            :color (:? (== index 2) "black")
            :onPress (fn []
                       (setIndex 2))}]]
         [:% n/Fill]
         [:% n/TextInput {:ref textRef
                          :editable false
                          :style {:padding 5
                                  :width 100
                                  :textAlign "right"}}]])))))

^{:refer js.react-native.animate/listenMap :added "4.0"}
(fact "listen to ref for map"
  ^:hidden
  
  (defn.js ListenMapDemo
    []
    (let [[active setActive]  (r/local 1)
          ind    (a/useBinaryIndicator
                  active
                  {:default {:type "timing"
                             :duration 200
                             :easing a/linear}})
          [index setIndex]  (r/local 1)
          ind2    (a/useIndexIndicator
                  index
                  {:default {:type "timing"
                             :duration 200
                             :easing a/linear}})
          textRef (r/ref)
          _       (r/init []
                    (a/listenMap
                     textRef
                     {:ind ind
                      :ind2 ind2}
                     (fn [#{ind ind2}]
                       (return
                        {:text
                         (+ "[" (j/toFixed ind 2)
                            ", " (j/toFixed ind2 2)
                            "]")}))))]
      (return
       (n/EnclosedCode 
{:label "js.react-native.animate/listenMap"} 
[:% n/Row
         [:% n/Text
          {:style {:width 300}}
          [:% n/Button
           {:title "P"
            :color "red"
           :onPress (fn [] (setActive (not active)))}]
          [:% n/Text "  "]
          [:% n/Button
           {:title "1"
            :color (:? (== index 1) "black")
            :onPress (fn []
                       (setIndex 1))}]
          [:% n/Text "  "]
          [:% n/Button
           {:title "2"
            :color (:? (== index 2) "black")
            :onPress (fn []
                       (setIndex 2))}]]
         [:% n/Fill]
         [:% n/TextInput {:ref textRef
                          :editable false
                          :style {:padding 5
                                  :width 100
                                  :textAlign "right"}}]])))))

^{:refer js.react-native.animate/listenTransformations :added "4.0"}
(fact "listens to a transformation"
  ^:hidden
  
  (defn.js ListenTransformationsDemo
    []
    (let [[active setActive]  (r/local 1)
          ind    (a/useBinaryIndicator
                  active
                  {:default {:type "timing"
                             :duration 200
                             :easing a/linear}})
          [index setIndex]  (r/local 1)
          ind2    (a/useIndexIndicator
                   index
                   {:default {:type "timing"
                              :duration 200
                              :easing a/linear}})
          textRef (r/ref)
          _ (r/init []
              (a/listenTransformations
                       textRef
                       {:ind ind
                        :ind2 ind2}
                       (fn [#{ind ind2}]
                         (return
                          {:text
                           (+ "[" (j/toFixed ind 2)
                              ", " (j/toFixed ind2 2)
                              "]")}))
                       (fn:> {})))]
      (return
       (n/EnclosedCode 
{:label "js.react-native.animate/listenTransformations"} 
[:% n/Row
         [:% n/Text
          {:style {:width 300}}
          [:% n/Button
           {:title "P"
            :color "red"
            :onPress (fn [] (setActive (not active)))}]
          [:% n/Text "  "]
          [:% n/Button
           {:title "1"
            :color (:? (== index 1) "black")
            :onPress (fn []
                       (setIndex 1))}]
          [:% n/Text "  "]
          [:% n/Button
           {:title "2"
            :color (:? (== index 2) "black")
            :onPress (fn []
                       (setIndex 2))}]]
         [:% n/Fill]
         [:% n/TextInput {:ref textRef
                          :editable false
                          :style {:padding 5
                                  :width 100
                                  :textAlign "right"}}]])))))

^{:refer js.react-native.animate/runWithCancel :added "4.0"}
(fact "runs a function, cancelling the animation if too slow"
  ^:hidden
  
  (defn.js RunWithCancelDemo
    []
    (let [[active setActive]   (r/local true)
          #{indicator
            zero-fn
            one-fn
            check-fn}  (r/const (event-animate/make-binary-transitions
                                 a/IMPL
                                 active {:default {:type "timing"
                                                   :duration 1000
                                                   :easing a/linear}}))
          progressing (r/const (event-animate/new-progressing))
          boxRef   (a/useListenSingle
                    indicator
                    (fn [v]
                      (return
                       {:style
                        {:transform
                         [{:scaleX (* 100 (j/max 0.1 v))}]}})))
          textRef    (a/useListenSingle
                      indicator
                      (fn [v]
                        (return {:text (j/toFixed v 3)})))]
      (return
       (n/EnclosedCode 
{:label "js.react-native.animate/runWithCancel"} 
[:% n/Row
         [:% n/Button
          {:title "PUSH"
           :onPress (fn []
                      (setActive (not active))
                      (if active
                        (a/runWithCancel zero-fn progressing)
                        (a/runWithCancel one-fn progressing)))}]
         [:% n/View
           {:style {:alignItems "center"
                    :justifyContent "center"
                    :flex 1}}
           [:% n/View {:ref boxRef
                       :style [{:width 1.2
                                :height 20
                                :backgroundColor "red"}]}]]
         [:% n/TextInput {:ref textRef
                          :editable false
                          :style {:padding 5
                                  :width 100
                                  :textAlign "right"}}]])))))

^{:refer js.react-native.animate/runWithChained :added "4.0"}
(fact "runs with chained"
  ^:hidden
  
  (defn.js RunWithOneDemo
    []
    (let [[active setActive]   (r/local true)
          progressing          (r/const (event-animate/new-progressing))
          [progressView setProgressView] (r/local progressing)
          #{indicator
            zero-fn
            one-fn
            check-fn}   (r/const
                        (event-animate/make-binary-transitions
                         a/IMPL
                         active {:default {:type "timing"
                                           :duration 1000
                                           :easing a/linear}}))
          
          boxRef   (a/useListenSingle
                    indicator
                    (fn [v]
                      (return
                       {:style
                        {:transform
                         [{:scaleX (* 100 (j/max 0.1 v))}]}})))
          textRef    (a/useListenSingle
                      indicator
                      (fn [v]
                        (return {:text (j/toFixed v 3)})))]
      (return
       (n/EnclosedCode 
{:label "js.react-native.animate/runWithOne"} 
[:% n/Row
         [:% n/Button
          {:title "PUSH"
           :onPress (fn []
                      (setActive (not active))
                      (if active
                        (a/runWith "chained-one" zero-fn progressing
                                   (fn []
                                     (setProgressView #{(:.. progressing)})))
                        (a/runWith "chained-one" one-fn progressing
                                   (fn []
                                     (setProgressView #{(:.. progressing)}))))
                      (setProgressView progressing))}]
         [:% n/View
          {:style {:alignItems "center"
                   :justifyContent "center"
                   :flex 1}}
          [:% n/View {:ref boxRef
                      :style [{:width 1.2
                               :height 20
                               :backgroundColor "red"}]}]]
         [:% n/TextInput {:ref textRef
                          :editable false
                          :style {:padding 5
                                  :width 100
                                  :textAlign "right"}}]] 
[:% n/Text (n/format-entry #{progressView})])))))

^{:refer js.react-native.animate/runWith :added "4.0"}
  (fact "generic runWith function for animations"
  ^:hidden

  (defn.js RunWithAllDemo
    []
    (let [[active setActive]   (r/local true)
          progressing (r/const (event-animate/new-progressing))
          [progressView setProgressView] (r/local progressing)
          #{indicator
            zero-fn
            one-fn
            check-fn} (r/const
                       (event-animate/make-binary-transitions
                        a/IMPL
                        active {:default {:type "timing"
                                          :duration 1000
                                          :easing a/linear}}))
          
          boxRef   (a/useListenSingle
                    indicator
                    (fn [v]
                      (return
                       {:style
                        {:transform
                         [{:scaleX (* 100 (j/max 0.1 v))}]}})))
          textRef    (a/useListenSingle
                      indicator
                      (fn [v]
                        (return {:text (j/toFixed v 3)})))]
      (return
       (n/EnclosedCode 
{:label "js.react-native.animate/runWithAll"} 
[:% n/Row
         [:% n/Button
          {:title "PUSH"
           :onPress (fn []
                      (setActive (not active))
                      (if active
                        (a/runWith "chained-all" zero-fn progressing
                                   (fn []
                                     (setProgressView #{(:.. progressing)})))
                        (a/runWith "chained-all" one-fn progressing
                                   (fn []
                                     (setProgressView #{(:.. progressing)}))))
                      (setProgressView progressing))}]
         [:% n/View
          {:style {:alignItems "center"
                   :justifyContent "center"
                   :flex 1}}
          [:% n/View {:ref boxRef
                      :style [{:width 1.2
                               :height 20
                               :backgroundColor "red"}]}]]
         [:% n/TextInput {:ref textRef
                          :editable false
                          :style {:padding 5
                                  :width 100
                                  :textAlign "right"}}]] 
[:% n/Text (n/format-entry #{progressView})])))))

^{:refer js.react-native.animate/useProgess :added "4.0"}
(fact "creates a progress result and a progress function")

^{:refer js.react-native.animate/useBinaryIndicator :added "4.0"}
(fact "creates a binary indicator from state"
  ^:hidden
  
  (defn.js UseBinaryIndicatorDemo
    []
    (let [[active setActive]   (r/local true)
          ind    (a/useBinaryIndicator
                  active
                  {:default {:type "timing"
                             :duration 200
                             :width 100
                             :easing a/linear}})
          textRef (a/useListenArray
                   [ind]
                   (fn [v]
                     (return {:text (j/toFixed v 3)})))
          boxRef  (a/useListenArray
                   [ind]
                   (fn [v]
                     (return
                      {:style
                       {:transform
                        [{:scaleX (* 100 (j/max 0.1 v))}]}})))]
      (return
       (n/EnclosedCode 
{:label "js.react-native.animate/useBinaryIndicator"} 
[:% n/View
         [:% n/Row
          [:% n/Button
           {:title (:? active
                       "ON"
                       "OFF")
            :onPress (fn [] (setActive (not active)))}]
          
          [:% n/View
           {:style {:alignItems "center"
                    :justifyContent "center"
                    :flex 1}}
           [:% n/View {:ref boxRef
                       :style [{:width 1.2
                                :height 20
                                :backgroundColor "red"}]}]]
          [:% n/TextInput {:ref textRef
                           :editable false
                           :style {:padding 5
                                   :width 100
                                   :textAlign "right"}}]]])))))

^{:refer js.react-native.animate/usePressIndicator :added "4.0"}
(fact "accentuates the press"
  ^:hidden
  
  (defn.js UsePressIndicatorDemo
    []
    (let [[active setActive]   (r/local false)
          [progress setProgress] (r/local nil)
          ind    (a/usePressIndicator
                  active
                  {:default {:type "timing"
                             :duration 1000
                             :width 100
                             :easing a/linear}}
                  (fn [progress]
                    (setProgress #{(:.. progress)})))
          textRef (a/useListenArray
                   [ind]
                   (fn [v]
                     (return {:text (j/toFixed v 3)})))
          boxRef  (a/useListenArray
                   [ind]
                   (fn [v]
                     (return
                      {:style
                       {:transform
                        [{:scaleX (* 100 (j/max 0.1 v))}]}})))]
      (return
       (n/EnclosedCode 
{:label "js.react-native.animate/usePressIndicator"} 
[:% n/View
         [:% n/Row
          [:% n/Pressable
           {:onPressIn  (fn [] (setActive true))
            :onPressOut (fn [] (setActive false))}
           [:% n/Text
            {:style {:padding 10
                     :backgroundColor (:? active ["green" "black"])
                     :color "white"}}
            "PRESS"]]
          
          [:% n/View
           {:style {:alignItems "center"
                    :justifyContent "center"
                    :flex 1}}
           [:% n/View {:ref boxRef
                       :style [{:width 1.2
                                :height 20
                                :backgroundColor "red"}]}]]
          [:% n/TextInput {:ref textRef
                           :editable false
                           :style {:padding 5
                                   :width 100
                                   :textAlign "right"}}]]] 
[:% n/Text (k/json-encode progress)])))))

^{:refer js.react-native.animate/useLinearIndicator :added "4.0"}
(fact "uses the linear indicator"
  ^:hidden
  
  (defn.js UseLinearIndicatorDemo
    []
    (let [[index setIndex]  (r/local 1)
          [progress setProgress] (r/local nil)
          ind    (a/useLinearIndicator
                  index
                  {:default {:type "timing"
                             :duration 1000
                             :width 100
                             :easing a/linear}}
                  (fn [progress]
                    (setProgress #{(:.. progress)}))
                  "chained-all")
          boxRef  (a/useListenArray
                   [ind]
                   (fn [v]
                     (return
                      {:style
                       {:transform
                        [{:scaleX (* 20 (j/max 0.1 v))}]}})))
          textRef (a/useListenArray
                   [ind]
                   (fn [v]
                     (return {:text (j/toFixed v 3)})))]
      (return
       (n/EnclosedCode 
{:label "js.react-native.animate/useLinearIndicator"} 
[:% n/Row
         [:% n/Text
          [:% n/Button
           {:title "1"
            :color (:? (== index 1) "black")
            :onPress (fn []
                       (setIndex 1))}]
          [:% n/Text "  "]
          [:% n/Button
           {:title "2"
            :color (:? (== index 2) "black")
            :onPress (fn []
                       (setIndex 2))}]
          [:% n/Text "  "]
          [:% n/Button
           {:title "3"
            :color (:? (== index 3) "black")
            :onPress (fn []
                       (setIndex 3))}]]
         [:% n/View
          {:style {:alignItems "center"
                   :justifyContent "center"
                   :flex 1}}
          [:% n/View {:ref boxRef
                      :style [{:width 1.2
                               :height 20
                               :backgroundColor "green"}]}]]
         [:% n/TextInput {:ref textRef
                          :editable false
                          :style {:padding 5
                                  :width 100
                                  :textAlign "right"}}]] 
[:% n/Text (k/json-encode progress)])))))

^{:refer js.react-native.animate/useIndexIndicator :added "4.0"}
(fact "creates a index indicator from state"
  ^:hidden
  
  (defn.js UseIndexIndicatorDemo
    []
    (let [[index setIndex]  (r/local 1)
          ind    (a/useIndexIndicator
                  index
                  {:default {:type "timing"
                             :duration 500
                             :width 100
                             :easing a/linear}})
          boxRef  (a/useListenArray
                   [ind]
                   (fn [v]
                     (return
                      {:style
                       {:transform
                        [{:scaleX (* 20 (j/max 0.1 v))}]}})))
          textRef (a/useListenArray
                   [ind]
                   (fn [v]
                     (return {:text (j/toFixed v 3)})))]
      (return
       (n/EnclosedCode 
{:label "js.react-native.animate/useIndexIndicator"} 
[:% n/Row
         [:% n/Text
          [:% n/Button
           {:title "1"
            :color (:? (== index 1) "black")
            :onPress (fn []
                       (setIndex 1))}]
          [:% n/Text "  "]
          [:% n/Button
           {:title "2"
            :color (:? (== index 2) "black")
            :onPress (fn []
                       (setIndex 2))}]
          [:% n/Text "  "]
          [:% n/Button
           {:title "3"
            :color (:? (== index 3) "black")
            :onPress (fn []
                       (setIndex 3))}]]
         [:% n/View
          {:style {:alignItems "center"
                   :justifyContent "center"
                   :flex 1}}
          [:% n/View {:ref boxRef
                      :style [{:width 1.2
                               :height 20
                               :backgroundColor "green"}]}]]
         [:% n/TextInput {:ref textRef
                          :editable false
                          :style {:padding 5
                                  :width 100
                                  :textAlign "right"}}]])))))

^{:refer js.react-native.animate/useCircularIndicator :added "4.0"}
(fact "constructs a circular indicator"
  ^:hidden
  
  (defn.js UseCircularIndicatorDemo
    []
    (let [[index setIndex]  (r/local 10)
          [progress setProgress] (r/local nil)
          ind    (a/useCircularIndicator
                  index
                  {:default {:type "timing"
                             :duration 2000
                             :easing a/linear}}
                  (fn [progress]
                    (setProgress #{(:.. progress)}))
                  "chained-all")
          boxRef  (a/useListenArray
                   [ind]
                   (fn [v]
                     (return
                      {:style
                       {:transform
                        [{:rotateZ (+ v "deg")}]}})))
          textRef (a/useListenArray
                   [ind]
                   (fn [v]
                     (return {:text (j/toFixed v 3)})))]
      (return
       (n/EnclosedCode 
{:label "js.react-native.animate/useCircularIndicator"} 
[:% n/Row
         [:% n/Text
          [:% n/Button
           {:title "60"
            :color (:? (== index 60) "black")
            :onPress (fn []
                       (setIndex 60))}]
          [:% n/Text "  "]
          [:% n/Button
           {:title "120"
            :color (:? (== index 120) "black")
            :onPress (fn []
                       (setIndex 120))}]
          [:% n/Text "  "]
          [:% n/Button
           {:title "180"
            :color (:? (== index 180) "black")
            :onPress (fn []
                       (setIndex 180))}]

          [:% n/Text "  "]
          [:% n/Button
           {:title "240"
            :color (:? (== index 240) "black")
            :onPress (fn []
                       (setIndex 240))}]

          [:% n/Text "  "]
          [:% n/Button
           {:title "300"
            :color (:? (== index 300) "black")
            :onPress (fn []
                       (setIndex 300))}]

          [:% n/Text "  "]
          [:% n/Button
           {:title "360"
            :color (:? (== index 360) "black")
            :onPress (fn []
                       (setIndex 360))}]
          ]
         [:% n/View
          {:style {:alignItems "center"
                   :justifyContent "center"
                   :flex 1}}
          [:% n/View {:ref boxRef
                      :style [{:width 40
                               :height 40
                               :backgroundColor "green"
                               :borderBottom "3px solid black"}]}]]
         [:% n/TextInput {:ref textRef
                          :editable false
                          :style {:padding 5
                                  :width 100
                                  :textAlign "right"}}]] 
[:% n/Text (k/json-encode progress)])))))

^{:refer js.react-native.animate/usePosition :added "4.0"}
(fact "constructs position indicator for slider and pan"
  ^:hidden
  
  (defn.js UsePositionDemo
    []
    (var [value setValue]  (r/local (fn:> 5)))
    (var #{position
           forwardFn
           reverseFn}  (a/usePosition {:length 100
                                       :max 10
                                       :min 0
                                       :step 0.01
                                       :value value
                                       :setValue setValue}))
    (var boxRef  (a/useListenSingle
                  position
                  (fn [v]
                    (return
                     {:style
                      {:transform
                       [{:translateX v}]}}))))
    (var textRef (a/useListenSingle
                  position
                  (fn [v]
                    (return {:text (j/toFixed v 3)}))))
    (return
     (n/EnclosedCode 
{:label "js.react-native.animate/usePosition"} 
[:% n/Row
       [:% n/Button
        {:title "+1"
         :onPress (fn:> (a/setValue position (+ (. position _value)
                                                5)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "-1"
         :onPress (fn:> (a/setValue position (- (. position _value)
                                                5)))}]
       [:% n/View
        {:style {:alignItems "center"
                 :justifyContent "center"
                 :flex 1}}
        [:% n/View
         {:style [{:width  1
                   :height 20
                   :backgroundColor "green"
                   :transform
                   [{:scaleX (* 10 value)}]}]}]]
       [:% n/TextInput {:ref textRef
                        :editable false
                        :style {:padding 5
                                :width 100
                                :textAlign "right"}}]] 
[:% n/Text (k/json-encode #{value position})]))))

^{:refer js.react-native.animate/useRange :added "4.0"}
(fact "constructs lower and upper bound indicator for range"
  ^:hidden
  
  (defn.js UseRangeDemo
    []
    (var [upper setUpper]  (r/local (fn:> 7)))
    (var [lower setLower]  (r/local (fn:> 2)))
    (var #{positionUpper
           positionLower
           forwardFn
           reverseFn}  (a/useRange {:length 100
                                    :max 10
                                    :min 0
                                    :step 0.01
                                    :lower lower
                                    :setLower setLower
                                    :upper upper
                                    :setUpper setUpper}))
    (var boxLowerRef  (a/useListenSingle
                  positionLower
                  (fn [v]
                    (return
                     {:style
                      {:transform
                       [{:translateX v}]}}))))
    (var textLowerRef (a/useListenSingle
                  positionLower
                  (fn [v]
                    (return {:text (j/toFixed v 3)}))))
    (var boxUpperRef  (a/useListenSingle
                  positionUpper
                  (fn [v]
                    (return
                     {:style
                      {:transform
                       [{:translateX v}]}}))))
    (var textUpperRef (a/useListenSingle
                  positionUpper
                  (fn [v]
                    (return {:text (j/toFixed v 3)}))))
    (return
     (n/EnclosedCode 
{:label "js.react-native.animate/useRange"} 
[:% n/Row
       [:% n/Button
        {:title "-l"
         :onPress (fn:> (a/setValue positionLower
                                    (- (. positionLower _value)
                                       5)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "+l"
         :onPress (fn:> (a/setValue positionLower
                                    (+ (. positionLower _value)
                                       5)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "-h"
         :onPress (fn:> (a/setValue positionUpper
                                    (- (. positionUpper _value)
                                       5)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "+h"
         :onPress (fn:> (a/setValue positionUpper
                                    (+ (. positionUpper _value)
                                       5)))}]
       [:% n/View
        {:style {
                 :justifyContent "center"
                 :flex 1}}
        [:% n/View
         {:style [{:width  (* 10 (- upper lower))
                   :height 20
                   :backgroundColor "green"
                   :transform
                   [{:translateX (+ 100 (* 10 lower))}]}]}]]
       [:% n/TextInput {:ref textLowerRef
                        :editable false
                        :style {:padding 5
                                :width 100
                                :textAlign "right"}}]
       [:% n/TextInput {:ref textUpperRef
                        :editable false
                        :style {:padding 5
                                :width 100
                                :textAlign "right"}}]] 
[:% n/Text (k/json-encode #{upper lower})]))))

^{:refer js.react-native.animate/useShowing :added "4.0"}
(fact "constructs a function that removes"
  ^:hidden
  
  (defn.js UseShowingDemo
    []
    (var isMounted  (r/useIsMounted))
    (var [visible setVisible]  (r/local (fn:> true)))
    (var [showing
          vindicator] (a/useShowing visible
                                    {:default {:type "timing"
                                               :duration 500
                                               :easing a/linear}}
                                    isMounted))
    (var boxRef  (a/useListenSingle
                  vindicator
                  (fn [v]
                    (return
                     {:style
                      {:transform
                       [{:scaleX (* 100 (j/max 0.1 v))}]}}))))
    (var textRef (a/useListenSingle
                  vindicator
                  (fn [v]
                    (return {:text (j/toFixed v 3)}))))
    (return
     (n/EnclosedCode 
{:label "js.react-native.animate/useShowing"} 
[:% n/Row
       [:% n/Button
        {:title "Toggle"
         :onPress (fn:> (setVisible (not visible)))}]
       [:% n/View
        {:style {:alignItems "center"
                 :justifyContent "center"
                 :flex 1}}
        (:? showing 
            [[:% n/View {:ref boxRef
                         :key "showing"
                         :style [{:width 1
                                  :height 20
                                  :backgroundColor "green"}]}]])]
       [:% n/TextInput {:ref textRef
                        :editable false
                        :style {:padding 5
                                :width 100
                                :textAlign "right"}}]] 
[:% n/Text (k/json-encode #{visible showing})])))

  (def.js MODULE (!:module)))

(comment
  (!.js
   (REF.current.setNativeProps
    {:text "HELLO"}))

  (!.js
   (REF.current.setNativeProps
    {:style {:backgroundColor "red"}}))

  (!.js
   (REF.current.setNativeProps
    {:text "hello"})))
