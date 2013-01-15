(ns exchange.forex.mtgoxexchange
;;;  (:use exchange.exchange)
  (:use exchange.net.netutil)
  (:use [clj-json [core :as json]]
        clojure.repl
        clojure.java.javadoc)
  (:import exchange.exchange.Exchange)
  (:import exchange.exchange.Monitor)
  (:import exchange.exchange.Tick)
  (:import exchange.exchange.Order)
  (:import com.xeiam.xchange.Currencies
           com.xeiam.xchange.Exchange
           com.xeiam.xchange.ExchangeFactory
           com.xeiam.xchange.dto.marketdata.Ticker
           com.xeiam.xchange.service.marketdata.polling.PollingMarketDataService)
  )

(comment
  (defn get-mtgox-exchange []
    (.createExchange ExchangeFactory/INSTANCE "com.xeiam.xchange.mtgox.v1.MtGoxExchange"))

  (defn get-market-data-service [exchange]
    (.getPollingMarketDataService exchange))

  (defn get-ticker [market-data-service]
    (.getTicker market-data-service Currencies/BTC Currencies/USD))
)


;;; constant data
(comment
  (def MTGOX_TICKER_URL "https://mtgox.com/code/data/ticker.php")
  (def MTGOX_DEPTH_URL "https://mtgox.com/api/0/data/getDepth.php?Currency=")
  (def MTGOX_BALANCE_URL "https://mtgox.com/api/0/info.php")
  (def MTGOX_ORDERS_URL "https://mtgox.com/api/0/getOrders.php")
  (def MTGOX_CANCEL_ORDER_URL "https://mtgox.com/api/0/cancelOrder.php")
  (def MTGOX_BUY_URL "https://mtgox.com/api/0/buyBTC.php")
  (def MTGOX_SELL_URL "https://mtgox.com/api/0/sellBTC.php"))


(def TEST_ORDERS (lazy-seq [(Order. (System/currentTimeMillis) :sell :active 1.0 5.55 10 :USD)]))

;;; design point/quesnnnntion: I can maintain orders for testing/simulating however
;;; should we just query the damn site when someone asks for the current orders?
(def orders (atom []))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Lets define some utilityish functions

(defn- login
  [user password]
;;; tbd lol
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Specific ticker functions

;;; this is a ghetto version of using a proper macro to dynamically
;;; create an instance of any record using any data.  This is not reusable - boo!
(defn- tick-instance
  ([]
     (Tick. nil nil nil nil nil nil nil nil nil))
  ([data]
     (merge (Tick. nil nil nil nil nil nil nil nil nil) data)))

(defn- get-ticker-from-json
  [url]
  (json/parse-string (fetch-url-as-str url)))

(defn- get-ticker-from-url
  [url]
  (let [tic (str-map-to-kw-map ((get-ticker-from-json url) "ticker"))]
    (tick-instance tic)))

(defn- get-tick
  []
  "Returns a new instance of a Ticker that represents the current values."
  (tick-instance (conj (get-ticker-from-url MTGOX_TICKER_URL) {:time (System/currentTimeMillis)})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Specific account/balance functions

(defn- get-mtgox-balance
  [currency]
  (login "user" "password")
  (str-map-to-kw-map (json/parse-string (fetch-url-as-str MTGOX_BALANCE_URL))))

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
  (let [depth (fetch-url-as-str (get-depth-url currency))
	bids ((str-map-to-kw-map (json/parse-string depth)) :bids)
	asks ((str-map-to-kw-map (json/parse-string depth)) :asks)
	time (System/currentTimeMillis)
	order-fn (fn [order type]
		   (let [price (second order) amount (first order)]
		     (Order. time type :active price amount (* price amount) currency)))]
    (lazy-cat
     (map #(order-fn % :bid) bids)
     (map #(order-fn % :ask) asks))))

(comment
  (defn listener [exchange context]
  (let [exch exchange
        ctx context
        ticker (get-ticker exch)]
    (fn []
      (try
        (print (Date. (System/currentTimeMillis)) ": ")
        (println (get-tick ticker))
        (Thread/sleep 10000)
        (catch Throwable t
          ;;; todo - get better at exception handling in clojure..
          (.printStackTrace t))))))
  )

(defn get-mtgox-exchange
  [identifier]
  "Returns a new Mt Gox exchange."
  (let [running? (atom false)
        interval 10
        start-mon (fn [running] (swap! running (fn [_] true)))
        stop-mon (fn [running] (swap! running (fn [_] false)))
        listen-fn (fn []
                    (when (not @running?)
                      (println (str "starting " identifier))
                      (start-mon running?)
                      (loop []
                        (when @running?
                         ;;; todo: listen
                          (println "running.. lalala..")
                          (Thread/sleep (* interval 1000))
                          (recur)))))]
    (reify
      Exchange
      (cancel-order [this order] nil)
      ;;(get-ticker [this] (get-mtgox-ticker this))
      (get-balance [this] (get-mtgox-balance))
      (get-depth [this sec] (get-depth-from-mtgox sec))
      (get-history [this] nil)
      (get-orders [this] TEST_ORDERS)
      (place-order [this type amount price sec] (println "placed order"))
      (get-name [this] identifier)
      (get-securities [this] "todo: BTC/USD")
      Monitor
      (start [this pairs]
        (future (listen-fn)))
      (stop [this]
        (when @running?
          (println (str "stopping " identifier))
          (stop-mon running?))))))
