<?php
    include_once('/home/awayteam/api/pub/apiconfig.php');
    include_once('/home/awayteam/api/pub/models/Team.php');
    
    class TeamMembers
    {
        //Attributes
        public $teamMemberId;
        public $teamId;
        public $userId;
        public $manager;
        public $pendingApproval;
        
        public function __construct() {
            $this->initialize();
        }
        
        public function initialize() {
            $this->teamMemberId = -999;
            $this->teamId = -999;
            $this->userId = -999;
            $this->manager = false;
            $this->pendingApproval = false;
        }
        
        public function TeamMemberIdExists($id) {
            global $db;
            if($id) {
                $query = "select count(teamMemberId) as num from team_member where teamMemberId = " . myEsc($id);
                $sql = mysql_query($query, $db);
                $data = mysql_fetch_assoc($sql);
                
                if ($data['num'] == 0) {
                    return false;
                } else {
                    return true;
                }   
            }
        }
        
        public function VerifyManagerForUser($teamId, $userId) {
            global $db;
            if($teamId && $userId) {
                $query = "select manager from team_member where teamId = " .myEsc($teamId) . " AND userId = " .myEsc($userId);
                $sql = mysql_query($query,$db);
                $data = mysql_fetch_assoc($sql);
                return $data['manager'];
            }
        }
        
        public function VerifyTeamMemberExist($teamId, $userId) {
            global $db;
            if($teamId && $userId) {
                $query = "select count(teamMemberId) as num from team_member where teamId = " . myEsc($teamId) . " and userId = " . myEsc($userId) . " and pendingApproval=0";
                $sql = mysql_query($query, $db);
                $data = mysql_fetch_assoc($sql);
                
                if($data['num'] == 0) {
                    return false;
                } else {
                    return true;
                }
            }
        }       

        public function VerifyTeamMemberPending($teamId, $userId)
        {
            global $db;

            if($teamId && $userId)
            {
                $query = "select count(teamMemberId) as num from team_member where teamId = " . myEsc($teamId) . " and userId = " . myEsc($userId) . " and pendingApproval=1";
                $sql = mysql_query($query, $db);
                $data = mysql_fetch_assoc($sql);
    
                if($data['num'] == 0) 
                {
                    return false;
                } 
                else 
                {
                    return true;
                }   
            }
        }
        
        public function GetNumberOfTeamMembersRemaining($teamId) {
            global $db;
            
            if($teamId) {
                $query = "select count(teamMemberId) as num from team_member where teamId = " .myEsc($teamId);
                $sql = mysql_query($query,$db);
                $data = mysql_fetch_assoc($sql);
                return $data['num'];
            }
        }
        
        public function GetNumberOfTeamManager($teamId) {
            global $db;
            if($teamId) {
                $query = "select count(manager) as num from team_member where teamId = " .myEsc($teamId);
                $sql = mysql_query($query,$db);
                $data = mysql_fetch_assoc($sql);
                return $data['num'];
            }
        }

        public function InsertTeamMember() {
            global $db;
            
            if (strtolower($this->pendingApproval) == "true")
            {
                $this->pendingApproval = 1;
            }
            else
            {
                $this->pendingApproval = 0;
            }

            
            $query = sprintf("insert into team_member (teamId, userId, manager, pendingApproval) values (%d,%d,%d,%d)",               
                myEsc($this->teamId),
                myEsc($this->userId),
                myEsc($this->manager),
                myEsc($this->pendingApproval));
            
                            
            mysql_query($query, $db);
            
            $id = mysql_insert_id();
            
            if($id >=0) {
                $this->teamMemberId = $id;
            }
            
            return $this->teamMemberId ;
        }
        
        public function AddTeamMember($teamId,$loginId) {
            global $db;
            
            $manager = 0;
            $query = "select userId from user where loginId = '" .myEsc($loginId) . "'";
            $sql = mysql_query($query,$db);
            
            if(mysql_num_rows($sql)) {
                while($rlt = mysql_fetch_array($sql, MYSQL_ASSOC)) {
                    $userId = $rlt['userId'];
                }
            }
            $query = "select teamManaged from team where teamId = " . myEsc($teamId);
            
            $sql = mysql_query($query, $db);
            if(mysql_num_rows($sql)) {
                while($rlt = mysql_fetch_array($sql, MYSQL_ASSOC)) {
                    $teamManaged = $rlt["teamManaged"];
                }               
            }
            
            if($teamManaged == 1) {
                $pendingApproval = 1;

                $query = sprintf("insert into team_member(teamId, userId, manager, pendingApproval) values (%d,%d,%d,%d)",
                    myEsc($teamId),
                    myEsc($userId),
                    myEsc($manager),
                    myEsc($pendingApproval));
            } else {
                $pendingApproval = 0;
                $query = sprintf("insert into team_member(teamId, userId, manager, pendingApproval) values (%d,%d,%d,%d)",
                    myEsc($teamId),
                    myEsc($userId),
                    myEsc($manager),
                    myEsc($pendingApproval)); 
            }
            
            mysql_query($query,$db);

            $id = mysql_insert_id();
            
            if($id>=0) {
                $this->teamMemberId = $id;
            }
            
            return $this->teamMemberId;           
        }
        
         public function AddFirstTeamMember($teamId, $loginId) {
            global $db;
            
            $manager = 0;
            $pendingApproval = 0;
            $query = "select userId from user where loginId = '" .myEsc($loginId) . "'";
            $sql = mysql_query($query,$db);
            
            if(mysql_num_rows($sql)) {
                while($rlt = mysql_fetch_array($sql, MYSQL_ASSOC)) {
                    $userId = $rlt['userId'];
                }
            }
            $query = "select teamManaged from team where teamId = " . myEsc($teamId);
            
            $sql = mysql_query($query, $db);
            if(mysql_num_rows($sql)) {
                while($rlt = mysql_fetch_array($sql, MYSQL_ASSOC)) {
                    $teamManaged = $rlt["teamManaged"];
                }               
            }
            
            if($teamManaged == 1) {
                $manager = 1;
                $query = sprintf("insert into team_member(teamId, userId, manager, pendingApproval) values (%d,%d,%d,%d)",
                    myEsc($teamId),
                    myEsc($userId),
                    myEsc($manager),
                    myEsc($pendingApproval));            
            } else {
                $manager = 0;
                $query = sprintf("insert into team_member(teamId, userId, manager, pendingApproval) values (%d,%d,%d,%d)",
                    myEsc($teamId),
                    myEsc($userId),
                    myEsc($manager),
                    myEsc($pendingApproval)); 
            }
            
            mysql_query($query,$db);
            $id = mysql_insert_id();
            
            if($id>=0) {
                $this->teamMemberId = $id;
            }
            
            return $this->teamMemberId;           
        }
        
        public function SelectTeamMemberFromId($id) {
            global $db;
            $aTeamMember = new TeamMembers;

            if($this->TeamMemberIdExists($id)) {
                $query = "select * from team_member where teamMemberId = " . myEsc($id);
                $sql = mysql_query($query, $db);
                if(mysql_num_rows($sql) > 0) {
                    $result = array();
                    while($rlt = mysql_fetch_array($sql, MYSQL_ASSOC)) {
                        $result[] = $rlt;
                    }
                    
                    foreach($result[0] as $column=>$value) {
                        $aTeamMember->$column = $value;
                    }
                    
                    return $aTeamMember;
                }
            }            
        }
        
        public function SelectTeamMemberFromTeamId($teamId) {
            global $db;
            $aTeamMember = new TeamMembers;
            $teamMemberList = array();
            
            if($teamId) {
                $query = "select * from team_member where teamId = " . myEsc($teamId);
                $sql = mysql_query($query, $db);
                
                if(mysql_num_rows($sql) > 0) {
                    while($rlt = mysql_fetch_array($sql, MYSQL_ASSOC)) {
                        $teamMemberList = $rlt;
                    }
                }                
            }
            
            return $teamMemberList;
        }
        
        public function ModifyTeamMember() {
            global $db;
            $query = sprintf("update team_member set teamId=%d, userId=%d, manager=%d, pendingApproval=%d where teamMemberId=" . myEsc($this->teamMemberId),
                    myEsc($this->teamId),
                    myEsc($this->userId),
                    myEsc($this->manager),
                    myEsc($this->pendingApproval));
            
            $sql = mysql_query($query, $db);
            return $sql;          
        }        

        public function ModifyManagerAttribute($teamMemberId, $newManagerValue) {
            global $db;
            
            if($teamMemberId == -999) {
                return false;
            } else if($this->TeamMemberIdExists($teamMemberId)) {
                $query = "update team_member set manager=" .myEsc($newManagerValue) 
                        . " where teamMemberId = " .myEsc($teamMemberId);
                $sql = mysql_query($query, $db);
                return $sql;
            } else {
                return false;
            }
        }
        
        public function ModifyPendingApproval($teamMemberId, $booleanValue) {
            global $db;

            if($teamMemberId== -999) {
                return false;
            } else if($this->TeamMemberIdExists($teamMemberId)) {
                $query = "update team_member set pendingApproval=" .myEsc($booleanValue)
                        . " where teamMemberId = " .myEsc($teamMemberId);
                $sql = mysql_query($query, $db);
                return $sql;                
            } else {
                return false;
            }
        }
        
        public function ModifyTeamMemberTeamId($teamMemberId, $teamId) {
            global $db;
                        
            if($teamMemberId == -999) {
                return false;
            } else if ($this->TeamMemberIdExists($teamMemberId)) {
                $query = "update team_member set teamId=" . myEsc($teamId) 
                        . " where teamMemberId = " .myEsc($teamMemberId);
                $sql = mysql_query($query, $db);
                return $sql;
            } else {
                return false;
            }
        }
        
        public function DeleteTeamMember($teamId, $userId) {
            global $db;
            
            if($teamId && $userId) {
                $query = "delete from team_member where teamId = " .myEsc($teamId) . " AND userId = " . myEsc($userId);
            }
            
            $sql = mysql_query($query, $db);
            return $sql;
        }
        
        public function DeleteTeamMemberConfirmation($teamId,$userId) {
            $deletionStatus = 0;
            $team = new team;
            

            if($team->IsTeamManaged($teamId) && $this->VerifyManagerForUser($teamId,$userId) 
                    && $this->GetNumberOfTeamManager($teamId) == 1 ){
                $deletionStatus = 0;
            } else if($this->GetNumberOfTeamMembersRemaining($teamId) == 1) {
                $deletionStatus = 0;                
            } else {
                $result = $this->DeleteTeamMember($teamId,$userId);
                if($result != false) {
                    $deletionStatus = 1;
                } else {
                    $deletionStatus = 2;
                }
            }
            return $deletionStatus;
        }
        
        public function DeleteTeamMemberTeamRemove($teamId,$userId) {
            $deletionStatus = 0;
            $team = new team;
            
            $result = $this->DeleteTeamMember($teamId, $userId);
            if($result != false) {
                $deletionStatus = 1;
                if($this->GetNumberOfTeamMembersRemaining($teamId) == 0 || $this->GetNumberOfTeamManager($teamId) == 0) {
                    $result = $team->DeleteTeam($teamId);
                    
                    if($result != false) {
                        $deletionStatus=2;
                    }
                }
            }
            return $deletionStatus;
        }
    }
?>
