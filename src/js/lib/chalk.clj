(ns js.lib.chalk
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [keyword]))

(l/script :js
  {:import [["chalk" :as chalk]]})

;;
;; Chalk
;;

(def$.js chalk chalk)

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "chalk"
                                   :tag "js"}]
  [black
   red
   green
   yellow
   blue
   magenta
   cyan
   white
   blackBright 
   redBright
   greenBright
   yellowBright
   blueBright
   magentaBright
   cyanBright
   whiteBright
   gray
   bgBlack
   bgRed
   bgGreen
   bgYellow
   bgBlue
   bgMagenta
   bgCyan
   bgWhite
   bgBlackBright
   bgRedBright
   bgGreenBright
   bgYellowBright
   bgBlueBright
   bgMagentaBright
   bgCyanBright
   bgWhiteBright])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "chalk"
                                   :tag "js"}]
  [reset
   bold
   dim
   italic
   underline
   inverse
   hidden
   strikethrough
   visible])

(h/template-entries [l/tmpl-entry  {:type :fragment
                                    :base "chalk"
                                    :tag "js"}]
  [supportsColor
   stderr
   stdout
   Instance
   rgb
   hex
   keyword])
