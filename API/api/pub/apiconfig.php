<?php
    //OWNER: S.NAIMOLI

    error_reporting(E_ALL);

    $db = NULL;
    $db = dbConnect();

    function dbConnect()
    {
        global $db;
        $DB_SERVER = "localhost";
        $DB_USER = "awayteam";
        $DB_PASSWORD = "awayteam";
        $DB_NAME = "awayteam";

        $db = mysql_connect($DB_SERVER,$DB_USER,$DB_PASSWORD);
        if($db)
        {
            $sel = mysql_select_db($DB_NAME,$db);
        }

        return $db;
    }

    function myEsc($str)
    {
        return mysql_real_escape_string($str);
    }

    function getTime()
    {
        $mTime = microtime(true);
        $mTime = round($mTime, 0);

        return $mTime;
    }

    function logIt($str)
    {
        file_put_contents ('/tmp/phplogtest.txt', $str . "\n", FILE_APPEND | LOCK_EX);
    }

?>
