// ADS I Class Project
// Pipelined RISC-V Core - ID Stage
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/09/2026 by Tobias Jauch (@tojauch)

/*
Instruction Decode (ID) Stage: decoding and operand fetch

Extracted Fields from 32-bit Instruction (see RISC-V specification for reference):
    opcode: instruction format identifier
    funct3: selects variant within instruction format
    funct7: further specifies operation type (R-type only)
    rd: destination register address
    rs1: first source register address
    rs2: second source register address
    imm: 12-bit immediate value (I-type, sign-extended)

Register File Interfaces:
    regFileReq_A, regFileResp_A: read port for rs1 operand
    regFileReq_B, regFileResp_B: read port for rs2 operand

Internal Signals:
    Combinational decoders for instructions

Functionality:
    Decode opcode to determine instruction and identify operation (ADD, SUB, XOR, ...)
    Handle flushes due to mispredicted branches

Outputs:
    uop: micro-operation code (identifies instruction type)
    rd: destination register index
    operandA: first operand
    operandB: second operand 
    XcptInvalid: exception flag for invalid instructions
*/

package core_tile

import chisel3._
import chisel3.util._
import uopc._

// -----------------------------------------
// Decode Stage
// -----------------------------------------

//ToDo: Add your implementation according to the specification above here 

class ID extends Module {
  val io = IO(new Bundle {
    val instr = Input(UInt(32.W))

    val regFileReq_A  = Output(new regFileReadReq)
    val regFileResp_A = Input(new regFileReadResp)

    val regFileReq_B  = Output(new regFileReadReq)
    val regFileResp_B = Input(new regFileReadResp)

    val uop = Output(uopc())
    val rd = Output(UInt(5.W))
    val rs1 = Output(UInt(5.W))
    val rs2 = Output(UInt(5.W))

    val operandA = Output(UInt(32.W))
    val operandB = Output(UInt(32.W))
    val XcptInvalid = Output(Bool())
  })

  val opcode = io.instr(6, 0)
  val rd     = io.instr(11, 7)
  val funct3 = io.instr(14, 12)
  val rs1    = io.instr(19, 15)
  val rs2    = io.instr(24, 20)
  val funct7 = io.instr(31, 25)

  val immI = Cat(Fill(20, io.instr(31)), io.instr(31, 20))

  io.regFileReq_A.addr := rs1
  io.regFileReq_B.addr := rs2

  io.uop := NOP
  io.rd := rd
  io.rs1 := rs1
  io.rs2 := rs2
  io.operandA := io.regFileResp_A.data
  io.operandB := io.regFileResp_B.data
  io.XcptInvalid := false.B

  val validInstr = WireDefault(false.B)

  when(opcode === "b0110011".U) {

    switch(Cat(funct7, funct3)) {
      is("b0000000000".U) { io.uop := ADD;  validInstr := true.B }
      is("b0100000000".U) { io.uop := SUB;  validInstr := true.B }
      is("b0000000001".U) { io.uop := SLL;  validInstr := true.B }
      is("b0000000010".U) { io.uop := SLT;  validInstr := true.B }
      is("b0000000011".U) { io.uop := SLTU; validInstr := true.B }
      is("b0000000100".U) { io.uop := XOR;  validInstr := true.B }
      is("b0000000101".U) { io.uop := SRL;  validInstr := true.B }
      is("b0100000101".U) { io.uop := SRA;  validInstr := true.B }
      is("b0000000110".U) { io.uop := OR;   validInstr := true.B }
      is("b0000000111".U) { io.uop := AND;  validInstr := true.B }
    }

  }.elsewhen(opcode === "b0010011".U) {
    io.operandB := immI

    switch(funct3) {
      is("b000".U) { io.uop := ADDI;  validInstr := true.B }
      is("b010".U) { io.uop := SLTI;  validInstr := true.B }
      is("b011".U) { io.uop := SLTUI; validInstr := true.B }
      is("b100".U) { io.uop := XORI;  validInstr := true.B }
      is("b110".U) { io.uop := ORI;   validInstr := true.B }
      is("b111".U) { io.uop := ANDI;  validInstr := true.B }

      is("b001".U) {
        when(funct7 === "b0000000".U) {
          io.uop := SLLI
          validInstr := true.B
        }
      }

      is("b101".U) {
        when(funct7 === "b0000000".U) {
          io.uop := SRLI
          validInstr := true.B
        }.elsewhen(funct7 === "b0100000".U) {
          io.uop := SRAI
          validInstr := true.B
        }
      }
    }
  }

  when(!validInstr && io.instr =/= 0.U && io.instr =/= "h00000013".U) {
    io.XcptInvalid := true.B
  }
}
