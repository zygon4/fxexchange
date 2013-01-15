(ns exchange.db.datomic
  (:use clojure.repl
        clojure.java.javadoc)
  (:use [datomic.api :as d]))

(def uri "datomic:free://localhost:4334/seattle")
(def path "/home/zygon/external/lab/lib/datomic-free-0.8.3372/samples/")

(defn load-schema
  []
  (let [_ (d/create-database uri)
        conn (d/connect uri)
        schema-tx (read-string
                   (slurp (str path "seattle/seattle-schema.dtm")))]
    @(d/transact conn schema-tx)
    conn ;;; return connection
    ))

(defn load-data
  [conn]
  (when-let [_ conn]
    (let [data-tx (read-string
                   (slurp (str path "seattle/seattle-data0.dtm")))]
      @(d/transact conn data-tx))))

(defn test-q
  [con]
  (let [results (q '[:find ?c :where [?c :community/name]] (db con))
        id (ffirst results)
        entity (-> con db (d/entity id))]
    (keys entity)
    (:community/name entity)))


(defn perf-test
  [[inserts updates deletes]]
  (try
    
    (println (str "creating db.." d/create-database uri))

    (let [con (d/connect uri)
          schema-tx (read-string
                     (slurp (str path "seattle/seattle-schema.dtm")))
          data-tx (read-string
                   (slurp (str path "seattle/seattle-data0.dtm")))]
      @(d/transact con schema-tx)
      
      (time
       (dotimes [_ inserts] ;;; todo: single insert vs entire dataset
         (println "hi")
         @(d/transact con data-tx)))
           
      ;; todo: updates/deletes
      )
    (catch Throwable t (.printStackTrace t))
    (finally
       ;;; is close expected?
     (println "deleting db..")
     (delete-database uri))))