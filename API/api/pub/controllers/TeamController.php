<?php
    
    //Owner: David Vu
    
    require_once('/home/awayteam/api/pub/models/Team.php');
    require_once('/home/awayteam/api/pub/apiconfig.php');
    
    class TeamController extends Team
    {    
        public function CreateTeam ($teamParametersArray, $loginId) {
            $tTeam = new Team;
            $tTeam = $this->arrayToObject($teamParametersArray);            
            $newTeamId = $tTeam->InsertTeam($loginId);
            return $newTeamId;
        }
        
        public function GetAllTeams() {
            return $this->SelectAllTeams();
        }
        
        public function GetTeamFromID($teamId,$loginId) {
            return $this->SelectTeamFromId($teamId,$loginId);
        }
        
        public function GetTeamListForUser($loginId) {
            return $this->GetTeamList($loginId);
        }
        
        public function SearchAllTeams($teamName) {
            return $this->SearchTeams($teamName);
        }
        
        public function ModifyTeamName($teamId, $teamName) {            
            return $this->ModifyTeamNameModel($teamId,$teamName);
        }
        
        public function ModifyTeam($teamParametersArray,$userId) {
            $tTeam = new Team;
            $tTeam = $this->arrayToObject($teamParametersArray);
            $retCode = $tTeam->ModifyTeamModel($userId);            
            return $retCode;
        }
        
        public function DeleteTeam($teamId) {
            return $this->DeleteTeam($teamId);
        }
        
        private function arrayToObject($teamArray) {
            $tTeam = new Team;
            foreach($teamArray as $item=>$value) {
                $tTeam->$item = $value;
            }
            
            return $tTeam;            
        }
    }
?>
