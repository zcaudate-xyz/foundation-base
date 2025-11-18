(ns code.dev.server.api-prompt
  (:require [std.lib :as h]
            [std.string :as str]))

(defn with-prompt-fn
  [prompt-fn body]
  (let [input  (prompt-fn body)
        output @(h/sh {:args ["gemini" "<<" "EOF" input "EOF"]})]
    (->> (str/split-lines output)
         (filter (fn [line]
                   (not (str/starts-with? line "```"))))
         (str/join-lines)
         (heal/heal))))

(defn to-js-prompt
  [body]
  (str/join-lines
   ["SYSTEM PROMPT START ----"
    "You are an expert programming language translator and std.lang expert, being able to translate js/ts/jsx/tsx code"
    "into a clojure compatible javascript dsl. The dsl spec is presented in the SYSTEM INFO section. You will take code"
    "presented in USER PROMPT and translate it to std.lang dsl. Only output dsl code with no explainations. Only output"
    "the function/functions available in the input. Do not output the MODULE form, the ns form or the l/script form as they"
    "are only there for setup."
    "SYSTEM PROMPT END ----"
    "SYSTEM INFO START ----"
    (slurp ".prompts/plans/translate_js.md")
    "SYSTEM INFO END ----"
    "USER PROMPT START ----"
    body
    "USER PROMPT END ----"]))

(defn to-plpgsql-prompt
  [body]
  (str/join-lines
   ["SYSTEM PROMPT START ----"
    "You are an expert programming language translator and std.lang expert, being able to translate plpgsql code"
    "into a clojure compatible javascript dsl. The dsl spec is presented in the SYSTEM INFO section. You will take code"
    "presented in USER PROMPT and translate it to std.lang dsl. Only output dsl code with no explainations. Only output"
    "the function/functions available in the input. Do not output the MODULE form, the ns form or the l/script form as they"
    "are only there for setup."
    "SYSTEM PROMPT END ----"
    "SYSTEM INFO START ----"
    (slurp ".prompts/plans/translate_pg.md")
    "SYSTEM INFO END ----"
    "USER PROMPT START ----"
    body
    "USER PROMPT END ----"]))

(defn to-jsxc-prompt
  [body]
  (str/join-lines
   ["SYSTEM PROMPT START ----"
    "You are an expert programming language.  being able to translate jsxc/ts/jsxcx/tsx code"
    "into a clojure compatible javascript dsl tree form. There is a decomposition process and a reconstruction"
    "process, breaking down the components into a managable flat structure. The spec is presented in the SYSTEM INFO section."
    "You will take code presented in USER PROMPT"
    "the function/functions available in the input. Do not output the MODULE form, the ns form or the l/script form as they"
    "are only there for setup."
    "SYSTEM PROMPT END ----"
    "SYSTEM INFO START ----";
    (slurp ".prompts/plans/translate_jsxc.md")
    "SYSTEM INFO END ----"
    "USER PROMPT START ----"
    body
    "USER PROMPT END ----"]))
