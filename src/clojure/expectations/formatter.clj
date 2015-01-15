(ns expectations.formatter
  (:require [hiccup.page :refer [doctype]]
            [hiccup.core :refer [html]]
            [hiccup.element :refer [unordered-list]]))

;;; UTILITIES FOR REPORTING FUNCTIONS
(defn getenv [var]
  (System/getenv var))

(defn on-windows? []
  (re-find #"[Ww]in" (System/getProperty "os.name")))

(defn colorize-choice []
  (clojure.string/upper-case (or (getenv "EXPECTATIONS_COLORIZE")
                                 (str (not (on-windows?))))))

(def ansi-colors {:reset "[0m"
                  :red     "[31m"
                  :blue    "[34m"
                  :yellow    "[33m"
                  :cyan    "[36m"
                  :green   "[32m"
                  :magenta "[35m"})

(defn ansi [code]
  (str \u001b (get ansi-colors code (:reset ansi-colors))))

(defn color [code & s]
  (str (ansi code) (apply str s) (ansi :reset)))

(defn colorize-filename [s]
  (condp = (colorize-choice)
    "TRUE" (color :magenta s)
    s))

(defn colorize-results [pred s]
  (condp = (colorize-choice)
    "TRUE" (if (pred)
             (color :green s)
             (color :red s))
    s))

(defn colorize-warn [s]
  (condp = (colorize-choice)
    "TRUE" (color :yellow s)
    s))

(defn colorize-raw [s]
  (condp = (colorize-choice)
    "TRUE" (color :cyan s)
    s))

;;; TEST RESULT REPORTING
(defn test-file [{:keys [file line]}]
  (colorize-filename (str (last (re-seq #"[A-Za-z_\.]+" file)) ":" line)))

(defprotocol Formatter
  "Realization of standart interface."

  ;; DYNAMIC FORMATTER PART
  (start [this]
         "This method is invoked before any tests are run, right after
          they have all been collected. This can be useful for special
          formatters that need to provide progress on feedback (graphical ones).")

  (stop [this] 
        "Invoked after all tests have executed, before dumping post-run reports.")

  ;; TEST OUTPUT METHODS
  (ns-started [this test-ns]
                 "This method is invoked at the beginning of the execution of each group.")

  (ns-finished [this test-ns] "Invoked at the end of the execution of each group.")

  (test-started [this name meta] "Invoked at the beginning of the execution of each example.")

  (test-finished [this name meta] "Invoked when test finished with any result")

  (passed [this name meta] "Invoked when an test passes.")

  (failed [this name meta msg] "Invoked when an test fails.")

  (error [this name meta msg] "Invoked when test thrown an error.")

  ;; BUFFERED FORMATTER PART
  (start-dump [this]
              "This method is invoked after all of the tests have executed. The
               next method to be invoked after this one is #'dump-failures
               (BaseTextFormatter then calls #'dump_failures once for each failed
               test).")

  (dump-failures [this notification] "Dumps detailed information about each test failure.")

  (dump-profile [this slowest-tests slowest-groups] 
                "This method is invoked after the dumping the summary if profiling is
                 enabled.")

  (dump-summary [this notification]
                "This method is invoked after the dumping of tests and failures.
                 Each parameter is assigned to a corresponding attribute.")

  ;(pending [notification] "Invoked when an test is pending.")

  ;(message [this msg] "Used by the reporter to send messages to the output stream.")

  ;(dump-pending [this notification] 
  ;              "Outputs a report of pending tests. This gets invoked
  ;               after the summary if option is set to do so.")

  ; (init [this params] "initialization formatter")

  ; (close [this] "Close all resources")
  
  )

(def style (html [:style "body,html{width:100%}#report{width:900px;margin:20px auto 0}#report h3{background:#18576B;color:#CBECDD;padding:10px 20px}#report table{border-collapse:collapse;border:1px solid #AAA;width:100%}#report table td,#report table th{padding:12px 5px}#report table th{background:#E0E0E0;border-bottom:1px solid #AAA;border-right:1px solid #AAA}#report table tr:nth-child(odd){background:#F2F2F2}#report table td:nth-child(odd){background:rgba(0,0,0,.03)}#report table tr.passed{color:#055926}#report table tr.error{color:#F83131}#report table tr.failed{color:#220A6F}#report ul.summary{list-style-type:none;margin-top:20px;border:1px solid #AAA;padding:0}#report ul.summary li{padding:8px 20px}#report ul.summary li:nth-child(even){background:#F0F0F0}"]))

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
            [:tr (html (map #(vector :th %) ["test-name" "status" "info"]))])
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

(defrecord Printer []
  Formatter

  (start [_]) (stop [_])

  (ns-started [_ test-ns] (println (str "Couple of test in " test-ns)))

  (ns-finished [_ _] (println "\n"))

  (test-started [_ _ _])

  (test-finished [_ _ _])

  (passed [_ name meta] (println (str "\n\t" name " ------- OK")))

  (failed [_ name meta msg] 
    (println (str "\nfaliure in (" (test-file meta) ") : " (:ns meta)))
    (println msg))

  (error [_ name meta {:keys [test expected actual result stack]}]
    (println (clojure.string/join
               "\n" [(colorize-raw test)
                     (str "  exp-msg: " expected)
                     (str "  act-msg: " actual)
                     (str "    threw: " (class result) " - " (.getMessage result))
                     stack])))

  (start-dump [_])

  (dump-failures [_ _])

  (dump-profile [_ _ _])

  (dump-summary [_ {:keys [test pass fail error run-time ignored-expectations]}]
    (println 
      (str "\nRan " test " tests containing "
           (+ pass fail error) " assertions in " run-time " msecs\n"
           (when (> ignored-expectations 0) (colorize-warn (str "IGNORED " ignored-expectations " EXPECTATIONS\n")))
           (colorize-results (partial = 0 fail error) (str fail " failures, " error " errors")) "."))))


