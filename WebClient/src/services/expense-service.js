/**
 * Created by Clay Parker on 7/20/2014.
 */
angular.module('expenseService', [])
    .provider('expenseService', function () {
        var errorState = 'error',
            logoutState = 'home';

        this.$get = function ($rootScope, $http, $q, $state, $log) {

            /**
             * Low-level, private functions.
             */


            /**
             * High level, public methods
             */
            var wrappedService = {
                /**
                 * Public properties
                 */

                teamExpenses: [],
                selectedExpense: null,

                createExpense: function(description, amount, expenseDate, teamId, loginId, expenseType){
                    var postData = {description:description, amount:amount, expDate:expenseDate, teamId:teamId, loginId:loginId, expType:expenseType};
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/expense/createexpense",
                        method: "POST",
                        data: postData
                    });

                    return promise;
                },
                modifyExpense: function(expenseId, description, amount, expenseDate, teamId, loginId, expenseType){
                    var postData = {expenseId:expenseId, description:description, amount:amount, expDate:expenseDate, teamId:teamId, loginId:loginId, expType:expenseType};
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/expense/modifyexpense",
                        method: "POST",
                        data: postData
                    });

                    return promise;
                },
                deleteExpense: function(expenseId){
                    var postData = {expenseId:expenseId};
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/expense/deleteexpense",
                        method: "POST",
                        data: postData
                    });

                    return promise;
                },
                getTeamExpenses: function(loginId, teamId){
                    var postData = {loginId:loginId, teamId:teamId};
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/expense/getexpense",
                        method: "POST",
                        data: postData
                    });
                    promise.success(function(data){
                        wrappedService.selectedExpense = null;
                        wrappedService.teamExpenses = data.response;
                    });

                    return promise;
                },
                getExpense: function(loginId, teamId, expenseId){
                    var postData = {loginId:loginId, teamId:teamId, expenseId:expenseId};
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/expense/getexpense",
                        method: "POST",
                        data: postData
                    });
                    return promise;
                },
                getTeamExpensesByType: function(loginId, teamId, reqType){
                    var postData = {loginId:loginId, teamId:teamId, reqType:reqType};
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/expense/getexpense",
                        method: "POST",
                        data: postData
                    });

                    return promise;
                },
                getExpensesByDate: function(loginId, teamId, reqDate){
                    var postData = {loginId:loginId, teamId:teamId, reqDate:reqDate};
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/expense/getexpense",
                        method: "POST",
                        data: postData
                    });

                    return promise;
                },
                getReceipt: function(loginId, teamId, expenseId){
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/Expense/GetReceipt?loginId="+loginId+"&teamId="+teamId+"&expenseId="+expenseId,
                        method: "GET"
                    });

                    return promise;
                },
                putReceipt: function(loginId, teamId, expenseId, file, auth, authChallenge){
                    var fd = new FormData();
                    fd.append('loginId', loginId);
                    fd.append('teamId', teamId);
                    fd.append('expenseId', expenseId);
                    fd.append('file', file);
                    fd.append('AWT_AUTH', auth);
                    fd.append('AWT_AUTH_CHALLENGE', authChallenge);


                    var promise = $http.post("https://api.awayteam.redshrt.com/expense/putreceipt", fd, {
                        transformRequest: angular.identity,
                        headers: {'Content-Type': undefined}
                    });

                    promise.success(function(data){
                        $log.info("Successfully uploaded receipt for expense: "+expenseId+" on team: "+teamId);
                        wrappedService.getTeamExpenses(loginId, teamId);
                    });

                    promise.error(function(data){
                        $log.error("Failed to upload receipt for expense: "+expenseId+" on team: "+teamId);
                    });

                    return promise;
                }
            };

            return wrappedService;
        };
    });
