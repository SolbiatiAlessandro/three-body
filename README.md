# three-body

Browser based physical simulation of three bodies of equal mass
subjected to gravitational attraction in absence of friction written in [ClojureScript](https://clojurescript.org/).

The simulation tries to keep constant the overall system's energy
to avoid degenerates states.

ref: http://nbabel.org/equations

**[https://youtube.com/6D9ARLVxytM](https://www.youtube.com/watch?v=6D9ARLVxytM)**  

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/6D9ARLVxytM/0.jpg)](https://www.youtube.com/watch?v=6D9ARLVxytM)

## Usage

Animates three html elements simulating the Three Body Problems.
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


## Setup

To get an interactive development environment run:

    npm install
    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
