(ns js.blessed.layout
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.react :as r]
             [js.lib.chalk :as chalk]]})

(def.js primaryNormal
  {:hover {:fg "black"
           :bg "white"
           :bold false}
   :bold false
   :fg "white"
   :bg "black"})
   
(def.js primarySelected
  {:hover {:bg "gray"
           :fg "yellow"
           :bold true}
   :bold true
   :bg "black"
   :fg "yellow"})

(def.js toggleEngaged
  {:bg "black"
   :fg "magenta"
   :bold false})

(def.js toggleSelected
  {:bg "white"
   :fg "black"
   :hover {:bg "black"
           :fg "white"
           :bold true}
   :bold true})

(def.js toggleNormal
  {:bg "black"
   :fg "white"
   :hover {:bg "gray"
           :fg "white"}
   :bold false})

(defn.js PrimaryButton
  "creates a primary layout button"
  {:added "4.0"}
  ([#{[label
       index
       selected
       route
       setRoute
       refLink
       (:.. rprops)]}]
   (let [content (+ (chalk/inverse (+ " " index " ")) "  " label  "  ")]
     (return
      [:button #{[:ref refLink
                  :shrink true
                  :mouse true
                  :keys true
                  :content content
                  :style   (:? selected -/primarySelected -/primaryNormal)
                  :onClick (fn []
                             (setRoute route))
                  (:.. rprops)]}]))))

