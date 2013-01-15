(ns currency.indicator
  (:import currency.trade.Trade))

(set! *warn-on-reflection* true)

(defprotocol Indicator
  "This represents an data indicator"
  (get-indication [this data] "Returns the current indication levels."))

(def DAY_IN_MILLIS (* 1000 60 60 24)) ;;; millis * sec * minute * hour

(defn days-ago-at-most? ;;; corny name and ties this to a specific "at most" impl
  "Returns true if the unix-time is within days from the current unix-time"
  [time days]
  (let [current (System/currentTimeMillis)]
    (case (and (>= days 0) (<= time current))
	  true (and (> time (* days DAY_IN_MILLIS)))
	  false)))

(defn- high-low
  [data pred? days]
  (apply pred? (map :price (filter #(days-ago-at-most? (:time %) days) @data))))

(defn- volume-ind
  [data days]
  (reduce + (map :volume (filter #(days-ago-at-most? (:time %) days) @data))))

;;; Whoa whoa, this code blows.  We need to abstract out some stuff.
;;; What's common?  The reify Indicator and "get-indication [this data]" piece.
;;; However, each indicator has a different name and possibly a different function
;;; and/or function argument.
;;; Also the case that we need to generate new Indicators per call - or maybe not now?
;;; Each indicator is not holding all the data anymore..
;;; Update: A little better now.  Less code but still not quite abstracted properly.
(defn get-indicator
  "Returns a new indicator given the type keyword. :volume :high :low.
The args may represent specific timeframes for the indications in units
of days."
  [indicator & args]
  ({:volume
    (reify Indicator
	   (get-indication [this data] (volume-ind data (first args))))
    :high
    (reify Indicator
	   (get-indication [this data] (high-low data max (dec (first args)))))
    :low
    (reify Indicator
	   (get-indication [this data] (high-low data min (dec (first args)))))
   } indicator))
