(function(app) {

    app.config(['$stateProvider', function ($stateProvider) {
      $stateProvider.state('editAccount', {
          url: '/editAccount',
          views: {
                "main": {
                    controller: 'EditUserController',
                    templateUrl: 'editAccount/editAccount.tpl.html'
                }
          },
          data:{ pageTitle: 'Edit Account' },
          accessLevel: accessLevels.public
        });
    }]);

    app.controller('EditUserController', ['$scope', '$http', '$timeout', '$state', function ($scope, $http, $timeout, $state) {
      $scope.xhr = false;
      $scope.redirect = false;

      $scope.setUserInfo = function(){
            var test = 2 +2;
            var hi = "";
      };

      $scope.editUser = function (formInstance) {
        // xhr is departing
        $scope.xhr = true;
       // var user = $http.param($scope.registerObj);
        //alert(''+user);
        $http({
            url:"https://api.awayteam.redshrt.com/user/modifyuser",
            method: "POST", 
            data: $scope.editUserObj
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

}(angular.module("AwayTeam.editAccount", [
    'AwayTeam.router'
])));


