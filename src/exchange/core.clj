(ns exchange.core
  (:use clojure.repl
        clojure.java.javadoc)
  (:import exchange.exchange.FXExchange)
  (:import exchange.exchange.FXMonitor)
  (:use exchange.forex.mtgoxexchange))

(comment
  "todo list:

*order book abstraction with datomic in mind
*historic ticker/general historic info abstraction
    - I guess this would be part of the general "Exchange"
    - "History" protocol?
    - 
")


(defn -main
  "I don't do a whole lot."
  [& args]
  (let [ex (get-mtgox-exchange "mtgox")]
;;;    (start ex [])
    (Thread/sleep 2000)
;;;    (stop ex)
    ))