(ns js.blessed.ui-util
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.react   :as r]
             [js.blessed :as b]
             [js.blessed.ui-style :as ui-style]]})

(defn.js ^{:static/lint-globals '#{process}}
  copyClipboard
  "helper function to copy text to clipboard"
  {:added "4.0"}
  ([text]
   (let [copyExec (:? (== process.platform "darwin") "pbcopy" "xclip")
         proc (. (require "child_process")
                 (spawn copyExec))]
     (proc.stdin.write text)
     (proc.stdin.end))))

(defn.js say
  "helper function to say something"
  {:added "4.0"}
  ([text]
   (let [proc (. (require "child_process")
                 (spawn "say"))]
     (proc.stdin.write text)
     (proc.stdin.end))))

(defn.js CopyButton
  "provides a Copy Button"
  {:added "4.0"}
  ([props]
   (let [#{input color onClick} props
         [clicked setClicked] (r/local false)
         bprops (k/obj-assign-nested
                 {:shrink true
                  :content (:? clicked "  OK  " " COPY ")
                  :mouse true
                  :disabled clicked
                  :onClick (fn []
                             (-/copyClipboard
                              (j/inspect input {:colors false}))
                             (setClicked true)
                             (j/delayed [1000]
                               (setClicked false))
                             (if onClick (onClick)))
                  :style (ui-style/styleSmall (:? clicked "gray" color))}
                 props)]
     (return [:button #{(:.. bprops)}]))))

(defn.js parseErrorTag
  "parses the error tag"
  {:added "4.0"}
  ([error]
   (try  (return  (. (j/read error.detail)
                     ["tag"]))
         (catch [err] (return nil)))))

(defn.js ErrorLine
  "provides an error line"
  {:added "4.0"}
  [#{[record
      show
      error
      (:= setError (fn:>))
      (:.. rprops)]}]
  (var #{tag message detail} (or error {}))
  (var bprops (j/assign
               {:mouse true
                :height 1
                :onClick (fn []
                           (setError {}))
                :hoverText " CLEAR "
                :style {:fg "red"
                        :bg "black"
                        :hover {:fg "white"
                                :bg "black"}}
                :content 
                (+ "ERROR - "
                   (or show tag message
                       (:? detail
                           (-/parseErrorTag error)
                           "user.system/unknown_error")))}
               rprops))
  (return (:? (and (not show)
                   (or (not error)
                       (== 0 (j/length (j/keys error))))) nil [:button #{...bprops}])))

(defn.js StatusLine
  "provides a status line"
  {:added "4.0"}
  [#{[message
      show
      color
      (:= setMessage (fn:>))
      (:.. rprops)]}]
  (:= message (:? (or (not message)
                      (== 0 (k/len message))) (or show "") message))
  (var bprops (j/assign
               {:mouse true
                :height 1
                :onClick (fn [] (setMessage ""))
                :hoverText " CLEAR "
                :style {:fg (or color "blue")
                        :bg "black"
                         :hover {:fg "white"
                                 :bg "black"}}
                :content message}
               rprops))
  (return (:? (k/is-empty? message) nil [:button #{...bprops}])))
