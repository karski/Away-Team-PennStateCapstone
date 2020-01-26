<?php
    include_once('/home/awayteam/api/pub/apiconfig.php');
    include_once('/home/awayteam/api/pub/models/Manager.php');

    class ManagerController extends Manager
    {

        public function GetTeamPendingUsers($teamId) 
        {
            return $this->GetPendingUsers($teamId);
        }

        public function GetTeamPendingUsersAllTeams($manUserId)
        {
            return $this->GetPendingUsersAllTeams($manUserId);
        }
        
        public function TakeAction($teamId, $userId, $action) 
        {
            $action = strtolower($action);

            switch ($action)
            {
                case "approve":
                    return $this->ApproveUser($teamId, $userId);
                    break;
                case "remove":
                    return $this->RemoveUser($teamId, $userId);
                    break;
                case "promote":
                    return $this->PromoteUser($teamId, $userId);
                    break;
                case "demote":
                    return $this->DemoteUser($teamId, $userId);
                    break;
                default:
                    break;
            }
        }
    
    }
?>
