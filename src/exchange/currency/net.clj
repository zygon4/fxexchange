(ns currency.net
  (:use [clj-json [core :as json]])
  (:import [java.net URL]
	   [java.io BufferedReader InputStreamReader]))

;;; "static" data
(def TICKER_URL "https://mtgox.com/code/data/ticker.php")

(defrecord Ticker
  [high low avg vwap vol last buy sell])

;;; functions

(defn- fetch-url-as-str
  "Returns a url as a string"
  [address]
  (let [url (URL. address)]
    (with-open [stream (.openStream url)]
      (let [buf (BufferedReader. (InputStreamReader. stream))]
	(apply str (line-seq buf))))))

(defn- get-raw-ticker
  "Returns a raw ticker"
  []
  (fetch-url-as-str TICKER_URL))

(defn- get-ticker-from-json
  []
  (json/parse-string (get-raw-ticker)))

(defn- str-map-to-kw-map
  [data]
  "The purpose of this function is to turn a string-based map into a keyword-based
map.  E.g. {\"foo\" 3 \"bar\" 4} => {:foo 3 :bar 4}"
  (reduce merge (map #(hash-map (keyword %1) %2) (keys data) (vals data))))

;;; this is a ghetto version of using a proper macro to dynamically
;;; create an instance of any record using any data.  This is not reusable - boo!
(defn- ticker-instance
  ([]
     (Ticker. nil nil nil nil nil nil nil nil))
  ([data]
     (merge (Ticker. nil nil nil nil nil nil nil nil) data)))

;;; (map tic (keys (get-ticker)))
(defn get-ticker
  []
  (let [tic (str-map-to-kw-map ((get-ticker-from-json) "ticker"))]
    (ticker-instance tic)))




