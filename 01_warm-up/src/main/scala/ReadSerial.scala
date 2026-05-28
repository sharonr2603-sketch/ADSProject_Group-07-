// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package readserial

import chisel3._
import chisel3.util._


/** controller class */
class Controller extends Module{
  
  val io = IO(new Bundle {

    // Inputs
    val rxd = Input(Bool())

    val countDone = Input(Bool())

    // Outputs
    val enable = Output(Bool())

    val valid = Output(Bool())
  })

  // INTERNAL VARIABLES

  // FSM states
  val idle :: receive :: Nil = Enum(2)

  // Current state register
  val stateReg = RegInit(idle)

  // Registered valid pulse
  val validReg = RegInit(false.B)

  // DEFAULT OUTPUTS


  io.enable := false.B

  io.valid := validReg

  // valid pulse lasts only one clock cycle
  validReg := false.B


  // FSM


  switch(stateReg){


    // IDLE STATE
    // Wait for start bit = 0

    is(idle){

      when(io.rxd === false.B){

        stateReg := receive
      }
    }


    // RECEIVE STATE
    // Receive 8 serial bits


    is(receive){

      // Enable counter + shift register
      io.enable := true.B

      // 8 bits received
      when(io.countDone){

        // Generate valid pulse
        validReg := true.B

        // Return to idle
        stateReg := idle
      }
    }
  }
}


/** counter class */
class Counter extends Module{
  
  val io = IO(new Bundle {

    val enable = Input(Bool())

    val done = Output(Bool())
  })


  // INTERNAL VARIABLES


  // 3-bit counter (counts 0 to 7)
  val countReg = RegInit(0.U(3.W))

  // =====================================================
  // FUNCTIONALITY
  // =====================================================

  io.done := false.B

  when(io.enable){

    when(countReg === 7.U){

      // 8 bits received
      io.done := true.B

      // Reset counter
      countReg := 0.U

    }.otherwise{

      // Increment counter
      countReg := countReg + 1.U
    }
  }
}


/** shift register class */
class ShiftRegister extends Module{
  
  val io = IO(new Bundle {

    val enable = Input(Bool())

    val serialIn = Input(Bool())

    val parallelOut = Output(UInt(8.W))
  })

  
  // INTERNAL VARIABLES
  

  // 8-bit shift register
  val shiftReg = RegInit(0.U(8.W))

  
  // FUNCTIONALITY
  

  when(io.enable){

    // Shift left and insert new serial bit
    shiftReg := Cat(shiftReg(6,0), io.serialIn )
  }

  io.parallelOut := shiftReg
}


/** 
  * Serial Receiver Top Module
  */
class ReadSerial extends Module{
  
  val io = IO(new Bundle {

    val rxd = Input(Bool())

    val data = Output(UInt(8.W))

    val valid = Output(Bool())
  })


  
  // MODULE INSTANTIATION
  

  val controller = Module(new Controller())

  val counter = Module(new Counter())

  val shiftRegister = Module(new ShiftRegister())

  
  // CONNECTIONS
  

  // Controller input
  controller.io.rxd := io.rxd

  // Counter done -> controller
  controller.io.countDone := counter.io.done

  // Controller enable -> counter
  counter.io.enable := controller.io.enable

  // Controller enable -> shift register
  shiftRegister.io.enable := controller.io.enable

  // Serial input -> shift register
  shiftRegister.io.serialIn := io.rxd

  
  // OUTPUTS
  

  io.data := shiftRegister.io.parallelOut

  io.valid := controller.io.valid 

  
  // DEBUG PRINT
  

  printf(p"RXD=${io.rxd} DATA=${io.data} VALID=${io.valid}\n")
}