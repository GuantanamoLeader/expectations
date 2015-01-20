(ns expectations.formatters.html
  (:import expectations.formatters.formatter.Formatter)
  (:require [hiccup.page :refer [doctype]]
            [hiccup.core :refer [html]]
            [hiccup.element :refer [unordered-list]]))

(def style (html [:style "body,html{width:100%}#report{width:900px;margin:20px auto 0}#report h3{background:#18576B;color:#CBECDD;padding:10px 20px}#report table{border-collapse:collapse;border:1px solid #AAA;width:100%}#report table td,#report table th{padding:12px 5px}#report table th{background:#E0E0E0;border-bottom:1px solid #AAA;border-right:1px solid #AAA}#report table tr:nth-child(odd){background:#F2F2F2}#report table td:nth-child(odd){background:rgba(0,0,0,.03)}#report table tr.passed{color:#055926}#report table tr.error{color:#F83131}#report table tr.failed{color:#220A6F}#report ul.summary{list-style-type:none;margin-top:20px;border:1px solid #AAA;padding:0}#report ul.summary li{padding:8px 20px}#report ul.summary li:nth-child(even){background:#F0F0F0}"]))

(def columns ["test-name" "status" "info"])

(defprotocol Render
  (render [this object]))

(defrecord HTMLFormatter [output]
  Formatter Render

  (render [this o]
    (when (:output this)
      (.write output o)
      (.flush output)))

  (start [this]
    (->> 
      (html (:html5 doctype) "<html>"
            [:head [:meta {:charset "utf-8"}]
             [:title "Report for Expectations"]
             style]
            "<body><div id=\"report\">")
      (.render this)))

  (stop [this]
    (.render this (html "</div></body></html>")))

  (ns-started [this test-ns]
    (->>
      (html [:h3 test-ns] "<table>"
            [:tr (html (map #(vector :th %) columns))])
      (.render this)))

  (ns-finished [this test-ns]
    (.render this "</table>"))

  (test-started [_ _ _])

  (test-finished [_ _ _])

  (passed [this name meta]
    (.render this (html [:tr.passed [:td name] [:td "Pass"] [:td ""]])))

  (failed [this name meta msg]
    (.render this 
             (html [:tr.failed 
                    [:td name] 
                    [:td "Fail"] 
                    [:td.info [:pre (clojure.string/replace msg #"\[\d?\dm" "")]]])))

  (error [this name meta {test :test}]
    (.render this 
             (html [:tr.error [:td name] [:td "Error"] [:td [:pre.message test]]])))

  (start-dump [_])

  (dump-failures [_ _])

  (dump-profile [_ _ _])

  (dump-summary [this {:keys [test pass fail error run-time ignored-expectations]}]
    (->> (html [:h3 "Summary"]
               (unordered-list {:class "summary"}
                               [(str "Ran " test (+ pass fail) " assertions in " run-time " msecs")
                                (str fail " failures")
                                (str error " errors")
                                (str pass " passes")]))
         (.render this))))
