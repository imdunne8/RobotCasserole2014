/*
 * Class to handle all inputs, states, and transitions of the shooter/intake assembly.
 * 2014 FRC Team 1736 Robot Casserole
 */

package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Joystick;

public class Jaws {
    Solenoid bottomJawLeftSolenoid, bottomJawRightSolenoid, topJawSolenoid, LshooterSolenoid, RshooterSolenoid;
    Talon rollerTalon;
    Joystick joy;
    int lastState = 0;
    int currentState = 0;
    
    //Array of time in seconds that it takes to complete each state
    double[] stateTimers = {0, 0, 0, 0, 0, 3, 3.5, 5, 3};
    double timeInState = 0;
    //Jaw position - false is default and down position, true is up.
    public boolean jawPos = false;
    //Jaw angle - false is default and closed position, true is open.
    public boolean jawAng = false;
    
    public Jaws(int bottomJawLeftSolenoidId, int bottomJawRightSolenoidId, int topJawSolenoidId, int rollerTalonId, int RshooterSolenoidId, int LshooterSolenoidId, Joystick joy)
    {   
        RshooterSolenoid = new Solenoid(RshooterSolenoidId);
        LshooterSolenoid = new Solenoid(LshooterSolenoidId);
        bottomJawLeftSolenoid = new Solenoid(bottomJawLeftSolenoidId);
        bottomJawRightSolenoid = new Solenoid(bottomJawRightSolenoidId);
        topJawSolenoid = new Solenoid(topJawSolenoidId);
        rollerTalon = new Talon(rollerTalonId);
        this.joy = joy;
    }
    
    static class State{
        static int defense = 0;
        static int floorIntake = 1;
        static int humanIntake = 2;
        static int lowPossession = 3;
        static int highPossession = 4;
        static int floorPass = 5;
        static int highPass = 6;
        static int shoot = 7;
        static int shooterReset = 8;
    }
    
    public void lowerJaw()
    {
        if(jawPos)
        {
            bottomJawLeftSolenoid.set(false);
            bottomJawRightSolenoid.set(false);
            jawPos = false;
        }
    }
    
    public void update(){
        
    }
    
    public void raiseJaw()
    {
        if(!jawPos)
        {
            //True is activated and false is not...as far as I know
            bottomJawLeftSolenoid.set(true);
            bottomJawRightSolenoid.set(true);
            jawPos = true;
        }
    }
    
    public void openJaw()
    {
        if(!jawAng)
        {
            topJawSolenoid.set(true);
            jawAng = true;
        }
    }
    
    public void closeJaw()
    {
        if(jawAng)
        {
            topJawSolenoid.set(false);
            jawAng = false;
        }
    }
    
    public void intakeRoller()
    {
        rollerTalon.set(1);
    }
    
    public void outRoller()
    {
        rollerTalon.set(-1);
    }
    
    public void offRoller()
    {
        rollerTalon.set(0);
    }
    
    public void shoot()
    {
        RshooterSolenoid.set(true);
        LshooterSolenoid.set(true);
    }
    
    public void shooterReset()
    {
        RshooterSolenoid.set(false);
        LshooterSolenoid.set(false);
    }
    
    public void floorIntake()
    {
        lowerJaw();
        closeJaw();
        intakeRoller();    
        shooterReset();
    }
    
    public void humanIntake()
    {
        raiseJaw();
        openJaw();
        offRoller();      
        shooterReset();       
    }
    
    public void possession()
    {
        raiseJaw();
        closeJaw();
        offRoller();
        shooterReset();
    }
    
    public void floorPass()
    {
        lowerJaw();
        closeJaw();
        outRoller();
        shooterReset();
    }
    
    public void highPass()
    {
        raiseJaw();
        closeJaw();
        outRoller();       
        shooterReset();
    }
    
    public void robotShoot()
    {
        raiseJaw();
        openJaw();
        offRoller();
        shoot();
    }
}