(ns js.react-native.react-test
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]
            [js.cell.playground :as browser]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/web-main
            #_#_:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [xt.lang.base-lib :as k]]
   :export [MODULE]})

^{:refer js.react/useRefresh :adopt true :added "4.0" :unchecked true}
(fact "performs refresh function"
  ^:hidden
  
  (defn.js UseRefreshDemo
    []
    (var refresh (r/useRefresh))
    (var getCount (r/useGetCount))
    (return
     (n/EnclosedCode 
{:label "js.react/useRefresh"} 
[:% n/Row
       [:% n/Button
        {:title "Refresh"
         :onPress refresh}]
       [:% n/Padding {:style {:flex 1}}]
       [:% n/Text (+ "Count: " (getCount))]]))))

^{:refer js.react/useGetCount :adopt true :added "4.0" :unchecked true}
(fact "ref counter"
  ^:hidden
  
  (defn.js UseGetCountDemo
    []
    (var [valA setValA] (r/local (fn:> 0)))
    (var [valB setValB] (r/local (fn:> 0)))
    (var getCount (r/useGetCount))
    (return
     (n/EnclosedCode 
{:label "js.react/useGetCount"} 
[:% n/Row
       [:% n/Button
        {:title   "IncA"
         :onPress (fn:> (setValA (+ valA 1)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title   "IncB"
         :onPress (fn:> (setValB (+ valB 1)))}]
       [:% n/Padding {:style {:flex 1}}]
       [:% n/Text (+ "count: " (getCount)
                     " valA: " valA
                     " valB: " valB)]]))))

^{:refer js.react/useFollowRef :adopt true :added "4.0" :unchecked true}
(fact "ref follower"
  ^:hidden
  
  (defn.js UseFollowRefDemo
    []
    (var [val setVal] (r/local (fn:> 0)))
    (var valRef (r/useFollowRef val))
    (return
     (n/EnclosedCode 
{:label "js.react/useFollowRef"} 
[:% n/Row
       [:% n/Button
        {:title   "Inc"
         :onPress (fn:> (setVal (+ val 1)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title   "Alert"
         :onPress (fn:> (alert (+ "valRef: " (r/curr valRef))))}]
       [:% n/Padding {:style {:flex 1}}]
       [:% n/Text (+ "valRef: " (r/curr valRef)
                     " val: " val)]]))))


^{:refer js.react/useIsMounted :adopt true :added "4.0" :unchecked true}
(fact "checks if component is mounted"
  ^:hidden

  (defn.js IsMountedPane
    [#{callback
       setMounted}]
    (var isMounted (r/useIsMounted))
    (r/init []
      (callback (isMounted))
      (return (fn []
                (callback (isMounted)))))
    (return
     [:% n/Button
      {:title "Unmount"
       :onPress (fn:> (setMounted false))}]))
  
  (defn.js UseIsMountedDemo
    []
    (var [mounted setMounted] (r/local (fn:> false)))
    (var [on setOn]   (r/local 0))
    (var [off setOff] (r/local 0))
    (var callback (fn [mounted]
                    (if mounted
                      (do (setOn  (+ 1 on)))
                      (do (setOff (+ 1 off))))))
    (return
     (n/EnclosedCode 
{:label "js.react/useIsMounted"} 
[:% n/Row
       (:? mounted
           [[:% n/Row
             (:? mounted [[:% -/IsMountedPane
                           #{callback
                             setMounted}]])]
            [:% n/Button
             {:title   (+ "Mount")
              :onPress (fn:> (setMounted true))}]])
       [:% n/Padding {:style {:flex 1}}]
       [:% n/Text (+ "On: " on " Off: " off)]]))))

^{:refer js.react/useMountedCallback :adopt true :added "4.0" :unchecked true}
(fact "performs a callback when mounted"

  (defn.js MountedCallbackPane
    [#{setOff
       off
       setOn
       on
       setMounted}]
    (r/useMountedCallback
     (fn [mounted]
       (if mounted
         (do (setOn  (+ 1 on)))
         (do (setOff (+ 1 off))))))
    (return
     [:% n/Button
      {:title "Unmount"
       :onPress (fn:> (setMounted false))}]))
  
  (defn.js UseMountedCallbackDemo
    []
    (var [mounted setMounted] (r/local (fn:> false)))
    (var [on setOn]   (r/local 0))
    (var [off setOff] (r/local 0))
    (return
     (n/EnclosedCode 
{:label "js.react/useMountedCallback"} 
[:% n/Row
       (:? mounted
           [[:% n/Row
             (:? mounted [[:% -/MountedCallbackPane
                           #{setOff
                             off
                             setOn
                             on
                             setMounted}]])]
            [:% n/Button
             {:title   (+ "Mount")
              :onPress (fn:> (setMounted true))}]])
       [:% n/Padding {:style {:flex 1}}]
       [:% n/Text (+ "On: " on " Off: " off)]]))))

