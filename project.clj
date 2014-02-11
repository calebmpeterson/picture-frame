(defproject picture-frame "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [conf-er "1.0.1"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.3"]
                 [org.clojure/data.json "0.2.4"]
                 [ororo "0.1.0"]
                 [net.mikera/imagez "0.3.1"]]
  :jvm-opts ["-Dconfig=config.conf" "-Xmx512M"]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler picture-frame.handler/app
         :port 3282}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
