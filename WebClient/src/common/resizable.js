/**
 * Created by Clay Parker on 7/21/2014.
 */
angular.module('AwayTeam.resizable', [])
.directive('resizable', function($window) {
    return function($scope) {
        $scope.initializeWindowSize = function() {
            $scope.windowHeight = $window.innerHeight;
            return $scope.windowWidth = $window.innerWidth;
        };
        $scope.initializeWindowSize();
        return angular.element($window).bind('resize', function() {
            $scope.initializeWindowSize();
            return $scope.$apply();
        });
    };
});