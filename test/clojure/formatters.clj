(ns formatters
  (:use expectations)
  (:require [expectations.formatters.html :refer 
             [->HTMLFormatter render html-key instance]]
            [hiccup.core :refer [html]]
            [clojure.java.io :refer [writer file]]
            [expectations.formatters.formatter :as fmt]))

(defrecord HTMLFmtrM [output names-of-columns])

(def new-key ::my-html)

(defmethod fmt/passed new-key [this name meta]
  (->> (html 
        [:tr.passed 
         [:td name]
         [:td "Pass"] 
         [:td (or (:info meta) "nooobies")]])
       (render (:output this))))

(def col-names ["test-name" "status" "info"])

(defn set-html-fromatter
  "set html render for notifer"
  {:expectations-options :before-run}
  []
  (derive new-key html-key)
  (swap! *formatter* 
         (fn [_] 
           (vary-meta (->HTMLFmtrM (writer (file "test.html")) col-names) assoc :type new-key)
          )))

(expect 1 1)

(expect false (/ 1 0))

(expect false true)
