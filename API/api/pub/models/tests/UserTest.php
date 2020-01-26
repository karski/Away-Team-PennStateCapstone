<?php

require_once('/home/awayteam/api/pub/models/User.php');
require_once('/home/awayteam/api/pub/apiconfig.php');

//should generate 2 over failures for the whole class

class UserTest extends \PHPUnit_Framework_TestCase
{
    public function testLoginIDExist()
    {
        $usr = new User;
        $this->assertEquals(true, $usr->LoginIDExist("naimols"));
        $this->assertEquals(false, $usr->LoginIDExist("naimols1"));
    }

    public function testEmailExist()
    {
        $usr = new User;
        $this->assertEquals(true, $usr->EmailExist("steve\@naimolinet.com"));
        $this->assertEquals(true, $usr->EmailExist("steve1\@naimolinet.com"));
        
    }
    public function testUserInit()
    //tests user __construct() and initialize() functions
    {
        $usr = new User;
        
        $this->assertEquals(-999, $usr->userId);
        $this->assertEquals("", $usr->loginId);
        $this->assertEquals("xx", $usr->cellPhone);  //expectedFailure
    }

    public function testGetUser()
    {
        $newUser = new User;
        $getUser = new User;
        
        dbConnect();

        $newUser->loginId = "test";
        $newUser->email   = "test@test.com";
        $newUser->firstName = "john";
        $newUser->lastName = "doe";
        $newUser->password = "1234";
        $newUser->cellPhone = "5555555555";
        $newUser->emergencyPhone = "55555555";

        $id = $newUser->InsertUser();

        $getUser = $getUser->SelectUserFromID($id);
        
        $this->assertEquals($newUser, $getUser);
        
    }

    public function testPasswordHashAccuracy()
    //tests GenerateSecrets() and ValidatePasswordHash()
    {
        $user = array();
        $i = 0;
        
        //generate 100 users with 100 random 12 character passwords 
        //and then make sure all passwords,hashs and salts work together
        for ($i=0; $i<100; $i++)
        {
            $user[$i] = new User;
           
            //generate random password
            //method adapted from: http://stackoverflow.com/questions/853813/how-to-create-a-random-string-using-php 
            $length = 12;
            $valid_chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-=`~!@#$%^&*()_+';
            $random_string = "";
            $num_valid_chars = strlen($valid_chars);

            for ($j = 0; $j < $length; $j++)
            {
                $random_pick = mt_rand(1, $num_valid_chars);
                $random_char = $valid_chars[$random_pick-1];

                $random_string .= $random_char;
            }
    
            $user[$i]->loginId = $random_string;
            
            $userSecretInfo = $user[$i]->GenerateSecrets($random_string);
            
            $user[$i]->userSalt = $userSecretInfo['salt'];
            $user[$i]->password = $userSecretInfo['password'];
            $user[$i]->userIdentifier = $userSecretInfo['identifier'];  //not used for authentication but authorization
            $user[$i]->userSecret = $userSecretInfo['secret'];           //not used for authentication but authorization
        }

        //now test to see if hashes match passwords

        for ($i=0;$i<100;$i++)
        {
            $this->assertEquals(true,$user[$i]->ValidatePasswordHash($user[$i]->loginId));
        }

        //now testing for failure

        $user[0]->loginId = $user[0]->loginId . "xxx"; //change password to assert fail
        $this->assertEquals(true, $user[0]->ValidatePasswordHash($user[0]->loginId)); //verify failure
    }

}

?>
