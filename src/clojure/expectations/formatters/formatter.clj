(ns expectations.formatters.formatter
  (:require [hiccup.page :refer [doctype]]
            [hiccup.core :refer [html]]
            [hiccup.element :refer [unordered-list]]))

(defn dispatch-key [& args] (-> args first meta :type))

#_( Realization of standart interface. )

  ;; DYNAMIC FORMATTER PART
(defmulti start
  "This method is invoked before any tests are run, right after
  they have all been collected. This can be useful for special
  formatters that need to provide progress on feedback (graphical ones)."
  
  dispatch-key)

(defmulti stop
  "Invoked after all tests have executed, before dumping post-run reports."

  dispatch-key)

#_( TEST OUTPUT METHODS )

(defmulti ns-started
  "This method is invoked at the beginning of the execution of each group."

  dispatch-key)

(defmulti ns-finished 
  "Invoked at the end of the execution of each group."

  dispatch-key)

(defmulti test-started 
  "Invoked at the beginning of the execution of each example."

  dispatch-key)

(defmulti test-finished 
  "Invoked when test finished with any result"

  dispatch-key)

(defmulti passed 
  "Invoked when an test passes."

  dispatch-key)
  

(defmulti failed 
  "Invoked when an test fails."

  dispatch-key)

(defmulti error 
  "Invoked when test thrown an error."

  dispatch-key)

#_( BUFFERED FORMATTER PART )

(defmulti start-dump
  "This method is invoked after all of the tests have executed. The
  next method to be invoked after this one is #'dump-failures
  (BaseTextFormatter then calls #'dump_failures once for each failed
  test)."

  dispatch-key)

(defmulti dump-failures
  "Dumps detailed information about each test failure."

  dispatch-key)

(defmulti dump-profile
  "This method is invoked after the dumping the summary if profiling is
  enabled."

  dispatch-key)

(defmulti dump-summary
  "This method is invoked after the dumping of tests and failures.
  Each parameter is assigned to a corresponding attribute."

  dispatch-key)

#_( 
   
   COMMING SOON

  (pending [notification] "Invoked when an test is pending.")

  (message [this msg] "Used by the reporter to send messages to the output stream.")

  (dump-pending [this notification] 
                "Outputs a report of pending tests. This gets invoked
                 after the summary if option is set to do so.")

   (init [this params] "initialization formatter")

   (close [this] "Close all resources")
  
)

(defmethod start :default [& init-args])

(defmethod stop :default [& out-args])

(defmethod ns-started :default [& ns-name])

(defmethod ns-finished :default [& ns-name])

(defmethod test-started :default [& test-name|test-meta])

(defmethod test-finished :default [& test-name|test-meta])

(defmethod passed :default [& test-name|test-meta])

(defmethod failed :default [& test-name|test-meta])

(defmethod error :default [& test-name|test-meta])

(defmethod start-dump :default [& dump-agrs])

(defmethod dump-failures :default [& dump-agrs])

(defmethod dump-profile :default [& dump-agrs])

(defmethod dump-summary :default [& dump-agrs])