^{:refer js.react/useFollowDelayed :adopt true :added "4.0" :unchecked true}
(fact "ref follower after delay"
  ^:hidden
  
  (defn.js UseFollowDelayedDemo
    []
    (var [val setVal] (r/local (fn:> 0)))
    (var isMounted (r/useIsMounted))
    (var [valDelay] (r/useFollowDelayed val 500 isMounted))
    (return
     (n/EnclosedCode 
{:label "js.react/useFollowDelayed"} 
[:% n/Row
       [:% n/Button
        {:title   "Inc"
         :onPress (fn:> (setVal (+ val 1)))}]
       [:% n/Text " "]
       [:% n/Padding {:style {:flex 1}}]
       [:% n/Text (+ "valDelay: " valDelay
                     " val: " val)]]))))

^{:refer js.react/useInterval :adopt true :added "4.0" :unchecked true}
(fact "performs a task at a given iterval"
  ^:hidden
  
  (defn.js IntervalPane
    [#{setMounted}]
    (var [val setVal] (r/local (fn:> 100)))
    (var [up setUp]   (r/local (fn:> true)))
    (var [index setIndex]   (r/local (fn:> 0)))
    (var choices [300 1000 nil])
    (var ms (. choices [(mod index 3)]))
    
    (var upFn   (fn []
                  (setVal (+ val 1))))
    (var downFn (fn []
                  (setVal (- val 1))))
    (var #{stopInterval
           startInterval} (r/useInterval (:? up upFn downFn)
                                         ms))
    (return
     [:% n/Row
      {:style {:marginVertical 5}}
      [:% n/Button
       {:title (+ "Duration " ms)
        :onPress (fn []
                   (setIndex (+ 1 index))
                   (when ms
                     (setUp (not up))))}]
      [:% n/Text " "]
      [:% n/Button
       {:title (:? up "Down" "Up")
        :onPress (fn:> (setUp (not up)))}]

      [:% n/Text " "]
      [:% n/Button
       {:title "Start"
        :onPress startInterval}]

      [:% n/Text " "]
      [:% n/Button
       {:title "Stop"
        :onPress stopInterval}]
      [:% n/Padding {:style {:flex 1}}]
      [:% n/Text (+ "value: " val)]]))
  
  (defn.js UseIntervalDemo
    []
    (var [mounted setMounted] (r/local false))
    (return
     (n/EnclosedCode 
{:label "js.react/useInterval"} 
[:% n/Row
       [:% n/Button
        {:title (:? mounted "Hide" "Show")
         :onPress (fn:> (setMounted (not mounted)))}]] 
(:? mounted [[:% -/IntervalPane
                    {:key "interval"}]])))))

^{:refer js.react/useTimeout :adopt true :added "4.0" :unchecked true}
(fact  "performs a task at a given iterval"
  ^:hidden
  
  (defn.js TimeoutPane
    [#{setMounted}]
    (var [val setVal] (r/local (fn:> 100)))
    (var [up setUp]   (r/local (fn:> true)))
    (var [index setIndex]   (r/local (fn:> 0)))
    (var choices [300 1000 nil])
    (var ms (. choices [(mod index 3)]))
    
    (var upFn   (fn []
                  (setVal (+ val 1))))
    (var downFn (fn []
                  (setVal (- val 1))))
    (var #{stopTimeout
           startTimeout} (r/useTimeout (:? up upFn downFn)
                                         ms))
    (return
     [:% n/Row
      {:style {:marginVertical 5}}
      [:% n/Button
       {:title (+ "Duration " ms)
        :onPress (fn []
                   (setIndex (+ 1 index))
                   (when ms
                     (setUp (not up))))}]
      [:% n/Text " "]
      [:% n/Button
       {:title (:? up "Down" "Up")
        :onPress (fn:> (setUp (not up)))}]

      [:% n/Text " "]
      [:% n/Button
       {:title "Start"
        :onPress startTimeout}]

      [:% n/Text " "]
      [:% n/Button
       {:title "Stop"
        :onPress stopTimeout}]
      [:% n/Padding {:style {:flex 1}}]
      [:% n/Text (+ "value: " val)]]))
  
  (defn.js UseTimeoutDemo
    []
    (return
     (n/EnclosedCode 
{:label "js.react/useTimeout"} 
[:% -/TimeoutPane]))))


