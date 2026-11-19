(ns app.nextjs.devtools
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.tamagui :as tm]
             [js.react :as r]
             [xt.lang.common-lib :as kl]
             [xt.lang.common-data :as kd]
             [xt.lang.common-string :as ks]
             [xt.lang.spec-base :as xt]
             [app.nextjs.devtools.ui :as ui]
             [app.nextjs.devtools.runtime :as runtime]]
   :import [["@tamagui/lucide-icons" :as
             #{FlaskConical Activity Wrench User LogOut LogIn Minus SquarePlus SquareMinus
               Monitor}]]})

(def.js FrameDevtoolContext (r/createContext))

(defn.js api-sign-in
  "Uses the application's Supabase client; no project account list or RPC schema is embedded."
  [client user]
  (return (runtime/sign-in client user)))

;;
;; Helpers
;;


(defn.js FrameDevtoolLayout
  "The layout component for the development tool interface."
  {:added "0.1"}
  [#{color children}]
  (return [:% tm/View
           {"aria-label" "Runtime devtools"
            :role "region"
            :opacity 0.95
            :position "fixed"
            :bottom 20
            :right 15
            :zIndex 1000
            :padding "3px"
            :borderRadius "$2"
            :backgroundColor color} children]))

(defn.js FrameDevtoolPopup
  "A popup component for displaying development tool options or information."
  {:added "0.1"}
  [#{icon label color children placement contentProps arrowProps}]
  (var [open setOpen] (r/useState false))
  (return [:% ui/PopoverMenu
           #{[:open open :onOpenChange setOpen :placement (or placement "bottom-start") :icon icon
              :size "$4" :button
              [:% ui/ButtonNormal
               {"aria-label" label :title label :icon icon :backgroundColor color :size "$2"}]
              :buttonProps {:size "$2"} :contentProps
              {:.. [contentProps]
               :backgroundColor color
               :borderWidth 0
               :gap "$1"
               :opacity 0.8
               :padding "$2"} :arrowProps
              {:.. [arrowProps] :backgroundColor color :borderWidth 0 :size "$3"}]} children]))

