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
;; Photo API
;;

(def max-frame-width 1680)
(def max-frame-height 1050)
(def max-photo-width (- max-frame-width 80))
(def max-photo-height (- max-frame-height 80))

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

(defn image->istream [^BufferedImage image]
  (let [os (java.io.ByteArrayOutputStream.)
        _  (javax.imageio.ImageIO/write image "png" os)
        is (java.io.ByteArrayInputStream. (.toByteArray os))]
    is))


(defn blur [^BufferedImage image]
  (imagez/filter-image
   (imagez-filter/box-blur 50 50) image))

(defn calculate-scale-factor [width height max-width max-height]
  (let [target-width  (min max-width width)
        target-height (min max-height height)]
    (min (double (/ target-width width)) (double (/ target-height height)))))

(defn resize [image max-width max-height]
  (let [scale-factor (calculate-scale-factor
                       (.getWidth image) (.getHeight image)
                       max-width max-height)]
    (imagez/zoom scale-factor image)))


;;
;; Response Helpers
;;

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/write-str data)})


;;
;; Controller Actions
;;

(defn handle-photo [path]
  (-> (str photo-lib path)
      (clojure.java.io/file)
      (javax.imageio.ImageIO/read)
      (resize max-photo-width max-photo-height)
      (image->istream)))

(defn handle-blur [path]
  (-> (str photo-lib path)
      (clojure.java.io/file)
      (javax.imageio.ImageIO/read)
      (blur)
      (resize max-frame-width max-frame-height)
      (image->istream)))


;;
;; Routes
;;

(defroutes app-routes
  (GET "/" [] (index))
  (GET "/sandbox" [] (sandbox))
  (GET "/next" [] (pathify photo-lib (rand-photo photo-lib)))
  (GET "/weather" [] (json-response (forecast-today default-location)))
  (GET ["/picture/:path", :path #".*"] [path] (handle-photo path))
  (GET ["/blur/:path",    :path #".*"] [path] (handle-blur path))
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
