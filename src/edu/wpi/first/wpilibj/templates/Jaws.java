/*
 * Class to handle all inputs, states, and transitions of the shooter/intake assembly.
 * 2014 FRC Team 1736 Robot Casserole
 */

package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Jaws {
    Solenoid bottomJawLeftSolenoid, bottomJawRightSolenoid, topJawSolenoid, LshooterSolenoid, RshooterSolenoid;
    Talon rollerTalon;
    
    int lastState = State.highPossession;
    int currentState = State.highPossession;
    int desiredState = State.highPossession;
    
    Joystick shooterJoy;
    
    //Array of time in seconds that it takes to complete each state
    double[] stateTimers = {0, 0, 0, 0.325, 1.2, 1, 1, 1};
    double timeInState = 0;
    double lastTime = 0;
    
    //Jaw position - true is default and up position, false is down.
    public boolean jawPos = true;
    //Jaw angle - false is default and closed position, true is open.
    public boolean jawAng = false;
    public double lastDPadval = 0;
    
    public Jaws(int bottomJawLeftSolenoidId, int bottomJawRightSolenoidId, int topJawSolenoidId, int rollerTalonId, int RshooterSolenoidId, int LshooterSolenoidId, Joystick shooterJoy)
    {   
        RshooterSolenoid = new Solenoid(RshooterSolenoidId);
        LshooterSolenoid = new Solenoid(LshooterSolenoidId);
        bottomJawLeftSolenoid = new Solenoid(bottomJawLeftSolenoidId);
        bottomJawRightSolenoid = new Solenoid(bottomJawRightSolenoidId);
        topJawSolenoid = new Solenoid(topJawSolenoidId);
        rollerTalon = new Talon(rollerTalonId);
        this.shooterJoy = shooterJoy;
    }
    
    public static class State{
        static int floorIntake = 0;
        static int humanIntake = 1;
        static int highPossession = 2;
        static int trussPass = 3;
        static int shoot = 4;
        static int shooterReset = 5;
        static int shotPrep = 6;
        static int trussPrep = 7;
    }
    
    public void update()
    {
        SmartDashboard.putNumber("Truss Delay", stateTimers[State.trussPass]);
        if(shooterJoy.getRawAxis(3) < -0.5)
        {
            intakeRoller();
        }
        else if(shooterJoy.getRawAxis(3) > 0.5)
        {
            outRoller();
        }
        else
        {
            rollerTalon.set(0);
        }
        if(shooterJoy.getRawAxis(6) < -0.5 && lastDPadval < 0)
        {
            if(stateTimers[State.trussPass] > 0.1)
            {
                stateTimers[State.trussPass] = stateTimers[State.trussPass] - 0.025;
            }
        }
        else if(shooterJoy.getRawAxis(6) > 0.5 && lastDPadval > 0)
        {
            if(stateTimers[State.trussPass] < 0.5)
            {
                stateTimers[State.trussPass] = stateTimers[State.trussPass] + 0.025;
            }
        }
        lastDPadval = shooterJoy.getRawAxis(6);
        double currentTime = Timer.getFPGATimestamp();
        timeInState += currentTime - lastTime;
        lastTime = currentTime;
        boolean needStateTransition = false;
        if(currentState == State.shoot)
        {
            desiredState = State.shooterReset;
            needStateTransition = true;
        }
        else if(currentState == State.trussPass)
        {
            desiredState = State.shooterReset;
            needStateTransition = true;
        }
        else
        {
            if(desiredState != State.shoot && desiredState != State.trussPass)
            {
                desiredState = currentState;
            }
            else
            {
                needStateTransition = true;
            }
        }
        if(!needStateTransition) 
        {
            //Set state from controller
            if(shooterJoy.getRawButton(2))
            {
                desiredState = State.highPossession;
            }
            else if(shooterJoy.getRawButton(1))
            {
                desiredState = State.floorIntake;
            }
            else if(shooterJoy.getRawButton(4))
            {
                desiredState = State.humanIntake;
            }
            if(shooterJoy.getRawButton(5) && currentState != State.highPossession && currentState != State.floorIntake)
            {
                desiredState = State.trussPass;
            }
            else if(shooterJoy.getRawButton(5) && currentState == State.highPossession)
            {
                desiredState = State.trussPrep;
            }
            else if(shooterJoy.getRawButton(5) && currentState == State.floorIntake)
            {
                desiredState = State.floorIntake;
            }
            else if(shooterJoy.getRawButton(6) && currentState != State.highPossession && currentState != State.floorIntake)
            {   
                desiredState = State.shoot;
            }
            else if(shooterJoy.getRawButton(6) && currentState == State.highPossession)
            {   
                desiredState = State.shotPrep;
            }
            else if(shooterJoy.getRawButton(6) && currentState == State.floorIntake)
            {
                desiredState = State.floorIntake;
            }
            else if(shooterJoy.getRawButton(9) && shooterJoy.getRawButton(10))
            {
                desiredState = State.highPossession;
            }
        }
        if(timeInState >= stateTimers[currentState])
        {
            if(desiredState == State.highPossession)
            {
                //System.out.println("High Possession Called");
                highPossession();
                if(currentState != State.highPossession)
                {
                    currentState = State.highPossession;
                    timeInState = 0;
                }
            }
            else if(desiredState == State.floorIntake)
            {
                //System.out.println("Floor intake called");
                floorIntake();
                if(currentState != State.floorIntake)
                {
                    currentState = State.floorIntake;
                    timeInState = 0;
                }
            }
            else if(desiredState == State.humanIntake)
            {
                //System.out.println("Human Intake Called");
                humanIntake();
                if(currentState != State.humanIntake)
                {
                    currentState = State.humanIntake;
                    timeInState = 0;
                }
            }
            else if(desiredState == State.shoot)
            {
                //System.out.println("Shoot Called");
                shoot();
                if(currentState != State.shoot)
                {
                    currentState = State.shoot;
                    timeInState = 0;
                }
            }
            else if(desiredState == State.trussPass)
            {
                //System.out.println("Truss Pass Called");
                shoot();
                if(currentState != State.trussPass)
                {
                    currentState = State.trussPass;
                    timeInState = 0;
                }
            }
            else if(desiredState == State.shooterReset)
            {
                //System.out.println("Shooter Reset Called");
                shooterReset();
                if(currentState != State.shooterReset)
                {
                    currentState = State.shooterReset;
                    timeInState = 0;
                }
            }
            else if(desiredState == State.trussPrep)
            {
                //System.out.println("Truss Prep Called");
                openJaw();
                if(currentState != State.trussPrep)
                {
                    currentState = State.trussPrep;
                    timeInState = 0;
                }
                desiredState = State.trussPass;
            }
            else if(desiredState == State.shotPrep)
            {
                //System.out.println("Shot Prep Called");
                openJaw();
                if(currentState != State.shotPrep)
                {
                    currentState = State.shotPrep;
                    timeInState = 0;
                }
                desiredState = State.shoot;
            }
        }
    }
    
    public void raiseJaw()
    {
        //System.out.println("raiseJaw");
        //True is activated and false is not...as far as I know
        bottomJawLeftSolenoid.set(false);
        bottomJawRightSolenoid.set(false);
        jawPos = true;
    }
    
        public void lowerJaw()
    {
        //System.out.println("Lower Jaw");
        bottomJawLeftSolenoid.set(true);
        bottomJawRightSolenoid.set(true);
        jawPos = false;
    }
    
    public void openJaw()
    {
        //System.out.println("Open Jaw");
        topJawSolenoid.set(true);
        jawAng = true;
    }
    
    public void closeJaw()
    {
        //System.out.println("Close Jaw");
        topJawSolenoid.set(false);
        jawAng = false;
    }
    
    public void intakeRoller()
    {
        rollerTalon.set(1);
    }
    
    public void outRoller()
    {
        rollerTalon.set(shooterJoy.getRawAxis(3) * -1);
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
    }
    
    public void humanIntake()
    {
        raiseJaw();
        openJaw();             
    }
    
    public void highPossession()
    {
        raiseJaw();
        closeJaw();
    }
    
    public void lowPossession()
    {
        lowerJaw();
        closeJaw();
    }
    
    public void highPass()
    {
        raiseJaw();
        closeJaw();     
    }
}