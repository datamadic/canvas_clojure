(ns from-scratch.core
  (:require-macros [
                    from-scratch.macros :as m
                    ]))


(enable-console-print!)

(println (m/m 4))
; (println (m/m 4))

(def canvas (js/document.getElementById "canvas"))
(def context (.getContext canvas  "2d" ))

(set! (.-fillStyle context) "rgba(0, 0, 200, 0.8)")

;; x y width height
(.strokeRect context  5 5  100 100)
(.fillRect context 10 10 100 100)
(.clearRect context 60 60 100 100)

(defn rect-at [x, y, ctx]
  (.strokeRect ctx  x y  100 100))

(defn clear-at [x, y, ctx]
  (.clearRect ctx  x y  100 100))

(.addEventListener  canvas "mousemove" #(rect-at
                                         (- (.-offsetX %) 50)
                                         (- (.-offsetY %) 50)
                                         ;(.-offsetY %) 
                                         context))

(.addEventListener  canvas "click" #(clear-at
                                         (- (.-offsetX %) 50)
                                         (- (.-offsetY %) 50)
                                         context))
;ctx.fillStyle = "green";
;ctx.fillRect(10, 10, 100, 100);

(println "this is the context" canvas)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

