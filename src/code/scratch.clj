(ns badge-creator
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :require [[js.react :as r]
             [js.lib.radix :as rx]
             [xt.lang.base-lib :as k]
             [lib.rewards :as rewards]]
   :import  [["lucide-react" :as #{Award ArrowLeft Info}]]
   :export [MODULE]})

(def.js criteriaTypes
  [{:id "task_completion", :label "Task Completion", :description "Complete X tasks"}
   {:id "points_earned", :label "Points Earned", :description "Earn X tokens"}
   {:id "market_wins", :label "Market Wins", :description "Win X prediction markets"}
   {:id "streak", :label "Login Streak", :description "Login X consecutive days"}
   {:id "referrals", :label "Referrals", :description "Refer X users"}
   {:id "custom", :label "Custom", :description "Define custom criteria"}])

(def.js rarities
  [{:id "common", :label "Common", :color "#b4b4b4", :description "Easy to earn"}
   {:id "rare", :label "Rare", :color "#60a5fa", :description "Moderate challenge"}
   {:id "epic", :label "Epic", :color "#a78bfa", :description "Significant achievement"}
   {:id "legendary", :label "Legendary", :color "#fbbf24", :description "Ultimate achievement"}])

(def.js emojiSuggestions
  ["🌟", "🏆", "💎", "🔥", "⭐", "🎯", "✨", "👑", "🥇", "🎖️", "💪", "🚀"])

(defn.js BadgeCreator [#{onBack}]
  (var [badgeData setBadgeData] (r/useState
                                 {:name ""
                                  :description ""
                                  :icon "🏆"
                                  :rarity "common"
                                  :criteriaType "task_completion"
                                  :criteriaValue ""
                                  :customCriteria ""}))

  (defn.js handleChange [field value]
    (return (setBadgeData (fn [prev] (return (k/obj-assign {} prev {field value}))))))

  (var selectedRarity (k/arr-find -/rarities (fn [r] (return (== (. r id) (. badgeData rarity))))))

  (return
    [:div {:className "h-full overflow-auto p-3 md:p-6"}
     ;; Header
     [:div {:className "mb-4 md:mb-6"}
      [:% rx/Button
       {:variant "ghost"
        :size "sm"
        :onClick onBack
        :className "text-[#6d6d6d] dark:text-[#b4b4b4] hover:text-black dark:hover:text-white hover:bg-[#f5f5f7] dark:hover:bg-[#2d2d2d] mb-4"}
       [:% ArrowLeft {:className "h-4 w-4 mr-2"}]
       "Back to Badges"]
      [:div {:className "flex items-center gap-3 mb-2"}
       [:div {:className "w-10 h-10 bg-gradient-to-br from-[#fbbf24] to-[#f472b6] rounded flex items-center justify-center"}
        [:% Award {:className "h-5 w-5 text-white"}]]
       [:div
        [:h2 {:className "text-xl md:text-2xl text-black dark:text-white"} "Create New Badge"]
        [:p {:className "text-xs md:text-sm text-[#6d6d6d] dark:text-[#b4b4b4]"} "Set up an achievement badge for users to earn"]]]]

     [:div {:className "grid grid-cols-1 lg:grid-cols-3 gap-4 md:gap-6"}
      ;; Form
      [:div {:className "lg:col-span-2 space-y-4 md:space-y-6"}
       ;; Basic Information
       [:% rx/Card {:className "bg-white dark:bg-[#1f1f1f] border-[#d0d0d0] dark:border-[#2d2d2d] p-4 md:p-6"}
        [:h3 {:className "text-black dark:text-white mb-4"} "Basic Information"]
        [:div {:className "space-y-4"}
         [:div
          [:% rx/Label {:className "text-[#6d6d6d] dark:text-[#b4b4b4] mb-2"} "Badge Name *"]
          [:% rx/TextField
           {:placeholder "e.g., Task Master"
            :value (. badgeData name)
            :onChange (fn [e] (return (-/handleChange "name" (. e target value))))
            :className "bg-white dark:bg-[#0a0a0a] border-[#d0d0d0] dark:border-[#2d2d2d] text-black dark:text-white"}]]

         [:div
          [:% rx/Label {:className "text-[#6d6d6d] dark:text-[#b4b4b4] mb-2"} "Description *"]
          [:% rx/TextArea
           {:placeholder "Describe what users need to do to earn this badge..."
            :value (. badgeData description)
            :onChange (fn [e] (return (-/handleChange "description" (. e target value))))
            :className "bg-white dark:bg-[#0a0a0a] border-[#d0d0d0] dark:border-[#2d2d2d] text-black dark:text-white min-h-24"}]]

         [:div
          [:% rx/Label {:className "text-[#6d6d6d] dark:text-[#b4b4b4] mb-2"} "Badge Icon *"]
          [:div {:className "space-y-3"}
           [:div {:className "flex items-center gap-3"}
            [:div
             {:className "w-12 h-12 md:w-16 md:h-16 rounded-full flex items-center justify-center text-2xl md:text-3xl"
              :style {:backgroundColor (+ (. selectedRarity color) "20")
                      :border (+ "2px solid " (. selectedRarity color))}}
             (. badgeData icon)]
            [:% rx/TextField
             {:placeholder "Enter emoji"
              :value (. badgeData icon)
              :onChange (fn [e] (return (-/handleChange "icon" (. e target value))))
              :className "bg-white dark:bg-[#0a0a0a] border-[#d0d0d0] dark:border-[#2d2d2d] text-black dark:text-white flex-1"
              :maxLength 2}]]
           [:div {:className "flex flex-wrap gap-2"}
            (k/arr-map -/emojiSuggestions
                       (fn [emoji]
                         (return
                           [:button
                            {:key emoji
                             :onClick (fn [] (return (-/handleChange "icon" emoji)))
                             :className "w-10 h-10 bg-white dark:bg-[#0a0a0a] border border-[#d0d0d0] dark:border-[#2d2d2d] rounded hover:border-[#4772b3] transition-colors text-xl"}
                            emoji])))]]]]

       ;; Rarity
       [:% rx/Card {:className "bg-white dark:bg-[#1f1f1f] border-[#d0d0d0] dark:border-[#2d2d2d] p-4 md:p-6"}
        [:h3 {:className "text-black dark:text-white mb-4"} "Rarity Level"]
        [:div {:className "grid grid-cols-2 lg:grid-cols-4 gap-3"}
         (k/arr-map -/rarities
                    (fn [rarity]
                      (return
                        [:button
                         {:key (. rarity id)
                          :onClick (fn [] (return (-/handleChange "rarity" (. rarity id))))
                          :className (+ "p-3 md:p-4 rounded border-2 transition-all "
                                        (:? (== (. badgeData rarity) (. rarity id))
                                            "bg-opacity-10"
                                            "border-[#d0d0d0] dark:border-[#2d2d2d] bg-[#f5f5f7] dark:bg-[#0a0a0a] hover:border-[#4772b3]"))
                          :style {:borderColor (:? (== (. badgeData rarity) (. rarity id)) (. rarity color) undefined)
                                  :backgroundColor (:? (== (. badgeData rarity) (. rarity id)) (+ (. rarity color) "10") undefined)}}
                         [:div
                          {:className "w-6 h-6 md:w-8 md:h-8 rounded-full mx-auto mb-2"
                           :style {:backgroundColor (. rarity color)}}]
                         [:div {:className "text-xs md:text-sm text-black dark:text-white mb-1"} (. rarity label)]
                         [:div {:className "text-xs text-[#6d6d6d] dark:text-[#b4b4b4]"} (. rarity description)]])))]

       ;; Criteria
       [:% rx/Card {:className "bg-white dark:bg-[#1f1f1f] border-[#d0d0d0] dark:border-[#2d2d2d] p-4 md:p-6"}
        [:h3 {:className "text-black dark:text-white mb-4"} "Earning Criteria"]
        [:div {:className "space-y-4"}
         [:div
          [:% rx/Label {:className "text-[#6d6d6d] dark:text-[#b4b4b4] mb-2"} "Criteria Type *"]
          [:select
           {:value (. badgeData criteriaType)
            :onChange (fn [e] (return (-/handleChange "criteriaType" (. e target value))))
            :className "w-full px-3 py-2 bg-white dark:bg-[#0a0a0a] border border-[#d0d0d0] dark:border-[#2d2d2d] rounded text-black dark:text-white text-sm"}
           (k/arr-map -/criteriaTypes
                      (fn [type]
                        (return
                          [:option {:key (. type id) :value (. type id)}
                           (+ (. type label) " - " (. type description))])))]]

         (:? (!= (. badgeData criteriaType) "custom")
             [:div
              [:% rx/Label {:className "text-[#6d6d6d] dark:text-[#b4b4b4] mb-2"} "Required Amount *"]
              [:% rx/TextField
               {:type "number"
                :placeholder "e.g., 50"
                :value (. badgeData criteriaValue)
                :onChange (fn [e] (return (-/handleChange "criteriaValue" (. e target value))))
                :className "bg-white dark:bg-[#0a0a0a] border-[#d0d0d0] dark:border-[#2d2d2d] text-black dark:text-white"}]
              [:p {:className "text-xs text-[#6d6d6d] mt-1"}
               (:? (== (. badgeData criteriaType) "task_completion") "Number of tasks to complete"
                   (:? (== (. badgeData criteriaType) "points_earned") "Number of tokens to earn"
                       (:? (== (. badgeData criteriaType) "market_wins") "Number of markets to win"
                           (:? (== (. badgeData criteriaType) "streak") "Number of consecutive days"
                               (:? (== (. badgeData criteriaType) "referrals") "Number of users to refer"
                                   nil)))))]]
             [:div
              [:% rx/Label {:className "text-[#6d6d6d] dark:text-[#b4b4b4] mb-2"} "Custom Criteria *"]
              [:% rx/TextArea
               {:placeholder "Describe the custom criteria for earning this badge..."
                :value (. badgeData customCriteria)
                :onChange (fn [e] (return (-/handleChange "customCriteria" (. e target value))))
                :className "bg-white dark:bg-[#0a0a0a] border-[#d0d0d0] dark:border-[#2d2d2d] text-black dark:text-white min-h-24"}]]]]]

      ;; Preview/Info
      [:div {:className "space-y-4 md:space-y-6"}
       ;; Preview Card
       [:% rx/Card {:className "bg-white dark:bg-[#1f1f1f] border-[#d0d0d0] dark:border-[#2d2d2d] p-4 md:p-6"}
        [:h3 {:className "text-black dark:text-white mb-4"} "Preview"]
        [:div {:className "p-4 md:p-6 bg-[#f5f5f7] dark:bg-[#0a0a0a] rounded border border-[#d0d0d0] dark:border-[#2d2d2d] text-center"}
         ;; Icon
         [:div
          {:className "w-20 h-20 md:w-24 md:h-24 rounded-full flex items-center justify-center text-4xl md:text-5xl mx-auto mb-4"
           :style {:backgroundColor (+ (. selectedRarity color) "20")
                   :border (+ "3px solid " (. selectedRarity color))}}
          (. badgeData icon)]

         ;; Name
         [:h3 {:className "text-black dark:text-white mb-2"}
          (:? (. badgeData name) (. badgeData name) "Badge Name")]

         ;; Description
         [:p {:className "text-xs text-[#6d6d6d] dark:text-[#b4b4b4] mb-4"}
          (:? (. badgeData description) (. badgeData description) "Badge description will appear here")]

         ;; Criteria
         [:div {:className "p-3 bg-white dark:bg-[#1f1f1f] rounded border border-[#d0d0d0] dark:border-[#2d2d2d] mb-4"}
          [:div {:className "text-xs text-[#6d6d6d] mb-1"} "How to earn"]
          [:div {:className "text-xs text-black dark:text-white"}
           (:? (k/and (!= (. badgeData criteriaType) "custom") (. badgeData criteriaValue))
               (r/fragment
                (:? (== (. badgeData criteriaType) "task_completion") (+ "Complete " (. badgeData criteriaValue) " tasks")
                    (:? (== (. badgeData criteriaType) "points_earned") (+ "Earn " (. badgeData criteriaValue) " tokens")
                        (:? (== (. badgeData criteriaType) "market_wins") (+ "Win " (. badgeData criteriaValue) " markets")
                            (:? (== (. badgeData criteriaType) "streak") (+ (. badgeData criteriaValue) " day login streak")
                                (:? (== (. badgeData criteriaType) "referrals") (+ "Refer " (. badgeData criteriaValue) " users")
                                    nil)))))
               (:? (k/and (== (. badgeData criteriaType) "custom") (. badgeData customCriteria))
                   (. badgeData customCriteria)
                   "Criteria will appear here"))]]

         ;; Rarity
         [:% rx/Badge
          {:variant "outline"
           :style {:color (. selectedRarity color) :borderColor (. selectedRarity color)}
           :className "text-xs capitalize"}
          (. selectedRarity label)]]

       ;; Info Card
       [:% rx/Card {:className "bg-[#fbbf24]/10 border-[#fbbf24] p-4"}
        [:div {:className "flex items-start gap-3"}
         [:% Info {:className "h-5 w-5 text-[#fbbf24] flex-shrink-0 mt-0.5"}]
         [:div
          [:div {:className "text-sm text-black dark:text-white mb-2"} "Badge Design Tips"]
          [:ul {:className "text-xs text-[#6d6d6d] dark:text-[#b4b4b4] space-y-1"}
           [:li "• Choose distinct emojis for each badge"]
           [:li "• Match rarity to difficulty"]
           [:li "• Set achievable but challenging goals"]
           [:li "• Clear criteria descriptions help users"]]]]]

       ;; Actions
       [:div {:className "space-y-3"}
        [:% rx/Button
         {:className "w-full text-white"
          :style {:backgroundColor (. selectedRarity color)
                  :opacity (:? (k/or (k/not (. badgeData name))
                                     (k/not (. badgeData description))
                                     (k/and (!= (. badgeData criteriaType) "custom") (k/not (. badgeData criteriaValue)))
                                     (k/and (== (. badgeData criteriaType) "custom") (k/not (. badgeData customCriteria))))
                               0.5
                               1)}
          :disabled (k/or (k/not (. badgeData name))
                          (k/not (. badgeData description))
                          (k/and (!= (. badgeData criteriaType) "custom") (k/not (. badgeData criteriaValue)))
                          (k/and (== (. badgeData criteriaType) "custom") (k/not (. badgeData customCriteria))))}
         [:% Award {:className "h-4 w-4 mr-2"}]
         "Create Badge"]
        [:% rx/Button
         {:variant "outline"
          :className "w-full border-[#d0d0d0] dark:border-[#2d2d2d] text-black dark:text-[#e0e0e0] hover:bg-[#f5f5f7] dark:hover:bg-[#2d2d2d] hover:text-black dark:hover:text-white"
          :onClick onBack}
         "Cancel"]]]]]))

(def.js MODULE (!:module))
