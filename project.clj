(defproject exchange "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [com.datomic/datomic-free "0.8.3372"]
      		 [incanter "1.3.0"]
                 [com.xeiam.xchange/xchange-core "1.3.0-SNAPSHOT"]
                 [com.xeiam.xchange/xchange-mtgox "1.3.0-SNAPSHOT"]]

  :warn-on-reflection true

  :repositories [["jetty" "mvnrepository.com/artifact"]
                 ["sonatype"
                  {:url "https://oss.sonatype.org/content/repositories/snapshots"
                   :snapshots true
                   :update :daily}]
                 ]
  )
