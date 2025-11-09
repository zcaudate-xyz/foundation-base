(ns code.ai.base-paraphrase
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.lib.bin :as bin]
            [std.string :as str]
            [xt.lang.base-notify :as notify])
  (:import (ai.djl Application$NLP
                   Application$CV)
           (ai.djl.ndarray NDList)
           (ai.djl.repository.zoo Criteria ZooModel)
           (ai.djl.huggingface.zoo HfModelZoo)
           (ai.djl.training.util ProgressBar)))

(l/script :python
  {:runtime :basic
   :require [[python.core :as py]
             [xt.lang.base-repl :as repl]]})



(comment
  (l/rt:restart)
  
  (into {} (l/rt :python))
  @(std.concurrent.relay/send 
    (rt.basic.server-basic/get-relay
     (rt.basic.server-basic/get-server
      "s1gn6b69yfl2"
     :python))
    {:op :read-some})
  

  (notify/wait-on :python
    (repl/notify 1))
  
  (notify/wait-on [:python 10000]
    (var SENTENCE "Deep Java Library (DJL) is an open-source, high-level, engine-agnostic Java framework for deep learning. DJL is designed to be easy to get started with and simple to use for Java developers. DJL provides a native Java development experience and functions like any other regular Java library.

You don't have to be machine learning/deep learning expert to get started. You can use your existing Java expertise as an on-ramp to learn and use machine learning and deep learning. You can use your favorite IDE to build, train, and deploy your models. DJL makes it easy to integrate these models with your Java applications.

Because DJL is deep learning engine agnostic, you don't have to make a choice between engines when creating your projects. You can switch engines at any point. To ensure the best performance, DJL also provides automatic CPU/GPU choice based on hardware configuration.

DJL's ergonomic API interface is designed to guide you with best practices to accomplish deep learning tasks. The following pseudocode demonstrates running inference:")
    (var AutoTokenizer (. (py/pkg "transformers")
                          AutoTokenizer))
    (var AutoModelForSeq2SeqLM (. (py/pkg "transformers")
                                  AutoModelForSeq2SeqLM))
    (var tokenizer (AutoTokenizer.from_pretrained "shorecode/t5-efficient-tiny-summarizer-general-purpose-v3"))
    (var model (AutoModelForSeq2SeqLM.from_pretrained "shorecode/t5-efficient-tiny-summarizer-general-purpose-v3"))

    (var input-text (+ "summarise: " SENTENCE))
    (var input-ids (. (tokenizer input-text :return-tensors "pt")
                      input-ids))
    (repl/notify input-ids))
  
  ("shorecode/t5-efficient-tiny-summarizer-general-purpose-v3")
  (py/pkg-load "pip")

  @(h/sh {:args ["pip" "install" "transformers"]
          :inherit true
          :async true}))



(comment
  
  (def *criteria*
    (-> (Criteria/builder)
        #_(.optApplication Application$NLP/SENTIMENT_ANALYSIS)
        (.setTypes String String)      ; Input: String, Output: String
        (.optArtifactId "shorecode/t5-efficient-tiny-summarizer-general-purpose-v3")
        (.optProgress (ProgressBar.))
        #_(.optModelName  )
        
        (.build)))
  (def *criteria*
    (-> (Criteria/builder)
        (.setTypes NDList NDList)
        (.optEngine "PyTorch")
        (.optModelUrls "djl://ai.djl.huggingface.pytorch/Seznam/simcse-dist-mpnet-czeng-cs-en")
        (.optProgress (ProgressBar.))
        (.build)
        (.loadModel)))
  
  (def *criteria*
    (-> (Criteria/builder)
        (.setTypes String String)
        (.optEngine "PyTorch")
        (.optModelUrls "djl://ai.djl.huggingface.pytorch/shorecode/t5-efficient-tiny-summarizer-general-purpose-v3")
        (.optProgress (ProgressBar.))
        (.build)
        (.loadModel)))
  
  (def *criteria*
    (-> (Criteria/builder)
        (.optEngine "PyTorch")
        (.optModelUrls "djl://ai.djl.pytorch/resnet13_embedding")
        (.optProgress (ProgressBar.))
        (.build)))
  (.opModelUrls "djl://ai.djl.pytorch/resnet13_embedding")
  
  (-> (Criteria/builder)
      (.setTypes String String)    ; Summarization is String -> String
      (.optEngine "PyTorch")
      
      
      (.optGroupId "ai.djl.huggingface.pytorch") 
      (.optArtifactId "shorecode/t5-efficient-tiny-summarizer-general-purpose-v3")
      
      
      
      (.optProgress (ProgressBar.))
      (.build)
      (.loadModel))
  "shorecode/t5-efficient-tiny-summarizer-general-purpose-v3"
  
  (.? (Criteria/builder))
  
  #_#_(.optModelZoo (HfModelZoo/getModelZoo "default"))
  #_(.optModelUrls "djl://ai.djl.huggingface.pytorch/Seznam:simcse-dist-mpnet-czeng-cs-en"
                   #_"djl://ai.djl.huggingface.pytorch/google-t5:t5-small")
  
  (def *model*
    (h/future (.loadModel *criteria*)))
  
  (def *model1*
    (h/future (.loadModel *criteria*)))
  
  (def
    (h/map-entries (fn [[k v]]
                     [(str k)
                      (count v)])
                   (HfModelZoo/listModels))
    (h/future (HfModelZoo/listModels))
    (HfModelZoo/getModelZoo "ai.djl.huggingface.pytorch"))

  (def *hf*
    (h/future (HfModelZoo/loadModel *criteria*)))
  )

(comment
  ai.djl.util.ProgressBar
  ()

  ai.djl.huggingface.zoo.HfModelZoo/
  
  (def model-url "https://huggingface.co/sshleifer/distilbart-cnn-6-6/resolve/main/")

  (defn shorten [text]
    (let [config (doto (SearchConfig.)
                   (.setMaxSeqLength 80))
          criteria (.. (Criteria/builder)
                       (setTypes ai.djl.ndarray.NDList
                                 ai.djl.modality.nlp.generate.Seq2SeqLMOutput)
                       (optModelUrls model-url)
                       (optEngine "PyTorch")
                       (optTranslatorFactory (DeferredTranslatorFactory.))
                       bu [<35          ;60;49M[<35;56;42M
                           ]ild)]
      (with-open [^ZooModel model (.loadModel criteria)
                  ^Predictor predictor (.newPredictor model)
                  ^NDManager manager (.newSubManager (.getNDManager model))
                  ^HuggingFaceTokenizer tokenizer
                  (HuggingFaceTokenizer/newInstance "sshleifer/distilbart-cnn-6-6")]
        (let [generator (TextGenerator. predictor "greedy" config)
              enc (.encode tokenizer text)
              ids (.getIds enc)
              input (.expandDims (.create manager (long-array ids)) 0)
              out (.generate generator input)
              summary (.decode tokenizer (.toLongArray out))]
          summary))))

  (.%> ai.djl.modality.nlp.translator.SimpleText2TextTranslator)
  
  (comment
    ai.djl.huggingface.zoo.HfModelZoo
    ai.djl.huggingface.pipeline.HuggingFaceInference
    ai.djl.genai.pipeline.TextGenerationPipeline
    ai.djl.huggingface.zoo.HuggingFaceModelZoo
    ai.djl.translate.Seq2SeqTranslator
    ai.djl.huggingface.translator.TextGenerationTranslatorFactory

    (filter (fn [s]
              (re-find #"ProgressBar" s))
            (s/class-seq
             '[[ai.djl/api "0.34.0"]]))
    ("ai.djl.training.util.ProgressBar")
    
    ("ai.djl.modality.nlp.translator.SimpleText2TextTranslator")
    ("ai.djl.Application$NLP" "ai.djl.Application$Tabular" "ai.djl.Application"
     "ai.djl.Application$CV" "ai.djl.Application$TimeSeries" "ai.djl.Application$Audio")
    (jvm.artifact.search/class-seq
     '[[ai.djl.huggingface/tokenizers "0.34.0"]
       [ai.djl.pytorch/pytorch-engine "0.34.0"]])))
