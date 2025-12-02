(ns js.core.style-test
  (:use code.test)
  (:require [js.core.style :refer :all]))

^{:refer js.core.style/rect :added "4.0" :unchecked true}
(fact "creates a rect"
  ^:hidden
  
  ((:template @rect) 4)
  => {:width 4, :height 4}

  ((:template @rect) 4 10)
  => {:width 4, :height 10})

^{:refer js.core.style/padding :added "4.0" :unchecked true}
(fact "creates a padding"
  ^:hidden
  
  ((:template @padding) 4)
  => {:paddingTop 4, :paddingRight 4,
      :paddingBottom 4, :paddingLeft 4})

^{:refer js.core.style/margin :added "4.0" :unchecked true}
(fact "creates a margin"
  ^:hidden
  
  ((:template @margin) 0 10)
  => {:marginTop 0, :marginRight 10, :marginBottom 0, :marginLeft 10})

^{:refer js.core.style/align :added "4.0" :unchecked true}
(fact "creates an alignment"
  ^:hidden
  
  ((:template @align) "center" "flex-end")
  => {:flex 1,
      :justifyContent "center",
      :alignItems "flex-end"})

^{:refer js.core.style/pos :added "4.0" :unchecked true}
(fact "creates a position"
  ^:hidden
  
  ((:template @pos) "absolute")
  => {:position "absolute"})

^{:refer js.core.style/fillAbsolute :added "4.0" :unchecked true}
(fact "creates a fill absolute style"
  ^:hidden
  
  ((:template @fillAbsolute))
  => {:position "absolute", :top 0, :bottom 0, :left 0, :right 0})

^{:refer js.core.style/centered :added "4.0" :unchecked true}
(fact "creates a centered object"
  ^:hidden
  
  ((:template @centered))
  => {:justifyContent "center", :alignContent "center", :alignItems "center"})

^{:refer js.core.style/hue->rgb :added "4.0" :unchecked true}
(fact "helper function for hsl->rbg")

^{:refer js.core.style/hsl->rgb-fn :added "4.0" :unchecked true}
(fact "hsl to rbg array"
  ^:hidden
  
  (hsl->rgb-fn {:h 230 :s 40 :l 40})
  => [61 75 143])

^{:refer js.core.style/hsl->rgb :added "4.0" :unchecked true}
(fact "converts hsl to rbg"
  ^:hidden
  
  (hsl->rgb {:h 230 :s 40 :l 40})
  => "\"#3d4b8f\"")

(comment
  (./import)
  (./ns:reset)
  

  ^{:refer js.core.style/rgb :added "4.0" :unchecked true}
  (fact "formats an rgb string"
    ^:hidden
    
    (rgb [1 3 4])
    => "\"rgb(\" + 1 + \",\" + 3 + \",\" + 4 + \")\"")
  
  ^{:refer js.core.style/rgba :added "4.0" :unchecked true}
  (fact "formats an rbga string"
    ^:hidden
    
    (rgba [1 3 4 0.8])
    => "\"rgba(\" + 1 + \",\" + 3 + \",\" + 4 + \",\" + 0.8 + \")\""))

