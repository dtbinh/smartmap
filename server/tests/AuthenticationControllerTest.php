<?php

use Silex\Application;

use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Session\Storage\MockArraySessionStorage;
use Symfony\Component\HttpFoundation\Session\Storage\MockFileSessionStorage;
use Symfony\Component\HttpFoundation\Session\Session;

use SmartMap\DBInterface\User;
use SmartMap\DBInterface\UserRepository;
use SmartMap\Control\AuthenticationController;
use SmartMap\Control\ControlException;

use Facebook\FacebookSession;
use Facebook\FacebookRedirectLoginHelper;
use Facebook\FacebookRequest;
use Facebook\FacebookResponse;
use Facebook\FacebookSDKException;
use Facebook\FacebookRequestException;
use Facebook\FacebookAuthorizationException;
use Facebook\GraphObject;

use Doctrine\DBAL\DriverManager;
use Doctrine\DBAL\Configuration;


/**
 *
<<<<<<< HEAD
 * To run theses tests, you need to
 * -Have a db named smartmap live on localhost, that has the user root with no pwd
 * -Set these settings in web/index.php
 * -Set the bootstrap to autoload.php : php phpunit.phar --bootstrap ..\..\vendor\autoload.php [TESTS TO RUN]
=======
 * To run these tests, you need to
 *  -Have a db named smartmap live on localhost, that has the user root with no pwd
 *  -Set these settings in web/index.php
 *  -Set the bootstrap to autoload.php : php phpunit.phar --bootstrap ..\..\vendor\autoload.php [TESTS TO RUN]
>>>>>>> dev
 *
 * In the case that some tests fail in the future, try connecting to SmartMap app with the mock user. It might need to
 * reaccept the permissions.
 *
 * The mock fb account is robin.genolet@epfl.ch hackerinside
 * name: Swag Sweng
 * ID: 1482245642055847
 * fb access token: CAAEWMqbRPIkBAJjvxMI0zNXLgzxYJURV5frWkDu8T60EfWup92GNEE7xDIVohfpa43Qm7FNbZCvZB7bXVTd0ZC0qLHZCj
 * u2zZBR3mc8mQH0OskEe7X5mZAWOlLZCIzsAWnfEy1ZAzz2JgYPKjaIwhIpI9OvJkQNWkJnX3rIwv4v9lL7hr9yx8LKuOegEHfZCcCNp491jewilZ
 * Cz69ZA2ohryEYy
 *
 * @author SpicyCH
 * 
 * @author Pamoi (code reviewed - 01.11.2014)
 *        
 */
class AuthenticationControllerTest extends PHPUnit_Framework_TestCase {
    
    private $authController;
    private $validPostRequest;
    private $validFbToken;
    
