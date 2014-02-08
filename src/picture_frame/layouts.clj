(ns picture-frame.layouts
  (:use [hiccup core page]))


(defn page [title & content]
  (html5
   [:head
    [:title title]
    [:link {:rel "shortcut icon" :href "/img/picture.png"}]
    (include-css "/css/main.css")
    (include-js "/libs/jquery/jquery-2.1.0.min.js")
    (include-js "/libs/moment/moment.min.js")
    (include-js "/libs/underscore/underscore-min.js")
    (include-js "/js/script.js")]
   [:body content]))