// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Shoot and Drive";
  private static final String kCustomAuto = "Shoot";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  
  NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
  NetworkTableEntry tx = table.getEntry("tx");
  NetworkTableEntry ty = table.getEntry("ty");
  NetworkTableEntry ta = table.getEntry("ta");
  NetworkTableEntry tv = table.getEntry("tv");
  NetworkTableEntry pipeline = table.getEntry("pipeline");

  Double ang = null;
  Double dist = null;
  Double xd1;

  private final Joystick m_Xbox_Drive = new Joystick(0);
  private final XboxController m_Xbox_Co_Drive = new XboxController(1);

  private final Compressor pcm_Compressor = new Compressor(0, PneumaticsModuleType.CTREPCM);

  private final CANSparkMax m_Front_Left = new CANSparkMax(9, MotorType.kBrushless);
  private final CANSparkMax m_Front_Right = new CANSparkMax(2, MotorType.kBrushless);
  private final CANSparkMax m_Back_Left = new CANSparkMax(3, MotorType.kBrushless);
  private final CANSparkMax m_Back_Right = new CANSparkMax(1, MotorType.kBrushless);

  private final MotorControllerGroup m_Left = new MotorControllerGroup(m_Front_Left, m_Back_Left);
  private final MotorControllerGroup m_Right = new MotorControllerGroup(m_Front_Right, m_Back_Right);

  private final DifferentialDrive m_Drive = new DifferentialDrive(m_Left, m_Right);

  private final DoubleSolenoid p_Intake_Left = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 2, 3);
  private final DoubleSolenoid p_Intake_Right = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 1, 0);

  //private final CANSparkMax m_Aim = new CANSparkMax(8, MotorType.kBrushless);
  private final CANSparkMax m_Intake = new CANSparkMax(10, MotorType.kBrushed);
  private final CANSparkMax m_Elevator = new CANSparkMax(6, MotorType.kBrushless);
  private final CANSparkMax m_Winch = new CANSparkMax(5, MotorType.kBrushless);
  private final CANSparkMax m_Shoot1 = new CANSparkMax(4, MotorType.kBrushed);
  private final CANSparkMax m_Shoot2 = new CANSparkMax(7, MotorType.kBrushed);
  private final CANSparkMax m_Shoot_Back = new CANSparkMax(8, MotorType.kBrushed);

  private final Timer m_timer = new Timer();
  private final Timer m_timer2 = new Timer();
  public static boolean m_first = false;
  public static boolean m_second = false;
  public static boolean m_third = false;                                                                                                                                                                                                                                                                                                                                                                                                                              
  private boolean m_stopAndShoot = false;
  private boolean m_driveer = false;
  private boolean m_done = false;
  private double speed = 0;
  private double getY;



  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);


    m_Left.setInverted(false);
    m_Right.setInverted(true);


  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * 
   * 
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    double x = tx.getDouble(0.0);
    double y = ty.getDouble(0.0);
    double area = ta.getDouble(0.0);

    ang = (y * (Math.PI / 180)) + 0.4188;

    dist =  64 / Math.tan(ang);

    SmartDashboard.putNumber("Distance", dist);
    SmartDashboard.putNumber("LimelightX", x);
    SmartDashboard.putNumber("LimelightY", y);
    SmartDashboard.putNumber("LimelightArea", area);
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);

    m_Elevator.set(0);
    m_Shoot1.set(0);
    m_Shoot2.set(0);
    m_Shoot_Back.set(0);

    m_timer.reset();
    m_timer.start();
    m_stopAndShoot = false;
    m_driveer = false;
    m_done = false;
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        if(m_stopAndShoot == false){
          // m_Left.set(.5);
          // m_Right.set(.5);
          m_Shoot1.set(0.48);
          m_Shoot2.set(-0.48);
        }
        if(m_timer.advanceIfElapsed(5.0)){
          m_Elevator.set(0.40);
          m_stopAndShoot = true;
        }
        break;
      case kDefaultAuto:
      default:

      if(m_stopAndShoot == false){
        m_Shoot1.set(0.48);
        m_Shoot2.set(-0.48); 
        if(m_timer.advanceIfElapsed(5.0)){
          m_Elevator.set(0.40);
          m_stopAndShoot = true;
        }
      }

      if(m_stopAndShoot == true && m_driveer == false){
        if(m_timer.advanceIfElapsed(1.0)){
          m_Elevator.set(0);
          m_Shoot1.set(0);
          m_Shoot2.set(0);
          m_driveer = true;
        }
      }

      if(m_driveer == true && m_done == false){
          m_Left.set(.20);
          m_Right.set(.20);
          if(m_timer.advanceIfElapsed(3.5)){
            m_done = true;
          }
      }

      if(m_done == true){
        m_Left.set(0.0);
        m_Right.set(0.0);
      }

        break;
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    p_Intake_Left.set(Value.kForward);
    p_Intake_Right.set(Value.kForward);
    pcm_Compressor.enableDigital();
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    getY = -m_Xbox_Drive.getY();

    speed = speed + (getY - speed)/12;

    m_Drive.arcadeDrive(speed/1.4, m_Xbox_Drive.getX());

    if(m_Xbox_Co_Drive.getLeftTriggerAxis() > .50){
      m_Intake.set(m_Xbox_Co_Drive.getLeftTriggerAxis());
    }
    else{
      m_Intake.set(0.0);
    }

    

    // if(m_Xbox_Co_Drive.getRightTriggerAxis() > .10){
    //   double y1 = dist;
    //   double m = 3.73289;
    //   double b = -101.579;
    //   xd1 = (.01 * ((y1 - b) / m)) + 0.18;

    //   if(xd1 > 1.00){
    //     xd1 = 1.00;
    //   }
    //   if(xd1 < 0.25){
    //     xd1 = 0.00;
    //   }
    //   m_Shoot1.set(xd1);
    //   m_Shoot2.set(-xd1);
    // }
    if(m_Xbox_Co_Drive.getYButton() == true){

      if(m_first == false && m_second == false && m_third == false){
          m_Shoot1.set(0.48);
          m_Shoot2.set(-0.48);

          m_timer2.start();
          if(m_timer2.advanceIfElapsed(1.3)){
              m_Elevator.set(.40);
              m_first = true;
          }
      }

      if(m_first == true && m_second == false && m_third == false){
          if(m_timer2.advanceIfElapsed(0.2)){
              m_Elevator.set(0.0);
              m_second = true;
          }
      }
      if(m_first == true && m_second == true && m_third == false){
          if(m_timer2.advanceIfElapsed(1.2)){
              m_Elevator.set(0.40);
              m_third = true;
          }
      }
      if(m_first == true && m_second == true && m_third == true){
          if(m_timer2.advanceIfElapsed(1.0)){
              m_Elevator.set(0.0);
              m_Shoot1.set(0);
              m_Shoot2.set(0);

              m_timer2.stop();
              m_timer2.reset();

              m_first = false;
              m_second = false;
              m_third = false;
          }
      }

  }
    else{

      if(m_Xbox_Co_Drive.getRightY() > 0.20){
        m_Elevator.set(0.40);
      }
      else if (m_Xbox_Co_Drive.getRightY() < -0.20 ){
        m_Elevator.set(-0.40);
      }
      else{
        m_Elevator.set(0.00);
      }    

      if(m_Xbox_Co_Drive.getBButton() == true){
        m_Shoot1.set(0.7);
        m_Shoot2.set(-0.7);
  
      }

      else {
        m_Shoot1.set(0.00);
        m_Shoot2.set(0.00);
      }

      if(m_Xbox_Co_Drive.getXButton()){
        m_Shoot_Back.set(0.5);
      }

      else if(m_Xbox_Co_Drive.getAButton()){
        m_Shoot_Back.set(-0.5);
      }

      else {
        m_Shoot_Back.set(0);
      }



    }




    if(m_Xbox_Co_Drive.getStartButtonPressed()){
      m_Winch.set(.75);
    }
    else if (m_Xbox_Co_Drive.getStartButton() == false){
      if(m_Xbox_Co_Drive.getBackButton() == true){
        m_Winch.set(-0.75);
      }
      else if(m_Xbox_Co_Drive.getStartButton() == false){
        m_Winch.set(0.00);
      }
    }


    // if(m_Xbox_Co_Drive.getAButtonPressed()){

    //   double x = tx.getDouble(0.0);

    //   if(x > .2){
    //     m_Aim.set(.2);
    //   }
    //   if(x < -.12){
    //     m_Aim.set(-.2);
    //   }
    //   if (x > -.2 && x < .2) {
    //     m_Aim.set(0.0);
    //   }
    // }
    // else if (m_Xbox_Co_Drive.getLeftX() > 0.20 || m_Xbox_Co_Drive.getLeftX() < -0.20){
    //   m_Aim.set(m_Xbox_Co_Drive.getLeftX());
    // }
    // // else if (m_Xbox_Co_Drive.getLeftX() > 0.00 && m_Xbox_Co_Drive.getLeftX() < 0.00){
    // //   m_Aim.set(0.00);
    // // }
    // else{
    //   m_Aim.set(0.00);
    // }

    
      
    
    if(m_Xbox_Co_Drive.getLeftBumperPressed()){
      p_Intake_Left.set(Value.kReverse);
      p_Intake_Right.set(Value.kReverse);
      //r extend
    }
    if(m_Xbox_Co_Drive.getRightBumperPressed()){
      p_Intake_Left.set(Value.kForward);
      p_Intake_Right.set(Value.kForward);
      //r retract
    }
    else if (m_Xbox_Drive.getRawButtonPressed(2)){
      p_Intake_Left.set(Value.kForward);
      p_Intake_Right.set(Value.kForward);
    }
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
    m_timer.reset();
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {

        pcm_Compressor.enableDigital();

    if(m_Xbox_Co_Drive.getStartButtonPressed()){
      m_Winch.set(.50);
    }
    else if (m_Xbox_Co_Drive.getStartButton() == false){
      if(m_Xbox_Co_Drive.getBackButton() == true){
        m_Winch.set(-0.40);
      }
      else if(m_Xbox_Co_Drive.getStartButton() == false){
        m_Winch.set(0.00);
      }
    }
  }

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
}
