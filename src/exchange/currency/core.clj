
(set! *warn-on-reflection* true)

(ns currency.core
  (:use [clojure.contrib.duck-streams :only (read-lines)]
	[clojure.contrib.string :only (split)]
	[currency.indicator])
;;;  (:import currency.ticker.Ticker)
  (:import currency.trade.Trade)
;;;  (:import currency.agent.Agent)
  )

(comment
  "TODO:
    URL for getting current depth
    URL for getting trades?
    http encoding user/pw to make requests
    make sure the market depth and all server requests return a lazy-seq 
  ")



(comment
  "All this code is commmented out until further notice.
Frankly it's not exactly what I want right now. I will focus
on going from a ticker URL (and eventually file) as input
to a learning scheme."
(def file (java.io.File. "/home/zygon/external/data/bc.csv"))
(def data (atom []))
(def current-indicators (atom []))

(defn register
  "Register an indicator"
  [indicator]
  (swap! current-indicators conj indicator))

(defn unregister
  "Unregister an indicator"
  [indicator]
  (swap! current-indicators disj indicator))

(defn init []
  (reset! data [])
  (reset! current-indicators [])
  nil)

(defn run [file]
  (let [proc (fn [trade]
	       (let [trade-data (split #"," 3 trade)
		     time (* 1000 (Long/parseLong (nth trade-data 0)))
		     price (Double/parseDouble (nth trade-data 1))
		     volume (Double/parseDouble (nth trade-data 2))]
		 (swap! data conj (Trade. time price volume))))]
    (doseq [trade (read-lines (.getPath file))]
      (proc trade))))

(defn get-indication-value
  [indicator & args]
  (get-indication (apply get-indicator indicator args) data))
)


(def running true)
pp
;;;(def ticker-agent (agent (->> ticker-provider .get-ticker)))

;;;(def ticker-provider (currency.mtgoxticker/get-provider))

(defn ticker-agent-fn
  [agent tic-provider delay]
  (when running
    (send-off *agent* #'ticker-agent-fn tic-provider delay))
  (do
    (. Thread sleep delay)
    (merge agent (->> tic-provider .get-ticker))))

(comment (defn -main
;;;  [file]
;;;  (init)
;;;  (register (get-indicator :volume))
;;;  (register (get-indicator :low))
;;;  (decide (Agent. (get-indicator :volume) (fn [_] (println "decided!"))))
;;;  (run file)
;;;  (println (get-indication-value :volume 30))
;;;  (println (get-indication-value :low 30))
  []
  (send-off ticker-agent ticker-agent-fn ticker-provider 5000)
  (dotimes [i 50] (do (. Thread (sleep 2000)) (println @ticker-agent)))
  nil))

(comment
  "I'd like to describe some simple syntax for the user-side interaction.
The vision is for a user to write some simple lines of code to generate
an agent program.  It will utilize declared indicators and some simple math
operations.  Keep in mind this is not defining a brand new language, it is
simply converting a syntax set to clojure.

Want to be able to code statements such as:

If 20 day moving average crosses over 50 day moving average then buy
If 20 day moving average crosses below 50 day moving average then sell

;;;(*http://fxtradingguide.com/trading-system-algorithms/)

We'll have data and functions.  I'd like to keep the syntax extremely similar
without reverting to a lispish sytax.

typing:  static (i guess - for type hinting mostly)
scoping: I'd like lexical but dynamic might be easier.
first class fns: of course, but how? Is everything anonymous kinda
like a Scheme.

*built in functions for buy/sell, built in data for current trades/depth
<order> (<price> <amount>)

*How to handle current orders, current depth?
could be indicator.. simply e.g.
dep = depth :SELL
outstanding = orders :BUY

possible indicators: depth, orders, volume, sma, low, high, previous (:open [<value> <unit>], :close [<value> <unit>])

; example of defining a function and calling it, setting the value to a variable
get-val = {a + b}
foo = get-val(1 2)
;; example of anonymous fn
foo = {a + b} (1 2)

daily-day-vol = volume :day 1
calc-something = {daily-day-vol / 3}
calculated-val = calc-something ()

vol = volume :month 1

fn = (int a double b) => (a + b)
OR
a = 1
b = 3
fn = (a + b)





<thing> ::= <indicator> <EOL>
          | <function> <EOL>

<indicator> ::= <name> <options>
<function> ::= 

<options> ::= <option>
            | <option> <options>

<option> ::= \":\"<name> <opt-value>

<order> ::= \"BUY\" | \"SELL\"

<unit> ::= \"second\" | \"minute\" | \"hour\" | \"day\" | \"month\" | \"year\"



So, \"If 20 day moving average crosses over 50 day moving average then buy\"

def 20-day-ma = sma [20 day]
def 50-day-ma = sma [50 day]


def decision = ([a b] (if (> a b) (BUY (* 1000 0.01))))
decision = {20-day-ma > 50-day-ma

**Wondering the value of a full syntax or should I just create
an agent representation with a set of indicators and functions?

example: somehow data is known
;;(defn agent [indicators functions]
;;  doseq [fn functions] (apply fn indicators))

	    
")
