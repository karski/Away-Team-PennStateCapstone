<?php
    
    include_once('/home/awayteam/api/pub/apiconfig.php');
    include_once('/home/awayteam/api/pub/models/TeamUtilities.php');
    
    class Location
    {
        public $locId;
        public $locName;
        public $locLatitude;
        public $locLongitude;
        
        public function __construct() {
            $this->initialize();
        }
        
        public function initialize() {
            $this->locId = -999;
            $this->locName = "";
            $this->locLatitude = "";
            $this->locLongitude = "";            
        }
        
        public function InsertLocation() {
            $query = sprintf("insert into location (locName, locLatitude, locLongitude) values ('%d','%s', '%s')",                
                myEsc($this->locName),
                myEsc($this->locLatitude),
                myEsc($this->locLongitude));
                
            mysql_query($query, $db);
                
            $id = mysql_insert_id();
            
            if($id >=0) {
                $this->locId = $id;
            }
            
            return $id;
        }
        
        public function SelectAllLocations() {
            global $db;
            $query = "select * from location";
            $sql = mysql_query($query, $db);
            
            if(mysql_num_rows($sql) > 0) {
                $result = array();
                
                while($row = mysql_fetch_array($sql, MYSQL_ASSOC)) {
                    $result[] = $rlt;
                }
                
                foreach($result[0] as $item=>$value) {
                    $tLocation->$item=$value;
                }
                
                return $tTeam;
            }
        }
        
        public function SelectLocationFromLocationId($locationId) {
            global $db;
            $tLocation = new Location;
            
            $query = "select * from location where locId = " . myEsc($locationId);
            $sql = mysql_query($query, $db);
            $result = array();
            
            if(mysql_num_rows($sql) > 0) {
                $result[] = $rlt;
                
                while($row = mysql_fetch_array($sql, MYSQL_ASSOC)) {
                    $result[] = $rlt;
                }
                
                foreach($result[0] as $item=>$value) {
                    $tLocation->$item=$value;
                }
            }
            
            return $tLocation;
        }
        
        public function DeleteLocation($locId) {
            global $db;
            if($locId) {
                $query = "delete from location where locId = " . myEsc($locId);
                $sql = mysql_query($query, $db);
                return $sql;
            }  
        }        
    }
?>