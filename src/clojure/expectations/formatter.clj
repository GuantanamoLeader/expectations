(ns expectations.formatter
  (:require [hiccup.page :refer [doctype]]
            [hiccup.core :refer [html]]))

(defprotocol Formatter
  "Realization of standart interface."
  (init [this output] "Set output buffer.")

  (close [this]
         "Invoked at the very end, `close` allows the formatter to clean
          up resources, e.g. open streams, etc.")

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

  (test-started [this test-var] "Invoked at the beginning of the execution of each example.")

  (passed [this test-var] "Invoked when an test passes.")

  (failed [this test-var] "Invoked when an example fails.")

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

  (dump-summary [this summary test-count failure-count pending-count]
                "This method is invoked after the dumping of tests and failures.
                 Each parameter is assigned to a corresponding attribute.")

  ;(pending [notification] "Invoked when an test is pending.")

  ;(message [this msg] "Used by the reporter to send messages to the output stream.")

  ;(dump-pending [this notification] 
  ;              "Outputs a report of pending tests. This gets invoked
  ;               after the summary if option is set to do so.")

  )

(defrecord HTMLFormatter []
  Formatter

  (render [this o]
    (when-let [s (:output this)]
      (write s o)))

  (init [this output]
    (assoc this 
           :output output
           :columns ["Name of test" "Status" "INFO"])
    (->> 
      (html (:html5 doctype) "<html>"
            [:head [:meta {:charset "utf-8"}]
                   [:title "Report for Expectations"]]
            "<body>")
      (.render this)))

  (close [this]
    (.render this (html "</body></html>"))
    (assoc this :output nil))

  (start [_])
  (stop [_])

  (ns-started [this test-ns]
    (->>
      (html [:h3 test-ns] "<table>"
            (vec (cons :th (map #(vector :td %) (:columns this)))))
      (.render this)))

  (ns-finished [this test-ns]
    (.render this "</table>"))

  (test-started [_ _])

  (passed [this test-var]
    (.render this (html [:tr.passed [:td test-var] [:td "Pass"] [:td ""]])))

  (failed [this test-var]
    (.render this 
             (html [:tr.failed [:td test-var] [:td "Fail"] [:td "DEBUG INFO"]])))

  (start-dump [_])

  (dump-failures [_ _])

  (dump-profile [_ _ _])

  (dump-summary [this summary test-count failure-cout pending-count]
    (->> (html (unordered-list {:class "summary"}
                              [summary test-count failure-cout pending-count]))
         (.render this))))

(defrecord StringFormatter []
  Formatter

  (init [_ _]) (close [_]) (start [_]) (stop [_])

  (ns-started [_ test-ns] (println (str "Couple of test in " this-ns)))

  (ns-finished [_ _] (println "\n"))

  (test-started [_ _])

  (passed [_ test-var] (println (str (:name test-var) " ------- OK")))

  (failed [_ test-var] (println (str (:name test-var) "\n" (:msg test-var))))

  (start-dump [_])

  (dump-failures [_])

  (dump-profile [_])

  (dump-summary [_ _ _ _ _]))

