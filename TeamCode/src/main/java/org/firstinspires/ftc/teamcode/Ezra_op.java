/* Copyright (c) 2021 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name="Ezra_op", group="Linear OpMode")
public class Ezra_op extends LinearOpMode {

    // Declare OpMode members for each of the 4 motors.
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor leftFrontDrive = null;
    private DcMotor leftBackDrive = null;
    private DcMotor rightFrontDrive = null;
    private DcMotor rightBackDrive = null;
    private DcMotor slideRight = null;
    private DcMotor intake = null;
    private Servo elbow_Left = null;
    private Servo elbow_Right = null;
    private Servo claw_Green;
    private Servo claw_Red;
    private  double MAX_POSITION = 3;
    private Hardware hardware;

    @Override
    public void runOpMode() {

        // Initialize the hardware variables. Note that the strings used here must correspond
        // to the names assigned during the robot configuration step on the DS or RC devices.
        hardware = new Hardware(hardwareMap);
        slideRight = hardwareMap.get(DcMotor.class, "SR");
        leftFrontDrive = hardwareMap.get(DcMotor.class, "FL");
        leftBackDrive = hardwareMap.get(DcMotor.class, "BL");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "FR");
        rightBackDrive = hardwareMap.get(DcMotor.class, "BR");
        intake = hardwareMap.get(DcMotor.class, "IT");
        elbow_Left = hardwareMap.get(Servo.class, "EL");
        elbow_Right = hardwareMap.get(Servo.class, "ER");
        claw_Green = hardwareMap.get(Servo.class, "CG");
        claw_Red = hardwareMap.get(Servo.class, "CR");
        // Wait for the game to start (driver presses PLAY)
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();
        runtime.reset();

        boolean slowMode = false;
        boolean slideSlowMode = false;

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            double max;

            // POV Mode uses left joystick to go forward & strafe, and right joystick to rotate.
            double axial   = -gamepad1.left_stick_y;  // Note: pushing stick forward gives negative value
            double lateral =  gamepad1.left_stick_x;
            double yaw     =  gamepad1.right_stick_x;
            // Combine the joystick requests for each axis-motion to determine each wheel's power.
            // Set up a variable for each drive wheel to save the power level for telemetry.
            double leftFrontPower  = axial + lateral + yaw;
            double rightFrontPower = axial - lateral - yaw;
            double leftBackPower   = axial - lateral + yaw;
            double rightBackPower  = axial + lateral - yaw;
            double slidePower = gamepad2.left_stick_y;



            // Normalize the values so no wheel power exceeds 100%
            // This ensures that the robot maintains the desired motion.
            max = Math.max(Math.abs(leftFrontPower), Math.abs(rightFrontPower));
            max = Math.max(max, Math.abs(leftBackPower));
            max = Math.max(max, Math.abs(rightBackPower));

            if (max > 1.0) {
                leftFrontPower  /= max;
                rightFrontPower /= max;
                leftBackPower   /= max;
                rightBackPower  /= max;
            }

            if (gamepad1.left_bumper)
                slowMode = !slowMode;
            if (gamepad2.a)
                slideSlowMode = !slideSlowMode;

            // Send calculated power to wheels
            double powers[] = { leftFrontPower, leftBackPower, rightBackPower, rightFrontPower };
            if (slowMode)
                hardware.setMotorSlowMode(powers);
            else
                hardware.setMotorPowers(powers);

            if ((slideRight.getPower() >0)&&(slideRight.getCurrentPosition() > MAX_POSITION)){
                slideRight.setPower(0);
            }

            if (slideSlowMode)
                hardware.setSlidesSlowMode(slidePower);
            else
                hardware.setSlidesPower(slidePower);


            if (gamepad1.dpad_up)
                hardware.setIntakePower(1);
            else if (gamepad1.dpad_down)
                hardware.setIntakePower(-1);
            else{
                hardware.setIntakePower(0);
            }

            if (gamepad2.right_stick_y > 0.2)
                hardware.setElbowPosition(1);
            else if (gamepad2.right_stick_y < -0.2)
                hardware.setElbowPosition(0);
            else{
                hardware.setElbowPosition(0.5);
            }

            if (gamepad2.left_bumper)
                hardware.setClaw_Greenposition(1);
            else if (gamepad2.left_trigger > 0.2)
                hardware.setClaw_Greenposition(
                        hardware.getClaw_Greenposition() - 0.05
                );

            if (gamepad2.right_bumper)
                hardware.setClaw_Redposition(1);
            else if (gamepad2.right_trigger > 0.2)
                hardware.setClaw_Redposition(
                        hardware.getClaw_Redposition() - 0.05
                );

            // Show the elapsed game time and wheel power.
            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("Front left/Right", "%4.2f, %4.2f", leftFrontPower, rightFrontPower);
            telemetry.addData("Back  left/Right", "%4.2f, %4.2f", leftBackPower, rightBackPower);
            telemetry.update();
        }
    }}
