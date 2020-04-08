angular.module('exercise', [])
    .run(function ($rootScope, $http) {

        $rootScope.getServiceList = function () {
            $http.get('/service').then(function (result) {
                $rootScope.serviceList = result.data;
            });
        };

        $rootScope.addService = function (url, description) {
            $http.post('/service/', {
                url: url,
                description: description
            }).then(function (result) {
                $rootScope.url = undefined;
                $rootScope.description = undefined;
                $rootScope.getServiceList();
            });
        };

        $rootScope.editService = function (service) {
            $rootScope.editServiceId = service.id;
            $rootScope.editServiceUrl = service.url;
            $rootScope.editServiceDescription= service.description;
        };

        $rootScope.updateService = function (serviceId, url, description) {
            $http.put('/service/' + serviceId, {
                url: url,
                description:description
            }).then(function (result) {
                $rootScope.editServiceId = undefined;
                $rootScope.editServiceUrl = undefined;
                $rootScope.editServiceDescription = undefined;
                $rootScope.getServiceList();
            });
        };

        $rootScope.removeService = function (serviceId) {
            $http.delete('/service/' + serviceId).then(function (result) {
                $rootScope.getServiceList();
            });
        };


        $rootScope.getServiceList();
    });