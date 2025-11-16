(ns js.i18next
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:import [["react-i18next" :as [* ReactI18n]]
            ["i18next" :as [* I18next]]
            ["react-i18next" :as [* ReactI18next]]
            ["i18next" :as [* I18next]]]})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactI18next"
                                   :tag "js"}]
  [I18nContext
   I18nextProvider
   Trans
   TransWithoutContext
   Translation
   composeInitialProps
   date
   getDefaults
   getI18n
   getInitialProps
   initReactI18next
   number
   plural
   select
   selectOrdinal
   setDefaults
   setI18n
   time
   useSSR
   useTranslation
   withSSR
   withTranslation])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "I18next"
                                   :tag "js"}]
  [changeLanguage
   createInstance
   default
   dir
   exists
   getFixedT
   hasLoadedNamespace
   init
   loadLanguages
   loadNamespaces
   loadResources
   reloadResources
   setDefaultNamespace
   t
   use])
