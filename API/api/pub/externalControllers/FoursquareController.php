<?php
    //OWNER: S. NAIMOLI

    require_once('/home/awayteam/api/pub/apiconfig.php');

    $client_id      = '435V4BJK54VBSSG142Y0PQBJ0XPW4SCCKPXYQ3WVIYLXYCVA';
    $client_secret  = 'KQQJOCFHO1OGFTRRFMNZIAK2MU5ITQKSXYJ34MSDVEBSLBXS';
    $version        = '20140701';

    $category['artsEnt']     = "4d4b7104d754a06370d81259";
    $category['food']        = "4d4b7105d754a06374d81259";
    $category['nightlife']   = "4d4b7105d754a06376d81259";
    $category['shopServ']    = "4d4b7105d754a06378d81259";
    $category['travTrans']   = "4d4b7105d754a06379d81259";

    //logic functions
    class FoursquareController 
    {
        public function FindSpot($criteria)
        {
            global $category;
            global $client_id;
            global $client_secret;
            global $version;

            $search = array();

            if (isset($criteria['searchMethod']) and isset($criteria['searchValue']))
            {
                switch ($criteria['searchMethod'])
                {
                    case "ll":
                        $search['ll'] = $criteria['searchValue'];
                        break;
                    case "near";
                        $search['near'] = $criteria['searchValue'];
                        break;
                }
            }

            if (isset($criteria['category']))
            {
                switch($criteria['category'])
                {
                    case "artsEnt":
                        $search['categoryId'] = $category['artsEnt'];
                        break; 
                    case "food":
                        $search['categoryId'] = $category['food'];
                        break;
                    case "nightlife":
                        $search['categoryId'] = $category['nightlife'];
                        break;
                    case "shopServ":
                        $search['categoryId'] = $category['shopServ'];
                        break;
                    case "travTrans":
                        $search['categoryId'] = $category['travTrans'];
                        break;
                }
            }

            if (isset($criteria['query']))
            {
                $search['query'] = $criteria['query'];
            }
                       
 
            if (isset($criteria['radius']))
            {
                $miles = $criteria['radius'] + 0;
                $meters = $miles * 1609;
                
                $search['radius'] = $meters;
            }

            if (isset($criteria['limit']))
            {
                $search['limit'] = $criteria['limit'] + 0;
            }
            

            $search['intent']           = "browse";
            $search['client_id']        = $client_id;
            $search['client_secret']    = $client_secret;
            $search['v']                = $version;

            $json = $this->CallAPI("GET", "https://api.foursquare.com/v2/venues/search", $search);
            
            $json = stripslashes($json);
            $results = json_decode($json,true);

            $results = $this->FormatResults($results); 
            
            return $results;
        }

        private function FormatResults($data)
        {
            error_reporting(E_ERROR);  //incase of null values

            $tmp = $data;
            
            if (isset($tmp['response']['venues']))
            {
                $origSpots = $tmp['response']['venues'];
            }

            $spots = array();

            foreach($origSpots as $venue)
            {
                $place['name']      = $venue['name'];
                $place['url']       = $venue['url'];
                $place['phone']     = $venue['contact']['phone'];
                $place['location']  = $venue['location'];
                $place['category']  = $venue['categories'][0]['shortName'];
                $place['menu']      = $venue['menu']['mobileUrl'];
                $place['crowd']     = $venue['hereNow']['count'];
                
                unset($place['location']['formattedAddress']);
            
                //convert meters to feet
                $place['location']['distance'] = $place['location']['distance'] + 0;
                $place['location']['distance'] = round($place['location']['distance'] * 3.28084, 0);
                

                array_push($spots, $place);
            }                
            
            return $spots;
        }
        
        private function CallAPI($method, $url, $data = false)
        {
            //http://stackoverflow.com/questions/9802788/call-a-rest-api-in-php
            $curl = curl_init();
            
            switch ($method)
            {
                case "POST":
                    curl_setopt($curl, CURLOPT_POST, 1);
                    
                    if ($data)
                        curl_setopt($curl, CURLOPT_POSTFIELDS, $data);
                    break;
                case "PUT":
                    curl_setopt($curl, CURLOPT_PUT, 1);
                    break;
                default:
                    if ($data)
                        $url = sprintf("%s?%s", $url, http_build_query($data));
            }
            
            curl_setopt($curl, CURLOPT_URL, $url);
            curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
        
            return curl_exec($curl);
        }

    }
?>
