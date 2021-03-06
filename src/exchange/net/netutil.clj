(ns exchange.net.netutil
  (:require [clj-json [core :as json]])
  (:import [java.net URL]
	   [java.io BufferedReader InputStreamReader]))

;;; functions

(defn fetch-url-as-str
  "Returns a url as a string"
  [address]
  (let [url (URL. address)]
    (with-open [stream (.openStream url)]
      (let [buf (BufferedReader. (InputStreamReader. stream))]
	(apply str (line-seq buf))))))

(defn str-map-to-kw-map
  [data]
  "The purpose of this function is to turn a string-based map into a keyword-based
map.  E.g. {\"foo\" 3 \"bar\" 4} => {:foo 3 :bar 4}"
  (reduce merge (map #(hash-map (keyword %1) %2) (keys data) (vals data))))



;;(defn stop [running] (swap! running (fn [_] false)))
;;(defn start [running] (swap! running (fn [_] true)))

(defn create-thing [fn & opts]
  (fn []
    (let [running (atom true)]
      {:start #(swap! running (fn [] true))
       :stop  #(swap! running (fn [] false))
       })))

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
          (.printStackTrace t)))))))

;;;(def running (atom true))


(defn foo [running]
  (try (loop [cnt 5]
         (when (and (pos? cnt) @running)
           (println cnt)
           (Thread/sleep 1000)
           (recur (dec cnt)))) (finally (println "uh oh"))))