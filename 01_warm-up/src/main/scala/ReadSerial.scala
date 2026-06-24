// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package readserial

import chisel3._
import chisel3.util._


/** controller class */
class Controller extends Module {

  val io = IO(new Bundle {

    val reset_n = Input(Bool())
    val rxd = Input(Bool())
    val countDone = Input(Bool())

    val enable = Output(Bool())
    val valid = Output(Bool())
  })

  val idle :: receive :: Nil = Enum(2)

  val stateReg = RegInit(idle)

  val validReg = RegInit(false.B)

  io.enable := false.B
io.valid := validReg

when(io.reset_n) {

  stateReg := idle
  validReg := false.B

}.otherwise {

  validReg := false.B

  switch(stateReg) {

    is(idle) {

      when(io.rxd === false.B) {
        stateReg := receive
      }
    }

    is(receive) {

      io.enable := true.B

      when(io.countDone) {

        validReg := true.B

        stateReg := idle
      }
    }
  }
}
}
/** counter class */
class Counter extends Module {

  val io = IO(new Bundle {

    val enable = Input(Bool())
    val reset_n = Input(Bool())

    val done = Output(Bool())
  })

  val countReg = RegInit(0.U(3.W))

  io.done := false.B

  when(io.reset_n) {

    countReg := 0.U

  }.otherwise {

    when(io.enable) {

      when(countReg === 7.U) {

        io.done := true.B

        countReg := 0.U

      }.otherwise {

        countReg := countReg + 1.U
      }
    }
  }
}


/** shift register class */
class ShiftRegister extends Module {

  val io = IO(new Bundle {

    val reset_n = Input(Bool())

    val enable = Input(Bool())

    val serialIn = Input(Bool())

    val parallelOut = Output(UInt(8.W))
  })

  val shiftReg = RegInit(0.U(8.W))

  when(io.reset_n) {

    shiftReg := 0.U

  }.elsewhen(io.enable) {

    shiftReg := Cat(shiftReg(6,0), io.serialIn)
  }

  io.parallelOut := shiftReg
}


/** 
  * Serial Receiver Top Module
  */
class ReadSerial extends Module{
  
    val io = IO(new Bundle {

  val rxd = Input(Bool())

  val reset_n = Input(Bool())

  val data = Output(UInt(8.W))

  val valid = Output(Bool())
})
  


  
  // MODULE INSTANTIATION
  

  val controller = Module(new Controller())

  val counter = Module(new Counter())

  val shiftRegister = Module(new ShiftRegister())

  
  // CONNECTIONS

  controller.io.reset_n := io.reset_n

counter.io.reset_n := io.reset_n

shiftRegister.io.reset_n := io.reset_n

controller.io.rxd := io.rxd

controller.io.countDone := counter.io.done

counter.io.enable := controller.io.enable

shiftRegister.io.enable := controller.io.enable

shiftRegister.io.serialIn := io.rxd

  
  // OUTPUTS
  

  io.data := shiftRegister.io.parallelOut

  io.valid := controller.io.valid 

  
  // DEBUG PRINT
  

  printf(p"RXD=${io.rxd} DATA=${io.data} VALID=${io.valid}\n")
}