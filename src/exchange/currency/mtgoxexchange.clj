(ns currency.mtgoxexchange
  (:use [currency.net])
  (:use [clj-json [core :as json]])
;  (:use [incanter core charts])
  (:import currency.exchange.Exchange)
  (:import currency.exchange.Ticker)
  (:import currency.exchange.Order))


;;; constant data
(def MTGOX_TICKER_URL "https://mtgox.com/code/data/ticker.php")
(def MTGOX_DEPTH_URL "https://mtgox.com/api/0/data/getDepth.php?Currency=")
(def MTGOX_BALANCE_URL "https://mtgox.com/api/0/info.php")
(def MTGOX_ORDERS_URL "https://mtgox.com/api/0/getOrders.php")
(def MTGOX_CANCEL_ORDER_URL "https://mtgox.com/api/0/cancelOrder.php")
(def MTGOX_BUY_URL "https://mtgox.com/api/0/buyBTC.php")
(def MTGOX_SELL_URL "https://mtgox.com/api/0/sellBTC.php")


(def TEST_ORDERS (lazy-seq [(Order. (System/currentTimeMillis) :sell :active 1.0 5.55 10 :USD)]))

;;; design point/question: I can maintain orders for testing/simulating however
;;; should we just query the damn site when someone asks for the current orders?
(def orders (atom []))


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

(defn- login
  [user password]
;;; tbd lol
  )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Specific ticker functions

;;; this is a ghetto version of using a proper macro to dynamically
;;; create an instance of any record using any data.  This is not reusable - boo!
(defn- ticker-instance
  ([]
     (Ticker. nil nil nil nil nil nil nil nil nil))
  ([data]
     (merge (Ticker. nil nil nil nil nil nil nil nil nil) data)))

(defn- get-ticker-from-mtgox
  []
  "Returns a new instance of a Ticker that represents the current values."
  (let [tic (str-map-to-kw-map (json/parse-string (get-url MTGOX_TICKER_URL) "ticker"))
	tic-with-time (conj {:time (System/currentTimeMillis)} tic)]
    (ticker-instance tic-with-time)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Specific account/balance functions

(defn- get-balance
  [currency]
  (login "user" "password")
  (str-map-to-kw-map (json/parse-string (get-url MTGOX_BALANCE_URL))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Specific order functions

;;; A helper multimethod to format the URL values based on type.
(defmulti fm class)
(defmethod fm Double [x] "%f")
(defmethod fm Float [x] "%f")
(defmethod fm Integer [x] "%d")
(defmethod fm Long [x] "%d")
(defmethod fm String [x] "%s")
(defmethod fm Object [x] "%s")

(defn- get-order-url
  [url amount price currency]
  (let [fm (format "%s?amount=%s&price=%s&Currency=%s" (fm url) (fm amount) (fm price) (fm currency))]
    (format fm url amount price currency)))

(defn- place-mtgox-order
  [type amount price currency]
  "POST data: amount=#&price=#&Currency=PLN
   returns a list of your open orders"
  (let [type-url {:buy MTGOX_BUY_URL :sell MTGOX_SELL_URL}]
    (println "placed order:" (get-order-url (type-url type) amount price currency))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Specific depth functions

(defn- get-depth-url
  [currency]
  (str MTGOX_DEPTH_URL (name currency)))

(defn- get-depth-from-mtgox
  [currency]
  "Returns the depth of the mtgox exchange as a lazy sequence of orders."
  (let [depth (get-url (get-depth-url currency))
	bids ((str-map-to-kw-map (json/parse-string depth)) :bids)
	asks ((str-map-to-kw-map (json/parse-string depth)) :asks)
	time (System/currentTimeMillis)
	order-fn (fn [order type]
		   (let [price (second order) amount (first order)]
		     (Order. time type :active price amount (* price amount) currency)))]
    (lazy-cat
     (map #(order-fn % :bid) bids)
     (map #(order-fn % :ask) asks))))


(defn get-exchange
  []
  "Returns a new Mt Gox exchange."
  (reify Exchange
	   (get-ticker [this] (get-ticker-from-mtgox))
	   (get-ticker [this start end] (throw (UnsupportedOperationException. "Unsupported")))

	   (get-balance [this] (get-balance))
	   
	   (get-orders [this] TEST_ORDERS)
	   
	   (get-depth [this currency] (get-depth-from-mtgox currency))

	   (place-order [this type amount price currency] (println "placed order"))
;;;			(place-mtgox-order type amount price currency))
	   (cancel-order [this order] nil)))
