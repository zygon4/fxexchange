(ns currency.trade
  (:use [currency.net])
  (:use [clj-json [core :as json]])
  (:use [incanter.core :only [view]]))

(defrecord Trade [time price volume])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Lets define some utilityish functions
(defn- str-map-to-kw-map
  [data]
  "Turns a string-based map into a keyword-based map.  E.g. {\"foo\" 3 \"bar\" 4} => {:foo 3 :bar 4}"
  (reduce merge (map #(hash-map (keyword %1) %2) (keys data) (vals data))))

(defn- get-url
  "Returns a URL as a string"
  [url]
  (fetch-url-as-str url))

(def MTGOX_TRADE_URL "https://mtgox.com/api/1/BTCUSD/public/trades?raw")

(def trades (str-map-to-kw-map (json/parse-string (get-url MTGOX_TRADE_URL))))