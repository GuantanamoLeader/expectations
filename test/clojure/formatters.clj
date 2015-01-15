(ns formatters
  (:use expectations)
  (:import [expectations.formatter HTMLFormatter])
  (:require [clojure.java.io :refer [writer file]]))

(defn set-html-fromatter 
  "set html render for notifer"
  {:expectations-options :before-run}
  []
  (swap! *formatter* 
         (fn [_] 
           (let [stdout-bf (writer System/out)
                 file-bf (writer (file "test.html"))]
             (HTMLFormatter. file-bf)))))

(expect 1 1)

(expect false (/ 1 0))

(expect false true)
