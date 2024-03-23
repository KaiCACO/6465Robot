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
import frc.robot.Constants.AutoConstants;
import frc.robot.Constants.DriveConstants;
import frc.robot.Constants.OIConstants;
import frc.robot.subsystems.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.SwerveControllerCommand;
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
  private final Timer m_Timer = new Timer();

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
    public Command getLeftAutoCommand(CANSparkMax m_Intake, CANSparkMax m_SecondIntake, CANSparkMax m_ShooterLeft, CANSparkMax m_ShooterRight) {
        return new SequentialCommandGroup(
            Commands.runOnce(() -> {m_Timer.reset(); m_Timer.start();})
            .andThen(
                shoot(m_Intake, m_SecondIntake, m_ShooterLeft, m_ShooterRight).withTimeout(2.5)
            )
            .andThen(
                Commands.runOnce(() -> {
                    m_Intake.set(1);
                    m_SecondIntake.set(1);
                    m_ShooterLeft.set(-0.1);
                    m_ShooterRight.set(-0.1);
                    m_Timer.stop();
                    m_Timer.reset();
                })
            ),

            Commands.waitSeconds(0.5)
        );
    }
    public Command getRightAutoCommand(CANSparkMax m_Intake, CANSparkMax m_SecondIntake, CANSparkMax m_ShooterLeft, CANSparkMax m_ShooterRight) {
        return new SequentialCommandGroup(
            Commands.runOnce(() -> {m_Timer.reset(); m_Timer.start();})
            .andThen(
                shoot(m_Intake, m_SecondIntake, m_ShooterLeft, m_ShooterRight).withTimeout(2.5)
            )
            .andThen(
                Commands.runOnce(() -> {
                    m_Intake.set(1);
                    m_SecondIntake.set(1);
                    m_ShooterLeft.set(-0.1);
                    m_ShooterRight.set(-0.1);
                    m_Timer.stop();
                    m_Timer.reset();
                })
            ),

            Commands.waitSeconds(0.5)
        );
    }

    public Command getMiddleAutoCommand(CANSparkMax m_Intake, CANSparkMax m_SecondIntake, CANSparkMax m_ShooterLeft, CANSparkMax m_ShooterRight) {
        // Create config for trajectory
        TrajectoryConfig config = new TrajectoryConfig(
            AutoConstants.kMaxSpeedMetersPerSecond,
            AutoConstants.kMaxAccelerationMetersPerSecondSquared)
            // Add kinematics to ensure max speed is actually obeyed
            .setKinematics(DriveConstants.kDriveKinematics);

        Trajectory forwardTrajectory = TrajectoryGenerator.generateTrajectory(
            new Pose2d(0, 0, Rotation2d.fromDegrees(0)),
            List.of(new Translation2d(1.5, 0), new Translation2d(0.5, 0.1)),
            new Pose2d(0, 0, Rotation2d.fromDegrees(0)),
            config);

        Trajectory forwardLeftTrajectory = TrajectoryGenerator.generateTrajectory(
            new Pose2d(0, 0, Rotation2d.fromDegrees(0)),
            List.of(new Translation2d(0.5, 1.1), new Translation2d(1.5, 1.2)),
            new Pose2d(0.1, 0, Rotation2d.fromDegrees(0)),
            config);

        Trajectory forwardRightTrajectory = TrajectoryGenerator.generateTrajectory(
            new Pose2d(0, 0, Rotation2d.fromDegrees(0)),
            List.of(new Translation2d(0.5, -1.1), new Translation2d(1.5, -1.2)),
            new Pose2d(0.1, 0, Rotation2d.fromDegrees(0)),
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
        
        // Reset odometry to the starting pose
        m_robotDrive.resetOdometry(forwardTrajectory.getInitialPose());

        // Sequence the commands with motor activation
        return new SequentialCommandGroup(
            Commands.runOnce(() -> {m_Timer.reset(); m_Timer.start(); m_Intake.setInverted(true); m_ShooterLeft.setInverted(false); m_ShooterRight.setInverted(false);}),
            shoot(m_Intake, m_SecondIntake, m_ShooterLeft, m_ShooterRight).withTimeout(1.8),

            Commands.runOnce(() -> {
                m_Intake.set(1);
                m_SecondIntake.set(1);
                m_ShooterLeft.set(-0.1);
                m_ShooterRight.set(-0.1);
            }),

            forwardCommand.andThen(() -> m_robotDrive.drive(0, 0, 0, false, false)),

            Commands.runOnce(() -> {m_Timer.reset(); m_Timer.start();}),
            shoot(m_Intake, m_SecondIntake, m_ShooterLeft, m_ShooterRight).withTimeout(1.8),

            Commands.runOnce(() -> {
                m_Intake.set(1);
                m_SecondIntake.set(1);
                m_ShooterLeft.set(-0.1);
                m_ShooterRight.set(-0.1);
            }),

            forwardLeftCommand.andThen(() -> m_robotDrive.drive(0, 0, 0, false, false)),

            Commands.runOnce(() -> {m_Timer.reset(); m_Timer.start();}),
            shoot(m_Intake, m_SecondIntake, m_ShooterLeft, m_ShooterRight).withTimeout(1.8),

            Commands.runOnce(() -> {
                m_Intake.set(1);
                m_SecondIntake.set(1);
                m_ShooterLeft.set(-0.1);
                m_ShooterRight.set(-0.1);
            }),

            forwardRightCommand.andThen(() -> m_robotDrive.drive(0, 0, 0, false, false)),

            Commands.runOnce(() -> {m_Timer.reset(); m_Timer.start();}),
            shoot(m_Intake, m_SecondIntake, m_ShooterLeft, m_ShooterRight).withTimeout(1.8),
            Commands.runOnce(() -> {
                m_Intake.set(0);
                m_SecondIntake.set(0);
                m_ShooterLeft.set(0);
                m_ShooterRight.set(0);
            })
        );
    }

    private Command shoot(CANSparkMax m_Intake, CANSparkMax m_SecondIntake, CANSparkMax m_ShooterLeft, CANSparkMax m_ShooterRight) {
        return Commands.run(()->{            
            if (!m_Timer.hasElapsed(0.5)) {
                m_Intake.set(-0.5);
            }
            else if (!m_Timer.hasElapsed(1.4)) {
                m_Intake.set(0);
                m_ShooterLeft.set(1);
                m_ShooterRight.set(1);
            }
            else if (!m_Timer.hasElapsed(1.8)) {
                m_Intake.set(1);
            }
        });
    };
}