(ns js.blessed.ui-group
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :js
  {:runtime :basic
   :config {:id :playground/tui-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}}
   :require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.react   :as r]
             [js.blessed :as b]
             [js.blessed.ui-style :as ui-style]]})

(defn.js useTree
  "wrapper for `js.react/useTree`"
  {:added "4.0"}
  [#{tree
     root
     parents
     initial
     setInitial
     branchesFn
     targetFn
     formatFn
     displayFn}]
  (:= formatFn  (or formatFn (fn:> [v] (j/inspect v {:colors true}))))
  (:= displayFn (or displayFn
                    (fn:> [target _branch _parents _root]
                      [:box {:content (formatFn target)}])))
  (return (r/useTree #{tree
                       root
                       parents
                       initial
                       setInitial
                       branchesFn
                       targetFn
                       formatFn
                       displayFn})))

(defn.js layoutTabs
  "layout tabs for horizontal and vertical"
  {:added "4.0"}
  [items layout height format]
  (var lefts
       (:? (== layout "vertical")
           (j/fill (Array (k/len items))
                   0)
           (-> items
               (j/reduce (fn [acc item]
                           (let [prev (k/last acc)
                                 curr (+ prev
                                         1
                                         (. (format item) ["length"]))]
                             (acc.push curr)
                             (return acc)))
                         [0]))))
  (var tops
       (:? (== layout "vertical")
         (j/map items
                (fn:> [e i]
                  (* i (or height 1))))
         (j/fill (Array (k/len items))
                 0)))
  (return [lefts tops]))

(defn.js EnumMultiIndexed
  "Constructs EnumMultiIndexed"
  {:added "4.0"}
  ([props]
   (var #{[items
           color
           layout
           (:= height 1)
           (:= setIndices (fn:>))
           (:= indices [])
           (:= format k/identity)]} props)
   (var [lefts tops] (-/layoutTabs items layout height format))
   (var tprops (ui-style/getTopProps props))
   (return [:box #{[(:.. tprops)]}
            (j/map items
                   (fn [item i]
                     (var text (format item))
                     (return
                      [:button {:key item
                                :style (:? (. indices [i]) (ui-style/styleSmall color) ui-style/styleInvertDisabled)
                                :left  (. lefts [i])
                                :top   (. tops [i])
                                :width (k/len text)
                                :height height
                                :shrink true
                                :mouse true
                                :onClick (fn []
                                           (setIndices
                                            (j/map indices
                                                   (fn:> [e ei] (:? (== ei i) (not e) e)))))
                                :content text}])))])))

(defn.js EnumMulti
  "Constructs EnumMultiIndexed"
  {:added "4.0"}
  ([#{[data
       valueFn
       values
       setValues
       (:.. lprops)]}]
   (var #{setIndices
          items
          indices} (r/convertIndices #{data
                                       valueFn
                                       values
                                       setValues}))
   
   (return [:% -/EnumMultiIndexed #{[setIndices
                                     indices
                                     items
                                     (:.. lprops)]}])))

(defn.js TabsView
  "Constructs Tabs"
  {:added "4.0"}
  ([props]
   (let [#{items
           color
           onChange
           layout
           checkIndex
           setIndex
           index
           format
           height} props
         [internal setInternal] (r/local (or index 0))
         _ (if (not format) (:= format k/identity))
         [lefts tops] (-/layoutTabs items layout height format)
         tprops (ui-style/getTopProps props)
         _ (r/run []
            (if (and (k/is-number? index)
                     (not= internal index))
              (setInternal index)))]
     (return [:box #{[:mouse true
                      :scrollable true
                      :scrollbar ui-style/styleScrollBar
                      (:.. tprops)]}
              (j/map items
                     (fn [item i]
                       (var text (format item))
                       (return
                        [:button {:key item
                                  :style (:? (and (== i internal)
                                                  (:? checkIndex
                                                      (checkIndex i)
                                                      true))
                                             (ui-style/styleSmall color)
                                             ui-style/styleInvertDisabled)
                                  :left  (. lefts [i])
                                  :top   (. tops [i])
                                  :width (k/len text)
                                  :height (or height 1)
                                  :shrink true
                                  :mouse true
                                  :onClick (fn []
                                              (setInternal i)
                                              (if setIndex (setIndex i))
                                              (if onChange (onChange (k/get-key items i))))
                                  :content text}])))]))))

(defn.js Tabs
  "Construct Tabs"
  {:added "4.0"}
  ([#{[data
       valueFn
       value
       setValue
       (:.. lprops)]}]
   (var #{setIndex
          items
          index} (r/convertIndex #{data
                                   valueFn
                                   value
                                   setValue}))
   
   (return [:% -/TabsView #{[setIndex
                             items
                             index
                             (:.. lprops)]}])))

(defn.js TabsVPane
  "constructs a tabs pane"
  {:added "4.0"}
  [#{[tabsFormat
      tree
      parents
      root
      initial
      setInitial
      branchesFn
      targetFn
      formatFn
      displayFn
      color
      layout
      width
      (:.. rprops)]}]
  (var tprops (ui-style/getTopProps rprops))
  (var #{branch
         setBranch
         branches
         view}  (-/useTree #{tree
                             parents
                             root
                             initial
                             setInitial
                             branchesFn
                             targetFn
                             formatFn
                             displayFn}))
  (return
   [:box #{[width
            (:.. tprops)]}
    [:% -/Tabs
     #{[width 
        layout
        color
        :value branch
        :setValue (fn [k]
                    (setBranch k))
        :data branches
        :format tabsFormat]}]
    [:box {:top 1}
     [:box {:top 1}
      view]]]))

