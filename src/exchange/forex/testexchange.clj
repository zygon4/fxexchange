(ns exchange.forex.testexchange
  (:use clojure.repl
        clojure.java.javadoc)
  (:require
   ;;[clojure.math.numeric-tower :as math]
   [incanter.core :as icore]
   ;;         [incanter.stats :as istat]
   [incanter.io :as iio]
   ;;          [incanter.pdf :as ipdf]
   ;;         [incanter.charts :as icharts]
            )
  (:import exchange.exchange.Exchange)
  (:import exchange.exchange.Monitor)
  (:import exchange.exchange.Tick)
  (:import exchange.exchange.Order)
  (:import java.io.FileReader)
  (:import java.io.BufferedReader))


(def ^String BC "/home/zygon/external/data/bc.csv")

(def bc-set (iio/read-dataset BC :header true))

(def bc-data (atom (icore/to-dataset [])))

(defn get-test-tick
  []
  )

(defn get-test-exchange
  [identifier]
  (let []
    (reify
      Exchangen
      (get-tick [this] nil)
      (get-balance [this] 1.0)
      (get-orders [this] nil)
      (get-depth [this sec] nil)
      (get-name [this] "Test Exchange")
      (get-securities [this] nil)
      (place-order [this type amount price sec] nil)
      (cancel-order [this order] nil)
      (get-history [this] nil))))


;;; Lets make a DSL!
(comment
  "Example queries"
  {:ma.sma {:$eq 1 :unit :day}}
  {:ma.ema {:$eq 4 :unit :hour}}
  {:fib {:high {:$eq 1 :unit :day} :low {:$eq 1 :unit :day}}}
  {:vol {:$eq 1 :unit :min}}

  "Example results"
  [4.01 5.65 [4.45 4.74 4.87] 50.34]
  "OR.."
  {:ma.sma 4.01
   :ma.ema 5.65
   :fib [4.45 4.74 4.87]
   :vol 50.34}
  "Yeah I like that more.."
  )

(defn- query [& criteria]
  (apply merge criteria))

(defn sma
  [frame unit]
  {:ma.sma {:$eq frame :unit unit}})

(defn ema
  [frame unit]
  {:ma.ema {:$eq frame :unit unit}})

(defn vol
  [frame unit]
  {:vol {:$eq frame :unit unit}})

(comment
  "Now whats the big idea here? I'd prefer to have
   an observer pattern with indicators and desired
   numbers.  Until then just query and work with the
   results."
  )

(defn listener [identifier]
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
    (reify Monitor
      (start [^Monitor this pairs]
        (future (listen-fn)))
      (stop [^Monitor this]
        (when @running?
          (println (str "stopping" + identifier))
          (stop-mon running?))))))


