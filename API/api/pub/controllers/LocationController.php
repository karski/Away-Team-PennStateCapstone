<?php
    
    //Owner: David Vu
    
    require_once('/home/awayteam/api/pub/models/Location.php');
    require_once('/home/awayteam/api/pub/apiconfig.php');
    
    class LocationController extends Location
    {
        public function CreateLocation($locationParametersArray) {
            $aLocation = new Location;
            $aLocation = arrayToObject($locationParametersArray);
            $newLocationId = $aLocation->InsertLocation();
        }
        
        public function GetAllLocations() {
            return $this->SelectAllLocations();
        }
        
        public function GetLocationFromId($locId) {
            return $this->SelectLocationFromLocationId($locId);
        }
    }
    
?>