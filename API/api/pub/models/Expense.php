<?php

    //OWNER: S. NAIMOLI

    include_once('/home/awayteam/api/pub/apiconfig.php');
    //include db config

    class Expense 
    {   
        //class attributes

        public $expenseId;
        public $description;
        public $amount;
        public $expDate;
        public $teamId;
        public $userId;
        public $receipt;
        public $expType; //(1 = 'breakfast', 2 = 'lunch', 3 = 'dinner',
                         // 4 = 'snack', 5 = 'other')  DB sets 5 as default if not entered
    
        public function __construct()
        {   
            $this->initialize();
        }   
    
        public function initialize()
        {   
            $this->expenseId        = -999;
            $this->description      = "";
            $this->amount           = 0.00;
            $this->expDate          = '2013-12-31';
            $this->teamId           = -999;
            $this->userId           = -999;
            $this->receipt          = NULL;
            $this->expType          = 5;             
        }   

        public function InsertExpense()
        {
            if (($this->amount <= 0.00) || ($this->description == ""))
            {
                return -999;
            }

            global $db;

            $id = -999;

            $query = sprintf("insert into expense (description,amount,expDate,teamId,userId,expType) values ('%s', %f, '%s', %d, %d, %d)",
                myEsc($this->description),
                myEsc($this->amount),
                myEsc($this->expDate),
                myEsc($this->teamId),
                myEsc($this->userId),
                myEsc($this->expType));

            mysql_query($query, $db);
            $id = mysql_insert_id();
            

            if ($id >= 0)
            {
                $this->expenseId = $id;
            }

            return $id;
        }

        public function ApplyReceipt($type)
        {   
            global $db;

            $receipt = $this->receipt;

            $receipt = 'data:image/' . $type . ';base64,' . base64_encode($receipt);
            $this->receipt = $receipt;

            $query = "update expense set receipt='" . $this->receipt . "' where expenseId=" . myEsc($this->expenseId);
    
            $sql = mysql_query($query, $db);
            return $sql;
        }   

        public function DeleteExpense($id)
        {
            global $db;
            if ($id)
            {
                $query = "delete from expense where expenseId=" . myEsc($id);
            }
            
            $sql = mysql_query($query, $db);
            return $sql;
        }

        public function UpdateExpense()
        {
            global $db;

            $query = sprintf("update expense set description='%s', amount=%f, expDate='%s',expType=%d where expenseId=%d",
                myEsc($this->description),
                myEsc($this->amount),
                myEsc($this->expDate),
                myEsc($this->expType),
                myEsc($this->expenseId));

            $sql = mysql_query($query, $db);
            
            return $sql;
        }

        public function SelectReceipt($expenseId, $userId, $teamId)
        {
            global $db;

            $query = sprintf("select receipt from expense where userId=%d and teamId=%d and expenseId=%d",
                myEsc($userId),
                myEsc($teamId),
                myEsc($expenseId));

            $sql = mysql_query($query, $db);

            if (mysql_num_rows($sql) > 0)
            {
                $result = array();
                while ($rlt = mysql_fetch_array($sql, MYSQL_ASSOC))
                {
                    $result[] = $rlt;
                }

                return $result[0]['receipt'];
            }
            else
            {
                return null;
            }
        }        

        public function SelectExpense($expenseId, $userId, $teamId)
        {
            //select one expense
            
            global $db;
            $tExpense = new Expense;
            
            if (!$expenseId || !$userId || !$teamId)
            {
                return $tExpense;
            }

            $query = sprintf("select userId,teamId,description, amount, expDate, expType+0 as expType, expenseId, octet_length(receipt) as receipt from expense where expenseId=%f and userId=%f and teamId=%f",
                myEsc($expenseId),
                myEsc($userId),
                myEsc($teamId));

            $sql = mysql_query($query, $db);

            if (mysql_num_rows($sql) > 0)
            {
                $result = array();
                while ($rlt = mysql_fetch_array($sql, MYSQL_ASSOC))
                {
                    $result[] = $rlt;
                }

                foreach($result[0] as $item=>$value)
                {
                    $tExpense->$item = $value;
                }

                if ($tExpense->receipt == null || $tExpense->receipt <= 0)
                {
                    $tExpense->receipt = false;
                }
                else
                {
                    $tExpense->receipt = true;
                }
            }

            return $tExpense;
            
        }

        public function SelectExpensesByDate($reqDate, $userId,$teamId)
        {
            //select type,id,amount,date from a day

            global $db;
            $tExpense = array(); //whole
            $sExpense = new Expense; //part
            
            if (!$reqDate || !$userId || !$teamId)
            {
                return $sExpense;
            }

            $query = sprintf("select userId,teamId,description, amount, expDate, expType+0 as expType, expenseId, octet_length(receipt) as receipt from expense where expDate='%s' and userId=%f and teamId=%f",
                myEsc($reqDate),
                myEsc($userId),
                myEsc($teamId));

            $sql = mysql_query($query, $db);
            
            if (mysql_num_rows($sql) > 0)
            {
                while ($rlt = mysql_fetch_array($sql, MYSQL_ASSOC))
                {   
                    $sExpense = $rlt;
                    
                    if ($sExpense['receipt'] == null || $sExpense['receipt'] <= 0)
                    {
                        $sExpense['receipt'] = false;
                    }
                    else
                    {
                        $sExpense['receipt'] = true;
                    }
                    
                    array_push($tExpense, $sExpense);
                }   
            }
            
            return $tExpense;
        }

        public function SelectExpensesByType($reqType, $userId, $teamId)
        {
            //select id,amount,date,type that are a certain type

           global $db;
            $tExpense = array(); //whole
            $sExpense = new Expense; //part
    
            if (!$reqType || !$userId || !$teamId)
            {   
                return $sExpense;
            }   

            $query = sprintf("select userId,teamId,description, amount, expDate, expType+0 as expType, expenseId, octet_length(receipt) as receipt from expense where expType=%f and userId=%f and teamId=%f",
                myEsc($reqType),
                myEsc($userId),
                myEsc($teamId));

            $sql = mysql_query($query, $db);
    
            if (mysql_num_rows($sql) > 0)
            {   
                while ($rlt = mysql_fetch_array($sql, MYSQL_ASSOC))
                {   
                    $sExpense = $rlt;
                    
                    if ($sExpense['receipt'] == null || $sExpense['receipt'] <= 0)
                    {
                        $sExpense['receipt'] = false;
                    }
                    else
                    {
                        $sExpense['receipt'] = true;
                    }

                    array_push($tExpense, $sExpense);
                }
            }   
                
            return $tExpense;

        }

        public function SelectExpenses($userId,$teamId)
        {
            //select all expenses from trip on team, group by date, order by type

           global $db;
            $tExpense = array(); //whole
            $sExpense = new Expense; //part
           
            if (!$userId || !$teamId)
            {  
                return $sExpense;
            }

            $query = sprintf("select userId,teamId,description, amount, expDate, expType+0 as expType, expenseId, octet_length(receipt) as receipt from expense where userId=%f and teamId=%f order by expDate",
                myEsc($userId),
                myEsc($teamId));

            $sql = mysql_query($query, $db);

            if (mysql_num_rows($sql) > 0)
            {
                while ($rlt = mysql_fetch_array($sql, MYSQL_ASSOC))
                {
                    $sExpense = $rlt;
                
                    if ($sExpense['receipt'] == null || $sExpense['receipt'] <= 0)
                    {
                        $sExpense['receipt'] = false;
                    }
                    else
                    {
                        $sExpense['receipt'] = true;
                    }

                    array_push($tExpense, $sExpense);
                }
            }

            return $tExpense;

        }
    }
?>
