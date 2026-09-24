(ns pune.ui-metamask-basic
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:macro-only true
   :bundle {:onboarding   [["@metamask/onboarding" :as MetaMaskOnboarding]]
            :provider     [["@metamask/detect-provider" :as MetaMaskDetectProvider]]}
   :import  [["@metamask/onboarding" :as MetaMaskOnboarding]]
   :require [[melbourne.ui-text :as ui-text]
             [js.lib.eth-lib :as eth-lib]
             [js.react-native.ui-util :as ui-util]
             [js.react :as r]
             [js.react-native :as n]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "MetaMaskOnboarding"
                                   :tag "js"}]

  [isMetaMaskInstalled])

(h/template-entries [l/tmpl-macro {:base "MetaMaskOnboarding"
                                   :inst "mtm"
                                   :tag "js"}]
  [[startOnboarding   []]
   [stopOnboarding    []]])

(defmacro.js onboarding
  "creates the onboarding object"
  {:added "4.0"}
  []
  '(new MetaMaskOnboarding))

(defmacro.js detectProvider
  "detects the metamask provider"
  {:added "4.0"}
  []
  '(MetaMaskDetectProvider))

(defmacro.js hasMetaMask?
  "checks that platform contains metamask"
  {:added "4.0"}
  []
  '(and window
        window.ethereum
        window.ethereum.isMetaMask))

(defmacro.js request
  "creates a metamask request"
  {:added "4.0"}
  [m & [f]]
  (apply list '. '(!:G ethereum)
         (list 'request m)
         (if f
           [(list 'then f)]
           [])))

(defmacro.js on
  "creates a metamask request"
  {:added "4.0"}
  [event & [f]]
  (list '. '(!:G ethereum)
        (list 'on event f)))

(defmacro.js removeListener
  "creates a metamask request"
  {:added "4.0"}
  [event & [f]]
  (list '. '(!:G ethereum)
        (list 'removeListener event f)))

(defmacro.js isConnected
  "creates a metamask request"
  {:added "4.0"}
  []
  (list '. '(!:G ethereum) '(isConnected)))

(defglobal.js MetamaskContext (r/createContext))


(defn.js format-address-string
  [s len]
  (return (+ "0x" (j/substring s 2 (or len 20)) "...")))

(defn.js format-chain-id
  [n]
  (return (+ "0x" (. n (toString 16)))))

(defn.js MetamaskProvider
  "constructs an isolated context for portals and gateways to appear"
  {:added "4.0"}
  [#{children}]
  (var #{Provider} -/MetamaskContext)
  (var value (r/const (eth-lib/new-web3-provider
                       (. window ethereum))))
  (return
   [:% Provider
    {:value value}
    children]))

(defn.js MetamaskConsumer
  [#{[component
      (:.. rprops)]}]
  (var #{Consumer} -/MetamaskContext)
  (return [:% Consumer
           (fn [provider]
             (return
              (r/% component (Object.assign rprops
                                       #{provider}))))]))

(defn.js MetamaskEnsureInstall
  [#{design
     fallback
     children}]
  (var [installed setInstalled] (r/local))
  (var [t #{stopNow}] (r/useNow 1100))
  (var onboarding (r/const
                   (new MetaMaskOnboarding)))

  (r/run [installed t]
    (when (and (not installed)
               (-/isMetaMaskInstalled))
      (setInstalled true)
      (stopNow)))
  (return
   (:? (or (!:G ethereum)
           installed)
       
       (or children [:% n/View])
       
       (or fallback
           [:% n/Row
            {:style {:flex 1
                     :justifyContent "center"
                     :alignItems "center"}}
            [:% ui-text/ButtonAccent
             {:design design
              :text  "Install Metamask"
              :style {:fontWeight 600}
              :onPress
              (fn []
                (-/startOnboarding onboarding)
                (-/stopOnboarding onboarding))}]]))))

(defn.js useEnsureConnected
  [onChange]
  (var [accounts setAccounts] (r/local {}))
  (var setTable (r/const
                 (fn [arr]
                   (var accounts (data/arr-juxt (or arr [])
                                             (fn [s]
                                               (return (+ "0x" (j/toUpperCase (j/substring s 2)))))
                                             lib/T))
                   (setAccounts accounts)
                   (when onChange (onChange accounts)))))
  (var requestFn
       (r/const (fn:>
                  (. (-/request {:method "eth_requestAccounts"} setTable)
                     (catch lib/identity)))))
  (r/init []
    (requestFn)
    (-/on "accountsChanged" setTable)
    (return
     (fn []
       (-/removeListener "accountsChanged" setTable))))
  (r/watch [accounts]
    (when (lib/nil? accounts)
      (jc/future-delayed [1000]
        (requestFn))))
  (return #{accounts
            setAccounts
            requestFn}))

(defn.js MetamaskEnsureConnected
  [#{design
     onChange
     fallback
     children}]
  (var #{accounts
         setAccounts
         requestFn}
       (-/useEnsureConnected onChange))
  (return
   (:? (data/is-empty? accounts)
       (or fallback
           [:% n/Row
            [:% ui-text/ButtonAccent
             {:design design
              :text  "Link Site"
              :onPress requestFn}]])
       (or children [:% n/View]))))

(defn.js useChainIdExternal
  [chainId setChainId]
  (var chainIdRef (r/useFollowRef chainId))
  (var interval (r/useInterval
                 (fn []
                   (when (not (r/curr chainIdRef))
                     (. (-/request {:method "eth_chainId"} )
                        (then (fn [res]
                                (setChainId res)))
                        (catch lib/identity))))
                 1000))
  (r/init []
    (. (-/request {:method "eth_chainId"} setChainId)
       (catch lib/identity))
    (-/on "chainChanged" setChainId)
    (return
     (fn []
       (-/removeListener "chainChanged" setChainId)))))

(defn.js useChainId
  []
  (var [chainId setChainId] (r/local))
  (-/useChainIdExternal chainId setChainId)
  (return chainId))

(defn.js MetamaskEnsureChain
  [#{design
     chainId
     children}]
  (var [id setId] (r/local ""))
  (var [visible setVisible] (r/local false))
  (r/init []
    (. (-/request {:method "eth_chainId"} setId)
       (catch lib/identity))
    (-/on "chainChanged" setId)
    (setVisible true)
    (return
     (fn []
       (-/removeListener "chainChanged" setId))))
  (return
   (:? (== chainId (j/toString id))
       (or children [:% n/View])
       [:% ui-util/Fade
        {:visible visible}
        [:% n/View
         {:style {:justifyContent "center"
                  :alignItems "center"}}
         [:% ui-text/H6
          {:design design
           #_#_:variant {:fg {:key "error"}}}
          "Metamask Incorrect Chain"]]])))

(defn.js MetamaskEnsureAddress
  [#{design
     addresses
     fallback
     fallbackLink
     fallbackProps
     onChange
     children}]
  (var #{accounts
         setAccounts
         requestFn}
       (-/useEnsureConnected onChange))
  (return
   (:? (data/is-empty? accounts)
       (:? fallbackLink
           (r/% fallbackLink
                (Object.assign #{design} fallbackProps))
           [:% n/Row
            [:% ui-text/ButtonAccent
             {:design design
              :text  "Link Site"
              :onPress requestFn}]])
       
       (data/arr-some addresses (fn:> [addr] (. accounts [addr])))
       (or children [:% n/View])
       
       :else
       (:? fallback
           (r/% fallback
                (Object.assign #{design accounts} fallbackProps))
           [:% ui-util/FadeIn
            [:% n/View
             {:style {:justifyContent "center"
                      :alignItems "center"}}
             [:% ui-text/H6
              {:design design}
              "Metamask Incorrect Account"]
             [:% ui-text/P
              {:design design
               :style {:fontSize 8}
               :numberOfLines 1}
              (data/first addresses)]]]))))

(def.js MODULE (!:module))

(comment
  (def +provider-keys+
    ["_events"
     "_eventsCount"
     "_maxListeners"
     "_log"
     "_state"
     "selectedAddress"
     "chainId"
     "_handleAccountsChanged"
     "_handleConnect"
     "_handleChainChanged"
     "_handleDisconnect"
     "_handleUnlockStateChanged"
     "_rpcRequest"
     "request"
     "_rpcEngine"
     "_handleStreamDisconnect"
     "_jsonRpcConnection"
     "_sentWarnings"
     "networkVersion"
     "isMetaMask"
     "_sendSync"
     "enable"
     "send"
     "sendAsync"
     "_warnOfDeprecation"
     "_metamask"]))

(comment)