^{:refer js.react/useCountdown :adopt true :added "4.0" :unchecked true}
(fact "countdown value every second"
  ^:hidden

  (defn.js CountdownPane
    []
    (var [current
          setCurrent
          #{stopCountdown
            startCountdown}] (r/useCountdown 100
                                             nil
                                             {:interval 300}))
    (return
     [:% n/Row
      {:style {:marginVertical 5}}
      [:% n/Button
       {:title "Reset"
        :onPress (fn:> (setCurrent 100))}]
      
      [:% n/Text " "]
      [:% n/Button
       {:title "Start"
        :onPress startCountdown}]

      [:% n/Text " "]
      [:% n/Button
       {:title "Stop"
        :onPress stopCountdown}]
      [:% n/Padding {:style {:flex 1}}]
      [:% n/Text (+ "value: " current)]]))
  
  (defn.js UseCountdownDemo
    []
    (var [mounted setMounted] (r/local false))
    (return
     (n/EnclosedCode 
{:label "js.react/useCountdown"} 
[:% n/Row
       [:% n/Button
        {:title (:? mounted "Hide" "Show")
         :onPress (fn:> (setMounted (not mounted)))}]] 
(:? mounted [[:% -/CountdownPane
                    {:key "countdown"}]])))))

^{:refer js.react/useNow :adopt true :added "4.0" :unchecked true}
(fact "uses the current time"
  ^:hidden

  (defn.js NowPane
    []
    (var [current
          #{stopNow
            startNow}] (r/useNow 300))
    (return
     [:% n/Row
      {:style {:marginVertical 5}}
      [:% n/Button
       {:title "Start"
        :onPress startNow}]

      [:% n/Text " "]
      [:% n/Button
       {:title "Stop"
        :onPress stopNow}]
      [:% n/Padding {:style {:flex 1}}]
      [:% n/Text (+ "value: " current)]]))
  
  (defn.js UseNowDemo
    []
    (var [mounted setMounted] (r/local false))
    (return
     (n/EnclosedCode 
{:label "js.react/useNow"} 
[:% n/Row
       [:% n/Button
        {:title (:? mounted "Hide" "Show")
         :onPress (fn:> (setMounted (not mounted)))}]] 
(:? mounted [[:% -/NowPane
                    {:key "now"}]])))))

^{:refer js.react/useChanging :adopt true :added "4.0" :unchecked true}
(fact "uses value and setValue that may be influenced by available data"
  ^:hidden

  (defn.js UseChangingDemo
    []
    (var [data setData] (r/local ["A" "B" "C"]))
    (var [value setValue] (r/useChanging data))
    (return
     (n/EnclosedCode 
{:label "js.react/useChanging"} 
[:% n/Row
       [:% n/Button
        {:title "ABC"
         :onPress (fn:> (setData ["A" "B" "C"]))}]
       [:% n/Button
        {:title "XYZ"
         :onPress (fn:> (setData ["X" "Y" "Z"]))}]
       [:% n/Fill]
       [:% n/Tabs
        #{data value setValue}]]))))


^{:refer js.react/useTree :adopt true :added "4.0" :unchecked true}
(fact "tree function helper"
  ^:hidden
  
  (defn.js UseTreeDemo
    []
    (var #{branch
           setBranch
           branches
           view}  (r/useTree {:tree {:a {:b {:c 1}}
                                     :x {:y {:z 2}}}
                              :targetFn  (fn [tree branch _parents _root]
                                           (return (k/get-key tree branch)))
                              :displayFn (fn [target branch parents root]
                                           (return
                                            [:% n/TextDisplay
                                             {:content (n/format-entry
                                                        #{target branch parents root})}]))}))
    (return
     (n/EnclosedCode 
{:label "js.react/useTree"} 
[:% n/Row {:style {:marginVertical 5}}
       [:% n/Button
        {:title "a"
         :onPress (fn:> (setBranch "a"))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "x"
         :onPress (fn:> (setBranch "x"))}]] 
view 
[:% n/Row {:style {:padding 5}}] 
[:% n/TextDisplay
       {:content (n/format-entry #{branch branches})}])))
  
  (def.js MODULE
    (do (:# (!:uuid))
        (!:module)))
  )
