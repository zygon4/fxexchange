(ns exchange.forex.mtgoxexchange
  (:use clojure.repl
        clojure.java.javadoc)
  (:import exchange.exchange.FXExchange
           exchange.exchange.FXMonitor
           exchange.exchange.FXOrder
           exchange.exchange.FXTicker)
  (:import com.xeiam.xchange.Currencies
           com.xeiam.xchange.Exchange
           com.xeiam.xchange.ExchangeFactory
           com.xeiam.xchange.dto.marketdata.Ticker
           com.xeiam.xchange.service.marketdata.polling.PollingMarketDataService))

;;; design point/question: I can maintain orders for testing/simulating however
;;; should we just query the damn site when someone asks for the current orders?
(def orders (atom []))

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

(defn- get-tick [market-data-service]
  (let [tick (.getTicker market-data-service Currencies/BTC Currencies/USD)]
    (FXTicker.
     (.getLocalMillis (.getLocalTime (.getTimestamp tick)))
     (.getAmount (.getHigh tick))
     (.getAmount (.getLow tick))
     (.getAmount (.getVolume tick))
     (.getAmount (.getLast tick))
     (.getAmount (.getBid tick))
     (.getAmount (.getAsk tick)))))

(defn get-mtgox-exchange
  [identifier]
  "Returns a new Mt Gox exchange."
  (let [exchange (.createExchange ExchangeFactory/INSTANCE "com.xeiam.xchange.mtgox.v1.MtGoxExchange")
        market-data-service (.getPollingMarketDataService exchange)
        running? (atom false)
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
      FXExchange
      (cancel-order [this order] nil)
      (get-ticker [this] (get-tick market-data-service))
      (get-balance [this] nil)
      (get-depth [this sec] nil)
      (get-history [this] nil)
      (get-orders [this] nil)
      (place-order [this type amount price sec] (println "placed order"))
      (get-name [this] identifier)
      (get-securities [this] "todo: BTC/USD")
      FXMonitor
      (start [this pairs]
        (future (listen-fn)))
      (stop [this]
        (when @running?
          (println (str "stopping " identifier))
          (stop-mon running?))))))

