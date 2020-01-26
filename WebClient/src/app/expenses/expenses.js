/**
 * Each section of the site has its own module. It probably also has
 * submodules, though this boilerplate is too simple to demonstrate it. Within
 * 'src/app/home', however, could exist several additional folders representing
 * additional modules that would then be listed as dependencies of this one.
 * For example, a 'note' section could have the submodules 'note.create',
 * 'note.delete', 'note.edit', etc.
 *
 * Regardless, so long as dependencies are managed correctly, the build process
 * will automatically take take of the rest.
 */
(function(app) {

    app.config(['$stateProvider', function ($stateProvider) {
        $stateProvider.state('expenses', {
            url: '/expenses',
            views: {
                "main": {
                    controller: 'ExpensesController',
                    templateUrl: 'expenses/expenses.tpl.html'
                }
            },
            data:{ pageTitle: 'Expenses' },
            accessLevel: accessLevels.public
        });
    }]);

    // As you add controllers to a module and they grow in size, feel free to place them in their own files.
    //  Let each module grow organically, adding appropriate organization and sub-folders as needed.
    app.controller('ExpensesController', ['$scope', '$state', '$upload', '$http', '$modal', 'teamService', 'loginService', 'expenseService', 'growlNotifications', 'ExpenseTypeList',
        function ($scope, $state, $upload, $http, $modal, teamService, loginService, expenseService, growlNotifications, ExpenseTypeList) {
            $scope.teamService = teamService;
            $scope.expenseService = expenseService;
            $scope.selectedExpense = [];
            $scope.teamExpenses = [];
            $scope.expenseTotal = 0;

            $scope.getExpenseData = function() {
                if(teamService.selectedTeam.teamId == null){
                    $state.go('home');
                    return;
                }
                expenseService.getTeamExpenses(loginService.user.loginId, teamService.selectedTeam.teamId);
            };

            $scope.getExpenseData();

            $scope.expenseTypes = ExpenseTypeList;

            $scope.cellInputEditableTemplate = '<input ng-class="\'colt\' + col.index" ng-input="COL_FIELD" ng-model="COL_FIELD" />';
            $scope.cellSelectEditableTemplate = '<select ng-cell-input ng-class="\'colt\' + col.index" ng-input="COL_FIELD" ng-model="COL_FIELD" ng-options="type.name for type in expenseTypes" data-placeholder="-- Select One --" />';

            $scope.expenseGridOptions = {
                data: 'teamExpenses',
                multiSelect: false,
                enableRowSelection: true,
                enableCellEditOnFocus: true,
                selectedItems: $scope.selectedExpense,
                columnDefs: [
                    { field: 'description', displayName: 'Description', enableCellEdit: true, editableCellTemplate: $scope.cellInputEditableTemplate  },
                    { field: 'expType',   displayName: 'Type', enableCellEdit: true, editableCellTemplate: $scope.cellSelectEditableTemplate,
                        cellFilter: 'ExpenseTypeMap'    },
                    { field: 'expDate',  displayName: 'Date', enableCellEdit: false , cellFilter: "moment" },
                    { field: 'amount',  displayName: 'Total', enableCellEdit: true, editableCellTemplate: $scope.cellInputEditableTemplate   },
                    { field:'receipt', displayName:'Receipt', enableCellEdit: false, cellTemplate:
                        '<div class="ngCellText"> <i ng-class="\'fa icon-file-text-alt pull-right\'" tooltip="Add Receipt" ng-file-select="onFileSelect($files)" accept="image/*"></i>' +
                        ' <i ng-class="\'fa icon-paper-clip pull-right\'" ng-show="row.getProperty(col.field) == true" ng-click="viewReceipt()" tooltip="View Receipt"></i></div>'}
                ],
                plugins: [ngGridCustomFlexibleHeightPlugin]
            };

            $scope.$on('ngGridEventEndCellEdit', function(data) {
                if($scope.selectedExpense[0] != null){
                    var exp = $scope.selectedExpense[0];
                    if(exp.expType.id != null){
                        exp.expType = exp.expType.id;
                    }
                    var updatePromise = expenseService.modifyExpense(exp.expenseId, exp.description, exp.amount, exp.expDate, exp.teamId, loginService.user.loginId, exp.expType);
                    updatePromise.success(function(data){
                        if(data.response == "success"){
                            growlNotifications.add('Successfully Updated Expense!', 'success');
                            $scope.getExpenseData();
                        }else{
                            growlNotifications.add('Failed to Update Expense', 'danger');
                        }
                    });
                    updatePromise.error(function(){
                        growlNotifications.add('Failed to Update Expense', 'danger');
                    });
                }else{
                    growlNotifications.add('No expense was selected.', 'danger');
                }
            });

            $scope.viewReceipt = function(){
                var modalInstance = $modal.open({
                    templateUrl: 'expenses/viewReceipt.tpl.html',
                    controller: 'ViewReceiptModalCtrl'
                });
            };

            $scope.onFileSelect = function($files) {
                var file = $files[0];
                var auth = "";
                var authChallenge = "";

                if (loginService != null && loginService.userIdentifier != null && loginService.user != null && loginService.isLoggedIn) {
                    var unixTimeStamp                   = Math.round((new Date()).getTime() / 1000);
                    var challenge                       = unixTimeStamp + loginService.user.loginId.toLowerCase() + loginService.userIdentifier;
                    authChallenge                       = CryptoJS.HmacSHA256(challenge, loginService.userSecret).toString(CryptoJS.enc.Hex);
                    auth                = loginService.userIdentifier;
                }else{
                    return;
                }


                var promise = expenseService.putReceipt(loginService.user.loginId, $scope.selectedExpense[0].teamId, $scope.selectedExpense[0].expenseId, file, auth, authChallenge);

                promise.success(function(){
                    growlNotifications.add("Successfully uploaded receipt for expense "+$scope.selectedExpense[0].description, 'success');
                });

                promise.error(function(){
                    growlNotifications.add("Failed to upload receipt for expense "+$scope.selectedExpense[0].description, 'danger');
                });
            };

            $scope.expenseNotSelected = function() {
                if ($scope.selectedExpense[0] != null) {
                    return false;
                }
                else {
                    return true;
                }
            };

            $scope.$watch(function () {
                    return teamService.selectedTeam;
                },
                function(newVal, oldVal) {
                    $scope.getExpenseData();
                }, true);


            $scope.$watch(function () {
                    return expenseService.teamExpenses;
                },
                function(newVal, oldVal) {
                    $scope.teamExpenses = expenseService.teamExpenses;
                    var sum = 0;
                    for(var i=0; i < $scope.teamExpenses.length; i++){
                        sum += parseFloat($scope.teamExpenses[i].amount);
                    }
                    $scope.expenseTotal = sum;
                }, true);

            $scope.$watch(function () {
                    return $scope.selectedExpense;
                },
                function(newVal, oldVal) {
                    if($scope.selectedExpense.length > 0){
                        expenseService.selectedExpense = $scope.selectedExpense[0];
                    }else{
                        expenseService.selectedExpense = null;
                    }
                }, true);


            $scope.addExpenseModal = function () {
                var modalInstance = $modal.open({
                    templateUrl: 'expenses/addExpense.tpl.html',
                    controller: 'AddExpenseModalCtrl'
                });
            };

            $scope.deleteExpense = function () {
                if($scope.expenseNotSelected() === false){
                    var deletePromise = expenseService.deleteExpense($scope.selectedExpense[0].expenseId);
                    deletePromise.success(function(data){
                        if(data.response == "success"){
                           growlNotifications.add('Successfully Deleted Expense!', 'success');
                           $scope.getExpenseData();
                        }else{
                            growlNotifications.add('Failed to Delete Expense', 'danger');
                        }
                    });
                    deletePromise.error(function(){
                        growlNotifications.add('Failed to Delete Expense', 'danger');
                    });
                }

            };

    }]);


    app.controller('AddExpenseModalCtrl', ['$scope', '$modalInstance', '$http', '$timeout', 'teamService', 'loginService', 'expenseService', 'growlNotifications', 'ExpenseTypeList', function ($scope, $modalInstance, $http, $timeout, teamService, loginService, expenseService, growlNotifications, ExpenseTypeList) {
        $scope.addExpense = {expType:1};
        $scope.addExpenseWorking = false;
        $scope.expenseTypes = ExpenseTypeList;

        $scope.today = function() {
            $scope.expDate = new Date();
        };
        $scope.today();

        $scope.clear = function () {
            $scope.expDate = null;
        };

        $scope.toggleMin = function() {
            $scope.minDate = $scope.minDate ? null : new Date();
        };
        $scope.toggleMin();

        $scope.open = function() {

            $timeout(function() {
                $scope.opened = true;
            });
        };

        $scope.dateOptions = {
            formatYear: 'yy',
            startingDay: 1
        };

        $scope.format = 'yyyy-MM-dd';


        $scope.ok = function () {
            var expense = $scope.addExpense;
            var date = expense.expDate.getFullYear() + "-" + (expense.expDate.getMonth()+1) + "-" + expense.expDate.getDate();
            $scope.addExpenseWorking = true;
            var addExpensePromise = expenseService.createExpense(expense.description, expense.amount, date, teamService.selectedTeam.teamId, loginService.user.loginId, expense.expType.id);
            addExpensePromise.success(function(expense, status, headers, config) {
                if (expense.response === "success") {
                    $scope.addExpenseWorking = false;
                    growlNotifications.add('Successfully Added Expense!', 'success');
                    expenseService.getTeamExpenses(loginService.user.loginId, teamService.selectedTeam.teamId);
                    $modalInstance.close();
                }
            });
            addExpensePromise.error(function () {
                $scope.addExpenseWorking = false; //TODO present error message to the user
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

    app.controller('ViewReceiptModalCtrl', ['$scope', '$modalInstance', 'expenseService', 'loginService', 'teamService',  function ($scope, $modalInstance, expenseService, loginService, teamService) {
        $scope.imageSource = '';
        $scope.expense = {};

        $scope.getReceipt = function(){
            var receiptPromise = expenseService.getReceipt(loginService.user.loginId, teamService.selectedTeam.teamId, expenseService.selectedExpense.expenseId);

            receiptPromise.success(function(data){
                $scope.imageSource =data;
                console.log($scope.imageSource);
            });

            receiptPromise.error(function(data){
                console.log(data);
                $scope.imageSource = '';
            });
        };

        $scope.$watch(function () {
                return expenseService.selectedExpense;
            },
            function(newVal, oldVal) {
                if(expenseService.selectedExpense == null){
                    $scope.imageSource = '';
                }else{
                    $scope.expense = expenseService.selectedExpense;
                    $scope.imageSource = "https://api.awayteam.redshrt.com/Expense/GetReceipt?loginId="+loginService.user.loginId+"&teamId="+teamService.selectedTeam.teamId+"&expenseId="+expenseService.selectedExpense.expenseId;
                }
            }, true);

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);


    app.directive('ngConfirmClick', [
        function(){
            return {
                link: function (scope, element, attr) {
                    var msg = attr.ngConfirmClick || "Are you sure?";
                    var clickAction = attr.confirmedClick;
                    element.bind('click',function (event) {
                        if ( window.confirm(msg) ) {
                            scope.$eval(clickAction);
                        }
                    });
                }
            };
        }]);

    app.factory('ExpenseTypeList', function() {
        return [
            {id:1, name: 'Breakfast'},
            {id:2, name:'Lunch'},
            {id:3, name:'Dinner'},
            {id:4, name:'Snack'},
            {id:5, name:'Other'}
        ];
    });

    app.filter('ExpenseTypeMap', function(ExpenseTypeList) {

        return function(input) {

            var typesMatched = ExpenseTypeList.filter(function(type) {
                if(input.id == null && input.name == null) {
                    return type.id == input;
                }
                else if(input.id == null){
                    return type.name.toLowerCase() == input.name;
                }
                else {
                    return type.id == input.id;
                }
            });

            if (typesMatched.length == 1) {
                return typesMatched[0].name;
            }
            else {
                return '*unknown type*';
            }


            return text;
        };
    });

    app.filter('moment', function () {
        return function (dateString) {
            var date = new Date(dateString);
            return (date.getMonth()+1) + "-" +   date.getDate() + "-" + date.getFullYear();
        };
    });

// The name of the module, followed by its dependencies (at the bottom to facilitate enclosure)
}(angular.module("AwayTeam.expenses", [
    'AwayTeam.router'
])));