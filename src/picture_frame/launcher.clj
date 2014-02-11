(ns picture-frame.launcher
  (:require [ring.adapter.jetty :as jetty]
            [picture-frame.handler :refer [app]]))

(defn -main [port]
  (jetty/run-jetty app {:port (Integer. port) :join? false}))