(defn.js layoutMenu
  "layout for menu entry"
  {:added "4.0"}
  ([items]
   (let [entries (j/filter items (fn:> [e] (:? (k/arr? e.hidden) (not (e.hidden)) (not e.hidden))))
         lens     (j/map entries (fn:> [e] (k/len e.label)))
         lefts    (j/reduce lens
                            (fn [acc l]
                               (j/push acc (+ (k/last acc) l 8))
                               (return acc))
                             [0])]
     (return (j/map entries (fn [e i]
                               (let [name  (or e.route (j/toLowerCase e.label))
                                     left  (. lefts [i])
                                     width (- (. lefts [(+ i 1)])
                                              left)]
                                 (return #{...e name left width}))))))))

(defn.js layoutToggles
  "layout for toggle entry"
  {:added "4.0"}
  ([items]
   (let [entries (j/filter items (fn:> [e] (:? (k/fn? e.hidden) (not (e.hidden)) (not e.hidden))))
         lens     (j/map entries (fn:> [e] (:? (== e.type "separator") 1 3)))
         lefts    (j/reduce lens
                             (fn [acc l]
                               (j/push acc (+ (k/last acc) l))
                               (return acc))
                             [0])]
     (return (j/map entries (fn [e i]
                               (let [left  (. lefts [i])
                                     width (- (. lefts [(+ i 1)])
                                              left)]
                                 (return #{...e left width}))))))))

(defn.js PrimaryMenu
  "creates a primary menu"
  {:added "4.0"}
  ([#{[entries
       route
       setRoute
       (:.. rprops)]}]
   (var box (r/ref nil))
   (r/init []
     (. (r/curr box)
        (onScreenEvent "keypress"
                       (fn [_ key]
                         (let [e (-> entries
                                     (j/filter 
                                      (fn [e]
                                        (return (== e.index key.name))))
                                     (k/first))]
                           (when e
                             (setRoute e.route))))))
     (return (fn []
               (. (r/curr box) (free)))))
   (return
    [:box #{[:ref box
             :shrink true
             :style {:bg "black"}
             (:.. rprops)]}
     (j/map entries (fn [e] (return
                             [:% -/PrimaryButton #{[:key e.route
                                                    :selected (== route e.route)
                                                    setRoute
                                                    (:.. e)]}])))])))

(defn.js PrimaryToggle
  "creates a primary toggle"
  {:added "4.0"}
  [#{[active
      setActive
      label
      (:.. rprops)]}]
  (return
   [:button #{[:shrink true
               :mouse true
               :keys true
               :width 3
               :style (:? active -/toggleSelected -/toggleNormal)
               :content (+ " " label " ")
               :onClick (fn []
                          (if setActive
                            (setActive (not active))))
               (:.. rprops)]}]))

(defn.js PrimaryToggles
  "creates primary toggles"
  {:added "4.0"}
  [#{[entries
      (:.. rprops)]}]
  (let [width (j/reduce entries (fn:> [acc e] (+ acc e.width)) 0)]
    (return
     [:box #{[:shrink true
              :style {:bg "red"}
              :width width
              (:.. rprops)]}
      (j/map entries (fn [e i]
                        (return
                         (:? (== e.type "separator") [:box #{[:key i :style {:bg "black"} (:.. e)]}] [:% -/PrimaryToggle #{[:key i (:.. e)]}]))))])))

(defn.js SecondaryButton
  "creates a secondary button"
  {:added "4.0"}
  ([#{[label
       index
       setIndex
       selected
       noIndex
       refLink
       (:.. rprops)]}]
   (let [colorFn (:? selected chalk/yellow chalk/bold)
         content (colorFn 
                  (+ "" (:? (not noIndex) (:? selected (chalk/inverse (+ " " index " ")) (+ " " index " ")) "")
                     " " label))]
     (return
      [:button #{[:ref refLink
                  :width 26
                  ;; :right 0
                  :mouse  true
                  :keys   true
                  :shrink true
                  :border  {}
                  :content content
                  :style   {:bg "black"
                            :border {:fg "black"
                                     :bg "black"}}
                  :onClick (fn [] (setIndex index))
                  (:.. rprops)]}]))))

(defn.js SecondaryMenu
  "creates a secondary button"
  {:added "4.0"}
  ([#{[items
       label
       (:= index 1)
       noIndex
       setIndex
       menuContent
       menuFooter]}]
   (let [_ (:= items (:? (j/isArray items) items (j/keys items)))
         entries (j/map items
                         (fn [e i]
                           (return (j/assign
                                    {:top   (+ 2 (* i 2))
                                     :index (+ i 1)}
                                    e))))
         box (r/ref nil)
         MenuContent menuContent
         MenuFooter menuFooter]
     (r/run []
       (when (not noIndex)
         (. (r/curr box)
            (onScreenEvent "keypress"
                           (fn [_ key]
                             (let [i   (j/parseInt key.full)
                                   sel (j/filter entries
                                                  (fn:> [e] (== e.index i)))]
                               
                               (when (and sel (< 0 (k/len sel)))
                                 (setIndex i))))))
         (return (fn []
                   (. (r/curr box) (free))))))
     (return
      [:box {:ref box
             :top  0
             :width 26
             :height "100%"
             ;; :right 0
             :shrink true
             :scrollable true
             :style {:bold true
                     :bg "black"}}
       (j/map entries (fn [e]
                         (return
                          [:% -/SecondaryButton
                           #{[:key e.index
                              setIndex
                              noIndex
                              :selected (== index e.index)
                              (:.. e)]}])))
       [:box {:style {:bold true
                      :bg "black"
                      :fg "white"}
              :align "left"
              :top 0
              :height 1
              :shrink true
              :width "100%"
              :content (chalk/inverse (chalk/yellow (+ " " (j/toUpperCase label) " ")))}]
       (:? MenuContent [:box {:left 1 :right 1 :style {:bg "black"} :top (+ 4 (* 2 (k/len items)))} [:% MenuContent]])
       (:? MenuFooter [:box {:bottom 0 :height 2 :right 0 :style {:bg "black"}} [:% MenuFooter]])]))))

(defn.js LayoutHeaderBlock
  "constructs the header block"
  {:added "4.0"}
  [#{children}]
  (return [:box {:top 0
                 :left 0
                 :right 0
                 :height 1
                 :bg "black"}
           children]))

(defn.js LayoutFooterBlock
  "constructs the footer block"
  {:added "4.0"}
  [#{children}]
  (return [:box {:bottom 0
                 :left 0
                 :right 0
                 :height 1
                 :bg "black"}
           children]))

(defn.js LayoutBodyBlock
  "constructs the body block"
  {:added "4.0"}
  [#{children}]
  (return [:box {:top 1
                 :bottom 1
                 :width "100%"}
           children]))

(defn.js BlankRoute
  "constructs a blank Route Page"
  {:added "4.0"}
  [#{route}]
  (return [:box (+ "Missing: " route)]))

(defn.js LayoutBody
  "constructs the body"
  {:added "4.0"}
  ([#{label
      items
      index
      setIndex
      menuWidth
      menuHide
      menuContent
      menuFooter
      console
      consoleHeight}]
   (let [#{view name props} (. items [(- index 1)])
         Component (or view -/BlankRoute)
         rightOffset  (:? (not menuHide)
                          (or menuWidth 28))
         bottomOffset (:? console
                          (or consoleHeight 20)
                          1)
         Console  console]
     (return
      [:% -/LayoutBodyBlock
       [:box
        {:right 0
         :height "100%"
         :width rightOffset
         :bg "black"}
        (:? (not menuHide) [:box {:bg "black" :left 1} [:% -/SecondaryMenu {:index index :items items :label label :menuContent menuContent :menuFooter menuFooter :setIndex setIndex}]])]
       [:box
        {:top 1
         :left 1
         :bottom (+ bottomOffset 1)
         :right (+ rightOffset 1)}
        [:% r/Try
           {:fallback (fn [] (return [:box "ERRORED"]))}
           [:% Component {:route name}]]]
       (:? Console [:box {:bg "black" :bottom 0 :height bottomOffset} [:% Console]])]))))

(defn.js LayoutStatus
  "constructs the status line"
  {:added "4.0"}
  [#{[busy
      setBusy
      (:= status {:content ""
                  :type "info"})
      setStatus
      autoClear
      (:.. rprops)]}]
  (let [#{content type} status
        width  (j/min [(:? content (k/len content) 0)
                        50])
        clearFn (fn:> (setStatus {:content ""
                                :type "info"}))]
    (r/init []
      (when autoClear
        (let [id (j/delayed [2500]
                   (setStatus {:content ""
                               :type "info"}))]
          (return (fn:> (clearTimeout id))))))
    (return [:box #{[:height 1
                     :shrink true
                     :bg "black"
                     (:.. rprops)]}
             [:button {:style (:? busy {:bg "yellow" :bold true :fg "gray"} {:bg "gray" :bold true :fg "white"})
                       :left 0 :width 3
                       :mouse true
                       :on-click (fn [] (setBusy false))
                       :content (:? busy " ! " " * ")}]
             (:? content [:button {:content (+ " " content " ") :left 3 :mouse true :on-click (fn [] (setStatus {:content "" :type "info"})) :style {:bg (or (. {:error "yellow" :info "black" :warn "yellow"} [type]) "blue") :bold (not= type "info") :fg (:? (== type "info") "white" "black")}}])])))

(defn.js LayoutNotify
  "constructs the notification panel"
  {:added "4.0"}
  ([#{[setNotify
       (:= notify {:content ""
                   :type "info"
                   :show false
                   :layout {}})]}]
   (let [#{[type
            content
            show
            (:= layout {})]} notify
         style (:? (== type "error") {:bg "red" :fg "white"} {:bg "blue" :bold true :border {:bg "blue" :fg "white"} :fg "white"})]
     (return
      [:box #{[:transparent true
               :draggable true
               :mouse true
               :hidden (not show)
               :width  60
               :height 20
               :style style
               (:.. layout)]}
       [:box {:left 1 :right 1
              :transparent true
              :height 8
              :scrollable true
              :mouse true
              :keys true
              :style style
              :content content}]
       [:button {:width 3
                 :height 3
                 :top 0
                 :right 0
                 :inputOnFocus true
                 :mouse true
                 :keys true
                 :on-click (fn []
                             (setNotify
                              {:content ""
                               :type "info"
                               :show false
                               :layout {}}))
                 :style (k/set-in (k/clone-nested style)
                                  ["hover"]
                                  {:bg "white"
                                   :fg "grey"})
                 :content " x "}]]))))

