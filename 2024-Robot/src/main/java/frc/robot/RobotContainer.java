// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.PS4Controller.Button;
import frc.robot.Constants.AutoConstants;
import frc.robot.Constants.DriveConstants;
import frc.robot.Constants.OIConstants;
import frc.robot.subsystems.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.SwerveControllerCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import java.util.List;

import com.revrobotics.CANSparkMax;

/*
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems
  private final DriveSubsystem m_robotDrive = new DriveSubsystem();

  // The driver's controller
  XboxController m_driverController = new XboxController(OIConstants.kDriverControllerPort);

  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
  public RobotContainer() {

    // Configure default commands
    m_robotDrive.setDefaultCommand(
        // The left stick controls translation of the robot.
        // Turning is controlled by the X axis of the right stick.
        new RunCommand(
            () -> m_robotDrive.drive(
                -MathUtil.applyDeadband(m_driverController.getLeftY(), OIConstants.kDriveDeadband),
                -MathUtil.applyDeadband(m_driverController.getLeftX(), OIConstants.kDriveDeadband),
                -MathUtil.applyDeadband(m_driverController.getRightX(), OIConstants.kDriveDeadband),
                true, true),
            m_robotDrive));
  }


  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
    public Command getLeftAutoCommand(CANSparkMax m_intake, CANSparkMax m_SecondIntake, CANSparkMax m_ShooterLeft, CANSparkMax m_ShooterRight) {
        Command shoot = new Shoot(m_ShooterLeft, m_ShooterRight, m_intake, m_SecondIntake);
        return shoot.withTimeout(4);
    }
    public Command getRightAutoCommand(CANSparkMax m_intake, CANSparkMax m_SecondIntake, CANSparkMax m_ShooterLeft, CANSparkMax m_ShooterRight) {
        Command shoot = new Shoot(m_ShooterLeft, m_ShooterRight, m_intake, m_SecondIntake);
        return shoot.withTimeout(4);
    }

    public Command getMiddleAutoCommand(CANSparkMax m_intake, CANSparkMax m_SecondIntake, CANSparkMax m_ShooterLeft, CANSparkMax m_ShooterRight) {
        // Create config for trajectory
        TrajectoryConfig config = new TrajectoryConfig(
            AutoConstants.kMaxSpeedMetersPerSecond,
            AutoConstants.kMaxAccelerationMetersPerSecondSquared)
            // Add kinematics to ensure max speed is actually obeyed
            .setKinematics(DriveConstants.kDriveKinematics);

        Trajectory forwardTrajectory = TrajectoryGenerator.generateTrajectory(
            new Pose2d(0, 0, Rotation2d.fromDegrees(0)),
            List.of(new Translation2d(1.5, 0)),
            new Pose2d(0, 0, Rotation2d.fromDegrees(0)),
            config);

        Trajectory forwardLeftTrajectory = TrajectoryGenerator.generateTrajectory(
            new Pose2d(0, 0, Rotation2d.fromDegrees(0)),
            List.of(new Translation2d(0.5, 1.0), new Translation2d(1.5, 1.0)),
            new Pose2d(0, 0, Rotation2d.fromDegrees(0)),
            config);

        Trajectory forwardRightTrajectory = TrajectoryGenerator.generateTrajectory(
            new Pose2d(0, 0, Rotation2d.fromDegrees(0)),
            List.of(new Translation2d(0.5, -1.0), new Translation2d(1.5, -1.0)),
            new Pose2d(0, 0, Rotation2d.fromDegrees(0)),
            config);


        var thetaController = new ProfiledPIDController(
            AutoConstants.kPThetaController, 0, 0, AutoConstants.kThetaControllerConstraints);
        thetaController.enableContinuousInput(-Math.PI, Math.PI);

        // Swerve controller commands for each trajectory
        SwerveControllerCommand forwardCommand = new SwerveControllerCommand(
            forwardTrajectory,
            m_robotDrive::getPose,
            DriveConstants.kDriveKinematics,
            new PIDController(AutoConstants.kPXController, 0, 0),
            new PIDController(AutoConstants.kPYController, 0, 0),
            thetaController,
            m_robotDrive::setModuleStates,
            m_robotDrive);

        // Swerve controller commands for each trajectory
        SwerveControllerCommand forwardLeftCommand = new SwerveControllerCommand(
            forwardLeftTrajectory,
            m_robotDrive::getPose,
            DriveConstants.kDriveKinematics,
            new PIDController(AutoConstants.kPXController, 0, 0),
            new PIDController(AutoConstants.kPYController, 0, 0),
            thetaController,
            m_robotDrive::setModuleStates,
            m_robotDrive);

        // Swerve controller commands for each trajectory
        SwerveControllerCommand forwardRightCommand = new SwerveControllerCommand(
            forwardRightTrajectory,
            m_robotDrive::getPose,
            DriveConstants.kDriveKinematics,
            new PIDController(AutoConstants.kPXController, 0, 0),
            new PIDController(AutoConstants.kPYController, 0, 0),
            thetaController,
            m_robotDrive::setModuleStates,
            m_robotDrive);

        Command shoot = Commands.startEnd(
            () -> {
                
            },
            () -> {

            }
        );

        Command intake = Commands.startEnd(
            () -> {m_intake.set(0.8);},
            () -> {m_intake.set(0);}
        )
        .withTimeout(4);
        // Reset odometry to the starting pose
        m_robotDrive.resetOdometry(forwardTrajectory.getInitialPose());

        // Sequence the commands with motor activation
        return new SequentialCommandGroup(
            shoot,

            forwardCommand
            .withTimeout(4)
            .alongWith(intake)

            // shoot,

            // forwardLeftCommand
            // .withTimeout(4),
            // //.alongWith(intake.withTimeout(4)),

            // shoot,

            // forwardRightCommand
            // .withTimeout(4),
            // //.alongWith(intake.withTimeout(4)),

            // shoot
        );
    }
    
    public class Shoot extends Command {
        private final Timer m_timer = new Timer();

        private CANSparkMax m_shooterLeft;
        private CANSparkMax m_shooterRight;
        private CANSparkMax m_intake;
        private CANSparkMax m_secondIntake;

        public Shoot(CANSparkMax m_ShooterLeft, CANSparkMax m_ShooterRight, CANSparkMax m_Intake, CANSparkMax m_SecondIntake) {
            this.m_shooterLeft = m_ShooterLeft;
            this.m_shooterRight = m_ShooterRight;
            this.m_intake = m_Intake;
            this.m_secondIntake = m_SecondIntake;
        }
    
        // Called just before this Command runs the first time
        public void initialize() {
            this.m_shooterLeft.set(0.6);
            this.m_shooterRight.set(0.6);
            m_timer.reset();
            m_timer.start();
        }

        // Called repeatedly when this Command is scheduled to run
        public void execute() {
            System.out.println(m_timer.get());
            if (!m_timer.hasElapsed(0.3)) {
                this.m_intake.set(-0.5);
                this.m_secondIntake.set(-0.5);
            }
            else if (!m_timer.hasElapsed(1.5)) {
                this.m_intake.set(0);
                this.m_secondIntake.set(0);
                this.m_shooterLeft.set(0.6);
                this.m_shooterRight.set(0.6);
            }
            else {
                this.m_intake.set(1);
                this.m_secondIntake.set(1);
            }
        }
    
        // Make this return true when this Command no longer needs to run execute()
        public boolean isFinished() {
            return m_timer.get() > 2.5;
        }
    
        // Called once after isFinished returns true
        public void end() {
            this.m_intake.set(0);
            this.m_secondIntake.set(0);
            this.m_shooterLeft.set(0);
            this.m_shooterRight.set(0);
        }
    
        // Called when another command which requires one or more of the same
        // subsystems is scheduled to run
        public void interrupted() {
           end();
        }
    }
    
}