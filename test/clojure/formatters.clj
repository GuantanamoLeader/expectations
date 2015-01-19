(ns formatters
  (:use expectations)
  (:require [expectations.formatters.html-formatter :refer 
             [->HTMLFormatter instance render html-key]]
            [expectations.formatters.formatter :refer [class->key] :as fmt]
            [hiccup.core :refer [html]]
            [clojure.java.io :refer [writer file]]))

(defrecord HTMLFmtrM [output names-of-columns])
(def new-key (class->key HTMLFmtrM))

(defmethod fmt/passed new-key [this name meta]
  (render (:output this)
          (html 
            [:tr.passed 
             [:td name]
             [:td "Pass"] 
             [:td (or (:info meta) "")]])))

(def col-names ["test-name" "status" "info"])

(defn set-html-fromatter 
  "set html render for notifer"
  {:expectations-options :before-run}
  []
(derive new-key html-key)
  (swap! *formatter* 
         (fn [_] 
           ;instance
           (->HTMLFmtrM (writer (file "test.html")) col-names)
           )))

(expect 1 1)

(expect false (/ 1 0))

(expect false true)