(defn.js LayoutHeader
  "constructs the header"
  {:added "4.0"}
  [#{route
     setRoute
     header}]
  (let [items   (. header ["menu"])
        entries (-/layoutMenu items)]
    (return
     [:% -/LayoutHeaderBlock
      [:% -/PrimaryMenu
       #{route
         setRoute
         entries}]])))

(defn.js LayoutFooter
  "constructs the footer"
  {:added "4.0"}
  [#{footer
     busy
     setBusy
     status
     setStatus
     route
     setRoute
     header}]
  (let [items    (or (. footer ["menu"]) [])
        ientries (-/layoutMenu items)
        toggles  (or (. footer ["toggle"]) [])
        tentries (-/layoutToggles toggles)
        ioffset  (j/reduce tentries
                            (fn:> [acc e] (+ acc e.width))
                            0)
        soffset  (j/reduce ientries
                            (fn:> [acc e] (+ acc e.width))
                            ioffset)]
    (return
     [:% -/LayoutFooterBlock
      
      [:% -/PrimaryMenu
       #{route
         setRoute
         {:entries ientries
          :right ioffset}}]
      [:% -/LayoutStatus
       #{busy
         setBusy
         status
         setStatus
         {:right soffset
          :left 0}}]
      [:% -/PrimaryToggles
       {:entries tentries
        :right 0}]])))

(defn.js LayoutMain
  "constructs the main page"
  {:added "4.0"}
  ([#{[(:= header  {:menu []
                    :toggle nil
                    :user  nil})
       (:= footer  {:menu []
                    :toggle nil})
       init
       route
       setRoute
       index
       setIndex
       sections
       status
       setStatus
       busy
       setBusy
       notify
       setNotify
       menuWidth
       menuContent
       menuFooter
       menuHide
       console
       consoleHeight]}]
   (let [[__route            
          __setRoute]     (:? setRoute [route setRoute] (r/local init))
         [__index
          __setIndex]      (:? setIndex [index setIndex] (r/local 0))
         section           (j/assign {}
                                     (. sections
                                        [(or __route init)])
                                     {:route (or __route init)
                                      :index (or __index 1)
                                      :setIndex __setIndex})
         [__status
          __setStatus]      (:? setStatus [status setStatus] (r/local (or status {})))
         [__busy
          __setBusy]        (:? setBusy [busy setBusy] (r/local busy))
         [__notify
          __setNotify]      (:? setNotify [notify setNotify] (r/local (or notify {})))]
     (return
      ^MAIN
      [:box

       ^HEADER
       [:% -/LayoutHeader
        #{[:route (or __route init)
           :setRoute __setRoute
           header]}]
       
       ^BODY
       [:% -/LayoutBody #{menuContent
                          menuHide
                          menuWidth
                          menuFooter
                          console
                          consoleHeight
                          (:.. section)}]
       
       ^FOOTER
       [:% -/LayoutFooter
        #{[:route (or __route init)
           :setRoute __setRoute
           :setStatus __setStatus
           :status __status
           :setBusy __setBusy
           :busy __busy
           footer]}]
       
       ^NOTIFY
       [:% -/LayoutNotify
        {:notify __notify
         :setNotify __setNotify}]]))))
