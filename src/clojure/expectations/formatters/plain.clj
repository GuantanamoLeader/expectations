(ns expectations.formatters.plain
  (:require [expectations.formatters.formatter :as fmt]))

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

(defrecord PlainFormatter [])

(def plain-key ::plain)

(def instance (vary-meta (PlainFormatter.) assoc :type plain-key))

(defmethod fmt/ns-started plain-key [_ test-ns] (println (str "Couple of test in " test-ns)))

(defmethod fmt/ns-finished plain-key [_ _] (println "\n"))

(defmethod fmt/passed plain-key [_ name meta] (println (str "\n\t" name " ------- OK")))

(defmethod fmt/failed plain-key [_ name meta msg] 
             (println (str "\nfaliure in (" (test-file meta) ") : " (:ns meta)))
             (println msg))

(defmethod fmt/error plain-key [_ name meta {:keys [test expected actual result stack]}]
            (println (clojure.string/join
                       "\n" [(colorize-raw test)
                             (str "  exp-msg: " expected)
                             (str "  act-msg: " actual)
                             (str "    threw: " (class result) " - " (.getMessage result))
                             stack])))

(defmethod fmt/dump-summary plain-key 
  [_ {:keys [test pass fail error run-time ignored-expectations]}]
  (println 
    (str "\nRan " test " tests containing "
         (+ pass fail error) " assertions in " run-time " msecs\n"
         (when (> ignored-expectations 0) (colorize-warn (str "IGNORED " ignored-expectations " EXPECTATIONS\n")))
         (colorize-results (partial = 0 fail error) (str fail " failures, " error " errors")) ".")))