(defn.js TabsHPane
  "Constructs a List"
  {:added "4.0"}
  [#{[tree
      parents
      root
      initial
      setInitial
      branchesFn
      targetFn
      formatFn
      displayFn
      color
      layout
      height
      (:= width 15)
      (:= tabsFormat k/identity)
      (:.. rprops)]}]
  (var tprops (ui-style/getTopProps rprops true))
  (var #{branch
         setBranch
         branches
         view}  (-/useTree #{tree
                             parents
                             root
                             initial
                             setInitial
                             branchesFn
                             targetFn
                             formatFn
                             displayFn}))
  (return
   [:box #{[:height height
            (:.. tprops)]}
    [:% -/Tabs
     #{[:width width
        :layout "vertical"
        :color color
        :value branch
        :setValue (fn [k]
                    (setBranch k))
        :data branches
        :format (fn:> [v] (j/padEnd (tabsFormat v) width " "))]}]
    [:box {:left width}
     [:box {:left 1}
      view]]]))

;;
;; LIST
;;

(defn.js ListView
  "Constructs a ListView"
  {:added "4.0"}
  ([props]
   (let [#{items
           color
           onChange
           index
           setIndex
           initial
           format
           height
           proxy} props
         _ (if (not format)  (:= format k/identity))
         _ (:= proxy (or proxy (r/ref)))
         tprops (ui-style/getTopProps props)
         handler (fn [e selected]
                   (if setIndex (setIndex selected))
                   (if onChange (onChange selected)))
         [init] (r/useStep (fn [setDone]
                             (. (r/curr proxy)
                                (select (or initial 0)))
                             (setDone true)))]
     (r/watch [init]
       (when init
         (. (r/curr proxy)
            (on "action" handler))))
     (return [:box #{(:.. tprops)}
              [:list {:ref proxy
                      :height height
                      :interactive true
                      :mouse true
                      :scrollable true
                      :keys true
                      :inputOnFocus true
                      :items (k/arr-map items format)
                      :style (ui-style/styleListView color)}]]))))

(defn.js List
  "Constructs a List"
  {:added "4.0"}
  ([#{[data
       valueFn
       value
       setValue
       (:.. lprops)]}]
   (var #{setIndex
          items
          index} (r/convertIndex #{data
                                   valueFn
                                   value
                                   setValue}))
   (return [:% -/ListView
            #{[:key value
               :initial index
               setIndex
               items
               index
               (:.. lprops)]}])))

(defn.js ListPane
  "Constructs a List"
  {:added "4.0"}
  [#{[tree
      parents
      root
      initial
      setInitial
      branchesFn
      targetFn
      formatFn
      displayFn
      color
      layout
      height
      (:= width 15)
      (:= listFormat k/identity)
      (:.. rprops)]}]
  (var tprops (ui-style/getTopProps rprops true))
  (var #{branch
         setBranch
         branches
         view}  (-/useTree #{tree
                             parents
                             root
                             initial
                             setInitial
                             branchesFn
                             targetFn
                             formatFn
                             displayFn}))
  (return
   [:box #{[:height height
            (:.. tprops)]}
    [:box {:width width
           :height height
           :style {:bg "black"}}
     [:% -/List
      #{[:width width
         :layout "vertical"
         :color color
         :value (or branch (k/first branches))
         :setValue (fn [k]
                     (setBranch k))
         :data branches
         :format (fn:> [v] (j/padEnd (listFormat v) width " "))]}]]
    [:box {:left width}
     [:box {:left 1}
      view]]]))

(defn.js TreePane
  "constructs a tree pane"
  {:added "4.0"}
  [#{[tree
      (:= root tree)
      (:= parents [])
      levels
      (:.. rprops)]}]
  (var tprops (ui-style/getTopProps rprops true))
  (when (k/is-empty? levels)
    (return [:box "NO DATA"]))
  (var [level (:.. more)] levels)
  (var #{type} level)
  (var Pane (:? (== type "list")
                -/ListPane

                (or (== type "tabs")
                    (== type "tabsV"))
                -/TabsVPane

                (== type "tabsH")
                -/TabsHPane

                :else
                -/ListPane))
  (var isFinal (== 1 (k/len levels)))
  (when isFinal
    (return [:% Pane #{[tree
                        root
                        parents
                        (:.. level)]}]))
  
  (var formatFn k/identity)
  (var displayFn (fn [newTree branch parents root]
                   (return [:% -/TreePane
                            {:key branch
                             :tree newTree
                             :root root
                             :parents [(:.. parents) branch]
                             :levels more}])))
  (return [:box #{[(:.. tprops)]}
           [:% Pane #{[tree
                       root
                       parents
                       displayFn
                       (:.. level)]}]]))

(defn.js displayTarget
  "helper function for display"
  {:added "4.0"}
  [Target]
  (if (k/nil? Target)
    (return [:box])
    (return [:% Target])))
