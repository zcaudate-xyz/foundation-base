(ns js.lib.rn-expo
  (:require [std.lang :as l]
            [std.lib.foundation :as f]))

(l/script :js
  {:import [["expo-image-picker" :as [* ExpoImagePicker]]
            ["expo-splash-screen" :as [* ExpoSplash]]
            ["expo-facebook" :as [* ExpoFacebook]]
            ["expo-contacts" :as [* ExpoContacts]]
            ["expo-web-browser" :as [* ExpoBrowser]]
            ["expo-auth-session" :as [* ExpoAuth]]
            ["expo-font" :as [* ExpoFont]]
            ["expo-store-review" :as [* ExpoReview]]
            ["expo-clipboard" :as [* ExpoClipboard]]
            ["expo" :as [* Expo]]
            ["expo-media-library" :as [* ExpoMedia]]]})

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "Expo"
				   :tag "js"}]
                    [registerRootComponent])

  ;;
  ;; Auth
  ;;

(f/template-entries [l/tmpl-entry {:type :fragment
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

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoAuthQueryParams"
				   :tag "js"}]
                    [buildQueryString
                     getQueryParams])

  ;;
  ;; Browser
  ;;

(f/template-entries [l/tmpl-entry {:type :fragment
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


(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoClipboard"
				   :tag "js"}]
                    [[clipGetString getStringAsync]
                     [clipSetString setString]
                     [clipAddListener addClipboardListener]
                     [clipRemoveListener removeClipboardListener]])

  ;;
  ;; Contacts
  ;;


(f/template-entries [l/tmpl-entry {:type :fragment
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



(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoFont"
				   :tag "js"}]
                    [useFonts
                     [fontLoadAsync  loadAsync]
                     [fontIsLoaded   isLoaded]
                     [fontIsLoading  isLoading]])

  ;;
  ;; Facebook
  ;;


(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoFacebook"
				   :tag "js"}]
                    [[fbInitialize initializeAsync]
                     [fbRequestPermissions requestPermissionsAsync]
                     [fbGetPermissions getPermissionsAsync]
                     [fbLoginWithReadPermissions loginWithReadPermissionsAsync]
                     [fbSetAdvertiserTrackingEnabled setAdvertiserTrackingEnabledAsync]
                     [fbLogout logOutAsync]
                     [fbGetAuthenticationCredential getAuthenticationCredentialAsync]])

(f/template-entries [l/tmpl-entry {:type :fragment
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



(f/template-entries [l/tmpl-entry {:type :fragment
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


(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoReview"
				   :tag "js"}]
                    [[reviewHasAction hasAction]
                     [reviewIsAvailable isAvailableAsync]
                     [reviewRequest requestReview]
                     [reviewStoreUrl storeUrl]])

  ;;
  ;; Splash
  ;;


(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoSplash"
				   :tag "js"}]
                    [[splashHide hideAsync]
                     [splashPreventHide preventAutoHideAsync]])
  
