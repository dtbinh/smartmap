<?php

namespace SmartMap\DBInterface;

use Symfony\Component\HttpFoundation\Request;

use SmartMap\Control\ControlException;

/**
 * Models a user.
 *
 * @author Pamoi
 *
 * @author SpicyCH (code reviewed - 03.11.2014) : added javadoc, need to unit test
 */
class User
{
    private $mId;
    private $mFbId;
    private $mName;
    private $mVisibility;
    private $mLongitude;
    private $mLatitude;
    
    /**
     * Constructor
     * @param Long $id
     * @param Long $fbId
     * @param String $name
     * @param enum $visibility
     * @param double $longitude
     * @param double $latitude
     */
    function __construct($id, $fbId, $name, $visibility, $longitude, $latitude)
    {
        // Checking for validity
        $this->checkId($id);
        //$this->checkId($fbId);
        $this->checkVisibility($visibility);
        
        $this->mId = $id;
        $this->mFbId = $fbId;
        $this->mName = $name;
        $this->mVisibility = $visibility;
        $this->mLongitude = $longitude;
        $this->mLatitude = $latitude;
    }
    
    /**
     * Gets the current user id from the server request. Throws an exception
     * if the session parameter userId is not set.
     * 
     * @param Request $request
     * @throws ControlException 
     * @return a Long representing the user id
     */
    public static function getIdFromRequest(Request $request)
    {
        if (!$request->hasSession())
        {
            throw new ControlException('Trying to access session but the session is not started.');
        }
        
        $session = $request->getSession();
        
        // The userId is set in the $_SESSION when successfully authenticated
        $id = $session->get('userId');
        
        if ($id == null)
        {
            throw new ControlException('The user is not authenticated.');
        }
        
        return $id;
    }
    
    /**
     * Get the user's id
     * @return a Long representing the user id
     */
    public function getId()
    {
        return $this->mId;
    }
    
    /**
     * Set the user's id
     * @param Long $id
     * @return \SmartMap\DBInterface\User
     */
    public function setId($id)
    {
        $this->checkId($id);
        
        $this->mId = $id;
        
        return $this;
    }
    
    /**
     * Get the user's facebook id
     * @return the facebook id associated to this user
     */
    public function getFbId()
    {
        return $this->mFbId;
    }
    
    /**
     * Set the user's facebook id
     * @param Long $fbId the facebook id to associate to the user
     * @return \SmartMap\DBInterface\User
     */
    public function setFbId($fbId)
    {
        $this->mFbId = $fbId;
        
        return $this;
    }
    
    /**
     * Get the user's name
     * @return a String for the user's name
     */
    public function getName()
    {
        return $this->mName;
    }
    
    /**
     * Set the user's name
     * @param String $name
     * @return \SmartMap\DBInterface\User
     */
    public function setName($name)
    {
        $this->mName = $name;
        
        return $this;
    }
    
    /**
     * Get the user's visibility
     * @return the visibility: VISIBLE or INVISIBLE
     */
    public function getVisibility()
    {
        return $this->mVisibility;
    }
    
    /**
     * Set the user's visibility
     * @param enum $visibility the visibility, VISIBLE or INVISIBLE
     * @return \SmartMap\DBInterface\User
     */
    public function setVisibility($visibility)
    {
        $this->checkVisibility($visibility);
        
        $this->mVisibility = $visibility;
        
        return $this;
    }
    
    /**
     * Get the user's longitude coordinate
     * @return the longitude
     */
    public function getLongitude()
    {
        return $this->mLongitude;
    }
    
    /**
     * Set the user's longitude coordinate
     * @param double $longitude the longitude to set
     * @return \SmartMap\DBInterface\User
     */
    public function setLongitude($longitude)
    {
        $this->mLongitude = $longitude;
        
        return $this;
    }
    
    /**
     * Get the user's latitude coordinate
     * @return the latitude
     */
    public function getLatitude()
    {
        return $this->mLatitude;
    }
    
    /**
     * Set the user's latitude coordinate
     * @param unknown $latitude the latitude
     * @return \SmartMap\DBInterface\User
     */
    public function setLatitude($latitude)
    {
        $this->mLatitude = $latitude;
        
        return $this;
    }
    
    /**
     * @param Long $id
     * @throws \InvalidArgumentException if the id is below 1
     */
    private function checkId($id)
    {
        if ($id <= 0)
        {
            throw new \InvalidArgumentException('id must be greater than 0');
        }
    }
    
    /**
     * 
     * @param enum $visibility
     * @throws \InvalidArgumentException if the visibility is not VISIBLE or INVISIBLE
     */
    private function checkVisibility($visibility)
    {
        if (!($visibility == 'VISIBLE' OR $visibility == 'INVISIBLE'))
        {
            throw new \InvalidArgumentException('Visibility must be VISIBLE or INVISIBLE.');
        }
    }
}