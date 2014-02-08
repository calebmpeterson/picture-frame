(ns picture-frame.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :refer [redirect header]]
            [hiccup.element :as html]
            [clojure.data.json :as json]
            [picture-frame.layouts :refer [page]]
            [ororo.core :as ororo]
            [mikera.image.core :as imagez]
            [mikera.image.filters :as imagez-filter]
            [conf-er :refer :all])
  (:import [java.awt.image BufferedImage]))


;;
;; Components
;;

(defn widgets []
  [:div.widgets
   [:div#clock
    [:div#time.widget]
    [:div#date.widget]]
   [:div#weather
    [:img#weather-icon.widget]
    [:div#high.widget]
    [:div#low.widget]]])


;;
;; Pages
;;

(defn index []
  (page "Picture Frame"
        [:img#photo.photo-buffer]
        [:img#blur.photo-buffer]
        [:div.photo-frame]
        [:div.photo]
        (widgets)))


(defn sandbox []
  (page "Sandbox"
        [:div#sandbox]
        (widgets)))


;;
;; Photo API
;;

(def photo-lib (config :photo-library))

(defn rand-photo [lib-dir]
  (->> (clojure.java.io/file lib-dir)
    (file-seq)
    (filter #(.isFile %))
    (rand-nth)))

(defn pathify [lib-dir photo-file]
  (.. photo-file
    (getPath)
    (substring (count lib-dir))
    (replace "\\" "/")))

(defn blur [photo-file]
  (let [img    (javax.imageio.ImageIO/read photo-file)
        blured (imagez/filter-image
                 (imagez-filter/box-blur 50 50) img)
        os     (java.io.ByteArrayOutputStream.)
        _      (javax.imageio.ImageIO/write blured, "png", os)
        is     (java.io.ByteArrayInputStream. (.toByteArray os))]
    is))


;;
;; Weather API
;;

(def api-key (config :wunderground-api-key))
(def default-location (config :weather-location))

(defn forecast [location]
  (let [complete (ororo/forecast api-key location)
        textual  (:txt_forecast complete)
        daily    (:forecastday  textual)]
    complete))

(defn forecast-today [location]
  (let [complete (ororo/forecast api-key location)
        today    (get-in complete [:simpleforecast :forecastday 0])
        high     (get-in today [:high :fahrenheit])
        low      (get-in today [:low  :fahrenheit])
        icon-url (get-in today [:icon_url])
        icon     (get-in today [:icon])]
    {:high high, :low low, :icon_url icon-url, :icon icon}))


;;
;; Response Helpers
;;

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/write-str data)})


;;
;; Routes
;;

(defroutes app-routes
  (GET "/" [] (index))
  (GET "/sandbox" [] (sandbox))
  (GET "/next" [] (pathify photo-lib (rand-photo photo-lib)))
  (GET "/weather" [] (json-response (forecast-today default-location)))
  (GET ["/picture/:path", :path #".*"] [path] (clojure.java.io/file (str photo-lib path)))
  (GET ["/blur/:path",    :path #".*"] [path] (blur (clojure.java.io/file (str photo-lib path))))
  (route/resources "/")
  (route/not-found "Not Found"))


;;
;; Middleware
;;

(defn wrap-cache-control [handler path-prefix]
  (fn [request]
    (let [response (handler request)]
    (if (.startsWith (:uri request) path-prefix)
        (assoc-in response [:headers "Cache-Control"] "max-age=3600")
      response))))


;;
;; Router
;;

(def app
  (-> (handler/site app-routes)
      (wrap-cache-control "/picture/")
      (wrap-cache-control "/blur/")))
