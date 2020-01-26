<?php
    include_once('/home/awayteam/api/pub/apiconfig.php');
    
    class TeamTasks
    {
        //Attributes
        public $taskId;
        public $taskTitle;
        public $taskDescription;
        public $taskCompleted;
        public $taskTeamId;
        
        public function __construct() {
            $this->initialize();
        }
        
        public function initialize() {
            $this->taskId = -999;
            $this->taskTitle = "";
            $this->taskDescription = "";
            $this->taskCompleted =false;
            $this->taskTeamId = -999;            
        }
        
        public function VerifyTaskForTeam($taskId,$taskTeamId) {
            global $db;
            
            if($taskId && $taskTeamId) {
                $query = "select count(taskId) as num from team_tasks where taskId = " . myEsc($taskId) . " AND taskTeamId = " . myEsc($taskTeamId);
                
                $sql = mysql_query($query,$db);            
                $data = mysql_fetch_assoc($sql);
                    
                if ($data['num'] == 0) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        
        public function InsertTeamTask() {
            global $db;           
    
            $this->taskCompleted = 0;
            
            $query = sprintf("insert into team_tasks (taskTitle, taskDescription, taskCompleted, taskTeamId) values ('%s', '%s',%d,%d)",
                    myEsc($this->taskTitle),
                    myEsc($this->taskDescription),
                    myEsc($this->taskCompleted),
                    myEsc($this->taskTeamId));
            
            mysql_query($query, $db);
            
            $id = mysql_insert_id();
            
            if($id >=0) {
                $this->taskId = $id;
            }
            
            return $id;
        }
        
        public function ModifyTeamTask() {
            global $db;  
            
            $query = sprintf("update team_tasks set taskTitle='%s', taskDescription ='%s' where taskId = %d",
                    myEsc($this->taskTitle),
                    myEsc($this->taskDescription),
                    myEsc($this->taskId));
                    
            $sql = mysql_query($query, $db);
                
            return $sql;
        }
        
        public function MarkTeamTaskComplete($taskId, $taskCompleted) {
            global $db;
            $taskCompleteFlag = false;
            
            if($taskCompleted == "true") {
                $taskCompleteFlag = 1;
            } else {
                $taskCompleteFlag = 0;
            }
            
            $query = "update team_tasks set taskCompleted = " . myEsc($taskCompleteFlag) . " where taskId = " . myEsc($taskId);
            $sql = mysql_query($query,$db);
            return $sql;
        }
        
        public function DeleteTeamTask($taskId) {
            global $db;
            if($taskId) {
                $query = "delete from team_tasks where taskId = " .myEsc($taskId);
                $sql = mysql_query($query, $db);
                return $sql;
            }
        }

        //added by request
        public function SelectTeamTasks($teamId)
        {
            global $db;
            $tTasks = array(); //whole
            $sTask  = new TeamTasks; //part

            if (!$teamId)
            {
                return $sTask;
            }
            
            $query = "select taskId, taskTitle, taskDescription, taskCompleted, taskTeamId from team_tasks where taskTeamId=" . myEsc($teamId);

            $sql = mysql_query($query, $db);

            if (mysql_num_rows($sql) > 0)
            {
                while ($rlt = mysql_fetch_array($sql, MYSQL_ASSOC))
                {
                    $sTask = $rlt;

                    if ($sTask['taskCompleted'] == 0)
                    {
                        $sTask['taskCompleted'] = false;
                    }
                    else
                    {
                        $sTask['taskCompleted'] = true;
                    }

                    array_push($tTasks, $sTask);
                }
                
                return $tTasks;
            }
            else
            {
                return $sTask;
            }
        }
    }
?>
