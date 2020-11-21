(ns three-body.core
  "Animates three html elements simulating the Three Body Problems.
  The three elements need to look like this:

	  <div id='body1'></div>
	  <div id='body2'></div>
	  <div id='body3'></div>

  You should also implement a function `threeBodyAnimationOn` 
  that returns true for the animation to run and false for the 
  animation to freeze.

  How you might want you use it is to run

      `lein cljsbuild once min`

  And serve the generated files in `js/compiled`

  ref. PHYSICS for more informations on the simulation
  "
  (:require ))

(def debug false)

;; DATASTRUCTURES

(declare render)
(defrecord Body [id x y vx vy ax ay out])
(def start-time 1000)
(def min-boundary 0)
(def max-boundary 100)
(def max-time-step 50)
(def offset 10)
(defn rand-x [] (+ (+ offset min-boundary)  (rand-int (- max-boundary (* 2 offset)))))
(defn body [id] (Body. id (rand-x) (rand-x) 0 0 0 0 false))
(defn init-bodies [n] (map (fn [id] (body (+ 1 id))) (range n)))
(def comb [[1 2] [0 2] [0 1]])

(def prev-ts (atom start-time))
(def stop-running-ts (atom nil))

(def bodies (atom (init-bodies 3)))

;; PHYSICS
;; Dynamical simulation of three bodies of equal mass
;; subjected to gravitational attraction in absence of friction.
;; The simulation tries to keep constant the overall system's energy
;; to avoid degenerates states.
;;
;; ref: http://nbabel.org/equations

(def adjusted-G 0.005)

(defn g-force [first_body second_body]
  "Compute gravitational attraction between two bodies
  ref: https://en.wikipedia.org/wiki/Newton%27s_law_of_universal_gravitation"
  (let [dy (- (:y first_body) (:y second_body)) 
        dx (- (:x first_body) (:x second_body))  
        angle (js/Math.atan2 dy dx)
        distance (js/Math.sqrt (+ (* dy dy) (* dx dx)))
        distance (max distance offset)
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

(defn wall-bounce 
  ([body axis velocity] 
   (let [first-wall (+ min-boundary offset )
         first-wall-hit (>= first-wall (get body axis))
         second-wall (- max-boundary offset)
         second-wall-hit (<= second-wall (get body axis))]
     (cond
       first-wall-hit (assoc body velocity (* -1 (get body velocity)) axis first-wall)
       second-wall-hit (assoc body velocity (* -1 (get body velocity)) axis second-wall)
       :else body)))
  ([bodies]
   (let [
         bodies (map (fn [body] (wall-bounce body :x :vx)) bodies)
         bodies (map (fn [body] (wall-bounce body :y :vy)) bodies)
         ] bodies)
   ))

(defn three-body-animation-off 
  "return false if animation is on, return decading dt if animation is off" [initial-dt] 
  (let [running (js/threeBodyAnimationOn)
        decay 0.1
        ts-nil (= @stop-running-ts nil)]
    (cond 
      (and (not running) ts-nil) (swap! stop-running-ts (fn [_ x] x) initial-dt) ;; first step after stopping
      (and (not running) (not ts-nil)) (swap! stop-running-ts (fn [prev] (max 0 (- prev decay) ))) ;; stopping
      (and running (not ts-nil)) (swap! stop-running-ts (fn [_ x] x) nil)  ;; first step after restarting
      )
    @stop-running-ts))

;; if you read the code until here we would probably have a nice chat together
;; so shoot me a DM at https://twitter.com/lessand_ro or mail at hello@lessand.ro 
;; and tell me what you are building

(defn step-physics-simulation [bodies dt]
  (let [
        ;; prevent velocity explosion for large lagged timesteps 
        running-dt (min dt max-time-step)
        ;; reduces gradually dt when stopping animation
        stopped-dt (three-body-animation-off running-dt)
        dt (if (not stopped-dt) running-dt stopped-dt) 
        bodies-i (step-accelleration bodies)
        bodies-i1 (step-position bodies-i dt)
        bodies-i1 (step-accelleration bodies-i1)
        bodies (step-velocities bodies-i bodies-i1 dt)
        bodies (wall-bounce bodies)
        ]
    (render bodies)
    (if debug (js/console.log dt))
    bodies))

;; RENDERING

(defn render [bodies] 
  (doseq [body bodies] 
    (let [style (.-style (.getElementById js/document (str "body" (:id body))))]
      (set! (.-bottom style) (str (:y body) "%"))
      (set! (.-left style) (str (:x body) "%")))))


(defn main [ts]
  (do
   (js/window.requestAnimationFrame main)  
   (if (> ts start-time)
     (do 
       (swap! bodies step-physics-simulation (- ts @prev-ts))
       (swap! prev-ts (fn [_ ts] ts) ts)))
   (if debug (js/console.log (clj->js bodies)))
   ))

(render @bodies)
(main 0)
