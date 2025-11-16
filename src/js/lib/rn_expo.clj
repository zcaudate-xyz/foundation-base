(ns js.lib.rn-expo
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:bundle {:lib       [["expo" :as [* Expo]]]
            :auth      [["expo-auth-session" :as [* ExpoAuth]]]
            :contacts  [["expo-contacts" :as [* ExpoContacts]]]
            :clipboard [["expo-clipboard" :as [* ExpoClipboard]]]
            :image-picker  [["expo-image-picker" :as [* ExpoImagePicker]]]
            :media     [["expo-media-library" :as [* ExpoMedia]]]
            :facebook  [["expo-facebook" :as [* ExpoFacebook]]]
            :splash    [["expo-splash-screen" :as [* ExpoSplash]]]
            :review    [["expo-store-review" :as [* ExpoReview]]]
            :font      [["expo-font" :as [* ExpoFont]]]
            :browser   [["expo-web-browser" :as [* ExpoBrowser]]]}})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "Expo"
				   :tag "js"}]
                    [registerRootComponent])

  ;;
  ;; Auth
  ;;

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoAuth"
				   :tag "js"}]
                    [useAuthRequest
                     [useAuthAutoDiscovery useAutoDiscovery]
                     [authMakeRedirectUri makeRedirectUri]
                     [authFetchDiscovery fetchDiscoveryAsync]
                     [authExchangeCode exchangeCodeAsync]
                     [authRefresh refreshAsync]
                     [authRevoke revokeAsync]
                     [authStart startAsync]
                     [authDismiss dismiss]
                     [authGetRedirectUrl getRedirectUrl]
                     [authLoad loadAsync]])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoAuthQueryParams"
				   :tag "js"}]
                    [buildQueryString
                     getQueryParams])

  ;;
  ;; Browser
  ;;

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoBrowser"
				   :tag "js"}]

                    [[browserOpen openBrowserAsync]
                     [browserOpenAuth openAuthSessionAsync]
                     [browserStartAuth maybeCompleteAuthSession]
                     [browserWarmUp warmUpAsync]
                     [browserInitUrl mayInitWithUrlAsync]
                     [browserCoolDown coolDownAsync]
                     [browserDismiss dismissBrowser]
                     [browserTabsSupported getCustomTabsSupportingBrowsersAsync]])

  ;;
  ;; Clipboard
  ;;


(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoClipboard"
				   :tag "js"}]
                    [[clipGetString getStringAsync]
                     [clipSetString setString]
                     [clipAddListener addClipboardListener]
                     [clipRemoveListener removeClipboardListener]])

  ;;
  ;; Contacts
  ;;


(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoContacts"
				   :tag "js"}]
                    [contactsIsAvailable    isAvailableAsync]
                    [contactsRequestPermissions requestPermissionsAsync]
                    [contactsPermissions getPermissionsAsync]
                    [contactsGet getContactsAsync]
                    [contactsGetById getContactByIdAsync]
                    [contactsAdd addContactAsync]
                    [contactsUpdate updateContactAsync]
                    [contactsEdit presentFormAsync]
                    [contactsRemove removeContactAsync]
                    [contactsWriteToFile writeContactToFileAsync])


  ;;
  ;; Font
  ;;



(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoFont"
				   :tag "js"}]
                    [useFonts
                     [fontLoadAsync  loadAsync]
                     [fontIsLoaded   isLoaded]
                     [fontIsLoading  isLoading]])

  ;;
  ;; Facebook
  ;;


(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoFacebook"
				   :tag "js"}]
                    [[fbInitialize initializeAsync]
                     [fbRequestPermissions requestPermissionsAsync]
                     [fbGetPermissions getPermissionsAsync]
                     [fbLoginWithReadPermissions loginWithReadPermissionsAsync]
                     [fbSetAdvertiserTrackingEnabled setAdvertiserTrackingEnabledAsync]
                     [fbLogout logOutAsync]
                     [fbGetAuthenticationCredential getAuthenticationCredentialAsync]])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoImagePicker"
				   :tag "js"}]
                    [[imageCameraRequest requestCameraPermissionsAsync]
                     [imageMediaLibraryRequest  requestMediaLibraryPermissionsAsync]
                     [imageCameraPermissions getCameraPermissionsAsync]
                     [imageMediaLibraryPermissions getMediaLibraryPermissionsAsync]
                     [imageLibraryLaunch launchImageLibraryAsync]
                     [imageCameraLaunch launchCameraAsync]
                     [imageGetPending getPendingResultAsync]])

  ;;
  ;; Media 
  ;;



(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoMedia"
				   :tag "js"}]
                    [[mediaRequestPermissions requestPermissionsAsync]
                     [mediaPermissions getPermissionsAsync]
                     [mediaPickPermissions presentPermissionsPickerAsync]
                     [mediaCreateAsset createAssetAsync]
                     [mediaSave saveToLibraryAsync]
                     [mediaAlbumNeedsMigration albumNeedsMigrationAsync]
                     [mediaAlbumMigrate migrateAlbumIfNeededAsync]
                     [mediaGetAssets getAssetsAsync]
                     [mediaGetInfo getAssetInfoAsync]
                     [mediaDeleteAssets deleteAssetsAsync]
                     [mediaAlbumsList getAlbumsAsync]
                     [mediaGetAlbum getAlbumAsync]
                     [mediaCreateAlbum createAlbumAsync]
                     [mediaDeleteAlbums deleteAlbumsAsync]
                     [mediaAddToAlbum addAssetsToAlbumAsync]
                     [mediaRemoveFromAlbum removeAssetsFromAlbumAsync]
                     [mediaGetMoments getMomentsAsync]
                     [mediaAddListener addListener]
                     [mediaRemoveAllListeners removeAllListeners]])

  ;;
  ;; Review
  ;;


(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoReview"
				   :tag "js"}]
                    [[reviewHasAction hasAction]
                     [reviewIsAvailable isAvailableAsync]
                     [reviewRequest requestReview]
                     [reviewStoreUrl storeUrl]])

  ;;
  ;; Splash
  ;;


(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoSplash"
				   :tag "js"}]
                    [[splashHide hideAsync]
                     [splashPreventHide preventAutoHideAsync]])
  
