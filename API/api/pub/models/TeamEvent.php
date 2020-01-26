<?php
    include_once('/home/awayteam/api/pub/apiconfig.php');
    
    class TeamEvent 
    {
        //Attributes
        public $teamEventId;
        public $teamEventName;
        public $teamEventDescription; 
        public $teamEventLocationString;
        public $teamEventStartTime;
        public $teamEventEndTime;
        public $teamEventTeamId;
        
        public function __construct() {
            $this->initialize();
        }
        
        public function initialize() {
            $this->teamEventId = -999;
            $this->teamEventName = "";
            $this->teamEventDescription = "";
            $this->teamEventLocationString = "";
            $this->teamEventStartTime = '2013-12-31 0:0';
            $this->teamEventEndTime = '2013-12-31 0:0';
            $this->teamEventTeamId = -999;
        }
        
        public function ValidateDateTime($dateTimeString) {
            $aDateTime = DateTime::createFromFormat('Y-n-j H:i',$dateTimeString);
            $validationResult = false;
            if($aDateTime && $aDateTime->format('Y-n-j H:i') == $dateTimeString) {
                $validationResult = true;
            } else {
                $validationResult = false;
            }
            
            return $validationResult;
        }
        
        public function VerifyTeamEventTeamId($teamEventTeamId, $teamEventId) {
            global $db;
            
            $query = "select count(teamEventId) as num from team_event where teamEventId = " . myEsc($teamEventId) . " AND teamEventTeamId = " . myEsc($teamEventTeamId);
            $sql = mysql_query($query,$db);
            $data = mysql_fetch_assoc($sql);
            
            if($data['num'] == 0) {
                return false;
            } else {
                return true;
            }
        }
        
        public function InsertEvent() {
            global $db;
            $query = sprintf("insert into team_event(teamEventName,teamEventDescription,teamEventLocationString,teamEventStartTime,teamEventEndTime,teamEventTeamId) 
                                values('%s','%s','%s','%s','%s',%d)",
                    myEsc($this->teamEventName),
                    myEsc($this->teamEventDescription),
                    myEsc($this->teamEventLocationString),
                    myEsc($this->teamEventStartTime),
                    myEsc($this->teamEventEndTime),
                    myEsc($this->teamEventTeamId));
            
            mysql_query($query, $db);
            
            $id = mysql_insert_id();
            
            if($id > 0) {
                $this->teamEventId = $id;
            }
            
            return $id;
        }
        
        public function SelectAllEvents() {
            global $db;
            
            $query = "select * from team_event";            
            $sql = mysql_query($query, $db);
            $eventList = array();
            
            if(mysql_num_rows($sql) > 0) {                
                while($row = mysql_fetch_object($sql)) {
                    $anEvent = $row;
                    $eventList[] = $anEvent;                  
                }            
            }
            
            return $eventList;
        }
        
        public function SelectEventFromEventId($eventId) {
            global $db;
            
            $anEvent = new TeamEvent;            
            $query = "select * from team_event where teamEventId = " . myEsc($eventId);
            $sql = mysql_query($query, $db);
            if(mysql_num_rows($sql) > 0) {
                $result = array();
                
                while($row = mysql_fetch_object($sql)) {
                    $anEvent = $row;
                    $result[] = $anEvent;
                }
                
                return $result;
            }
        }
        
        public function SelectEventFromEventName($eventName) {
            global $db;
            
            $anEvent = new TeamEvent;
            $query = "select * from team_event where teamEventName = '" . myEsc($eventName) . "'";
            $sql = mysql_query($query, $db);
            if(mysql_num_rows($sql) > 0) {
                $result = array();
                
                while($row = mysql_fetch_object($sql)) {
                    $anEvent = $row;
                    $result[] = $anEvent;
                }
                
                return $result;
            }
        }       
        
        public function ModifyEvent() {
            global $db;
            $query = sprintf("update team_event set teamEventName='%s', teamEventDescription='%s',teamEventLocationString='%s', teamEventStartTime='%s',teamEventEndTime='%s'
                                where teamEventId = " . myEsc($this->teamEventId),
                    myEsc($this->teamEventName),
                    myEsc($this->teamEventDescription),
                    myEsc($this->teamEventLocationString),
                    myEsc($this->teamEventStartTime),
                    myEsc($this->teamEventEndTime));
            
            $sql = mysql_query($query, $db);
                
            return $sql;                  
        }
        
        public function ModifyEventName($teamEventId, $newEventName) {  
            global $db;
            
            if($teamEventId == -999) {
                return false;
            } else if ( $newEventName) {            
                $query = "update team_event set teamEventName = '" . myEsc($newEventName)  .
                        "' where teamEventId = " . myEsc($teamEventId);
                $sql = mysql_query($query, $db);
                return $sql;
            } else {
                return false;
            }
        }
        
        public function DeleteEvent($eventId) {
            global $db;
            if($eventId) {
                $query = "delete from team_event where teamEventId = " . myEsc($eventId);
                $sql = mysql_query($query, $db);
                return $sql;
            }
        }
    }
?>