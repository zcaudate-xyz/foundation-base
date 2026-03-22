(ns js.lib.chalk
  (:require [std.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [keyword]))

(l/script :js
  {:import [["chalk" :as chalk]]})

;;
;; Chalk
;;

(def$.js chalk chalk)

(f/template-entries [l/tmpl-entry {:type :fragment
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

(f/template-entries [l/tmpl-entry {:type :fragment
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

(f/template-entries [l/tmpl-entry  {:type :fragment
                                    :base "chalk"
                                    :tag "js"}]
  [supportsColor
   stderr
   stdout
   Instance
   rgb
   hex
   keyword])
