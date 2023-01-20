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
import edu.wpi.first.net.PortForwarder;
import org.photonvision.PhotonCamera;
import org.photonvision.targeting.PhotonTrackedTarget;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
 

  // private final Joystick m_Xbox_Drive = new Joystick(1);
  // private final XboxController m_Xbox_Co_Drive = new XboxController(0);

  // private final Compressor pcm_Compressor = new Compressor(0, PneumaticsModuleType.CTREPCM);

  // private final CANSparkMax m_Front_Left = new CANSparkMax(1, MotorType.kBrushless);
  // private final CANSparkMax m_Front_Right = new CANSparkMax(10, MotorType.kBrushless);
  // private final CANSparkMax m_Back_Left = new CANSparkMax(2, MotorType.kBrushless);
  // private final CANSparkMax m_Back_Right = new CANSparkMax(9, MotorType.kBrushless);

  // private final MotorControllerGroup m_Left = new MotorControllerGroup(m_Front_Left, m_Back_Left);
  // private final MotorControllerGroup m_Right = new MotorControllerGroup(m_Front_Right, m_Back_Right);

  // private final DifferentialDrive m_Drive = new DifferentialDrive(m_Left, m_Right);

  // private final DoubleSolenoid p_Intake_Left = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 2, 3);
  // private final DoubleSolenoid p_Intake_Right = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 6, 7);

  // //private final CANSparkMax m_Aim = new CANSparkMax(8, MotorType.kBrushless);
  // private final CANSparkMax m_Intake = new CANSparkMax(3, MotorType.kBrushed);
  // private final CANSparkMax m_Elevator = new CANSparkMax(6, MotorType.kBrushless);
  // private final CANSparkMax m_Winch = new CANSparkMax(5, MotorType.kBrushless);
  // private final CANSparkMax m_Shoot1 = new CANSparkMax(4, MotorType.kBrushed);
  // private final CANSparkMax m_Shoot2 = new CANSparkMax(7, MotorType.kBrushed);

  // private final Timer m_timer = new Timer();
  // private final Timer m_timer2 = new Timer();
  // public static boolean m_first = false;
  // public static boolean m_second = false;
  // public static boolean m_third = false;                                                                                                                                                                                                                                                                                                                                                                                                                              
  // private boolean m_stopAndShoot = false;
  // private boolean m_driveer = false;
  // private boolean m_done = false;

  PhotonCamera camera = new PhotonCamera("microsoftCamera");

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
  
    PortForwarder.add(5800, "photonvision.local", 5800);
    
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    
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
    

  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {

  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {

  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {
    var result = camera.getLatestResult();
    PhotonTrackedTarget target = result.getBestTarget();
    if (!(target == null)) {
      double area = target.getArea();
      System.out.println(area);
    }
  }

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
}
