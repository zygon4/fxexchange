(ns currency.exchange
  (:use [currency.net])
  (:use [clj-json [core :as json]])
;;;  (:use [clojure.contrib.duck-streams :only (read-lines)])
  (:import [java.net URL]))

(defrecord Ticker
  [time high low avg vwap vol last buy sell])

(defrecord Order
  [time type status price amount total currency])

(defprotocol Exchange
  "An interface to an exchange that provides ticker values, user controls for buying/selling,
reviewing depth, etc.."

;;; does the fn with start/end make sense?
  (get-ticker [this]
	      [this start end] "Returns a Ticker or a lazy sequence of tickers if start/end times.")

  (get-balance [this] "Returns a map of current balances by monetary type.")
  
  (get-orders [this] "Returns a lazy sequence of outstanding Orders.")

;;; note does the general depth include the our outstanding orders??
;;; also: we are not returning a lazy-seq yet
  (get-depth [this currency] "Returns the current market Orders as a lazy sequence.")

  (place-order [this type amount price currency] "Places a market order.")

;;; is this order a true Order? An agent?)
  (cancel-order [this order] "Cancels an order")

;;; More!
  )
