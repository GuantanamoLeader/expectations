(ns expectations.formatters.plain
  (:import [expectations.formatters.formatter Formatter]))

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

(defrecord PlainFormatter []
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

