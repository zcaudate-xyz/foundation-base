(ns js.lib.i18next
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:bundle {:default   [["i18n" :as I18n]]}})

(h/template-entries [l/tmpl-macro {:base "SupabaseClient"
                                   :inst "supabase"
                                   :subtree []
                                   :tag "js"}]
  [[addResource []]
   [addResourceBundle []]
   [addResources []]
   [changeLanguage []]
   [cloneInstance []]
   [constructor []]
   [createInstance []]
   [dir []]
   [exists []]
   [format []]
   [getDataByLanguage []]
   [getFixedT []]
   [getResource []]
   [getResourceBundle []]
   [hasLoadedNamespace []]
   [hasResourceBundle []]
   [init []]
   [isInitialized []]
   [isInitializing []]
   [isLanguageChangingTo []]
   [language []]
   [languages []]
   [loadLanguages []]
   [loadNamespaces []]
   [loadResources []]
   [logger []]
   [modules []]
   [observers []]
   [options []]
   [reloadResources []]
   [removeResourceBundle []]
   [resolvedLanguage []]
   [services []]
   [setDefaultNamespace []]
   [setResolvedLanguage []]
   [store []]
   [t []]
   [toJSON []]
   [translator []]
   [use []]])


(mapv (fn [name]
        [(symbol name) []])
      (sort [
             "observers",
             "options",
             "services",
             "logger",
             "modules",
             "constructor",
             "init",
             "loadResources",
             "reloadResources",
             "use",
             "setResolvedLanguage",
             "changeLanguage",
             "getFixedT",
             "t",
             "exists",
             "setDefaultNamespace",
             "hasLoadedNamespace",
             "loadNamespaces",
             "loadLanguages",
             "dir",
             "cloneInstance",
             "toJSON",
             "createInstance",
             "isInitializing",
             "store",
             "translator",
             "format",
             "getResource",
             "hasResourceBundle",
             "getResourceBundle",
             "getDataByLanguage",
             "addResource",
             "addResources",
             "addResourceBundle",
             "removeResourceBundle",
             "isLanguageChangingTo",
             "language",
             "languages",
             "resolvedLanguage",
             "isInitialized"
             ]))


