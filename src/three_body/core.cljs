(ns three-body.core
    (:require ))

(def debug false)

;; DATASTRUCTURES

(declare render)
(defrecord Body [id x y vx vy ax ay out])
(def min-boundary 0)
(def max-boundary 100)
(def initial-offset 3)
(defn rand-x [] (+ (+ initial-offset min-boundary)  (rand-int (- max-boundary (* 2 initial-offset)))))
(defn body [id] (Body. id (rand-x) (rand-x) 0 0 0 0 false))
(defn init-bodies [n] (map (fn [id] (body (+ 1 id))) (range n)))
(def comb [[1 2] [0 2] [0 1]])
(def prev-ts (atom 0))
(def bodies (atom (init-bodies 3)))

;; PHYSICS

(def adjusted-G 0.05)

;; https://en.wikipedia.org/wiki/Verlet_integration#Velocity_Verlet
;; http://nbabel.org/equations
;; https://physics.stackexchange.com/a/404826/279554

(defn g-force [first_body second_body]
  "Compute gravitational attraction between two bodies
  ref: https://en.wikipedia.org/wiki/Newton%27s_law_of_universal_gravitation"
  (let [dy (- (:y first_body) (:y second_body)) 
        dx (- (:x first_body) (:x second_body))  
        angle (js/Math.atan2 dy dx)
        distance (js/Math.sqrt (+ (* dy dy) (* dx dx)))
        magnitude (/ adjusted-G (* distance distance))]
    [(* -1 magnitude (js/Math.cos angle)) (* magnitude -1 (js/Math.sin angle))]))

(defn vector-sum [vectors]
  (reduce (fn [acc x] [(+ (first acc) (first x)) (+ (second acc) (second x))]) vectors))

(defn step-accelleration
 ([i bodies] 
  "Compute accelleration for i-th body"
  (let [acc-components (map 
                           (fn [j] (g-force (nth bodies i) (nth bodies j)))
                           (nth comb i))]
    (vector-sum acc-components))) 
 ([bodies]
   (map-indexed (fn [i body] (let [[ax ay] (step-accelleration i bodies)]
                  (assoc body :ax ax :ay ay)))
                bodies )))

(defn step-position [bodies dt]
  "Integrate position with velocity verlet method 
  ref: https://en.wikipedia.org/wiki/Verlet_integration#Velocity_Verlet"
  (let [integrator-1d (fn [x v a] (+ x (* v dt) (* 0.5 a (* dt dt))))
        body-integrator (fn [body] (assoc body 
                                         :x (integrator-1d (:x body) (:vx body) (:ax body))
                                         :y (integrator-1d (:y body) (:vy body) (:ay body))
                                         ))]
    (map body-integrator bodies)))

(defn step-velocities [bodies-i bodies-i1 dt]
  "Integrate velocities with velocity verlet method 
  ref: https://en.wikipedia.org/wiki/Verlet_integration#Velocity_Verlet"
  (let [integrator-1d (fn [v a a1] (+ v (* 0.5 (+ a a1) dt)))
        body-integrator (fn [i body] (assoc body 
                                         :vx (integrator-1d (:vx (nth bodies-i i)) (:ax (nth bodies-i i)) (:ax body))
                                         :vy (integrator-1d (:vy (nth bodies-i i)) (:ay (nth bodies-i i)) (:ay body))
                                         ))]
    (map-indexed body-integrator bodies-i1)) 
  )

(defn step-physics-simulation [bodies dt]
  (let [bodies-i (step-accelleration bodies)
        bodies-i1 (step-position bodies-i dt)
        bodies-i1 (step-accelleration bodies-i1)
        bodies (step-velocities bodies-i bodies-i1 dt)]
    (render bodies)
    (if debug (js/console.log dt))
    bodies))

;; RENDERING

(defn out-of-boundaries [bodies]
  (filter (fn [body] (and (>= min-boundary (:x body)) (<= (:y body) max-boundary))) bodies))

(defn render [bodies] 
  (doseq [body bodies] 
    (let [style (.-style (.getElementById js/document (str "body" (:id body))))]
      (set! (.-bottom style) (str (:y body) "%"))
      (set! (.-left style) (str (:x body) "%")))))

(defn main [ts]
  (do
   (js/window.requestAnimationFrame main)  
   (swap! bodies step-physics-simulation (- ts @prev-ts))
   ;(swap! bodies out-of-boundaries)
   (swap! prev-ts (fn [_ ts] ts) ts)
   (if debug (js/console.log (clj->js bodies)))
   ))

(main 0)
