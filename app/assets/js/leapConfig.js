angular.module("leap", ['angular-leap']);

angular.module("leap")
    .run(function ($rootScope) {
        $rootScope.left     = function() {console.log("left")};
        $rootScope.up       = function() {console.log("up")};
        $rootScope.down     = function() {console.log("down")};
        $rootScope.right    = function() {console.log("right")};
		$rootScope.circle	= function() {console.log("circle")};
    });


