/**
 * Created by Clay Parker on 8/4/2014.
 */
angular.module('AwayTeam.phoneFilter', []).filter('tel', function () {
        return function (tel) {
            if (!tel) { return ''; }

            var value = tel.toString().trim().replace(/^\+/, '');

            if (value.match(/[^0-9]/)) {
                return tel;
            }

            var country, city, number;

            switch (value.length) {
                case 7:
                    country = "";
                    city = "";
                    number = value;
                    break;
                case 10: // +1PPP####### -> C (PPP) ###-####
                    country = "";
                    city = value.slice(0, 3);
                    number = value.slice(3);
                    break;

                case 11: // +CPPP####### -> CCC (PP) ###-####
                    country = value[0];
                    city = value.slice(1, 4);
                    number = value.slice(4);
                    break;

                case 12: // +CCCPP####### -> CCC (PP) ###-####
                    country = value.slice(0, 3);
                    city = value.slice(3, 5);
                    number = value.slice(5);
                    break;

                default:
                    return tel;
            }



            number = number.slice(0, 3) + '-' + number.slice(3);

            var output = "";


            return city.length > 0 ?(country + " (" + city + ") " + number).trim() : (number).trim();
        };
    });