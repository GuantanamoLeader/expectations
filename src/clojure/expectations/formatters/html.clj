(ns expectations.formatters.html
  (:require [expectations.formatters.formatter :as fmt]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [doctype]]
            [hiccup.element :refer [unordered-list]]
            [clojure.java.io :refer [writer]]))

(def style (html [:style "body,html{width:100%}#report{width:900px;margin:20px auto 0}#report h3{background:#18576B;color:#CBECDD;padding:10px 20px}#report table{border-collapse:collapse;border:1px solid #AAA;width:100%}#report table td,#report table th{padding:12px 5px}#report table th{background:#E0E0E0;border-bottom:1px solid #AAA;border-right:1px solid #AAA}#report table tr:nth-child(odd){background:#F2F2F2}#report table td:nth-child(odd){background:rgba(0,0,0,.03)}#report table tr.passed{color:#055926}#report table tr.error{color:#F83131}#report table tr.failed{color:#220A6F}#report ul.summary{list-style-type:none;margin-top:20px;border:1px solid #AAA;padding:0}#report ul.summary li{padding:8px 20px}#report ul.summary li:nth-child(even){background:#F0F0F0}"]))

(defrecord HTMLFormatter [output names-of-columns])

(def html-key ::html)

(def names-of-columns ["test-name" "status" "info"])

(def instance (vary-meta (HTMLFormatter. (writer System/out) names-of-columns) assoc :type html-key))

(defn render [output obj]
  (.write output obj)
  (.flush output))

(defmethod fmt/start html-key [this]
  (->> 
    (html (:html5 doctype) "<html>"
          [:head [:meta {:charset "utf-8"}]
           [:title "Report for Expectations"]
           style]
          "<body><div id=\"report\">")
    (render (:output this))))

(defmethod fmt/stop html-key [this]
  (render (:output this) (html "</div></body></html>")))

(defmethod fmt/ns-started html-key [this test-ns]
  (->>
    (html [:h3 test-ns] "<table>"
          [:tr (html (map #(vector :th %) (:names-of-columns this)))])
    (render (:output this))))

(defmethod fmt/ns-finished html-key [this test-ns]
    (render (:output this) "</table>"))

(defmethod fmt/passed html-key [this name meta]
    (render (:output this) (html [:tr.passed [:td name] [:td "Pass"] [:td ""]])))

(defmethod fmt/failed html-key [this name meta msg]
    (render (:output this) 
            (html [:tr.failed 
                   [:td name] 
                   [:td "Fail"] 
                   [:td.info [:pre (clojure.string/replace msg #"\[\d?\dm" "")]]])))

(defmethod fmt/error html-key [this name meta {test :test}]
  (render (:output this) 
          (html [:tr.error [:td name] [:td "Error"] [:td [:pre.message test]]])))

(defmethod fmt/dump-summary html-key 
  [this {:keys [test pass fail error run-time ignored-expectations]}]
  (->> (html [:h3 "Summary"]
             (unordered-list {:class "summary"}
                             [(str "Ran " test (+ pass fail) " assertions in " run-time " msecs")
                              (str fail " failures")
                              (str error " errors")
                              (str pass " passes")]))
       (render (:output this))))

