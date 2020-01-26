<?php
    require_once('/home/awayteam/api/pub/apiconfig.php');
    
    class TeamUtilities
    {
        public function TeamIdExists($teamId) {
            global $db;
            if($teamId) {
                $query = "select count(teamId) as num from team where teamId=" . myEsc($teamId);
                logIt("teamIdExists query: " . var_export($query,true));
                $sql = mysql_query($query, $db);
                $data = mysql_fetch_assoc($sql);
                
                if ($data['num'] == 0) {
                    return false;
                } else {
                    return true;
                }                
            }
        }
        
        public function TeamNameUsed($teamName) {
            global $db;
            if($teamName) {
                $teamName = strtolower($teamName);
                $query = "select count(teamName) as num from team where teamName='" .myEsc($teamName) . "'";
                $sql = mysql_query($query, $db);
                $data = mysql_fetch_assoc($sql);
    
                if ($data['num'] == 0) {
                    return false;
                } else {
                    return true;
                }
            }
        }        

        public function GetTeamName($teamId)
        {
            global $db;

            if ($teamId)
            {
                $query = "select teamName from team where teamId=" . myEsc($teamId);
                
                $sql = mysql_query($query, $db);
                $data = mysql_fetch_assoc($sql);

                return $data['teamName'];
            }
        }
    }
?>
