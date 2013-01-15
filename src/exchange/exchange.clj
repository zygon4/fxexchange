(ns exchange.exchange
  (:use clojure.repl
        clojure.java.javadoc))

(defrecord FXTicker
    [time high low vol last buy sell])

(defrecord FXOrder
  [time type status price amount total currency])

(defprotocol FXHistory
  "Represents the exchanges historic information, transactions, etc."
  (get-tick-range [this start] "Returns a range of Ticks in the form
of a seq for the security given a start time.")
  (get-tick-range [this start end] "Returns a range of Ticks in the form
of a seq for the security given a start time and end time."))

(defprotocol FXMonitor
  "Represents a generic monitor that has a lifecycle."
  (start [this pairs] "Starts the monitor.  If pairs are supplied then only
those pairs will be monitored.")
  (stop [this] "Stops the monitor"))

(defprotocol FXExchange
  "An interface to an exchange that provides ticker values, user controls for buying/selling,
reviewing depth, etc.."

  (get-ticker [this] "Returns the exchange's ticker instance.")

  (get-balance [this] "Returns a map of current balances by monetary type.")
  
  (get-orders [this] "Returns a lazy sequence of outstanding Orders.")
  
;;; note: does the general depth include the our outstanding orders??
;;; also: we are not returning a lazy-seq yet
  (get-depth [this sec] "Returns the current market Orders as a lazy sequence.")

  (get-name [this] "Returns the Exchange's name")

  (get-securities [this] "Returns the Exchange's securities of interest.")
  
  (place-order [this type amount price sec] "Places a market order.")

;;; is this order a true Order? An agent?)
  (cancel-order [this order] "Cancels an order")

  (get-history [this] "Returns the Exchanges historic logs")

  ;;; todo: get order book

;;; More!
  )