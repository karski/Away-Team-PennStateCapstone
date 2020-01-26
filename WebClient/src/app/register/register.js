(function(app) {

    app.config(['$stateProvider', function ($stateProvider) {
      $stateProvider.state('register', {
          url: '/register',
          views: {
                "main": {
                    controller: 'RegisterController',
                    templateUrl: 'register/register.tpl.html'
                }
          },
          data:{ pageTitle: 'Register' },
          accessLevel: accessLevels.public
        });
    }]);

    app.controller('RegisterController', ['$scope', '$http', '$timeout', '$state', function ($scope, $http, $timeout, $state) {
      $scope.xhr = false;
      $scope.redirect = false;

      $scope.registerObj = {
      };

      $scope.registerUser = function (formInstance) {
        // xhr is departing
        $scope.xhr = true;
       // var user = $http.param($scope.registerObj);
        //alert(''+user);
        $http({
            url:"https://api.awayteam.redshrt.com/user/createuser",
            method: "POST", 
            data: $scope.registerObj
          })
        .success(function(data, status, headers, config) {
          console.info('post success - ', data);
          if(data.response === "failure"){
            return;
          }
          $scope.xhr = false;
          $scope.redirect = true;
          $timeout(function () {
            $state.go('home');
          }, 2000);
        }).error(function(data, status, headers, config) {
             data.errors.forEach(function (error, index, array) {
                formInstance[error.field].$error[error.name] = true;
              });
              formInstance.$setPristine();
              console.info('post error - ', data);
              $scope.xhr = false;
        });

      };
    }]);

    app.directive('userUnique', ['$http', function (async) {
        return {
            require: 'ngModel',
            link: function (scope, elem, attrs, ctrl) {

                elem.on('blur', function (evt) {
                    scope.$apply(function () {
                        var val = elem.val();
                        if (val !== "") {
                            var ajaxConfiguration = { method: 'GET', url: 'https://api.awayteam.redshrt.com/user/LoginIDExist?loginId=' + val};
                            async(ajaxConfiguration)
                                .success(function (data, status, headers, config) {
                                    if (data.message === "not available") {
                                        ctrl.$setValidity('unique', false);
                                    } else {
                                        ctrl.$setValidity('unique', true);
                                    }
                                });
                        }
                    });
                });
            }
        };
    }]);

    app.directive('emailUnique', ['$http', function (async) {
        return {
            require: 'ngModel',
            link: function (scope, elem, attrs, ctrl) {

                elem.on('blur', function (evt) {
                    scope.$apply(function () {
                        var val = elem.val();
                        if (val !== "") {
                            var ajaxConfiguration = { method: 'GET', url: 'https://api.awayteam.redshrt.com/user/EmailExist?email=' + val};
                            async(ajaxConfiguration)
                                .success(function (data, status, headers, config) {
                                    if (data.message === "not available") {
                                        ctrl.$setValidity('emailUnique', false);
                                    } else {
                                        ctrl.$setValidity('emailUnique', true);
                                    }
                                });
                        }
                    });
                });
            }
        };
    }]);

}(angular.module("AwayTeam.register", [
    'AwayTeam.router'
])));


