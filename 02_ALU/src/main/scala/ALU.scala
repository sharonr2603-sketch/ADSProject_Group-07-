// ADS I Class Project
// Assignment 02: Arithmetic Logic Unit and UVM Testbench
//
// Chair of Electronic Design Automation, RPTU University Kaiserslautern-Landau
// File created on 09/21/2025 by Tharindu Samarakoon (gug75kex@rptu.de)
// File updated on 10/29/2025 by Tobias Jauch (tobias.jauch@rptu.de)

package Assignment02

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

// ALU operation encoding
object AluOp extends ChiselEnum {
  val ADD, SUB, AND, OR, XOR,
      SLL, SRL, SRA,
      SLT, SLTU,
      PASSB = Value
}

class ALU extends Module {

  val io = IO(new Bundle {
    val operandA  = Input(UInt(32.W))
    val operandB  = Input(UInt(32.W))
    val operation = Input(AluOp())

    val aluResult = Output(UInt(32.W))
  })

  // RV32I shift amount uses only lower 5 bits of operandB
  val shiftAmount = io.operandB(4, 0)

  // Default output for safety
  io.aluResult := 0.U(32.W)

  switch(io.operation) {

    is(AluOp.ADD) {
      io.aluResult := io.operandA + io.operandB
    }

    is(AluOp.SUB) {
      io.aluResult := io.operandA - io.operandB
    }

    is(AluOp.AND) {
      io.aluResult := io.operandA & io.operandB
    }

    is(AluOp.OR) {
      io.aluResult := io.operandA | io.operandB
    }

    is(AluOp.XOR) {
      io.aluResult := io.operandA ^ io.operandB
    }

    is(AluOp.SLL) {
      io.aluResult := io.operandA << shiftAmount
    }

    is(AluOp.SRL) {
      io.aluResult := io.operandA >> shiftAmount
    }

    is(AluOp.SRA) {
      io.aluResult := (io.operandA.asSInt >> shiftAmount).asUInt
    }

    is(AluOp.SLT) {
      io.aluResult := Mux(
        io.operandA.asSInt < io.operandB.asSInt,
        1.U(32.W),
        0.U(32.W)
      )
    }

    is(AluOp.SLTU) {
      io.aluResult := Mux(
        io.operandA < io.operandB,
        1.U(32.W),
        0.U(32.W)
      )
    }

    is(AluOp.PASSB) {
      io.aluResult := io.operandB
    }
  }
}