(defn.js FrameDevtoolButton
  "A button component for triggering development tool actions."
  {:added "0.1"}
  [{:# [children onPress] :.. [props]}]
  (return
   (r/createElement
    ui/ButtonNormal
    (Object.assign {} props {:fontSize "12px" :justifyContent "left" :onPress onPress :size "$2"})
    children)))

(defn.js FrameDevtoolIconButton
  "An icon button component for triggering development tool actions."
  {:added "0.1"}
  [{:# [children] :.. [props]}]
  (return (r/createElement
           ui/ButtonNormal
           (Object.assign {} props {:margin 0 :padding "3px" :size "$2" :width "24px"})
           children)))

;;
;; Users


(defn.js FrameDevtoolUserRow
  "A row component for displaying user information within the development tool."
  {:added "0.1"}
  [#{user}]
  (var #{controls page api} (r/useContext -/FrameDevtoolContext))
  (var backgroundColor "$color1")
  (var #{auth} user)
  (var isCurrent (== (kd/get-in controls.session ["user" "email"]) (kd/get-in user ["email"])))
  (var ncolor (:? isCurrent "$color12" "$color1"))
  (var nbackgroundColor (:? isCurrent "$color1" "$color12"))
  (return
   [:% tm/XStack
    {:alignItems "center"
     :flex 1
     :paddingHorizontal 2
     :borderRadius 4
     :backgroundColor nbackgroundColor
     :width "120px"}
    [:% -/FrameDevtoolIconButton
     {:icon (:? auth Minus)
      :color ncolor
      :backgroundColor nbackgroundColor
      :disabled (or (not auth) (not (. api capabilities deleteUser)))
      :onPress (fn []
                 (. (api.mutations.delete-testuser.mutateAsync {:id (. auth id)})
                    (then (fn []
                            (. page.toast
                               (show (ui/t "User Deleted")
                                     {:message (xt/x:json-encode
                                                (kd/obj-pick user ["email" "handle" "is_super"]))}))
                            (when isCurrent
                              (return (api.mutations.sign-out.mutateAsync)))))))}]
    [:% -/FrameDevtoolButton
     {:flex 1
      "aria-label" (+ "Sign in as " user.handle)
      :color ncolor
      :backgroundColor nbackgroundColor
      :disabled (or controls.busy
                    (not (. user password))
                    (and (not auth) (not (. api capabilities createUser))))
      :onPress
      (fn []
        (cond (not auth)
              (. (api.mutations.create-testuser.mutateAsync user)
                 (then (fn [res]
                         (when res.error
                           (throw res.error))
                         (. page.toast (show (ui/t "User Created") {:message (. user email)}))
                         (when (== (. user is-verified) false)
                           (return res))
                         (return (api.mutations.sign-in.mutateAsync user))))
                 (catch (fn [error]
                          (. page.toast
                             (show (ui/t "User Setup Failed")
                                   {:message
                                    (or error.message
                                        "Could not create or sign in to this test user.")})))))
              (not isCurrent)
              (. (api.mutations.sign-in.mutateAsync user)
                 (catch (fn [error]
                          (. page.toast
                             (show (ui/t "Sign In Failed")
                                   {:message (or error.message
                                                 "Check the test account credentials.")})))))))}
     (. user handle)]
    [:% -/FrameDevtoolIconButton
     {:icon Monitor
      :label "SharedWorker"
      :color ncolor
      :backgroundColor nbackgroundColor
      :onPress (fn [] (console.log (runtime/snapshot user)))}]]))

(defn.js FrameDevtoolUser
  "A component for managing and displaying user-related information within the development tool."
  {:added "0.1"}
  []
  (var #{controls api accounts} (r/useContext -/FrameDevtoolContext))
  (var users accounts)
  (var userError controls.error)
  (return
   [:% -/FrameDevtoolPopup
    {:icon User
     :label "Test accounts"
     :color controls.color
     :placement "left-end"
     :contentProps {:backgroundColor "$color12"}
     :arrowProps {:backgroundColor "$color12"}}
    [:% tm/YStack {:gap "$1"}
     (:? userError
         [:% tm/Text {:fontSize "8px" :color "$red8" :maxWidth "190px"}
          (+ "Could not load users: " userError)]
         nil)
     (. accounts
        (map (fn [account]
               (var auth-user (:? account.id {"id" account.id} account.auth))
               (var user (kd/obj-assign {} account))
               (:= user.auth auth-user)
               (:= user (kd/obj-assign user account))
               (var key (. account handle))
               (return [:% -/FrameDevtoolUserRow #{key user}]))))]]))

(defn.js FrameDevtoolSharedWorker
  "A popup inspector for the current SharedWorker connection."
  {:added "0.1" :public true}
  []
  (var #{controls sharedWorker} (r/useContext -/FrameDevtoolContext))
  (var state (kd/get-in sharedWorker ["state"]))
  (var connection (kd/get-in state ["connection"]))
  (var status (:? connection "connected" "disconnected"))
  (var transport-id (or (kd/get-in connection ["transport_id"]) "n/a"))
  (var transport-kind (or (kd/get-in connection ["transport" "meta" "kind"]) "n/a"))
  (var worker (or (kd/get-in connection ["ready" "worker"]) "n/a"))
  (var signal (or (kd/get-in connection ["ready" "signal"]) "n/a"))
  (var init-status (or (kd/get-in state ["init" "status"]) "n/a"))
  (return
   [:% -/FrameDevtoolPopup
    {:icon Monitor
     :label "SharedWorker"
     :color controls.color
     :placement "bottom-end"
     :contentProps {:alignItems "flex-start" :justifyContent "left" :minWidth "190px"}}
    [:% tm/YStack {:gap "$1"} [:% tm/Text {:fontFamily "monospace" :fontSize "8px"} "SharedWorker"]
     [:% tm/Text {:fontFamily "monospace" :fontSize "8px"} (+ "status: " status)]
     [:% tm/Text {:fontFamily "monospace" :fontSize "8px"} (+ "transport: " transport-kind)]
     [:% tm/Text {:fontFamily "monospace" :fontSize "8px"} (+ "transport-id: " transport-id)]
     [:% tm/Text {:fontFamily "monospace" :fontSize "8px"} (+ "worker: " worker)]
     [:% tm/Text {:fontFamily "monospace" :fontSize "8px"} (+ "signal: " signal)]
     [:% tm/Text {:fontFamily "monospace" :fontSize "8px"} (+ "init: " init-status)]
     [:% -/FrameDevtoolButton
      {:backgroundColor controls.color
       :onPress (fn [] (console.log "SHAREDWORKER" (runtime/snapshot sharedWorker)))} "Inspect"]]]))

(defn.js FrameDevtoolActivity
  "A component for displaying recent activity or logs within the development tool."
  {:added "0.1"}
  []
  (var context (r/useContext -/FrameDevtoolContext))
  (var #{controls} context)
  (return
   [:% -/FrameDevtoolPopup
    {:icon Activity
     :label "Inspect runtime"
     :color controls.color
     :placement "bottom-end"
     :contentProps {:alignItems "flex-start" :justifyContent "left"}}
    [:% -/FrameDevtoolButton
     {:backgroundColor controls.color
      :onPress (fn [] (console.log "G Store" (runtime/snapshot context.store)))} "G Store"]
    [:% -/FrameDevtoolButton
     {:backgroundColor controls.color
      :onPress (fn [] (console.log "G UI" (runtime/snapshot context.ui)))} "G UI"]
    [:% -/FrameDevtoolButton
     {:backgroundColor controls.color
      :onPress (fn [] (console.log "SESSION" (runtime/snapshot controls.session)))} "Auth Session"]
    [:% -/FrameDevtoolButton
     {:backgroundColor controls.color
      :onPress (fn [] (console.log "TOOL CONTEXT" (runtime/snapshot context)))} "Tool Context"]
    [:% -/FrameDevtoolButton
     {:backgroundColor controls.color :onPress (fn [] (console.log (runtime/snapshot context)))}
     "Dev All"]
    [:% -/FrameDevtoolButton
     {:backgroundColor controls.color
      :onPress (fn []
                 (:= (. globalThis DevContext) (runtime/snapshot context.context))
                 (console.log (runtime/snapshot context.context)))} "Dev Context"]
    [:% -/FrameDevtoolButton
     {:backgroundColor controls.color
      "data-devtool-action" "queries"
      :onPress (fn []
                 (:= (. globalThis DevQueries) (runtime/snapshot context.api.queries))
                 (console.log (. globalThis DevQueries)))} "Dev Queries"]
    [:% -/FrameDevtoolButton
     {:backgroundColor controls.color
      :onPress (fn []
                 (:= (. globalThis DevOutputs)
                     (kd/obj-map (runtime/snapshot context.api.queries) (kd/key-fn "output")))
                 (console.log (. globalThis DevOutputs)))} "Dev Outputs"]]))

(defn.js FrameDevtoolWrench
  "A component representing a wrench icon, typically used for settings or tools within the development panel."
  {:added "0.1"}
  []
  (var context (r/useContext -/FrameDevtoolContext))
  (var #{controls page} context)
  (var #{current setCurrent} (ui/useThemeGlobal))
  (return
   [:% -/FrameDevtoolPopup
    {:icon Wrench
     :label "Devtool actions"
     :color controls.color
     :placement "bottom"
     :contentProps {:alignItems "flex-start" :justifyContent "left"}}
    [:% -/FrameDevtoolButton
     {:disabled (not context.toastConfigured)
      :backgroundColor controls.color
      :onPress (fn []
                 (. page.toast
                    (show (ui/t "Ping Toast")
                          {:message (ui/t "This is an example of the ping toast")})))} "Ping Toast"]
    [:% -/FrameDevtoolButton
     {:disabled (not context.themeConfigured)
      :backgroundColor controls.color
      :onPress (fn [] (setCurrent (:? (== current "light") "dark" "light")))}
     (+ "Theme " (ks/capitalize current))]
    (. (or (. (r/useContext -/FrameDevtoolContext) actions) [])
       (map (fn [action]
              (return [:% -/FrameDevtoolButton {:key action.id :onPress action.run}
                       action.label]))))]))

(defn.js FrameDevtoolInfo
  "A component for displaying general information or statistics within the development tool."
  {:added "0.1"}
  []
  (var #{controls api page} (r/useContext -/FrameDevtoolContext))
  (var #{accounts} (r/useContext -/FrameDevtoolContext))
  (var dims (ui/useWindowDimensions))
  (var activeAccount
       (. accounts
          (find (fn [account]
                  (return (== account.email (kd/get-in controls.session ["user" "email"])))))))
  (var handle
       (or (:? activeAccount activeAccount.handle nil)
           (kd/get-in controls.session ["user" "user_metadata" "handle"])
           (kd/get-in controls.session ["user" "email"])
           "anon"))
  (return
   [:% tm/XStack {:padding "$1" :gap "$1"}
    [:% tm/YStack {:flex 1}
     [:% -/FrameDevtoolButton
      {:backgroundColor controls.color
       "aria-label" (:? controls.session "Sign out" "Sign in")
       :disabled (or controls.busy (and (not controls.session) (== accounts.length 0)))
       :width "90px"
       :onPress (fn []
                  (cond controls.session (api.mutations.sign-out.mutateAsync)
                        :else (. (api.mutations.sign-in.mutateAsync (. accounts [0]))
                                 (catch (fn [error]
                                          (. page.toast
                                             (show (ui/t "Sign In Failed")
                                                   {:message
                                                    (or
                                                     error.message
                                                     "Check the test account credentials.")})))))))}
      handle]]
    [:% tm/YStack [:% tm/Text {:fontFamily "monospace" :fontSize "8px"} (+ "w: " (. dims width))]
     [:% tm/Text {:fontFamily "monospace" :fontSize "8px"} (+ "h: " (. dims height))]]]))

;;
;; Context
;;

(defn.js useFrameDevtoolContext
  "Creates only the debugging context from a supplied Supabase client and app inspection bindings."
  [options]
  (return (runtime/use-runtime options)))

(defn.js useDevtoolInjection
  "A hook to inject content into the devtool."
  {:added "0.1"}
  [content]
  (var #{setDebugContent} (r/useContext -/FrameDevtoolContext))
  (r/useEffect (fn [] (setDebugContent content) (return (fn [] (setDebugContent nil)))) []))

(defn.js useDevtoolSharedWorker
  "Registers a SharedWorker resource with the devtool inspector."
  {:added "0.1" :public true}
  [resource]
  (var context (r/useContext -/FrameDevtoolContext))
  (var setSharedWorker (:? context (. context ["setSharedWorker"]) nil))
  (r/useEffect (fn []
                 (when setSharedWorker
                   (setSharedWorker resource)
                   (return (fn [] (setSharedWorker nil))))
                 (return undefined))
               [setSharedWorker resource]))

(defn.js FrameDevtoolScreen
  "A component for displaying injected debug content."
  {:added "0.1"}
  []
  (var #{debugContent controls} (r/useContext -/FrameDevtoolContext))
  (if (not debugContent) (return nil))
  (return [:% -/FrameDevtoolPopup
           {:icon FlaskConical
            :label "Page debug"
            :color controls.color
            :placement "top-end"
            :contentProps {:backgroundColor "$color12"}
            :arrowProps {:backgroundColor "$color12"}} debugContent]))

(defn.js FrameDevtoolProvider
  "The app owns layout, theme and authentication client; this provider owns only devtool state."
  [props]
  (var context (-/useFrameDevtoolContext props))
  (return [:% (. -/FrameDevtoolContext Provider) {:value context}
           [:% (. ui/ThemeGlobalContext Provider) {:value context.theme} props.children]]))

(defn.js FrameDevtoolPanel
  "The visible panel of the devtool."
  {:added "0.1"}
  []
  (var context (r/useContext -/FrameDevtoolContext))
  (var #{controls} context)
  (when (== process.env.NODE_ENV "production")
    (return nil))
  (return [:% -/FrameDevtoolLayout {:color controls.color}
           [:% tm/YStack {:gap "$1"}
            [:% tm/XStack {:gap "$1"}
             (:? (not= process.env.NODE_ENV "production") [:% -/FrameDevtoolUser] nil)
             [:% -/FrameDevtoolWrench] [:% -/FrameDevtoolScreen] [:% -/FrameDevtoolSharedWorker]
             [:% -/FrameDevtoolActivity]] [:% -/FrameDevtoolInfo]]]))