    public function setUp()
    {
        $config = new Configuration();
        $connectionParams = array(
                        'dbname' => 'smartmap',
                        'user' => 'root',
                        'password' => '',
                        'host' => 'localhost',
                        'driver' => 'pdo_mysql' 
        );
        
        $conn = DriverManager::getConnection($connectionParams, $config);
        
<<<<<<< HEAD
        $userRepo = new UserRepository ( $conn );
        $authContr = new AuthenticationController ( $userRepo, '305881779616905', 'b851a1eb3edcaf637f92fbb2af2b3b47' );
        
        $this->authController = $authContr;
        
        $this->validFbToken = "CAAEWMqbRPIkBAJjvxMI0zNXLgzxYJURV5frWkDu8T60EfWup92GNEE7xDIVohfpa43Qm7" .
                        "FNbZCvZB7bXVTd0ZC0qLHZCju2zZBR3mc8mQH0OskEe7X5mZAWOlLZCIzsAWnfEy1ZAzz2JgYPKjaIwhIpI9OvJkQ" .
                        "NWkJnX3rIwv4v9lL7hr9yx8LKuOegEHfZCcCNp491jewilZCz69ZA2ohryEYy";
        
        $this->validPostRequest = array (
                        "name" => "SmartMap SwEng",
                        "facebookId" => "1482245642055847",
                        "facebookToken" => $this->validFbToken
=======
        $userRepo = new UserRepository($conn);
        $authContr = new AuthenticationController($userRepo, '305881779616905', 'b851a1eb3edcaf637f92fbb2af2b3b47');
        
        $this->authController = $authContr;
        
        $this->validFbToken = 'CAAEWMqbRPIkBAJjvxMI0zNXLgzxYJURV5frWkDu8T60EfWup92GNEE7xDIVohfpa43Qm7' .
                        'FNbZCvZB7bXVTd0ZC0qLHZCju2zZBR3mc8mQH0OskEe7X5mZAWOlLZCIzsAWnfEy1ZAzz2JgYPKjaIwhIpI9OvJkQ' .
                        'NWkJnX3rIwv4v9lL7hr9yx8LKuOegEHfZCcCNp491jewilZCz69ZA2ohryEYy';
        
        $this->validPostRequest = array(
                        'name' => 'SmartMap SwEng',
                        'facebookId' => '1482245642055847',
                        'facebookToken' => $this->validFbToken
>>>>>>> dev
        );
    }
    

<<<<<<< HEAD
    public function testCanLoginWithGoodRequestParams() {
        $request = new Request ( $getRequest = array (), $this->validPostRequest );
=======
    public function testCanLoginWithGoodRequestParams()
    {
        $request = new Request($getRequest = array(), $this->validPostRequest);
>>>>>>> dev
        
        $session = new Session(new MockFileSessionStorage());
        $request->setSession($session);
        
        $serverResponse = $this->authController->authenticate($request);
        $json = json_decode($serverResponse->getContent());
        
        $this->assertEquals( 'Ok', $json->status);
    
    }
    
    /**
     * @expectedException SmartMap\Control\ControlException
     * @expectedExceptionMessage Session is null.
     */
<<<<<<< HEAD
    public function testNoSessionYieldsException() {
        $request = new Request ( $getRequest = array (), $this->validPostRequest );
        $serverResponse = $this->authController->authenticate ( $request );
=======
    public function testNoSessionYieldsException()
    {
        $request = new Request($getRequest = array(), $this->validPostRequest);
        $serverResponse = $this->authController->authenticate($request);
>>>>>>> dev
    }
    
    /**
     * @expectedException SmartMap\Control\ControlException
<<<<<<< HEAD
     * @expectedExceptionMessage Bad fb session
     */
    public function testCannotLoginWithBadSession() {
        $postReq = array (
                        "name" => "SmartMap SwEng",
                        "facebookId" => "1482245642055847",
                        "facebookToken" => "ehf h e ue" 
=======
     * @expectedExceptionMessage Mismatch or expired facebook data
     */
    public function testCannotLoginWithBadSession()
    {
        $postReq = array (
                        'name' => 'SmartMap SwEng',
                        'facebookId' => '1482245642055847',
                        'facebookToken' => 'ehf h e ue'
>>>>>>> dev
        );
        $request = new Request($getRequest = array(), $postReq);
        
        $session = new Session(new MockFileSessionStorage());
        $request->setSession($session);
        
        $serverResponse = $this->authController->authenticate($request);
        $json = json_decode($serverResponse->getContent());
        
        $this->assertEquals("error", $json->status);
    }
    
    /**
     * @expectedException SmartMap\Control\ControlException
     * @expectedExceptionMessage do not match the fb access token
     */
<<<<<<< HEAD
    public function testCannotLoginWithGoodSessionButBadName() {
        $postReq = array (
                        "name" => "Robich",
                        "facebookId" => "1482245642055847",
                        "facebookToken" => $this->validFbToken 
        );
        $request = new Request ( $getRequest = array (), $postReq );
        
        $session = new Session ( new MockFileSessionStorage () );
        $request->setSession ( $session );
        
        $serverResponse = $this->authController->authenticate ( $request );
        $json = json_decode ( $serverResponse->getContent () );
        
        $this->assertEquals ( "error", $json->status );
    }
    
    /**
     * @expectedException SmartMap\Control\ControlException
     * @expectedExceptionMessage do not match the fb access token
     */
    public function testCannotLoginWithGoodSessionButBadId() {
        $postReq = array (
                        "name" => "SmartMap SwEng",
                        "facebookId" => "1337",
                        "facebookToken" => $this->validFbToken 
        );
        $request = new Request ( $getRequest = array (), $postReq );
        
        $session = new Session ( new MockFileSessionStorage () );
        $request->setSession ( $session );
        
        $serverResponse = $this->authController->authenticate ( $request );
        $json = json_decode ( $serverResponse->getContent () );
        
        $this->assertEquals ( "error", $json->status );
    }
    
    /**
     * @expectedException SmartMap\Control\ControlException
     * @expectedExceptionMessage An error occured while dealing with the database
     */
    public function testDatabaseErrorYieldsRightExceptionMessage() {
        $config = new Configuration ();
        $connectionParams = array (
                        'dbname' => 'smartmap',
                        'user' => 'Robich',
                        'password' => 'LetMeIn',
                        'host' => 'localhost',
                        'driver' => 'pdo_mysql' 
        );
        
        $conn = DriverManager::getConnection ( $connectionParams, $config );
        
        $userRepo = new UserRepository ( $conn );
        $authContr = new AuthenticationController ( $userRepo, '305881779616905', 'b851a1eb3edcaf637f92fbb2af2b3b47' );
        
        $request = new Request ( $getRequest = array (), $this->validPostRequest );
        
        $session = new Session ( new MockFileSessionStorage () );
        $request->setSession ( $session );
        
        $serverResponse = $authContr->authenticate ( $request );
        $json = json_decode ( $serverResponse->getContent () );
        
        $this->assertEquals ( "Some text, shouldn't matter!", $json->status );
    }
    
    /**
     * @expectedException SmartMap\Control\ControlException
     * @expectedExceptionMessage Missing POST parameter
     */
    public function testNoPostParamsYieldException() {
        $request = new Request ( $getRequest = array (), $postParams = array () );
        
        $session = new Session ( new MockFileSessionStorage () );
        $request->setSession ( $session );
        
        $serverResponse = $this->authController->authenticate ( $request );
        $json = json_decode ( $serverResponse->getContent () );
        
        $this->assertEquals ( "OK", $json->status );
    }
    
    public function testSessionUserIdIsSetAfterLogin() {
        $request = new Request ( $getRequest = array (), $this->validPostRequest );
=======
    public function testCannotLoginWithGoodSessionButBadName()
    {
        $postReq = array(
                        'name' => 'Robich',
                        'facebookId' => '1482245642055847',
                        'facebookToken' => $this->validFbToken 
        );
        $request = new Request($getRequest = array(), $postReq);
        
        $session = new Session(new MockFileSessionStorage());
        $request->setSession($session);
        
        $serverResponse = $this->authController->authenticate($request);
        $json = json_decode($serverResponse->getContent());
>>>>>>> dev
        
        $this->assertEquals("error", $json->status);
    }
    
    /**
     * @expectedException SmartMap\Control\ControlException
     * @expectedExceptionMessage do not match the fb access token
     */
    public function testCannotLoginWithGoodSessionButBadId()
    {
        $postReq = array(
                        'name' => 'SmartMap SwEng',
                        'facebookId' => '1337',
                        'facebookToken' => $this->validFbToken 
        );
        $request = new Request($getRequest = array(), $postReq);
        
        $session = new Session(new MockFileSessionStorage());
        $request->setSession($session);
        
        $serverResponse = $this->authController->authenticate($request);
        $json = json_decode($serverResponse->getContent());
        
        $this->assertEquals('error', $json->status);
    }
    
    /**
     * @expectedException \PDOException
     */
    public function testDatabaseErrorYieldsPDOException()
    {
        $config = new Configuration ();
        $connectionParams = array (
                        'dbname' => 'smartmap',
                        'user' => 'Robich',
                        'password' => 'LetMeIn',
                        'host' => 'localhost',
                        'driver' => 'pdo_mysql' 
        );
        
        $conn = DriverManager::getConnection($connectionParams, $config);
        
        $userRepo = new UserRepository($conn);
        $authContr = new AuthenticationController($userRepo, '305881779616905', 'b851a1eb3edcaf637f92fbb2af2b3b47');
        
        $request = new Request($getRequest = array(), $this->validPostRequest);
        
        $session = new Session(new MockFileSessionStorage());
        $request->setSession($session);
        
        $serverResponse = $authContr->authenticate($request);
    }
    
    /**
     * @expectedException SmartMap\Control\ControlException
     * @expectedExceptionMessage Missing POST parameter
     */
    public function testNoPostParamsYieldException()
    {
        $request = new Request($getRequest = array (), $postParams = array());
        
        $session = new Session(new MockFileSessionStorage());
        $request->setSession($session);
        
        $serverResponse = $this->authController->authenticate($request);
    }
    
    public function testSessionUserIdIsSetAfterLogin()
    {
        $request = new Request($getRequest = array(), $this->validPostRequest);
        
        $session = new Session(new MockFileSessionStorage());
        $request->setSession($session);
        
        $this->assertTrue ( $session->get ( 'userId' ) == null );
        
        $serverResponse = $this->authController->authenticate($request);
        
<<<<<<< HEAD
        $this->assertTrue ( $session->get ( 'userId' ) > 0 );
=======
        $this->assertTrue($session->get('userId') > 0 );
>>>>>>> dev
    }
}
<<<<<<< HEAD
?>
=======
>>>>>>> dev