package com.example.locadine.pojos

class RouteData {
    var routes = ArrayList<Routes>()
}

class Routes {
    var legs = ArrayList<Legs>()

}

class Legs {
    var distance = Distance()
    var duration = Duration()
    var steps = ArrayList<Steps>()
}

class Steps {
    var distance = Distance()
    var duration = Duration()
    var polyline = PolyLine()
}

class PolyLine {
    var points = ""
}

class Duration {
    var text = ""
    var value = 0
}

class Distance {
    var text = ""
    var value = 0
}



