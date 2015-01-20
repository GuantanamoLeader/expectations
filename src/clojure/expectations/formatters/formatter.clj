(ns expectations.formatters.formatter)

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

